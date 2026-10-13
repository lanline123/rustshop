package com.tigshop.adminapi.controller.user;

import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.user.MemberTagCreateParam;
import com.tigshop.bean.param.user.MemberTagUpdateParam;
import com.tigshop.bean.vo.user.MemberTagVO;
import com.tigshop.common.annotation.Log;
import com.tigshop.service.user.MemberTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adminapi/user/memberTag")
@Tag(name = "会员标签管理", description = "会员标签管理")
@Validated
@PreAuthorize("@pms.hasPermission('memberTagManage')")
public class MemberTagController {

    @Resource
    private MemberTagService memberTagService;

    @GetMapping("/listByGroup")
    @Operation(summary = "查询组下标签列表")
    public List<MemberTagVO> listByGroup(@RequestParam Long groupId) {
        return memberTagService.listByGroupId(groupId);
    }

    @PostMapping("/create")
    @Operation(summary = "新增标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("新增标签：#{#param.tagName}")
    public void create(@Valid @RequestBody MemberTagCreateParam param) {
        memberTagService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("更新标签：#{#param.tagName}")
    public void update(@Valid @RequestBody MemberTagUpdateParam param) {
        memberTagService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除标签")
    @PreAuthorize("@pms.hasPermission('memberTagUpdateManage')")
    @Log("删除标签：#{#operateDTO.id}")
    public void del(@RequestBody OperateDTO operateDTO) {
        memberTagService.delTag(operateDTO.getId().longValue());
    }
}
