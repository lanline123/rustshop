package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺端保证金协议VO（读配置 deposit_protocol_enable / deposit_protocol_content）。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺端保证金协议")
public class DepositShopProtocolVO {

    @Schema(description = "协议是否开启(deposit_protocol_enable)")
    private Boolean enable;

    @Schema(description = "协议富文本内容(deposit_protocol_content)")
    private String content;
}
