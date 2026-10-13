package com.tigshop.bean.vo.finance;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 授信会员详情VO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "授信会员详情")
public class CreditUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "授信会员ID")
    private Integer creditUserId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员用户名")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "会员类型：1-个人，2-企业")
    private Integer type;

    @Schema(description = "会员类型文本")
    private String typeText;

    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    @Schema(description = "已使用额度")
    private BigDecimal usedAmount;

    @Schema(description = "可用额度")
    private BigDecimal availableAmount;

    @Schema(description = "结算账期类型：1-月结，2-季结")
    private Integer settlementType;

    @Schema(description = "结算账期类型文本")
    private String settlementTypeText;

    @Schema(description = "账期天数")
    private Integer settlementDays;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
