package com.tigshop.service.wholesale.impl;

import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.wholesale.WholesaleCartBatchAddDTO;
import com.tigshop.bean.vo.wholesale.WholesaleCartBatchResultVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.wholesale.WholesaleCartBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * C 端批发批量加车实现（W3 T-304）。
 *
 * <p>实现策略：逐项调用既有 {@link CartService#addProductToCart}（每项独立 {@code @Transactional}），
 * try-catch 收集失败明细。本类自身**不**加 {@code @Transactional}，避免外层事务把单行失败放大为整批回滚。
 *
 * @author Tigshop团队
 * @create 2026-06-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WholesaleCartBatchServiceImpl implements WholesaleCartBatchService {

    private final CartService cartService;

    @Override
    public WholesaleCartBatchResultVO addBatch(Integer userId, WholesaleCartBatchAddDTO dto) {
        WholesaleCartBatchResultVO result = new WholesaleCartBatchResultVO();
        List<WholesaleCartBatchResultVO.FailedItem> failedItems = new ArrayList<>();
        int successCount = 0;

        for (WholesaleCartBatchAddDTO.Item item : dto.getItems()) {
            try {
                cartService.addProductToCart(userId, toAddToCartDTO(userId, item));
                successCount++;
            } catch (GlobalException e) {
                log.warn("[wholesaleCartBatch] item failed userId={} productId={} skuId={} qty={} code={} reason={}",
                        userId, item.getProductId(), item.getSkuId(), item.getQuantity(),
                        e.getCode(), e.getMessage());
                failedItems.add(buildFailed(item, e.getMessage(), e.getCode()));
            } catch (Exception e) {
                log.error("[wholesaleCartBatch] item unexpected fail userId={} productId={} skuId={} qty={}",
                        userId, item.getProductId(), item.getSkuId(), item.getQuantity(), e);
                failedItems.add(buildFailed(item,
                        e.getMessage() == null ? "系统异常" : e.getMessage(),
                        HttpStatus.SERVICE_DATA_ERROR));
            }
        }

        result.setSuccessCount(successCount);
        result.setFailedCount(failedItems.size());
        result.setFailedItems(failedItems);
        return result;
    }

    /**
     * 把批量项映射为既有 {@link AddToCartDTO}：仅填批发场景必需字段，
     * 其余（type / extraData / cycleSpecId 等）由 {@link AddToCartDTO#initParam()} 与 Service 内部默认值兜底。
     */
    private AddToCartDTO toAddToCartDTO(Integer userId, WholesaleCartBatchAddDTO.Item item) {
        AddToCartDTO addDto = new AddToCartDTO();
        addDto.setId(item.getProductId());
        addDto.setSkuId(item.getSkuId());
        addDto.setNumber(item.getQuantity());
        addDto.setUserId(userId);
        return addDto;
    }

    private WholesaleCartBatchResultVO.FailedItem buildFailed(WholesaleCartBatchAddDTO.Item item,
                                                              String reason, int code) {
        WholesaleCartBatchResultVO.FailedItem failed = new WholesaleCartBatchResultVO.FailedItem();
        failed.setProductId(item.getProductId());
        failed.setSkuId(item.getSkuId());
        failed.setReason(reason);
        failed.setCode(code);
        return failed;
    }
}
