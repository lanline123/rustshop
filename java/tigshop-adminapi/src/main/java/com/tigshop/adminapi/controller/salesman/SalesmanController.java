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
import com.tigshop.bean.query.salesman.SalesmanRankingPageQuery;
import com.tigshop.bean.query.salesman.SalesmanStatisticalDetailPageQuery;
import com.tigshop.bean.param.salesman.SalesmanEditParam;
import com.tigshop.bean.param.salesman.SalesmanSaveParam;
import com.tigshop.bean.query.salesman.CustomerListPageQuery;
import com.tigshop.bean.query.salesman.SalesmanPageQuery;
import com.tigshop.bean.vo.salesman.*;
import com.tigshop.service.salesman.SalesmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 分销员列表控制器
 *
 * @author kidd
 * @since 2025/6/21
 */
@Tag(name = "分销员列表控制器", description = "分销员列表控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/salesman/salesman")
public class SalesmanController {

    private final SalesmanService salesmanService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<SalesmanPageVO> list(SalesmanPageQuery pageQuery) {
        return salesmanService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public SalesmanDetailVO detail(@RequestParam @NotNull(message = "id不能为空") Integer id) {
        return salesmanService.detail(id);
    }

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('salesmanModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated SalesmanSaveParam param) {
        salesmanService.create(param);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanModifyManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Validated SalesmanEditParam param) {
        salesmanService.update(param);
    }

    @GetMapping("/statisticalDetails")
    @Operation(summary = "分销员明细")
    public Page<SalesmanStatisticalVO> statisticalDetails(SalesmanStatisticalDetailPageQuery pageQuery, HttpServletResponse response) {
        return salesmanService.statistical(pageQuery, response);
    }

    @GetMapping("/commissionDetails")
    @Operation(summary = "分销员佣金详情")
    public CommissionDetailsVO commissionDetails(@RequestParam("salesmanId") Integer salesmanId) {
        return salesmanService.commissionDetails(salesmanId);
    }

    @GetMapping("/ranking")
    @Operation(summary = "分销员排行")
    public Page<SalesmanRankingVO> ranking(SalesmanRankingPageQuery pageQuery) {
        return salesmanService.ranking(pageQuery);
    }

    @GetMapping("/salesmanList")
    @Operation(summary = "分销员下拉列表")
    public Page<SalesmanSelectVO> salesmanList(String nickname) {
        return salesmanService.salesmanList(nickname);
    }

    @GetMapping("/customerList")
    @Operation(summary = "客户数量接口")
    public Page<CustomerListVO> customerList(CustomerListPageQuery pageQuery) {
        return salesmanService.customerList(pageQuery);
    }
}
