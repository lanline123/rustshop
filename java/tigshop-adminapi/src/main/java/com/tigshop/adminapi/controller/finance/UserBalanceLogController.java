// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserBalanceLogListDTO;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.service.finance.UserBalanceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 示例控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/userBalanceLog")
@Tag(name = "用户余额日志", description = "用户余额日志")
public class UserBalanceLogController {
    @Resource
    private UserBalanceLogService userBalanceLogService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<UserBalanceLogVO> list(UserBalanceLogListDTO listDTO) {
        return userBalanceLogService.list(listDTO);
    }

}
