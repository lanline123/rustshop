package com.tigshop.bean.query.order;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 订单管理列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "订单管理列表参数")
public class OrderListPageQuery extends BasePage {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "会员标签ID集合（二期，按会员标签筛选订单）")
    private List<Long> memberTagIds;

    @Schema(description = "会员标签匹配模式：0-满足任一，1-满足全部")
    private Integer memberTagMatchMode;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "发货状态")
    private Integer shippingStatus;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "物流ID")
    private Long logisticsId;

    @Schema(description = "预约开始时间")
    private String reservationStartTime;

    @Schema(description = "添加结束时间")
    private String reservationEndTime;

    @Schema(description = "添加开始时间")
    private String addStartTime;

    @Schema(description = "添加结束时间")
    private String addEndTime;

    @Schema(description = "评论状态")
    private Integer commentStatus;

    @Schema(description = "是否结算")
    private Integer isSettlement;

    @Schema(description = "是否兑换订单")
    private Integer isExchangeOrder;

    @Schema(description = "标记")
    private Integer mark;

    // *** UNKNOWN ***

    @Schema(description = "orderIds")
    private List<Integer> orderIds;

    @Schema(description = "支付时间")
    private String payTime;

    @NotBlank(message = "请选择导出项")
    @Schema(description = "导出项")
    private String exportItem;

    @Schema(description = "日期检索")
    private Integer dataType;
    //待晒单
    @Schema(description = "是否展示", allowableValues = "-1, 0, 1")
    private Integer isShowed;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "支付方式")
    private String payCode;

    //订单来源
    @Schema(description = "订单来源")
    private String orderSource;

    @Schema(description = "渠道子来源")
    private String orderSubSource;

    @Schema(description = "微信直播主键ID")
    private Integer wechatLiveId;

    @Schema(description = "微信直播房间ID")
    private Integer wechatLiveRoomId;

    @Schema(description = "微信直播商品ID")
    private Integer wechatGoodsId;

    @Schema(description = "直播分享人OpenId")
    private String liveShareOpenId;

    @Schema(description = "直播同步状态")
    private Integer liveSyncStatus;

    @Schema(description = "是否周期购订单：0-否，1-是")
    private Integer isCycleOrder;

    @Schema(description = "是否次卡订单：0-否，1-是")
    private Integer isCardOrder;

    @Schema(description = "周期购交付模式筛选：1-计划配送，2-按需兑换")
    private Integer cycleDeliveryType;

    @Schema(description = "父订单ID（用于查询周期购子订单）")
    private Integer parentOrderId;

    @Schema(description = "交易号")
    private String outTradeNo;

    @Schema(description = "是否包含已失效订单（拆单父订单）")
    private Boolean includeInvalid;

    @Schema(description = "发票场景：为true时返回已完成订单或已提交发票申请的订单")
    private Boolean invoiceScope;
}
