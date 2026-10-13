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
 * 商品级批发配置表（1 product : 1 row）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@TableName(value = "product_wholesale_config")
@Data
public class ProductWholesaleConfig {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** product.product_id */
    private Integer productId;

    /** 归属店铺：0=平台 >0=店铺（随 product.shop_id 冗余，CR-20260610-162600 多租户隔离） */
    private Integer shopId;

    /** 多规格价格模式：1=商品级混批 2=SKU级独立 */
    private Integer multiSpecPriceMode;

    /** 起批维度：1=SPU 2=SKU */
    private Integer moqDimension;

    /**
     * 起批数（CR-20260604-0937 统一字段）：
     * <ul>
     *   <li>{@code moq_dimension=1}（SPU）：同 SPU 下批发行加总起批数</li>
     *   <li>{@code moq_dimension=2}（SKU）：每个 SKU 的起批数（应用到所有 SKU）</li>
     * </ul>
     */
    private Integer moq;

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
