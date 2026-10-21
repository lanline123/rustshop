package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分销员排行
 *
 * @author kidd
 * @since 2025/6/21
 */
@Data
@Schema(description = "分销员排行")
public class SalesmanRankingVO {

    // *** Salesman ***

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    // *** User ***

    @Schema(description = "分销员名称")
    private String username;

    @Schema(description = "分销员昵称")
    private String nickname;

    // *** Other ***

    @Schema(description = "销售总额")
    private BigDecimal totalSaleAmount;

    @Schema(description = "总客户数")
    private Long totalCustomers;

    @Schema(description = "已支付客户数")
    private Long totalPayCustomers;

    @Schema(description = "订单数")
    private Integer orderNum;

    public void assembleData(User user, Long totalPayCustomers) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.totalPayCustomers = totalPayCustomers == null ? 0L : totalPayCustomers;
        this.totalSaleAmount = this.totalSaleAmount == null ? BigDecimal.ZERO : this.totalSaleAmount;
    }

}
