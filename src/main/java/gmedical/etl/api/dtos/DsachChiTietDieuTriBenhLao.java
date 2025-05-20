package gmedical.etl.api.dtos;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class DsachChiTietDieuTriBenhLao {

    @JacksonXmlProperty(localName = "CHITIET_DIEUTRI_BENHLAO")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ChiTietDieuTriBenhLao> chiTietDieuTriBenhLaoList;

}
