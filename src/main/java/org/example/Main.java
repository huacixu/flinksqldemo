package org.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.table.api.Expressions.$;

//Stream和tableapi混用
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        //1.获取Datastream执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //2.创建表执行环境
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        //3.获取数据
        DataStream<ClickLog> clickLogs = env.fromElements("Mary,.home,2022-02-02 12:00:00",
                "Bob,./cart,2022-02-02 12:00:00",
                "Mary,./prod?id=1,2022-02-02 12:00:05",
                "Liz,./home,2022-02-02 12:01:00",
                "Bob,./prod?id=3,2022-02-02 12:01:30",
                "Mary,./prod?id=7,2022-02-02 12:01:45").map(event -> {
                    String[] props = event.split(",");
                    return ClickLog
                            .builder()
                            .user(props[0])
                            .url(props[1])
                            .cTime(props[2])
                                    .build();
        });

        //4.流转换为动态表
        Table table = tEnv.fromDataStream(clickLogs);

        //5.执行TableApi查询
        Table resultTable = table.where($("user").isEqual("Mary"))
                .select($("user"),$("url"),$("cTime"));

        // 6. 将Table转为Datastream
        DataStream<ClickLog> selectedClickLogs = tEnv.toDataStream(resultTable,ClickLog.class);

        //7.结果处理：打印/输出
        selectedClickLogs.print();

        //8.执行Flink
        env.execute("flinksqldemo1");

    }
}