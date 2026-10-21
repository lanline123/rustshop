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
package com.tigshop.adminapi.controller.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.salesman.SalesmanOrderSettlementDTO;
import com.tigshop.bean.query.salesman.SalesmanOrderPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanOrderVO;
import com.tigshop.service.salesman.SalesmanOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 分销业绩订单结算控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/salesman/order")
@Tag(name = "分销业绩订单结算", description = "分销业绩订单结算功能")
@Validated
public class SalesmanOrderController {
    @Resource
    private SalesmanOrderService salesmanOrderService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanOrderVO> list(SalesmanOrderPageQuery pageQuery, HttpServletResponse response) {
        return salesmanOrderService.list(pageQuery, response);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('salesmanOrderBatch')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        salesmanOrderService.batch(batchDTO);
    }

    @PostMapping("/settlement")
    @Operation(summary = "人工结算")
    @PreAuthorize("@pms.hasPermission('salesmanSettlementManage')")
    public void settlement(@Valid @RequestBody SalesmanOrderSettlementDTO dto) {
        salesmanOrderService.manualSettle(dto.getIds());
    }
}
