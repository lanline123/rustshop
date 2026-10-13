// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.dto.user.FeedbackUpdateDTO;
import com.tigshop.bean.vo.user.UserFeedbackDetailVO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.service.user.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.ResultTextConstants.*;
/**
 * 用户后台反馈
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping("/adminapi/user/feedback")
@Tag(name="会员留言")
@PreAuthorize("@pms.hasPermission('feedbackManage')")
public class FeedbackController {
    @Resource
    private FeedbackService feedbackService;

    @GetMapping("/list")
    @Operation(summary = "会员留言列表")
    public Page<UserFeedbackVO> list(FeedBackListPageQuery pageQuery){
        return feedbackService.listForAdmin(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "会员留言详情")
    public UserFeedbackDetailVO detail(@RequestParam @NotNull(message = ID_CANNOT_IS_NULL) Integer id){
       return feedbackService.detail(id);
    }

    @PostMapping("/del")
    @Operation(summary = "删除会员留言")
    @PreAuthorize("@pms.hasPermission('feedbackModifyManage')")
    public void del(@RequestBody OperateDTO operate){
        feedbackService.del(operate.getId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新会员留言")
    @PreAuthorize("@pms.hasPermission('feedbackModifyManage')")
    public void update(@RequestBody FeedbackUpdateDTO updateDTO) {
        feedbackService.update(updateDTO);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('feedbackModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        feedbackService.batch(batchDTO);
    }
}
