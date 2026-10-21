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

package com.tigshop.service.msg;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.msg.AdminMsgListDTO;
import com.tigshop.bean.model.msg.AdminMsg;
import com.tigshop.bean.vo.msg.AdminMsgBroadcastVO;
import com.tigshop.bean.vo.msg.AdminMsgTypeListVO;
import com.tigshop.bean.vo.msg.AdminMsgVO;
import com.tigshop.bean.vo.msg.HomeMsgSummaryVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 管理员消息服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface AdminMsgService extends BaseService<AdminMsg> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<AdminMsgVO> list(AdminMsgListDTO listDTO);

    /**
     * 语音播报内容专用列表。
     *
     * <p>复用 {@link #list(AdminMsgListDTO)} 的身份/店铺/供应商/是否播报过滤口径，
     * 在此基础上按当前页批量聚合订单/售后的商品摘要与金额，一次性返回，
     * 使前端无需逐条拉取订单/售后详情。不改变消息中心 list 的返回结构与行为。</p>
     *
     * @param listDTO 查询参数（分页 + 可选 msgType）
     * @return 带商品摘要的分页结果
     */
    Page<AdminMsgBroadcastVO> broadcastList(AdminMsgListDTO listDTO);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     */
    void createMessage(AdminMsgCreateDTO createDTO);

    /**
     * 设置已读
     *
     * @param msgId id
     * @return boolean
     */
    boolean setReaded(Integer msgId);

    /**
     * 设置所有已读
     *
     * @return boolean
     */
    boolean setAllReaded();

    /**
     * 获取未读消息数量
     *
     * @return Long
     */
    Long getUnreadAdminMsgCount(String adminType, Integer shopId, Integer vendorId);

    /**
     * 统计自 startTime 起、按消息类型新增的后台消息数量。
     * 过滤口径与消息中心列表/未读统计一致（非供应商：shopId 且 vendorId=0；供应商：vendorId），
     * 使语音播报的增量判断与消息中心展示的消息保持事件级一致。
     *
     * @param adminType 管理员类型
     * @param shopId    店铺ID
     * @param vendorId  供应商ID
     * @param startTime 起始发送时间（秒），为空或 <=0 时不限制
     * @param msgTypes  需要统计的消息类型集合
     * @return msgType -> 数量
     */
    Map<Integer, Long> getNewMsgCountByType(String adminType, Integer shopId, Integer vendorId, Long startTime, List<Integer> msgTypes);

    /**
     * 获取消息类型列表
     *
     * @return List<AdminMsgTypeListVO>
     */
    List<AdminMsgTypeListVO> getAdminMsgTypeList();

    /**
     * 首页消息摘要：未读总数 + 最新一条消息。
     * 数据范围与 msgTypeArr / list 一致（按当前登录身份 + 店铺/供应商隔离）。
     *
     * @return HomeMsgSummaryVO
     */
    HomeMsgSummaryVO getHomeSummary();

}
