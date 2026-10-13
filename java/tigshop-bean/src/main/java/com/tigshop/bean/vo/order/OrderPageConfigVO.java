package com.tigshop.bean.vo.order;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单列表配置
 *
 * @author kidd
 * @since 2025/7/7 14:56
 */
@Data
public class OrderPageConfigVO {

    @ConfigItemField(SettingsEnum.IS_CHANGE_ORDER_STATUS)
    @Schema(description = "订单流转设置；0-关闭，1-开启")
    private Integer isChangeOrderStatus;

}
