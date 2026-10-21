package com.tigshop.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tigshop.bean.model.log.RequestLog;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.log.RequestLogMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.Set;

/**
 * @author Tigshop
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLogAspect {

    private static final ObjectMapper AUDIT_MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "pwd", "code", "captcha", "authorization", "cookie", "setcookie",
            "privatekey", "apikey", "accesskey", "credential", "credentials",
            "idcard", "idcardno", "idcardnumber", "bankcard", "bankcardno", "bankcardnumber",
            "smscode", "verifycode", "verificationcode", "validatecode", "authcode");

    private final RequestLogMapper requestLogMapper;
    private final TigshopProperties tigshopProperties;

    /**
     * 定义切点：拦截所有带 @PostMapping 注解的方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMappingMethods() {}

    /**
     * 环绕通知：在方法执行前后进行处理
     */
    @Around("postMappingMethods()")
    public Object logPostRequests(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求相关信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 请求的 URL
        String url = request.getRequestURI();
        // HTTP 请求方法 (一般就是 POST)
        String httpMethod = request.getMethod();
        if (!url.startsWith("/adminapi") || !"POST".equalsIgnoreCase(httpMethod)) {
            return joinPoint.proceed();
        }

        // 类名 + 方法名
        String classMethod = joinPoint.getSignature().toShortString();

        String requestParams = "";
        try {
            ArrayNode safeParams = AUDIT_MAPPER.createArrayNode();
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] names = signature.getParameterNames();
            Parameter[] parameters = signature.getMethod().getParameters();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse
                        || arg instanceof MultipartFile || arg instanceof InputStream || arg instanceof OutputStream) {
                    continue;
                }
                RequestParam binding = parameters[i].getAnnotation(RequestParam.class);
                String name = names == null ? parameters[i].getName() : names[i];
                boolean sensitive = isSensitiveField(name) || (binding != null
                        && (isSensitiveField(binding.name()) || isSensitiveField(binding.value())));
                safeParams.add(sensitive ? AUDIT_MAPPER.getNodeFactory().textNode("***")
                        : sanitizeAuditValue(AUDIT_MAPPER.valueToTree(arg)));
            }
            requestParams = AUDIT_MAPPER.writeValueAsString(safeParams);
        } catch (Exception e) {
            // 序列化异常文本也可能包含敏感值，不输出异常原文。
            log.warn("请求参数摘要生成失败，已省略参数");
            requestParams = "{\"omitted\":true}";
        }

        // 构造日志对象
        RequestLog logRecord = new RequestLog();
        logRecord.setUrl(url);
        logRecord.setHttpMethod(httpMethod);
        logRecord.setClassMethod(classMethod);
        logRecord.setRequestParams(requestParams);
        logRecord.setAddTime(DateUtil.formatDateTime(DateUtil.date()));
        logRecord.setAdminId(SecurityUtils.getCurrentAdminId());
        logRecord.setAdminIp(IpUtils.getIpAddr());
        logRecord.setAdminUsername(SecurityUtils.getCurrentUsername());
        String platform = StrUtil.format("{}({})", tigshopProperties.getVersion(), tigshopProperties.getVersionNum());
        logRecord.setPlatform(platform);

        // 插入数据库
        requestLogMapper.insert(logRecord);
        // 保存到 request 中，方便后续使用
        request.setAttribute("REQUEST_LOG_ID", logRecord.getRequestLogId());
        // 执行原方法，并返回结果
        return joinPoint.proceed();
    }

    private static JsonNode sanitizeAuditValue(JsonNode value) {
        if (value.isObject()) {
            ObjectNode result = AUDIT_MAPPER.createObjectNode();
            value.fields().forEachRemaining(field -> {
                String name = field.getKey();
                JsonNode child = field.getValue();
                result.set(name, isSensitiveField(name) && !child.isNull()
                        ? AUDIT_MAPPER.getNodeFactory().textNode("***") : sanitizeAuditValue(child));
            });
            return result;
        }
        if (value.isArray()) {
            ArrayNode result = AUDIT_MAPPER.createArrayNode();
            for (JsonNode child : value) {
                result.add(sanitizeAuditValue(child));
            }
            return result;
        }
        return value;
    }

    private static boolean isSensitiveField(String name) {
        String normalized = name.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELDS.contains(normalized)
                || normalized.contains("password") || normalized.contains("passwd") || normalized.endsWith("pwd")
                || normalized.contains("token") || normalized.contains("secret")
                || normalized.contains("mobile") || normalized.contains("phone") || normalized.contains("email")
                || normalized.endsWith("privatekey") || normalized.endsWith("apikey") || normalized.endsWith("accesskey");
    }
}
