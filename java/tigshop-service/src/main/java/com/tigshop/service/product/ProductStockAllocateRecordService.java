package com.tigshop.service.product;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.dto.product.AllocateStockOutDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateExcelExportDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateRecordCreateDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateRecordDTO;
import com.tigshop.bean.model.product.ProductStockAllocateRecord;
import com.tigshop.bean.vo.product.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 13978305776
* @description 针对表【product_stock_allocate_recoed(库存调拨表)】的数据库操作Service
* @createDate 2025-10-11 14:07:43
*/
public interface ProductStockAllocateRecordService extends IService<ProductStockAllocateRecord> {

    Page<ProductStockAllocateRecordPageVO> getProductStockAllocateRecordPage(ProductStockAllocateRecordDTO productStockAllocateRecordDTO);

    ProductStockAllocateRecordDetailVO productStockAllocateRecordDetail(Integer id);

    boolean createAllocate(ProductStockAllocateRecordCreateDTO createDTO);

    boolean auditAllocate(Integer id,Integer auditType,String remark);

    boolean stockOutAllocate(AllocateStockOutDTO allocateStockOutDTO);

    boolean cancelAllocate(List<Integer> allocateIds);

    boolean stockInAllocate(AllocateStockOutDTO allocateStockOutDTO);

    List<AllocateOrgVO> getOrgList();

    List<String> getAddBy();

    List<ProductStockAllocateExcelVO> exportAllocate(ProductStockAllocateExcelExportDTO excelExportDTO);

    boolean batchImportAllocate(MultipartFile file);

    void getBatchImportErrorData(MultipartFile file, HttpServletResponse response);
}
