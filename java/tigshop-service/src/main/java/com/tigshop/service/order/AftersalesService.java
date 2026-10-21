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

package com.tigshop.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.order.AftersalesEditParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.order.AfterSalesClientDetailVO;
import com.tigshop.bean.vo.order.AftersalesVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Set;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
public interface AftersalesService extends BaseService<Aftersales> {
    /**
     * 列表
     */
    Page<AftersalesVO> list(AftersalesListPageQuery pageQuery);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    AftersalesVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(AftersalesApplyParam createDTO);

    /**
     * 更新
     */
    void update(AftersalesEditParam param);

    /**
     * 接收
     * @param id 主键
     * @return boolean
     */
    boolean receive(Integer id);

    /**
     * 商家/供应商拒收
     * <p>在「商家待收货(5)/供应商待收货(22)」节点触发：不发起退款，货品由买卖双方线下协商处理。
     * 状态置为已拒绝(REFUSE=3) 并写入拒收理由（reply，买家前台作为拒绝原因展示），
     * 商家后续可通过 complete 关闭为已取消。</p>
     *
     * @param id    售后ID
     * @param reply 拒收理由
     */
    void rejectReceive(Integer id, String reply);

    /**
     * 自动同意退货退款售后
     *
     * @param aftersalesId 售后ID
     */
    void autoApproveReturnAftersales(Integer aftersalesId);

    /**
     * 自动确认退货收货
     *
     * @param id 售后ID
     * @return 是否处理成功
     */
    boolean autoReceive(Integer id);

    /**
     * 提交反馈记录
     * @param aftersaleId 售后id
     * @param aftersalesRecordCreateDTO 售后参数
     * @param currentAdminId 当前管理员id
     * @return boolean
     */
    boolean submitFeedbackRecord(Integer aftersaleId, AftersalesRecordCreateDTO aftersalesRecordCreateDTO, Integer currentAdminId);

    /**
     * 售后申请
     */
    void afterSalesApply(AftersalesApplyParam param);

    /**
     * 售后申请修改
     */
    void afterSalesApplyUpdate(ClientAfterSalesUpdateDTO updateDTO);

    /**
     * 售后详情
     * @param id 主键
     * @param userId 当前用户ID，用于校验权限
     * @return AfterSalesClientDetailVO
     */
    AfterSalesClientDetailVO clientDetail(Integer id, Integer userId);

    /**
     * 提交售后反馈记录
     * @param dto 参数
     * @return boolean
     */
    boolean submitFeedbackRecord(AfterSalesFeedbackDTO dto);

    /**
     * 取消售后申请
     * @param dto 取消参数
     * @return boolean
     */
    boolean cancel(AfterSalesCancelDTO dto);

    /**
     * 完结售后
     * @param completeDTO
     * @return
     */
    void complete(AfterSalesCompleteDTO completeDTO);

    /**
     * 获取新增售后申请数量
     * （按 addTime &gt;= startTime 过滤，按管理员维度筛选）
     * <p>
     * 注：本计数用于语音播报的增量判断，必须单调递增，故不叠加售后状态过滤，
     * 否则审核后计数回落会导致后续新申请漏播。
     *
     * @param adminType 管理员类型（区分供应商和店铺维度）
     * @param shopId    店铺 ID
     * @param vendorId  供应商 ID
     * @param startTime 起始时间（毫秒级 epoch）
     * @return 数量
     */
    Long getNewAftersaleCount(String adminType, Integer shopId, Integer vendorId, Long startTime);

    /**
     * 订单是否存在进行中的整单售后：订单全部商品被有效售后覆盖，且订单尚未因退款完成而取消。
     * 已发货订单在售后审核通过后仍保持"已发货"，此期间订单不能再被确认收货流转为已完成。
     *
     * @param orderId 订单ID
     * @return boolean
     */
    boolean hasInProgressFullOrderAftersales(Integer orderId);

    /**
     * 订单全部商品是否已被"已完成"的售后覆盖，即整单售后已退款完成。
     * 用于退款完成时收口订单状态，避免因运费不退导致退款金额与订单总额不相等而判不出整单退款。
     *
     * @param orderId 订单ID
     * @return boolean
     */
    boolean isFullOrderAftersalesCompleted(Integer orderId);

    /**
     * 批量判定这批售后单中哪些是「已完成(6)但平台财务退款尚未处理完成」的，一次查询避免逐行取退款申请。
     * 订单侧据此在财务退款到账前仍把订单视为售后中，口径与售后详情 / 列表的状态文案保持一致。
     *
     * @param aftersalesList 售后单
     * @return 待平台财务退款的售后ID
     */
    Set<Integer> findPendingFinanceRefundIds(List<Aftersales> aftersalesList);
}
