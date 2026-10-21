package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.enums.product.ProductStockRecordTypeEnum;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreProductSku;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.product.ProductStockRecord;
import com.tigshop.bean.model.product.ProductStockRecordDetail;
import com.tigshop.bean.vo.product.*;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.o2o.StoreSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductInventoryLogService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.product.ProductStockRecordDetailService;
import com.tigshop.service.product.ProductStockRecordService;
import com.tigshop.service.product.StockWarningService;
import com.tigshop.mapper.product.ProductStockRecordMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Service
public class ProductStockRecordServiceImpl extends BaseServiceImpl<ProductStockRecordMapper, ProductStockRecord>
        implements ProductStockRecordService {


    private final ProductSkuService productSkuService;

    private final ProductService productService;

    private final RedisCache redisCache;

    private final ProductStockRecordDetailService productStockRecordDetailService;

    private final StockWarningService stockWarningService;

    private final StoreProductMapper storeProductMapper;

    private final StoreSkuMapper storeSkuMapper;

    private final ProductInventoryLogService productInventoryLogService;

    @Override
    public Page<ProductStockRecordVO> productStockRecordPage(ProductStockDTO productStockDTO) {
        productStockDTO.setShopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
        if (StrUtil.isNotEmpty(productStockDTO.getBeginTime())) {
            productStockDTO.setQueryBeginTime(TigUtils.toTimestampYmd(productStockDTO.getBeginTime()));
        }
        if (StrUtil.isNotEmpty(productStockDTO.getEndTime())) {
            productStockDTO.setQueryEndTime(TigUtils.toTimestampYmd(productStockDTO.getEndTime()));
        }
        Page<Object> queryPage = new Page<>(productStockDTO.getPage(), productStockDTO.getSize());
        return this.baseMapper.productStockRecordPage(queryPage, productStockDTO);
    }

    @Override
    public ProductStockRecordVO getRecordDetail(Integer id) {

        ProductStockRecord stockRecord = this.getById(id);

        if (stockRecord == null) {
            return null;
        }
        List<ProductStockRecordDetail> stockRecordDetails = productStockRecordDetailService.list(Wrappers.lambdaQuery(ProductStockRecordDetail.class).eq(ProductStockRecordDetail::getStockRecordId, id));
        if (CollUtil.isEmpty(stockRecordDetails)) {
            return null;
        }
        ProductStockRecordVO productStockRecordVO = new ProductStockRecordVO();
        BeanUtils.copyProperties(stockRecord, productStockRecordVO);
        productStockRecordVO.setAddTime(TigUtils.handelTime(stockRecord.getAddTime()));
        ArrayList<ProductStockRecordDetailVO> productStockRecordDetailVOS = new ArrayList<>();
        for (ProductStockRecordDetail stockRecordDetail : stockRecordDetails) {
            ProductStockRecordDetailVO productStockRecordDetailVO = new ProductStockRecordDetailVO();
            Product product = productService.getById(stockRecordDetail.getProductId());
            if (product != null) {
                productStockRecordDetailVO.setProductName(product.getProductName());
                productStockRecordDetailVO.setProductUnit(product.getProductUnit());
                productStockRecordDetailVO.setProductPicThumb(product.getPicThumb());
            }
            ProductSku productSku = productSkuService.getById(stockRecordDetail.getSkuId());
            if (productSku != null) {
                productStockRecordDetailVO.setProductSkuName(productSku.getSkuValue());
            }
            productStockRecordDetailVO.setQuantity(stockRecordDetail.getQuantity());
            productStockRecordDetailVOS.add(productStockRecordDetailVO);
        }
        productStockRecordVO.setProductList(productStockRecordDetailVOS);
        return productStockRecordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean productStockIn(ProductStockInDTO productStockInDTO) {
        int shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        return productStockInForShop(shopId, productStockInDTO) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer productStockInForShop(Integer shopId, ProductStockInDTO productStockInDTO) {
        Assert.notNull(shopId, () -> new GlobalException("库存归属组织不能为空"));
        Assert.notEmpty(productStockInDTO.getProductStockRecordDetailList(), () -> new GlobalException("请选择商品"));
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("addOrDescStockLock", uuid, 10, TimeUnit.SECONDS);
        if (lock) {
            try {
                for (ProductStockRecordDetail productStockRecordDetail : productStockInDTO.getProductStockRecordDetailList()) {
                    Product product = productService.getById(productStockRecordDetail.getProductId());
                    increaseStockForShop(product, shopId, productStockRecordDetail);
                }
                ProductStockRecord productStockRecord = new ProductStockRecord();
                productStockRecord.setInOrOut(ProductStockRecordTypeEnum.IN.getCode());
                productStockRecord.setType(productStockInDTO.getType());
                productStockRecord.setComment(productStockInDTO.getComment());
                productStockRecord.setAddTime(System.currentTimeMillis() / 1000);
                productStockRecord.setAddBy(SecurityUtils.getCurrentUsername());
                productStockRecord.setShopId(shopId);
                String randomNumStr = String.valueOf(new Random().nextInt(90) + 10);
                //唯一编码 出入库类型 1位、店铺ID 3位、时间戳 13位、随机数 2位
                String seqNo = ProductStockRecordTypeEnum.IN.getCode() + String.format("%03d", shopId) + System.currentTimeMillis() + randomNumStr;
                productStockRecord.setStockCode(seqNo);
                boolean saved = this.save(productStockRecord);
                if (saved) {
                    for (ProductStockRecordDetail productStockRecordDetail : productStockInDTO.getProductStockRecordDetailList()) {
                        productStockRecordDetail.setStockRecordId(productStockRecord.getProductStockId());
                    }
                    if (!productStockRecordDetailService.saveBatch(productStockInDTO.getProductStockRecordDetailList())) {
                        throw new GlobalException("库存记录明细创建失败");
                    }
                    evaluateStockWarning(shopId, productStockInDTO.getProductStockRecordDetailList());
                }
                return saved ? productStockRecord.getProductStockId() : null;
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("addOrDescStockLock", uuid);
            }
        } else {
            throw new GlobalException("其他线程正在修改此数据，请稍侯再试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean productStockOut(ProductStockInDTO productStockInDTO) {
        int shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        return productStockOutForShop(shopId, productStockInDTO) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer productStockOutForShop(Integer shopId, ProductStockInDTO productStockInDTO) {
        Assert.notNull(shopId, () -> new GlobalException("库存归属组织不能为空"));
        Assert.notEmpty(productStockInDTO.getProductStockRecordDetailList(), () -> new GlobalException("请选择商品"));
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("addOrDescStockLock", uuid, 10, TimeUnit.SECONDS);
        if (lock) {
            try {
                for (ProductStockRecordDetail productStockRecordDetail : productStockInDTO.getProductStockRecordDetailList()) {
                    Product product = productService.getById(productStockRecordDetail.getProductId());
                    decreaseStockForShop(product, shopId, productStockRecordDetail);
                }
                ProductStockRecord productStockRecord = new ProductStockRecord();
                productStockRecord.setInOrOut(ProductStockRecordTypeEnum.OUT.getCode());
                productStockRecord.setType(productStockInDTO.getType());
                productStockRecord.setComment(productStockInDTO.getComment());
                productStockRecord.setAddTime(System.currentTimeMillis() / 1000);
                productStockRecord.setAddBy(SecurityUtils.getCurrentUsername());
                productStockRecord.setShopId(shopId);
                String randomNumStr = String.valueOf(new Random().nextInt(90) + 10);
                //唯一编码 出入库类型 1位、店铺ID 3位、时间戳 13位、随机数 2位
                String seqNo = ProductStockRecordTypeEnum.OUT.getCode() + String.format("%03d", shopId) + System.currentTimeMillis() + randomNumStr;
                productStockRecord.setStockCode(seqNo);
                boolean saved = this.save(productStockRecord);
                if (saved) {
                    for (ProductStockRecordDetail productStockRecordDetail : productStockInDTO.getProductStockRecordDetailList()) {
                        productStockRecordDetail.setStockRecordId(productStockRecord.getProductStockId());
                    }
                    if (!productStockRecordDetailService.saveBatch(productStockInDTO.getProductStockRecordDetailList())) {
                        throw new GlobalException("库存记录明细创建失败");
                    }
                    evaluateStockWarning(shopId, productStockInDTO.getProductStockRecordDetailList());
                }
                return saved ? productStockRecord.getProductStockId() : null;
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("addOrDescStockLock", uuid);
            }
        } else {
            throw new GlobalException("其他线程正在修改此数据，请稍侯再试");
        }
    }

    /**
     * 出入库后重新判定库存预警状态
     */
    private void evaluateStockWarning(int shopId, List<ProductStockRecordDetail> details) {
        if (CollUtil.isEmpty(details)) {
            return;
        }
        for (ProductStockRecordDetail detail : details) {
            stockWarningService.evaluate(shopId, detail.getProductId(), detail.getSkuId());
        }
    }

    private void increaseStockForShop(Product product, int shopId, ProductStockRecordDetail detail) {
        validateStockDetail(product, detail);
        boolean hasSku = detail.getSkuId() != null && detail.getSkuId() > 0;
        ProductSku productSku = hasSku ? requireProductSku(product, detail.getSkuId()) : null;
        StoreProduct storeProduct = resolveAssignedStoreProduct(product, shopId);
        if (storeProduct == null) {
            if (productSku != null) {
                productSkuService.incStock(productSku.getSkuId(), detail.getQuantity(), "入库增加库存");
            }
            productService.incStock(product.getProductId(), detail.getQuantity(), "入库增加库存", !hasSku);
            return;
        }

        StoreProductSku storeSku = productSku == null ? null : requireStoreSku(storeProduct, productSku.getSkuId());
        if (storeSku != null) {
            int updated = storeSkuMapper.update(null, Wrappers.lambdaUpdate(StoreProductSku.class)
                    .eq(StoreProductSku::getStoreProductSkuId, storeSku.getStoreProductSkuId())
                    .setIncrBy(StoreProductSku::getSkuStock, detail.getQuantity()));
            if (updated == 0) {
                throw new GlobalException("门店商品规格已失效");
            }
        }
        int updated = storeProductMapper.update(null, Wrappers.lambdaUpdate(StoreProduct.class)
                .eq(StoreProduct::getStoreProductId, storeProduct.getStoreProductId())
                .eq(StoreProduct::getIsDelete, 0)
                .setIncrBy(StoreProduct::getProductStock, detail.getQuantity()));
        if (updated == 0) {
            throw new GlobalException("平台分配商品已失效");
        }
        saveStoreInventoryLog(product.getProductId(), storeSku, storeProduct, shopId,
                detail.getQuantity(), true, "入库增加库存");
    }

    private void decreaseStockForShop(Product product, int shopId, ProductStockRecordDetail detail) {
        validateStockDetail(product, detail);
        boolean hasSku = detail.getSkuId() != null && detail.getSkuId() > 0;
        ProductSku productSku = hasSku ? requireProductSku(product, detail.getSkuId()) : null;
        StoreProduct storeProduct = resolveAssignedStoreProduct(product, shopId);
        if (storeProduct == null) {
            if (productSku != null) {
                productSkuService.decStock(productSku.getSkuId(), detail.getQuantity(), "出库扣减库存");
            }
            productService.decStock(product.getProductId(), detail.getQuantity(), "出库扣减库存", !hasSku);
            return;
        }

        StoreProductSku storeSku = productSku == null ? null : requireStoreSku(storeProduct, productSku.getSkuId());
        if (storeSku != null) {
            int updated = storeSkuMapper.update(null, Wrappers.lambdaUpdate(StoreProductSku.class)
                    .eq(StoreProductSku::getStoreProductSkuId, storeSku.getStoreProductSkuId())
                    .ge(StoreProductSku::getSkuStock, detail.getQuantity())
                    .setDecrBy(StoreProductSku::getSkuStock, detail.getQuantity()));
            if (updated == 0) {
                throw new GlobalException("门店商品规格库存不足");
            }
        }
        int updated = storeProductMapper.update(null, Wrappers.lambdaUpdate(StoreProduct.class)
                .eq(StoreProduct::getStoreProductId, storeProduct.getStoreProductId())
                .eq(StoreProduct::getIsDelete, 0)
                .ge(StoreProduct::getProductStock, detail.getQuantity())
                .setDecrBy(StoreProduct::getProductStock, detail.getQuantity()));
        if (updated == 0) {
            throw new GlobalException("门店商品库存不足或分配已失效");
        }
        saveStoreInventoryLog(product.getProductId(), storeSku, storeProduct, shopId,
                detail.getQuantity(), false, "出库扣减库存");
    }

    private void validateStockDetail(Product product, ProductStockRecordDetail detail) {
        if (product == null) {
            throw new GlobalException("商品不存在或不属于目标组织");
        }
        if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
            throw new GlobalException("库存变动数量必须大于0");
        }
    }

    private ProductSku requireProductSku(Product product, Integer skuId) {
        ProductSku productSku = productSkuService.getById(skuId);
        if (productSku == null || !Objects.equals(productSku.getProductId(), product.getProductId())) {
            throw new GlobalException("商品规格不存在或不属于目标商品");
        }
        return productSku;
    }

    private StoreProduct resolveAssignedStoreProduct(Product product, int shopId) {
        if (Objects.equals(product.getShopId(), shopId)) {
            return null;
        }
        if (!Objects.equals(product.getShopId(), 0) || shopId <= 0) {
            throw new GlobalException("商品不存在或不属于目标组织");
        }
        StoreProduct storeProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                .eq(StoreProduct::getProductId, product.getProductId())
                .eq(StoreProduct::getShopId, shopId)
                .eq(StoreProduct::getIsDelete, 0)
                .last("limit 1"));
        if (storeProduct == null) {
            throw new GlobalException("平台商品未分配给目标组织或分配已失效");
        }
        return storeProduct;
    }

    private StoreProductSku requireStoreSku(StoreProduct storeProduct, Integer skuId) {
        StoreProductSku storeSku = storeSkuMapper.selectOne(Wrappers.lambdaQuery(StoreProductSku.class)
                .eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId())
                .eq(StoreProductSku::getProductId, storeProduct.getProductId())
                .eq(StoreProductSku::getProductSkuId, skuId)
                .last("limit 1"));
        if (storeSku == null) {
            throw new GlobalException("平台分配商品规格不存在或已失效");
        }
        return storeSku;
    }

    private void saveStoreInventoryLog(Integer productId, StoreProductSku storeSku, StoreProduct storeProduct,
                                       int shopId, int quantity, boolean increase, String description) {
        int oldStock = storeSku == null ? storeProduct.getProductStock() : storeSku.getSkuStock();
        productInventoryLogService.save(ProductInventoryLog.builder()
                .productId(productId)
                .specId(storeSku == null ? 0 : Math.toIntExact(storeSku.getProductSkuId()))
                .number(increase ? oldStock + quantity : oldStock - quantity)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(oldStock)
                .type(increase ? 1 : 2)
                .changeNumber(quantity)
                .desc(description)
                .shopId(shopId)
                .build());
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        ExcelUtils<ProductStockOutExcelDTO> excelUtils = new ExcelUtils<>(List.of(), ProductStockOutExcelDTO.class);
        excelUtils.exportExcelWeb(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductImportSuccessListVO> batchImport(MultipartFile file) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
        Assert.isTrue(Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx"), () -> new GlobalException("请上传.xlsx格式文件"));

        ExcelUtils<ProductStockOutExcelDTO> productBatchStockOutListDTO = new ExcelUtils<>(file, ProductStockOutExcelDTO.class);
        List<ProductStockOutExcelDTO> productBatchStockOutList = productBatchStockOutListDTO.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(productBatchStockOutList), () -> new GlobalException("表格数据为空"));

        List<ProductImportErrorExcelVO> errorExcelVOS = checkImportData(productBatchStockOutList);
        if (CollUtil.isNotEmpty(errorExcelVOS)) {
            throw new GlobalException("导入失败:共导入" + productBatchStockOutList.size() + "个,失败" + errorExcelVOS.size() + "个");
        }
        ArrayList<ProductImportSuccessListVO> productStockPageVOS = new ArrayList<>();
        for (ProductStockOutExcelDTO productStockOutExcelDTO : productBatchStockOutList) {
            ProductImportSuccessListVO productStockPageVO = new ProductImportSuccessListVO();

            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class).eq(Product::getProductSn, productStockOutExcelDTO.getProductSn()).last("limit 1"));

            productStockPageVO.setProductId(product.getProductId());
            productStockPageVO.setProductName(product.getProductName());
            productStockPageVO.setProductUnit(product.getProductUnit());
            productStockPageVO.setProductPicThumb(product.getPicThumb());
            productStockPageVO.setCurrentProductStock(product.getProductStock());
            if (StrUtil.isNotEmpty(productStockOutExcelDTO.getProductSkuName())) {
                ProductSku productSku = productSkuService.getOne(Wrappers.lambdaQuery(ProductSku.class)
                        .eq(ProductSku::getProductId, product.getProductId())
                        .eq(ProductSku::getSkuValue, productStockOutExcelDTO.getProductSkuName())
                        .last("limit 1"));

                productStockPageVO.setProductSkuId(productSku.getSkuId());
                productStockPageVO.setProductSkuName(productSku.getSkuValue());
                productStockPageVO.setCurrentProductStock(productSku.getSkuStock());
            }
            productStockPageVO.setQuantity(Integer.parseInt(productStockOutExcelDTO.getQuantity()));
            productStockPageVOS.add(productStockPageVO);
        }

        return productStockPageVOS;
    }

    @Override
    public void getImportErrorData(MultipartFile file, HttpServletResponse response) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
        ExcelUtils<ProductStockOutExcelDTO> productBatchStockOutListDTO = new ExcelUtils<>(file, ProductStockOutExcelDTO.class);
        List<ProductStockOutExcelDTO> productBatchStockOutList = productBatchStockOutListDTO.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(productBatchStockOutList), () -> new GlobalException("表格数据为空"));

        List<ProductImportErrorExcelVO> errorExcelVOS = checkImportData(productBatchStockOutList);
        ExcelUtils<ProductImportErrorExcelVO> excelUtils = new ExcelUtils<>(errorExcelVOS, ProductImportErrorExcelVO.class);
        excelUtils.exportExcelWeb(response);

    }

    @Override
    public List<ProductImportSuccessListVO> batchImportAllocateProduct(MultipartFile file, Integer shopId, Integer targetShopId) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
        Assert.notNull(shopId, () -> new GlobalException("请选择出库组织"));
        Assert.notNull(targetShopId, () -> new GlobalException("请选择入库组织"));

        ExcelUtils<AllocateProductImportExcelDTO> productBatchStockOutListDTO = new ExcelUtils<>(file, AllocateProductImportExcelDTO.class);
        List<AllocateProductImportExcelDTO> productBatchStockOutList = productBatchStockOutListDTO.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(productBatchStockOutList), () -> new GlobalException("表格数据为空"));

        List<AllocateProductImportErrorExcelDTO> errorExcelVOS = checkAllocateImportData(productBatchStockOutList, shopId, targetShopId);
        if (CollUtil.isNotEmpty(errorExcelVOS)) {
            throw new GlobalException("导入失败:共导入" + productBatchStockOutList.size() + "个,失败" + errorExcelVOS.size() + "个");
        }
        ArrayList<ProductImportSuccessListVO> productStockPageVOS = new ArrayList<>();
        for (AllocateProductImportExcelDTO productImportSuccessListVO : productBatchStockOutList) {
            ProductImportSuccessListVO successListVO = new ProductImportSuccessListVO();

            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class)
                    .eq(Product::getProductSn, productImportSuccessListVO.getProductSn())
                    .last("limit 1"));

            successListVO.setProductId(product.getProductId());
            successListVO.setProductName(product.getProductName());
            successListVO.setProductUnit(product.getProductUnit());
            successListVO.setProductPicThumb(product.getPicThumb());
            successListVO.setQuantity(Integer.parseInt(productImportSuccessListVO.getQuantity()));
            successListVO.setCurrentProductStock(product.getProductStock());

            // 根据出库组织获取库存
            if (shopId > 0) {
                // 门店出库，从store_product获取库存
                StoreProduct storeProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                        .eq(StoreProduct::getProductId, product.getProductId())
                        .eq(StoreProduct::getShopId, shopId)
                        .eq(StoreProduct::getIsDelete, 0)
                        .last("limit 1"));
                if (storeProduct != null) {
                    successListVO.setCurrentProductStock(storeProduct.getProductStock());
                }
            }

            if (StrUtil.isNotEmpty(productImportSuccessListVO.getProductSkuName())) {
                ProductSku productSku = productSkuService.getOne(Wrappers.lambdaQuery(ProductSku.class)
                        .eq(ProductSku::getProductId, product.getProductId())
                        .eq(ProductSku::getSkuValue, productImportSuccessListVO.getProductSkuName())
                        .last("limit 1"));

                successListVO.setProductSkuId(productSku.getSkuId());
                successListVO.setProductSkuName(productSku.getSkuValue());
                successListVO.setCurrentProductStock(productSku.getSkuStock());

                // 根据出库组织获取SKU库存
                if (shopId > 0) {
                    StoreProduct storeProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                            .eq(StoreProduct::getProductId, product.getProductId())
                            .eq(StoreProduct::getShopId, shopId)
                            .eq(StoreProduct::getIsDelete, 0)
                            .last("limit 1"));
                    if (storeProduct != null) {
                        StoreProductSku storeSku = storeSkuMapper.selectOne(Wrappers.lambdaQuery(StoreProductSku.class)
                                .eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId())
                                .eq(StoreProductSku::getProductId, product.getProductId())
                                .eq(StoreProductSku::getProductSkuId, productSku.getSkuId())
                                .last("limit 1"));
                        if (storeSku != null) {
                            successListVO.setCurrentProductStock(storeSku.getSkuStock());
                        }
                    }
                }
            }
            productStockPageVOS.add(successListVO);
        }
        return productStockPageVOS;
    }

    private List<AllocateProductImportErrorExcelDTO> checkAllocateImportData(List<AllocateProductImportExcelDTO> productBatchStockOutList, Integer shopId, Integer targetShopId) {

        ArrayList<AllocateProductImportErrorExcelDTO> errorData = new ArrayList<>();
        for (AllocateProductImportExcelDTO productImportSuccessListVO : productBatchStockOutList) {
            AllocateProductImportErrorExcelDTO errorExportVO = new AllocateProductImportErrorExcelDTO();

            // 校验商品编码不能为空
            if (StrUtil.isEmpty(productImportSuccessListVO.getProductSn())) {
                BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                errorExportVO.setErrorMsg("商品编码不能为空");
                errorData.add(errorExportVO);
                continue;
            }

            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class)
                    .eq(Product::getProductSn, productImportSuccessListVO.getProductSn())
                    .eq(Product::getIsDelete, 0)
                    .last("limit 1"));

            // 校验商品是否存在
            if (product == null) {
                BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                errorExportVO.setErrorMsg("商品不存在");
                errorData.add(errorExportVO);
                continue;
            }

            // 校验只能调拨普通商品（实物商品）
            if (product.getProductType() != ProductType.PRODUCT_TYPE_NORMAL.getCode()) {
                BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                errorExportVO.setErrorMsg("只能调拨普通商品（实物商品）");
                errorData.add(errorExportVO);
                continue;
            }

            // 根据不同场景校验商品
            StoreProduct sourceStoreProduct = null;
            StoreProduct targetStoreProduct = null;

            // 场景1：平台调拨给门店 - shopId=0, targetShopId>0
            if (shopId == 0 && targetShopId > 0) {
                // 校验商品是平台商品（shop_id=0）
                if (product.getShopId() != 0) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("该商品不是平台商品，无法从平台调拨");
                    errorData.add(errorExportVO);
                    continue;
                }
                // 校验商品已分配给目标门店
                targetStoreProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                        .eq(StoreProduct::getProductId, product.getProductId())
                        .eq(StoreProduct::getShopId, targetShopId)
                        .eq(StoreProduct::getIsDelete, 0)
                        .last("limit 1"));
                if (targetStoreProduct == null) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("该商品未分配给目标门店");
                    errorData.add(errorExportVO);
                    continue;
                }
            }

            // 场景2：门店调拨给平台 - shopId>0, targetShopId=0
            if (shopId > 0 && targetShopId == 0) {
                // 校验出库门店有该商品
                sourceStoreProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                        .eq(StoreProduct::getProductId, product.getProductId())
                        .eq(StoreProduct::getShopId, shopId)
                        .eq(StoreProduct::getIsDelete, 0)
                        .last("limit 1"));
                if (sourceStoreProduct == null) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("出库门店不存在该商品");
                    errorData.add(errorExportVO);
                    continue;
                }
            }

            // 场景3：门店调拨给门店 - shopId>0, targetShopId>0
            if (shopId > 0 && targetShopId > 0) {
                // 校验出库门店有该商品
                sourceStoreProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                        .eq(StoreProduct::getProductId, product.getProductId())
                        .eq(StoreProduct::getShopId, shopId)
                        .eq(StoreProduct::getIsDelete, 0)
                        .last("limit 1"));
                if (sourceStoreProduct == null) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("出库门店不存在该商品");
                    errorData.add(errorExportVO);
                    continue;
                }
                // 校验入库门店也有该商品
                targetStoreProduct = storeProductMapper.selectOne(Wrappers.lambdaQuery(StoreProduct.class)
                        .eq(StoreProduct::getProductId, product.getProductId())
                        .eq(StoreProduct::getShopId, targetShopId)
                        .eq(StoreProduct::getIsDelete, 0)
                        .last("limit 1"));
                if (targetStoreProduct == null) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("入库门店不存在该商品");
                    errorData.add(errorExportVO);
                    continue;
                }
            }

            // 校验商品规格
            if (StrUtil.isEmpty(productImportSuccessListVO.getProductSkuName())) {
                // 没有填写规格，检查商品是否有规格
                long skuCount = productSkuService.count(Wrappers.lambdaQuery(ProductSku.class).eq(ProductSku::getProductId, product.getProductId()));
                if (skuCount > 0) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("该商品下有规格商品，请补齐商品规格");
                    errorData.add(errorExportVO);
                    continue;
                }
                // 如果是门店商品，还需检查门店商品是否有规格
                if (sourceStoreProduct != null) {
                    Long count = storeSkuMapper.selectCount(Wrappers.lambdaQuery(StoreProductSku.class).eq(StoreProductSku::getStoreProductId, sourceStoreProduct.getStoreProductId()));
                    if (count > 0) {
                        BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                        errorExportVO.setErrorMsg("该门店商品下有规格商品，请补齐商品规格");
                        errorData.add(errorExportVO);
                    }
                }
            } else {
                // 填写了规格，校验规格是否存在
                ProductSku productSku = productSkuService.getOne(Wrappers.lambdaQuery(ProductSku.class)
                        .eq(ProductSku::getProductId, product.getProductId())
                        .eq(ProductSku::getSkuValue, productImportSuccessListVO.getProductSkuName())
                        .last("limit 1"));

                if (productSku == null) {
                    BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                    errorExportVO.setErrorMsg("商品规格不存在");
                    errorData.add(errorExportVO);
                    continue;
                }
                // 如果是门店商品，还需检查门店商品规格是否存在
                if (sourceStoreProduct != null) {
                    StoreProductSku storeProductSku = storeSkuMapper.selectOne(Wrappers.lambdaQuery(StoreProductSku.class)
                            .eq(StoreProductSku::getStoreProductId, sourceStoreProduct.getStoreProductId())
                            .eq(StoreProductSku::getProductSkuId, productSku.getSkuId())
                            .last("limit 1"));
                    if (storeProductSku == null) {
                        BeanUtils.copyProperties(productImportSuccessListVO, errorExportVO);
                        errorExportVO.setErrorMsg("出库门店商品规格不存在");
                        errorData.add(errorExportVO);
                    }
                }
            }
        }
        return errorData;
    }

    @Override
    public void getAllocateProductErrorData(MultipartFile file, Integer shopId, Integer targetShopId, HttpServletResponse response) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
        Assert.notNull(shopId, () -> new GlobalException("请选择出库组织"));
        Assert.notNull(targetShopId, () -> new GlobalException("请选择入库组织"));

        ExcelUtils<AllocateProductImportExcelDTO> productBatchStockOutListDTO = new ExcelUtils<>(file, AllocateProductImportExcelDTO.class);
        List<AllocateProductImportExcelDTO> productBatchStockOutList = productBatchStockOutListDTO.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(productBatchStockOutList), () -> new GlobalException("表格数据为空"));

        ExcelUtils<AllocateProductImportErrorExcelDTO> excelUtils = new ExcelUtils<>(checkAllocateImportData(productBatchStockOutList, shopId, targetShopId), AllocateProductImportErrorExcelDTO.class);
        excelUtils.exportExcelWeb(response);
    }


    private List<ProductImportErrorExcelVO> checkImportData(List<ProductStockOutExcelDTO> productBatchStockOutList) {
        ArrayList<ProductImportErrorExcelVO> importErrorExcelVOS = new ArrayList<>();
        for (ProductStockOutExcelDTO productStockOutExcelDTO : productBatchStockOutList) {
            ProductImportErrorExcelVO productImportErrorExcelVO = new ProductImportErrorExcelVO();
            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class).eq(Product::getProductSn, productStockOutExcelDTO.getProductSn()).eq(Product::getShopId, HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId()).last("limit 1"));
            if (StrUtil.isEmpty(productStockOutExcelDTO.getProductSn())) {
                BeanUtils.copyProperties(productStockOutExcelDTO, productImportErrorExcelVO);
                productImportErrorExcelVO.setErrorMsg("商品编码没有填写");
                importErrorExcelVOS.add(productImportErrorExcelVO);
                continue;
            }
            if (product == null) {
                BeanUtils.copyProperties(productStockOutExcelDTO, productImportErrorExcelVO);
                productImportErrorExcelVO.setErrorMsg("商品不存在");
                importErrorExcelVOS.add(productImportErrorExcelVO);
                continue;
            }
            if (product.getProductType() != ProductType.PRODUCT_TYPE_NORMAL.getCode()) {
                BeanUtils.copyProperties(productStockOutExcelDTO, productImportErrorExcelVO);
                productImportErrorExcelVO.setErrorMsg("该商品不是实物商品不能出入库");
                importErrorExcelVOS.add(productImportErrorExcelVO);
                continue;
            }
            if (StrUtil.isEmpty(productStockOutExcelDTO.getProductSkuName())) {
                List<ProductSku> productSkus = productSkuService.list(Wrappers.lambdaQuery(ProductSku.class).eq(ProductSku::getProductId, product.getProductId()));
                if (CollUtil.isNotEmpty(productSkus)) {
                    BeanUtils.copyProperties(productStockOutExcelDTO, productImportErrorExcelVO);
                    productImportErrorExcelVO.setErrorMsg("该商品下面有规格商品，请补齐商品规格");
                    importErrorExcelVOS.add(productImportErrorExcelVO);
                }
            } else {
                ProductSku productSku = productSkuService.getOne(Wrappers.lambdaQuery(ProductSku.class).eq(ProductSku::getProductId, product.getProductId()).eq(ProductSku::getSkuValue, productStockOutExcelDTO.getProductSkuName()).last("limit 1"));
                if (productSku == null) {
                    BeanUtils.copyProperties(productStockOutExcelDTO, productImportErrorExcelVO);
                    productImportErrorExcelVO.setErrorMsg("商品规格不存在");
                    importErrorExcelVOS.add(productImportErrorExcelVO);
                }
            }

        }
        return importErrorExcelVOS;
    }


}

