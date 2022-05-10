package org.pengfei.hive.listener;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.hive.metastore.events.AlterTableEvent;
import org.apache.hadoop.hive.metastore.MetaStoreEventListener;
import org.apache.hadoop.hive.metastore.events.CreateTableEvent;

import org.apache.hadoop.hive.metastore.events.DropTableEvent;
import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class CustomHiveListener extends MetaStoreEventListener implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHiveListener.class);
    private static final ObjectMapper objMapper = new ObjectMapper();
    private final String output_file_path = "/tmp/hive_listener_output.txt";
    private final HiveMetaProducer producer;


    public CustomHiveListener(Configuration config) throws IOException {
        super(config);
        // logWithHeader(" created ");

        // writeWithHeader(" created ");
        String clientId = Thread.currentThread().getName();
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
        producer = new HiveMetaProducer(kafkaBrokerUrl, kafkaTopicName, clientId, "-1", 323840);

    }

    @Override
    public void onCreateTable(CreateTableEvent event) {
/**       1. log event */
//        logWithHeader(event.getTable());
//        logWithHeader(event.getParameters());

/**       2. write event to local file */
//        try {
//            writeWithHeader("In CreateTable");
//            writeWithHeader(" Table info: ");
//            writeWithHeader(event.getTable());
//            writeWithHeader(" event parameters ");
//            writeWithHeader(event.getParameters());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        /**       3. send event to kafka broker */
        sendMsgToKafkaBroker("createTable", event.getTable());
    }

    @Override
    public void onAlterTable(AlterTableEvent event) {
        /**       1. log event */
//        logWithHeader(event.getOldTable());
//        logWithHeader(event.getNewTable());
        /**       2. write event to local file */
//        try {
//            writeWithHeader("In AlterTable");
//            writeWithHeader(event.getOldTable());
//            writeWithHeader(event.getNewTable());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        /**       3. send event to kafka broker */
        sendMsgToKafkaBroker("AlterTableOld", event.getOldTable());
        sendMsgToKafkaBroker("AlterTableNew", event.getNewTable());
    }

    @Override
    public void onDropTable(DropTableEvent event) {
        /**       1. log event */
        // logWithHeader(event.getDeleteData());
        /**       2. write event to local file */
//        try {
//            writeWithHeader(" In DropTable ");
//            writeWithHeader(event.getDeleteData()); //return true if the table drop in success
//            writeWithHeader(event.getTable()); //return the table that has been dropped
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        /**       3. send event to kafka broker */
        sendMsgToKafkaBroker("DropTable", event.getTable());
    }

    private String buildLogMessage(Object obj) {
        return "[CustomListener][Thread: " + Thread.currentThread().getName() + "] | " + objToStr(obj);
    }

    private void logWithHeader(Object obj) {
        LOGGER.info(buildLogMessage(obj));
    }

    private void writeWithHeader(Object obj) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_file_path, true));
        writer.write(buildLogMessage(obj));
        writer.close();
    }

    private void sendMsgToKafkaBroker(String eventType, Object obj) {
        producer.sendMsg(eventType, objToStr(obj));
    }

    private String objToStr(Object obj) {
        try {
            return objMapper.writeValueAsString(obj);
        } catch (IOException e) {
            LOGGER.error("Error on conversion", e);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }
}