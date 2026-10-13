// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信直播商品主表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("wechat_live_goods")
@Schema(description = "微信直播商品主表")
public class WechatLiveGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "微信商品ID")
    @TableField(value = "wechat_goods_id")
    private Integer wechatGoodsId;

    @Schema(description = "系统商品ID")
    @TableField(value = "product_id")
    private Long productId;

    @Schema(description = "系统SKU ID，0表示无规格")
    @TableField(value = "product_sku_id")
    private Long productSkuId;

    @Schema(description = "商品名称")
    @TableField(value = "goods_name")
    private String goodsName;

    @Schema(description = "商品封面图")
    @TableField(value = "cover_img")
    private String coverImg;

    @Schema(description = "商品价格（分）")
    @TableField(value = "price")
    private Long price;

    @Schema(description = "价格类型：1-一口价，2-价格区间，3-显示折扣价")
    @TableField(value = "price_type")
    private Integer priceType;

    @Schema(description = "商品小程序路径")
    @TableField(value = "url")
    private String url;

    @Schema(description = "商品状态：0-未审核，1-审核中，2-审核通过，3-审核驳回")
    @TableField(value = "audit_status")
    private Integer auditStatus;

    @Schema(description = "创建时间")
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @Schema(description = "店铺ID")
    @TableField(value = "shop_id")
    private Integer shopId;

    @Schema(description = "实时库存（联表查询返回，非持久化字段）")
    @TableField(exist = false)
    private Integer stock;
}
