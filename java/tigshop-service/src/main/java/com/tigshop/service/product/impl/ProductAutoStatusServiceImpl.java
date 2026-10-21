// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.product.ProductAutoStatusCreateDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.product.ProductAutoStatusEnum;
import com.tigshop.bean.enums.product.ProductOperationTypeEnum;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.ECardGroup;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductAutoStatus;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.properties.ProductAutoStatusProperties;
import com.tigshop.mapper.product.ProductAutoStatusMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.product.ECardGroupService;
import com.tigshop.service.product.ProductAutoStatusService;
import com.tigshop.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 商品自动上下架服务实现
 *
 * @author Tigshop团队
 * @create 2025年12月2日
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAutoStatusServiceImpl extends BaseServiceImpl<ProductAutoStatusMapper, ProductAutoStatus> implements ProductAutoStatusService {
    private final ProductMapper productMapper;
    private final ObjectProvider<ProductService> productServices;
    private final ProductAutoStatusProperties properties;
    private final ApplicationContext applicationContext;
    private final ECardGroupService eCardGroupService;
    private final AdminMsgService adminMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(ProductAutoStatusCreateDTO dto) {
        // 验证计划时间是否大于当前时间
        long currentTime = DateUtil.currentSeconds();
        if (dto.getScheduledTime() <= currentTime) {
            return false;
        }
        ProductAutoStatus entity = ProductAutoStatus.builder()
                .productId(dto.getProductId())
                .shopId(dto.getShopId() != null ? dto.getShopId() : 0)
                .operationType(dto.getOperationType())
                .scheduledTime(dto.getScheduledTime())
                .status(ProductAutoStatusEnum.PENDING.getCode())
                .retryCount(0)
                .createTime(currentTime)
                .updateTime(currentTime)
                .isDeleted(0).build();
        return this.save(entity);
    }
    @Override
    public void executeAutoStatusTasks() {
        long currentTime = DateUtil.currentSeconds();
        int batchSize = properties.getBatchSize();

        while (true) {
            // 直接从数据库查询指定数量的待执行任务（使用 offset=0 因为已执行的会被标记为完成状态）
            List<ProductAutoStatus> pendingTasks = this.lambdaQuery().eq(ProductAutoStatus::getStatus, ProductAutoStatusEnum.PENDING.getCode())
                    .eq(ProductAutoStatus::getIsDeleted, 0)
                    //小于等于 scheduledTime <= 当前时间
                    .le(ProductAutoStatus::getScheduledTime, currentTime)
                    //小于
                    .lt(ProductAutoStatus::getRetryCount, properties.getMaxRetryCount())
                    //从大到小排序
                    .orderByDesc(ProductAutoStatus::getScheduledTime).last(" limit " + batchSize).list();

            if (pendingTasks == null || pendingTasks.isEmpty()) {
                break;
}
            // 使用异步方式执行本批任务
            List<CompletableFuture<Void>> futures = pendingTasks.stream().map(this::executeTask).toList();

            // 等待本批任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    /**
     * 异步执行单个任务
     */
    public CompletableFuture<Void> executeTask(ProductAutoStatus task) {
        ProductAutoStatusService bean = applicationContext.getBean(ProductAutoStatusService.class);
        return CompletableFuture.runAsync(() -> {
            try{
                bean.doExecuteTask(task);
            }catch (Throwable e){
                //忽略
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doExecuteTask(ProductAutoStatus task) {
        try {
            Integer shopId = task.getShopId() != null ? task.getShopId() : 0;

            // 查询商品
            Product product = productMapper.selectById(task.getProductId());
            if (product == null) {
                updateTaskStatus(task.getId(), ProductAutoStatusEnum.FAILED.getCode(), "商品不存在", task.getRetryCount());
                return;
            }


            // 执行上下架操作
            Integer newStatus = task.getOperationType().equals(ProductOperationTypeEnum.ONLINE.getCode())
                    ? Constants.YES : Constants.NO;

            //电子卡卷
            if (product.getProductType() == ProductType.PRODUCT_TYPE_CARD.getCode() && newStatus == 1) {
                ECardGroup byId = eCardGroupService.getById(product.getCardGroupId());
                if (byId.getIsUse() == 0) {
                    updateTaskStatus(task.getId(), ProductAutoStatusEnum.FAILED.getCode(),
                            "电子卡卷组【" + byId.getGroupName() + "】未启用，请先启用电子卡卷组！",
                            task.getRetryCount());
                    return;
                }
            }


            //商品的门店或者商品id
            Integer productShopId = product.getShopId();

            // 平台商品分配给门店的情形，只有 o2o 版本存在
            boolean isStoreAllocatedProduct = (productShopId == null || productShopId == 0) && shopId > 0;


            if (!isStoreAllocatedProduct) {
                //多商户,多门店自己的商品
                ProductService productService = productServices.getIfAvailable();
                productService.lambdaUpdate().eq(Product::getProductId, task.getProductId())
                        .eq(Product::getShopId, shopId)
                        .set(Product::getProductStatus, newStatus).update();
            }

            if(newStatus == 0){
                AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
                createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
                createDTO.setShopId(product.getShopId());
                createDTO.setProductId(product.getProductId());
                createDTO.setTitle("商品下架:" + product.getProductName());
                createDTO.setContent("您的商品【" + product.getProductName() + "】已经下架，请及时处理！");
                Map<String, Object> relatedData = new HashMap<>();
                relatedData.put("productId", product.getProductId());
                createDTO.setRelatedData(relatedData);
                adminMsgService.createMessage(createDTO);
            }

            updateTaskStatus(task.getId(), ProductAutoStatusEnum.SUCCESS.getCode(), null, task.getRetryCount());
        } catch (Exception e) {
            // 更新任务状态为失败
            updateTaskStatus(task.getId(), ProductAutoStatusEnum.FAILED.getCode(), "执行异常: " + e.getMessage(), task.getRetryCount());
            throw e;
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Integer taskId, Integer status, String errorMessage, Integer retryCount) {
        LambdaUpdateWrapper<ProductAutoStatus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductAutoStatus::getId, taskId).set(ProductAutoStatus::getStatus, status).set(ProductAutoStatus::getExecuteTime, DateUtil.currentSeconds()).set(ProductAutoStatus::getUpdateTime, DateUtil.currentSeconds()).set(ProductAutoStatus::getRetryCount, retryCount + 1);

        if (StrUtil.isNotBlank(errorMessage)) {
            updateWrapper.set(ProductAutoStatus::getErrorMessage, errorMessage);
        }

        this.update(updateWrapper);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelProductTasks(Integer productId, Integer shopId) {
        LambdaUpdateWrapper<ProductAutoStatus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductAutoStatus::getProductId, productId);
        updateWrapper.eq(ProductAutoStatus::getShopId, shopId==null ? 0: shopId);
        return this.remove(updateWrapper);
    }
}
