package com.tigshop.service.user.impl;

import com.tigshop.bean.model.user.UserRankLog;
import com.tigshop.mapper.user.UserRankLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserRankLogService;
import org.springframework.stereotype.Service;

/**
 * 用户等级变动配置
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Service
public class UserRankLongServiceImpl extends BaseServiceImpl<UserRankLogMapper, UserRankLog> implements UserRankLogService{
}
