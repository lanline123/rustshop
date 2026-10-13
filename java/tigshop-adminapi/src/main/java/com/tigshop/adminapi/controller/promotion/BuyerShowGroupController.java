package com.tigshop.adminapi.controller.promotion;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowGroupSaveParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowGroupUpdateParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowGroupPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowGroupPageVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowGroupVO;
import com.tigshop.service.promotion.BuyerShowGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 买家秀分组管理
 *
 * @author Tigshop团队
 * &#064;create  2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/buyerShow/group")
@Tag(name = "买家秀分组管理", description = "买家秀分组管理")
@Validated
@RequiredArgsConstructor
public class BuyerShowGroupController {

    private final BuyerShowGroupService buyerShowGroupService;

    @PostMapping("/create")
    @Operation(summary = "新建买家秀分组")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void create(@Valid @RequestBody BuyerShowGroupSaveParam param) {
        buyerShowGroupService.create(param);
    }

    @GetMapping("/list")
    @Operation(summary = "买家秀分组列表")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public Page<BuyerShowGroupPageVO> list(BuyerShowGroupPageQuery query) {
        return buyerShowGroupService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "买家秀分组详情")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public BuyerShowGroupVO detail(@RequestParam(value = "buyerShowGroupId") @NotNull(message = "买家分组主键不能为空") Integer buyerShowGroupId) {
        return buyerShowGroupService.detail(buyerShowGroupId);
    }

    @PostMapping("/update")
    @Operation(summary = "更新买家秀分组")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void update(@Valid @RequestBody BuyerShowGroupUpdateParam param) {
        buyerShowGroupService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除买家秀分组")
    @PreAuthorize("@pms.hasPermission('buyerShowUpdateManage')")
    public void delete(@RequestBody @Validated OperateDTO operate) {
        buyerShowGroupService.del(operate.getId());
    }


}
