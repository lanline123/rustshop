package com.tigshop.service.workbuddy;

import com.tigshop.bean.vo.workbuddy.ProductContextVO;

public interface ProductContextService {
    ProductContextVO context(String keyword, Integer categoryId, Integer sampleSize, Integer shopId);
}
