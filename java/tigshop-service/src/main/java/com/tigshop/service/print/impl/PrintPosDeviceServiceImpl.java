package com.tigshop.service.print.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintPosDevice;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.mapper.print.PrintPosDeviceMapper;
import com.tigshop.service.print.PrintPosDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrintPosDeviceServiceImpl implements PrintPosDeviceService {
    private final PrintPosDeviceMapper mapper;
    private final PrintMapper printMapper;

    @Override
    @Transactional
    public void replace(Integer shopId, Integer printId, String deviceCode) {
        mapper.delete(new LambdaQueryWrapper<PrintPosDevice>()
                .eq(PrintPosDevice::getShopId, shopId)
                .eq(PrintPosDevice::getPrintId, printId));
        if (StrUtil.isBlank(deviceCode)) {
            return;
        }
        String code = deviceCode.trim();
        boolean enabled = false;
        if (!enabled) {
            throw new GlobalException("设备码不存在或已停用: " + code);
        }
        PrintPosDevice binding = new PrintPosDevice();
        binding.setShopId(shopId);
        binding.setPrintId(printId);
        binding.setDeviceCode(code);
        binding.setAddTime(StringUtils.getCurrentTime());
        mapper.insert(binding);
    }

    @Override
    public void validateEnabledDevice(Integer shopId, String deviceCode) {
        if (StrUtil.isBlank(deviceCode)) {
            return;
        }
        boolean enabled = false;
    }

    @Override
    public Map<Integer, String> bindingsByPrintIds(Integer shopId, Collection<Integer> printIds) {
        if (printIds == null || printIds.isEmpty()) {
            return Map.of();
        }
        List<PrintPosDevice> rows = mapper.selectList(new LambdaQueryWrapper<PrintPosDevice>()
                .eq(PrintPosDevice::getShopId, shopId)
                .in(PrintPosDevice::getPrintId, printIds));
        Map<Integer, String> result = new java.util.LinkedHashMap<>();
        rows.forEach(row -> result.putIfAbsent(row.getPrintId(), row.getDeviceCode()));
        return result;
    }

    @Override
    public Map<String, List<String>> printNamesByDeviceCodes(Integer shopId, Collection<String> deviceCodes) {
        if (deviceCodes == null || deviceCodes.isEmpty()) {
            return Map.of();
        }
        List<PrintPosDevice> rows = mapper.selectList(new LambdaQueryWrapper<PrintPosDevice>()
                .eq(PrintPosDevice::getShopId, shopId)
                .in(PrintPosDevice::getDeviceCode, deviceCodes));
        if (rows.isEmpty()) {
            return Map.of();
        }
        List<Print> prints = printMapper.selectList(new LambdaQueryWrapper<Print>()
                .in(Print::getPrintId, rows.stream().map(PrintPosDevice::getPrintId).distinct().toList())
                .select(Print::getPrintId, Print::getPrintName));
        Map<Integer, String> nameByPrintId = new java.util.HashMap<>();
        prints.forEach(print -> nameByPrintId.put(print.getPrintId(), print.getPrintName()));
        Map<String, List<String>> result = new java.util.LinkedHashMap<>();
        rows.forEach(row -> {
            String printName = nameByPrintId.get(row.getPrintId());
            if (printName != null) {
                result.computeIfAbsent(row.getDeviceCode(), code -> new java.util.ArrayList<>()).add(printName);
            }
        });
        return result;
    }

    @Override
    public boolean hasBindings(Integer shopId, String deviceCode) {
        if (shopId == null || StrUtil.isBlank(deviceCode)) {
            return false;
        }
        return mapper.selectCount(new LambdaQueryWrapper<PrintPosDevice>()
                .eq(PrintPosDevice::getShopId, shopId)
                .eq(PrintPosDevice::getDeviceCode, deviceCode.trim())) > 0;
    }
}
