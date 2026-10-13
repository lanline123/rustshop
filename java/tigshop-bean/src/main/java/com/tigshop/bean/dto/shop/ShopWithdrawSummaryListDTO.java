package com.tigshop.bean.dto.shop;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺提现汇总列表查询参数
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺提现汇总列表查询参数")
public class ShopWithdrawSummaryListDTO extends BasePage {

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺状态：1开业 4暂停运营 10关店")
    private Integer status;

    @Schema(description = "店铺类型：1店铺，2门店，3自提点")
    private Integer shopType;

    @Schema(description = "开店开始时间")
    private String addTimeStart;

    @Schema(description = "开店结束时间")
    private String addTimeEnd;

    @Schema(description = "提现开始时间")
    private String withdrawTimeStart;

    @Schema(description = "提现结束时间")
    private String withdrawTimeEnd;
}
