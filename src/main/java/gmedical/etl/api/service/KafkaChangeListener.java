package gmedical.etl.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gmedical.etl.api.model.MyDataModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class KafkaChangeListener {

    private final MinioService minioService;


    public KafkaChangeListener(MinioService minioService) {
        this.minioService = minioService;

    }

    @KafkaListener(topics = "minio-topic", groupId = "gmedical")
    public void listen(String message) throws Exception {
        System.out.println("Received Kafka message: " + message);
        JSONObject json = new JSONObject(message);


        // ✅ Lấy key từ JSON root level
        String key = json.getString("Key"); // Đây là dạng không encode
        System.out.println("🔑 Key gốc: " + key);
        // Tách bỏ tên bucket khỏi key nếu cần
        String objectPath = key.replace("gmedical.lake/", "");
        minioService.downloadFile(objectPath);


    }
}
