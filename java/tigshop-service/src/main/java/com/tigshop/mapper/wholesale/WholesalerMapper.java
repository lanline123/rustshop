// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.wholesale;

import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 批发商 Mapper
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Mapper
public interface WholesalerMapper extends BaseMapper<Wholesaler> {

    /**
     * 按 grade_id 分组统计批发商数量（仅未删除）。
     *
     * @param gradeIds 等级 ID 列表
     * @return [{gradeId, userCount}] 按 gradeId 分组；未命中的 gradeId 不会出现
     */
    List<Map<String, Object>> countByGradeIds(@Param("gradeIds") List<Integer> gradeIds);

    /**
     * 批量变更批发商等级（仅未删除）。预留给「批量改等级」业务使用。
     *
     * @param userIds    user_id 列表
     * @param gradeId    目标等级 ID
     * @param updateTime 更新时间（Unix 秒）
     * @return 受影响行数
     */
    int updateGradeByUserIds(@Param("userIds") List<Integer> userIds,
                             @Param("gradeId") Integer gradeId,
                             @Param("updateTime") Long updateTime);

    /**
     * 查询某等级下待移除的批发商（仅未删除）。
     *
     * <p>固定按 grade_id 过滤，确保只命中真正属于该等级的记录（跨等级误删保护）；
     * wholesalerIds 非空时再加 wholesaler_id IN 限定，为空/为 null 时表示该等级全部。
     *
     * @param gradeId       批发等级 ID
     * @param wholesalerIds 指定批发商 ID 列表，可空（空/null 表示该等级全部）
     * @return 命中的批发商记录（含 wholesaler_id、user_id）
     */
    List<Wholesaler> selectByGradeForRemove(@Param("gradeId") Integer gradeId,
                                            @Param("wholesalerIds") List<Integer> wholesalerIds);

    /**
     * 按等级软删批发商（is_delete=1 并更新 update_time，仅未删除）。
     *
     * <p>固定按 grade_id 过滤，跨等级误删保护；wholesalerIds 非空时再加 wholesaler_id IN 限定，
     * 为空/为 null 时软删该等级全部。
     *
     * @param gradeId       批发等级 ID
     * @param wholesalerIds 指定批发商 ID 列表，可空（空/null 表示该等级全部）
     * @param updateTime    更新时间（Unix 秒）
     * @return 受影响行数
     */
    int softDeleteByGrade(@Param("gradeId") Integer gradeId,
                          @Param("wholesalerIds") List<Integer> wholesalerIds,
                          @Param("updateTime") Long updateTime);

    /**
     * 按 wholesaler_id 查询待移除的批发商（仅未删除）。
     *
     * <p>不限定 grade_id，用于「按 ID 直接移除批发商身份」场景；已删除/不存在的 ID 不会命中。
     *
     * @param wholesalerIds 批发商 ID 列表（非空）
     * @return 命中的批发商记录（含 wholesaler_id、user_id）
     */
    List<Wholesaler> selectByIdsForRemove(@Param("wholesalerIds") List<Integer> wholesalerIds);

    /**
     * 按 wholesaler_id 软删批发商身份（is_delete=1 并更新 update_time，仅未删除）。
     *
     * @param wholesalerIds 批发商 ID 列表（非空）
     * @param updateTime    更新时间（Unix 秒）
     * @return 受影响行数
     */
    int softDeleteByIds(@Param("wholesalerIds") List<Integer> wholesalerIds,
                        @Param("updateTime") Long updateTime);

    /**
     * 按 user_id 查询批发商身份（<b>含软删行</b>，绕过 @TableLogic）。
     *
     * <p>用于「删后重加」复活判定：user_id 受唯一索引 uk_user_id 约束，至多一行。
     *
     * @param userId 用户 ID
     * @return 该用户的批发商记录（含已软删），无则 null
     */
    Wholesaler selectByUserIdIncludeDeleted(@Param("userId") Integer userId);

    /**
     * 按 user_id 列表批量查询批发商身份（<b>含软删行</b>，绕过 @TableLogic）。
     *
     * <p>用于批量导入复活判定。
     *
     * @param userIds 用户 ID 列表（非空）
     * @return 命中的批发商记录（含已软删）
     */
    List<Wholesaler> selectByUserIdsIncludeDeleted(@Param("userIds") List<Integer> userIds);

    /**
     * 复活并重置单个批发商身份（按 wholesaler_id）。
     *
     * <p>set is_delete=0、status=1、grade_id、source、remark、update_time；
     * 用于「软删后重新添加」场景，保留原 wholesaler_id 与历史。
     *
     * @param wholesalerId 批发商 ID
     * @param gradeId      新等级 ID
     * @param source       来源（1=注册自动 2=后台导入 3=手动添加）
     * @param remark       备注
     * @param updateTime   更新时间（Unix 秒）
     * @return 受影响行数
     */
    int reviveById(@Param("wholesalerId") Integer wholesalerId,
                   @Param("gradeId") Integer gradeId,
                   @Param("source") Integer source,
                   @Param("remark") String remark,
                   @Param("updateTime") Long updateTime);

    /**
     * 批量复活批发商身份（按 user_id，仅复活已软删行）。
     *
     * <p>set is_delete=0、status=1、grade_id、source、update_time（不动 remark）；用于批量导入复活。
     *
     * @param userIds    用户 ID 列表（非空）
     * @param gradeId    新等级 ID
     * @param source     来源
     * @param updateTime 更新时间（Unix 秒）
     * @return 受影响行数
     */
    int reviveByUserIds(@Param("userIds") List<Integer> userIds,
                        @Param("gradeId") Integer gradeId,
                        @Param("source") Integer source,
                        @Param("updateTime") Long updateTime);
}
