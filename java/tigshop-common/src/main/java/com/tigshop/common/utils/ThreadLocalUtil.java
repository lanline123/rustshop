package com.tigshop.common.utils;

/**
 * @author Jayce
 * @create 2024/9/26 17:17
 */
public class ThreadLocalUtil {
    // 创建ThreadLocal实例
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    // 设置变量的值
    public static void setValue(String value) {
        threadLocal.set(value);
    }

    // 获取变量的值
    public static String getValue() {
        return threadLocal.get();
    }

    // 清除ThreadLocal存储的变量
    public static void clear() {
        threadLocal.remove();

    }
}
