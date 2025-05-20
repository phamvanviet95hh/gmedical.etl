package gmedical.etl.api.dtos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "CHITIEU_CHITIET_DVKT_VTYT")
public class Xml3 {

    @JacksonXmlProperty(localName = "CHI_TIET_DVKT")
    private ChiTietDvkt chiTietDvkt;

}
