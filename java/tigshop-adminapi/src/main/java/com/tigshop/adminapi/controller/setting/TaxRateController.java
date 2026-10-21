// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.setting.TaxRateDTO;
import com.tigshop.bean.query.setting.TaxRatePageQuery;
import com.tigshop.bean.vo.setting.TaxRateVO;
import com.tigshop.service.setting.TaxRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 税率管理控制器
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
@RestController
@Tag(name = "税率管理")
@Validated
@RequestMapping("/adminapi/setting/taxRate")
@PreAuthorize("@pms.hasPermission('taxRateManage')")
public class TaxRateController {

    @Resource
    private TaxRateService taxRateService;

    @GetMapping("/list")
    @Operation(summary = "税率列表")
    public Page<TaxRateVO> list(TaxRatePageQuery query) {
        return taxRateService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "税率详情")
    public TaxRateVO detail(@RequestParam("taxRateId") Integer taxRateId) {
        return taxRateService.detail(taxRateId);
    }

    @PostMapping("/create")
    @Operation(summary = "新增税率")
    @PreAuthorize("@pms.hasPermission('taxRateUpdateManage') or @pms.hasPermission('taxRateModifyManage')")
    public void create(@Valid @RequestBody TaxRateDTO dto) {
        taxRateService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "更新税率")
    @PreAuthorize("@pms.hasPermission('taxRateUpdateManage') or @pms.hasPermission('taxRateModifyManage')")
    public void update(@Valid @RequestBody TaxRateDTO dto) {
        taxRateService.update(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除税率")
    @PreAuthorize("@pms.hasPermission('taxRateUpdateManage') or @pms.hasPermission('taxRateModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        taxRateService.del(operateDTO.getId());
    }
}
