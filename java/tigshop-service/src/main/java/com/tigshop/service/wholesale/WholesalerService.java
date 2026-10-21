package com.tigshop.service.wholesale;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tigshop.bean.dto.user.UserListDTO;
import com.tigshop.bean.dto.wholesale.WholesalerAddDTO;
import com.tigshop.bean.dto.wholesale.WholesalerImportDTO;
import com.tigshop.bean.dto.wholesale.WholesalerRemoveByGradeDTO;
import com.tigshop.bean.dto.wholesale.WholesalerRemoveDTO;
import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.bean.query.wholesale.WholesalerPageQuery;
import com.tigshop.bean.vo.wholesale.WholesalerImportResultVO;
import com.tigshop.bean.vo.wholesale.WholesalerSelectableUserVO;
import com.tigshop.bean.vo.wholesale.WholesalerVO;

/**
 * 批发商身份 Service（W1 核心）。
 *
 * <p>职责：
 * <ul>
 *   <li>C 端高频入口判定 {@link #isWholesaleBuyer}（Redis 缓存 + 总开关短路），被购物车/下单调用</li>
 *   <li>后台分页/添加/批量导入/启停/改等级</li>
 *   <li>注册自动转批发钩子 {@link #autoRegisterIfEnabled}（Phase B W4 接入注册链路）</li>
 * </ul>
 *
 * <p>错误码（api-contract §0.3，5300-5399 段）统一消费 {@link com.tigshop.common.constant.HttpStatus} 的 WHOLESALE_* 常量。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesalerService {

    /**
     * 是否批发商（C 端入口核心判定）。
     * <ul>
     *   <li>{@code wholesale.enabled = 0} → 永远 false（短路，不写缓存）</li>
     *   <li>userId = null/≤0 → false</li>
     *   <li>否则查 wholesaler 表 status=1（is_delete 由 @TableLogic 过滤）</li>
     *   <li>走 Redis 缓存 {@code wholesale:isBuyer:{userId}}，TTL 10 分钟；写操作主动失效</li>
     * </ul>
     */
    boolean isWholesaleBuyer(Integer userId);

    /** 失效单个用户的批发商身份缓存（add/import/setStatus/changeGrade 后调用） */
    void invalidateBuyerCache(Integer userId);

    /** 全量失效批发商身份缓存（批发设置变更时由 {@link WholesaleSettingService#save} 调用） */
    void invalidateAllBuyerCache();

    /** 取批发商身份（用于价格/订单需要 grade_id 时；非启用中批发商返回 null） */
    Wholesaler getByUserId(Integer userId);

    /** 当前等级 ID（不是启用中批发商返回 0） */
    Integer getGradeIdByUserId(Integer userId);

    /** 后台分页列表（CR-VOS-1：回填 user/user_rank/grade 字段；CR-VOS-2：keyword 联合搜索 + fromTag 筛选） */
    IPage<WholesalerVO> page(WholesalerPageQuery query);

    /**
     * 「可选为批发商的会员」分页（添加批发商选品用，2026-06-11）。
     *
     * <p>复用会员列表 {@code userService.list} 的全部查询条件，逐行追加批发商身份标识：
     * <ul>
     *   <li>{@code isWholesaler}：是否已是有效批发商（非软删记录存在即为 true，含禁用态）；</li>
     *   <li>{@code gradeId}/{@code gradeName}：已是批发商时回填其当前批发等级。</li>
     * </ul>
     * 不剔除任何会员，前端可据标识置灰禁选或自行过滤；分页口径与会员列表完全一致。
     */
    IPage<WholesalerSelectableUserVO> selectableUserPage(UserListDTO query);

    /** 后台单个添加，返回 wholesalerId */
    Integer add(WholesalerAddDTO dto);

    /**
     * 批量导入：
     * <ul>
     *   <li>importAll=true → 导入全部启用会员；否则按 userIds</li>
     *   <li>existsStrategy=OVERWRITE_GRADE（默认，C15）：已是批发商 → 覆盖 grade，不动 status，计入 skipped</li>
     *   <li>existsStrategy=SKIP：已是批发商跳过</li>
     * </ul>
     */
    WholesalerImportResultVO importBatch(WholesalerImportDTO dto);

    /** 启停（status 0=禁用 1=启用） */
    void setStatus(Integer wholesalerId, Integer status);

    /** 改等级（gradeId 须启用中） */
    void changeGrade(Integer wholesalerId, Integer gradeId);

    /**
     * 按等级移除批发商（删除等级前清空该等级下批发商的配套接口）。
     *
     * <p>语义：「移除」= 对 wholesaler 记录做软删（is_delete=1），即该用户不再是批发商身份，
     * <b>不是改等级</b>。
     * <ul>
     *   <li>removeAll=true：移除该等级下全部未删除批发商，忽略 wholesalerIds；</li>
     *   <li>removeAll!=true：按 wholesalerIds 移除，且仅限真正属于该 gradeId 的记录（跨等级误删保护）；
     *       wholesalerIds 为空 → 抛 {@link com.tigshop.common.constant.HttpStatus#BAD_REQUEST}；</li>
     *   <li>软删的同时更新 update_time（Unix 秒）；</li>
     *   <li>逐个受影响 user_id 调用 {@link #invalidateBuyerCache} 失效批发商身份缓存。</li>
     * </ul>
     *
     * @param dto 入参（gradeId 必填、wholesalerIds 可空、removeAll 默认 false）
     * @return 实际移除（软删）的批发商条数；目标为空时返回 0
     */
    int removeByGrade(WholesalerRemoveByGradeDTO dto);

    /**
     * 按 ID 直接移除批发商身份（CR-20260611-104500）。
     *
     * <p>语义：对 wholesaler 记录软删（is_delete=1 + 更新 update_time），该用户不再是批发商身份，
     * <b>不是改等级</b>；不需要 gradeId，也不受默认等级保护（默认等级保护仅约束「删等级 5306」）。
     * <ul>
     *   <li>wholesalerIds 为空 → 抛 {@link com.tigshop.common.constant.HttpStatus#BAD_REQUEST}；</li>
     *   <li>已删除 / 不存在的 ID 自动跳过，不计入 removedCount；</li>
     *   <li>逐个受影响 user_id 调用 {@link #invalidateBuyerCache} 失效批发商身份缓存。</li>
     * </ul>
     *
     * @param dto 入参（wholesalerIds 必填非空）
     * @return 实际移除（软删）的批发商条数
     */
    int remove(WholesalerRemoveDTO dto);

    /**
     * 注册自动转批发（注册钩子用，Phase B W4 调用）。
     * 双开关（enabled + autoRegister）+ 默认等级有效性判定；失败不抛错，不影响注册主流程。
     */
    void autoRegisterIfEnabled(Integer userId);
}
