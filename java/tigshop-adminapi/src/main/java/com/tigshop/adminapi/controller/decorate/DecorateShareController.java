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
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.decorate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.DecorateShareCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateShareListDTO;
import com.tigshop.bean.dto.decorate.DecorateShareUpdateDTO;
import com.tigshop.bean.vo.decorate.DecorateShareVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.decorate.DecorateShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 售后申请表控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/decorate/decorateShare")
@Tag(name = "装修导入导出模块", description = "装修导入导出模块")
@Validated
public class DecorateShareController {
    @Resource
    private DecorateShareService decorateShareService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<DecorateShareVO> list(DecorateShareListDTO listDTO) {
        return decorateShareService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public DecorateShareVO detail(@RequestParam Integer id) {
        return decorateShareService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void create(@Valid @RequestBody DecorateShareCreateDTO createDTO) {
        decorateShareService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void update(@Valid @RequestBody DecorateShareUpdateDTO updateDTO) {
        decorateShareService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        decorateShareService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        decorateShareService.updateField(updateField, new String[]{});
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('decorateShareBatch')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        decorateShareService.batch(batchDTO);
    }

    @GetMapping("/share")
    @Operation(summary = "分享")
    public DecorateShareVO share(@RequestParam(value = "decorateId") int decorateId) {
        return decorateShareService.share(decorateId);
    }

    @GetMapping("/import")
    @Operation(summary = "导入")
    public void importShare(@RequestParam(required = false) String url) {
        if (url == null || url.isEmpty()) {
            throw new GlobalException("请输入要导入的链接!");
        }
        decorateShareService.importUrl(url);
    }

}
