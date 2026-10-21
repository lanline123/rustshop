package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOpenJoinMemberQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOpenJonPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOrderPageQuery;
import com.tigshop.bean.vo.promotion.GrouponRecordOpenJoinMemberVO;
import com.tigshop.bean.vo.promotion.GrouponRecordOpenJonPageVO;
import com.tigshop.bean.vo.promotion.GrouponRecordOrderPageVO;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团活动记录管理
 *
 * @author kidd
 * @since 2025/8/29 09:23
 */
@RequiredArgsConstructor
@Tag(name = "拼团活动记录管理", description = "拼团活动记录管理")
@RestController
@PreAuthorize("@pms.hasPermission('grouponPromotionManage')")
@RequestMapping("/adminapi/promotion/groupon/record")
public class GrouponPromotionRecordController {

    private final GrouponPromotionRecordService grouponPromotionRecordService;

    @Operation(summary = "开团记录")
    @GetMapping("/openJoinList")
    public Page<GrouponRecordOpenJonPageVO> openJoinList(GrouponRecordOpenJonPageQuery pageQuery) {
        return grouponPromotionRecordService.openJoinList(pageQuery);
    }

    @Operation(summary = "订单记录")
    @GetMapping("/orderList")
    public Page<GrouponRecordOrderPageVO> orderList(GrouponRecordOrderPageQuery pageQuery) {
        return grouponPromotionRecordService.orderList(pageQuery);
    }

    @Operation(summary = "团员列表")
    @GetMapping("/groupMemberList")
    public List<GrouponRecordOpenJoinMemberVO> groupMemberList(GrouponRecordOpenJoinMemberQuery pageQuery) {
        return grouponPromotionRecordService.groupMemberList(pageQuery);
    }

    @Operation(summary = "手动成团")
    @PostMapping("/manualGroup")
    public void manualGroup(@RequestParam("grouponPromotionRecordId") Long grouponPromotionRecordId) {
        grouponPromotionRecordService.manualGroup(grouponPromotionRecordId);
    }

    @Operation(summary = "批量手动成团")
    @PostMapping("/batchGroup")
    public void batchManualGroup(@RequestBody List<Long> grouponPromotionRecordIds) {
        grouponPromotionRecordService.manualGroup(grouponPromotionRecordIds);
    }

}
