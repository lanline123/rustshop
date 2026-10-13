package com.tigshop.service.user;

import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.vo.user.UserRankConfigVO;
import com.tigshop.service.common.BaseService;

/**
 * 用户等级配置
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
public interface UserRankConfigService extends BaseService<UserRankConfig> {
    /**
     * 根据类型获取用户等级配置
     *
     * @param type 类型
     * @return UserRankConfig
     */
    UserRankConfig getUserRankConfig(String type);

    /**
     * 根据类型获取用户等级配置
     *
     * @param type 类型
     * @return UserRankConfigVO
     */
    UserRankConfigVO getUserRankConfigVO(String type);

    /**
     * 获取成长值配置
     *
     * @return JSONObject
     */
    JSONObject getGrowConfig(String type);

}
