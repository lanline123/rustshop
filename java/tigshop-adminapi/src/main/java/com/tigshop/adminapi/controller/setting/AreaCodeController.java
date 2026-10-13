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
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.settings.areacode.AreaCodeEditParam;
import com.tigshop.bean.param.settings.areacode.AreaCodeSaveParam;
import com.tigshop.bean.dto.setting.AreaCodeListDTO;
import com.tigshop.bean.vo.setting.AreaCodeVO;
import com.tigshop.service.setting.AreaCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.AREA_CODE_FIELDS;

/**
 * 区号管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月31日 09:51
 */
@RestController
@RequestMapping("/adminapi/setting/areaCode")
@Tag(name = "区号管理", description = "区号管理")
@PreAuthorize("@pms.hasPermission('areaCodeManage')")
@Validated
public class AreaCodeController {

    @Resource
    private AreaCodeService areaCodeService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<AreaCodeVO> list(AreaCodeListDTO dto) {
        return areaCodeService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public AreaCodeVO detail(@RequestParam Integer id) {
        return areaCodeService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('areaCodeModifyManage')")
    public void create(@Valid @RequestBody AreaCodeSaveParam param) {
        areaCodeService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('areaCodeModifyManage')")
    public void update(@Valid @RequestBody AreaCodeEditParam param) {
        areaCodeService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('areaCodeModifyManage')")
    public void del(@RequestBody OperateDTO operate) {
        areaCodeService.del(operate.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('areaCodeModifyManage')")
    public boolean updateField(@RequestBody @Validated UpdateFieldDTO dto) {
        return areaCodeService.updateField(dto, AREA_CODE_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('areaCodeModifyManage')")
    public void batch(@RequestBody @Validated BatchDTO dto) {
        areaCodeService.batch(dto);
    }
}
