package org.distributed.kvstore.common.messages;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GetMessageTest {

    @Test
    public void getMessageSerialization() {
        byte[] key = "distributed".getBytes(Charset.defaultCharset());
        GetMessage message = new GetMessage(key);
        assertEquals(31, message.serialize().length);
    }

    @Test
    public void deSerializationToGetMessageType() {
        byte[] key = "distributed".getBytes(Charset.defaultCharset());
        GetMessage message = new GetMessage(key);
        RawMessage deserializedMessage = message.deSerialize(message.serialize());
        assertInstanceOf(GetMessage.class,deserializedMessage);
    }


}
