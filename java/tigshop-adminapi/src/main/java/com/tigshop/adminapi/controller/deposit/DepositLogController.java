package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositLogListDTO;
import com.tigshop.bean.vo.deposit.DepositLogVO;
import com.tigshop.service.deposit.DepositLogAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台端保证金全局交易明细控制器。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/log")
@Tag(name = "店铺保证金交易明细", description = "平台端保证金全局交易明细")
@PreAuthorize("@pms.hasPermission('depositLogManage')")
public class DepositLogController {

    @Resource
    private DepositLogAdminService depositLogAdminService;

    @GetMapping("/list")
    @Operation(summary = "全局交易明细分页")
    public Page<DepositLogVO> list(DepositLogListDTO listDTO) {
        return depositLogAdminService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "交易明细详情")
    public DepositLogVO detail(Integer id) {
        return depositLogAdminService.detail(id);
    }
}
