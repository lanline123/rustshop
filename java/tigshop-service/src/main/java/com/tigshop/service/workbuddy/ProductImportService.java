package com.tigshop.service.workbuddy;

import com.tigshop.bean.dto.workbuddy.ProductDraftDTO;
import com.tigshop.bean.vo.workbuddy.ProductCategoryVO;
import com.tigshop.bean.vo.workbuddy.ProductImportResultVO;
import com.tigshop.bean.vo.workbuddy.ProductRequirementsVO;
import com.tigshop.bean.vo.workbuddy.ProductValidationVO;

import java.util.List;

public interface ProductImportService {

    ProductRequirementsVO requirements();

    /**
     * 检索分类，供调用方定位导入所需的 categoryId。
     *
     * <p>两种用法：给 keyword 按名称模糊检索；给 parentId 列出其下级。
     * 二者都为空时返回顶级分类，供调用方从头下钻。
     */
    List<ProductCategoryVO> categories(String keyword, Integer parentId);

    ProductValidationVO validate(List<ProductDraftDTO> products, Integer shopId);

    /**
     * 逐商品创建。必须携带由 {@link #validate} 生成的 previewId 和 Idempotency-Key。
     *
     * @param products     客户端提交的商品；可为空，为空时以预览留存的数据为准
     * @param previewId    校验接口返回的预览标识
     * @param confirmation 用户已确认预览结果
     */
    ProductImportResultVO importProducts(List<ProductDraftDTO> products, Integer shopId, String previewId,
                                         String idempotencyKey, boolean confirmation);
}
