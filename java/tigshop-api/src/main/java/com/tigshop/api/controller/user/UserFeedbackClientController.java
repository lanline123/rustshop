package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.dto.user.FeedbackCreateDTO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.user.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户反馈
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/feedback"))
@Tag(name = "前台用户反馈留言")
@Validated

public class UserFeedbackClientController {
    @Resource
    FeedbackService feedbackService;

    @GetMapping("/list")
    @Operation(summary = "订单咨询/留言列表")
    public Page<UserFeedbackVO> list(FeedBackListPageQuery dto) {
        dto.setUserId(SecurityUtils.getCurrentUserId());
        return feedbackService.list(dto);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交留言")
    public void submit(@RequestBody FeedbackCreateDTO dto) {
        feedbackService.submit(dto);
    }
}
