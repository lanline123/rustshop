package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.enums.salesman.SalesmanOrderStatus;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销员详细统计信息VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分销员详细统计信息")
public class SalesmanStatisticalVO {

    // *** Salesman ***

    @Schema(description = "销售员ID")
    private Integer salesmanId;

    @Schema(description = "等级ID")
    private Integer level;

    @Schema(description = "分销员分组")
    private Integer groupId;

    @Schema(description = "上级用户id")
    private Integer pid;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "累计销售金额")
    private BigDecimal saleAmount;

    // *** Other ***

    @Schema(description = "等级文本")
    private String levelText;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "user信息")
    private SalesmanInfo baseUserInfo;

    @Schema(description = "分组信息")
    private SalesmanGroupInfo groupInfo;

    @Schema(description = "销售员名称")
    private String salesmanName;

    @Schema(description = "支付订单数")
    private Integer orderCount;

    @Schema(description = "退款订单数")
    private Integer refundCount;

    @Schema(description = "支付订单金额")
    private BigDecimal payMoney;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "已结算收益")
    private BigDecimal totalCommission;

    @Schema(description = "累计收益")
    private BigDecimal totalAmountCommission;

    @Schema(description = "总客户数")
    private long totalCustomer;

    @Schema(description = "销售订单信息")
    private List<SalesmanOrderInfo> salesmanOrderInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分销员分组信息")
    public static class SalesmanGroupInfo {

        @Schema(description = "分组ID")
        private Integer groupId;

        @Schema(description = "分组名称")
        private String groupName;

        public SalesmanGroupInfo(SalesmanGroup salesmanGroup) {
            if (salesmanGroup != null) {
                this.groupId = salesmanGroup.getGroupId();
                this.groupName = salesmanGroup.getGroupName();
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分销员信息")
    public static class SalesmanInfo {

        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "手机号")
        private String mobile;

        @Schema(description = "分销员名称")
        private String username;

        @Schema(description = "分销员昵称")
        private String nickname;

        @Schema(description = "分销员头像")
        private String avatar;

        @Schema(description = "上级分销员ID")
        private Integer userId;

        @Schema(description = "分销注册时间")
        private String distributionRegisterTime;

        public SalesmanInfo(User user) {
            this.mobile = user.getMobile();
            this.username = user.getUsername();
            this.nickname = user.getNickname();
            this.avatar = user.getAvatar();
            this.userId = user.getUserId();
            this.distributionRegisterTime = TigUtils.handelTime((long) user.getDistributionRegisterTime());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "销售订单信息")
    public static class SalesmanOrderInfo {

        // *** SalesmanOrder ***

        @Schema(description = "分销员订单ID")
        private Integer salesmanOrderId;

        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "分销员ID")
        private Integer salesmanId;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "状态")
        private Integer status;

        @Schema(description = "商品ID")
        private Integer itemId;

        @Schema(description = "订单金额")
        private BigDecimal orderAmount;

        // *** Other ***

        @Schema(description = "状态文本")
        private String statusText;

        @Schema(description = "添加时间")
        private String addTime;

        public SalesmanOrderInfo(SalesmanOrder salesmanOrder) {
            this.salesmanOrderId = salesmanOrder.getSalesmanOrderId();
            this.orderId = salesmanOrder.getOrderId();
            this.salesmanId = salesmanOrder.getSalesmanId();
            this.amount = salesmanOrder.getAmount();
            this.status = salesmanOrder.getStatus();
            this.itemId = salesmanOrder.getItemId();
            this.orderAmount = salesmanOrder.getOrderAmount();

            this.statusText = SalesmanOrderStatus.getByCode(salesmanOrder.getStatus());
            this.addTime = TigUtils.handelTime(salesmanOrder.getAddTime());
        }
    }

    public SalesmanStatisticalVO(Salesman salesman, User user, SalesmanGroup salesmanGroup, List<SalesmanOrder> salesmanOrders) {
        this.salesmanId = salesman.getSalesmanId();
        this.level = salesman.getLevel();
        this.groupId = salesman.getGroupId();
        this.pid = salesman.getPid();
        this.shopId = salesman.getShopId();
        this.saleAmount = salesman.getSaleAmount();

        this.addTime = TigUtils.handelTime(salesman.getAddTime());
        this.salesmanName = user.getNickname();

        this.baseUserInfo = new SalesmanInfo(user);
        this.groupInfo = new SalesmanGroupInfo(salesmanGroup);

        this.salesmanOrderInfo = salesmanOrders.stream().map(SalesmanOrderInfo::new).toList();
    }
}

