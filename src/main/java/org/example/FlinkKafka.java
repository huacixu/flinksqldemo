package org.example;

import org.apache.flink.table.api.*;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.row;

public class FlinkKafka {
    public static void main(String[] args) {

        //1.创建TableEnv
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                //.useBlinkPlanner() //从1.14之后就删除了其它执行器
                //.inStreamingMode() // 默认就是流方式
                .build();


        TableEnvironment tEnv = TableEnvironment.create(settings);

        Schema schema = Schema.newBuilder()
                .column("user",DataTypes.STRING())
                .column("url",DataTypes.STRING())
                .column("cTime",DataTypes.STRING())
                .build();


        //2.注册表
        tEnv.createTemporaryTable("sourceTable",TableDescriptor.forConnector("kafka")
                .schema(schema)
                .option("topic","clicklog-input")
                .option("properties.bootstrap.servers","hd02:9092")
                .option("scan.startup.mode","earliest-offset")
                .option("properties.group.id","testGroup")
                .option("format","json")
                .build()
        );
        //kafka-topics.sh --create --bootstrap-server hd02:9092 --replication-factor 3 --partitions 1 --topic clicklog-input
        //创建主题

        //3.创建sink table
        tEnv.createTemporaryTable("sinkTable",TableDescriptor.forConnector("print")
                .schema(schema)
                .build());

        //4.执行查询
        Table resultTable = tEnv.sqlQuery("select * from sourceTable");
//        tEnv.from("sourceTable").select($("id"),$("name"));

        //5.输出
        resultTable.executeInsert("sinkTable");

        //6.写入到kafka
        tEnv.createTemporaryTable("sinkKafkaTable",TableDescriptor.forConnector("kafka")
                .schema(schema)
                .option("topic","clicklog-output")
                .option("properties.bootstrap.servers","hd02:9092")
                .option("format","csv")
                .build());

        resultTable.executeInsert("sinkKafkaTable");

    }
}
