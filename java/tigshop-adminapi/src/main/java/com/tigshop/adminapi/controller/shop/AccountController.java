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
package com.tigshop.adminapi.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.AccountListDTO;
import com.tigshop.bean.vo.shop.AccountVO;
import com.tigshop.bean.vo.shop.ShopAccountLogVO;
import com.tigshop.service.shop.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺资金变化控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/shopAccount")
@Tag(name = "店铺资金变化", description = "店铺资金变化功能")
public class AccountController {
    @Resource
    private AccountService accountService;

    @GetMapping("/index")
    @Operation(summary = "获取统计数据")
    public AccountVO index(AccountListDTO listDTO) {
        return accountService.index(listDTO);
    }

    @GetMapping("logList")
    @Operation(summary = "获取资金日志列表")
    public Page<ShopAccountLogVO> logList(AccountListDTO listDTO) {
        return accountService.list(listDTO);
    }
}
