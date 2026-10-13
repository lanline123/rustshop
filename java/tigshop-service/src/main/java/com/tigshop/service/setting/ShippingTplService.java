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
import com.tigshop.bean.dto.setting.ShippingTplListDTO;
import com.tigshop.bean.dto.setting.ShippingTplUpdateDTO;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.param.settings.shippingtpl.ShippingTplSaveParam;
import com.tigshop.bean.vo.product.ProductShippingTplListVO;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTplVO;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTypeVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 运费模板服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShippingTplService extends BaseService<ShippingTpl> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ShippingTplVO> list(ShippingTplListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ShippingTplVO detail(Integer id);

    /**
     * 创建
     */
    void create(ShippingTplSaveParam param);

    /**
     * 更新
     */
    void update(ShippingTplUpdateDTO updateDTO);

    /**
     * 初始化运费模板
     */
    void initShippingTpl(Integer shopId);

    /**
     * 获取运费模板
     */
    List<ShippingTypeVO> getShippingTypeListByShopId();

    /**
     * 获取运费模板
     * @return ShippingTplListVO
     */
    List<ProductShippingTplListVO> getShippingTplListByShopId();

    /**
     * 获取默认运费模板
     *
     * @return ID
     */
    Long getDefaultShippingTplId(Integer shopId);

    /**
     * 设置默认运费模板
     *
     * @param id 运费模板ID
     */
    void setDefault(Long id);
}
