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
import com.tigshop.bean.dto.user.UserMessageLogCreateDTO;
import com.tigshop.bean.param.user.UserMessageLogRecallParam;
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.vo.user.UserMessageLogPageVO;
import com.tigshop.service.user.UserMessageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/user/userMessageLog")
@Tag(name = "会员消息管理")
@Validated
@PreAuthorize("@pms.hasPermission('messageLogManage')")
public class UserMessageLogController {

    @Resource
    private UserMessageLogService userMessageLogService;

    @GetMapping("/list")
    @Operation(summary = "会员消息列表")
    public Page<UserMessageLogPageVO> list(UserMessageListPageQuery pageQuery) {
        return userMessageLogService.list(pageQuery);
    }

    @PostMapping("/create")
    @Operation(summary = "创建会员消息")
    @PreAuthorize("@pms.hasPermission('userMessageLogModifyManage')")
    public void create(@RequestBody UserMessageLogCreateDTO dto) {
        userMessageLogService.create(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除会员消息")
    @PreAuthorize("@pms.hasPermission('userMessageLogModifyManage')")
    public void del(@RequestBody @Validated UserMessageLogRecallParam param) {
        userMessageLogService.del(param.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('userMessageLogModifyManage')")
    public void batch(@RequestBody BatchDTO batch) {
        userMessageLogService.batch(batch);
    }

    @PostMapping("recall")
    @Operation(summary = "撤回")
    @PreAuthorize("@pms.hasPermission('userMessageLogModifyManage')")
    public void recall(@RequestBody @Validated UserMessageLogRecallParam param) {
        userMessageLogService.recall(param);
    }
}
