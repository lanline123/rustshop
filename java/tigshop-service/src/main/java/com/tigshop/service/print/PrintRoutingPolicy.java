package com.tigshop.service.print;

import com.tigshop.bean.model.print.Print;

import java.util.List;
import java.util.Map;

/**
 * 收银台打印机选择策略：明确设备码时严格使用该设备绑定的打印机；
 * 未提供设备码时才按默认打印机和首台启用打印机回退。
 */
public final class PrintRoutingPolicy {

    private PrintRoutingPolicy() {
    }

    public static List<Print> select(String deviceCode,
                                     List<Print> enabledPrinters,
                                     Map<Integer, String> bindings,
                                     Integer defaultPrintId) {
        if (enabledPrinters == null || enabledPrinters.isEmpty()) {
            return List.of();
        }
        if (deviceCode != null && !deviceCode.isBlank()) {
            return enabledPrinters.stream()
                    .filter(print -> deviceCode.equals(bindings.get(print.getPrintId())))
                    .toList();
        }
        if (defaultPrintId != null) {
            List<Print> defaults = enabledPrinters.stream()
                    .filter(print -> defaultPrintId.equals(print.getPrintId()))
                    .toList();
            if (!defaults.isEmpty()) {
                return defaults;
            }
        }
        return List.of(enabledPrinters.getFirst());
    }
}
