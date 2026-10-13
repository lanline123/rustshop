package com.tigshop.bean.query.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户端分销客户列表参数。
 */
@Getter
@Setter
@Schema(description = "用户端分销客户列表参数")
public class CustomerApiPageQuery extends CustomerListPageQuery {

    @Schema(description = "客户分类：all全部，ordered下单客户，salesman下级分销员")
    private String customerType = "all";

    @Schema(description = "时间筛选开始日期：全部客户按绑定时间，下单客户按最后下单时间，格式 yyyy-MM-dd")
    private String startTime;

    @Schema(description = "时间筛选结束日期：全部客户按绑定时间，下单客户按最后下单时间，格式 yyyy-MM-dd")
    private String endTime;

    @Schema(description = "当前店铺ID，由后端从请求上下文注入")
    private Integer shopId;

    public String normalizedCustomerType() {
        return switch (customerType == null ? "all" : customerType) {
            case "ordered", "salesman" -> customerType;
            default -> "all";
        };
    }
}
