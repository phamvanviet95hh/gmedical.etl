package gmedical.etl.api.controllers;


import gmedical.etl.api.service.IcebergService;
import gmedical.etl.api.service.IcebergSparkWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping(value = "v1/data/")
public class SparkController {

    @Autowired
    private IcebergService icebergService;

    @Autowired
    private IcebergSparkWriter icebergSparkWriter;


    @PostMapping(value = "/import")
    public ResponseEntity<String> importData() throws IOException {

        try {
            icebergSparkWriter.writeSampleData();
            return ResponseEntity.ok("Data written to Iceberg table successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error writing data: " + e.getMessage());
        }
    }
//
//    @PostMapping(value = "/show")
//    public String showData(){
//        Dataset<Row> result = sparkSession.read().format("iceberg").load("namespaces.dbtest.table-test3");
//        result.show();
//        return result.toString();
//    }
}
