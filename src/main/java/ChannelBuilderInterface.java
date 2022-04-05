import org.apache.kafka.common.Configurable;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.memory.MemoryPool;
import org.apache.kafka.common.network.ChannelMetadataRegistry;
import org.apache.kafka.common.security.auth.KafkaPrincipal;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ChannelBuilderInterface extends AutoCloseable, Configurable {
    KafkaChannel buildChannel(String id, SelectionKey key, int maxReceiveSize, MemoryPool memoryPool, ChannelMetadataRegistry metadataRegistry) throws KafkaException , IOException;
    @Override
    void close();

}
