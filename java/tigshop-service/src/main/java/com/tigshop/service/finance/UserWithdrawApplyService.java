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
import com.tigshop.bean.dto.finance.UserWithdrawApplyCreateDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyListDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyUpdateDTO;
import com.tigshop.bean.model.finance.UserWithdrawApply;
import com.tigshop.bean.vo.finance.UserWithdrawApplyVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
 * 提现申请服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserWithdrawApplyService extends BaseService<UserWithdrawApply> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<UserWithdrawApplyVO> list(UserWithdrawApplyListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    UserWithdrawApplyVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(UserWithdrawApplyCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     */
    void update(UserWithdrawApplyUpdateDTO updateDTO);

    /**
     * 获取提现金额
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    BigDecimal getUserWithdrawTotal(String startTime, String endTime);
}
