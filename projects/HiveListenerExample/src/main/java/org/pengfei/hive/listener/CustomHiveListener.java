package org.pengfei.hive.listener;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.hive.metastore.events.AlterTableEvent;
import org.apache.hadoop.hive.metastore.MetaStoreEventListener;
import org.apache.hadoop.hive.metastore.events.CreateTableEvent;

import org.apache.hadoop.hive.metastore.events.DropTableEvent;
import org.apache.hadoop.hive.metastore.events.PreEventContext;
import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CustomHiveListener extends MetaStoreEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHiveListener.class);
    private static final ObjectMapper objMapper = new ObjectMapper();
    private final String output_file_path="/tmp/hive_listener_output.txt";


    public CustomHiveListener(Configuration config) throws IOException {
        super(config);
        // logWithHeader(" created ");
        writeWithHeader(" created ");

    }

    @Override
    public void onCreateTable(CreateTableEvent event) {
//        logWithHeader(event.getTable());
//        logWithHeader(event.getParameters());
        try {
            writeWithHeader("In CreateTable");
            writeWithHeader(" Table info: ");
            writeWithHeader(event.getTable());
            writeWithHeader(" event parameters ");
            writeWithHeader(event.getParameters());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAlterTable(AlterTableEvent event) {
//        logWithHeader(event.getOldTable());
//        logWithHeader(event.getNewTable());
        try {
            writeWithHeader("In AlterTable");
            writeWithHeader(event.getOldTable());
            writeWithHeader(event.getNewTable());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDropTable(DropTableEvent event){
        // logWithHeader(event.getDeleteData());
        try {
            writeWithHeader(" In DropTable ");
            writeWithHeader(event.getDeleteData());
            writeWithHeader(event.getTable());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildLogMessage(Object obj){
       return  "[CustomListener][Thread: " + Thread.currentThread().getName() + "] | " + objToStr(obj);
    }
    private void logWithHeader(Object obj) {
        LOGGER.info(buildLogMessage(obj));
    }

    private void writeWithHeader(Object obj) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_file_path,true));
        writer.write(buildLogMessage(obj));
        writer.close();
    }

    private String objToStr(Object obj) {
        try {
            return objMapper.writeValueAsString(obj);
        } catch (IOException e) {
            LOGGER.error("Error on conversion", e);
        }
        return null;
    }
}