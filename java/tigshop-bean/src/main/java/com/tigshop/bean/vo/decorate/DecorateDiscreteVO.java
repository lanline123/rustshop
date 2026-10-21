package com.tigshop.bean.vo.decorate;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.tigshop.bean.enums.decorate.DecorateDiscreteTypeEnum;
import com.tigshop.bean.model.decorate.DecorateDiscrete;
import com.tigshop.common.utils.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 装修组件
 *
 * @author Kidd
 * @since 2025/6/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修组件参数")
public class DecorateDiscreteVO {

    // *** DecorateDiscrete ***

    @Schema(description = "装修组件ID")
    private Integer id;

    @Schema(description ="模块SN")
    private String decorateSn;

    @Schema(description ="模块名称")
    private String decorateName;

    @Schema(description ="店铺ID")
    private Integer shopId;

    // *** Other ***

    @Schema(description ="模块数据，JSON格式")
    private Object data;

    public DecorateDiscreteVO(DecorateDiscrete decorateDiscrete) {
        this.id = decorateDiscrete.getId();
        this.decorateSn = decorateDiscrete.getDecorateSn();
        this.decorateName = decorateDiscrete.getDecorateName();
        this.shopId = decorateDiscrete.getShopId();

        if (DecorateDiscreteTypeEnum.OPEN_ADVERTISING.getCode().equals(decorateDiscrete.getDecorateSn())
                || DecorateDiscreteTypeEnum.PC_INDEX_TIPS.getCode().equals(decorateDiscrete.getDecorateSn())
        ) {
            this.data = JSON.parse(decorateDiscrete.getData());
        } else {
            JsonNode data = JsonUtil.fromJson(decorateDiscrete.getData(), JsonNode.class);
            this.data = JsonUtil.convertKeysToCamelCase(data);
        }

    }
}