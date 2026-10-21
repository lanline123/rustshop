package com.tigshop.bean.vo.decorate;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺装修VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺装修参数")
public class ShopDecorateVO {
    @Schema(description = "页面管理ID")
    private Integer decorateId;

    @Schema(description ="模块页面")
    private JSONObject pageModule;

    @Schema(description ="模块数据")
    private JSONArray moduleList;
}
