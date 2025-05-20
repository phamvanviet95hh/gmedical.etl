package gmedical.etl.api.dtos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "GIAMDINHHS")
public class GiamDinhHs {

    @JacksonXmlProperty(localName = "THONGTINDONVI")
    private ThongTinDonVi thongTinDonVi;

    @JacksonXmlProperty(localName = "THONGTINHOSO")
    private ThongTinHoSo thongTinHoSo;

}
