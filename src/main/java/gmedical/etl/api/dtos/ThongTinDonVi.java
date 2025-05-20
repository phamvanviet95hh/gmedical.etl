package gmedical.etl.api.dtos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
public class ThongTinDonVi {

    @JacksonXmlProperty(localName = "MACSKCB")
    private String maCSKCB;

}
