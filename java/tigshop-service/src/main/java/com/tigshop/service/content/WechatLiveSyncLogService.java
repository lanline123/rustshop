package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.content.WechatLiveSyncLog;
import com.tigshop.bean.query.content.WechatLiveSyncLogPageQuery;

import java.util.List;

/**
 * 微信直播同步日志服务
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
public interface WechatLiveSyncLogService extends IService<WechatLiveSyncLog> {

    Page<WechatLiveSyncLog> page(WechatLiveSyncLogPageQuery pageQuery);

    List<WechatLiveSyncLog> listLatestByOrderIds(List<Integer> orderIds);
}
