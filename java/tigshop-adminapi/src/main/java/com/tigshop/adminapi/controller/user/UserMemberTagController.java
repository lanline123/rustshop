package com.tigshop.adminapi.controller.user;

import com.tigshop.bean.param.user.UserMemberTagBatchParam;
import com.tigshop.bean.param.user.UserMemberTagParam;
import com.tigshop.bean.vo.user.UserMemberTagVO;
import com.tigshop.common.annotation.Log;
import com.tigshop.service.user.UserMemberTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adminapi/user/userMemberTag")
@Tag(name = "用户标签关联管理", description = "用户标签关联管理")
@Validated
@PreAuthorize("@pms.hasPermission('memberTagManage')")
public class UserMemberTagController {

    @Resource
    private UserMemberTagService userMemberTagService;

    @GetMapping("/listByUser")
    @Operation(summary = "查询用户的所有标签")
    public List<UserMemberTagVO> listByUser(@RequestParam Long userId) {
        return userMemberTagService.listByUser(userId);
    }

    @PostMapping("/bindTags")
    @Operation(summary = "给用户打标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("给用户打标签：#{#param.userId}")
    public void bindTags(@Valid @RequestBody UserMemberTagParam param) {
        userMemberTagService.bindTags(param);
    }

    @PostMapping("/unbindTags")
    @Operation(summary = "取消用户标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("取消用户标签：#{#param.userId}")
    public void unbindTags(@Valid @RequestBody UserMemberTagParam param) {
        userMemberTagService.unbindTags(param);
    }

    @PostMapping("/batchBindTags")
    @Operation(summary = "批量给用户打标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("批量给用户打标签：#{#param.userIds}")
    public void batchBindTags(@Valid @RequestBody UserMemberTagBatchParam param) {
        userMemberTagService.batchBindTags(param);
    }

    @PostMapping("/batchUnbindTags")
    @Operation(summary = "批量取消用户标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("批量取消用户标签：#{#param.userIds}")
    public void batchUnbindTags(@Valid @RequestBody UserMemberTagBatchParam param) {
        userMemberTagService.batchUnbindTags(param);
    }
}
