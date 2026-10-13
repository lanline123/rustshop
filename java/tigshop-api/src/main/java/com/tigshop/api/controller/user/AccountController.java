package com.tigshop.api.controller.user;

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

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 账户明细
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/account"))
@Tag(name = "前台用户账户信息")
public class AccountController {
    @Resource
    UserBalanceLogService userBalanceLogService;
    @GetMapping("/list")
    @Operation(summary = "账户信息列表")
    public Page<UserBalanceLogVO> list(UserBalanceLogListDTO listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        listDTO.setBalance(1);
        // 余额账户明细（D1107，wallet_type=0）。老客户历史佣金归属余额，故不再按文案排除“结算佣金”
        listDTO.setWalletType(0);
        return userBalanceLogService.list(listDTO);
    }

    @GetMapping("/commissionList")
    @Operation(summary = "佣金账户明细列表（D1107）")
    public Page<UserBalanceLogVO> commissionList(UserBalanceLogListDTO listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        // 佣金账户明细（wallet_type=1）
        listDTO.setWalletType(1);
        // 与会员余额明细一致：仅展示金额>0的收支，过滤掉「佣金提现冻结/解冻」等金额为0的冻结流水（D1107 bug 0d20c092）
        listDTO.setBalance(1);
        return userBalanceLogService.list(listDTO);
    }
}
