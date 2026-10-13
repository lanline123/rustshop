// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.user.CollectProductListDTO;
import com.tigshop.bean.dto.user.CollectProductSaveDTO;
import com.tigshop.bean.dto.user.CollectProductUpdateDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.user.CollectProduct;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.query.user.CollectProductCancelParam;
import com.tigshop.bean.vo.product.ProductAvailabilityVO;
import com.tigshop.bean.vo.user.CollectProductVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.user.CollectProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.user.CollectProductService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.user.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.YES;
import static com.tigshop.common.constant.user.CollectProductConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 商品收藏服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class CollectProductServiceImpl extends BaseServiceImpl<CollectProductMapper, CollectProduct> implements CollectProductService {
    @Resource
    UserService userService;
    @Resource
    ProductService productService;
    @Resource
    ProductSkuService productSkuService;
    @Resource
    UserRankService userRankService;
    @Resource
    GalleryPicService galleryPicService;

    @Override
    public Page<CollectProductVO> list(CollectProductListDTO listDTO) {
        // 分页
        Integer userId = getCurrentUserId();
        Page<CollectProduct> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        MPJLambdaWrapper<CollectProduct> queryWrapper = new MPJLambdaWrapper<>();
        //过滤已下架的商品
        queryWrapper.leftJoin(Product.class, Product::getProductId, CollectProduct::getProductId).eq(Product::getProductStatus, YES);
        queryWrapper.like(ObjectUtil.isNotEmpty(listDTO.getKeyword()), Product::getProductName, listDTO.getKeyword());
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        if (userId > 0) {
            queryWrapper.eq(CollectProduct::getUserId, userId);
        }

        // 执行查询
        Page<CollectProduct> collectProductPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<CollectProduct> collectProductPageRecords = collectProductPage.getRecords();
        if (collectProductPageRecords.isEmpty()) {
            return new Page<>();
        }
        //取出productId
        List<Integer> productIds = collectProductPage.getRecords().stream().map(CollectProduct::getProductId).toList();
        List<Product> products = productService.listByIds(productIds);
        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(productIds);
        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));
        // 转换为VO
        User user = userService.getById(userId);
        UserRank userRank = userRankService.getById(user.getRankId());
        // 如果用户等级不存在，设置默认折扣为100（不打折）
        BigDecimal discount = (userRank != null) ? userRank.getDiscount() :null;
        List<CollectProductVO> collectProductVOList = collectProductPageRecords.stream()
                .map(collectProduct -> {
                    CollectProductVO collectProductVO = new CollectProductVO();
                    BeanUtils.copyProperties(collectProduct, collectProductVO);
                    BeanUtils.copyProperties(user, collectProductVO);

                    Integer productId = collectProduct.getProductId();
                    Product product = productMap.get(productId);
                    if (product != null) {
                        BeanUtils.copyProperties(product, collectProductVO);
                    }
                    collectProductVO.setAddTime(TigUtils.handelTime(collectProduct.getAddTime()));
                    collectProductVO.setDiscount(discount);
                    List<ProductSkuDTO> skus = productSkuMap.get(productId);
                    collectProductVO.setProductSku(skus);
                    LambdaQueryWrapper<ProductSku> queryWrapperSku = new LambdaQueryWrapper<>();
                    queryWrapperSku.eq(ProductSku::getProductId, productId);
                    queryWrapperSku.eq(ProductSku::getIsEnabled, YES);
                    queryWrapperSku.orderByAsc(ProductSku::getSkuPrice);
                    queryWrapperSku.last("limit 1");
                    ProductSku productSku = productSkuService.getOne(queryWrapperSku);
                    if (productSku != null) {
                        collectProductVO.setSkuPrice(productSku.getSkuPrice());
                    }
                    // 获取最终价格
                    int skuId = 0;
                    if (skus != null && !skus.isEmpty()) {
                        skuId = skus.getFirst().getSkuId();
                    }
                    ProductAvailabilityVO detail = productService.getProductSkuDetail(productId, skuId, 0, "", collectProduct.getShopId(),null);
                    collectProductVO.setPrice(detail.getPrice());
                    return collectProductVO;
                }).toList();
        // W6 Wave 3.5：批发商身份命中平台商品（shopId=0，由 copyProperties(product) 带出）时回填首条批发价
        // （CR-20260603-141000 / CD-032）。shopId 经 BeanUtils.copyProperties(product, vo) 自动填充。
        // CR-20260603-1720 / CD-035：同步回填 wholesaleCardMoq（卡片可直接加购起批数）
        // CR-20260613-0943：同步回填 wholesaleMoqDimension（起批维度）+ wholesaleMultiple（SPU 兜底倍数）
        // CR-20260615-1017：传入零售价 map（productPrice），折扣模式批发价据此计算（修复折扣模式全 null）
        Map<Integer, BigDecimal> collectRetailPriceMap = collectProductVOList.stream()
                .filter(vo -> vo.getProductId() != null && vo.getProductPrice() != null)
                .collect(Collectors.toMap(
                        CollectProductVO::getProductId,
                        CollectProductVO::getProductPrice,
                        (a, b) -> a));
        productService.backfillFirstWholesalePriceForListGeneric(
                collectProductVOList,
                CollectProductVO::getShopId,
                CollectProductVO::getProductId,
                CollectProductVO::setFirstWholesalePrice,
                CollectProductVO::setWholesaleCardMoq,
                CollectProductVO::setWholesaleMoqDimension,
                CollectProductVO::setWholesaleMultiple,
                collectRetailPriceMap);
        return PageUtil.convertPage(collectProductPage, collectProductVOList);
    }

    @Override
    public CollectProductVO detail(Integer id) {
        if (id != null) {
            CollectProduct collectProduct = this.getById(id);
            CollectProductVO collectProductVO = new CollectProductVO();
            BeanUtils.copyProperties(collectProduct, collectProductVO);
            return collectProductVO;
        }
        return null;
    }

    @Override
    public void create(CollectProductSaveDTO saveDTO) {
        Integer userId = getCurrentUserId();

        if (saveDTO != null && saveDTO.getProductId() > 0) {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getProductId, saveDTO.getProductId());
            queryWrapper.eq(Product::getIsDelete, 0);
            Product product = productService.getOne(queryWrapper);
            if (product == null) {
                throw new GlobalException(PRODUCT_NOT_EXIST);
            }

            LambdaQueryWrapper<CollectProduct> queryWrapperCollectProduct = new LambdaQueryWrapper<>();
            queryWrapperCollectProduct.eq(CollectProduct::getUserId, userId);
            queryWrapperCollectProduct.eq(CollectProduct::getProductId, saveDTO.getProductId());
            if (this.getOne(queryWrapperCollectProduct) != null) {
                throw new GlobalException(PRODUCT_ALREADY_COLLECT);
            }
            CollectProduct collectProduct = new CollectProduct();
            collectProduct.setProductId(saveDTO.getProductId());
            collectProduct.setUserId(userId);
            collectProduct.setAddTime(StringUtils.getCurrentTime());
            this.save(collectProduct);
        }

    }

    @Override
    public void update(CollectProductUpdateDTO updateDTO) {

        if (updateDTO != null) {
            CollectProduct collectProduct = new CollectProduct();
            BeanUtils.copyProperties(updateDTO, collectProduct);
            this.updateById(collectProduct);
        }

    }

    @Override
    public void cancel(CollectProductCancelParam param) {
        Integer userId = getCurrentUserId();

        CollectProduct collectProduct = this.lambdaQuery()
                .eq(CollectProduct::getProductId, param.getId())
                .eq(CollectProduct::getUserId, userId)
                .one();

        Assert.isFalse(collectProduct == null, () -> new GlobalException(COLLECT_NOT_EXIST));

        this.removeById(collectProduct);
    }
}
