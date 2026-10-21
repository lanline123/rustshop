// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.shop;

import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.shop.Account;
import org.apache.ibatis.annotations.Insert;

/**
 * 店铺资金变化映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface AccountMapper extends BaseMapper<Account> {
    /** 唯一事件键保证并发回调幂等，其他数据库错误仍向上抛出。 */
    @Insert("""
            INSERT INTO shop_account_log
                (add_time, shop_money, frozen_money, type, remarks, new_shop_money,
                 new_frozen_money, shop_id, is_new, balance_type, pay_channel, change_amount, event_key)
            VALUES (#{addTime}, 0, 0, 5, #{remarks}, NULL, 0, #{shopId}, 1, 2,
                    #{payChannel}, #{changeAmount}, #{eventKey})
            ON DUPLICATE KEY UPDATE event_key = event_key
            """)
    int insertOnlineEvent(Account account);
}
