package com.tigshop.service.user.impl;


import com.tigshop.bean.model.user.UserFollow;
import com.tigshop.mapper.user.UserFollowMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserFollowService;
import org.springframework.stereotype.Service;

/**
* @author wzh
*/
@Service
public class UserFollowServiceImpl extends BaseServiceImpl<UserFollowMapper, UserFollow>
    implements UserFollowService {

}
