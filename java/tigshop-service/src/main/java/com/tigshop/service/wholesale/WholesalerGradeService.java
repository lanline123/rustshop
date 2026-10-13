package com.tigshop.service.wholesale;

import com.tigshop.bean.dto.wholesale.WholesalerGradeSaveDTO;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.vo.wholesale.WholesalerGradeVO;

import java.util.List;
import java.util.Map;

/**
 * 批发商等级 Service
 *
 * <p>约束：
 * <ul>
 *   <li>delete 为软删（CR-20260610-2210）：绑定批发商时拦截（5305），默认等级不可删（5306）</li>
 *   <li>默认等级 ID 由 config.wholesaleDefaultGradeId 指定，语义由 /setting 接口承载（CR-VOS-3）</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesalerGradeService {

    /**
     * 列表（按 sortOrder 升序）。
     *
     * @param statusFilter  状态筛选（0=禁用 1=启用），为 null 时不筛选
     * @param withUserCount 是否回填已绑定客户数
     */
    List<WholesalerGradeVO> list(Integer statusFilter, boolean withUserCount);

    /** 新增或编辑（gradeId 为 null = 新增），返回 gradeId */
    Integer save(WholesalerGradeSaveDTO dto);

    /** 启停（status 0=禁用 1=启用） */
    void setStatus(Integer gradeId, Integer status);

    /**
     * 删除等级（软删，CR-20260610-2210）。
     *
     * <ul>
     *   <li>等级不存在 → 5303</li>
     *   <li>是默认批发等级（config.wholesaleDefaultGradeId）→ 5306（请先在批发设置更换默认等级）</li>
     *   <li>该等级下仍绑定批发商用户（含禁用，{@code wholesaler.is_delete=0}）→ 5305（需先删除该等级下所有批发商用户）</li>
     *   <li>校验通过 → 置 {@code is_delete=1} 软删</li>
     * </ul>
     */
    void delete(Integer gradeId);

    /** 内部使用：按 ID 取 entity（已删除返回 null） */
    WholesalerGrade getById(Integer gradeId);

    /** 内部使用：校验 gradeId 启用中（不存在抛 5303；已禁用抛 5304） */
    void requireEnabled(Integer gradeId);

    /** 内部使用：按 ID 列表批量取（用于列表回填 gradeName） */
    Map<Integer, WholesalerGrade> mapByIds(List<Integer> gradeIds);
}
