package gmedical.etl.api.service;

import org.apache.flink.configuration.Configuration;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;

import org.apache.flink.table.api.TableEnvironment;


import java.util.HashMap;
import java.util.Map;

import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.stereotype.Service;

@Service
public class IcebergFlinkService {


    public void writeSampleData() throws Exception {
        Configuration config = new Configuration();
        config.setString("fs.s3a.access.key", "qHdHtnT6nwZqFydGe89l");
        config.setString("fs.s3a.secret.key", "mYS5VJi5sr2Kx9o7lRnMmnxTQDLsJR6lppfoteef");
        config.setString("fs.s3a.endpoint", "http://10.6.8.29:9100");
        config.setString("fs.s3a.path.style.access", "true");
        config.setString("fs.s3a.connection.ssl.enabled", "false");

        // set up the execution environment
        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        // set up the table environment
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(
                env,
                EnvironmentSettings.newInstance().inStreamingMode().build());


        // Register Iceberg REST catalog
        Map<String, String> icebergConfig = new HashMap<>();
        icebergConfig.put("type", "iceberg");
        icebergConfig.put("catalog-type", "rest");
        icebergConfig.put("uri", "http://10.6.8.29:8181");
        icebergConfig.put("warehouse", "s3://iceberg-warehouse");
        icebergConfig.put("property-version", "1");

        tableEnv.executeSql("CREATE CATALOG iceberg WITH ("
                + "'type'='iceberg',"
                + "'catalog-impl'='org.apache.iceberg.nessie.NessieCatalog',"
                + "'io-impl'='org.apache.iceberg.aws.s3.S3FileIO',"
                + "'uri'='http://10.6.8.29:8181/',"
                + "'authentication.type'='none',"
                + "'ref'='main',"
                + "'client.assume-role.region'='us-east-1',"
                + "'warehouse' = 's3://iceberg-warehouse',"
                + "'s3.endpoint'='http://10.6.8.29:9100'"
                + ")");

        tableEnv.useCatalog("iceberg");

        // Optional: if you already have a table in Iceberg, skip this CREATE TABLE step
        tableEnv.executeSql("CREATE TABLE IF NOT EXISTS sample_table (id INT, name STRING) WITH ('format-version'='2', 'write.format.default'='parquet')");

        // Create a temporary table with inline values
        tableEnv.executeSql("CREATE TEMPORARY TABLE input_data (id INT, name STRING) WITH ('connector'='values')");

        tableEnv.executeSql("INSERT INTO input_data VALUES (1, 'Alice'), (2, 'Bob'), (3, 'Charlie')");

        // Insert into Iceberg table
        tableEnv.executeSql("INSERT INTO sample_table SELECT * FROM input_data");

    }
}
