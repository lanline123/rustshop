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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Tigshop团队
 */
@Component
public class SpringContentUtils {
    // 静态变量保存 ApplicationContext
    private static ApplicationContext applicationContext;

    // 通过构造方法注入 ApplicationContext
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (Objects.isNull(SpringContentUtils.applicationContext)) {
            SpringContentUtils.applicationContext = applicationContext;
        }

    }

    // 获取 Bean 方法
    public static <T> T getBean(Class<T> clazz) {
        // 从 Spring 容器中获取 Bean，类型为 clazz
        if (applicationContext != null) {
            return applicationContext.getBean(clazz);
        }
        return null;
    }

    // 获取 Bean 方法，可以根据 Bean 名称来获取
    public static Object getBean(String beanName) {
        if (applicationContext != null) {
            return applicationContext.getBean(beanName);
        }
        return null;
    }

    // 获取 Bean 方法，根据 Bean 名称和类型获取
    public static <T> T getBean(String beanName, Class<T> clazz) {
        if (applicationContext != null) {
            return applicationContext.getBean(beanName, clazz);
        }
        return null;
    }
}