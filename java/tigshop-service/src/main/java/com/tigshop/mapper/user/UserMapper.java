package com.tigshop.mapper.user;

import com.tigshop.bean.model.user.User;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {

    User selectForUpdateById(@Param("userId") Integer userId);
}

