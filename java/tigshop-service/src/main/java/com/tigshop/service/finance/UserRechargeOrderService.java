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
import com.tigshop.bean.dto.finance.*;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.bean.vo.finance.ClientUserRechargeOrderVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderPayVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
 * 充值记录服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserRechargeOrderService extends BaseService<UserRechargeOrder> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<UserRechargeOrderVO> list(UserRechargeOrderListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    UserRechargeOrderVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(UserRechargeOrderCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(UserRechargeOrderUpdateDTO updateDTO);

    /**
     * 获取用户余额总额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return BigDecimal
     */
    BigDecimal getUserRechargeSurplusTotal(String startTime, String endTime);

    /**
     * 获取用户申请记录
     */
    Page<ClientUserRechargeOrderVO> clientList(UserRechargeOrderListDTO listDTO);

    /**
     * 用户充值操作
     */
    Integer rechargeOperation(ClientUserRechargeOrderUpdateDTO updateDTO);

    /**
     * 用户充值支付
     * @param payDTO 参数
     * @return UserRechargeOrderPayVO
     */
    UserRechargeOrderPayVO pay(ClientUserRechargeOrderPayDTO payDTO);

    /**
     * 前台用户充值支付详情，按订单和当前用户共同查询。
     */
    UserRechargeOrderPayVO pay(ClientUserRechargeOrderPayDTO payDTO, Integer userId);

    /**
     * 获取充值支付状态
     * @param id 主键
     * @return Integer
     */
    Integer checkStatus(Integer id, Integer userId);

    /**
     * 后台/内部检查充值订单状态
     *
     * @param id 主键
     * @return 状态
     */
    Integer checkStatus(Integer id);

    /**
     * 创建充值支付
     * @param payDTO 参数
     * @param userId 用户ID
     * @return Map
     */
    PayCreateVO createPay(UserRechargeOrderPayDTO payDTO, Integer userId);
}
