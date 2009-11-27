/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.databinding.hessian.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.format.BaseParameterEncoder;
import org.fabric3.spi.binding.format.EncoderException;

/**
 * ParameterEncoder that uses Hessian.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HessianParameterEncoder extends BaseParameterEncoder {
    private SerializerFactory factory;

    /**
     * Constructor.
     *
     * @param loader the classloader for deserializing parameter and fault types. Hessian requires this classloader to be set as the TCCL.
     */
    public HessianParameterEncoder(ClassLoader loader) {
        super(loader);
        factory = new SerializerFactory();
        // add custom serializers
        factory.addFactory(new QNameSerializerFactory());
    }


    protected byte[] serialize(Object o) throws EncoderException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(bos);
            out.setSerializerFactory(factory);
            out.startMessage();
            out.writeObject(o);
            out.completeMessage();
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }


    protected <T> T deserialize(Class<T> clazz, byte[] bytes, ClassLoader classLoader) throws EncoderException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            InputStream is = new ByteArrayInputStream(bytes);
            Thread.currentThread().setContextClassLoader(classLoader);
            Hessian2Input in = new Hessian2Input(is);
            in.setSerializerFactory(factory);
            in.startMessage();
            Object ret = in.readObject(clazz);
            in.completeMessage();
            in.close();
            return clazz.cast(ret);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}