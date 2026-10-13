package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.AllocateProductImportErrorExcelDTO;
import com.tigshop.bean.dto.product.ProductStockDTO;
import com.tigshop.bean.dto.product.ProductStockInDTO;
import com.tigshop.bean.dto.product.ProductStockOutExcelDTO;
import com.tigshop.bean.model.product.ProductStockRecord;
import com.tigshop.bean.vo.product.ProductImportErrorExcelVO;
import com.tigshop.bean.vo.product.ProductImportSuccessListVO;
import com.tigshop.bean.vo.product.ProductStockPageVO;
import com.tigshop.bean.vo.product.ProductStockRecordVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 13978305776
* @description 针对表【product_stock_record(商品出入库记录)】的数据库操作Service
* @createDate 2025-09-30 13:10:59
*/
public interface ProductStockRecordService extends BaseService<ProductStockRecord> {

    Page<ProductStockRecordVO> productStockRecordPage(ProductStockDTO productStockDTO);

    ProductStockRecordVO getRecordDetail(Integer id);

    boolean productStockIn(ProductStockInDTO productStockInDTO);

    boolean productStockOut(ProductStockInDTO productStockInDTO);

    /**
     * 供应链内部库存入口：显式指定业务归属组织，返回原库存记录ID。
     */
    Integer productStockInForShop(Integer shopId, ProductStockInDTO productStockInDTO);

    /**
     * 供应链内部库存入口：显式指定业务归属组织，返回原库存记录ID。
     */
    Integer productStockOutForShop(Integer shopId, ProductStockInDTO productStockInDTO);

    void downloadTemplate(HttpServletResponse response);

    List<ProductImportSuccessListVO> batchImport(MultipartFile file);

    void getImportErrorData(MultipartFile file, HttpServletResponse response);


    List<ProductImportSuccessListVO> batchImportAllocateProduct(MultipartFile file, Integer shopId, Integer targetShopId);

    void getAllocateProductErrorData(MultipartFile file, Integer shopId, Integer targetShopId, HttpServletResponse response);
}
