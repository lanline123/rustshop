package com.tigshop.service.product;

import com.tigshop.bean.dto.product.ProductBatchDealDTO;
import com.tigshop.bean.dto.product.ProductBatchEditDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.product.ProductBatchModifyVO;
import com.tigshop.bean.vo.product.ProductImportExcelVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 产品批量操作 服务
 *
 * @author kidd
 * @since 2025/3/26 16:16
 */
public interface ProductBatchService extends BaseService<Product> {

    /**
     * 下载商品导入模版（直接输出 xlsx 流）
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 商品导出Excel
     */
    List<ProductImportExcelVO> productBatchDeal(ProductBatchDealDTO dto);

    /**
     * 商品导入
     */
    ProductBatchModifyVO productBatchModify(Integer isAutoCat, Integer isAutoBrand, Integer isChangePic, Integer isUpload, MultipartFile file);

    /**
     * 商品批量修改
     */
    boolean productBatchEdit(ProductBatchEditDTO dto);
}
