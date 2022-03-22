package org.pengfei.hive.listener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.events.AlterTableEvent;
import org.apache.hadoop.hive.metastore.MetaStoreEventListener;
import org.apache.hadoop.hive.metastore.events.CreateTableEvent;
import org.apache.hadoop.hive.metastore.events.DropTableEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * This class extends MetaStoreEventListener and will be called if a hive action (e.g. create table) happens
 *
 * It overrides onCreateTable, onAlterTable, onDropTable, when these events occur, event messages will be sent to
 * a given kafka cluster.
 */
public class CustomHiveListener extends MetaStoreEventListener implements AutoCloseable {

    private static final Gson gson = new GsonBuilder().create();
    private final HiveMetaProducer producer;
    private final String clusterName;
    private final String kafkaBrokerUrl;
    private final String kafkaTopicName;

    public String getClusterName() {
        return clusterName;
    }

    public String getKafkaBrokerUrl() {
        return kafkaBrokerUrl;
    }

    public String getKafkaTopicName() {
        return kafkaTopicName;
    }

    /**
     * The constructor first call the constructor of MetaStoreEventListener, then it reads env variable
     * KAFKA_BROKER_URL, KAFKA_TOPIC_NAME,KUBERNETES_NAMESPACE. At last, it builds a kafka producer
     *
     * @param config default MetaStoreEventListener configuration which is provided by hive cluster
     * @throws IllegalArgumentException if one of the three environment variables is missing
     */
    public CustomHiveListener(Configuration config) throws IllegalArgumentException {
        super(config);
        String clientId = Thread.currentThread().getName();
        kafkaBrokerUrl = System.getenv("KAFKA_BROKER_URL");
        kafkaTopicName = System.getenv("KAFKA_TOPIC_NAME");
        clusterName = System.getenv("KUBERNETES_NAMESPACE");
        if (kafkaBrokerUrl == null) {
            throw new IllegalArgumentException("environment variable KAFKA_BROKER_URL is required");
        }
        if (kafkaTopicName == null) {
            throw new IllegalArgumentException("environment variable KAFKA_TOPIC_NAME is required");
        }
        if (clusterName == null) {
            throw new IllegalArgumentException("environment variable KUBERNETES_NAMESPACE is required");
        }
        producer = new HiveMetaProducer(kafkaBrokerUrl, kafkaTopicName, clientId, "-1", 323840);


    }

    /**
     * This method will send the createTable event to the kafka cluster along with the hive clusterName.
     * The message key is : create_table. The message value is the table
     * @param event The event sent by the hive cluster when a createTable action is called
     */
    @Override

    public void onCreateTable(CreateTableEvent event) {

        // prepare event message
        JsonElement targetTable = objToJsonObj(event.getTable());
        targetTable.getAsJsonObject().addProperty("clusterName", this.clusterName);
        String tableJsonStr = gson.toJson(targetTable);
        // send event to kafka broker
        sendMsgToKafkaBroker("create_table", tableJsonStr);
    }

    /**
     * This method will send the alterTable event to the kafka cluster along with the hive clusterName
     * The message key is : alter_table. The message value is : the old table before alter and new table after
     * along with the cluster name
     * @param event The event sent by the hive cluster when a createTable action is called
     */
    @Override
    public void onAlterTable(AlterTableEvent event) {

        // for the alter table, in the event we need
        Map<String, Table> tables = new HashMap<>();
        tables.put("oldTable", event.getOldTable());
        tables.put("newTable", event.getNewTable());
        JsonElement targetTables = objToJsonObj(tables);
        targetTables.getAsJsonObject().addProperty("clusterName", this.clusterName);
        String tablesJsonStr = gson.toJson(targetTables);
        sendMsgToKafkaBroker("alter_table", tablesJsonStr);
    }

    /**
     * This method will send the dropTable event to the kafka cluster along with the hive clusterName
     * The message key is : drop_table. The message value is : the table along with the cluster name
     * @param event The event sent by the hive cluster when a createTable action is called
     */
    @Override
    public void onDropTable(DropTableEvent event) {
        JsonElement targetTable = objToJsonObj(event.getTable());
        targetTable.getAsJsonObject().addProperty("clusterName", this.clusterName);
        String tableJsonStr = gson.toJson(targetTable);
        sendMsgToKafkaBroker("drop_table", tableJsonStr);
    }


    /**
     * This method sends msg to kafka cluster
     *
     * @param eventType The message key which is the hive event type
     * @param msg The message value which is the hive event value
     */
    private void sendMsgToKafkaBroker(String eventType, String msg) {
        producer.sendMsg(eventType, msg);
    }

    /**
     * @param obj Serialize an obj to JsonElement by using gson
     * @return a JsonElement
     */
    private JsonElement objToJsonObj(Object obj) {
        return gson.toJsonTree(obj);
    }


    /**
     * Auto close kafka producer after hive shutdown
     */
    @Override
    public void close() {
        producer.close();
    }
}