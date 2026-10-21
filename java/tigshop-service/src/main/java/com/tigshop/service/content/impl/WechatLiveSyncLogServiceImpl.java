package com.tigshop.service.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.content.WechatLiveSyncLog;
import com.tigshop.bean.query.content.WechatLiveSyncLogPageQuery;
import com.tigshop.mapper.content.WechatLiveSyncLogMapper;
import com.tigshop.service.content.WechatLiveSyncLogService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 微信直播同步日志服务实现
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Service
public class WechatLiveSyncLogServiceImpl extends ServiceImpl<WechatLiveSyncLogMapper, WechatLiveSyncLog> implements WechatLiveSyncLogService {

    @Override
    public Page<WechatLiveSyncLog> page(WechatLiveSyncLogPageQuery pageQuery) {
        Page<WechatLiveSyncLog> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<WechatLiveSyncLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(pageQuery.getOrderId() != null, WechatLiveSyncLog::getOrderId, pageQuery.getOrderId())
                .eq(pageQuery.getAftersaleId() != null, WechatLiveSyncLog::getAftersaleId, pageQuery.getAftersaleId())
                .eq(pageQuery.getRefundId() != null, WechatLiveSyncLog::getRefundId, pageQuery.getRefundId())
                .eq(pageQuery.getRoomId() != null, WechatLiveSyncLog::getRoomId, pageQuery.getRoomId())
                .eq(pageQuery.getSyncStatus() != null, WechatLiveSyncLog::getSyncStatus, pageQuery.getSyncStatus())
                .eq(pageQuery.getDirection() != null && !pageQuery.getDirection().isEmpty(), WechatLiveSyncLog::getDirection, pageQuery.getDirection())
                .eq(pageQuery.getEventType() != null && !pageQuery.getEventType().isEmpty(), WechatLiveSyncLog::getEventType, pageQuery.getEventType())
                .orderByDesc(WechatLiveSyncLog::getId);
        return this.page(page, queryWrapper);
    }

    @Override
    public List<WechatLiveSyncLog> listLatestByOrderIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<WechatLiveSyncLog> logs = this.list(new LambdaQueryWrapper<WechatLiveSyncLog>()
                .in(WechatLiveSyncLog::getOrderId, orderIds)
                .orderByDesc(WechatLiveSyncLog::getId));
        Map<Integer, WechatLiveSyncLog> latestMap = new LinkedHashMap<>();
        for (WechatLiveSyncLog log : logs) {
            latestMap.putIfAbsent(log.getOrderId(), log);
        }
        return orderIds.stream()
                .map(latestMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
