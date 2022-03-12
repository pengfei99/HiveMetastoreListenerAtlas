package org.pengfei.hive.listener;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class HiveMetaProducer {


    private final String brokerUrl;
    private final String topicName;
    private final String ackMode;
    private final long bufferMemorySize = 33554432;
    private final String clientId;
    private final int batchSize;
    private String compressionType = "snappy";

    private KafkaProducer producer;


    public HiveMetaProducer(String brokerUrl, String topicName, String clientId, String ackMode, int batchSize) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.ackMode = ackMode;
        this.clientId = clientId;
        this.batchSize = batchSize;
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemorySize);
        config.put(ProducerConfig.ACKS_CONFIG, ackMode);
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

        // config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, clientId);
        // set a 3s timeout for each transaction
        //config.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 300);

        this.producer = new KafkaProducer<>(config);
    }


    /**
     * This method send message inside a transaction
     */
    public void sendMsg(String key, String message) {

        // init transaction
        // producer.initTransactions();

        // start transaction
        // producer.beginTransaction();
        // try to send message and commitTransaction
//        try {
//
//
//
//
//           // producer.commitTransaction();
//        }
//        // if encounter exception during the transaction, abort, rollback to init stat
//        // all messages in this transaction received by the broker will be dropped
//        catch (Exception e) {
//            producer.abortTransaction();
//        }
        producer.send(new ProducerRecord(topicName, key, message));
    }

    public void close() {
        this.producer.close();
    }

    public static void main(String[] args) {
        String clientId = "toto";
        String kafkaBrokerUrl = System.getenv("KAFKA_BROKER_URL");
        String kafkaTopicName = System.getenv("KAFKA_TOPIC_NAME");
        if (kafkaBrokerUrl == null) {
            kafkaBrokerUrl = "pengfei.org:9092";
            //throw new IllegalArgumentIOException("environment variable KAFKA_BROKER_URL is required");
        }
        if (kafkaTopicName == null) {
            kafkaTopicName = "hive-meta";
            //throw new IllegalArgumentIOException("environment variable KAFKA_TOPIC_NAME is required");
        }
        HiveMetaProducer producer = new HiveMetaProducer(kafkaBrokerUrl, kafkaTopicName, clientId, "-1", 323840);

        producer.sendMsg("key", "message_test");
        producer.close();
    }


}