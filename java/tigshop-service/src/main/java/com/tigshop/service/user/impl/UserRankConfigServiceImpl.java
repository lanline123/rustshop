package com.tigshop.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.user.UserGrowConfig;
import com.tigshop.bean.enums.user.RankGrowthLogTypeEnum;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.vo.user.UserRankConfigVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.mapper.user.UserRankMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserRankConfigService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户等级配置
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Service
public class UserRankConfigServiceImpl extends BaseServiceImpl<UserRankConfigMapper, UserRankConfig> implements UserRankConfigService {
    @Override
    public UserRankConfig getUserRankConfig(String type) {
        LambdaQueryWrapper<UserRankConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRankConfig::getCode, type);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }

    @Override
    public UserRankConfigVO getUserRankConfigVO(String type) {
        LambdaQueryWrapper<UserRankConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRankConfig::getCode, type);
        queryWrapper.last("limit 1");
        UserRankConfig userRankConfig = getOne(queryWrapper);
        UserRankConfigVO userRankConfigVO = new UserRankConfigVO();
        BeanUtils.copyProperties(userRankConfig, userRankConfigVO);
        userRankConfigVO.setData(JsonUtil.fromJson(userRankConfig.getData(), JSONObject.class));
        return userRankConfigVO;
    }

    @Override
    public JSONObject getGrowConfig(String type) {
        LambdaQueryWrapper<UserRankConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRankConfig::getCode, type);
        queryWrapper.last("limit 1");
        return JsonUtil.fromJson(getOne(queryWrapper).getData(), JSONObject.class);
    }
}
