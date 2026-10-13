package com.tigshop.api.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.promotion.LotteryConfirmReceiveParam;
import com.tigshop.bean.query.promotion.LotteryRecordQuery;
import com.tigshop.bean.vo.promotion.LotteryActivityInfoVO;
import com.tigshop.bean.vo.promotion.LotteryRecordVO;
import com.tigshop.bean.vo.promotion.LotteryResultVO;
import com.tigshop.bean.vo.promotion.LotteryWinnerVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.promotion.ILotteryDrawService;
import com.tigshop.service.promotion.ILotteryRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抽奖Controller(用户端)
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Tag(name = "抽奖活动", description = "抽奖活动相关接口")
@RestController
@RequestMapping("/api/promotion/lottery")
@RequiredArgsConstructor
@Validated
public class LotteryController {

    @Resource
    private ILotteryDrawService lotteryDrawService;

    @Resource
    private ILotteryRecordService lotteryRecordService;

    @Operation(summary = "获取抽奖活动信息", description = "获取当前进行中的抽奖活动信息")
    @GetMapping("/info")
    public LotteryActivityInfoVO info() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return lotteryDrawService.getOngoingActivityInfo(userId);
    }

    @Operation(summary = "执行抽奖", description = "参与当前进行中的抽奖活动")
    @PostMapping("/draw")
    public LotteryResultVO draw() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return lotteryDrawService.drawOngoingActivity(userId);
    }

    @Operation(summary = "我的中奖记录")
    @GetMapping("/myRecords")
    public Page<LotteryRecordVO> myRecords(LotteryRecordQuery query) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return lotteryRecordService.getUserRecords(userId, query);
    }

    @Operation(summary = "确认领取商品奖品")
    @PostMapping("/confirmReceive")
    public void confirmReceive(@Valid @RequestBody LotteryConfirmReceiveParam param) {
        Integer userId = SecurityUtils.getCurrentUserId();
        lotteryRecordService.confirmReceive(param.getRecordId(), param.getOrderId(), userId);
    }

    @Operation(summary = "获取中奖名单", description = "获取抽奖活动的中奖名单（用户名脱敏展示）")
    @GetMapping("/winners")
    public List<LotteryWinnerVO> winners(
            @Parameter(description = "活动ID（不传则查询当前进行中的活动）") @RequestParam(required = false) Integer activityId) {
        return lotteryRecordService.getWinnerList(activityId);
    }
}


