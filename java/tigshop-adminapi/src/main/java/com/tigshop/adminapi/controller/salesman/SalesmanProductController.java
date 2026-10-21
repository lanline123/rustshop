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
import com.tigshop.bean.dto.salesman.SalesmanProductAnalysisDTO;
import com.tigshop.bean.param.salesman.SalesmanProductEditParam;
import com.tigshop.bean.enums.salesman.SalesmanProductType;
import com.tigshop.bean.param.salesman.SalesmanProductSaveParam;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.bean.vo.salesman.SalesmanProductVO;
import com.tigshop.service.salesman.SalesmanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分销商品控制器
 *
 * @author kidd
 * @since 2025/6/23
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/salesman/product")
@Tag(name = "分销商品", description = "分销商品功能")
public class SalesmanProductController {

    private final SalesmanProductService salesmanProductService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<SalesmanProductListVO> list(SalesmanProductPageQuery pageQuery) {
        return salesmanProductService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public SalesmanProductVO detail(@RequestParam("id") Integer id) {
        return salesmanProductService.detailByProductId(id);
    }

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('salesmanProductModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated SalesmanProductSaveParam param) {
        salesmanProductService.create(param);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanProductModifyManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Validated SalesmanProductEditParam param) {
        salesmanProductService.update(param);
    }

    @Operation(summary = "配置")
    @GetMapping("/config")
    public Map<Integer, String> config() {
        return SalesmanProductType.getAll();
    }

    @Operation(summary = "商品成交分析")
    @GetMapping("/analysis")
    public Page<Map<String, Object>> analysis(SalesmanProductAnalysisDTO analysisDTO) {
        return salesmanProductService.analysis(analysisDTO);
    }

}
