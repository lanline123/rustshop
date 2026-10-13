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

package com.tigshop.service.setting;

import com.tigshop.bean.param.settings.config.LicensedEditParam;
import com.tigshop.bean.param.settings.config.LicensedSaveParam;
import com.tigshop.bean.vo.setting.config.LicensedSettingsVO;

/**
 * 软件许可服务
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:08
 */
public interface LicensedService {

    /**
     * 授权信息
     */
    LicensedSettingsVO getLicensedIndex();

    /**
     * 更新授权
     */
    void update(LicensedEditParam param);

    /**
     * 更新授权
     */
    void saveLicensed(LicensedSaveParam param);
}
