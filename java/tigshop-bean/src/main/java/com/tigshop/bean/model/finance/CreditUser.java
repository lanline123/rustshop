package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 授信会员实体
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@TableName("credit_user")
@Schema(description = "授信会员")
public class CreditUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "credit_user_id", type = IdType.AUTO)
    @Schema(description = "授信会员ID")
    private Integer creditUserId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "信用额度（单位：元）")
    private BigDecimal creditLimit;

    @Schema(description = "已使用额度（单位：元）")
    private BigDecimal usedAmount;

    @Schema(description = "可用额度（单位：元）")
    private BigDecimal availableAmount;

    @Schema(description = "结算账期类型：1-月结30天，2-季结90天")
    private Integer settlementType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "当前结算周期起点（秒级时间戳），结算清空后重置")
    private Long periodStartTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
