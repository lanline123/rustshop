package com.tigshop.service.wholesale;

import com.tigshop.bean.dto.wholesale.WholesaleCartBatchAddDTO;
import com.tigshop.bean.vo.wholesale.WholesaleCartBatchResultVO;

/**
 * C 端批发批量加车 Service（W3 T-304）。
 *
 * <p>设计要点（api-contract §3 C3 / design §5.5）：
 * <ul>
 *   <li>整批级校验由 Controller 完成（{@link WholesaleSettingService#isEnabled} 5300 +
 *       {@link WholesalerService#isWholesaleBuyer} 403）；本 Service 假设已通过整批级校验。</li>
 *   <li>逐项独立事务：复用既有 {@code CartService#addProductToCart}（{@code @Transactional}），
 *       每项一个事务边界，部分失败不回滚成功行；本 Service 自身**不**加 {@code @Transactional}。</li>
 *   <li>逐项 try-catch 收集 {@code failedItems[{productId, skuId, reason, code}]}，业务码取
 *       {@link com.tigshop.common.exception.GlobalException#getCode}；非 GlobalException
 *       归入 {@link com.tigshop.common.constant.HttpStatus#SERVICE_DATA_ERROR}。</li>
 *   <li>命中批发价 / 起批量 / 倍数 / 降级 等逻辑由 {@code CartService#addProductToCart}
 *       内部（T-303 接入的 {@code CartWholesaleEnhancer#markAndValidateOnAdd}）承担，
 *       本 Service 不重复校验。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-03
 */
public interface WholesaleCartBatchService {

    /**
     * 批量加车。逐项独立事务，部分失败不回滚成功行；失败行收集到 {@link WholesaleCartBatchResultVO#getFailedItems}。
     *
     * @param userId 当前登录用户 ID（Controller 已保证为已登录的批发商）
     * @param dto    批量入参（@Valid 已在 Controller 层完成 size 1-50 / 必填校验）
     * @return 成功数 + 失败数 + 失败明细
     */
    WholesaleCartBatchResultVO addBatch(Integer userId, WholesaleCartBatchAddDTO dto);
}
