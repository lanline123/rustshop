package com.tigshop.api.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordPageQuery;
import com.tigshop.bean.vo.promotion.GrouponRecordDetailVO;
import com.tigshop.bean.vo.promotion.GrouponRecordVO;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拼团记录管理
 *
 * @author kidd
 * @since 2025/8/29 15:35
 */
@RequiredArgsConstructor
@Tag(name = "拼团记录管理")
@RestController
@RequestMapping("/api/promotion/groupon/record")
public class GrouponRecordsController {

    private final GrouponPromotionRecordService grouponPromotionRecordService;

    @Operation(summary = "拼团活动记录列表")
    @GetMapping("/list")
    public Page<GrouponRecordVO> clientList(GrouponRecordPageQuery pageQuery) {
        return grouponPromotionRecordService.clientList(pageQuery);
    }

    @Operation(summary = "拼团活动详情")
    @GetMapping("/details")
    public GrouponRecordDetailVO clientDetails(@RequestParam("grouponPromotionRecordId") Long grouponPromotionRecordId) {
        return grouponPromotionRecordService.clientDetails(grouponPromotionRecordId);
    }

}
