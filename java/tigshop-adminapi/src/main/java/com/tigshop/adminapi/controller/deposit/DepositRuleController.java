package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.deposit.DepositRuleDTO;
import com.tigshop.bean.dto.deposit.DepositRuleListDTO;
import com.tigshop.bean.dto.deposit.DepositRuleStatusDTO;
import com.tigshop.bean.vo.deposit.DepositRuleVO;
import com.tigshop.service.deposit.DepositRuleService;
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
 * 店铺保证金规则控制器（平台端）。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/rule")
@Tag(name = "保证金规则", description = "店铺保证金规则管理")
@PreAuthorize("@pms.hasPermission('depositRuleManage')")
public class DepositRuleController {

    @Resource
    private DepositRuleService depositRuleService;

    @GetMapping("/list")
    @Operation(summary = "获取规则列表")
    public Page<DepositRuleVO> list(DepositRuleListDTO listDTO) {
        return depositRuleService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取规则详情")
    public DepositRuleVO detail(Integer id) {
        return depositRuleService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建规则")
    @PreAuthorize("@pms.hasPermission('depositRuleUpdate')")
    public void create(@RequestBody DepositRuleDTO dto) {
        depositRuleService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "更新规则")
    @PreAuthorize("@pms.hasPermission('depositRuleUpdate')")
    public void update(@RequestBody DepositRuleDTO dto) {
        depositRuleService.update(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除规则")
    @PreAuthorize("@pms.hasPermission('depositRuleUpdate')")
    public void del(@RequestBody OperateDTO operateDTO) {
        depositRuleService.del(operateDTO.getId());
    }

    @PostMapping("/status")
    @Operation(summary = "启用/停用规则")
    @PreAuthorize("@pms.hasPermission('depositRuleUpdate')")
    public void status(@RequestBody DepositRuleStatusDTO dto) {
        depositRuleService.changeStatus(dto);
    }
}
