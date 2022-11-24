package org.example;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.catalog.hive.HiveCatalog;

import static org.apache.flink.table.api.Expressions.$;

public class FlinkHive {
    public static void main(String[] args) {
        //因为报错日志中有Permission denied: user=Administrator,所以应该是程序认为我们当前是Administrator用户，究其原因还是权限问题吗，操作hdfs的用户不是集群上规定的用户，而且不存在 supergroup组内
        System.setProperty("HADOOP_USER_NAME","denny");

        EnvironmentSettings settings = EnvironmentSettings.inBatchMode();
        TableEnvironment tableEnv = TableEnvironment.create(settings);

        String name            = "myhive";
        String defaultDatabase = "test";
        String hiveConfDir     = "D:\\code\\flinkcdc\\flinksql\\src\\main\\resources";

        HiveCatalog hive = new HiveCatalog(name, defaultDatabase, hiveConfDir);
        tableEnv.registerCatalog("myhive", hive);

// set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog("myhive");
        tableEnv.useDatabase(defaultDatabase);

        //Flink SQL方式读取HIVE表数据
//        tableEnv.executeSql("select * from clicklog;").print();

        //Flink TABLE API 方式读取HIVE表数据
        Table resultTable = tableEnv.from("clicklog").select($("username"),$("url"),$("ctime"));
        resultTable.execute().print();

        //插入数据到另外一个表中clicklog2，表结构一样

        //TABLE API方式写入到HIVE
//        resultTable.executeInsert("clicklog2");

//        tableEnv.executeSql("insert overwrite clicklog3 select * from clicklog;");//Streaming mode not support overwrite.


        //删除
        //注意：当需要删除某一条数据的时候，我们需要使用 insert overwrite
        //insert overwrite table table_name select * from table_name where 条件
        //释义：就是用满足条件的数据去覆盖原表的数据，这样只要在where条件里面过滤需要删除的数据就可以了
        //SQL parse failed. Bang equal '!=' is not allowed under the current SQL conformance level
        //!=  -> <>
        //dbeaver可以执行：insert overwrite table clicklog3 select * from clicklog3 where username !='Denny'
        tableEnv.executeSql("insert overwrite clicklog3 select * from clicklog3 where username <>'Denny'");

        //SQL方式写入到HIVE
//        tableEnv.executeSql("INSERT INTO clicklog\n" +
//                "(username, url, ctime)\n" +
//                "VALUES('XCX', './hello', '2022-11-20 17:00:00');");

    }
}
