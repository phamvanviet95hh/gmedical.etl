package gmedical.etl.api.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.*;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class IcebergService {

    private final String warehousePath = "s3://iceberg-warehouse"; // hoặc s3a://bucket-name

    public void importData() throws IOException {
        Table table = null;

        Configuration hadoopConf = new Configuration();
        hadoopConf.set("fs.s3.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConf.set("fs.s3a.access.key", "qHdHtnT6nwZqFydGe89l");
        hadoopConf.set("fs.s3a.secret.key", "mYS5VJi5sr2Kx9o7lRnMmnxTQDLsJR6lppfoteef");
        hadoopConf.set("fs.s3a.endpoint", "http://10.6.8.29:9100");
        hadoopConf.set("fs.s3a.path.style.access", "true");
        hadoopConf.set("fs.s3a.connection.ssl.enabled", "false");
        HadoopCatalog catalog = new HadoopCatalog(hadoopConf, warehousePath);

        // 3. Định nghĩa bảng
        TableIdentifier identifier = TableIdentifier.of("dbtest", "table-test4");

        // 4. Tạo bảng nếu chưa có
        if (!catalog.tableExists(identifier)) {
            Schema schema = new Schema(
                    Types.NestedField.required(1, "id", Types.IntegerType.get()),
                    Types.NestedField.optional(2, "name", Types.StringType.get()),
                    Types.NestedField.required(3, "age", Types.IntegerType.get()),
                    Types.NestedField.required(4, "address", Types.StringType.get())
            );
            PartitionSpec spec = PartitionSpec.unpartitioned();
            table = catalog.createTable(identifier, schema, spec);
            System.out.println("Bảng chưa tồn tại đã tạo mới...");
        }else {
            table = catalog.loadTable(identifier);
            System.out.println("Bảng đã tồn tại...");
        }





        // 5. Ghi 1 bản ghi mẫu để sinh metadata
        Record record = GenericRecord.create(table.schema());
        record.setField("id", 1);
        record.setField("name", "Phạm Văn Việt");
        record.setField("age", 30);
        record.setField("address", "Hà Nội");

        // 6. Tạo file output để ghi dữ liệu
        OutputFile outFile = table.io().newOutputFile(table.location() + "/data/sample.parquet");

        try (FileAppender<Record> writer = Parquet.write(outFile)
                .schema(table.schema())
                .createWriterFunc(GenericParquetWriter::buildWriter)
                .build()) {
            writer.add(record);

            System.out.println("Đã thực hiện ghi ");
        }

        // 7. Commit dữ liệu vào Iceberg
        DataFile dataFile = DataFiles.builder(table.spec())
                .withPath(outFile.location())
                .withFileSizeInBytes(outFile.toInputFile().getLength())
                .withRecordCount(1)
                .build();

        table.newAppend().appendFile(dataFile).commit();

        System.out.println("Đã thực hiện commit ");
    }

    private File writeParquetFile(Schema schema) {
        try {
            Record record = GenericRecord.create(schema);
            record.setField("id", 101);
            record.setField("name", "Phạm Văn Việt");  // optional có thể để null nếu muốn
            record.setField("age", 30);
            record.setField("address", "Hà Nội");

            File file = File.createTempFile("iceberg-data-", ".parquet");
            file.deleteOnExit();

            try (FileAppender<Record> writer = Parquet.write(Files.localOutput(file))
                    .schema(schema)
                    .createWriterFunc(GenericParquetWriter::buildWriter)
                    .build()) {
                writer.add(record);
            }

            return file;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi ghi Parquet", e);
        }
    }
}