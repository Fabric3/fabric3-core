/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.monitor.impl.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.event.ParameterEntry;
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
import static org.fabric3.api.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * Performs bytecode generation at runtime to create a monitor proxy.
 * <p/>
 * The monitor proxy avoids object creation such as auto-boxing and varargs for highly performant environments. This is done by dynamically generating
 * writeParameters method with a specific number of arguments for each proxy interface method. The implementation of the proxy interface method invokes this
 * writeParameters method. Performance characteristics should therefore be the same as hand-implementing the proxy interface.
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
 *                entry.setTemplate(currentMessage);
 *                entry.setTimestamp(System.currentTimeMillis);
 *
 *                writeParameters(arg1, arg2,[...other arguments], entry);
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
 *    private int writeParameters(String template, [Type]arg1, [Type]arg2, ..., MonitorEventEntry entry) {
 *
 *        buffer.limit(numberArgs);
 *
 *        entry.getParameterEntries[0].setXXXX(arg1);    // set argument 1
 *        .... // set the other arguments
 *
 *    }
 * </code>
 * </pre>
 */
@Service(MonitorProxyServiceExtension.class)
public class BytecodeMonitorProxyService extends AbstractMonitorProxyService implements Opcodes {
    public static final String PARAM_ENTRY = Type.getInternalName(ParameterEntry.class);
    public static final String MONITOR_EVENT_ENTRY = Type.getInternalName(MonitorEventEntry.class);
    public static final String ABSTRACT_MONITOR_HANDLER = Type.getInternalName(AbstractMonitorHandler.class);
    public static final String DISPATCH_INFO = Type.getInternalName(DispatchInfo.class);
    public static final String MONITOR_LEVEL = Type.getInternalName(MonitorLevel.class);
    public static final String DESTINATION_ROUTER = Type.getInternalName(RingBufferDestinationRouter.class);

    public BytecodeMonitorProxyService(@Reference RingBufferDestinationRouter router, @Reference Monitorable monitorable) {
        super(router, monitorable);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException {
        if (destination == null) {
            destination = DEFAULT_DESTINATION;
        }

        String proxyClassName = type.getName().replace("$", "") + "_Proxy";
        int destinationIndex = router.getDestinationIndex(destination);
        ClassLoader loader = type.getClassLoader();

        Map<Method, DispatchInfo> levels = new LinkedHashMap<>();
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            DispatchInfo info = createDispatchInfo(type, loader, method);
            levels.put(method, info);
        }
        byte[] classBytes = generateClass(type, ClassWriter.COMPUTE_FRAMES);

        BytecodeClassLoader bytecodeClassLoader = getClassLoader(type);

        Class<?> clazz = bytecodeClassLoader.defineClass(proxyClassName, classBytes);
        try {
            Collection<DispatchInfo> values = levels.values();
            DispatchInfo[] infos = values.toArray(new DispatchInfo[values.size()]);

            AbstractMonitorHandler handler = (AbstractMonitorHandler) clazz.getConstructor().newInstance();
            handler.init(destinationIndex, monitorable, router, infos, enabled);
            return type.cast(handler);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new MonitorCreationException(e);
        }
    }

    /**
     * Performs the actual bytecode generation to implement the given interface
     *
     * @param type  the interface to implement
     * @param flags the ClassWriter flags
     * @return the generated bytecode
     */
    <T> byte[] generateClass(Class<T> type, int flags) {
        String proxyClassNameInternal = Type.getInternalName(type).replace("$", "") + "_Proxy";
        String proxyClassDescriptor = "L" + proxyClassNameInternal + ";";

        String interfazeName = Type.getInternalName(type);

        ClassWriter cw = new ClassWriter(flags);
        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, proxyClassNameInternal, null, ABSTRACT_MONITOR_HANDLER, new String[]{interfazeName});

        cw.visitSource(type.getName() + "Proxy.java", null);
        if (type.isLocalClass()) {
            String enclosingName = Type.getInternalName(type.getEnclosingClass());

            cw.visitInnerClass(interfazeName, enclosingName, type.getSimpleName(), ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);
        }

        writeConstructor(cw, proxyClassDescriptor);

        Method[] methods = type.getMethods();
        int index = 0;
        for (Method method : methods) {
            String signature = calculateWriteParametersSignature(method);

            Class<?>[] parameterTypes = method.getParameterTypes();
            generateMethod(cw, method, index, proxyClassNameInternal, signature);
            writeGenerateParametersMethod(cw, index, signature, parameterTypes);
            index++;
        }

        cw.visitEnd();

        return cw.toByteArray();
    }

    /**
     * Implements a monitor interface method.
     *
     * @param cw                       the class writer
     * @param index                    the method index
     * @param proxyClassNameInternal   the parameter signature
     * @param writeParametersSignature the parameter types
     */
    private void generateMethod(ClassWriter cw, Method method, int index, String proxyClassNameInternal, String writeParametersSignature) {
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

        // lookup the DispatchInfo based on the index for the method
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "infos", "[L" + DISPATCH_INFO + ";");
        mv.visitVarInsn(ILOAD, varIndexPosition);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, varDispatchInfoPosition);
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitLineNumber(70, l11);
        mv.visitVarInsn(ALOAD, varDispatchInfoPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, DISPATCH_INFO, "getLevel", "()L" + MONITOR_LEVEL + ";");
        mv.visitVarInsn(ASTORE, varCurrentLevelPosition);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitLineNumber(71, l12);
        mv.visitVarInsn(ALOAD, varDispatchInfoPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, DISPATCH_INFO, "getMessage", "()Ljava/lang/String;");
        mv.visitVarInsn(ASTORE, varCurrentMessagePosition);

        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        Label l13 = new Label();
        mv.visitJumpInsn(IFNULL, l13);
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_LEVEL, "intValue", "()I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "monitorable", "Lorg/fabric3/api/host/monitor/Monitorable;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/fabric3/api/host/monitor/Monitorable", "getLevel", "()L" + MONITOR_LEVEL + ";");
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_LEVEL, "intValue", "()I");
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
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "asyncEnabled", "Z");
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
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "router", "L" + DESTINATION_ROUTER + ";");
        mv.visitMethodInsn(INVOKEINTERFACE, DESTINATION_ROUTER, "get", "()L" + MONITOR_EVENT_ENTRY + ";");
        mv.visitVarInsn(ASTORE, varEntryPosition);
        Label l19 = new Label();
        mv.visitLabel(l19);
        mv.visitLineNumber(83, l19);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "destinationIndex", "I");
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "setDestinationIndex", "(I)V");
        Label l20 = new Label();
        mv.visitLabel(l20);
        mv.visitLineNumber(84, l20);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitVarInsn(LLOAD, varStartPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "setTimestampNanos", "(J)V");
        Label l21 = new Label();
        mv.visitLabel(l21);
        mv.visitLineNumber(85, l21);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "getBuffer", "()Lorg/fabric3/monitor/spi/buffer/ResizableByteBuffer;");
        mv.visitVarInsn(ASTORE, varBufferPosition);

        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitVarInsn(ALOAD, varCurrentMessagePosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "setTemplate", "(Ljava/lang/String;)V");

        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");

        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "setEntryTimestamp", "(J)V");

        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "setLevel", "(L" + MONITOR_LEVEL + ";)V");

        Label l22 = new Label();
        mv.visitLabel(l22);
        mv.visitLineNumber(87, l22);

        mv.visitVarInsn(ALOAD, 0);

        // Load the method arguments onto the stack. Note that we access the method arguments using i+1 since the 0 position is used by "this" (params begin
        // at 1).
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType.isPrimitive()) {
                if (Integer.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else if (Long.TYPE.equals(paramType)) {
                    mv.visitVarInsn(LLOAD, i + 1);
                } else if (Double.TYPE.equals(paramType)) {
                    mv.visitVarInsn(DLOAD, i + 1);
                } else if (Boolean.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else if (Float.TYPE.equals(paramType)) {
                    mv.visitVarInsn(FLOAD, i + 1);
                } else if (Short.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else if (Byte.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else if (Character.TYPE.equals(paramType)) {
                    mv.visitVarInsn(ILOAD, i + 1);
                } else {
                    throw new AssertionError("Unhandled type: " + paramType);
                }

            } else {
                mv.visitVarInsn(ALOAD, i + 1);
            }
        }

        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitMethodInsn(INVOKESPECIAL, proxyClassNameInternal, "writeParameters" + index, writeParametersSignature);

        Label l24 = new Label();
        mv.visitLabel(l24);
        mv.visitLineNumber(90, l24);

        mv.visitLabel(l1);
        mv.visitLineNumber(95, l1);
        mv.visitVarInsn(ALOAD, varEntryPosition);
        Label l27 = new Label();
        mv.visitJumpInsn(IFNULL, l27);
        Label l28 = new Label();
        mv.visitLabel(l28);
        mv.visitLineNumber(96, l28);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "router", "L" + DESTINATION_ROUTER + ";");
        mv.visitVarInsn(ALOAD, varEntryPosition);
        mv.visitMethodInsn(INVOKEINTERFACE, DESTINATION_ROUTER, "publish", "(L" + MONITOR_EVENT_ENTRY + ";)V");
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
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "router", "L" + DESTINATION_ROUTER + ";");
        mv.visitVarInsn(ALOAD, varArgsPosition);
        mv.visitMethodInsn(INVOKEINTERFACE, DESTINATION_ROUTER, "publish", "(L" + MONITOR_EVENT_ENTRY + ";)V");
        mv.visitLabel(l29);
        mv.visitVarInsn(ALOAD, 13);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l27);
        mv.visitLineNumber(99, l27);
        Label l31 = new Label();
        mv.visitJumpInsn(GOTO, l31);
        mv.visitLabel(l16);
        mv.visitLineNumber(100, l16);
        pushInteger(numParams, mv);
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
                } else if (Double.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(DLOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                } else if (Float.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(FLOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                } else if (Boolean.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(ILOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Z)Ljava/lang/Boolean;");
                } else if (Short.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(ILOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                } else if (Byte.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(ILOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                } else if (Character.TYPE.equals(paramTypes[i])) {
                    mv.visitVarInsn(ILOAD, i + 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
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
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "router", "L" + DESTINATION_ROUTER + ";");
        mv.visitVarInsn(ALOAD, varCurrentLevelPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "destinationIndex", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "runtimeName", "Ljava/lang/String;");
        mv.visitVarInsn(LLOAD, varTimestampPosition);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ABSTRACT_MONITOR_HANDLER, "source", "Ljava/lang/String;");
        mv.visitVarInsn(ALOAD, varCurrentMessagePosition);
        mv.visitVarInsn(ALOAD, varArgsPosition);
        mv.visitMethodInsn(INVOKEINTERFACE,
                           DESTINATION_ROUTER,
                           "send",
                           "(Lorg/fabric3/api/annotation/monitor/MonitorLevel;ILjava/lang/String;JLjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V");
        mv.visitLabel(l31);
        mv.visitLineNumber(106, l31);
        mv.visitInsn(RETURN);

        Label methodEnd = new Label();
        mv.visitLabel(methodEnd);

        mv.visitLocalVariable("this", "Lorg/fabric3/monitor/impl/proxy/AbstractMonitorHandler;", null, l4, methodEnd, 0);

        // Load the method params as local variables. Note the index starts at 1 since 0 is reserved for "this".
        for (int i = 1; i <= numParams; i++) {
            Class<?> paramType = paramTypes[i - 1];
            if (String.class.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/String;", null, l4, methodEnd, i);
            } else if (Integer.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "I", null, l4, methodEnd, i);
            } else if (Long.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "J", null, l4, methodEnd, i);
            } else if (Double.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "D", null, l4, methodEnd, i);
            } else if (Boolean.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Z", null, l4, methodEnd, i);
            } else if (Float.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "F", null, l4, methodEnd, i);
            } else if (Short.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "S", null, l4, methodEnd, i);
            } else if (Byte.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "B", null, l4, methodEnd, i);
            } else if (Character.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "C", null, l4, methodEnd, i);
            } else if (paramType.isPrimitive()) {
                throw new AssertionError("Unhandled type: " + paramType);
            } else {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/Object;", null, l4, methodEnd, i);
            }

        }

        mv.visitLocalVariable("index", "I", null, l5, methodEnd, varIndexPosition);

        mv.visitLocalVariable("currentLevel", "L" + MONITOR_LEVEL + ";", null, l12, methodEnd, varCurrentLevelPosition);
        mv.visitLocalVariable("currentMessage", "Ljava/lang/String;", null, l12, methodEnd, varCurrentMessagePosition);
        mv.visitLocalVariable("timestamp", "J", null, l15, methodEnd, varTimestampPosition);

        mv.visitLocalVariable("info", "L" + DISPATCH_INFO + ";", null, l11, l12, varDispatchInfoPosition);

        mv.visitLocalVariable("entry", "L" + MONITOR_EVENT_ENTRY + ";", null, l0, l27, varEntryPosition);
        mv.visitLocalVariable("args", "[Ljava/lang/Object;", null, l32, l31, varArgsPosition);

        mv.visitLocalVariable("start", "J", null, l18, l1, varStartPosition);
        mv.visitLocalVariable("buffer", "Lorg/fabric3/monitor/spi/buffer/ResizableByteBuffer;", null, l22, l1, varBufferPosition);

        mv.visitMaxs(9, 14);
        mv.visitEnd();
    }

    /**
     * Creates the writeParameters method. The method signature will take the same arguments as the proxy interface method that it is to be invoked from.
     *
     * @param cw         the class writer
     * @param index      the method index
     * @param signature  the parameter signature
     * @param paramTypes the parameter types
     */
    private void writeGenerateParametersMethod(ClassWriter cw, int index, String signature, Class<?>[] paramTypes) {
        int varMethodArgOffset = 1;

        int offset = calculateParameterSpace(paramTypes);

        int varEntryPosition = offset + 1;
        int varNumberArgsPosition = varEntryPosition + 1;
        int varParamEntryPosition = varNumberArgsPosition + 1;
        int varIPosition = varParamEntryPosition + 1;

        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "writeParameters" + index, signature, null, null);

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

        mv.visitVarInsn(ALOAD, varEntryPosition);   //set the param entry limit
        mv.visitVarInsn(ILOAD, varNumberArgsPosition);
        mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "setLimit", "(I)V");

        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLineNumber(112, l2);

        // Setup i variable for the for loop and then iterate until the number of arguments is reached
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, varIPosition);

        Label l3 = new Label();
        mv.visitLabel(l3);

        // jump if i (specified by the for loop) is greater than the number of arguments
        mv.visitVarInsn(ILOAD, varIPosition);
        mv.visitVarInsn(ILOAD, varNumberArgsPosition);
        Label l4 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, l4);

        Label endIf = new Label();
        for (int i = 0; i < paramTypes.length; i++) {
            // load ring buffer entry
            mv.visitVarInsn(ALOAD, varEntryPosition);
            mv.visitMethodInsn(INVOKEVIRTUAL, MONITOR_EVENT_ENTRY, "getEntries", "()[L" + PARAM_ENTRY + ";");
            pushInteger(i, mv);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ASTORE, varParamEntryPosition);
            mv.visitVarInsn(ALOAD, varParamEntryPosition);

            Class<?> paramType = paramTypes[i];
            if (Character.TYPE.equals(paramType)) {
                // load method parameter
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setCharValue", "(C)V");
            } else if (Integer.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setIntValue", "(I)V");
            } else if (Long.TYPE.equals(paramType)) {
                mv.visitVarInsn(LLOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setLongValue", "(J)V");
            } else if (Double.TYPE.equals(paramType)) {
                mv.visitVarInsn(DLOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setDoubleValue", "(D)V");
            } else if (Boolean.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setBooleanValue", "(Z)V");
            } else if (Float.TYPE.equals(paramType)) {
                mv.visitVarInsn(FLOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setFloatValue", "(F)V");
            } else if (Short.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setShortValue", "(S)V");
            } else if (Byte.TYPE.equals(paramType)) {
                mv.visitVarInsn(ILOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setByteValue", "(B)V");
            } else if (Object.class.isAssignableFrom(paramType)) {
                mv.visitVarInsn(ALOAD, varMethodArgOffset + i);
                mv.visitMethodInsn(INVOKEVIRTUAL, PARAM_ENTRY, "setObjectValue", "(Ljava/lang/Object;)V");
            } else {
                throw new AssertionError("Unhandled type: " + paramType);
            }
        }
        mv.visitLabel(endIf);
        mv.visitLineNumber(121, endIf);

        // increment i and counter, then loop
        mv.visitIincInsn(varIPosition, 1);
        mv.visitJumpInsn(GOTO, l3);
        // end of for loop

        mv.visitLabel(l4);

        mv.visitInsn(RETURN);
        Label endMethod = new Label();
        mv.visitLabel(endMethod);

        mv.visitLocalVariable("this", "L" + ABSTRACT_MONITOR_HANDLER + ";", null, l0, endMethod, 0);

        for (int i = 1; i <= paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i - 1];
            if (Integer.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "I", null, l0, endMethod, i + 1);
            } else if (Long.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "J", null, l0, endMethod, i + 1);
            } else if (Double.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "D", null, l0, endMethod, i + 1);
            } else if (Boolean.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "Z", null, l0, endMethod, i + 1);
            } else if (Float.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "F", null, l0, endMethod, i + 1);
            } else if (Short.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "S", null, l0, endMethod, i + 1);
            } else if (Byte.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "B", null, l0, endMethod, i + 1);
            } else if (Character.TYPE.equals(paramType)) {
                mv.visitLocalVariable("arg" + i, "C", null, l0, endMethod, i + 1);
            } else if (paramType.isPrimitive()) {
                throw new AssertionError("Unhandled type");
            } else {
                mv.visitLocalVariable("arg" + i, "Ljava/lang/Object;", null, l0, endMethod, i + 1);
            }
        }

        mv.visitLocalVariable("entry", "L" + MONITOR_EVENT_ENTRY + ";", null, l0, endMethod, varEntryPosition);
        mv.visitLocalVariable("numberArgs", "I", null, l1, endMethod, varNumberArgsPosition);
        mv.visitLocalVariable("current", "L" + PARAM_ENTRY + ";", null, l2, endMethod, varParamEntryPosition);
        mv.visitLocalVariable("i", "I", null, l2, endMethod, varIPosition);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * Creates a writeParameters method based on the number of arguments for the proxy interface method in the form:
     * <pre>
     *      void writeParameters(String template, <type> arg1, <type> arg2, ...<type> argN, MonitorEventEntry entry)
     * </pre>
     * The method sets the parameters on the MonitorEventEntry instance.
     *
     * @param method the proxy interface method
     * @return the signature
     */
    private String calculateWriteParametersSignature(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        StringBuilder paramSignature = new StringBuilder("(");
        for (Class<?> paramType : paramTypes) {
            paramSignature.append(Type.getDescriptor(paramType));
        }
        paramSignature.append("L").append(MONITOR_EVENT_ENTRY).append(";)V");
        return paramSignature.toString();
    }

    private void writeConstructor(ClassWriter cw, String proxyClassDescriptor) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(56, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_MONITOR_HANDLER, "<init>", "()V");
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

    /**
     * Returns a classloader for loading the proxy class, creating one if necessary.
     *
     * @param type the type to generate a classloader for
     * @return the classloader
     */
    private BytecodeClassLoader getClassLoader(Class<?> type) {
        ClassLoader parent = type.getClassLoader();
        ClassLoader extensionClassLoader = getClass().getClassLoader();
        BytecodeClassLoader classLoader = new BytecodeClassLoader(URI.create("BytecodeClassLoader"), parent);
        classLoader.addParent(extensionClassLoader); // proxy classes need to be visible as well
        return classLoader;
    }

}
