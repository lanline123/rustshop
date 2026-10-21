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
import com.tigshop.bean.query.salesman.CustomerTransactionPageQuery;
import com.tigshop.bean.vo.salesman.CustomerTransactionOrderVO;
import com.tigshop.service.salesman.SalesmanOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tigshop团队
 * @create 2025/6/19 10:16
 */
@RestController
@RequestMapping("/adminapi/salesman/customerTransaction")
@Tag(name = "客户成交", description = "客户成交")
public class SalesmanCustomerTransactionController {

    @Resource
    SalesmanOrderService salesmanOrderService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<CustomerTransactionOrderVO> listCustomerTransaction(CustomerTransactionPageQuery customerTransactionPageQuery) {
        return salesmanOrderService.listCustomerTransaction(customerTransactionPageQuery);
    }
}
