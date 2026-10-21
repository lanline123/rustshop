package com.tigshop.service.wholesale;

import com.tigshop.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * 批发商促销列表过滤门面（CR-20260713-1620）。
 *
 * <p>语义：批发商「只看批发价」——对当前批发商「已配置对其生效批发价」的商品，
 * 从 C 端各类促销/活动列表隐藏（避免与批发价冲突的促销价出现在活动列表里）。
 *
 * <p>短路口径：非批发商 / 未登录 / 批发功能未启用（{@code wholesale.enabled=0}）
 * 一律返回空集，调用方据此不加任何过滤，与现网行为逐字节等价。
 *
 * <p>本类整文件位于 {@code service/wholesale/} 目录，随 VORT feature
 * {@code common.wholesale} 在关闭版型被物理删除；消费方（秒杀/拼团等核心 service）
 * 对本类的 import/字段/调用必须用 {@code // @vort-if common.wholesale} 包裹。
 *
 * @author Tigshop团队
 * @create 2026-07-13
 */
@Service
@RequiredArgsConstructor
public class WholesalePromotionFilter {

    private final WholesalerService wholesalerService;
    private final WholesalePriceService wholesalePriceService;

    /**
     * 当前登录用户若为批发商，返回其「有批发价、需从促销列表隐藏」的商品 ID 集合；
     * 否则（未登录 / 非批发商 / 功能未启用）返回空集，表示不过滤。
     *
     * @return 需从 C 端促销列表隐藏的 product_id 集合（不过滤时为空集，非 null）
     */
    public Set<Integer> currentBuyerExcludedProductIds() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return Collections.emptySet();
        }
        // isWholesaleBuyer 内部已对全局开关 wholesale.enabled 短路
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return Collections.emptySet();
        }
        Integer gradeId = wholesalerService.getGradeIdByUserId(userId);
        return wholesalePriceService.listPricedProductIdsByGrade(gradeId);
    }
}
