// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.order.ReturnPicDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.AftersalesTypeEnum;
import com.tigshop.bean.model.order.*;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "售后管理参数")
public class AftersalesApplyParam {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "售后类型")
    private Integer aftersaleType;

    @Schema(description = "售后原因")
    private String aftersaleReason;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "反馈图片")
    private List<ReturnPicDTO> pics;

    @NotEmpty(message = "请填写申请售后数量")
    @Schema(description = "items")
    private List<ItemsDTO> items;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "是否是后台取消订单")
    private Boolean isAdminCancel = false;

    @Data
    public static class ItemsDTO {
        @Schema(description = "订单item_id")
        private Integer orderItemId;

        @Schema(description = "数量")
        private Integer number;
    }

    public Aftersales createAfterSales(Integer orderId, Integer shopId, Integer userId) {

        String aftersalesSnPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String aftersalesSnSuffix = RandomUtil.randomNumbers(5);
        String aftersalesSn = aftersalesSnPrefix + aftersalesSnSuffix;

        String pics = "";
        if (CollUtil.isNotEmpty(this.pics)) {
            pics = JSON.toJSONString(this.pics);
        }
        this.userId = userId == null ? SecurityUtils.getCurrentUserId() : userId;
        return Aftersales.builder()
                .orderId(orderId)
                .aftersaleType(this.aftersaleType)
                .aftersaleReason(this.aftersaleReason)
                .description(this.description)
                .refundAmount(this.refundAmount)
                .userId(this.userId)
                .status(AftersalesStatusEnum.IN_REVIEW.getCode())
                .addTime(StringUtils.getCurrentTime())
                .shopId(shopId)
                .aftersalesSn(aftersalesSn)
                .pics(pics)
                .build();
    }

    public List<AftersalesItem> createAfterSalesItems(Integer aftersaleId) {
        return this.items.stream()
                .map(item -> AftersalesItem.builder()
                        .aftersaleId(aftersaleId)
                        .orderItemId(item.getOrderItemId())
                        .number(item.getNumber())
                        .build()).toList();
    }

    public AftersalesLog createAftersalesLog(Integer aftersaleId) {

        String logInfo = "会员发起了" + AftersalesTypeEnum.getTypeName(this.aftersaleType) + ": " + this.description;

        String pics = null;
        if (CollUtil.isNotEmpty(this.pics)) {
            pics = JSON.toJSONString(this.pics);
        }

        return AftersalesLog.builder()
                .aftersaleId(aftersaleId)
                .logInfo(logInfo)
                .userName(SecurityUtils.getCurrentUsername())
                .addTime(StringUtils.getCurrentTime().toString())
                .returnPic(pics)
                .build();
    }

    public AdminMsgCreateDTO createAdminMsgCreateDTO(Order order, OrderItem orderItem, Aftersales aftersales) {

        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("aftersaleId", aftersales.getAftersaleId());
        relatedData.put("aftersalesSn", aftersales.getAftersalesSn());

        return AdminMsgCreateDTO.builder()
                .msgType(AdminMsgTypeEnum.AFTER_SALE_REQUEST.getCatId())
                .shopId(order.getShopId())
                .orderId(order.getOrderId())
                .title("售后申请通知:" + orderItem.getProductName())
                .content("您的订单【" + order.getOrderSn() + "】发起售后申请，请及时处理。")
                .relatedData(relatedData)
                .build();
    }
}
