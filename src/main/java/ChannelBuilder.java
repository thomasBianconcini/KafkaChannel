import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.memory.MemoryPool;
import org.apache.kafka.common.network.ChannelMetadataRegistry;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.network.PlaintextTransportLayer;
import org.apache.kafka.common.network.TransportLayer;

public class ChannelBuilder implements ChannelBuilderInterface  {
    private final ListenerName listenerName;
    private Map<String, ?> configs;
    public ChannelBuilder(ListenerName listenerName) {
        this.listenerName = listenerName;
    }
    @Override
    public MyKafkaChannel buildChannel(String id, SelectionKey key, int maxReceiveSize, MemoryPool memoryPool, ChannelMetadataRegistry metadataRegistry) throws KafkaException, IOException {
        try {
            PlaintextTransportLayer transportLayer = buildTransportLayer(key);
            return buildChannel(id, transportLayer, maxReceiveSize, memoryPool != null ? memoryPool : MemoryPool.NONE, metadataRegistry);
        } catch (Exception e) {
            throw new KafkaException(e);
        }
    }
    protected PlaintextTransportLayer buildTransportLayer(SelectionKey key) throws IOException {
        return new PlaintextTransportLayer(key);
    }
    MyKafkaChannel buildChannel(String id, TransportLayer transportLayer, int maxReceiveSize, MemoryPool memoryPool, ChannelMetadataRegistry metadataRegistry) {
        return new MyKafkaChannel(id, transportLayer, maxReceiveSize, memoryPool, metadataRegistry);
    }
    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}