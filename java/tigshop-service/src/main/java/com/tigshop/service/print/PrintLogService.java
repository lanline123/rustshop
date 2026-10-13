// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.print.adapter.PushResult;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintLog;
import com.tigshop.bean.query.print.PrintLogPageQuery;
import com.tigshop.bean.vo.print.PrintLogPageVO;
import com.tigshop.service.common.BaseService;

/**
 * 打印日志服务
 * <p>
 * 记录每次推送打印任务的全生命周期，状态机详见 {@code PrintLogStatusEnum}。
 * 所有状态转移通过带条件的 {@code UPDATE} 保证合法性和幂等性。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
public interface PrintLogService extends BaseService<PrintLog> {

    /**
     * 创建一条状态为 PENDING(0) 的日志
     *
     * @param print       打印机（不会修改，仅读字段）
     * @param orderId     订单 ID
     * @param triggerType 触发方式（1 自动 / 2 手动）
     * @return 持久化后的日志（含 logId）
     */
    PrintLog createPending(Print print, Integer orderId, Integer triggerType);

    /**
     * 按推送结果更新为 PUSHED(1) / PUSH_FAILED(3) / OFFLINE(4)
     *
     * @param logId  日志 ID
     * @param result 推送结果
     */
    void markPushed(Long logId, PushResult result);

    /**
     * 标记推送过程抛异常，状态置 PUSH_FAILED(3)
     *
     * @param logId 日志 ID
     * @param ex    异常
     */
    void markFailed(Long logId, Throwable ex);

    /**
     * 回调上报，只在当前状态 = PUSHED(1) 时允许转移到 PRINTED(2) / OFFLINE(4)
     * <p>
     * DR-01=α 下本方法在 MVP 阶段不会被调用；保留契约供后续扩展（DR-01=β / γ）。
     *
     * @param taskNo 平台任务号（对商米即 order_id）
     * @param status 目标状态（2 或 4）
     * @param rawPayload 原始回调报文
     * @return 是否成功转移（已是终态则返回 false，幂等）
     */
    boolean markCallback(String taskNo, Integer status, String rawPayload);

    /**
     * 按 task_no 查日志（走 idx_task_no 索引）
     */
    PrintLog findByTaskNo(String taskNo);

    /**
     * 分页查询（按 shop_id 隔离）
     */
    Page<PrintLogPageVO> page(PrintLogPageQuery query);
}
