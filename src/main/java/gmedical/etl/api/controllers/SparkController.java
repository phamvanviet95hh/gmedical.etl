package gmedical.etl.api.controllers;


import gmedical.etl.api.service.IcebergFlinkService;
import gmedical.etl.api.service.IcebergService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "v1/data/")
public class SparkController {

    @Autowired
    private IcebergService icebergService;

    @Autowired
    private IcebergFlinkService icebergFlinkService;


    @PostMapping(value = "/import")
    public ResponseEntity<String> importData() throws Exception {


            icebergFlinkService.writeSampleData();
            return ResponseEntity.ok("Data written to Iceberg table successfully.");

    }
//
//    @PostMapping(value = "/show")
//    public String showData(){
//        Dataset<Row> result = sparkSession.read().format("iceberg").load("namespaces.dbtest.table-test3");
//        result.show();
//        return result.toString();
//    }
}
