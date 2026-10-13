// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.common.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端工具类
 *
 * @author Jayce
 * @create 2024年10月28日 17:47:52
 */
public class ServletUtils {
    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    public static String getRequestSchemeAndHost() {
        HttpServletRequest request = getRequest();
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        // 构建完整的URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(host);

        // 如果端口号不是默认的80（http）或443（https），则添加端口号
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            url.append(":").append(port);
        }

        return url.toString();
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * 响应返回结果
     *
     * @param response 渲染对象
     * @param aj       待返回的结果
     */
    public static void render(HttpServletResponse response, AjaxResult aj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            renderString(response, objectMapper.writeValueAsString(aj));
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }
    }

}
