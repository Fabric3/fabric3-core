/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.monitor.impl.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.spi.classloader.BytecodeClassLoader;
import org.fabric3.spi.monitor.DispatchInfo;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.fabric3.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * Performs bytecode generation at runtime to create a monitor proxy.
 * <p/>
 * The monitor proxy avoids object creation such as auto-boxing and varargs for highly performant environments. This is done by dynamically generating
 * writeTemplate method with a specific number of arguments for each proxy interface method. The implementation of the proxy interface method invokes this
 * writeTemplate method. Performance characteristics should therefore be the same as hand-implementing the proxy interface.
 * <p/>
 * As a further optimization, the {@link DispatchInfo} for an invoked proxy method will be looked up in an array based on the method index. This will be
 * noticeably faster than looking up the DispatchInfo in a Map keyed by Method as required by JDK proxies.
 * <p/>
 * The implementation creates code similar to the following:
 * <p/>
 * <pre>
 * <code>
 *      public void invoke([Type]arg1, [Type]arg2...) throws Throwable {
 *        int index = 1;
 *        MonitorLevel currentLevel;
 *        String currentMessage;
 *        if (level != null) {
 *            currentLevel = level;
 *            currentMessage = template;
 *        } else {
 *            DispatchInfo info = infos[index];
 *            currentLevel = info.getLevel();
 *            currentMessage = info.getMessage();
 *        }
 *        if (currentLevel == null || currentLevel.intValue() < monitorable.getLevel().intValue()) {
 *            // monitoring is off
 *            return;
 *        }
 *        long timestamp = System.currentTimeMillis();
 *        if (asyncEnabled) {
 *            MonitorEventEntry entry = null;
 *            try {
 *                long start = System.nanoTime();
 *                entry = router.get();
 *                entry.setDestinationIndex(destinationIndex);
 *                entry.setTimestampNanos(start);
 *                ByteBuffer buffer = entry.getBuffer();
 *
 *                int bytesWritten = MonitorEntryWriter.writePrefix(currentLevel, timestamp, buffer, timestampWriter);
 *
 *                bytesWritten = bytesWritten + writeTemplate(template, arg1, arg2,[...other arguments],buffer);
 *                buffer.put(NEWLINE);
 *                bytesWritten++;
 *                buffer.limit(bytesWritten);
 *
 *            } finally {
 *                if (entry != null) {
 *                    router.publish(entry);
 *                }
 *            }
 *        } else {
 *            Object[] args = new Object[..];
 *            args[0] = arg1;
 *            args[1] = arg2;
 *            // ... load other arguments in the array
 *            router.send(currentLevel, destinationIndex, runtimeName, timestamp, source, currentMessage, args);
 *        }
 *
 *    }
 *
 *    private int writeTemplate(String template, [Type]arg1, [Type]arg2, ...,ByteBuffer buffer) {
 *        int numberArgs =...;  // set to the number of arguments
 *        if (template == null) {
 *            return 0;
 *        }
 *
 *        int bytesWritten = 0;
 *        int counter = 0;
 *        for (int i = 0; i < template.length(); i++) {
 *            char current = template.charAt(i);
 *            if ('{' == current) {
 *                if (counter >= numberArgs) {
 *                    throw new ServiceRuntimeException("Monitor message contains more parameters than are supplied by the method interface: " + template);
 *                }
 *                if (counter == 1) {
 *                    bytesWritten = bytesWritten + CharSequenceWriter.write(arg1, buffer);
 *               } else if (counter ==....){
 *                    bytesWritten = bytesWritten + IntWriter.write(arg2, buffer);
 *                }
 *                i = i + 2;    // skip two places
 *                counter++;
 *            } else {
 *                bytesWritten++;
 *                buffer.put((byte) current);
 *            }
 *        } return bytesWritten;
 *    }
 * </code>
 * </pre>
 */
@Service(MonitorProxyServiceExtension.class)
public class BytecodeMonitorProxyService extends AbstractMonitorProxyService implements Opcodes {
    private static final String HANDLER_NAME = Type.getInternalName(AbstractMonitorHandler.class);

    public BytecodeMonitorProxyService(@Reference RingBufferDestinationRouter router, @Reference Monitorable monitorable, @Reference HostInfo info) {
        super(router, monitorable, info);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException {
        if (destination == null) {
            destination = DEFAULT_DESTINATION;
        }

        String proxyClassName = type.getName().replace("$", "") + "_Proxy";
        String proxyClassNameInternal = Type.getInternalName(type).replace("$", "") + "_Proxy";
        String proxyClassDescriptor = "L" + proxyClassNameInternal + ";";

        String interfazeName = Type.getInternalName(type);

        int destinationIndex = router.getDestinationIndex(destination);
        ClassLoader loader = type.getClassLoader();

        Map<Method, DispatchInfo> levels = new LinkedHashMap<Method, DispatchInfo>();
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            DispatchInfo info = createDispatchInfo(type, loader, method);
            levels.put(method, info);
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, proxyClassNameInternal, null, HANDLER_NAME, new String[]{interfazeName});

        cw.visitSource(type.getName() + "Proxy.java", null);
        if (type.isLocalClass()) {
            String enclosingName = Type.getInternalName(type.getEnclosingClass());

            cw.visitInnerClass(interfazeName, enclosingName, type.getSimpleName(), ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);
        }

        writeConstructor(cw, proxyClassDescriptor);

        int index = 0;
        for (Method method : methods) {
            String signature = calculateWriteTemplateSignature(method);

            Class<?>[] parameterTypes = method.getParameterTypes();
            writeMethod(cw, method, index, proxyClassNameInternal, signature);
            writeTemplateMethod(cw, index, signature, parameterTypes);
            index++;
        }

        cw.visitEnd();

        byte[] classBytes = cw.toByteArray();
        BytecodeClassLoader bytecodeClassLoader = new BytecodeClassLoader(URI.create("test"), getClass().getClassLoader());
        Class<?> clazz = bytecodeClassLoader.defineClass(proxyClassName, classBytes);
        try {
            Collection<DispatchInfo> values = levels.values();
            DispatchInfo[] infos = values.toArray(new DispatchInfo[values.size()]);

            AbstractMonitorHandler handler = (AbstractMonitorHandler) clazz.getConstructor().newInstance();
            handler.init(destinationIndex, runtimeName, monitorable, router, infos, timestampWriter, enabled);
            return type.cast(handler);
        } catch (InvocationTargetException e) {
            throw new MonitorCreationException(e);
        } catch (NoSuchMethodException e) {
            throw new MonitorCreationException(e);
        } catch (InstantiationException e) {
            throw new MonitorCreationException(e);
        } catch (IllegalAccessException e) {
            throw new MonitorCreationException(e);
        }
    }

    /**
     * Implements a monitor interface method.
     *
     * @param cw                     the class writer
     * @param index                  the method index
     * @param proxyClassNameInternal the parameter signature
     * @param writeTemplateSignature the parameter types
     */
    private void writeMethod(ClassWriter cw, Method method, int index, String proxyClassNameInternal, String writeTemplateSignature) {
        String methodSignature = Type.getMethodDescriptor(method);

        // calculate the position of local variables. Per the JVM spec, pos 0 is reserved for a reference to "this"
        Class<?>[] paramTypes = method.getParameterTypes();
        int numParams = paramTypes.length;

        int offset = calculateParameterSpace(paramTypes);

        // calculate position of local variables
        int varIndexPosition = offset + 1;         // pos of the index variable used for looking up the DispatchInfo
        int varCurrentLevelPosition = varIndexPosition + 1;
        int varCurrentMessagePosition = varCurrentLevelPosition + 1;
        int varTimestampPosition = varCurrentMessagePosition + 1;
        int varDispatchInfoPosition = varCurrentMessagePosition + 1;  // Note this is the same as varTimestampPos since there is an if

        int varEntryPosition = varTimestampPosition + 2; // Note +2
        int varArgsPosition = varTimestampPosition + 2; // Note +2 and the same as varEntryPosition since there is an if

        int varStartPosition = varEntryPosition + 1;
        int varBufferPosition = varStartPosition + 2;
        int varBytesWrittenPosition = varBufferPosition + 1;

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), methodSignature, null, null);

        mv.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, null);
        Label l3 = new Label();
        mv.visitTryCatchBlock(l2, l3, l2, null);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLineNumber(62, l4);

        // set the index var used to lookup the DispatchInfo. The array of DispatchInfo objects correspond to the ordering of Methods in the proxy interface.
        pushInteger(index, mv);
        mv.visitVarInsn(ISTORE, varIndexPosition);

        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLineNumber(65, l5);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "level", "Lorg/fabric3/api/annotation/monitor/MonitorLevel;");
        Label l6 = new Label();
        mv.visitJumpInsn(IFNULL, l6);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitLineNumber(66, l7);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "level", "Lorg/fabric3/api/annotation/monitor/MonitorLevel;");
        mv.visitVarInsn(ASTORE, varCurrentLevelPosition);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitLineNumber(67, l8);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "template", "Ljava/lang/String;");
        mv.visitVarInsn(ASTORE, varCurrentMessagePosition);
        Label l9 = new Label();
        mv.visitLabel(l9);
        Label l10 = new Label();
        mv.visitJumpInsn(GOTO, l10);
        mv.visitLabel(l6);
        mv.visitLineNumber(69, l6);

        // lookup the DispatchInfo based on the index for the method
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "infos", "[Lorg/fabric3/spi/monitor/DispatchInfo;");
        mv.visitVarInsn(ILOAD, varIndexPosition);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, varDispatchInfoPosition);
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitLineNumber(70, l11);
        mv.visitVarInsn(ALOAD, varDispatchInfoPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/spi/monitor/DispatchInfo", "getLevel", "()Lorg/fabric3/api/annotation/monitor/MonitorLevel;");
        mv.visitVarInsn(ASTORE, varCurrentLevelPosition);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitLineNumber(71, l12);
        mv.visitVarInsn(ALOAD, varDispatchInfoPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/spi/monitor/DispatchInfo", "getMessage", "()Ljava/lang/String;");
        mv.visitVarInsn(ASTORE, varCurrentMessagePosition);
        mv.visitLabel(l10);
        mv.visitLineNumber(73, l10);
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        Label l13 = new Label();
        mv.visitJumpInsn(IFNULL, l13);
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/api/annotation/monitor/MonitorLevel", "intValue", "()I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "monitorable", "Lorg/fabric3/host/monitor/Monitorable;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/fabric3/host/monitor/Monitorable", "getLevel", "()Lorg/fabric3/api/annotation/monitor/MonitorLevel;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/api/annotation/monitor/MonitorLevel", "intValue", "()I");
        Label l14 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, l14);
        mv.visitLabel(l13);
        mv.visitLineNumber(75, l13);
        mv.visitInsn(RETURN);
        mv.visitLabel(l14);
        mv.visitLineNumber(77, l14);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
        mv.visitVarInsn(LSTORE, varTimestampPosition);
        Label l15 = new Label();
        mv.visitLabel(l15);
        mv.visitLineNumber(78, l15);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "asyncEnabled", "Z");
        Label l16 = new Label();
        mv.visitJumpInsn(IFEQ, l16);
        Label l17 = new Label();
        mv.visitLabel(l17);
        mv.visitLineNumber(79, l17);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, varArgsPosition);
        mv.visitLabel(l0);
        mv.visitLineNumber(81, l0);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
        mv.visitVarInsn(LSTORE, varStartPosition);
        Label l18 = new Label();
        mv.visitLabel(l18);
        mv.visitLineNumber(82, l18);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD,
                          "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler",
                          "router",
                          "Lorg/fabric3/monitor/impl/router/RingBufferDestinationRouter;");
        mv.visitMethodInsn(INVOKEINTERFACE,
                           "org/fabric3/monitor/impl/router/RingBufferDestinationRouter",
                           "get",
                           "()Lorg/fabric3/monitor/impl/router/MonitorEventEntry;");
        mv.visitVarInsn(ASTORE, varEntryPosition);
        Label l19 = new Label();
        mv.visitLabel(l19);
        mv.visitLineNumber(83, l19);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "destinationIndex", "I");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/monitor/impl/router/MonitorEventEntry", "setDestinationIndex", "(I)V");
        Label l20 = new Label();
        mv.visitLabel(l20);
        mv.visitLineNumber(84, l20);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitVarInsn(LLOAD, varStartPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/monitor/impl/router/MonitorEventEntry", "setTimestampNanos", "(J)V");
        Label l21 = new Label();
        mv.visitLabel(l21);
        mv.visitLineNumber(85, l21);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fabric3/monitor/impl/router/MonitorEventEntry", "getBuffer", "()Ljava/nio/ByteBuffer;");
        mv.visitVarInsn(ASTORE, varBufferPosition);
        Label l22 = new Label();
        mv.visitLabel(l22);
        mv.visitLineNumber(87, l22);
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        mv.visitVarInsn(LLOAD, varTimestampPosition);
        mv.visitVarInsn(ALOAD, varBufferPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD,
                          "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler",
                          "timestampWriter",
                          "Lorg/fabric3/monitor/impl/writer/TimestampWriter;");
        mv.visitMethodInsn(INVOKESTATIC,
                           "org/fabric3/monitor/impl/writer/MonitorEntryWriter",
                           "writePrefix",
                           "(Lorg/fabric3/api/annotation/monitor/MonitorLevel;JLjava/nio/ByteBuffer;Lorg/fabric3/monitor/impl/writer/TimestampWriter;)I");
        mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
        Label l23 = new Label();
        mv.visitLabel(l23);
        mv.visitLineNumber(89, l23);
        mv.visitVarInsn(ILOAD, varBytesWrittenPosition);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, varCurrentMessagePosition);

        // Load the method arguments onto the stack. Note that we access the method arguments using i+1 since the 0 position is used by "this" (params begin
        // at 1).
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType.isPrimitive()) {
                if (Integer.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else if (Long.TYPE.equals(paramType)) {
                    mv.visitVarInsn(LLOAD, i + 1);
                } else if (Boolean.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else if (Float.TYPE.equals(paramType)) {
                    mv.visitVarInsn(FLOAD, i + 1);
                } else {
                    throw new AssertionError("Unhandled type: " + paramType);
                }

            } else {
                mv.visitVarInsn(ALOAD, i + 1);
            }
        }

        mv.visitVarInsn(ALOAD, varBufferPosition);
        mv.visitMethodInsn(INVOKESPECIAL, proxyClassNameInternal, "writeTemplate" + index, writeTemplateSignature);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
        Label l24 = new Label();
        mv.visitLabel(l24);
        mv.visitLineNumber(90, l24);
        mv.visitVarInsn(ALOAD, varBufferPosition);
        mv.visitFieldInsn(GETSTATIC, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "NEWLINE", "[B");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "put", "([B)Ljava/nio/ByteBuffer;");
        mv.visitInsn(POP);
        Label l25 = new Label();
        mv.visitLabel(l25);
        mv.visitLineNumber(91, l25);
        mv.visitIincInsn(varBytesWrittenPosition, 1);
        Label l26 = new Label();
        mv.visitLabel(l26);
        mv.visitLineNumber(92, l26);
        mv.visitVarInsn(ALOAD, varBufferPosition);
        mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "limit", "(I)Ljava/nio/Buffer;");
        mv.visitInsn(POP);
        mv.visitLabel(l1);
        mv.visitLineNumber(95, l1);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        Label l27 = new Label();
        mv.visitJumpInsn(IFNULL, l27);
        Label l28 = new Label();
        mv.visitLabel(l28);
        mv.visitLineNumber(96, l28);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD,
                          "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler",
                          "router",
                          "Lorg/fabric3/monitor/impl/router/RingBufferDestinationRouter;");
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitMethodInsn(INVOKEINTERFACE,
                           "org/fabric3/monitor/impl/router/RingBufferDestinationRouter",
                           "publish",
                           "(Lorg/fabric3/monitor/impl/router/MonitorEventEntry;)V");
        mv.visitJumpInsn(GOTO, l27);
        mv.visitLabel(l2);
        mv.visitLineNumber(95, l2);
        mv.visitVarInsn(ASTORE, 13);
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        Label l29 = new Label();
        mv.visitJumpInsn(IFNULL, l29);
        Label l30 = new Label();
        mv.visitLabel(l30);
        mv.visitLineNumber(96, l30);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD,
                          "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler",
                          "router",
                          "Lorg/fabric3/monitor/impl/router/RingBufferDestinationRouter;");
        mv.visitVarInsn(ALOAD, varArgsPosition);
        mv.visitMethodInsn(INVOKEINTERFACE,
                           "org/fabric3/monitor/impl/router/RingBufferDestinationRouter",
                           "publish",
                           "(Lorg/fabric3/monitor/impl/router/MonitorEventEntry;)V");
        mv.visitLabel(l29);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l27);
        mv.visitLineNumber(99, l27);
        Label l31 = new Label();
        mv.visitJumpInsn(GOTO, l31);
        mv.visitLabel(l16);
        mv.visitLineNumber(100, l16);
        pushInteger(numParams, mv); //xcv replaced below
        //mv.visitInsn(numParams); // xcv
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitVarInsn(ASTORE, varArgsPosition);
        Label l32 = new Label();
        mv.visitLabel(l32);
        mv.visitLineNumber(101, l32);

        for (int i = 0; i < paramTypes.length; i++) {
            mv.visitVarInsn(ALOAD, varArgsPosition);
            pushInteger(i, mv);

            if (paramTypes[i].isPrimitive()) {
                // i+1 since that is the position of the method argument (position 0 is reserved for "this")
                if (Integer.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(ILOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                } else if (Long.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(LLOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                } else if (Float.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(FLOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                } else if (Boolean.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(ILOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Z)Ljava/lang/Boolean;");
                }
            } else {
                mv.visitVarInsn(ALOAD, i + 1);  // i+1 since that is the position of the method argument (position 0 is reserved for "this")
            }
            mv.visitInsn(AASTORE);
        }

        Label l34 = new Label();
        mv.visitLabel(l34);
        mv.visitLineNumber(103, l34);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD,
                          "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler",
                          "router",
                          "Lorg/fabric3/monitor/impl/router/RingBufferDestinationRouter;");
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "destinationIndex", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "runtimeName", "Ljava/lang/String;");
        mv.visitVarInsn(LLOAD, varTimestampPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/fabric3/monitor/impl/proxy/AbstractMonitorHandler", "source", "Ljava/lang/String;");
        mv.visitVarInsn(ALOAD, varCurrentMessagePosition);
        mv.visitVarInsn(ALOAD, varArgsPosition);
        mv.visitMethodInsn(INVOKEINTERFACE,
                           "org/fabric3/monitor/impl/router/RingBufferDestinationRouter",
                           "send",
                           "(Lorg/fabric3/api/annotation/monitor/MonitorLevel;ILjava/lang/String;JLjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V");
        mv.visitLabel(l31);
        mv.visitLineNumber(106, l31);
        mv.visitInsn(RETURN);
        Label l35 = new Label();
        mv.visitLabel(l35);

        mv.visitLocalVariable("this", "Lorg/fabric3/monitor/impl/proxy/AbstractMonitorHandler;", null, l4, l35, 0);

        // Load the method params as local variables. Note the index starts at 1 since 0 is reserved for "this".
        for (int i = 1; i <= numParams; i++) {
            Class<?> paramType = paramTypes[i - 1];
            if (String.class.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/String;", null, l4, l35, i);
            } else if (Integer.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "I", null, l4, l35, i);
            } else if (Long.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "J", null, l4, l35, i);
            } else if (Boolean.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Z", null, l4, l35, i);
            } else if (Float.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "F", null, l4, l35, i);
            } else if (paramType.isPrimitive()) {
                throw new AssertionError("Unhandled type: " + paramType);
            } else {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/Object;", null, l4, l35, i);
            }

        }

        mv.visitLocalVariable("index", "I", null, l5, l35, varIndexPosition);

        mv.visitLocalVariable("currentLevel", "Lorg/fabric3/api/annotation/monitor/MonitorLevel;", null, l12, l35, varCurrentLevelPosition);
        mv.visitLocalVariable("currentMessage", "Ljava/lang/String;", null, l10, l35, varCurrentMessagePosition);
        mv.visitLocalVariable("timestamp", "J", null, l15, l35, varTimestampPosition);

        mv.visitLocalVariable("currentLevel", "Lorg/fabric3/api/annotation/monitor/MonitorLevel;", null, l8, l6, varCurrentLevelPosition);
        mv.visitLocalVariable("currentMessage", "Ljava/lang/String;", null, l9, l6, varCurrentMessagePosition);
        mv.visitLocalVariable("info", "Lorg/fabric3/spi/monitor/DispatchInfo;", null, l11, l10, varDispatchInfoPosition);

        mv.visitLocalVariable("entry", "Lorg/fabric3/monitor/impl/router/MonitorEventEntry;", null, l0, l27, varEntryPosition);
        mv.visitLocalVariable("args", "[Ljava/lang/Object;", null, l32, l31, varArgsPosition);

        mv.visitLocalVariable("start", "J", null, l18, l1, varStartPosition);
        mv.visitLocalVariable("buffer", "Ljava/nio/ByteBuffer;", null, l22, l1, varBufferPosition);
        mv.visitLocalVariable("bytesWritten", "I", null, l23, l1, varBytesWrittenPosition);

        mv.visitMaxs(9, 14);
        mv.visitEnd();
    }

    /**
     * Creates the writeTemplate method. The method signature will take the same arguments as the proxy interface method that it is to be invoked from.
     *
     * @param cw         the class writer
     * @param index      the method index
     * @param signature  the parameter signature
     * @param paramTypes the parameter types
     */
    private void writeTemplateMethod(ClassWriter cw, int index, String signature, Class<?>[] paramTypes) {
        int varTemplatePosition = 1; // Position 0 is reserved for "this"
        int varMethodArgOffset = varTemplatePosition + 1;                        //2

        int offset = calculateParameterSpace(paramTypes);

        int varBufferPosition = varTemplatePosition + offset + 1;     //5
        int varNumberArgsPosition = varBufferPosition + 1;                       //6
        int varBytesWrittenPosition = varNumberArgsPosition + 1;                 //7
        int varCounterPosition = varBytesWrittenPosition + 1;                    //8
        int varIPosition = varCounterPosition + 1;                               //9
        int varCurrentPosition = varIPosition + 1;                               //10

        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "writeTemplate" + index, signature, null, null);

        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(103, l0);

        // set the number of arguments for this method
        pushInteger(paramTypes.length, mv);
        mv.visitVarInsn(ISTORE, varNumberArgsPosition);

        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLineNumber(104, l1);
        mv.visitVarInsn(ALOAD, varTemplatePosition);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNONNULL, l2);

        // if the template is null, return 0
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(105, l3);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);

        mv.visitLabel(l2);
        mv.visitLineNumber(108, l2);

        // set bytesWritten to 0
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, varBytesWrittenPosition);

        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLineNumber(109, l4);

        // set counter to 0
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, varCounterPosition);

        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLineNumber(110, l5);

        // for the length of the template, write its bytes or perform argument substitution to the byte buffer
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, varIPosition);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitVarInsn(ILOAD, varIPosition);
        mv.visitVarInsn(ALOAD, varTemplatePosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I");
        Label l7 = new Label();
        // jump if i (specified by the for loop) is greater than or equal to the template length
        mv.visitJumpInsn(IF_ICMPGE, l7);

        // get the next character in the template and load it on the stack
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitLineNumber(111, l8);
        mv.visitVarInsn(ALOAD, varTemplatePosition);
        mv.visitVarInsn(ILOAD, varIPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C");
        mv.visitVarInsn(ISTORE, varCurrentPosition);

        // compare the character to '{'
        Label l9 = new Label();
        mv.visitLabel(l9);
        mv.visitLineNumber(112, l9);
        mv.visitIntInsn(BIPUSH, 123);   // '{' char
        mv.visitVarInsn(ILOAD, varCurrentPosition);
        Label l10 = new Label();

        // if the character is not equal to '{', jump
        mv.visitJumpInsn(IF_ICMPNE, l10);

        // character equal to '{' - perform substitution with the next argument
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitLineNumber(113, l11);

        // if counter is greater or equal to the number of method params, throw an exception
        mv.visitVarInsn(ILOAD, varCounterPosition);
        mv.visitVarInsn(ILOAD, varNumberArgsPosition);
        Label l12 = new Label();
        mv.visitJumpInsn(IF_ICMPLT, l12);
        Label l13 = new Label();
        mv.visitLabel(l13);
        mv.visitLineNumber(114, l13);
        writeThrowTemplateException(varTemplatePosition, mv);

        // counter less than number of method params
        mv.visitLabel(l12);
        mv.visitLineNumber(116, l12);

        // Generate code that performs the actual substitution with the current argument. The generated code contains an if..else block that looks up the
        // current argument based on the counter number and loads it on the stack from its local variable position. After it is loaded, a writer is called to
        // put its contents into the byte buffer.

        Label endIf = new Label();
        for (int i = 0; i < paramTypes.length; i++) {
            // load the counter
            mv.visitVarInsn(ILOAD, varCounterPosition);

            // load the current argument number
            pushInteger(i, mv);
            Label label = new Label();
            // compare the counter to the argument number, if it is equal write the argument corresponding to the number to the buffer
            mv.visitJumpInsn(IF_ICMPNE, label);

            Class<?> paramType = paramTypes[i];
            if (CharSequence.class.isAssignableFrom(paramType)) {
                mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
                mv.visitVarInsn(ALOAD, varMethodArgOffset + i);  // Load the current method param
                mv.visitVarInsn(ALOAD, varBufferPosition);
                mv.visitMethodInsn(INVOKESTATIC,
                                   "org/fabric3/monitor/impl/writer/CharSequenceWriter",
                                   "write",
                                   "(Ljava/lang/CharSequence;Ljava/nio/ByteBuffer;)I");
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
            } else if (Integer.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitVarInsn(ALOAD, varBufferPosition);
                mv.visitMethodInsn(INVOKESTATIC, "org/fabric3/monitor/impl/writer/IntWriter", "write", "(ILjava/nio/ByteBuffer;)I");
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
            } else if (Long.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
                mv.visitVarInsn(LLOAD, varMethodArgOffset + i);
                mv.visitVarInsn(ALOAD, varBufferPosition);
                mv.visitMethodInsn(INVOKESTATIC, "org/fabric3/monitor/impl/writer/LongWriter", "write", "(JLjava/nio/ByteBuffer;)I");
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
            } else if (Boolean.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitVarInsn(ALOAD, varBufferPosition);
                mv.visitMethodInsn(INVOKESTATIC, "org/fabric3/monitor/impl/writer/BooleanWriter", "write", "(ZLjava/nio/ByteBuffer;)I");
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
            } else if (Float.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
                mv.visitVarInsn(FLOAD, varMethodArgOffset + i);
                mv.visitVarInsn(ALOAD, varBufferPosition);
                mv.visitMethodInsn(INVOKESTATIC, "org/fabric3/monitor/impl/writer/FloatWriter", "write", "(FLjava/nio/ByteBuffer;)I");
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, varBytesWrittenPosition);
            } else if (Object.class.isAssignableFrom(paramType)) {
                mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
                mv.visitVarInsn(ALOAD, varMethodArgOffset + i);  // Load the current method param
                mv.visitVarInsn(ALOAD, varBufferPosition);
                mv.visitMethodInsn(INVOKESTATIC, "org/fabric3/monitor/impl/writer/ObjectWriter", "write", "(Ljava/lang/Object;Ljava/nio/ByteBuffer;)I");
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, varBytesWrittenPosition);

            } else if (paramType.isPrimitive()) {
                throw new AssertionError("Unhandled type: " + paramType);
            }

            mv.visitLabel(label);
        }
        mv.visitLabel(endIf);
        mv.visitLineNumber(121, endIf);

        mv.visitVarInsn(ILOAD, varIPosition); // xcv 8
        mv.visitInsn(ICONST_2);   // skip 2 places for {0}
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, varIPosition);  // xcv 8

        Label l17 = new Label();
        mv.visitLabel(l17);
        mv.visitLineNumber(122, l17);

        mv.visitIincInsn(7, varTemplatePosition);
        Label l18 = new Label();
        mv.visitJumpInsn(GOTO, l18);

        // current not equal to '{'  - increment bytesWritten and write the current character to the byte buffer
        mv.visitLabel(l10);
        mv.visitLineNumber(124, l10);
        //        mv.visitIincInsn(6, varTemplatePosition);
        mv.visitIincInsn(varBytesWrittenPosition, 1);
        Label l19 = new Label();
        mv.visitLabel(l19);

        mv.visitLineNumber(125, l19);
        mv.visitVarInsn(ALOAD, varBufferPosition);
        mv.visitVarInsn(ILOAD, varCurrentPosition);
        mv.visitInsn(I2B);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "put", "(B)Ljava/nio/ByteBuffer;");
        mv.visitInsn(POP);
        mv.visitLabel(l18);

        // loop
        mv.visitLineNumber(110, l18);
        mv.visitIincInsn(varIPosition, 1);   //xcv 8
        mv.visitJumpInsn(GOTO, l6);

        // return bytesWritten
        mv.visitLabel(l7);
        mv.visitLineNumber(128, l7);
        mv.visitVarInsn(ILOAD, varBytesWrittenPosition);
        mv.visitInsn(IRETURN);
        Label l20 = new Label();
        mv.visitLabel(l20);

        mv.visitLocalVariable("this", "Lorg/fabric3/monitor/impl/proxy/AbstractMonitorHandler;", null, l0, l20, 0);
        mv.visitLocalVariable("template", "Ljava/lang/String;", null, l0, l20, varTemplatePosition);

        for (int i = 1; i <= paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i - 1];
            if (String.class.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/String;", null, l0, l20, i + 1);
            } else if (Integer.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "I", null, l0, l20, i + 1);
            } else if (Long.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "J", null, l0, l20, i + 1);
            } else if (Boolean.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Z", null, l0, l20, i + 1);
            } else if (Float.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "F", null, l0, l20, i + 1);
            } else if (paramType.isPrimitive()) {
                throw new AssertionError("Unhandled type");
            } else {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/Object;", null, l0, l20, i + 1);
            }
        }

        mv.visitLocalVariable("buffer", "Ljava/nio/ByteBuffer;", null, l0, l20, varBufferPosition);
        mv.visitLocalVariable("numberArgs", "I", null, l1, l20, varNumberArgsPosition);
        mv.visitLocalVariable("bytesWritten", "I", null, l4, l20, varBytesWrittenPosition);
        mv.visitLocalVariable("counter", "I", null, l5, l20, varCounterPosition);
        mv.visitLocalVariable("i", "I", null, l6, l7, varIPosition);
        mv.visitLocalVariable("current", "C", null, l9, l18, varCurrentPosition);

        mv.visitMaxs(4, 10);
        mv.visitEnd();

    }

    /**
     * Creates a writeTemplate method based on the number of arguments for the proxy interface method in the form:
     * <pre>
     *      int writeTemplate(String template, <type> arg1, <type> arg2, ...<type> argN, ByteBuffer buffer)
     * </pre>
     *
     * @param method the proxy interface method
     * @return the signature
     */
    private String calculateWriteTemplateSignature(Method method) {
        // create a writeTemplate method based on the number of arguments for the proxy interface method in the form:
        // int writeTemplate(String template, <type> arg1, <type> arg2, ...<type> argN, ByteBuffer buffer)
        Class<?>[] paramTypes = method.getParameterTypes();
        StringBuilder paramSignature = new StringBuilder("(Ljava/lang/String;");
        for (Class<?> paramType : paramTypes) {
            paramSignature.append(Type.getDescriptor(paramType));
        }
        paramSignature.append("Ljava/nio/ByteBuffer;)I");
        return paramSignature.toString();
    }

    private void writeConstructor(ClassWriter cw, String proxyClassDescriptor) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(56, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, HANDLER_NAME, "<init>", "()V");
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", proxyClassDescriptor, null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * Pushes an integer onto the stack
     *
     * @param value the value to push
     * @param mv    the method visitor
     */
    private void pushInteger(int value, MethodVisitor mv) {
        if (value == 0) {
            mv.visitInsn(ICONST_0);
        } else if (value == 1) {
            mv.visitInsn(ICONST_1);
        } else if (value == 2) {
            mv.visitInsn(ICONST_2);
        } else if (value == 3) {
            mv.visitInsn(ICONST_3);
        } else if (value == 4) {
            mv.visitInsn(ICONST_4);
        } else if (value == 5) {
            mv.visitInsn(ICONST_5);
        } else {
            mv.visitIntInsn(BIPUSH, value);
        }
    }

    /**
     * Writes bytecode that throws a ServiceRuntimeException if the template contains more substitution variables than the number of method parameters.
     *
     * @param varTemplatePosition the register position of the template string
     * @param mv                  the method visitor
     */
    private void writeThrowTemplateException(int varTemplatePosition, MethodVisitor mv) {
        mv.visitTypeInsn(NEW, "org/oasisopen/sca/ServiceRuntimeException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
        mv.visitLdcInsn("Monitor message contains more parameters than are supplied by the method interface: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitVarInsn(ALOAD, varTemplatePosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "org/oasisopen/sca/ServiceRuntimeException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
    }

    /**
     * Calculates the stack space needed by the given parameters. Doubles and Longs occupy two slots; other types occupy one spot.
     *
     * @param paramTypes the parameter types
     * @return the stack space
     */
    private int calculateParameterSpace(Class<?>[] paramTypes) {
        int offset = 0;
        for (Class<?> paramType : paramTypes) {
            if (Double.TYPE.equals(paramType) || Long.TYPE.equals(paramType)) {
                offset = offset + 2;
            } else {
                offset++;
            }
        }
        return offset;
    }


}
