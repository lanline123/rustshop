package com.tigshop.service.vendor;

import com.tigshop.bean.dto.vendor.VendorProductBatchDealDTO;
import com.tigshop.bean.dto.vendor.VendorProductBatchEditDTO;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.vo.vendor.product.VendorProductBatchModifyVO;
import com.tigshop.bean.vo.vendor.product.VendorProductImportExcelVO;
import com.tigshop.service.common.BaseService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 供应商商品批量操作 服务
 */
public interface VendorProductBatchService extends BaseService<VendorProduct> {

    /**
     * 下载供应商商品导入模版
     */
    List<VendorProductImportExcelVO> downloadTemplate();

    /**
     * 供应商商品导出Excel
     */
    List<VendorProductImportExcelVO> vendorProductBatchDeal(VendorProductBatchDealDTO dto);

    /**
     * 供应商商品导入
     */
    VendorProductBatchModifyVO vendorProductBatchModify(MultipartFile file);

    /**
     * 供应商商品批量修改
     */
    boolean vendorProductBatchEdit(VendorProductBatchEditDTO dto);
}
