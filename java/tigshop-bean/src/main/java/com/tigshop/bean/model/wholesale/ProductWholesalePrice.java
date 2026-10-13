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

import java.math.BigDecimal;

/**
 * 三维批发价表（SKU × 等级 × 阶梯）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@TableName(value = "product_wholesale_price")
@Data
public class ProductWholesalePrice {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** product.product_id */
    private Integer productId;

    /** 0=SPU兜底 >0=具体SKU */
    private Integer skuId;

    /** 0=通用价 >0=特定等级 */
    private Integer gradeId;

    /** 阶梯起量 */
    private Integer tierMinQty;

    /** 1=折扣换算 2=直接金额 */
    private Integer priceMode;

    /** 折率 ∈ [0.10, 9.99]，price_mode=1 时用 */
    private BigDecimal discount;

    /** 固定批发价，price_mode=2 时用 */
    private BigDecimal wholesalePrice;

    /** 0=禁用 1=启用 */
    private Integer status;

    /** 0=有效 1=删除（逻辑删除） */
    @TableLogic
    private Integer isDelete;

    /** 创建时间（Unix秒） */
    private Long createTime;

    /** 更新时间（Unix秒） */
    private Long updateTime;
}
