package org.fabric3.transform.binary;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.SingleTypeTransformer;

/**
 * A no-op transformer used to by-pass conversions of byte arrays. This implementation is useful when bytes are sent directly by an application over a
 * binary transport.
 */
public class ByteArrayToByteArrayTransformer implements SingleTypeTransformer<byte[], byte[]> {
    private static final JavaType TYPE = new JavaType(byte[].class);

    public DataType getSourceType() {
        return TYPE;
    }

    public DataType getTargetType() {
        return TYPE;
    }

    public byte[] transform(byte[] data, ClassLoader loader) throws Fabric3Exception {
        return data;
    }

}
