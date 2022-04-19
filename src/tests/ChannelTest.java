import static org.junit.Assert.*;
import org.apache.kafka.common.memory.MemoryPool;
import org.apache.kafka.common.network.ByteBufferSend;
import org.apache.kafka.common.network.ChannelMetadataRegistry;
import org.apache.kafka.common.network.NetworkSend;
import org.apache.kafka.common.network.TransportLayer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChannelTest {
    Random rd = new Random();
    @Test
    public void testSending() throws IOException {
        TransportLayer transport = Mockito.mock(TransportLayer.class);
        MemoryPool pool = Mockito.mock(MemoryPool.class);
        ChannelMetadataRegistry metadataRegistry = Mockito.mock(ChannelMetadataRegistry.class);

        MyKafkaChannel channel = new MyKafkaChannel("0", transport, 1024, pool, metadataRegistry);
        byte[] arr = new byte[128];
        rd.nextBytes(arr);
        System.out.println("send "+ arr);
        ByteBufferSend send = ByteBufferSend.sizePrefixed(ByteBuffer.wrap(arr));
        NetworkSend networkSend = new NetworkSend("0", send);

        channel.setSend(networkSend);
        assertTrue(channel.hasSend());
        assertThrows(IllegalStateException.class, () -> channel.setSend(networkSend));

        Mockito.when(transport.write(Mockito.any(ByteBuffer[].class))).thenReturn(4L);
        assertEquals(4L, channel.write());
        assertEquals(128, send.remaining());
        assertNull(channel.maybeCompleteSend());

        Mockito.when(transport.write(Mockito.any(ByteBuffer[].class))).thenReturn(64L);
        assertEquals(64, channel.write());
        assertEquals(64, send.remaining());
        assertNull(channel.maybeCompleteSend());

        Mockito.when(transport.write(Mockito.any(ByteBuffer[].class))).thenReturn(64L);
        assertEquals(64, channel.write());
        assertEquals(0, send.remaining());
        assertEquals(networkSend, channel.maybeCompleteSend());
    }

    @Test
    public void testReceiving() throws IOException {
        TransportLayer transport = Mockito.mock(TransportLayer.class);
        MemoryPool pool = Mockito.mock(MemoryPool.class);
        ChannelMetadataRegistry metadataRegistry = Mockito.mock(ChannelMetadataRegistry.class);

        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.when(pool.tryAllocate(sizeCaptor.capture())).thenAnswer(invocation -> {
            return ByteBuffer.allocate(sizeCaptor.getValue());
        });

        MyKafkaChannel channel = new MyKafkaChannel("0", transport, 1024, pool, metadataRegistry);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        Mockito.when(transport.read(bufferCaptor.capture())).thenAnswer(invocation -> {
            bufferCaptor.getValue().putInt(128);
            return 4;
        }).thenReturn(0);
        assertEquals(4, channel.read());
        assertEquals(4, channel.currentReceive().bytesRead());
        assertNull(channel.maybeCompleteReceive());

        Mockito.reset(transport);
        Random rd = new Random();
        byte[] arr = new byte[64];
        rd.nextBytes(arr);
        Mockito.when(transport.read(bufferCaptor.capture())).thenAnswer(invocation -> {
            bufferCaptor.getValue().put(arr);
            return 64;
        });
        assertEquals(64, channel.read());
        assertEquals(68, channel.currentReceive().bytesRead());
        assertNull(channel.maybeCompleteReceive());

        Mockito.reset(transport);
        byte[] arr2 = new byte[64];
        rd.nextBytes(arr2);
        Mockito.when(transport.read(bufferCaptor.capture())).thenAnswer(invocation -> {
            bufferCaptor.getValue().put(arr2);
            return 64;
        });
        assertEquals(64, channel.read());
        assertEquals(132, channel.currentReceive().bytesRead());
        assertNotNull(channel.maybeCompleteReceive());
        assertNull(channel.currentReceive());
    }

}