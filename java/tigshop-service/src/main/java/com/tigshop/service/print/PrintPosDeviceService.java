package com.tigshop.service.print;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PrintPosDeviceService {
    void replace(Integer shopId, Integer printId, String deviceCode);

    void validateEnabledDevice(Integer shopId, String deviceCode);

    Map<Integer, String> bindingsByPrintIds(Integer shopId, Collection<Integer> printIds);

    /** 设备码 → 绑定到该设备的打印机名称，用于收银台设备列表展示绑定情况。 */
    Map<String, List<String>> printNamesByDeviceCodes(Integer shopId, Collection<String> deviceCodes);

    boolean hasBindings(Integer shopId, String deviceCode);
}
