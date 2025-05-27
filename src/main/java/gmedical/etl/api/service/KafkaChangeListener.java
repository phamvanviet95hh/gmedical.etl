package gmedical.etl.api.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gmedical.etl.api.dtos.*;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Log4j2
public class KafkaChangeListener {

    private final MinioService minioService;


    public KafkaChangeListener(MinioService minioService) {
        this.minioService = minioService;

    }

    @KafkaListener(topics = "minio-topic", groupId = "gmedical")
    public void listen(String message) throws Exception {
        log.info("Received Kafka message: {}" , message);
        JSONObject json = new JSONObject(message);


        // ✅ Lấy key từ JSON root level
        String key = json.getString("Key"); // Đây là dạng không encode
        log.info("🔑 Key gốc: {}" , key);
        // Tách bỏ tên bucket khỏi key nếu cần
        String objectPath = key.replace("gmedical.lake/", "");
        InputStream fileData =  minioService.downloadFile(objectPath);
        this.convertData(fileData);
    }

    public void convertData(InputStream fileData) throws IOException, ParserConfigurationException, SAXException {
        byte[] xmlBytes = toByteArray(fileData);
        String xmlContent = new String(xmlBytes, StandardCharsets.UTF_8);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Parse XML thành DTO chính
        GiamDinhHs giamDinhHs = xmlMapper.readValue(xmlContent, GiamDinhHs.class);

        // Parse XML để lấy các FILEHOSO
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlBytes));
        NodeList fileHosoList = document.getElementsByTagName("FILEHOSO");

        // Map ánh xạ LOAIHOSO -> class
        Map<String, Class<?>> xmlClassMap = Map.ofEntries(
                Map.entry("XML1", Xml1.class),
                Map.entry("XML2", Xml2.class),
                Map.entry("XML3", Xml3.class),
                Map.entry("XML4", Xml4.class),
                Map.entry("XML5", Xml5.class),
                Map.entry("XML6", Xml6.class),
                Map.entry("XML7", Xml7.class),
                Map.entry("XML8", Xml8.class),
                Map.entry("XML9", Xml9.class),
                Map.entry("XML10", Xml10.class),
                Map.entry("XML11", Xml11.class),
                Map.entry("XML12", Xml12.class),
                Map.entry("XML13", Xml13.class),
                Map.entry("XML14", Xml14.class),
                Map.entry("XML15", Xml15.class)
        );

        for (int i = 0; i < fileHosoList.getLength(); i++) {
            Element fileHoso = (Element) fileHosoList.item(i);
            String loaiHoso = fileHoso.getElementsByTagName("LOAIHOSO").item(0).getTextContent();
            String encodedContent = fileHoso.getElementsByTagName("NOIDUNGFILE").item(0).getTextContent();

            byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
            String innerXml = new String(decodedBytes, StandardCharsets.UTF_8);
            log.info("== Loại Hồ Sơ: {}" , loaiHoso);

            Class<?> clazz = xmlClassMap.getOrDefault(loaiHoso, Xml1.class);
            Object xmlObj = xmlMapper.readValue(innerXml, clazz);

            // Xử lý từng loại
            if (xmlObj instanceof Xml1 xml1) {
                log.info("Mã liên kết : {}",xml1.getMaLk());
            } else if (xmlObj instanceof Xml2 xml2) {
                log.info("Số lượng đơn thuốc: {}" , xml2.getDanhSachChiTietThuoc().getChiTietThuoc().size());
            }

        }

        log.info("Root element: {}" , document.getDocumentElement().getNodeName());
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
}
