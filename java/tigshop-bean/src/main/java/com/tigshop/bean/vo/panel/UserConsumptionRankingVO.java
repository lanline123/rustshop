package com.tigshop.bean.vo.panel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 会员消费排行
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Getter
@Setter
@Schema(description = "会员消费排行")
public class UserConsumptionRankingVO {

    @ExcelProperty(value = "会员名称", index = 0)
    @Schema(description = "用户名")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @ExcelProperty(value = "手机号", index = 1)
    @Schema(description = "手机号")
    private String mobile;

    @ExcelProperty(value = "订单数", index = 2)
    @Schema(description = "订单数量")
    private Integer orderNum;

    @ExcelProperty(value = "消费总额", index = 3)
    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

}