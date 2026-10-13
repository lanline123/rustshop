package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺端首页保证金预警横幅VO。
 *
 * <p>预警判定：待补缴(pending)&gt;0 即需要预警。</p>
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺端保证金预警横幅")
public class DepositShopWarningVO {

    @Schema(description = "是否需要预警(待补缴>0)")
    private Boolean needWarning;

    @Schema(description = "待补缴金额")
    private BigDecimal pendingAmount;

    @Schema(description = "补缴截止时间戳(秒,0未触发)")
    private Long supplementDueTime;

    @Schema(description = "补缴截止时间(格式化,空=未触发)")
    private String supplementDueTimeText;

    @Schema(description = "缓冲缴存期剩余天数(向上取整;收取缓冲优先,否则补缴缓冲;无/已过期=0)")
    private Integer bufferDays;

    @Schema(description = "预警文案(不需预警时为空;按当前状态匹配的单条文案)")
    private String message;

    @Schema(description = "预警文案列表(不需预警时为空列表;当前按单条匹配返回,优先级 超期>补缴>收取,保留列表结构兼容)")
    private List<String> messages;
}
