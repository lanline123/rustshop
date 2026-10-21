package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentAuditParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentIsTopParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentShowOrHideParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowCommentPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowCommentPageVO;
import com.tigshop.service.promotion.BuyerShowCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 买家秀评论管理控制器
 *
 * @author Tigshop团队
 * &#064;create  2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/buyerShow/commentInfo")
@Tag(name = "买家秀评论管理", description = "买家秀评论管理")
@Validated
@RequiredArgsConstructor
public class BuyerShowCommentController {

    private final BuyerShowCommentService buyerShowCommentService;

    @GetMapping("/list")
    @Operation(summary = "获取相关评论列表")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public Page<BuyerShowCommentPageVO> list(BuyerShowCommentPageQuery query) {
        return buyerShowCommentService.list(query);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核（通过、拒绝）")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void audit(@RequestBody @Validated BuyerShowCommentAuditParam param) {
        buyerShowCommentService.audit(param);
    }

    @Operation(summary = "批量操作（审核操作类型为audit）")
    @PostMapping("/batch")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void batch(@RequestBody @Valid BatchDTO batch) {
        buyerShowCommentService.batch(batch);
    }

    @PostMapping("/showOrHide")
    @Operation(summary = "是否公开（公开、隐藏）")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void showOrHide(@RequestBody @Validated BuyerShowCommentShowOrHideParam param) {
        buyerShowCommentService.showOrHide(param);
    }

    @PostMapping("/isTop")
    @Operation(summary = "是否置顶（置顶、取消置顶）")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void isTop(@RequestBody @Validated BuyerShowCommentIsTopParam param) {
        buyerShowCommentService.isTop(param);
    }

}
