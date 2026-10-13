package com.tigshop.helper;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.mapper.setting.ConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置项 Helper
 *
 * @author kidd
 * @since 2025/6/10 15:42
 */
@Slf4j
@Component
public class SettingsHelper {

    private final ConfigMapper configMapper;

    /**
     * 缓存数据库配置值
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    public SettingsHelper(ConfigMapper configMapper) {
        this.configMapper = configMapper;
        this.initialize();
    }

    /**
     * 初始化缓存所有数据库配置值
     */
    private void initialize() {
        // TODO 不用项目缓存，后面换 redis 缓存
        // List<ConfigPO> configs = configMapper.selectList(Wrappers.emptyWrapper());
        // for (ConfigPO config : configs) {
        //     configCache.put(config.getBizCode(), config.getBizVal());
        // }

        // TODO 补充数据库中没有的默认项, 暂时注释，当配置项的默认值都设定好可以打开
        // for (SettingsEnum setting : SettingsEnum.values()) {
        //     if (!configCache.containsKey(setting.getBizCode())) {
        //         set(setting, setting.getDefaultValue());
        //     }
        // }
    }

    /**
     * 获取配置（自动解析类型 + 默认值回退）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(SettingsEnum setting) {
        String rawValue = getRaw(setting);
        if (setting == SettingsEnum.BUSINESS_LICENSE_ENABLED
                && (rawValue == null || rawValue.trim().isEmpty())) {
            return (T) Integer.valueOf(1);
        }
        Object parsed = parse(setting.getValueType(), rawValue);
        // 新增开关对历史缺失、空值及非法值均按开启处理，保证向后兼容。
        if (setting == SettingsEnum.BUSINESS_LICENSE_ENABLED
                && (!(parsed instanceof Integer) || (((Integer) parsed) != 0 && ((Integer) parsed) != 1))) {
            parsed = 1;
        }
        return (T) parsed;
    }

    /**
     * 获取原始字符串（带默认值）
     */
    public String getRaw(SettingsEnum setting) {
        // TODO 不用项目缓存，后面换 redis 缓存
        // return configCache.getOrDefault(setting.getBizCode(), setting.getDefaultValue());
        ConfigPO config = configMapper.selectOne(
                Wrappers.lambdaQuery(ConfigPO.class)
                        .eq(ConfigPO::getBizCode, setting.getBizCode()));
        return config != null ? config.getBizVal() : setting.getDefaultValue() != null ? setting.getDefaultValue() : "0";
    }

    /**
     * 用一次数据库查询读取一组配置原值。缺失项不写入结果，由调用方按业务规则处理默认值。
     */
    public Map<String, String> getRawSnapshot(SettingsEnum... settings) {
        if (settings == null || settings.length == 0) {
            return Collections.emptyMap();
        }
        List<String> bizCodes = Arrays.stream(settings)
                .map(SettingsEnum::getBizCode)
                .toList();
        List<ConfigPO> configs = configMapper.selectList(
                Wrappers.lambdaQuery(ConfigPO.class)
                        .in(ConfigPO::getBizCode, bizCodes)
                        .eq(ConfigPO::getIsDel, 0));
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (ConfigPO config : configs) {
            snapshot.putIfAbsent(config.getBizCode(), config.getBizVal());
        }
        return snapshot;
    }

    /**
     * 设置配置值（更新数据库 + 刷新缓存）
     */
    public void set(SettingsEnum setting, Object value) {
        String bigVal;
        if (setting == SettingsEnum.BUSINESS_LICENSE_ENABLED) {
            String raw = value == null ? null : String.valueOf(value);
            bigVal = "0".equals(raw) ? "0" : "1";
        } else {
            bigVal = String.valueOf(value);
        }

        // 更新数据库
        ConfigPO config = new ConfigPO();
        config.setBizCode(setting.getBizCode());
        config.setBizVal(bigVal);
        this.set(config);
    }

    public void set(ConfigPO config) {
        if (SettingsEnum.BUSINESS_LICENSE_ENABLED.getBizCode().equals(config.getBizCode())) {
            config.setBizVal("0".equals(config.getBizVal()) ? "0" : "1");
        }

        // 更新数据库
        configMapper.delete(Wrappers.lambdaQuery(ConfigPO.class).eq(ConfigPO::getBizCode, config.getBizCode()));
        configMapper.insert(config);

        // 更新缓存
        // configCache.put(config.getBizCode(), bizVal);
    }

    /**
     * 类型解析方法
     */
    private Object parse(SettingsEnum.ValueTypeEnum type, String valueStr) {
        try {
            boolean blank = valueStr == null || valueStr.isBlank();
            return switch (type) {
                case STRING -> valueStr;
                case INTEGER -> blank ? 0 : Integer.parseInt(valueStr);
                case LONG -> blank ? 0L : Long.parseLong(valueStr);
                case BOOLEAN -> !blank && Boolean.parseBoolean(valueStr);
                case DECIMAL -> blank ? BigDecimal.ZERO : new BigDecimal(valueStr);
                case JSON -> blank ? null : JSON.parse(valueStr);
                case JSON_ARRAY -> blank ? null : JSON.parseArray(valueStr);
                case JSON_OBJECT -> blank ? null : JSON.parseObject(valueStr);
            };
        } catch (Exception e) {
            log.error("配置项类型转换失败：{} -> {}", valueStr, type, e);
            return null;
        }
    }
}
