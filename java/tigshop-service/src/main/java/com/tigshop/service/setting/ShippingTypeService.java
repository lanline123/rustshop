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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.ShippingTypeCreateDTO;
import com.tigshop.bean.dto.setting.ShippingTypeListDTO;
import com.tigshop.bean.dto.setting.ShippingTypeUpdateDTO;
import com.tigshop.bean.model.setting.ShippingType;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTypeVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 配送类型服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShippingTypeService extends BaseService<ShippingType> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ShippingTypeVO> list(ShippingTypeListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ShippingTypeVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ShippingTypeCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ShippingTypeUpdateDTO updateDTO);

    /**
     * 获取配送类型列表
     */
    List<ShippingTypeVO> getShippingTypeListByShopId(Integer shopId);
}
