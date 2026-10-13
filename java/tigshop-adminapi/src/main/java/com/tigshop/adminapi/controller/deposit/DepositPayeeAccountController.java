package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.deposit.DepositPayeeAccountDTO;
import com.tigshop.bean.dto.deposit.DepositPayeeAccountListDTO;
import com.tigshop.bean.vo.deposit.DepositPayeeAccountVO;
import com.tigshop.service.deposit.DepositPayeeAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台保证金收款账户控制器（平台端）。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/payeeAccount")
@Tag(name = "保证金收款账户", description = "平台保证金收款账户管理")
@PreAuthorize("@pms.hasPermission('depositPayeeAccountManage')")
public class DepositPayeeAccountController {

    @Resource
    private DepositPayeeAccountService depositPayeeAccountService;

    @GetMapping("/list")
    @Operation(summary = "获取收款账户分页列表")
    public Page<DepositPayeeAccountVO> list(DepositPayeeAccountListDTO listDTO) {
        return depositPayeeAccountService.list(listDTO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建收款账户")
    @PreAuthorize("@pms.hasPermission('depositPayeeAccountUpdate')")
    public void create(@RequestBody DepositPayeeAccountDTO dto) {
        depositPayeeAccountService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "更新收款账户")
    @PreAuthorize("@pms.hasPermission('depositPayeeAccountUpdate')")
    public void update(@RequestBody DepositPayeeAccountDTO dto) {
        depositPayeeAccountService.update(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除收款账户")
    @PreAuthorize("@pms.hasPermission('depositPayeeAccountUpdate')")
    public void del(@RequestBody OperateDTO operateDTO) {
        depositPayeeAccountService.del(operateDTO.getId());
    }
}
