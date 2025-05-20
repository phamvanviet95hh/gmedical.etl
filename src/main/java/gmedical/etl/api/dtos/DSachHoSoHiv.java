package gmedical.etl.api.dtos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class DSachHoSoHiv {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "HO_SO_BENH_AN_CHAM_SOC_VA_DIEU_TRI_HIV_AIDS")
    private List<HoSoBACSVADTHiv> hivs;

}
