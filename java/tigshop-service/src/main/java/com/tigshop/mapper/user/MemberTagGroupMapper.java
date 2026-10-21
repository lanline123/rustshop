package com.tigshop.mapper.user;

import com.tigshop.bean.model.user.MemberTagGroup;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MemberTagGroupMapper extends BaseMapper<MemberTagGroup> {

    @Select("SELECT MAX(is_top) FROM member_tag_group WHERE is_del = 0 AND shop_id = #{shopId}")
    Integer selectMaxIsTop(@Param("shopId") Integer shopId);
}
