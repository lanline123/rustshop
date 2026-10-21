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
import com.tigshop.bean.query.salesman.SalesmanContentPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanContentVO;
import com.tigshop.service.salesman.SalesmanContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分销内容管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/salesman/content")
@Tag(name = "分销内容管理", description = "分销内容管理功能")
public class SalesmanContentController {
    @Resource
    private SalesmanContentService salesmanContentService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanContentVO> list(SalesmanContentPageQuery query) {
        query.setFrom("user");
        query.setSortField("is_top");
        return salesmanContentService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SalesmanContentVO detail(@RequestParam Integer id) {
        return salesmanContentService.detail(id);
    }

}
