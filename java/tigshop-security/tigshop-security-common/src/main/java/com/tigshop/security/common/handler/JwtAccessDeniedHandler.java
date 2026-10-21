/*
 * ---------------------------------------------------------------------+
 *  文件 --
 * ---------------------------------------------------------------------+
 *  版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 *  作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 *  提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */

package com.tigshop.security.common.handler;

import cn.hutool.json.JSONUtil;
import com.tigshop.common.core.AjaxResult;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * @author Tigshop团队
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        if (httpServletResponse.isCommitted()) {
            log.debug("Response already committed for {}, skipping AccessDenied body",
                    httpServletRequest.getRequestURI());
            return;
        }
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        outputStream.write(JSONUtil.toJsonStr(AjaxResult.error(SERVICE_DATA_ERROR, PARAM_ERROR)).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}
