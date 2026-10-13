// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.renderer;

import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.common.exception.GlobalException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小票渲染器工厂（与 {@code PrintPlatformAdapterFactory} 同构）
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Component
public class ReceiptRendererFactory {

    private final Map<Integer, ReceiptRenderer> renderers;

    public ReceiptRendererFactory(List<ReceiptRenderer> rendererList) {
        Map<Integer, ReceiptRenderer> map = new HashMap<>(rendererList.size() * 2);
        for (ReceiptRenderer r : rendererList) {
            Integer code = r.getPlatformCode();
            if (code == null) {
                throw new IllegalStateException("ReceiptRenderer 必须提供非空 platformCode：" + r.getClass().getName());
            }
            if (map.containsKey(code)) {
                throw new IllegalStateException("ReceiptRenderer 平台码冲突：code=" + code
                        + "，冲突类：" + map.get(code).getClass().getName()
                        + " vs " + r.getClass().getName());
            }
            map.put(code, r);
        }
        this.renderers = Map.copyOf(map);
    }

    public ReceiptRenderer of(Integer platform) {
        ReceiptRenderer r = renderers.get(platform);
        if (r == null) {
            throw new GlobalException("不支持的打印平台渲染器：" + PrintPlatformEnum.getDescByCode(platform)
                    + "(code=" + platform + ")");
        }
        return r;
    }
}
