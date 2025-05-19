package gmedical.etl.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gmedical.etl.api.model.MyDataModel;
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

    @KafkaListener(topics = "data-change-topic", groupId = "gmedical")
    public void listen(String message) {
        System.out.println("Received Kafka message: " + message);

    }
}
