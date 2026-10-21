package com.tigshop.service.user.impl;
/**
 * 户成长积分表实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
import com.tigshop.bean.model.user.UserGrowthPointsLog;
import com.tigshop.mapper.user.UserGrowthPointsLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserGrowthPointsLogService;
import org.springframework.stereotype.Service;

@Service
public class UserGrowthPointsLogServiceImpl extends BaseServiceImpl<UserGrowthPointsLogMapper, UserGrowthPointsLog> implements UserGrowthPointsLogService {
}
