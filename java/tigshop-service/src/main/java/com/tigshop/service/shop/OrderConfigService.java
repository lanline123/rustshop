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

package com.tigshop.service.shop;

import cn.hutool.json.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.bean.dto.shop.OrderConfigCreateDTO;
import com.tigshop.bean.dto.shop.OrderConfigListDTO;
import com.tigshop.bean.dto.shop.OrderConfigUpdateDTO;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.vo.shop.OrderConfigVO;
import com.tigshop.service.common.BaseService;

/**
 * 订单配置服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderConfigService extends BaseService<OrderConfig> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<OrderConfigVO> list(OrderConfigListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderConfigVO detail(Integer id);

    /**
     * 获取店铺订单配置详情
     * @param shopId 店铺id
     * @param code 标识
     */
    JSON shopOrderConfigDetail(Integer shopId, String code);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderConfigCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderConfigUpdateDTO updateDTO);

    /**
     * 店铺结算设置
     * @param dto
     */
    void saveOrderConfig(OrderConfigDTO dto);
}
