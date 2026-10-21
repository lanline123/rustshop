package com.tigshop.adminapi.controller.proxyshopping;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductListDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.product.ProductAvailabilityVO;
import com.tigshop.bean.vo.product.ProductDetailVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 代客下单商品管理控制器
 * 只支持普通商品和虚拟商品
 *
 * @author Tigshop项目组
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/proxyShopping/product")
@Tag(name = "代客下单商品管理", description = "代客下单商品管理")
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingProductController {

    public static final List<Integer> ADMIN_PROXY_SHOPPING_PRODUCT_TYPES = Arrays.asList(ProductType.PRODUCT_TYPE_NORMAL.getCode()
            , ProductType.PRODUCT_TYPE_VIRTUAL.getCode(), ProductType.PRODUCT_TYPE_CARD.getCode()
            , ProductType.PRODUCT_TYPE_PAID.getCode(), ProductType.PRODUCT_TYPE_GIFT_CARD.getCode()
            , ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode());

    private final ProductService productService;

    @GetMapping("/productDetail")
    @Operation(summary = "商品详情")
    public ProductDetailVO detail(@RequestParam(value = "productTsn", required = false) String productTsn,
                                  @RequestParam(value = "productSn", required = false) String productSn,
                                  @RequestParam(value = "shopId", required = false) Integer shopId,
                                  @RequestParam(value = "nearestShopId", required = false) Integer nearestShopId) {
        Product product = productService.lambdaQuery().eq(Product::getProductTsn, productTsn).last("limit 1").one();
        if (productSn != null) {
            return productService.clientDetail(productSn, shopId, nearestShopId);
        }
        return productService.clientDetail(product.getProductSn(), shopId, nearestShopId);
    }

    @GetMapping("/getProductAvailability")
    @Operation(summary = "商品可用数据")
    public ProductAvailabilityVO getProductAvailability(@RequestParam("id") Integer productId,
                                                        @RequestParam(name = "skuId", required = false) Integer skuId,
                                                        @RequestParam(name = "extraAttrIds", required = false) String extraAttrIds,
                                                        @RequestParam(value = "shopId", required = false) Integer shopId,
                                                        @RequestParam(value = "addressId", required = false) Integer addressId,
                                                        @RequestParam(value = "userId", required = false) Integer userId) {
        return productService.getProductSkuDetail(productId, skuId, 0, extraAttrIds, shopId, addressId, userId);
    }

    @PostMapping("/getProduct")
    @Operation(summary = "商品列表")
    public Page<ProductListResDTO> list(@RequestBody ProductListDTO DTO) {
        DTO.setProductTypes(ADMIN_PROXY_SHOPPING_PRODUCT_TYPES);
        DTO.setIsDelete(0);
        DTO.setProductStatus(1);
        DTO.setShopId(HeaderUtils.getShopId());
        //排除预售商品
        DTO.setSaleMode(0);
        return productService.list(DTO);
    }
}
