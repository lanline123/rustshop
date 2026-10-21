package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionCreateParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionUpdateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionClientPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionPageQuery;
import com.tigshop.bean.vo.promotion.GrouponPostVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionClientPageVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionDetailVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionPageVO;
import com.tigshop.service.promotion.GrouponPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 拼团活动管理
 *
 * @author kidd
 * @since 2025/8/28 13:47
 */
@RequiredArgsConstructor
@Tag(name = "拼团活动管理", description = "拼团活动相关操作")
@RestController
@PreAuthorize("@pms.hasPermission('grouponPromotionManage')")
@RequestMapping("/adminapi/promotion/groupon")
public class GrouponPromotionController {
    
    private final GrouponPromotionService grouponPromotionService;

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/create")
    public void create(@Valid @RequestBody PromotionPromotionCreateParam param) {
        grouponPromotionService.create(param);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/update")
    public void update(@Valid @RequestBody PromotionPromotionUpdateParam param) {
        grouponPromotionService.update(param);
    }

    @Operation(summary = "立即结束")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/finish")
    public void finish(@Valid @RequestBody OperateDTO param) {
        grouponPromotionService.finish(param);
    }

    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('grouponPromotionModifyManage')")
    @PostMapping("/del")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        grouponPromotionService.del(operateDTO.getId());
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public GrouponPromotionDetailVO detail(@RequestParam("grouponPromotionId") Integer grouponPromotionId) {
        return grouponPromotionService.detail(grouponPromotionId);
    }
    
    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<GrouponPromotionPageVO> list(GrouponPromotionPageQuery pageQuery) {
        pageQuery.queryData();
        return grouponPromotionService.list(pageQuery);
    }

    @Operation(summary = "活动推广")
    @GetMapping("/promotionPromote")
    public GrouponPostVO post(@RequestParam("grouponPromotionId") Integer grouponPromotionId) {
        return grouponPromotionService.post(grouponPromotionId);
    }

    @Operation(summary = "获取客户端列表")
    @GetMapping("/clientList")
    public Page<GrouponPromotionClientPageVO> clientList(GrouponPromotionClientPageQuery pageQuery) {
        pageQuery.queryData();
        return grouponPromotionService.clientList(pageQuery);
    }
}
