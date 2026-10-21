package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.bo.vendort.VendorProductBO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.param.vendor.product.VendorProductAuditParam;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.bean.query.vendor.VendorProductClientPageQuery;
import com.tigshop.bean.query.vendor.VendorProductPageQuery;
import com.tigshop.bean.query.vendor.VendorProductPlatformPageQuery;
import com.tigshop.bean.vo.vendor.product.*;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
* @author kidd
*/
public interface VendorProductService extends BaseService<VendorProduct> {

    /**
     * 创建
     */
    String create(VendorProductSaveParam param);

    /**
     * 编辑
     */
    String edit(VendorProductEditParam param);

    /**
     * 修改状态
     */
    void changeState(OperateDTO operate);

    /**
     * 供应商商品列表
     */
    Page<VendorProductPageVO> pageList(VendorProductPageQuery pageQuery);

    /**
     * 供应商商品详情
     */
    VendorProductDetailVO detail(Integer id);

    /**
     * 根据供应商ID下架相关产品
     *
     * @param vendorId 供应商ID
     */
    void discontinueByVendorId(Integer vendorId);

    /**
     * 根据供应商产品ID下架产品
     *
     * @param vendorProductId 供应商产品ID
     */
    void discontinueByVendorProductId(Integer vendorProductId);

    /**
     * 根据供应商产品ID集合下架产品
     *
     * @param vendorProductIds 供应商产品ID集合
     */
    void discontinueByVendorProductIds(List<Integer> vendorProductIds);

    /**
     * 平台端: 供应商商品列表
     */
    Page<VendorProductPlatformPageVO> platformPageList(VendorProductPlatformPageQuery pageQuery);

    /**
     * 平台端: 供应商商品详情
     */
    VendorProductPlatformDetailVO platformDetail(Integer id);

    /**
     * 平台端: 供应商商品审核
     */
    void audit(VendorProductAuditParam param);

    /**
     * 平台端: 待审核供应商商品数量
     */
    Long getWaitingCheckedVendorCount();

    /**
     * 客户端: 供应商商品客户端列表
     */
    Page<VendorProductClientPageVO> clientPage(VendorProductClientPageQuery pageQuery);

    /**
     * 查询供货商商品业务对象
     */
    List<VendorProductBO> selectVendorProducts(List<Long> vendorProductIds);

}
