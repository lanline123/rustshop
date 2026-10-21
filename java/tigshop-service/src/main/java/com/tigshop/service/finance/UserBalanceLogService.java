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

package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserBalanceLogCreatDTO;
import com.tigshop.bean.dto.finance.UserBalanceLogListDTO;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
 * 示例服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserBalanceLogService extends BaseService<UserBalanceLog> {
    /**
     * 列表
     */
    Page<UserBalanceLogVO> list(UserBalanceLogListDTO listDTO);

    /**
     * 创建
     * @param userBalanceLog 用户余额日志
     * @return boolean
     */
    boolean create(UserBalanceLogCreatDTO userBalanceLog);

    /**
     * 获取用户余额日志总
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return BigDecimal
     */
    BigDecimal getUserBalanceLogTotal(String startTime, String endTime);

    /**
     * 获取用户冻结余额日志总
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return BigDecimal
     */
    BigDecimal getUserFrozenBalanceLogTotal(String startTime, String endTime);

    /**
     * 变更资金记录
     * @param userBalanceLog 变更资金记录参数
     * @return boolean
     */
    boolean changesInFunds(UserBalanceLog userBalanceLog);

    /**
     * 变更用户 balance  和 frozen_balance
     * @param userId
     * @param log
     */
    void accountChange(Integer userId, UserBalanceLog log);
}
