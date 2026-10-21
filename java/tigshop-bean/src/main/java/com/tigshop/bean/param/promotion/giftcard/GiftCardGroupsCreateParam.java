package com.tigshop.bean.param.promotion.giftcard;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.promotion.GiftGroupsEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCardProducts;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月29日 10:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "卡分组创建参数")
public class GiftCardGroupsCreateParam {

    @Schema(description = "1-储值卡 2-兑换卡")
    private Integer groupType;

    @Schema(description = "1-电子卡 2-实体卡")
    private Integer cardType;

    @Schema(description = "卡组名称")
    private String cardGroupName;

    @Schema(description = "卡图片URL")
    private String imageUrl;

    @Schema(description = "有效期类型")
    private Integer validityType;

    @Schema(description = "有效期天数")
    private Integer validityDays;

    @Schema(description = "到期时间")
    private String expireTime;

    @Schema(description = "面额(储值卡专用)")
    private BigDecimal faceAmount;

    @Schema(description = "1-开启 0-关闭")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "商品列表")
    private List<GiftCardProducts> giftCardProducts;

    public GiftCardGroups buildGiftCardGroups(GiftCardGroupsCreateParam param){
        GiftCardGroups giftCardGroups = BeanUtil.copyProperties(param, GiftCardGroups.class);
        giftCardGroups.setAddTime(StringUtils.getCurrentTime());

        if (param.getShopId() == null) {
            giftCardGroups.setShopId(HeaderUtils.getShopId());
        }

        if (param.getValidityType().equals(ValidityTypeEnum.NUMBER.getCode())) {
            Assert.notNull(param.getValidityDays(), () -> new GlobalException("请填写天数有效期"));
        }

        if (param.getValidityType().equals(ValidityTypeEnum.FIXED.getCode())) {
            Assert.notNull(param.getExpireTime(), () -> new GlobalException("请填写固定时间有效期"));

            // 修改此处：将时间设置为当天的 23:59:59
            giftCardGroups.setExpireTime(StringUtils.getEndOfDayTimestamp(param.getExpireTime()));
        }

        if (param.getGroupType().equals(GiftGroupsEnum.EXCHANGE.getCode())){
            Assert.notNull(param.getGiftCardProducts(), () -> new GlobalException("请最少选择一个商品"));
        }

        if (param.getGroupType().equals(GiftGroupsEnum.TOP_UP.getCode())){
            Assert.notNull(param.getFaceAmount(), () -> new GlobalException("请填写面额"));
        }
        return giftCardGroups;
    }
}