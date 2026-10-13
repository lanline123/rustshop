package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserWithDrawApplyDTO;
import com.tigshop.bean.query.finance.userwithdrawapply.UserWithdrawApplyListQuery;
import com.tigshop.bean.dto.finance.UserWithdrawApplyDelDTO;
import com.tigshop.bean.dto.finance.WithdrawApplyAccountUpdateDTO;
import com.tigshop.bean.param.finance.withdrawapply.WithdrawApplyAccountCreateParam;
import com.tigshop.bean.vo.finance.WithdrawApplyAccountVO;
import com.tigshop.service.finance.WithdrawApplyAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 提现明细
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/withdrawApply"))
@Tag(name = "前台用户提现")
public class WithDrawApplyClientController {

    @Resource
    private WithdrawApplyAccountService withdrawApplyAccountService;

    @GetMapping("/list")
    @Operation(summary = "列表")
    public Page<WithdrawApplyAccountVO> list(UserWithdrawApplyListQuery query){
        return withdrawApplyAccountService.list(query);
    }

    @PostMapping("/createAccount")
    @Operation(summary = "创建提现账号")
    public void createAccount(@RequestBody @Validated WithdrawApplyAccountCreateParam param){
        withdrawApplyAccountService.create(param);
    }

    @PostMapping("/updateAccount")
    @Operation(summary = "修改提现账号")
    public void updateAccount(@RequestBody WithdrawApplyAccountUpdateDTO updateDTO){
        withdrawApplyAccountService.update(updateDTO, getCurrentUserId());
    }

    @GetMapping("/accountDetail")
    @Operation(summary = "提现账号详情")
    public WithdrawApplyAccountVO accountDetail(@RequestParam(value = "accountId") Integer accountId){
        return withdrawApplyAccountService.detail(accountId, getCurrentUserId());
    }

    @PostMapping("/delAccount")
    @Operation(summary = "删除提现账号")
    public void delAccount(@RequestBody UserWithdrawApplyDelDTO dto){
        withdrawApplyAccountService.delAccount(dto, getCurrentUserId());
    }

    @PostMapping("/apply")
    @Operation(summary = "提现申请")
    public void apply(@RequestBody @Validated UserWithDrawApplyDTO dto){
        withdrawApplyAccountService.apply(dto, getCurrentUserId());
    }

    @PostMapping("/applyCommission")
    @Operation(summary = "佣金提现申请（D1107）")
    public void applyCommission(@RequestBody @Validated UserWithDrawApplyDTO dto){
        withdrawApplyAccountService.applyCommission(dto, getCurrentUserId());
    }
}
