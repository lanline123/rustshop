// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.param.settings.config.LicenseSaveParam;
import com.tigshop.bean.param.settings.config.LicensedEditParam;
import com.tigshop.bean.param.settings.config.LicensedSaveParam;
import com.tigshop.bean.vo.setting.config.LicensedSettingsVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.TigshopApiClient;
import com.tigshop.service.authorization.AuthorizationLifecycleService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.LicensedService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Service
public class LicensedServiceImpl implements LicensedService {
    @Resource
    private ConfigService configService;

    @Resource
    private TigshopApiClient tigshopApiClient;

    @Resource
    private AuthorizationLifecycleService authorizationLifecycleService;

    @Override
    public LicensedSettingsVO getLicensedIndex() {
        LicensedSettingsVO settings = configService.getSettings(LicensedSettingsVO.class);
        settings.setLicense(StringUtils.hide(settings.getLicense(), 5, 15));
        return settings;
    }

    @Override
    public void update(LicensedEditParam param) {
        Map<String, Object> resMap = tigshopApiClient.updateLicense(param.getLicense(), param.getDomain(), "v2");
        Map<String, Object> data = (Map<String, Object>) resMap.get("data");
        Integer errcode = (Integer) data.get("errcode");
        Assert.isFalse(errcode > 0, () -> new GlobalException(String.valueOf(data.get("message"))));

        Map<String, Object> licensedMap = (Map<String, Object>) data.get("licensed");
        Assert.notNull(licensedMap, () -> new GlobalException("未获取到有效的授权信息"));
        licensedMap.put("license", param.getLicense());

        // 保存配置
        LicenseSaveParam saveParam = BeanUtil.copyProperties(licensedMap, LicenseSaveParam.class);
        configService.saveConfigSettings(saveParam);
        // 官网回源失败只记录告警，不回滚本地授权配置，也不阻止当前请求完成。
        authorizationLifecycleService.onAuthorizationChanged();
    }

    @Transactional
    @Override
    public void saveLicensed(LicensedSaveParam param) {
        configService.saveConfigSettings(param);
    }
}
