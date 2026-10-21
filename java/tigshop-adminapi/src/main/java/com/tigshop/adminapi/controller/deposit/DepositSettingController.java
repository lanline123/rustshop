package com.tigshop.adminapi.controller.deposit;

import com.tigshop.bean.dto.deposit.DepositSettingDTO;
import com.tigshop.bean.vo.deposit.DepositSettingVO;
import com.tigshop.service.deposit.DepositSettingService;
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
 * 店铺保证金设置控制器（平台端）。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/setting")
@Tag(name = "保证金设置", description = "店铺保证金全局设置")
@PreAuthorize("@pms.hasPermission('depositSettingManage')")
public class DepositSettingController {

    @Resource
    private DepositSettingService depositSettingService;

    @GetMapping("/detail")
    @Operation(summary = "获取保证金设置")
    public DepositSettingVO detail() {
        return depositSettingService.detail();
    }

    @PostMapping("/save")
    @Operation(summary = "保存保证金设置")
    @PreAuthorize("@pms.hasPermission('depositSettingSave')")
    public void save(@RequestBody DepositSettingDTO dto) {
        depositSettingService.save(dto);
    }
}
