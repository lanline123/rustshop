package com.tigshop.adminapi.controller.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigUpdateDTO;
import com.tigshop.bean.vo.setting.LogisticsCompanyConfigVO;
import com.tigshop.service.setting.LogisticsCompanyConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminapi/setting/logisticsCompanyConfig")
@Tag(name = "物流公司配置", description = "物流公司配置（租户绑定）功能")
@PreAuthorize("@pms.hasPermission('logisticsCompanyConfigManage','posOrder','shopLogisticsCompanyConfigManage','vendorLogisticsCompanyConfigManage')")
public class LogisticsCompanyConfigController {

    public static final String[] CONFIG_UPDATE_FIELDS = {"is_enabled", "sort_order"};

    @Resource
    private LogisticsCompanyConfigService logisticsCompanyConfigService;

    @GetMapping("/list")
    @Operation(summary = "获取当前租户物流公司配置列表")
    public Page<LogisticsCompanyConfigVO> list(LogisticsCompanyConfigListDTO listDTO) {
        return logisticsCompanyConfigService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取配置详情")
    public LogisticsCompanyConfigVO detail(@RequestParam Integer id) {
        return logisticsCompanyConfigService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "绑定物流公司")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyConfigEditManage')")
    public void create(@RequestBody LogisticsCompanyConfigCreateDTO createDTO) {
        logisticsCompanyConfigService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新物流公司配置")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyConfigEditManage')")
    public void update(@RequestBody LogisticsCompanyConfigUpdateDTO updateDTO) {
        logisticsCompanyConfigService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "解绑物流公司")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyConfigEditManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        logisticsCompanyConfigService.del(operateDTO.getId());
    }

    @PostMapping("/setDefault")
    @Operation(summary = "设置默认物流公司")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyConfigEditManage')")
    public void setDefault(@RequestParam Integer id) {
        logisticsCompanyConfigService.setDefault(id);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段（启用/禁用等）")
    @PreAuthorize("@pms.hasPermission('logisticsCompanyConfigEditManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        logisticsCompanyConfigService.updateField(updateField, CONFIG_UPDATE_FIELDS);
    }
}
