package org.distributed.kvstore.common.messages;

import java.io.*;

public class PutMessage extends RawMessage {

    public PutMessage(byte[] key, byte[] value) {
        super(key,value);
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);) {
                dataOutputStream.writeInt(payloadSize());
                dataOutputStream.writeInt(MAGIC_NUMBER);
                dataOutputStream.writeInt(VERSION);
                dataOutputStream.writeInt(OperationType.PUT.operationCode());
                dataOutputStream.writeInt(key.length);
                dataOutputStream.write(key);
                dataOutputStream.writeInt(value.length);
                dataOutputStream.write(value);
                dataOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int payloadSize() {
        return  PAYLOAD_LENGTH + MAGIC_NUMBER_LENGTH + VERSION_LENGTH + OPERATION_LENGTH + KEY_LENGTH + key.length + VALUE_LENGTH + value.length;
    }

    @Override
    public RawMessage deSerialize(byte[] message) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {
            dataInputStream.skipBytes(PAYLOAD_LENGTH);
            int magicNumber = dataInputStream.readInt();
            if (magicNumber != MAGIC_NUMBER) throw new RuntimeException("The message comes from an untrusted source");

            dataInputStream.skipBytes(VERSION_LENGTH);
            int operation = dataInputStream.readInt();
            if (operation != OperationType.PUT.operationCode()) throw new RuntimeException("The message cannot be deserialized");
            int keyLength = dataInputStream.readInt();
            byte[] key = new byte[keyLength];
            dataInputStream.readFully(key);

            int valueLength = dataInputStream.readInt();
            byte[] value = new byte[valueLength];
            dataInputStream.readFully(value);
            return new PutMessage(key,value);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
