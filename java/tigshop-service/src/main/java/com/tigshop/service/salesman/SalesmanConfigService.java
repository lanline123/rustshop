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

package com.tigshop.service.salesman;

import com.tigshop.bean.param.salesman.SalesmanConfigSaveParam;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.service.common.BaseService;

/**
 * 分销模式设置服务
 *
 * @author kidd
 * @since 2023-07-05
 */
public interface SalesmanConfigService extends BaseService<SalesmanConfig> {

    /**
     * 详情
     */
    SalesmanConfigVO detail(String code);

    /**
     * 更新
     */
    void save(String code, SalesmanConfigSaveParam param);
}
