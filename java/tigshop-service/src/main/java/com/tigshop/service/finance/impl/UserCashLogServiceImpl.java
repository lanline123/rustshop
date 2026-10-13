package com.tigshop.service.finance.impl;

import com.tigshop.bean.model.finance.UserCashLog;
import com.tigshop.mapper.finance.UserCashLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserCashLogService;
import org.springframework.stereotype.Service;

@Service
public class UserCashLogServiceImpl extends BaseServiceImpl<UserCashLogMapper, UserCashLog> implements UserCashLogService {
}
