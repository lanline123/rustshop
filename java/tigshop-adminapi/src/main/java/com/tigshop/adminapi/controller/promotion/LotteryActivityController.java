package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.promotion.LotteryActivityIdParam;
import com.tigshop.bean.param.promotion.LotteryActivityParam;
import com.tigshop.bean.query.promotion.LotteryActivityQuery;
import com.tigshop.bean.query.promotion.LotteryUserManageQuery;
import com.tigshop.bean.query.promotion.LotteryUserRecordQuery;
import com.tigshop.bean.vo.promotion.LotteryActivityListVO;
import com.tigshop.bean.vo.promotion.LotteryActivityVO;
import com.tigshop.bean.vo.promotion.LotteryUserManageVO;
import com.tigshop.bean.vo.promotion.LotteryUserRecordVO;
import com.tigshop.service.promotion.ILotteryActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 抽奖活动Controller(后台管理)
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Tag(name = "抽奖活动管理", description = "抽奖活动管理相关接口")
@RestController
@RequestMapping("/adminapi/promotion/lotteryActivity")
@RequiredArgsConstructor
@Validated
@PreAuthorize("@pms.hasPermission('lotteryManage')")
public class LotteryActivityController {

    @Resource
    private ILotteryActivityService lotteryActivityService;

    @Operation(summary = "抽奖活动列表", description = "分页查询抽奖活动列表")
    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('lotteryManage')")
    public Page<LotteryActivityListVO> list(LotteryActivityQuery query) {
        return lotteryActivityService.getList(query);
    }

    @Operation(summary = "抽奖活动详情")
    @GetMapping("/detail")
    @PreAuthorize("@pms.hasPermission('lotteryManage')")
    public LotteryActivityVO detail(@RequestParam Integer activityId) {
        return lotteryActivityService.getDetail(activityId);
    }

    @Operation(summary = "创建抽奖活动", description = "创建新的抽奖活动，会检查时间冲突")
    @PostMapping("/create")
    @PreAuthorize("@pms.hasPermission('lotteryActivityModifyManage')")
    public Integer create(@Valid @RequestBody LotteryActivityParam param) {
        return lotteryActivityService.create(param);
    }

    @Operation(summary = "编辑抽奖活动", description = "编辑活动信息，只能编辑未开始的活动，会检查时间冲突")
    @PostMapping("/update")
    @PreAuthorize("@pms.hasPermission('lotteryActivityModifyManage')")
    public void update(@Valid @RequestBody LotteryActivityParam param) {
        lotteryActivityService.update(param);
    }

    @Operation(summary = "删除抽奖活动", description = "删除活动（软删除），进行中的活动不能删除")
    @PostMapping("/delete")
    @PreAuthorize("@pms.hasPermission('lotteryActivityModifyManage')")
    public void delete(@Valid @RequestBody LotteryActivityIdParam param) {
        lotteryActivityService.delete(param.getActivityId());
    }

    @Operation(summary = "结束抽奖活动", description = "立即结束进行中的活动")
    @PostMapping("/end")
    @PreAuthorize("@pms.hasPermission('lotteryActivityModifyManage')")
    public void end(@Valid @RequestBody LotteryActivityIdParam param) {
        lotteryActivityService.endActivity(param.getActivityId());
    }

    @Operation(summary = "用户管理列表", description = "分页查询抽奖活动的用户参与信息")
    @GetMapping("/userManage")
    @PreAuthorize("@pms.hasPermission('lotteryManage')")
    public Page<LotteryUserManageVO> userManage(@Valid LotteryUserManageQuery query) {
        return lotteryActivityService.getUserManageList(query);
    }

    @Operation(summary = "用户中奖详情列表", description = "分页查询用户在活动中的抽奖记录")
    @GetMapping("/userRecord")
    @PreAuthorize("@pms.hasPermission('lotteryManage')")
    public Page<LotteryUserRecordVO> userRecord(@Valid LotteryUserRecordQuery query) {
        return lotteryActivityService.getUserRecordList(query);
    }
}
