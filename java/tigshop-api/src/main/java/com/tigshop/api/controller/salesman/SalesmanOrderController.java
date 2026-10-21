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

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.query.salesman.SalesmanOrderPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanOrderVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.salesman.SalesmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分销业绩订单结算控制器
 *
 * @author kidd
 * @create 2025/6/23
 */
@Tag(name = "分销业绩订单结算", description = "分销业绩订单结算功能")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/salesman/order")
public class SalesmanOrderController {

    private final SalesmanOrderService salesmanOrderService;

    private final SalesmanService salesmanService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanOrderVO> clientList(SalesmanOrderPageQuery pageQuery, HttpServletResponse response) {
        Salesman one = salesmanService.getOne(new LambdaUpdateWrapper<Salesman>().eq(Salesman::getUserId, SecurityUtils.getCurrentUserId()));
        pageQuery.setSalesmanId(one.getSalesmanId());
        return salesmanOrderService.list(pageQuery, response);
    }

}
