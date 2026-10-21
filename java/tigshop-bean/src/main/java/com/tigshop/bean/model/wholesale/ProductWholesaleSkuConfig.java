// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.wholesale;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SKU级批发配置表（倍数购买）。
 *
 * <p>CR-20260604-0937：删除 {@code moqSku} 字段，起批数统一到
 * {@link ProductWholesaleConfig#getMoq()}（不论 SPU/SKU 维度）；本表仅承载 {@code multiple}。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@TableName(value = "product_wholesale_sku_config")
@Data
public class ProductWholesaleSkuConfig {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** product.product_id */
    private Integer productId;

    /** product_sku.sku_id */
    private Integer skuId;

    /** 倍数购买（1=不限制），任何 dimension 下都按 SKU */
    private Integer multiple;

    /** 0=有效 1=删除（逻辑删除） */
    @TableLogic
    private Integer isDelete;

    /** 创建时间（Unix秒） */
    private Long createTime;

    /** 更新时间（Unix秒） */
    private Long updateTime;
}
