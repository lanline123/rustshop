package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemCreateParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemUpdateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponProductPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionItemPageQuery;
import com.tigshop.bean.vo.promotion.GrouponPostVO;
import com.tigshop.bean.vo.promotion.GrouponProductVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemDetailVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemPageVO;
import com.tigshop.service.promotion.GrouponPromotionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 拼团活动商品管理
 *
 * @author kidd
 * @since 2025/8/28 13:47
 */
@RequiredArgsConstructor
@Tag(name = "拼团活动商品管理", description = "拼团活动商品相关操作")
@RestController
@PreAuthorize("@pms.hasPermission('grouponPromotionManage')")
@RequestMapping("/adminapi/promotion/groupon/item")
public class GrouponPromotionItemController {
    
    private final GrouponPromotionItemService grouponPromotionItemService;

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody PromotionPromotionItemCreateParam param) {
        grouponPromotionItemService.create(param);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/update")
    public void update(@Valid @RequestBody PromotionPromotionItemUpdateParam param) {
        grouponPromotionItemService.update(param);
    }

    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/del")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        grouponPromotionItemService.del(operateDTO.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        grouponPromotionItemService.batch(batchDTO);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public GrouponPromotionItemDetailVO detail(@RequestParam("grouponPromotionItemId") Integer grouponPromotionItemId) {
        return grouponPromotionItemService.detail(grouponPromotionItemId);
    }
    
    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<GrouponPromotionItemPageVO> list(GrouponPromotionItemPageQuery pageQuery) {
        return grouponPromotionItemService.list(pageQuery);
    }

    @Operation(summary = "推广")
    @GetMapping("/post")
    public GrouponPostVO post(@RequestParam("grouponPromotionItemId") Long grouponPromotionItemId) {
        return grouponPromotionItemService.post(grouponPromotionItemId);
    }

    @Operation(summary = "拼团商品列表")
    @GetMapping("/productList")
    public Page<GrouponProductVO> productList(GrouponProductPageQuery pageQuery) {
        return grouponPromotionItemService.productList(pageQuery);
    }

}
