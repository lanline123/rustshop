package com.tigshop.aspect;

import cn.hutool.core.date.DateUtil;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.common.annotation.Log;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.authority.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 日志记录切面
 * @author Tigshop
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Resource
    private AdminLogService adminLogService;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(com.tigshop.common.annotation.Log)")
    public void logPointCut() {
    }

    @AfterReturning("logPointCut()")
    public void saveLog(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log log = method.getAnnotation(Log.class);

            if (log != null) {
                // 构造 SpEL 上下文
                String[] paramNames = nameDiscoverer.getParameterNames(method);
                Object[] args = joinPoint.getArgs();
                EvaluationContext context = new StandardEvaluationContext();
                if (paramNames != null) {
                    for (int i = 0; i < paramNames.length; i++) {
                        context.setVariable(paramNames[i], args[i]);
                    }
                }

                // 解析表达式
                String logInfoRaw = log.value();
                String logInfo;
                if (logInfoRaw.contains("#{")) {
                    Expression exp = parser.parseExpression(logInfoRaw, new TemplateParserContext());
                    logInfo = exp.getValue(context, String.class);
                } else {
                    logInfo = logInfoRaw;
                }
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                Object requestLogId = 0;
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    requestLogId = request.getAttribute("REQUEST_LOG_ID");
                }
                // 记录日志
                AdminLog adminLog = new AdminLog();
                adminLog.setLogTime(DateUtil.currentSeconds());
                adminLog.setIpAddress(IpUtils.getIpAddr());
                adminLog.setUserId(SecurityUtils.getCurrentAdminId());
                adminLog.setLogInfo(logInfo);
                adminLog.setRequestLogId((int)requestLogId);
                adminLogService.save(adminLog);
            }
        } catch (Exception e) {
            log.error("日志记录异常：", e);
        }
    }
}