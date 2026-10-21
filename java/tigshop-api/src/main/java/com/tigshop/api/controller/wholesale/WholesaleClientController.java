package com.tigshop.api.controller.wholesale;

import com.tigshop.bean.dto.wholesale.WholesaleCartBatchAddDTO;
import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.vo.wholesale.WholesaleCartBatchResultVO;
import com.tigshop.bean.vo.wholesale.WholesaleMeVO;
import com.tigshop.bean.vo.wholesale.WholesaleProductBuyRuleVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.wholesale.WholesaleCartBatchService;
import com.tigshop.service.wholesale.WholesaleProductQueryService;
import com.tigshop.service.wholesale.WholesaleSettingService;
import com.tigshop.service.wholesale.WholesalerGradeService;
import com.tigshop.service.wholesale.WholesalerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端批发接口（用户侧）。
 *
 * <p>接口契约见 api-contract §3（C1 /me、C2 /product/buyRule、C3 /cart/addBatch）。
 * 按 CD-019 散落归属约定，本 Controller 由各 task 散落实现：
 * <ul>
 *   <li>{@code POST /api/wholesale/cart/addBatch} — W3-T304（本文件首次建表 + 该端点）</li>
 *   <li>{@code GET  /api/wholesale/me}            — W1-T105（后续 append）</li>
 *   <li>{@code GET  /api/wholesale/product/buyRule} — W2-T208（后续 append）</li>
 * </ul>
 *
 * <p>异常通过 {@7 link GlobalException} 抛出，由全局 ExceptionHandler 映射为 AjaxResult。
 *
 * @author Tigshop团队
 * @create 2026-06-03
 */
@Tag(name = "C端-批发", description = "批发商体系 C 端接口（仅批发商可用）")
@RestController
@RequestMapping("/api/wholesale")
@RequiredArgsConstructor
@Validated
@Slf4j
public class WholesaleClientController {

    private final WholesalerService wholesalerService;
    private final WholesalerGradeService wholesalerGradeService;
    private final WholesaleSettingService wholesaleSettingService;
    private final WholesaleCartBatchService wholesaleCartBatchService;
    private final WholesaleProductQueryService wholesaleProductQueryService;

    /**
     * 我是否批发商（TASK-WS-105 / api-contract §3.C1）。
     *
     * <p>语义（全部走"返回 isWholesaler=false 而非报错"）：
     * <ul>
     *   <li>未登录（{@code userId<=0}）→ {@code isWholesaler=false}</li>
     *   <li>批发功能总开关关闭（{@code wholesaleEnabled=0}）→ {@code isWholesaler=false}</li>
     *   <li>已登录但非批发商 → {@code isWholesaler=false}</li>
     *   <li>已登录且为批发商 → 回填 {@code gradeId / gradeName}</li>
     * </ul>
     * 前端用 {@code globalEnabled} 决定是否渲染批发入口。
     *
     * @return 当前用户的批发身份概览
     */
    @Operation(summary = "我是否批发商",
            description = "未登录返回 isWholesaler=false；批发功能总开关关闭也返回 isWholesaler=false。前端用 globalEnabled 决定是否渲染批发入口。")
    @GetMapping("/me")
    public WholesaleMeVO me() {
        WholesaleMeVO vo = new WholesaleMeVO();
        boolean globalEnabled = wholesaleSettingService.isEnabled();
        vo.setGlobalEnabled(globalEnabled);

        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0 || !globalEnabled) {
            vo.setIsWholesaler(false);
            return vo;
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            vo.setIsWholesaler(false);
            return vo;
        }
        Wholesaler w = wholesalerService.getByUserId(userId);
        Integer gradeId = w != null ? w.getGradeId() : null;
        String gradeName = null;
        if (gradeId != null && gradeId > 0) {
            WholesalerGrade g = wholesalerGradeService.getById(gradeId);
            if (g != null) {
                gradeName = g.getGradeName();
            }
        }
        vo.setIsWholesaler(true);
        vo.setGradeId(gradeId);
        vo.setGradeName(gradeName);
        return vo;
    }

    /**
     * 商品起批规则（TASK-WS-208 / api-contract §3.C2）。
     *
     * <p>契约："非批发商也可查，前端预览用"，因此本端点不做身份判定；
     * 未配置批发能力的商品返回 {@code configured=false}（不抛错）。
     *
     * @param productId 商品 ID（必填，正整数）
     * @return 起批规则（含 dimension / moq / skuRules / hint）
     */
    @Operation(summary = "商品起批规则",
            description = "非批发商也可查（前端预览用）；未配置批发能力时 configured=false。")
    @GetMapping("/product/buyRule")
    public WholesaleProductBuyRuleVO productBuyRule(
            @Parameter(description = "商品 ID", required = true)
            @RequestParam("productId") @NotNull @Positive Integer productId) {
        return wholesaleProductQueryService.buildBuyRule(productId);
    }

    /**
     * 批量加车（仅批发商）。批发常态多 SKU 一次提交，部分失败不回滚成功行。
     *
     * <p>整批级校验（任意失败整批抛错）：
     * <ol>
     *   <li>{@code wholesaleEnabled = 0} → 5300 {@code WHOLESALE_DISABLED}</li>
     *   <li>非批发商（{@link WholesalerService#isWholesaleBuyer} = false）→ 403 {@code FORBIDDEN}</li>
     *   <li>DTO {@code @Valid} 校验 items size 1-50 + 必填 → 400</li>
     * </ol>
     *
     * <p>逐项失败（不阻断整批）：收集到 {@link WholesaleCartBatchResultVO#getFailedItems}，
     * code 取既有业务码（5310 / 5312 / 5320 / 5321 / 5322 等，由 {@code CartService#addProductToCart}
     * 内部接入的 {@code CartWholesaleEnhancer} 抛出）。
     *
     * @param dto 批量入参
     * @return 成功 / 失败计数 + 失败明细
     * @see com.tigshop.bean.dto.wholesale.WholesaleCartBatchAddDTO
     */
    @Operation(summary = "批量加车（仅批发商）",
            description = "批发常态多 SKU 一次提交。整批级失败抛 5300/403；行级失败进 failedItems。")
    @PostMapping("/cart/addBatch")
    public WholesaleCartBatchResultVO addBatch(@Valid @RequestBody WholesaleCartBatchAddDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new GlobalException("用户未登录", HttpStatus.UNAUTHORIZED);
        }
        if (!wholesaleSettingService.isEnabled()) {
            throw new GlobalException("批发功能未启用", HttpStatus.WHOLESALE_DISABLED);
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            throw new GlobalException("非批发商，无权使用批发批量加车", HttpStatus.FORBIDDEN);
        }
        return wholesaleCartBatchService.addBatch(userId, dto);
    }
}
