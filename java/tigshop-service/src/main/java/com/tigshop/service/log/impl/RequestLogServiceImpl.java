package com.tigshop.service.log.impl;

import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.log.RequestLogService;
import com.tigshop.mapper.log.RequestLogMapper;
import com.tigshop.bean.model.log.RequestLog;
import org.springframework.stereotype.Service;

/**
 * 请求日志服务实现类
 * @author Tigshop
 */
@Service
public class RequestLogServiceImpl extends BaseServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {
}
