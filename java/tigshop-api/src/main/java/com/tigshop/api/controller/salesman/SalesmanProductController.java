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
package com.tigshop.api.controller.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.salesman.SalesmanProductType;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanProductDetailVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.salesman.SalesmanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分销商品控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/salesman/product")
@Tag(name = "分销商品", description = "分销商品功能")
public class SalesmanProductController {
    @Resource
    private SalesmanProductService salesmanProductService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanProductListVO> list(SalesmanProductPageQuery pageQuery) {
        pageQuery.setStatus(1);
        pageQuery.setUserId(SecurityUtils.getCurrentUserId());
        pageQuery.setSalesmanProductType(SalesmanProductType.JOIN.getCode());

        return salesmanProductService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "客户端分销商品详情")
    public SalesmanProductDetailVO detail(@RequestParam("productId") Integer productId) {
        return salesmanProductService.detailClient(productId);
    }

}
