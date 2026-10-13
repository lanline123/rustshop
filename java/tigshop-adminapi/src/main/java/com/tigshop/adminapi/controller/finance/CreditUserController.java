package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.finance.CreditUserCreateDTO;
import com.tigshop.bean.dto.finance.CreditUserListDTO;
import com.tigshop.bean.dto.finance.CreditUserUpdateDTO;
import com.tigshop.bean.vo.finance.CreditUserListVO;
import com.tigshop.bean.vo.finance.CreditUserVO;
import com.tigshop.service.finance.CreditUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.CREDIT_USER_FIELDS;

/**
 * 授信会员管理控制器
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@RestController
@RequestMapping("/adminapi/credit/user")
@Tag(name = "授信会员管理", description = "授信会员管理功能")
@Validated
@PreAuthorize("@pms.hasPermission('termMemberManage')")
public class CreditUserController {

    @Resource
    private CreditUserService creditUserService;

    @GetMapping("/list")
    @Operation(summary = "授信会员列表")
    @PreAuthorize("@pms.hasPermission('termMemberManage')")
    public Page<CreditUserListVO> list(CreditUserListDTO listDTO) {
        return creditUserService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "授信会员详情")
    @PreAuthorize("@pms.hasPermission('termMemberManage')")
    public CreditUserVO detail(@RequestParam Integer creditUserId) {
        return creditUserService.detail(creditUserId);
    }

    @PostMapping("/create")
    @Operation(summary = "添加授信会员")
    @PreAuthorize("@pms.hasPermission('termMemberModifyManage')")
    public void create(@Valid @RequestBody CreditUserCreateDTO createDTO) {
        creditUserService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "修改授信会员")
    @PreAuthorize("@pms.hasPermission('termMemberModifyManage')")
    public void update(@Valid @RequestBody CreditUserUpdateDTO updateDTO) {
        creditUserService.update(updateDTO);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除授信会员")
    @PreAuthorize("@pms.hasPermission('termMemberModifyManage')")
    public void delete(@Valid @RequestBody OperateDTO operateDTO) {
        creditUserService.delete(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新授信会员字段（用于启用/禁用等）")
    @PreAuthorize("@pms.hasPermission('termMemberModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateFieldDTO) {
        creditUserService.updateField(updateFieldDTO, CREDIT_USER_FIELDS);
    }
}
