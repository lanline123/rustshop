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

import com.tigshop.bean.vo.salesman.SalesmanUserDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.query.salesman.CustomerApiPageQuery;
import com.tigshop.bean.param.salesman.CustomerRemarkUpdateParam;
import com.tigshop.bean.vo.salesman.CustomerDetailVO;
import com.tigshop.bean.vo.salesman.CustomerListVO;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * 分销员控制器
 *
 * @author kidd
 * @create 2025/6/23
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/salesman/salesman")
@Tag(name = "分销员控制器", description = "分销员控制器")
public class SalesmanController {

    private final SalesmanService salesmanService;

    private final SalesmanConfigService salesmanConfigService;

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public SalesmanUserDetailVO detail() {
        return salesmanService.detailForApi();
    }

    @GetMapping("/customerList")
    @Operation(summary = "获取我的客户列表")
    public Page<CustomerListVO> customerList(CustomerApiPageQuery pageQuery) {
        Salesman salesman = currentSalesman();
        assertCustomerInfoViewAllowed();
        pageQuery.setSalesmanId(salesman.getSalesmanId());
        pageQuery.setShopId(HeaderUtils.getShopId());
        return salesmanService.customerApiList(pageQuery);
    }

    @GetMapping("/customerDetail")
    @Operation(summary = "获取我的客户详情")
    public CustomerDetailVO customerDetail(@RequestParam("userId") Integer userId,
                                           @RequestParam(value = "startTime", required = false) String startTime,
                                           @RequestParam(value = "endTime", required = false) String endTime) {
        Salesman salesman = currentSalesman();
        assertCustomerInfoViewAllowed();
        return salesmanService.customerApiDetail(salesman.getSalesmanId(), userId,
                startTime, endTime, HeaderUtils.getShopId());
    }

    @PostMapping("/customerRemark")
    @Operation(summary = "更新客户备注")
    public void customerRemark(@Valid @RequestBody CustomerRemarkUpdateParam param) {
        Salesman salesman = currentSalesman();
        assertCustomerInfoViewAllowed();
        salesmanService.updateCustomerRemark(salesman.getSalesmanId(), param);
    }

    private void assertCustomerInfoViewAllowed() {
        SalesmanConfigVO config = salesmanConfigService.detail(SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode());
        if (config == null || !Integer.valueOf(1).equals(config.getViewCustomerInfo())) {
            throw new GlobalException("暂无权限查看客户信息");
        }
    }

    private Salesman currentSalesman() {
        Salesman salesman = salesmanService.getOne(new LambdaQueryWrapper<Salesman>()
                .eq(Salesman::getUserId, SecurityUtils.getCurrentUserId()));
        if (salesman == null) {
            throw new GlobalException("当前用户不是分销员");
        }
        return salesman;
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取分销员用户基础信息")
    public UserBaseVO getUserInfo(@RequestParam(name = "salesmanId") Integer salesmanId) {
        return salesmanService.getSalesman(salesmanId).getBaseUserInfo();
    }

}
