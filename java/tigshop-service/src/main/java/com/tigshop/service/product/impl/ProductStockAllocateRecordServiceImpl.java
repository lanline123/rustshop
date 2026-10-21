package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.AllocateStockOutDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateExcelExportDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateRecordCreateDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateRecordDTO;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.product.ProductStockAllocateOperationEnum;
import com.tigshop.bean.enums.product.ProductStockAllocateStateEnum;
import com.tigshop.bean.enums.product.ProductStockRecordTypeEnum;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.*;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.product.*;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.product.ProductStockAllocateRecordMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductStockAllocateRecordServiceImpl extends BaseServiceImpl<ProductStockAllocateRecordMapper, ProductStockAllocateRecord> implements ProductStockAllocateRecordService {

    private final ShopMapper shopMapper;

    private final ConfigService configService;

    private final TigshopProperties tigshopProperties;

    private final ProductStockAllocateRecordDetailService productStockAllocateRecordDetailService;

    private final ProductService productService;
    ;

    private final ProductSkuService productSkuService;

    private final ProductStockRecordService productStockRecordService;

    private final ProductStockRecordDetailService productStockRecordDetailService;

    private final ProductStockAllocateOperationRecordService productStockAllocateOperationRecordService;

    private final ShopService shopService;

    private final RedisCache redisCache;


    @Override
    public Page<ProductStockAllocateRecordPageVO> getProductStockAllocateRecordPage(ProductStockAllocateRecordDTO productStockAllocateRecordDTO) {
        Page<ProductStockAllocateRecordPageVO> page = new Page<>(productStockAllocateRecordDTO.getPage(), productStockAllocateRecordDTO.getSize());
        if (tigshopProperties.getIsO2o() == 0) {
            return page;
        }
        int shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        productStockAllocateRecordDTO.setShopId(shopId);

        Page<ProductStockAllocateRecordPageVO> allocateRecordPage = this.getBaseMapper().getPage(page, productStockAllocateRecordDTO);
        if (CollUtil.isEmpty(allocateRecordPage.getRecords())) {
            return allocateRecordPage;
        }

        for (ProductStockAllocateRecordPageVO record : allocateRecordPage.getRecords()) {
            record.setOutboundOrganizationName(resolveOrganizationName(record.getOutboundOrganizationId()));
            record.setWarehousingOrganizationName(resolveOrganizationName(record.getWarehousingOrganizationId()));
            //判断当前用户是否是入库方，且是否有待审核的调拨单
            if (HeaderUtils.getShopId() != null && Objects.equals(HeaderUtils.getShopId(), record.getWarehousingOrganizationId())) {
                if (Objects.equals(record.getStatus(), ProductStockAllocateStateEnum.AUDIT_WAIT.getCode()) && record.getWarehousingOrganizationId() == shopId) {
                    record.setCanAudit(true);
                }
            }
        }
        return allocateRecordPage;
    }

    /**
     * 组织 ID 为 0 表示平台自营；店铺可能已被删除，此时不阻断列表展示
     */
    private String resolveOrganizationName(Integer organizationId) {
        if (organizationId == null || organizationId == 0) {
            return configService.getConfigVal(SettingsEnum.SHOP_NAME);
        }
        Shop shop = shopMapper.selectById(organizationId);
        return shop == null ? "" : shop.getShopTitle();
    }

    @Override
    public ProductStockAllocateRecordDetailVO productStockAllocateRecordDetail(Integer id) {
        if (tigshopProperties.getIsO2o() == 0) {
            return null;
        }
        List<ProductStockAllocateRecordDetail> recordDetailList = productStockAllocateRecordDetailService.list(Wrappers.lambdaQuery(ProductStockAllocateRecordDetail.class).eq(ProductStockAllocateRecordDetail::getAllocateId, id));
        if (CollUtil.isEmpty(recordDetailList)) {
            return null;
        }
        ArrayList<ProductAllocateDetailVO> productAllocateDetailVOS = new ArrayList<>();
        for (ProductStockAllocateRecordDetail recordDetail : recordDetailList) {
            ProductAllocateDetailVO productAllocateDetailVO = new ProductAllocateDetailVO();
            BeanUtils.copyProperties(recordDetail, productAllocateDetailVO);
            Product product = productService.getById(recordDetail.getProductId());
            productAllocateDetailVO.setProductName(product.getProductName());
            productAllocateDetailVO.setProductUnit(product.getProductUnit());
            productAllocateDetailVO.setProductPicThumb(product.getPicThumb());
            if (recordDetail.getSkuId() != null) {
                ProductSku productSku = productSkuService.getById(recordDetail.getSkuId());
                if(productSku != null){
                    productAllocateDetailVO.setSkuName(productSku.getSkuValue());
                }

            }
            productAllocateDetailVOS.add(productAllocateDetailVO);
        }
        ProductStockAllocateRecordDetailVO productStockAllocateRecordDetailVO = new ProductStockAllocateRecordDetailVO();
        productStockAllocateRecordDetailVO.setProductAllocateDetailVOList(productAllocateDetailVOS);
        return productStockAllocateRecordDetailVO;
    }

    @Override
    public boolean createAllocate(ProductStockAllocateRecordCreateDTO createDTO) {
        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        Assert.isTrue(!Objects.equals(createDTO.getOutboundOrganizationId(), createDTO.getWarehousingOrganizationId()), () -> new GlobalException("出库组织和入库组织不能相同"));
        Assert.isTrue(CollUtil.isNotEmpty(createDTO.getProductStockAllocateRecordDetailList()), () -> new GlobalException("请选择商品"));
        //1.创建调拨单，并将状态设置成待审核，如果调拨单创建者是商家则无需审核
        ProductStockAllocateRecord productStockAllocateRecord = new ProductStockAllocateRecord();
        productStockAllocateRecord.setAllocateCode(Long.toString(System.currentTimeMillis()));
        if (HeaderUtils.getShopId() == null || HeaderUtils.getShopId() == 0) {
            productStockAllocateRecord.setStatus(ProductStockAllocateStateEnum.STOCK_OUT_WAIT.getCode());
        } else {
            productStockAllocateRecord.setStatus(ProductStockAllocateStateEnum.AUDIT_WAIT.getCode());
        }
        productStockAllocateRecord.setOutboundOrganizationId(createDTO.getOutboundOrganizationId());
        productStockAllocateRecord.setWarehousingOrganizationId(createDTO.getWarehousingOrganizationId());
        productStockAllocateRecord.setAddBy(SecurityUtils.getCurrentUsername());
        productStockAllocateRecord.setAddTime(System.currentTimeMillis() / 1000);
        productStockAllocateRecord.setStockAllocateComment(createDTO.getComment());
        this.save(productStockAllocateRecord);

        //2.创建调拨单详情
        List<ProductStockAllocateRecordDetail> allocateRecordDetails = createDTO.getProductStockAllocateRecordDetailList();
        if (CollUtil.isEmpty(allocateRecordDetails)) {
            return false;
        }
        for (ProductStockAllocateRecordDetail allocateRecordDetail : allocateRecordDetails) {
            allocateRecordDetail.setAllocateId(productStockAllocateRecord.getAllocateId());
            allocateRecordDetail.setStockOutQuantity(0);
        }

        //3.保存调拨单操作记录
        ProductStockAllocateOperationRecord operationRecord = new ProductStockAllocateOperationRecord();
        operationRecord.setAllocateId(productStockAllocateRecord.getAllocateId());
        operationRecord.setOperationType(ProductStockAllocateOperationEnum.ALLOCATE_SUBMIT.getName());
        operationRecord.setAddBy(SecurityUtils.getCurrentUsername());
        operationRecord.setAddTime(System.currentTimeMillis() / 1000);
        if (productStockAllocateRecord.getStatus().equals(ProductStockAllocateStateEnum.STOCK_OUT_WAIT.getCode())) {
            productStockAllocateOperationRecordService.save(operationRecord);
        }

        return productStockAllocateRecordDetailService.saveBatch(allocateRecordDetails);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean auditAllocate(Integer id, Integer auditType, String remark) {
        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        //查看是否有审核权限
        ProductStockAllocateRecord byId = this.getById(id);
        Assert.notNull(byId, () -> new GlobalException("调拨单不存在"));
        Integer currentShopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        Assert.isTrue(Objects.equals(currentShopId, byId.getWarehousingOrganizationId()) && Objects.equals(byId.getStatus(), ProductStockAllocateStateEnum.AUDIT_WAIT.getCode()), () -> new GlobalException("无权限审核此调拨单"));
        Assert.isTrue(Objects.equals(auditType, ProductStockAllocateStateEnum.STOCK_OUT_WAIT.getCode()) || Objects.equals(auditType, ProductStockAllocateStateEnum.AUDIT_REJECT.getCode()), () -> new GlobalException("审核类型错误"));
        byId.setStatus(auditType);

        ProductStockAllocateOperationRecord operationRecord = new ProductStockAllocateOperationRecord();
        operationRecord.setAllocateId(byId.getAllocateId());
        if (auditType == ProductStockAllocateStateEnum.AUDIT_REJECT.getCode()) {
            operationRecord.setOperationType(ProductStockAllocateOperationEnum.ALLOCATE_AUDIT_REJECT.getName());
            operationRecord.setReviewRejectedReason(remark);
        } else {
            operationRecord.setOperationType(ProductStockAllocateOperationEnum.ALLOCATE_AUDIT_PASS.getName());
        }
        operationRecord.setAddBy(SecurityUtils.getCurrentUsername());
        operationRecord.setAddTime(System.currentTimeMillis() / 1000);
        productStockAllocateOperationRecordService.save(operationRecord);
        return this.updateById(byId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean stockOutAllocate(AllocateStockOutDTO allocateStockOutDTO) {

        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        Integer shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        Assert.isTrue(Objects.equals(shopId, this.getById(allocateStockOutDTO.getAllocateId()).getOutboundOrganizationId()), () -> new GlobalException("无权限操作此调拨单"));
        ProductStockAllocateRecord byId = this.getById(allocateStockOutDTO.getAllocateId());
        Assert.notNull(byId, () -> new GlobalException("调拨单不存在"));
        Assert.isTrue(byId.getStatus() == ProductStockAllocateStateEnum.STOCK_OUT_WAIT.getCode(), () -> new GlobalException("调拨单不是待调出状态"));
        //1.更新调拨单状态，并更新调拨单详情
        byId.setStatus(ProductStockAllocateStateEnum.STOCK_IN_WAIT.getCode());
        byId.setStockOutComment(allocateStockOutDTO.getComment());
        this.updateById(byId);

        ProductStockAllocateOperationRecord operationRecord = new ProductStockAllocateOperationRecord();
        operationRecord.setAllocateId(byId.getAllocateId());
        operationRecord.setOperationType(ProductStockAllocateOperationEnum.ALLOCATE_OUT_SUBMIT.getName());
        operationRecord.setAddBy(SecurityUtils.getCurrentUsername());
        operationRecord.setAddTime(System.currentTimeMillis() / 1000);
        productStockAllocateOperationRecordService.save(operationRecord);

        productStockAllocateRecordDetailService.updateBatchById(allocateStockOutDTO.getStockAllocateRecordDetails());
        //2.将扣减出库实际库存
        ArrayList<ProductStockRecordDetail> productStockRecordDetails = new ArrayList<>();
        for (ProductStockAllocateRecordDetail stockAllocateRecordDetail : allocateStockOutDTO.getStockAllocateRecordDetails()) {
            //平台可以调拨所有自己的商品
            if (shopId == 0) {
                boolean hasSku = stockAllocateRecordDetail.getSkuId() != null;
                productService.decStock(stockAllocateRecordDetail.getProductId(), stockAllocateRecordDetail.getStockOutQuantity(), "调拨单出库扣减库存", !hasSku);
                if (hasSku) {
                    productSkuService.decStock(stockAllocateRecordDetail.getSkuId(), stockAllocateRecordDetail.getStockOutQuantity(), "调拨单出库扣减库存");
                }
            }

            ProductStockRecordDetail productStockRecordDetail = new ProductStockRecordDetail();
            productStockRecordDetail.setProductId(stockAllocateRecordDetail.getProductId());
            productStockRecordDetail.setSkuId(stockAllocateRecordDetail.getSkuId());
            productStockRecordDetail.setQuantity(stockAllocateRecordDetail.getStockOutQuantity());
            productStockRecordDetails.add(productStockRecordDetail);
        }

        //4.添加出库记录
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("addOrDescStockLock", uuid, 10, TimeUnit.SECONDS);
        ProductStockRecord productStockRecord = new ProductStockRecord();
        try {
            if (lock) {
                productStockRecord.setInOrOut(ProductStockRecordTypeEnum.OUT.getCode());
                productStockRecord.setType(ProductStockRecordTypeEnum.ALLOCATE_OUT.getCode());
                productStockRecord.setShopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
                productStockRecord.setComment(allocateStockOutDTO.getComment());
                String randomNumStr = String.valueOf(new Random().nextInt(90) + 10);
                //唯一编码 出入库类型 1位、店铺ID 3位、时间戳 13位、随机数 2位
                String seqNo = ProductStockRecordTypeEnum.IN.getCode() + String.format("%03d", shopId) + System.currentTimeMillis() + randomNumStr;
                productStockRecord.setStockCode(seqNo);
                productStockRecord.setAddBy(SecurityUtils.getCurrentUsername());
                productStockRecord.setAddTime(System.currentTimeMillis() / 1000);
                productStockRecordService.save(productStockRecord);
            } else {
                // 锁获取失败，可以选择重试、排队或快速失败
                throw new GlobalException("其他线程正在修改此数据，请稍侯再试");
            }
        } catch (Exception e) {
            log.error("保存商品库存记录异常", e);
            throw e; // 根据实际需要决定是否继续抛出
        } finally {
            redisCache.unlock("addOrDescStockLock", uuid);
        }


        productStockRecordDetails.forEach(productStockRecordDetail -> {
            productStockRecordDetail.setStockRecordId(productStockRecord.getProductStockId());
        });
        //保存出库记录明细
        return productStockRecordDetailService.saveBatch(productStockRecordDetails);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean stockInAllocate(AllocateStockOutDTO allocateStockOutDTO) {

        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        Integer shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        Assert.isTrue(Objects.equals(shopId, this.getById(allocateStockOutDTO.getAllocateId()).getWarehousingOrganizationId()), () -> new GlobalException("无权限操作此调拨单"));
        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        ProductStockAllocateRecord byId = this.getById(allocateStockOutDTO.getAllocateId());
        Assert.notNull(byId, () -> new GlobalException("调拨单不存在"));
        Assert.isTrue(Objects.equals(byId.getStatus(), ProductStockAllocateStateEnum.STOCK_IN_WAIT.getCode()), () -> new GlobalException("调拨单不是待调入状态"));
        //1.更新调拨单状态，并更新调拨单详情
        byId.setStatus(ProductStockAllocateStateEnum.COMPLETE.getCode());
        byId.setStockInComment(allocateStockOutDTO.getComment());
        this.updateById(byId);

        ProductStockAllocateOperationRecord operationRecord = new ProductStockAllocateOperationRecord();
        operationRecord.setAllocateId(byId.getAllocateId());
        operationRecord.setOperationType(ProductStockAllocateOperationEnum.ALLOCATE_IN_SUBMIT.getName());
        operationRecord.setAddBy(SecurityUtils.getCurrentUsername());
        operationRecord.setAddTime(System.currentTimeMillis() / 1000);
        productStockAllocateOperationRecordService.save(operationRecord);

        productStockAllocateRecordDetailService.updateBatchById(allocateStockOutDTO.getStockAllocateRecordDetails());
        //2.将扣减出库实际库存
        ArrayList<ProductStockRecordDetail> productStockRecordDetails = new ArrayList<>();
        for (ProductStockAllocateRecordDetail stockAllocateRecordDetail : allocateStockOutDTO.getStockAllocateRecordDetails()) {
            //平台可以调拨入库平台分配给门店的商品，但是因为选择商品的时候所选的都是平台分配给店铺的商品所有，平台作为入库方可以直接增加库存
            if (shopId == 0) {
                boolean hasSku = stockAllocateRecordDetail.getSkuId() != null;
                productService.incStock(stockAllocateRecordDetail.getProductId(), stockAllocateRecordDetail.getStockInQuantity(), "调拨单入库增加库存", !hasSku);
                if (hasSku) {
                    productSkuService.incStock(stockAllocateRecordDetail.getSkuId(), stockAllocateRecordDetail.getStockInQuantity(), "调拨单入库增加库存");
                }
            }
            ProductStockRecordDetail productStockRecordDetail = new ProductStockRecordDetail();
            productStockRecordDetail.setProductId(stockAllocateRecordDetail.getProductId());
            productStockRecordDetail.setSkuId(stockAllocateRecordDetail.getSkuId());
            productStockRecordDetail.setQuantity(stockAllocateRecordDetail.getStockInQuantity());
            productStockRecordDetails.add(productStockRecordDetail);
        }

        //4.添加出库记录
        ProductStockRecord productStockRecord = new ProductStockRecord();
        productStockRecord.setInOrOut(ProductStockRecordTypeEnum.IN.getCode());
        productStockRecord.setType(ProductStockRecordTypeEnum.ALLOCATE_IN.getCode());
        productStockRecord.setShopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
        productStockRecord.setComment(allocateStockOutDTO.getComment());
        String randomNumStr = String.valueOf(new Random().nextInt(90) + 10);
        //唯一编码 出入库类型 1位、店铺ID 3位、时间戳 13位、随机数 2位
        String seqNo = ProductStockRecordTypeEnum.OUT.getCode() + String.format("%03d", shopId) + System.currentTimeMillis() + randomNumStr;
        productStockRecord.setStockCode(seqNo);
        productStockRecord.setAddBy(SecurityUtils.getCurrentUsername());
        productStockRecord.setAddTime(System.currentTimeMillis() / 1000);
        productStockRecordService.save(productStockRecord);

        productStockRecordDetails.forEach(productStockRecordDetail -> {
            productStockRecordDetail.setStockRecordId(productStockRecord.getProductStockId());
        });
        //保存出库记录明细
        return productStockRecordDetailService.saveBatch(productStockRecordDetails);
    }

    @Override
    public List<AllocateOrgVO> getOrgList() {

        if (tigshopProperties.getIsO2o() == 0) {
            return List.of();
        }
        ArrayList<AllocateOrgVO> allocateOrgVOS = new ArrayList<>();
        String shopName = configService.getConfigVal(SettingsEnum.SHOP_NAME);
        allocateOrgVOS.add(new AllocateOrgVO(0, shopName));
        List<Shop> shopList = shopService.list(Wrappers.lambdaQuery(Shop.class).eq(Shop::getShopType, ShopTypeEnum.STORE.getCode()).eq(Shop::getStatus, 1).isNull(Shop::getStoreParentId).select(Shop::getShopId, Shop::getShopTitle));
        shopList.forEach(shop -> {
            allocateOrgVOS.add(new AllocateOrgVO(shop.getShopId(), shop.getShopTitle()));
        });
        return allocateOrgVOS;
    }

    @Override
    public List<String> getAddBy() {
        if (tigshopProperties.getIsO2o() == 0) {
            return List.of();
        }
        List<ProductStockAllocateRecord> addByList = this.list(Wrappers.lambdaQuery(ProductStockAllocateRecord.class).select(ProductStockAllocateRecord::getAddBy));
        if (CollUtil.isEmpty(addByList)) {
            return List.of();
        }
        return addByList.stream().map(ProductStockAllocateRecord::getAddBy).distinct().toList();
    }

    @Override
    public List<ProductStockAllocateExcelVO> exportAllocate(ProductStockAllocateExcelExportDTO excelExportDTO) {

        excelExportDTO.setShopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
        List<ProductStockAllocateRecord> allocateRecordList = this.baseMapper.getListByProductIds(excelExportDTO);


        if (CollUtil.isEmpty(allocateRecordList)) {
            return List.of();
        }

        List<ProductStockAllocateExcelVO> productStockAllocateExcelVOS = new ArrayList<>();
        for (ProductStockAllocateRecord productStockAllocateRecord : allocateRecordList) {
            ProductStockAllocateExcelVO productStockAllocateExcelVO = new ProductStockAllocateExcelVO();
            productStockAllocateExcelVO.setAllocateCode(productStockAllocateRecord.getAllocateCode());
            productStockAllocateExcelVO.setAddBy(productStockAllocateRecord.getAddBy());
            //设置组织名称，如果shopid = 0 ，则是平台
            if (productStockAllocateRecord.getWarehousingOrganizationId() == 0) {
                productStockAllocateExcelVO.setWarehousingOrganization(configService.getConfigVal(SettingsEnum.SHOP_NAME));
            } else {
                Shop shop = shopService.getById(productStockAllocateRecord.getWarehousingOrganizationId());
                productStockAllocateExcelVO.setWarehousingOrganization(shop.getShopTitle());
            }
            if (productStockAllocateRecord.getOutboundOrganizationId() == 0) {
                productStockAllocateExcelVO.setOutboundOrganization(configService.getConfigVal(SettingsEnum.SHOP_NAME));
            } else {
                Shop shop = shopService.getById(productStockAllocateRecord.getOutboundOrganizationId());
                productStockAllocateExcelVO.setOutboundOrganization(shop.getShopTitle());
            }
            productStockAllocateExcelVO.setStatus(ProductStockAllocateStateEnum.getNameByCode(productStockAllocateRecord.getStatus()));
            productStockAllocateExcelVO.setAddTime(TigUtils.handelTime(productStockAllocateRecord.getAddTime()));
            productStockAllocateExcelVOS.add(productStockAllocateExcelVO);
        }
        return productStockAllocateExcelVOS;
    }

    @Override
    public boolean batchImportAllocate(MultipartFile file) {
        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
        Assert.isTrue(Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx"), () -> new GlobalException("请上传.xlsx格式文件"));

        ExcelUtils<AllocateBatchImportVO> batchImportVOExcel = new ExcelUtils<>(file, AllocateBatchImportVO.class);
        List<AllocateBatchImportVO> productBatchAlocateList = batchImportVOExcel.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(productBatchAlocateList), () -> new GlobalException("表格数据为空"));

        //校验出库组织和入库组织
        long outOrgCount = productBatchAlocateList.stream().map(AllocateBatchImportVO::getStockOutOrg).distinct().count();
        if (outOrgCount > 1) {
            throw new GlobalException("出库组织不一致");
        }

        long inOrgCount = productBatchAlocateList.stream().map(AllocateBatchImportVO::getStockInOrg).distinct().count();
        if (inOrgCount > 1) {
            throw new GlobalException("入库组织不一致");
        }


        List<AllocateBatchImportErrorVO> allocateBatchImportErrorVOS = checkImportData(productBatchAlocateList);
        if (CollUtil.isNotEmpty(allocateBatchImportErrorVOS)) {
            throw new GlobalException("导入失败:共导入" + productBatchAlocateList.size() + "个,失败" + allocateBatchImportErrorVOS.size() + "个");
        }

        ProductStockAllocateRecordCreateDTO createDTO = new ProductStockAllocateRecordCreateDTO();
        List<ProductStockAllocateRecordDetail> details = new ArrayList<>();
        for (AllocateBatchImportVO productBatchAllocate : productBatchAlocateList) {

            //查询出库组织
            String stockOutOrg = productBatchAllocate.getStockOutOrg();
            Integer stockOutOrgId = getShopId(allocateBatchImportErrorVOS, productBatchAllocate, stockOutOrg);

            //查询入库组织
            String stockInOrg = productBatchAllocate.getStockInOrg();
            Integer stockInOrgId = getShopId(allocateBatchImportErrorVOS, productBatchAllocate, stockInOrg);


            //先去查询下里面有没有已经添加的调拨单，一个出库组织和一个入库组织 对应一个调拨单
            createDTO.setWarehousingOrganizationId(stockInOrgId);
            createDTO.setOutboundOrganizationId(stockOutOrgId);
            createDTO.setComment(productBatchAllocate.getComment());
            ProductStockAllocateRecordDetail detail = new ProductStockAllocateRecordDetail();
            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class).eq(Product::getProductSn, productBatchAllocate.getProductSn()).last("limit 1"));
            detail.setProductId(product.getProductId());
            detail.setQuantity(Integer.parseInt(productBatchAllocate.getQuantity()));
            if (StrUtil.isNotEmpty(productBatchAllocate.getSkuName())){
                ProductSku productSku = productSkuService.getOne(Wrappers.lambdaQuery(ProductSku.class).eq(ProductSku::getProductId, product.getProductId()).eq(ProductSku::getSkuValue, productBatchAllocate.getSkuName()).last("limit 1"));
                detail.setSkuId(productSku.getSkuId());
            }
            details.add(detail);
        }
        createDTO.setProductStockAllocateRecordDetailList(details);

        return this.createAllocate(createDTO);
    }

    @Override
    public void getBatchImportErrorData(MultipartFile file, HttpServletResponse response) {
        if (tigshopProperties.getIsO2o() == 0) {
            return ;
        }
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
        Assert.isTrue(Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx"), () -> new GlobalException("请上传.xlsx格式文件"));

        ExcelUtils<AllocateBatchImportVO> batchImportVOExcel = new ExcelUtils<>(file, AllocateBatchImportVO.class);
        List<AllocateBatchImportVO> productBatchAlocateList = batchImportVOExcel.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(productBatchAlocateList), () -> new GlobalException("表格数据为空"));
        //校验出库组织和入库组织
        long outOrgCount = productBatchAlocateList.stream().map(AllocateBatchImportVO::getStockOutOrg).distinct().count();
        if (outOrgCount > 1) {
            throw new GlobalException("出库组织不一致");
        }

        long inOrgCount = productBatchAlocateList.stream().map(AllocateBatchImportVO::getStockInOrg).distinct().count();
        if (inOrgCount > 1) {
            throw new GlobalException("入库组织不一致");
        }

        ExcelUtils<AllocateBatchImportErrorVO> excelUtils = new ExcelUtils<>(checkImportData(productBatchAlocateList), AllocateBatchImportErrorVO.class);
        excelUtils.exportExcelWeb(response);
    }

    private List<AllocateBatchImportErrorVO> checkImportData(List<AllocateBatchImportVO> productBatchAlocateList) {
        ArrayList<AllocateBatchImportErrorVO> errorVOS = new ArrayList<>();
        for (AllocateBatchImportVO productBatchAllocate : productBatchAlocateList) {
            String stockOutOrg = productBatchAllocate.getStockOutOrg();
            Integer stockOutOrgId = getShopId(errorVOS, productBatchAllocate, stockOutOrg);
            if (stockOutOrgId == null) {
                continue;
            }
            //查询入库组织
            String stockInOrg = productBatchAllocate.getStockInOrg();
            Integer stockInOrgId = getShopId(errorVOS, productBatchAllocate, stockInOrg);
            if (stockInOrgId == null) {
                continue;
            }


            if (StringUtils.isEmpty(productBatchAllocate.getProductSn())) {
                addImportErrorRecord(productBatchAllocate, "商品编码没有填写", errorVOS);
                continue;
            }

            int shopId = stockOutOrgId;
            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class)
                    .eq(Product::getProductSn, productBatchAllocate.getProductSn())
                    .last("limit 1"));

            if (product == null) {
                addImportErrorRecord(productBatchAllocate, "商品不存在", errorVOS);
                continue;
            }
            {
                if (StrUtil.isEmpty(productBatchAllocate.getSkuName())) {
                    long count = productSkuService.count(Wrappers.lambdaQuery(ProductSku.class).eq(ProductSku::getProductId, product.getProductId()));
                    if (count > 0) {
                        addImportErrorRecord(productBatchAllocate, "该商品下有规格商品，请不起商品规格", errorVOS);
                        continue;
                    }
                }else {
                    ProductSku productSku = productSkuService.getOne(Wrappers.lambdaQuery(ProductSku.class)
                            .eq(ProductSku::getProductId, product.getProductId())
                            .eq(ProductSku::getSkuValue, productBatchAllocate.getSkuName()).last("limit 1"));
                    if (productSku == null) {
                        addImportErrorRecord(productBatchAllocate, "商品规格不存在", errorVOS);
                        continue;
                    }
                }

            }
            if (product.getProductType() != ProductType.PRODUCT_TYPE_NORMAL.getCode()) {
                addImportErrorRecord(productBatchAllocate, "该商品不是实物商品不能调拨出库", errorVOS);
            }
        }
        return errorVOS;

    }

    private static void addImportErrorRecord(AllocateBatchImportVO productBatchAlocate, String errorMsg, List<AllocateBatchImportErrorVO> allocateBatchImportErrorVOS) {
        AllocateBatchImportErrorVO allocateBatchImportErrorVO = new AllocateBatchImportErrorVO();
        BeanUtils.copyProperties(productBatchAlocate, allocateBatchImportErrorVO);
        allocateBatchImportErrorVO.setErrorMsg(errorMsg);
        allocateBatchImportErrorVOS.add(allocateBatchImportErrorVO);
    }

    private Integer getShopId(List<AllocateBatchImportErrorVO> allocateBatchImportErrorVOS, AllocateBatchImportVO productBatchAlocate, String orgName) {
        String configVal = configService.getConfigVal(SettingsEnum.SHOP_NAME);
        //先查看是不是平台名称，不是的话，则去查询shop表
        if (!configVal.equals(orgName)) {
            Shop shop = shopService.getOne(Wrappers.lambdaQuery(Shop.class).eq(Shop::getShopTitle, orgName).eq(Shop::getStatus, 1).eq(Shop::getShopType, ShopTypeEnum.STORE.getCode()).last("limit 1"));
            if (shop == null) {
                addImportErrorRecord(productBatchAlocate, orgName + "门店不存在", allocateBatchImportErrorVOS);
                return null;
            }
            return shop.getShopId();
        } else {
            return 0;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelAllocate(List<Integer> allocateIds) {
        if (tigshopProperties.getIsO2o() == 0) {
            return false;
        }
        Assert.isTrue(CollUtil.isNotEmpty(allocateIds), () -> new GlobalException("请选择要取消的调拨单"));
        for (Integer id : allocateIds) {
            ProductStockAllocateRecord byId = this.getById(id);
            Assert.notNull(byId, () -> new GlobalException("调拨单不存在"));
            Assert.isTrue(byId.getStatus() <= ProductStockAllocateStateEnum.STOCK_OUT_WAIT.getCode() || byId.getStatus().equals(ProductStockAllocateStateEnum.AUDIT_REJECT.getCode()), () -> new GlobalException("该调拨单不能作废"));
            byId.setStatus(ProductStockAllocateStateEnum.CANCEL.getCode());
            ProductStockAllocateOperationRecord operationRecord = new ProductStockAllocateOperationRecord();
            operationRecord.setAllocateId(byId.getAllocateId());
            operationRecord.setOperationType(ProductStockAllocateOperationEnum.ALLOCATE_CANCEL.getName());
            operationRecord.setAddBy(SecurityUtils.getCurrentUsername());
            operationRecord.setAddTime(System.currentTimeMillis() / 1000);
            productStockAllocateOperationRecordService.save(operationRecord);
            this.updateById(byId);
        }

        return true;
    }
}




