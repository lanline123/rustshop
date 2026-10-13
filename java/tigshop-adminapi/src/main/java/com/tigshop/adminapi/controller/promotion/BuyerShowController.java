package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.promotion.buyershow.*;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowPageVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerVO;
import com.tigshop.service.promotion.BuyerShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.BUYER_SHOW_CONFIG_FIELDS;

/**
 * 买家秀管理控制器
 *
 * @author Tigshop团队
 * &#064;create  2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/buyerShow")
@Tag(name = "买家秀管理", description = "买家秀管理")
@Validated
@RequiredArgsConstructor
public class BuyerShowController {

    private final BuyerShowService buyerShowService;


    @GetMapping("/info")
    @Operation(summary = "生成虚拟用户信息")
    public void info() throws Exception {
        buyerShowService.info();
    }

    @GetMapping("/getBuyerInfo")
    @Operation(summary = "获取买家信息（随机）")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public BuyerVO getBuyerInfo(){
        return buyerShowService.getBuyerInfo();
    }

    @PostMapping("/create")
    @Operation(summary = "新建买家秀")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void create(@Valid @RequestBody BuyerShowSaveParam param) {
        buyerShowService.create(param);
    }

    @GetMapping("/list")
    @Operation(summary = "买家秀列表")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public Page<BuyerShowPageVO> list(BuyerShowPageQuery query) {
        return buyerShowService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "买家秀详情")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public BuyerShowVO detail(@RequestParam(value = "buyerShowId") @NotNull(message = "买家秀主键不能为空") Integer buyerShowId) {
        return buyerShowService.detail(buyerShowId);
    }

    @PostMapping("/update")
    @Operation(summary = "更新买家秀")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void update(@RequestBody @Validated BuyerShowUpdateParam param) {
        buyerShowService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除买家秀")
    @PreAuthorize("@pms.hasPermission('buyerShowDeleteManage')")
    public void delete(@RequestBody @Validated OperateDTO operate) {
        buyerShowService.del(operate.getId());
    }

    @PostMapping("/batchDel")
    @Operation(summary = "批量删除买家秀")
    @PreAuthorize("@pms.hasPermission('buyerShowDeleteManage')")
    public void batchDel(@RequestBody @Valid BuyerShowBatchDeleteParam param) {
        buyerShowService.batchDel(param.getIds());
    }

    @PostMapping("/audit")
    @Operation(summary = "审核（通过、拒绝）")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void audit(@RequestBody @Validated BuyerShowAuditParam param) {
        buyerShowService.audit(param);
    }

    @Operation(summary = "批量操作（审核操作类型为audit）")
    @PostMapping("/batch")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void batch(@RequestBody @Valid BatchDTO batch) {
        buyerShowService.batch(batch);
    }

    @PostMapping("/showOrHide")
    @Operation(summary = "是否公开（公开、隐藏）")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void showOrHide(@RequestBody @Validated BuyerShowShowOrHideParam param) {
        buyerShowService.showOrHide(param);
    }

    @PostMapping("/comment")
    @Operation(summary = "评论买家秀")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void comment(@RequestBody @Validated BuyerShowCommentParam  param) {
        buyerShowService.comment(param);
    }


    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        buyerShowService.updateField(updateField,  BUYER_SHOW_CONFIG_FIELDS);
    }
}
