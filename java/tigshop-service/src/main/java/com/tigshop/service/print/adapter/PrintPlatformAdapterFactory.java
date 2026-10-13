// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.adapter;

import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.common.exception.GlobalException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印平台适配器工厂
 * <p>
 * Spring 启动时扫描所有 {@link PrintPlatformAdapter} 实现，按
 * {@link PrintPlatformAdapter#getPlatformCode()} 注册到 map 中，供上层按
 * {@code Print.platform} 取用。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Component
public class PrintPlatformAdapterFactory {

    private final Map<Integer, PrintPlatformAdapter> adapters;

    public PrintPlatformAdapterFactory(List<PrintPlatformAdapter> adapterList) {
        Map<Integer, PrintPlatformAdapter> map = new HashMap<>(adapterList.size() * 2);
        for (PrintPlatformAdapter a : adapterList) {
            Integer code = a.getPlatformCode();
            if (code == null) {
                throw new IllegalStateException("PrintPlatformAdapter 必须提供非空 platformCode：" + a.getClass().getName());
            }
            if (map.containsKey(code)) {
                throw new IllegalStateException("PrintPlatformAdapter 平台码冲突：code=" + code
                        + "，冲突类：" + map.get(code).getClass().getName()
                        + " vs " + a.getClass().getName());
            }
            map.put(code, a);
        }
        this.adapters = Map.copyOf(map);
    }

    /**
     * 按平台码取适配器；找不到时抛业务异常（而非返回 null），避免上层漏空判
     *
     * @param platform 平台码
     * @return 适配器实例
     */
    public PrintPlatformAdapter of(Integer platform) {
        PrintPlatformAdapter a = adapters.get(platform);
        if (a == null) {
            throw new GlobalException("不支持的打印平台：" + PrintPlatformEnum.getDescByCode(platform)
                    + "(code=" + platform + ")");
        }
        return a;
    }
}
