package com.tigshop.service.common.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.common.TipsVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.common.TipsManageService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 提示管理接口实现
 *
 * @author kidd
 * @since 2025/6/19 13:45
 */
@RequiredArgsConstructor
@Service
public class TipsManageServiceImpl implements TipsManageService {

    private final RedisCache redisCache;
    private final ConfigService configService;

    @Override
    public List<TipsVO> list(String url) {

        // 判断后台域名和pc的域名不一致则返回 false
        String pcDomain = configService.getConfigVal(SettingsEnum.PC_DOMAIN);
        boolean domainBindStatus = StrUtil.contains(url, pcDomain);
        TipsVO domainBindTips = new TipsVO("domainBind", domainBindStatus);

        Integer adminId = SecurityUtils.getCurrentAdminId();
        String passwordTooSimple = redisCache.getCacheObject(Constants.PASSWORD_TOO_SIMPLE + adminId);
        TipsVO passwordTooSimpleTips = new TipsVO("passwordTooSimple", passwordTooSimple != null);

        return new ArrayList<>() {
            {
                add(domainBindTips);
                add(passwordTooSimpleTips);
            }
        };
    }

}
