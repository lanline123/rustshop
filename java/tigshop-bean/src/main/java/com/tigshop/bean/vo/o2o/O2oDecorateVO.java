package com.tigshop.bean.vo.o2o;

import com.alibaba.fastjson.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class O2oDecorateVO {

    @Schema(description = "模块数据，JSON格式")
    private Object data;


}
