package com.tigshop.service.wholesale.impl;

import com.tigshop.bean.dto.wholesale.WholesaleSettingSaveDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.vo.wholesale.WholesaleSettingVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.TransactionAfterCommitExecutor;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.ConfigValCache;
import com.tigshop.service.wholesale.WholesaleSettingService;
import com.tigshop.service.wholesale.WholesalerGradeService;
import com.tigshop.service.wholesale.WholesalerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 批发设置 Service 实现。
 *
 * <p>错误码（api-contract §0.3，5300-5399 段）一律消费 {@link HttpStatus} 中的 {@code WHOLESALE_*} 常量，
 * 禁用字面量；与 T-103 风格保持一致。
 *
 * <p>多店铺判定（C8 / 5330）按主控批准的 A2 兜底方案处理 — 详见 {@link #isMultiShop()}。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WholesaleSettingServiceImpl implements WholesaleSettingService {

    private final ConfigService configService;
    private final ConfigValCache configValCache;
    private final WholesalerGradeService gradeService;
    // 惰性获取：WholesalerServiceImpl 依赖本类（settingService.isEnabled），用 ObjectProvider 打破构造期循环依赖
    private final ObjectProvider<WholesalerService> wholesalerServiceProvider;

    // ====================================================================================
    // 查询
    // ====================================================================================

    @Override
    public WholesaleSettingVO get() {
        WholesaleSettingVO vo = new WholesaleSettingVO();
        vo.setWholesaleEnabled(readInt(SettingsEnum.WHOLESALE_ENABLED));
        vo.setWholesaleAutoRegister(readInt(SettingsEnum.WHOLESALE_AUTO_REGISTER));
        vo.setWholesaleDefaultGradeId(readInt(SettingsEnum.WHOLESALE_DEFAULT_GRADE_ID));
        vo.setWholesaleGivePoints(readInt(SettingsEnum.WHOLESALE_GIVE_POINTS));
        vo.setIsMultiShop(isMultiShop());

        // 默认等级名称回填：找不到 / 已禁用 → 设为 null 但不抛错（让 admin 能进设置页修复）
        Integer defaultGradeId = vo.getWholesaleDefaultGradeId();
        if (defaultGradeId != null && defaultGradeId > 0) {
            try {
                WholesalerGrade g = gradeService.getById(defaultGradeId);
                if (g != null && g.getStatus() != null && g.getStatus() == 1) {
                    vo.setDefaultGradeName(g.getGradeName());
                }
            } catch (Exception ignore) {
                // 静默：让设置页可访问以便修复
            }
        }
        return vo;
    }

    // ====================================================================================
    // 保存（高风险：含 3 项前置校验 + 原子写入 + 缓存失效预留）
    // ====================================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(WholesaleSettingSaveDTO dto) {
        // 校验 1：多店铺 + 启用 → 5330（C8）
        // 注：A2 兜底方案下 isMultiShop() 当前恒为 false，校验事实空跑；
        //     ShopService 增加 isMultiShop() 真实判定后无需改本类。
        if (Boolean.TRUE.equals(isMultiShop())
                && dto.getWholesaleEnabled() != null && dto.getWholesaleEnabled() == 1) {
            throw new GlobalException("多店铺暂不支持批发功能", HttpStatus.WHOLESALE_MULTI_SHOP_NOT_SUPPORT);
        }

        // 校验 2：启用批发但未配置默认等级 → 5331
        if (dto.getWholesaleEnabled() != null && dto.getWholesaleEnabled() == 1
                && (dto.getWholesaleDefaultGradeId() == null || dto.getWholesaleDefaultGradeId() == 0)) {
            throw new GlobalException("启用批发功能前必须配置默认批发等级",
                    HttpStatus.WHOLESALE_DEFAULT_GRADE_NOT_CONFIGURED);
        }

        // 校验 3：默认等级必须存在且 status=1（gradeService.requireEnabled 内部抛 5303/5304）
        if (dto.getWholesaleDefaultGradeId() != null && dto.getWholesaleDefaultGradeId() > 0) {
            gradeService.requireEnabled(dto.getWholesaleDefaultGradeId());
        }

        // 原子写入 4 项（外层 @Transactional 包裹）
        writeInt(SettingsEnum.WHOLESALE_ENABLED, defaultZero(dto.getWholesaleEnabled()));
        writeInt(SettingsEnum.WHOLESALE_AUTO_REGISTER, defaultZero(dto.getWholesaleAutoRegister()));
        writeInt(SettingsEnum.WHOLESALE_DEFAULT_GRADE_ID, defaultZero(dto.getWholesaleDefaultGradeId()));
        writeInt(SettingsEnum.WHOLESALE_GIVE_POINTS, defaultZero(dto.getWholesaleGivePoints()));

        TransactionAfterCommitExecutor.execute(() -> configValCache.invalidateAndBroadcast(List.of(
                SettingsEnum.WHOLESALE_ENABLED.getBizCode(),
                SettingsEnum.WHOLESALE_AUTO_REGISTER.getBizCode(),
                SettingsEnum.WHOLESALE_DEFAULT_GRADE_ID.getBizCode(),
                SettingsEnum.WHOLESALE_GIVE_POINTS.getBizCode()
        )));

        // 缓存失效：设置项变更（尤其 wholesaleEnabled 切换）影响 isWholesaleBuyer 判定，
        // 必须批量失效 wholesale:isBuyer:* keys，否则总开关关→开后旧的 "0" 仍会让批发商被判为非批发商。
        WholesalerService wholesalerService = wholesalerServiceProvider.getIfAvailable();
        if (wholesalerService != null) {
            wholesalerService.invalidateAllBuyerCache();
        }
        log.info("[wholesale-setting] save committed; isBuyer cache invalidated");
    }

    // ====================================================================================
    // 内部高频读（不缓存：依赖 ConfigService 自身缓存层）
    // ====================================================================================

    @Override
    public boolean isEnabled() {
        Integer v = readInt(SettingsEnum.WHOLESALE_ENABLED);
        return v != null && v == 1;
    }

    @Override
    public Integer getDefaultGradeId() {
        Integer v = readInt(SettingsEnum.WHOLESALE_DEFAULT_GRADE_ID);
        return v != null ? v : 0;
    }

    @Override
    public boolean isGivePoints() {
        Integer v = readInt(SettingsEnum.WHOLESALE_GIVE_POINTS);
        return v != null && v == 1;
    }

    @Override
    public boolean isAutoRegister() {
        Integer v = readInt(SettingsEnum.WHOLESALE_AUTO_REGISTER);
        return v != null && v == 1;
    }

    // ====================================================================================
    // 私有工具
    // ====================================================================================

    /**
     * 多店铺判定 — 主控批准的 A2 兜底（v2 单店铺前提）。
     *
     * <p>设计文档原话「多店铺批发是 v2 范围外」；当前项目 {@code ShopService} 暂未提供平台级
     * {@code isMultiShop()} 判定方法。本方法恒返回 {@code false}，{@link #save} 中的 5330 校验
     * 链路保留但事实空跑。
     *
     * <p>TODO(v2-multi-shop): 待 {@code ShopService} 增加 {@code isMultiShop()}
     * （或等价的平台级店铺数判定）后，将本方法改为调用真实判定，校验链路无需改动。
     *
     * @return 当前固定 false
     */
    private Boolean isMultiShop() {
        return Boolean.FALSE;
    }

    /**
     * 读取单项 Integer 配置；缺失或解析失败回退到 {@link SettingsEnum#getDefaultValue()}。
     */
    private Integer readInt(SettingsEnum key) {
        String raw = configService.getConfigVal(key);
        if (raw == null || raw.isEmpty()) {
            return parseIntOrNull(key.getDefaultValue());
        }
        Integer parsed = parseIntOrNull(raw);
        return parsed != null ? parsed : parseIntOrNull(key.getDefaultValue());
    }

    /**
     * 写入单项 Integer 配置：按 bizCode 取已有 ConfigPO（{@code @TableLogic isDel} 自动过滤），
     * 命中则更新 bizVal，未命中则新建。
     */
    private void writeInt(SettingsEnum key, Integer value) {
        if (value == null) {
            return;
        }
        ConfigPO config = configService.lambdaQuery()
                .eq(ConfigPO::getBizCode, key.getBizCode())
                .one();
        if (config == null) {
            config = new ConfigPO();
            config.setBizCode(key.getBizCode());
        }
        config.setBizVal(String.valueOf(value));
        configService.saveOrUpdate(config);
    }

    private static Integer defaultZero(Integer v) {
        return v != null ? v : 0;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
