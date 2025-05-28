package gmedical.etl.api.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class IcebergSparkWriter {

    public void writeSampleData() throws NoSuchTableException {

        SparkSession spark = SparkSession.builder()
                .appName("EmbeddedSparkIceberg")
                .master("local[*]")
                .config("spark.ui.enabled", "false")
                .config("spark.driver.extraJavaOptions", "--Dlog4j.configurationFile=spark-log4j2.properties")
                .config("spark.sql.catalog.my_catalog", "org.apache.iceberg.spark.SparkCatalog")
                .config("spark.sql.catalog.my_catalog.type", "rest")
                .config("spark.sql.catalog.my_catalog.uri", "http://10.6.8.29:8181")
                // Dùng MinIO thay vì AWS S3
                .config("spark.sql.catalog.my_catalog.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
                .config("spark.sql.catalog.my_catalog.s3.endpoint", "http://10.6.8.29:9100")  // MinIO endpoint
                .config("spark.sql.catalog.my_catalog.s3.access-key", "qHdHtnT6nwZqFydGe89l")
                .config("spark.sql.catalog.my_catalog.s3.secret-key", "mYS5VJi5sr2Kx9o7lRnMmnxTQDLsJR6lppfoteef")
                .config("spark.sql.catalog.my_catalog.s3.path-style-access", "true")
                .getOrCreate();

        spark.table("my_catalog.dbtest.`table-test4`").printSchema();

        StructType schema = new StructType()
                .add("id", DataTypes.IntegerType, false)
                .add("name", DataTypes.StringType, true)
                .add("age", DataTypes.IntegerType, false)
                .add("address", DataTypes.StringType, false);

        Row row1 = RowFactory.create(1, "Alice", 12, "Thái Bình");
        Row row2 = RowFactory.create(2, "Bob", 18, "Thái Bình");



        Dataset<Row> df = spark.createDataFrame(Arrays.asList(row1, row2), schema);

        df.writeTo("`my_catalog`.`dbtest`.`table-test4`").append();

        spark.stop();
    }
}
