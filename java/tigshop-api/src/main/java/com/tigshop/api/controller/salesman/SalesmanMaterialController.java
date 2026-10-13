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
import com.tigshop.bean.model.salesman.SalesmanMaterialCategory;
import com.tigshop.bean.query.salesman.SalesmanMaterialApiPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialVO;
import com.tigshop.service.salesman.SalesmanMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分销员分组控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/salesman/material")
@Tag(name = "分销员分组", description = "分销员分组功能")
public class SalesmanMaterialController {

    private final SalesmanMaterialService salesmanMaterialService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanMaterialVO> apiPage(SalesmanMaterialApiPageQuery pageQuery) {
        return salesmanMaterialService.apiPage(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SalesmanMaterialVO detail(@RequestParam Integer id) {
        return salesmanMaterialService.detail(id);
    }

    @GetMapping("/category")
    @Operation(summary = "获取分类")
    public List<SalesmanMaterialCategory> category() {
        return salesmanMaterialService.getAllCategory();
    }
}
