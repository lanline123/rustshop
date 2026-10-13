// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满减活动model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("product_promotion")
@Schema(description = "满减活动")
public class ProductPromotion implements Serializable {

    @TableId(value = "promotion_id", type = IdType.AUTO)
    @Schema(description = "活动ID")
    private Integer promotionId;

    @Schema(description = "活动名称")
    private String promotionName;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "[JSON]仅限会员等级")
    private String limitUserRank;

    @Schema(description = "活动范围:0,全部商品;3,指定商品.4，不含以下商品")
    @TableField(value = "`range`")
    private Integer range;

    @Schema(description = "[json]活动范围相关信息")
    private String rangeData;

    @Schema(description = "订单金额下限")
    private BigDecimal minOrderAmount;

    @Schema(description = "订单金额上限，可用于阶梯设置")
    private BigDecimal maxOrderAmount;

    @Schema(description = "优惠类型，1：满减，2：折扣，3：满赠")
    private Integer promotionType;

    @Schema(description = "[data]类型相关数据，如满减金额、折扣金额、赠品等")
    @TableField(value = "promotion_type_data")
    private String promotionTypeData;

    @Schema(description = "是否启用")
    private Integer isAvailable;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "优惠规则：1 阶梯优惠 0 循环优惠")
    private Integer rulesType;

    @Schema(description = "优惠单位：1 元 2 件")
    private Integer unit;
}