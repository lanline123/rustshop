package com.tigshop.bean.param.promotion.giftcard;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.promotion.GiftGroupsEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCardProducts;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月29日 10:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "卡分组更新参数")
public class GiftCardGroupsUpdateParam {

    @Schema(description = "卡组id")
    private Integer cardGroupsId;

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

    @Schema(description = "商品列表")
    private List<GiftCardProducts> giftCardProducts;

    public GiftCardGroups buildGiftCardGroups(GiftCardGroupsUpdateParam param){
        GiftCardGroups giftCardGroups = BeanUtil.copyProperties(param, GiftCardGroups.class);
        giftCardGroups.setUpdateTime(StringUtils.getCurrentTime());

        if (param.getValidityType().equals(ValidityTypeEnum.NUMBER.getCode())) {
            Assert.notNull(param.getValidityDays(), () -> new GlobalException("请填写天数有效期"));
        }

        if (param.getValidityType().equals(ValidityTypeEnum.FIXED.getCode())) {
            Assert.notNull(param.getExpireTime(), () -> new GlobalException("请填写固定时间有效期"));

            giftCardGroups.setExpireTime(StringUtils.getEndOfDayTimestamp(param.getExpireTime()));
        }

        return giftCardGroups;
    }
}