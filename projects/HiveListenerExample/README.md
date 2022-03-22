# Use this custom hive listener

## Step1. Build jar 

```shell
mvn assembly:assembly
```

## Step2. Add jar to hive metastore

### Add jar permanently (for production)
In this mode, the config will persist even if you restart the hive metastore
```shell
# Open the hive conf file
vim /opt/module/hive-3.1.2/conf/hive-env.sh

# At the end of the file, add the line and save:
export HIVE_AUX_JARS_PATH=file:///home/pliu/git/HiveMetastoreListenerAtlas/projects/HiveListenerExample/target/HiveListenerExample-0.1-jar-with-dependencies.jar

# Open the file
vim /opt/module/hive-3.1.2/conf/hive-site.xml

# Add the tag before the closing configuration tag </configuration> and save:
# Example for custom listener
<property>
  <name>hive.metastore.event.listeners</name>
  <value>org.pengfei.hive.listener.CustomHiveListener</value>
</property>

# Example for custom hook
<property>
  <name>hive.exec.post.hooks</name>
  <value>org.pengfei.hive.hook.CustomHiveHook</value>
</property>

# Restart your Hive server
```

### Add jar temporally (for test)

In this mode, the config **will not  persist**. All config are removed after you restart the hive metastore

```hiveql
ADD JAR file:///home/pliu/git/HiveMetastoreListenerAtlas/projects/HiveListenerExample/target/HiveListenerExample-0.1-jar-with-dependencies.jar;

-- set hook to custom jar
set hive.exec.post.hooks=org.pengfei.hive.hook.CustomHiveHook;

-- set listener to custom jar
set hive.metastore.event.listeners=org.pengfei.hive.listener.CustomHiveListener;

```


## Step 3. Set up kafka cluster

To run this custom hive listener, you need to set up a kafka cluster and get two important information.
- kafka_broker_url
- topic_name

## Step 4. Set up env var 
You need to set up three env var in the hive cluster

- KAFKA_BROKER_URL 
- KAFKA_TOPIC_NAME
- KUBERNETES_NAMESPACE

below is an example:

```shell
export KAFKA_BROKER_URL=hadoop1.insee.fr:9092,hadoop2.insee.fr:9092,hadoop3.insee.fr:9092
export KAFKA_TOPIC_NAME=hive-meta
export KUBERNETES_NAMESPACE=user-toto
```
