package com.tigshop.service.deposit.impl;

import org.springframework.security.access.prepost.PreAuthorize;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.deposit.DepositSettingDTO;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.vo.deposit.DepositSettingVO;
import com.tigshop.service.deposit.DepositSettingService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺保证金设置服务实现。
 *
 * <p>设置项以 config 表键值方式存储（每个 key 一行 bizCode/bizVal），与项目现有
 * SettingsHelper 的单键存储口径一致；读取时无值回退默认值，保存时逐项 upsert。</p>
 *
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class DepositSettingServiceImpl implements DepositSettingService {

    private static final String KEY_ENABLE = "deposit_enable";
    private static final String KEY_COLLECT_RULE = "deposit_collect_rule";
    private static final String KEY_COLLECT_NODE = "deposit_collect_node";
    private static final String KEY_COLLECT_BUFFER_DAYS = "deposit_collect_buffer_days";
    private static final String KEY_COLLECT_BUFFER_ACTION = "deposit_collect_buffer_action";
    private static final String KEY_SUPPLEMENT_BUFFER_DAYS = "deposit_supplement_buffer_days";
    private static final String KEY_SUPPLEMENT_BUFFER_ACTION = "deposit_supplement_buffer_action";
    private static final String KEY_PROTOCOL_ENABLE = "deposit_protocol_enable";
    private static final String KEY_PROTOCOL_CONTENT = "deposit_protocol_content";

    private static final List<String> ALL_KEYS = List.of(
            KEY_ENABLE, KEY_COLLECT_RULE, KEY_COLLECT_NODE,
            KEY_COLLECT_BUFFER_DAYS, KEY_COLLECT_BUFFER_ACTION,
            KEY_SUPPLEMENT_BUFFER_DAYS, KEY_SUPPLEMENT_BUFFER_ACTION,
            KEY_PROTOCOL_ENABLE, KEY_PROTOCOL_CONTENT);

    private final ConfigService configService;

    @Override
    public DepositSettingVO detail() {
        Map<String, String> values = configService.lambdaQuery()
                .in(ConfigPO::getBizCode, ALL_KEYS)
                .eq(ConfigPO::getIsDel, 0)
                .list()
                .stream()
                .filter(c -> c.getBizVal() != null)
                .collect(Collectors.toMap(ConfigPO::getBizCode, ConfigPO::getBizVal, (a, b) -> b));

        DepositSettingVO vo = new DepositSettingVO();
        vo.setDepositEnable(parseInt(values.get(KEY_ENABLE), 0));
        vo.setDepositCollectRule(parseInt(values.get(KEY_COLLECT_RULE), 1));
        vo.setDepositCollectNode(parseInt(values.get(KEY_COLLECT_NODE), 1));
        vo.setDepositCollectBufferDays(parseInt(values.get(KEY_COLLECT_BUFFER_DAYS), 0));
        vo.setDepositCollectBufferAction(parseInt(values.get(KEY_COLLECT_BUFFER_ACTION), 1));
        vo.setDepositSupplementBufferDays(parseInt(values.get(KEY_SUPPLEMENT_BUFFER_DAYS), 0));
        vo.setDepositSupplementBufferAction(parseInt(values.get(KEY_SUPPLEMENT_BUFFER_ACTION), 1));
        vo.setDepositProtocolEnable(parseInt(values.get(KEY_PROTOCOL_ENABLE), 0));
        vo.setDepositProtocolContent(values.getOrDefault(KEY_PROTOCOL_CONTENT, ""));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositSettingSave')")
    public void save(DepositSettingDTO dto) {
        upsert(KEY_ENABLE, dto.getDepositEnable());
        upsert(KEY_COLLECT_RULE, dto.getDepositCollectRule());
        upsert(KEY_COLLECT_NODE, dto.getDepositCollectNode());
        upsert(KEY_COLLECT_BUFFER_DAYS, dto.getDepositCollectBufferDays());
        upsert(KEY_COLLECT_BUFFER_ACTION, dto.getDepositCollectBufferAction());
        upsert(KEY_SUPPLEMENT_BUFFER_DAYS, dto.getDepositSupplementBufferDays());
        upsert(KEY_SUPPLEMENT_BUFFER_ACTION, dto.getDepositSupplementBufferAction());
        upsert(KEY_PROTOCOL_ENABLE, dto.getDepositProtocolEnable());
        upsert(KEY_PROTOCOL_CONTENT, dto.getDepositProtocolContent());
    }

    /**
     * 按 bizCode upsert 单个配置项（先删后插，与 SettingsHelper.set 口径一致）；值为 null 时跳过。
     */
    private void upsert(String bizCode, Object value) {
        if (value == null) {
            return;
        }
        configService.remove(Wrappers.lambdaQuery(ConfigPO.class).eq(ConfigPO::getBizCode, bizCode));
        configService.save(new ConfigPO(bizCode, String.valueOf(value)));
    }

    private static Integer parseInt(String raw, Integer def) {
        if (StrUtil.isBlank(raw)) {
            return def;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
