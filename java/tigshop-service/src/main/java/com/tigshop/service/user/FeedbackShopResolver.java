package com.tigshop.service.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.user.Feedback;
import com.tigshop.bean.enums.user.UserFeedbackType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.user.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 根据权威商品数据解析商品咨询的最终归属店铺。
 */
@Component
@RequiredArgsConstructor
public class FeedbackShopResolver {

    private final ProductMapper productMapper;
    private final StoreProductMapper storeProductMapper;
    private final FeedbackMapper feedbackMapper;
    private final TigshopProperties tigshopProperties;

    public int resolveProductShopId(Integer productId, Integer requestedShopId) {
        if (productId == null || productId <= 0) {
            throw new GlobalException("商品不存在");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new GlobalException("商品不存在");
        }

        int productShopId = product.getShopId() == null ? 0 : product.getShopId();
        if (!isO2o() || productShopId > 0) {
            return productShopId;
        }

        if (requestedShopId == null || requestedShopId <= 0) {
            return 0;
        }
        Long storeProductCount = storeProductMapper.selectCount(Wrappers.<StoreProduct>lambdaQuery()
                .eq(StoreProduct::getProductId, productId)
                .eq(StoreProduct::getShopId, requestedShopId)
                .eq(StoreProduct::getProductStatus, 1)
                .eq(StoreProduct::getIsDelete, 0));
        if (storeProductCount == null || storeProductCount <= 0) {
            throw new GlobalException("商品不属于当前门店或已下架");
        }
        return requestedShopId;
    }

    public int countProductConsultations(Integer productId, Integer requestedShopId) {
        int finalShopId = resolveProductShopId(productId, requestedShopId);
        Long count = feedbackMapper.selectCount(Wrappers.<Feedback>lambdaQuery()
                .eq(Feedback::getProductId, productId)
                .eq(Feedback::getShopId, finalShopId)
                .eq(Feedback::getType, UserFeedbackType.PRODUCT.getCode())
                .eq(Feedback::getParentId, 0));
        return count == null ? 0 : Math.toIntExact(count);
    }

    private boolean isO2o() {
        return tigshopProperties.getIsO2o() != null && tigshopProperties.getIsO2o() == 1;
    }
}
