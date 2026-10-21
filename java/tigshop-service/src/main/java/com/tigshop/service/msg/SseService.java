package com.tigshop.service.msg;

import com.tigshop.bean.vo.msg.AdminMsgCountVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Tigshop项目组
 * @create 2025年10月15日 13:49
 */
public interface SseService {
    SseEmitter msgCount(Long startTime);
}
