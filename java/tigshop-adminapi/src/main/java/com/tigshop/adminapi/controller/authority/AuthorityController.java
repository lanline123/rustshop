// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.authority;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AuthorityDTO;
import com.tigshop.bean.dto.authority.AuthorityListDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.model.authority.Authority;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.authority.AuthorityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.AUTHORITY_FIELDS;
import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 权限
 *
 * @author Jayce
 * @create 2024年10月18日 11:01
 */
@RestController
@RequestMapping("/adminapi/authority/authority")
@Tag(name = "权限")
@Validated
public class AuthorityController {

    @Resource
    private AuthorityService authorityService;

    @Resource
    private MessageHelper messageHelper;

    @GetMapping("/getAllAuthority")
    @Operation(summary = "获取所有权限")
    public List<Tree<Integer>> getAllAuthority(@RequestParam(value = "type", required = false) Integer type,
                                               @RequestParam(value = "adminType", required = false) String adminType) {
        return authorityService.getAllAuthority(type, adminType);
    }

    @GetMapping("/getAuthority")
    @Operation(summary = "获取所有权限")
    public List<Authority> getAuthority() {
        return authorityService.getAuthority();
    }

    @GetMapping("/list")
    @Operation(summary = "获取权限列表")
    public Page<AuthorityDTO> list(AuthorityListDTO authority) {
        return authorityService.list(authority);
    }

    @GetMapping("/getAuthorityParentName")
    @Operation(summary = "获取父级权限名称")
    public String getAuthorityParentName(@RequestParam("id") @NotNull(message = ID_CANNOT_IS_NULL) Integer id) {
        return authorityService.getAuthorityParentName(id);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('authorityBatchManage')")
    public void batch(@RequestBody BatchDTO batch) {
        if ("del".equals(batch.getType())) {
            authorityService.batchDel(batch.getIds());
        } else {
            authorityService.batch(batch);
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建权限")
    @PreAuthorize("@pms.hasPermission('authorityUpdateManage')")
    public void create(@RequestBody AuthorityDTO authority) {
        authorityService.create(authority);
    }

    @PostMapping("/del")
    @Operation(summary = "删除权限")
    @PreAuthorize("@pms.hasPermission('authorityDelManage')")
    public void del(@RequestBody OperateDTO operate) {
        // 不能有子级
        Authority one = authorityService.lambdaQuery().eq(Authority::getParentId, operate.getId()).last("limit 1").one();
        if (one != null) {
            throw new GlobalException(messageHelper.get("error.authority.has.children"));
        }
        authorityService.del(operate.getId());
    }

    @GetMapping("/detail")
    @Operation(summary = "获取权限详情")
    public AuthorityDTO detail(@RequestParam("id") @NotNull(message = ID_CANNOT_IS_NULL) Integer id) {
        return authorityService.detail(id);
    }

    @PostMapping("/update")
    @Operation(summary = "更新权限")
    @PreAuthorize("@pms.hasPermission('authorityUpdateManage')")
    public void update(@RequestBody AuthorityDTO authority) {
        authorityService.update(authority);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新权限字段")
    @PreAuthorize("@pms.hasPermission('authorityUpdateManage')")
    public void updateField(@RequestBody @Valid UpdateFieldDTO updateField) {
        authorityService.updateField(updateField, AUTHORITY_FIELDS);
    }
}
