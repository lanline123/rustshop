package com.tigshop.service.user;

import com.tigshop.bean.model.promotion.Sign;
import com.tigshop.bean.vo.user.UserSignInfoVo;
import com.tigshop.service.common.BaseService;

/**
 * @author wzh
 */
public interface UserSignService extends BaseService<Sign> {


    /**
     * 获取用户签到信息
     * @return 用户签到信息
     */
    UserSignInfoVo getUserSignInfoVo();

    /**
     * 签到
     */
    void sign();
}
