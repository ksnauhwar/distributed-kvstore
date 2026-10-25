package org.distributed.kvstore.common.messages;

public abstract class RawMessage {
    protected final byte[] key;
    protected final byte[] value;
    protected static int MAGIC_NUMBER_LENGTH = 4;
    protected static int MAGIC_NUMBER = 123;
    protected static int VERSION_LENGTH = 4;
    protected static int VERSION = 0;
    protected static int OPERATION_LENGTH = 4;
    protected static int KEY_LENGTH = 4;
    protected static int VALUE_LENGTH = 4;
    protected static int PAYLOAD_LENGTH = 4;

    public RawMessage(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public abstract byte[] serialize();

    public abstract RawMessage deSerialize(byte[] message);

    protected abstract int payloadSize();
}
