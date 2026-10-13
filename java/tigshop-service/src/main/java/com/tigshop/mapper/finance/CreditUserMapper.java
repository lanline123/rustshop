package com.tigshop.mapper.finance;

import com.tigshop.bean.model.finance.CreditUser;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 授信会员Mapper
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
public interface CreditUserMapper extends BaseMapper<CreditUser> {

    /**
     * 根据用户ID查询授信会员
     *
     * @param userId 用户ID
     * @return 授信会员
     */
    @Select("SELECT * FROM credit_user WHERE user_id = #{userId}")
    CreditUser selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID加锁查询授信会员（用于并发控制）
     *
     * @param userId 用户ID
     * @return 授信会员
     */
    @Select("SELECT * FROM credit_user WHERE user_id = #{userId} FOR UPDATE")
    CreditUser selectByUserIdForUpdate(@Param("userId") Integer userId);
}
