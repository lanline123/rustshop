package com.tigshop.mapper.deposit;

import com.tigshop.bean.model.deposit.ShopDeposit;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShopDepositMapper extends BaseMapper<ShopDeposit> {

    /**
     * 按 owner 维度加锁读取账户行（FOR UPDATE）。
     */
    ShopDeposit selectForUpdateByOwner(@Param("ownerType") Integer ownerType, @Param("ownerId") Integer ownerId);

    /**
     * 店铺在售商品类目：product 表 shop_id=? 且在售(product_status=1)、未删(is_delete=0)、category_id>0 的去重 category_id。
     */
    List<Integer> selectOnSaleCategoryIds(@Param("shopId") Integer shopId);

    /**
     * 供应商在售商品类目：vendor_product 表 vendor_id=? 且在售(product_state=1)、审核通过(audit_state=1)、
     * 未删(is_del=0)、product_category_id>0 的去重 product_category_id。
     */
    List<Integer> selectVendorOnSaleCategoryIds(@Param("vendorId") Integer vendorId);

    /**
     * 供应商在售商品id：vendor_product 表 vendor_id=? 且在售(product_state=1)、审核通过(audit_state=1)、未删(is_del=0)。
     *
     * <p>W5 保证金全店下架用。直接走 SQL（不引用 vendor 模块 Java 类型），使本模块在 vendor feature
     * 关闭时仍可独立编译/运行。</p>
     */
    List<Integer> selectVendorOnSaleProductIds(@Param("vendorId") Integer vendorId);

    /**
     * 物理下架供应商商品：vendor_product 表 id ∈ ids 置 product_state=0。返回影响行数。
     */
    int offShelfVendorProducts(@Param("ids") List<Integer> ids);

    /**
     * 恢复上架供应商商品：vendor_product 表 id ∈ ids 且未删(is_del=0) 置 product_state=1。返回影响行数。
     */
    int onShelfVendorProducts(@Param("ids") List<Integer> ids);
}
