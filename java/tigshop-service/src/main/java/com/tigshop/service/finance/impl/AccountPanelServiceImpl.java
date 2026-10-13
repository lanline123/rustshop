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

package com.tigshop.service.finance.impl;

import com.tigshop.bean.dto.finance.AccountPanelDTO;
import com.tigshop.bean.vo.finance.AccountPanelVO;
import com.tigshop.service.finance.AccountPanelService;
import com.tigshop.service.finance.UserBalanceLogService;
import com.tigshop.service.finance.UserRechargeOrderService;
import com.tigshop.service.finance.UserWithdrawApplyService;
import com.tigshop.service.order.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 账户总览服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AccountPanelServiceImpl implements AccountPanelService {

    @Resource
    private UserBalanceLogService userBalanceLogService;

    @Resource
    private UserRechargeOrderService userRechargeOrderService;

    @Resource
    private UserWithdrawApplyService userWithdrawApplyService;

    @Resource
    private OrderService orderService;

    @Override
    public AccountPanelVO list(AccountPanelDTO listDTO) {

        AccountPanelVO accountPanelVO = new AccountPanelVO();

        // 从 listDTO 中提取开始时间和结束时间
        String startTime = listDTO.getSearchStartDate();
        String endTime = listDTO.getSearchEndDate();

        // 获取用户余额变动总和，传入开始时间和结束时间
        accountPanelVO.setBalance( userBalanceLogService.getUserBalanceLogTotal(startTime, endTime));
        accountPanelVO.setFrozenMoney( userBalanceLogService.getUserFrozenBalanceLogTotal(startTime, endTime));
        // 充值金额
        accountPanelVO.setVoucherAmount( userRechargeOrderService.getUserRechargeSurplusTotal(startTime, endTime));
        // 提现金额
        accountPanelVO.setToCashAmount( userWithdrawApplyService.getUserWithdrawTotal(startTime, endTime));
        // 交易使用余额
        accountPanelVO.setSurplus( orderService.getOrderBalance(startTime, endTime, "balance"));
        // 交易使用积分
        accountPanelVO.setUsePoints( orderService.getOrderBalance(startTime, endTime, "use_points"));
        return accountPanelVO;

    }

}
