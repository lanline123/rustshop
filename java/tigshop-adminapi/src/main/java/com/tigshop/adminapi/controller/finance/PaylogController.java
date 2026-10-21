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
import com.tigshop.bean.dto.finance.PaylogCreateDTO;
import com.tigshop.bean.query.finance.PaylogListPageQuery;
import com.tigshop.bean.dto.finance.PaylogUpdateDTO;
import com.tigshop.bean.vo.finance.PaylogVO;
import com.tigshop.service.finance.PaylogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 交易日志控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/payLog")
@Tag(name = "交易日志", description = "交易日志功能")
public class PaylogController {
    @Resource
    private PaylogService paylogService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PaylogVO> list(PaylogListPageQuery pageQuery) {
        return paylogService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public PaylogVO detail(@RequestParam Integer id) {
        return paylogService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@RequestBody PaylogCreateDTO createDTO) {
        paylogService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@RequestBody PaylogUpdateDTO updateDTO) {
        paylogService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('payLogBatchManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        paylogService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        paylogService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('payLogBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        paylogService.batch(batchDTO);
    }
}
