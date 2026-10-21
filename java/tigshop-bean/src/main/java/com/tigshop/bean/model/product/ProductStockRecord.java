package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品出入库记录
 * @TableName product_stock_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品出入库记录")
@TableName("product_stock_record")
public class ProductStockRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer productStockId;

    /**
     * 仓库编号
     */
    @Schema(description = "仓库编号")
    private String stockCode;

    /**
     * 类型（1=入库，2=出库）
     */
    @Schema(description = "类型（1=入库，2=出库）")
    private Integer inOrOut;

    /**
     * 11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库
     */
    @Schema(description = "11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库")
    private Integer type;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Long addTime;

    /**
     * 经办人
     */
    @Schema(description = "经办人")
    private String addBy;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String comment;

    /**
     * 商城ID
     */
    @Schema(description = "商城ID")
    private Integer shopId;

}