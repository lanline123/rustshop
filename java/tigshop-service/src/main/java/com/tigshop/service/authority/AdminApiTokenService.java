package com.tigshop.service.authority;

import com.tigshop.bean.model.authority.AdminApiToken;
import com.tigshop.bean.vo.authority.AdminApiTokenGenerateVO;
import com.tigshop.bean.vo.authority.AdminApiTokenVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

public interface AdminApiTokenService extends BaseService<AdminApiToken> {

    /**
     * 生成API Token
     *
     * @param adminId       管理员ID
     * @param name          Token名称/备注
     * @param expiresInDays 过期天数，null表示永不过期
     * @return 包含明文Token的响应（仅此一次）
     */
    AdminApiTokenGenerateVO generateToken(Integer adminId, String name, Integer expiresInDays);

    /**
     * 列出管理员的所有API Token
     *
     * @param adminId 管理员ID
     * @return Token列表（不含明文）
     */
    List<AdminApiTokenVO> listTokens(Integer adminId);

    /**
     * 吊销API Token
     *
     * @param tokenId Token ID
     * @param adminId 管理员ID（校验归属）
     */
    void revokeToken(Integer tokenId, Integer adminId);

    /**
     * 校验API Token，返回管理员用户名
     *
     * @param plainToken 明文Token
     * @return 管理员用户名，无效则返回null
     */
    String validateToken(String plainToken);
}
