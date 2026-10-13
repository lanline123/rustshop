package com.tigshop.service.user;

import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.service.common.BaseService;
/**
 * 用户第三方授权信息服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserAuthorizeService extends BaseService<UserAuthorize> {
    /**
     * 检查用户是否授权
     * @param userId
     * @param authorizeType
     * @return boolean
     */
    boolean checkUserIsAuthorize(int userId, int authorizeType);
}
