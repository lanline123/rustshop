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

package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserWithDrawApplyDTO;
import com.tigshop.bean.query.finance.userwithdrawapply.UserWithdrawApplyListQuery;
import com.tigshop.bean.dto.finance.UserWithdrawApplyDelDTO;
import com.tigshop.bean.dto.finance.WithdrawApplyAccountUpdateDTO;
import com.tigshop.bean.model.finance.WithdrawApplyAccount;
import com.tigshop.bean.param.finance.withdrawapply.WithdrawApplyAccountCreateParam;
import com.tigshop.bean.vo.finance.WithdrawApplyAccountVO;
import com.tigshop.service.common.BaseService;

/**
 * 会员提现账号服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface WithdrawApplyAccountService extends BaseService<WithdrawApplyAccount> {

    /**
     * 列表
     */
    Page<WithdrawApplyAccountVO> list(UserWithdrawApplyListQuery query);

    /**
     * 详情
     *
     * @param accountId 账号id
     * @param userId userId
     * @return ItemVO
     */
    WithdrawApplyAccountVO detail(Integer accountId, Integer userId);

    /**
     * 创建
     */
    void create(WithdrawApplyAccountCreateParam param);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(WithdrawApplyAccountUpdateDTO updateDTO , Integer userId);

    /**
     * 删除提现账号
     * @param dto dto
     * @param userId 用户id
     * @return boolean
     */
    boolean delAccount(UserWithdrawApplyDelDTO dto, Integer userId);

    /**
     * 提现申请
     * @param dto dto
     * @param userId 用户id
     * @return boolean
     */
    boolean apply(UserWithDrawApplyDTO dto, Integer userId);

    /**
     * 佣金提现申请（D1107，从佣金账户扣减）
     * @param dto dto
     * @param userId 用户id
     * @return boolean
     */
    boolean applyCommission(UserWithDrawApplyDTO dto, Integer userId);
}
