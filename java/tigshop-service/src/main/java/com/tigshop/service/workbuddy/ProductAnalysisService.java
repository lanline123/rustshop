package com.tigshop.service.workbuddy;

import com.tigshop.bean.dto.workbuddy.ProductAnalysisDTO;
import com.tigshop.bean.vo.workbuddy.ProductAnalysisVO;

/**
 * 商品数据分析（只读）。返回结构使用稳定的 map 契约，供 MCP 层直接序列化。
 */
public interface ProductAnalysisService {

    ProductAnalysisVO analyze(ProductAnalysisDTO query, Integer shopId);
}
