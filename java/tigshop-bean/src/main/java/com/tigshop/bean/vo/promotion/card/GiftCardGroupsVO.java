package com.tigshop.bean.vo.promotion.card;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.enums.promotion.GiftCardEnum;
import com.tigshop.bean.enums.promotion.GiftGroupsEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.vo.product.ProductSkuVO;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月28日 11:00
 */
@Data
@Schema(description = "卡券分组")
public class GiftCardGroupsVO {
    @Schema(description = "卡组id")
    private Integer cardGroupsId;

    @Schema(description = "1-兑换卡 2-储值卡")
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
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal faceAmount;

    @Schema(description = "1-开启 0-关闭")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    @Schema(description = "更新时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String updateTime;

    @Schema(description = "卡组类型名称")
    private String groupTypeName;

    @Schema(description = "卡类型名称")
    private String cardTypeName;

    @Schema(description = "有效期类型名称")
    private String validityTypeName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "商品信息")
    private List<ProductSkuVO> giftCardProducts;

    @Schema(description = "是否可以点击删除按钮")
    private Boolean canDelete;


    public void initTypeName(GiftCardGroupsVO vo){
        this.groupTypeName = GiftGroupsEnum.getByCode(vo.getGroupType()).getDescription();
        this.cardTypeName = GiftCardEnum.getByCode(vo.getCardType()).getDescription();
        this.validityTypeName = ValidityTypeEnum.getByCode(vo.getValidityType()).getDescription();
    }
}