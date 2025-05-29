package gmedical.etl.api.service;

import org.apache.flink.configuration.Configuration;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;


import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.stereotype.Service;

@Service
public class IcebergFlinkService {


    public void writeSampleData() throws Exception {
        // Load Hadoop configuration for S3A (MinIO)
        Configuration hadoopConf = new Configuration();
        hadoopConf.setString("fs.s3a.access.key", "qHdHtnT6nwZqFydGe89l");
        hadoopConf.setString("fs.s3a.secret.key", "mYS5VJi5sr2Kx9o7lRnMmnxTQDLsJR6lppfoteef");
        hadoopConf.setString("fs.s3a.endpoint", "http://10.6.8.29:9100");
        hadoopConf.setBoolean("fs.s3a.path.style.access", true);
        hadoopConf.setBoolean("fs.s3a.connection.ssl.enabled", false);

        // Create StreamExecutionEnvironment with Hadoop configuration
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(new org.apache.flink.configuration.Configuration());

        // Create TableEnvironment in streaming mode
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        // Add Hadoop configuration to TableEnvironment
        tableEnv.getConfig().getConfiguration().addAll(hadoopConf);

        try {
            // Create Iceberg catalog using REST Catalog
            tableEnv.executeSql(
                    "CREATE CATALOG iceberg WITH (" +
                            "'type'='iceberg'," +
                            "'catalog-type'='rest'," +
                            "'uri'='http://10.6.8.29:8181'," +
                            "'warehouse'='s3://iceberg-warehouse'," +
                            "'s3.endpoint'='http://10.6.8.29:9100'," +
                            "'s3.path.style.access'='true'," +
                            "'authentication.type'='none'" +
                            ")"
            );

            // Use the created catalog
            tableEnv.useCatalog("iceberg");

            // Create Iceberg table
            tableEnv.executeSql(
                    "CREATE TABLE IF NOT EXISTS dbtest.`table-test6` (" +
                            "id INT, " +
                            "name STRING" +
                            ") WITH (" +
                            "'format-version' = '1', " +
                            "'write.format.default' = 'parquet'" +
                            ")"
            );

            // Insert sample data into the table
            tableEnv.executeSql(
                    "INSERT INTO dbtest.`table-test6` VALUES (1, 'Alice'), (2, 'Bob'), (3, 'Charlie')"
            );

            // Execute the Flink job
            env.execute("Iceberg Sample Data Write");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error executing Iceberg operations", e);
        }
    }
}
