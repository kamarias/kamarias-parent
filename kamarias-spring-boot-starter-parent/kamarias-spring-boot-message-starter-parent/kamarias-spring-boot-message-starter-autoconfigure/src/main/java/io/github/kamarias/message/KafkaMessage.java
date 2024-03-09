package io.github.kamarias.message;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;

import java.io.Serializable;
import java.util.Optional;

import static org.apache.kafka.clients.consumer.ConsumerRecord.NO_TIMESTAMP;
import static org.apache.kafka.clients.consumer.ConsumerRecord.NULL_SIZE;

/**
 * kafka消息
 * @author wangyuxing@gogpay.cn
 * @date 2024/3/7 23:33
 */
public class KafkaMessage<K,V> implements Serializable {

    /**
     * 消息主题
     */
    private final String topic;

    /**
     * 消息分区
     */
    private final int partition;

    /**
     * 消息偏移量
     */
    private final long offset;

    /**
     * 消息时间戳
     */
    private final long timestamp;

    /**
     * 消息时间类型
     */
    private final TimestampType timestampType;

    /**
     * 序列化、未压缩的密钥的大小（以字节为单位）。如果 key 为 null，则返回的大小为 -1。
     */
    private final int serializedKeySize;

    /**
     * 序列化、未压缩的值的大小（以字节为单位）。如果 value 为 null，则返回的大小为 -1。
     */
    private final int serializedValueSize;

    /**
     * 请求头
     */
    private final Headers headers;


    /**
     * 消息key
     */
    private final K key;

    /**
     * 消息值
     */
    private final V value;

    /**
     * 获取领导者纪元作为记录（如果可用）
     */
    private final Optional<Integer> leaderEpoch;


    public KafkaMessage(String topic,
                        int partition,
                        long offset,
                        K key,
                        V value) {
        this(topic, partition, offset, NO_TIMESTAMP, TimestampType.NO_TIMESTAMP_TYPE, NULL_SIZE, NULL_SIZE, key, value,
                new RecordHeaders(), Optional.empty());
    }


    public KafkaMessage(String topic,
                        int partition,
                        long offset,
                        long timestamp,
                        TimestampType timestampType,
                        int serializedKeySize,
                        int serializedValueSize,
                        K key,
                        V value,
                        Headers headers,
                        Optional<Integer> leaderEpoch) {
        if (topic == null) {
            throw new IllegalArgumentException("Topic cannot be null");
        }
        if (headers == null) {
            throw new IllegalArgumentException("Headers cannot be null");
        }

        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.timestampType = timestampType;
        this.serializedKeySize = serializedKeySize;
        this.serializedValueSize = serializedValueSize;
        this.key = key;
        this.value = value;
        this.headers = headers;
        this.leaderEpoch = leaderEpoch;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TimestampType getTimestampType() {
        return timestampType;
    }

    public int getSerializedKeySize() {
        return serializedKeySize;
    }

    public int getSerializedValueSize() {
        return serializedValueSize;
    }

    public Headers getHeaders() {
        return headers;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public Optional<Integer> getLeaderEpoch() {
        return leaderEpoch;
    }

}
