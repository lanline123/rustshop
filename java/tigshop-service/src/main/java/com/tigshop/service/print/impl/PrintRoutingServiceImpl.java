package com.tigshop.service.print.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.service.print.PrintPosDeviceService;
import com.tigshop.service.print.PrintRoutingPolicy;
import com.tigshop.service.print.PrintRoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrintRoutingServiceImpl implements PrintRoutingService {
    private final PrintMapper printMapper;
    private final PrintPosDeviceService bindingService;

    @Override
    public List<Print> resolve(Integer shopId, String deviceCode) {
        String normalizedDeviceCode = StrUtil.isBlank(deviceCode) ? null : deviceCode.trim();
        List<Print> enabled = printMapper.selectList(new LambdaQueryWrapper<Print>()
                .eq(Print::getShopId, shopId)
                .eq(Print::getStatus, PrintStatusEnum.ENABLED.getCode())
                .orderByDesc(Print::getIsDefault)
                .orderByAsc(Print::getPrintId));
        Map<Integer, String> bindings = bindingService.bindingsByPrintIds(
                shopId, enabled.stream().map(Print::getPrintId).toList());
        Integer defaultId = enabled.stream()
                .filter(print -> Integer.valueOf(1).equals(print.getIsDefault()))
                .map(Print::getPrintId).findFirst().orElse(null);
        return PrintRoutingPolicy.select(normalizedDeviceCode, enabled, bindings, defaultId);
    }

}
