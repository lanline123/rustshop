package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.dto.o2o.reservationtpl.ReservationTplDetailDTO;
import com.tigshop.bean.model.o2o.ReservationTpl;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "预约模板")
public class ReservationTplLisVO extends ReservationTpl {

}
