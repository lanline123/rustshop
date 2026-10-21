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
package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.finance.RefundLogCreateDTO;
import com.tigshop.bean.dto.finance.RefundLogListDTO;
import com.tigshop.bean.dto.finance.RefundLogUpdateDTO;
import com.tigshop.bean.vo.finance.RefundLogVO;
import com.tigshop.service.finance.RefundLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 退款记录控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/refundLog")
@Tag(name = "退款记录", description = "退款记录功能")
public class RefundLogController {
    @Resource
    private RefundLogService refundLogService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<RefundLogVO> list(RefundLogListDTO listDTO) {
        return refundLogService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public RefundLogVO detail(@RequestParam Integer id) {
        return refundLogService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@RequestBody RefundLogCreateDTO createDTO) {
        refundLogService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@RequestBody RefundLogUpdateDTO updateDTO) {
        refundLogService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@RequestBody OperateDTO operateDTO) {
        refundLogService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        refundLogService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@RequestBody BatchDTO batchDTO) {
        refundLogService.batch(batchDTO);
    }
}
