# Custom hive listener

## Build jar 

```shell
mvn assembly:assembly
```

## Add jar to hive metastore

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