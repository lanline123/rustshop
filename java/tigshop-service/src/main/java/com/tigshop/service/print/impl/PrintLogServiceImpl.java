// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.print.adapter.PushResult;
import com.tigshop.bean.enums.print.PrintLogStatusEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintLog;
import com.tigshop.bean.query.print.PrintLogPageQuery;
import com.tigshop.bean.vo.print.PrintLogPageVO;
import com.tigshop.mapper.print.PrintLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.print.PrintLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 打印日志服务实现
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrintLogServiceImpl extends BaseServiceImpl<PrintLogMapper, PrintLog> implements PrintLogService {

    /**
     * push_response / callback_payload 字段最大存储长度，超长时截断
     */
    private static final int MAX_RAW_LEN = 4096;

    @Override
    public PrintLog createPending(Print print, Integer orderId, Integer triggerType) {
        int now = (int) (System.currentTimeMillis() / 1000);
        PrintLog log = new PrintLog();
        log.setShopId(print.getShopId());
        log.setPrintId(print.getPrintId());
        log.setPlatform(print.getPlatform());
        log.setOrderId(orderId);
        log.setPrintSn(print.getPrintSn());
        log.setTriggerType(triggerType);
        log.setStatus(PrintLogStatusEnum.PENDING.getCode());
        log.setRetryCount(0);
        log.setAddTime(now);
        log.setUpdateTime(now);
        save(log);
        return log;
    }

    @Override
    public void markPushed(Long logId, PushResult result) {
        if (logId == null) {
            return;
        }
        int now = (int) (System.currentTimeMillis() / 1000);
        Integer status = result == null || result.getMappedStatus() == null
                ? PrintLogStatusEnum.PUSH_FAILED.getCode()
                : result.getMappedStatus();

        LambdaUpdateWrapper<PrintLog> wrapper = new LambdaUpdateWrapper<PrintLog>()
                .eq(PrintLog::getLogId, logId)
                .set(PrintLog::getStatus, status)
                .set(PrintLog::getPushedTime, now)
                .set(PrintLog::getUpdateTime, now);

        if (result != null) {
            wrapper.set(PrintLog::getErrorCode, result.getErrorCode())
                    .set(PrintLog::getErrorMsg, result.getErrorMsg())
                    .set(PrintLog::getTaskNo, result.getTaskNo())
                    .set(PrintLog::getPushResponse, truncate(result.getRawResponse()));
        }
        update(wrapper);
    }

    @Override
    public void markFailed(Long logId, Throwable ex) {
        if (logId == null) {
            return;
        }
        int now = (int) (System.currentTimeMillis() / 1000);
        String msg = ex == null ? "未知异常" : StrUtil.sub(String.valueOf(ex.getMessage()), 0, 500);
        update(new LambdaUpdateWrapper<PrintLog>()
                .eq(PrintLog::getLogId, logId)
                .set(PrintLog::getStatus, PrintLogStatusEnum.PUSH_FAILED.getCode())
                .set(PrintLog::getPushedTime, now)
                .set(PrintLog::getUpdateTime, now)
                .set(PrintLog::getErrorMsg, msg));
    }

    @Override
    public boolean markCallback(String taskNo, Integer status, String rawPayload) {
        if (StrUtil.isBlank(taskNo) || status == null) {
            return false;
        }
        int now = (int) (System.currentTimeMillis() / 1000);
        // 只允许 PUSHED(1) 向 PRINTED(2) / OFFLINE(4) 迁移；已是终态则幂等返回 false
        LambdaUpdateWrapper<PrintLog> wrapper = new LambdaUpdateWrapper<PrintLog>()
                .eq(PrintLog::getTaskNo, taskNo)
                .eq(PrintLog::getStatus, PrintLogStatusEnum.PUSHED.getCode())
                .set(PrintLog::getStatus, status)
                .set(PrintLog::getCallbackPayload, truncate(rawPayload))
                .set(PrintLog::getUpdateTime, now);
        if (PrintLogStatusEnum.PRINTED.getCode().equals(status)) {
            wrapper.set(PrintLog::getPrintedTime, now);
        }
        return update(wrapper);
    }

    @Override
    public PrintLog findByTaskNo(String taskNo) {
        if (StrUtil.isBlank(taskNo)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<PrintLog>()
                .eq(PrintLog::getTaskNo, taskNo)
                .last("limit 1"));
    }

    @Override
    public Page<PrintLogPageVO> page(PrintLogPageQuery query) {
        Page<PrintLog> page = buildSortOrder(query);
        LambdaQueryWrapper<PrintLog> wrapper = new LambdaQueryWrapper<PrintLog>()
                .eq(PrintLog::getShopId, getShopId());
        if (query.getPlatform() != null) {
            wrapper.eq(PrintLog::getPlatform, query.getPlatform());
        }
        if (query.getStatus() != null) {
            wrapper.eq(PrintLog::getStatus, query.getStatus());
        }
        if (query.getTriggerType() != null) {
            wrapper.eq(PrintLog::getTriggerType, query.getTriggerType());
        }
        if (query.getOrderId() != null) {
            wrapper.eq(PrintLog::getOrderId, query.getOrderId());
        }
        if (StrUtil.isNotBlank(query.getPrintSn())) {
            wrapper.like(PrintLog::getPrintSn, query.getPrintSn());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(PrintLog::getAddTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(PrintLog::getAddTime, query.getEndTime());
        }
        // 默认按 addTime 降序
        if (query.getSortField() == null || query.getSortField().isEmpty()) {
            wrapper.orderByDesc(PrintLog::getAddTime);
        }

        Page<PrintLog> data = this.page(page, wrapper);
        Page<PrintLogPageVO> result = new Page<>(data.getCurrent(), data.getSize(), data.getTotal());
        result.setRecords(data.getRecords().stream().map(PrintLogPageVO::new).toList());
        return result;
    }

    private static String truncate(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.length() > MAX_RAW_LEN ? raw.substring(0, MAX_RAW_LEN) : raw;
    }
}
