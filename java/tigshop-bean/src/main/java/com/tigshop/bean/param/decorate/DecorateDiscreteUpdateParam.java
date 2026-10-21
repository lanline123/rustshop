package com.tigshop.bean.param.decorate;

import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.decorate.DecorateDiscrete;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 装修组件更新参数
 *
 * @author Kidd
 * @since 2025/6/30
 */
@Schema(description = "装修组件更新参数")
@Data
public class DecorateDiscreteUpdateParam {

    // *** DecorateDiscrete ***

    @Schema(description = "装修组件ID")
    private Integer id;

    @NotBlank(message = "模块SN不能为空")
    @Schema(description = "模块SN")
    private String decorateSn;

    @Schema(description = "模块名称")
    private String decorateName;

    @Schema(description = "店铺ID")
    private Integer shopId;

    // *** Other ***

    @NotNull(message = "模块数据不能为空")
    @Schema(description = "模块数据，JSON格式")
    private Object data;

    public DecorateDiscrete createSaveDecorateDiscrete() {
        String data = getDataStr();
        Integer shopId = HeaderUtils.getShopId();

        return DecorateDiscrete.builder()
                .decorateSn(this.decorateSn)
                .decorateName(this.decorateName)
                .data(data)
                .shopId(shopId)
                .build();
    }

    public DecorateDiscrete createEditDecorateDiscrete(DecorateDiscrete decorateDiscrete) {
        String data = getDataStr();
        Integer shopId = HeaderUtils.getShopId();

        return DecorateDiscrete.builder()
                .id(decorateDiscrete.getId())
                .decorateSn(this.decorateSn)
                .decorateName(this.decorateName)
                .data(data)
                .shopId(shopId)
                .build();
    }

    private String getDataStr() {
        return JSON.toJSONString(this.data);
    }
}
