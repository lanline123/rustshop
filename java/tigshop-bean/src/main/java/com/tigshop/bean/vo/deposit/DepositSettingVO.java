package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺保证金设置VO（详情回显）。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金设置")
public class DepositSettingVO {

    @Schema(description = "保证金功能开关 1开启 0关闭")
    private Integer depositEnable;

    @Schema(description = "收取规则（固定：按主营类目最高=1）")
    private Integer depositCollectRule;

    @Schema(description = "收取节点 1上架时 2首单时")
    private Integer depositCollectNode;

    @Schema(description = "收取节点缓冲天数")
    private Integer depositCollectBufferDays;

    @Schema(description = "收取缓冲到期处罚 1限制上架 2全店下架(含禁新订单)")
    private Integer depositCollectBufferAction;

    @Schema(description = "补缴缓冲天数")
    private Integer depositSupplementBufferDays;

    @Schema(description = "补缴缓冲到期处罚 1限制上架 2全店下架(含禁新订单)")
    private Integer depositSupplementBufferAction;

    @Schema(description = "保证金协议开关 1开启 0关闭")
    private Integer depositProtocolEnable;

    @Schema(description = "保证金协议内容（富文本）")
    private String depositProtocolContent;
}
