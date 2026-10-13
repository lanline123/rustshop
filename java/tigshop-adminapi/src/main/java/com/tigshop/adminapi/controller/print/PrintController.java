// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.print;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.print.PrintAddParam;
import com.tigshop.bean.param.print.PrintUpdateParam;
import com.tigshop.bean.query.print.PrintPageQuery;
import com.tigshop.bean.vo.print.PrintDetailVO;
import com.tigshop.bean.vo.print.PrintPageVO;
import com.tigshop.service.print.PrintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.PRINT_FIELDS;

/**
 * @author Tigshop团队
 * @create 2025/7/22 10:29
 */
@RestController
@RequestMapping("/adminapi/print/print")
@Tag(name = "打印机管理", description = "打印机管理")
@RequiredArgsConstructor
public class PrintController {

    private final PrintService printService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('baseReceiptManage','shopBaseReceiptManage','posReceiptPrint')")
    public Page<PrintPageVO> list(PrintPageQuery pageQuery) {
        return printService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    @PreAuthorize("@pms.hasPermission('baseReceiptManage','shopBaseReceiptManage','posReceiptPrint')")
    public PrintDetailVO detail(@RequestParam @NotNull(message = "id不能为空") Integer id) {
        return printService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "新增打印机")
    @PreAuthorize("@pms.hasPermission('baseReceiptUpdateManage','shopBaseReceiptUpdateManage','posReceiptPrintUpdateManage')")
    public Boolean create(@RequestBody @Valid PrintAddParam printAddParam) {
        return printService.create(printAddParam);
    }

    @PostMapping("/update")
    @Operation(summary = "编辑打印机")
    @PreAuthorize("@pms.hasPermission('baseReceiptUpdateManage','shopBaseReceiptUpdateManage','posReceiptPrintUpdateManage')")
    public Boolean update(@RequestBody @Valid PrintUpdateParam printUpdateParam) {
        return printService.update(printUpdateParam);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('baseReceiptUpdateManage','shopBaseReceiptUpdateManage','posReceiptPrintUpdateManage')")
    public Boolean updateField(@RequestBody @Valid UpdateFieldDTO updateFieldDTO) {
        return printService.updateField(updateFieldDTO, PRINT_FIELDS);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('baseReceiptUpdateManage','shopBaseReceiptUpdateManage','posReceiptPrintUpdateManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        printService.del(operateDTO.getId());
    }

    @PostMapping("/hasEnabled")
    @Operation(summary = "是否有启用的打印机")
    public boolean hasEnabled() {
        return printService.hasEnabled();
    }

    @GetMapping("/devicePrinters")
    @Operation(summary = "查询收银台设备可用打印机")
    public List<PrintPageVO> devicePrinters(@RequestParam(required = false) String deviceCode) {
        return printService.devicePrinters(deviceCode);
    }
}
