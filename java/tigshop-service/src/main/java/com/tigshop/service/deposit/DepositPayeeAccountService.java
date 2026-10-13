package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositPayeeAccountDTO;
import com.tigshop.bean.dto.deposit.DepositPayeeAccountListDTO;
import com.tigshop.bean.vo.deposit.DepositPayeeAccountVO;

import java.util.List;

/**
 * 平台保证金收款账户服务。
 *
 * <p>独立表 shop_deposit_payee_account 存储，平台级收款账户（微信/支付宝/银行卡），
 * 供保证金充值核对使用。</p>
 *
 * @author Tigshop团队
 */
public interface DepositPayeeAccountService {

    /**
     * 平台端收款账户分页列表。
     */
    Page<DepositPayeeAccountVO> list(DepositPayeeAccountListDTO listDTO);

    /**
     * 启用状态收款账户列表（店铺/供应商端充值选择用）。
     *
     * @param accountType 账户类型 1银行卡 2支付宝 3微信；为空=全部
     * @param keyword     关键词（户名/账号模糊）；为空=不筛选
     */
    List<DepositPayeeAccountVO> listEnabled(Integer accountType, String keyword);

    /**
     * 收款账户详情。
     */
    DepositPayeeAccountVO detail(Integer id);

    /**
     * 新增收款账户。
     */
    void create(DepositPayeeAccountDTO dto);

    /**
     * 更新收款账户。
     */
    void update(DepositPayeeAccountDTO dto);

    /**
     * 删除收款账户。
     */
    boolean del(Integer id);
}
