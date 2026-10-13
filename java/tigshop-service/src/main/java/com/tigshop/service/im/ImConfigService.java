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

package com.tigshop.service.im;

import com.tigshop.bean.dto.im.ConfigCreateDTO;
import com.tigshop.bean.dto.im.ConfigDataDTO;
import com.tigshop.bean.dto.im.ConfigListDTO;
import com.tigshop.bean.dto.im.ConfigUpdateDTO;
import com.tigshop.bean.model.im.Config;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.im.ConfigVO;
import com.tigshop.service.common.BaseService;

/**
 * im配置服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ImConfigService extends BaseService<Config> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<ConfigVO, ConfigListDTO> list(ConfigListDTO listDTO);

    /**
     * 详情
     *
     * @param code 配置名称
     * @param shopId 店铺id
     * @return ItemVO
     */
    ConfigDataDTO detail(String code, Integer shopId);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ConfigCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ConfigUpdateDTO updateDTO);
}
