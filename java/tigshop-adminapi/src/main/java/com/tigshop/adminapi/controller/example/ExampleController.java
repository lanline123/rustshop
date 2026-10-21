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
package com.tigshop.adminapi.controller.example;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.example.ExampleCreateDTO;
import com.tigshop.bean.dto.example.ExampleListDTO;
import com.tigshop.bean.dto.example.ExampleUpdateDTO;
import com.tigshop.bean.vo.example.ExampleVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.example.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 示例控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/example/example")
@Tag(name = "示例", description = "示例功能")
@Validated
@PreAuthorize("@pms.hasPermission('exampleManage')")
public class ExampleController {
    @Resource
    private ExampleService exampleService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    @PreAuthorize("@pms.hasPermission('exampleList')")
    public Page<ExampleVO> list(ExampleListDTO listDTO) {
        return exampleService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    @PreAuthorize("@pms.hasPermission('exampleDetail')")
    public ExampleVO detail(@RequestParam Integer id) {
        return exampleService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('exampleCreate')")
    public void create(@Valid @RequestBody ExampleCreateDTO createDTO) {
        exampleService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('exampleUpdate')")
    public void update(@Valid @RequestBody ExampleUpdateDTO updateDTO) {
        exampleService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('exampleDel')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        exampleService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('exampleUpdateField')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        exampleService.updateField(updateField, CheckFieldConstants.GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('exampleBatch')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        exampleService.batch(batchDTO);
    }
}
