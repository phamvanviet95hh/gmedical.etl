package gmedical.etl.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gmedical.etl.api.model.MyDataModel;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Tags;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    private MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public List<MyDataModel> parseJson(InputStream stream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(stream, MyDataModel[].class));
    }

    public String getFileAsBase64(String fullPath) throws Exception {
        String objectPath = fullPath.replace(bucketName + "/", "");

        try (InputStream inputStream = this.minioClient().getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectPath)
                        .build()
        )) {
            byte[] fileBytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(fileBytes);
        }
    }

    public String uploadFile(String fileName, MultipartFile file) throws Exception {
        try {
            boolean bucketExists = this.minioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                this.minioClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            try (InputStream inputStream = file.getInputStream()) {
                this.minioClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, inputStream.available(), -1) // Specify size of stream
                                .contentType(file.getContentType())
                                .build());
            }
            log.info("save file {} onto: {}", file.getOriginalFilename(), fileName);
            return fileName;
        } catch (MinioException e) {
            throw new Exception("Error occurred: " + e);
        }
    }

    public String uploadFile(String rootPath, byte[] imageBytes, String fileName, String contentType) throws Exception {
        try {
            var fullFilePath = rootPath + fileName + "." + contentType.split("/")[1];
            boolean bucketExists = this.minioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                this.minioClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                this.minioClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fullFilePath)
                                .stream(inputStream, inputStream.available(), -1)
                                .contentType(contentType)
                                .build());
            }
            log.info("save file: {}", fullFilePath);
            return fullFilePath;
        } catch (MinioException e) {
            throw new Exception("Error occurred: " + e);
        }
    }

    public void uploadFile(String fileName, byte[] imageBytes) throws Exception {
        try {
            boolean bucketExists = this.minioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                this.minioClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                Map<String, String> newTags = new HashMap<>();
                newTags.put("etl_status", "data_received");
                this.minioClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .tags(newTags)
                                .stream(inputStream, inputStream.available(), -1)
                                .contentType(MediaType.APPLICATION_XML_VALUE)
                                .build());
            }
            log.info("save file: {}", fileName);
        } catch (MinioException e) {
            throw new Exception("Error occurred: " + e);
        }
    }
    public InputStream downloadFile(String bucket, String path) throws Exception {
        return this.minioClient().getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .build()
        );
    }

    public InputStream downloadFile(String fullFilePath) throws Exception {
        try {
            var builder = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullFilePath)
                    .build();
            InputStream object = this.minioClient().getObject(builder);
            byte[] xmlBytes = toByteArray(object);

            String xmlContent = new String(xmlBytes, StandardCharsets.UTF_8);
            System.out.println("XML content:\n" + xmlContent);

            InputStream xmlInputStreamForParsing = new ByteArrayInputStream(xmlBytes);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder buildera = factory.newDocumentBuilder();
            Document document = buildera.parse(xmlInputStreamForParsing);

            document.getDocumentElement().normalize();
            System.out.println("Root element: " + document.getDocumentElement().getNodeName());
            log.info("download file: {}", fullFilePath);
            // ✅ Tạo thư mục đích nếu chưa tồn tại
            String folderPath = "C:\\gmedical\\data\\";
            Files.createDirectories(Paths.get(folderPath));

            // ✅ Tên file từ object path
            String fileName = Paths.get(fullFilePath).getFileName().toString();

            // ✅ Đường dẫn đầy đủ để lưu file
            String destinationPath = folderPath + fileName;

            // ✅ Lưu stream vào file đích
            Files.copy(object, Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ File đã được tải và lưu tại: " + destinationPath);


            System.out.println("content : " + object);
            return object;
        } catch (Exception e) {
            throw new Exception("Error occurred: " + e);
        }
    }
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public void readXmlContent(InputStream xmlInputStream) throws Exception {

        // Kiểm tra stream có dữ liệu không
        if (xmlInputStream.available() == 0) {
            System.out.println("InputStream rỗng, không có dữ liệu.");
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlInputStream);

        document.getDocumentElement().normalize();

        System.out.println("Root element: " + document.getDocumentElement().getNodeName());

        // Ví dụ: lấy giá trị của thẻ <exampleTag>
        String exampleValue = document.getElementsByTagName("exampleTag")
                .item(0)
                .getTextContent();

        System.out.println("Value of exampleTag: " + exampleValue);
    }
    public boolean doesObjectExist(String objectName) {
        try {
            // Check if the object exists
            minioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true; // Object exists
        } catch (MinioException e) {
            if (e.getMessage().isEmpty()) {
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Tags getObjectTags(String objectName) {
        try {
            // Get object tags
            return minioClient().getObjectTags(
                    GetObjectTagsArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException e) {
            e.printStackTrace();
            return null; // Handle error appropriately
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle error appropriately
        }
    }

    public void copyFile(String sourceObjectName, String targetObjectName) throws MinioException {
        try {
            this.minioClient().copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(sourceObjectName)
                                    .build())
                            .bucket(bucketName)
                            .object(targetObjectName)
                            .build());

            log.info("File copied successfully.");
        } catch (Exception e) {
            throw new MinioException("Error occurred: " + e.getMessage());
        }
    }

}

