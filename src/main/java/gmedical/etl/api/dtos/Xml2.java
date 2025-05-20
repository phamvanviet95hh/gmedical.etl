package gmedical.etl.api.dtos;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "CHITIEU_CHITIET_THUOC")
public class Xml2 {

    @JacksonXmlProperty(localName = "CHI_TIET_THUOC")
    private List<ChiTietThuoc> chiTietThuoc;

}
