// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

/**
 * Security工具类
 *
 * @author Tigshop团队
 * @create 2024年11月15日 13:28
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户名（前后台都能用）
     *
     * @return String
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // 返回当前登录的用户名
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前用户的权限（后台使用）
     *
     * @return Collection<? extends GrantedAuthority>
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // 获取当前用户的权限
            return authentication.getAuthorities();
        }
        return null;
    }

    /**
     * 获取当前用户的详情(前后台都可以用)
     *
     * @return JSON
     */
    public static JSONObject getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != "anonymousUser") {
            return JSONUtil.parseObj(authentication.getPrincipal());
        }
        return null;
    }

    /**
     * 获取当前后台管理员用户的id
     *
     * @return String
     */
    public static Integer getCurrentAdminId() {
        return getCurrentId("adminId");
    }

    /**
     * 获取当前会员用户的id
     *
     * @return String
     */
    public static Integer getCurrentUserId() {
        return getCurrentId("userId");
    }

    /**
     * 通过id字段获取当前用户的id
     *
     * @param field 字段userId或adminId
     * @return String
     */
    public static Integer getCurrentId(String field) {
        JSONObject userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return 0;
        }
        Integer adminOrUserId = (Integer) userDetails.get(field);
        if (adminOrUserId == null || adminOrUserId == 0) {
            return 0;
        }
        return adminOrUserId;
    }

    /**
     * 判断当前用户是否是后台用户
     *
     * @return Boolean
     */
    public static Boolean isAdminUser() {
        Integer currentAdminId = getCurrentAdminId();
        return currentAdminId > 0;
    }
}
