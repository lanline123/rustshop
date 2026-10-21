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

import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.bean.param.settings.shippingtpl.ShippingTypeParam;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.setting.ShippingTplInfoVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 运费模板内容服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShippingTplInfoService extends BaseService<ShippingTplInfo> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<ShippingTplInfoVO, ShippingTplInfoListDTO> list(ShippingTplInfoListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ShippingTplInfoVO detail(Integer id);

    /**
     * 创建
     * @return boolean
     */
    boolean create(ShippingTplInfoCreateDTO createDTO);

    /**
     * 更新
     */
    boolean update(ShippingTplInfoUpdateDTO updateDTO);

    /**
     * 保存运费模板信息
     */
    void saveShippingTplInfo(List<ShippingTypeParam> shippingTypeParams, ShippingTpl shippingTpl);

    /**
     * 更新运费模板信息
     */
    void updateShippingTplInfo(List<ShippingTypeParam> shippingTypeParams, ShippingTpl shippingTpl);

    /**
     * 根据运费模板id获取运费模板信息
     * @param shippingTplId 运费模板id
     * @return List
     */
    List<ShippingTplInfo> getShippingTplInfoByShippingTplId(Integer shippingTplId);


    /**
     * 验证用户收货地址
     * @param shippingTplId 配送模板 id
     * @param addressId 用户收货地址id
     */
    boolean verifyUserAddress(Integer shippingTplId, Integer addressId);
}
