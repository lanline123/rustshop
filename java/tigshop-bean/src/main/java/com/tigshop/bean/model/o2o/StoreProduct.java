package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分配店铺商品
 */
@TableName(value = "store_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreProduct implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long storeProductId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "门店覆盖主图")
    private String picUrl;

    @Schema(description = "门店覆盖详情页")
    private String productDesc;

    @Schema(description = "商品状态；1-上架，0-下架")
    private Integer productStatus;

    @Schema(description = "产品库存")
    private Integer productStock;

    @Schema(description = "门店id")
    private Long shopId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "卡组ID")
    private Integer cardGroupId;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "预约模板Id")
    private Integer reservationTplId;

    @Schema(description = "配送员服务费类型（门店覆盖，仅单规格商品使用）：0=固定金额 1=按比例")
    private Integer deliveryServiceFeeType;

    @Schema(description = "配送员服务费值（门店覆盖，仅单规格商品使用）")
    private BigDecimal deliveryServiceFeeValue;

    @Schema(description = "服务员服务费类型（门店覆盖，仅单规格商品使用）：0=固定金额 1=按比例")
    private Integer serverServiceFeeType;

    @Schema(description = "服务员服务费值（门店覆盖，仅单规格商品使用）")
    private BigDecimal serverServiceFeeValue;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}