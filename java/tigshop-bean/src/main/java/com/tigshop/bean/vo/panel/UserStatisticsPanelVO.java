package com.tigshop.bean.vo.panel;

import com.alibaba.excel.annotation.ExcelProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用户统计面板
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Getter
@Setter
@Schema(description = "用户统计面板")
public class UserStatisticsPanelVO {

    @ExcelProperty(value = "访问量", index = 0)
    @Schema(description = "访问量")
    private Integer visitNum;

    @ExcelProperty(value = "访问量环比增长率", index = 1)
    @Schema(description = "访问量环比增长率")
    private BigDecimal visitGrowthRate;

    @ExcelProperty(value = "浏览量", index = 2)
    @Schema(description = "浏览量")
    private Integer viewNum;

    @ExcelProperty(value = "浏览量环比增长率", index = 3)
    @Schema(description = "浏览量环比增长率")
    private BigDecimal viewGrowthRate;

    @ExcelProperty(value = "新增用户数", index = 4)
    @Schema(description = "新增用户数")
    private Long addUserNum;

    @ExcelProperty(value = "新增用户数环比增长率", index = 5)
    @Schema(description = "新增用户数环比增长率")
    private BigDecimal addUserGrowthRate;

    @ExcelProperty(value = "成交用户数", index = 6)
    @Schema(description = "成交用户数")
    private Long dealUserNum;

    @ExcelProperty(value = "成交用户数环比增长率", index = 7)
    @Schema(description = "成交用户数环比增长率")
    private BigDecimal dealUserGrowthRate;

    @ExcelProperty(value = "访客-支付转化率", index = 8)
    @Schema(description = "访客-支付转化率")
    private BigDecimal visitToUser;

    @ExcelProperty(value = "访客-支付转化率环比增长率", index = 9)
    @Schema(description = "访客-支付转化率环比增长率")
    private BigDecimal visitToUserRate;

    @ExcelProperty(value = "充值用户数", index = 10)
    @Schema(description = "充值用户数")
    private Long rechargeUserNum;

    @ExcelProperty(value = "充值用户数环比增长率", index = 11)
    @Schema(description = "充值用户数环比增长率")
    private BigDecimal rechargeUserGrowthRate;

}
