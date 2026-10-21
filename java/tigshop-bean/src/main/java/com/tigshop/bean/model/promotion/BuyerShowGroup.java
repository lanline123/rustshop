package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 买家秀分组
 *
 * @author wzh
 * &#064;TableName  buyer_show_group
 */
@TableName(value = "buyer_show_group")
@Data
@Schema(description = "买家秀分组")
public class BuyerShowGroup {

    @TableId(type = IdType.AUTO)
    @Schema(description = "自增ID，买家秀分组ID")
    private Integer buyerShowGroupId;

    @JsonTranslate(dataType = 16)
    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "商品分组id（json）")
    private String productGroupId;

    @Schema(description = "店铺id")
    private Integer shopId;
}