package com.tigshop.service.wholesale;

import com.tigshop.bean.dto.wholesale.WholesaleSettingSaveDTO;
import com.tigshop.bean.vo.wholesale.WholesaleSettingVO;

/**
 * 批发设置 Service。
 *
 * <p>设计要点（CR-VOS-4 / api-contract §1.A10/A11）：
 * <ul>
 *   <li>4 项配置（{@code wholesaleEnabled} / {@code wholesaleAutoRegister} /
 *       {@code wholesaleDefaultGradeId} / {@code wholesaleGivePoints}）落 {@code config} 表，
 *       bizCode 由 {@link com.tigshop.bean.enums.setting.SettingsEnum#WHOLESALE_ENABLED} 等枚举提供，
 *       与 API JSON 字段、SettingsEnum bizCode 三方完全同名。</li>
 *   <li>{@code save} 含 3 项前置校验：
 *       多店铺(5330) → 默认等级未配置(5331) → 默认等级不存在/禁用(5303/5304 经
 *       {@link WholesalerGradeService#requireEnabled})。</li>
 *   <li>多店铺判定 v2 暂以单店铺前提兜底（详见实现注释）。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesaleSettingService {

    /**
     * 查询当前批发设置（含默认等级名称回填 + 当前是否多店铺）。
     *
     * @return 完整 4 项配置 + 派生字段 {@code defaultGradeName} / {@code isMultiShop}
     */
    WholesaleSettingVO get();

    /**
     * 保存批发设置（含 C8 多店铺 + 默认等级有效性前置校验）。
     *
     * @throws com.tigshop.common.exception.GlobalException 校验失败时抛出 5330 / 5331 / 5303 / 5304
     */
    void save(WholesaleSettingSaveDTO dto);

    /** 总开关：{@code wholesaleEnabled = 1} 返回 true，被 T-101 {@code isWholesaleBuyer} 短路调用 */
    boolean isEnabled();

    /** 默认批发等级 ID，未配置返回 0 */
    Integer getDefaultGradeId();

    /** 批发订单是否赠送积分 */
    boolean isGivePoints();

    /** 新会员注册是否自动转批发商（Phase B 注册钩子调用） */
    boolean isAutoRegister();
}
