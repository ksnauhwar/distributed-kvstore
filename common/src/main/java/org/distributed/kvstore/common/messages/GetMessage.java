package org.distributed.kvstore.common.messages;

import java.io.*;

public class GetMessage extends RawMessage {
    public GetMessage(byte[] key) {
        super(key,null);
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);) {
            dataOutputStream.writeInt(payloadSize());
            dataOutputStream.writeInt(MAGIC_NUMBER);
            dataOutputStream.writeInt(VERSION);
            dataOutputStream.writeInt(OperationType.GET.operationCode());
            dataOutputStream.writeInt(key.length);
            dataOutputStream.write(key);
            dataOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public RawMessage deSerialize(byte[] message) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
             DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {
            dataInputStream.skipBytes(PAYLOAD_LENGTH);
            int magicNumber = dataInputStream.readInt();
            if (magicNumber != MAGIC_NUMBER) throw new RuntimeException("The message comes from an untrusted source");
            dataInputStream.skipBytes(VERSION_LENGTH);
            dataInputStream.skipBytes(OPERATION_LENGTH);
            int keyLength = dataInputStream.readInt();
            byte[] key = new byte[keyLength];
            dataInputStream.readFully(key);
            return new GetMessage(key);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected int payloadSize() {
        return PAYLOAD_LENGTH + MAGIC_NUMBER_LENGTH + VERSION_LENGTH + OPERATION_LENGTH + KEY_LENGTH + key.length;
    }
}
