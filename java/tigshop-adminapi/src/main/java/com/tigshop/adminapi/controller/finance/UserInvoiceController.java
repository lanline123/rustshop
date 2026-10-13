// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.finance.UserInvoiceCreateDTO;
import com.tigshop.bean.dto.finance.UserInvoiceListDTO;
import com.tigshop.bean.dto.finance.UserInvoiceUpdateDTO;
import com.tigshop.bean.vo.finance.UserInvoiceVO;
import com.tigshop.service.finance.UserInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 发票资质控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/userInvoice")
@Tag(name = "发票资质", description = "发票资质功能")
public class UserInvoiceController {
    @Resource
    private UserInvoiceService userInvoiceService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<UserInvoiceVO> list(UserInvoiceListDTO listDTO) {
        return userInvoiceService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public UserInvoiceVO detail(@RequestParam Integer id) {
        return userInvoiceService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@RequestBody UserInvoiceCreateDTO createDTO) {
        userInvoiceService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('userInvoiceUpdateManage')")
    public void update(@RequestBody UserInvoiceUpdateDTO updateDTO) {
        userInvoiceService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('userInvoiceDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        userInvoiceService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        userInvoiceService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('userInvoiceBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        userInvoiceService.batch(batchDTO);
    }
}
