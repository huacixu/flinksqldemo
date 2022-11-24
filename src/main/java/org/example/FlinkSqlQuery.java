package org.example;

import org.apache.flink.table.api.*;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.row;

public class FlinkSqlQuery {
    public static void main(String[] args) {
        //1.创建TableEnv
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                //.useBlinkPlanner() //从1.14之后就删除了其它执行器
                //.inStreamingMode() // 默认就是流方式
                .build();


        TableEnvironment tEnv = TableEnvironment.create(settings);

        //2.创建source table
        Table sourceTable = tEnv.fromValues(
                DataTypes.ROW(
                        DataTypes.FIELD("id", DataTypes.DECIMAL(10,2)),
                        DataTypes.FIELD("name", DataTypes.STRING())
                ),row(1,"zhangshan"),row(2,"lishi")
        ).select($("id"),$("name"));

        //2.注册表
        tEnv.createTemporaryView("sourceTable",sourceTable);

        //3.创建sink table
        Schema schema = Schema.newBuilder()
                .column("id",DataTypes.DECIMAL(10,2))
                .column("name",DataTypes.STRING())
                .build();
        tEnv.createTemporaryTable("sinkTable",TableDescriptor.forConnector("print")
                .schema(schema)
                .build());

        //4.执行查询
        Table resultTable = tEnv.sqlQuery("select * from sourceTable");
                //tEnv.from("sourceTable").select($("id"),$("name"));

        //5.输出
        resultTable.executeInsert("sinkTable");
    }
}
