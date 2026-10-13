package com.tigshop.adminapi.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.user.MemberTagGroupCreateParam;
import com.tigshop.bean.param.user.MemberTagGroupQueryPage;
import com.tigshop.bean.param.user.MemberTagGroupUpdateParam;
import com.tigshop.bean.vo.user.MemberTagGroupDetailVO;
import com.tigshop.bean.vo.user.MemberTagGroupListVO;
import com.tigshop.common.annotation.Log;
import com.tigshop.service.user.MemberTagGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminapi/user/memberTagGroup")
@Tag(name = "会员标签组管理", description = "会员标签组管理")
@Validated
@PreAuthorize("@pms.hasPermission('memberTagManage')")
public class MemberTagGroupController {

    @Resource
    private MemberTagGroupService memberTagGroupService;

    @GetMapping("/list")
    @Operation(summary = "标签组分页列表")
    public Page<MemberTagGroupListVO> list(MemberTagGroupQueryPage queryPage) {
        return memberTagGroupService.list(queryPage);
    }

    @GetMapping("/detail")
    @Operation(summary = "标签组详情")
    public MemberTagGroupDetailVO detail(@RequestParam Long id) {
        return memberTagGroupService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "新增标签组")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("新增标签组：#{#param.groupName}")
    public void create(@Valid @RequestBody MemberTagGroupCreateParam param) {
        memberTagGroupService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新标签组")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("更新标签组：#{#param.groupName}")
    public void update(@Valid @RequestBody MemberTagGroupUpdateParam param) {
        memberTagGroupService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除标签组")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("删除标签组：#{#operateDTO.id}")
    public void del(@RequestBody OperateDTO operateDTO) {
        memberTagGroupService.delGroup(operateDTO.getId().longValue());
    }

    @PostMapping("/setTop")
    @Operation(summary = "置顶标签组")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("置顶标签组：#{#operateDTO.id}")
    public void setTop(@RequestBody OperateDTO operateDTO) {
        memberTagGroupService.setTop(operateDTO.getId().longValue());
    }

    @PostMapping("/cancelTop")
    @Operation(summary = "取消置顶标签组")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("取消置顶标签组：#{#operateDTO.id}")
    public void cancelTop(@RequestBody OperateDTO operateDTO) {
        memberTagGroupService.cancelTop(operateDTO.getId().longValue());
    }
}
