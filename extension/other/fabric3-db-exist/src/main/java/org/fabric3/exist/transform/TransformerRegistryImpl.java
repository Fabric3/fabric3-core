/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.transform;

import java.util.HashMap;
import java.util.Map;

/** */
public class TransformerRegistryImpl implements TransformerRegistry {

    final Map<Class, Transformer> transforms = new HashMap();
    final Map<Integer, Transformer> valueTransforms = new HashMap();

    public void register(Class clazz,Transformer transformer) {
        transforms.put(clazz, transformer);
    }

    public void unregister(Class clazz,Transformer transformer) {
        transforms.remove(clazz);
    }
    
      public void register(int type,Transformer transformer) {
        valueTransforms.put(type, transformer);
    }

    public void unregister(int type,Transformer transformer) {
        valueTransforms.remove(type);
    }

    public Transformer getTransformer(Class source) {
        Transformer transformer = transforms.get(source);
        if (transformer != null) {
            return transformer;
        }

        for (Class clazz : source.getInterfaces()) {
            transformer = transforms.get(clazz);
            if (transformer != null) {
                return transformer;
            }
        }

        if (source.isArray()){
            return transforms.get(Object[].class);
        }

        return getTransformer(source.getSuperclass());
    }

    public Transformer getTransformer(int type) {
        Transformer transformer = valueTransforms.get(type);
        if (transformer == null) {
            transformer = transforms.get(Object.class);
        }
        return transformer;
    }
}
