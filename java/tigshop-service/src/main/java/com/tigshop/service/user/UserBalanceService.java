// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 */
public interface UserBalanceService {

    /**
     * 冻结余额
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean incFrozenBalance(BigDecimal amount, Integer userId,String changeDesc);

    /**
     * 解冻余额
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean decFrozenBalance(BigDecimal amount, Integer userId,String changeDesc);

    /**
     * 增加余额
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean incBalance(BigDecimal amount, Integer userId,String changeDesc);

    /**
     * 减少余额
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean decBalance(BigDecimal amount, Integer userId,String changeDesc);

    /**
     * 增加佣金账户余额（D1107，分销结算专用入账）
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean incCommissionBalance(BigDecimal amount, Integer userId, String changeDesc);

    /**
     * 减少佣金账户余额（D1107）
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean decCommissionBalance(BigDecimal amount, Integer userId, String changeDesc);

    /**
     * 增加佣金账户冻结余额（D1107，提现冻结）
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean incFrozenCommissionBalance(BigDecimal amount, Integer userId, String changeDesc);

    /**
     * 减少佣金账户冻结余额（D1107）
     * @param amount 金额
     * @param userId 用户ID
     * @param changeDesc 变动描述
     */
    Boolean decFrozenCommissionBalance(BigDecimal amount, Integer userId, String changeDesc);

}
