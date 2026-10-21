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

package com.tigshop.service.finance;

import com.tigshop.bean.dto.finance.OrderFinanceAftersalesCreateDTO;
import com.tigshop.bean.dto.finance.OrderFinanceAftersalesListDTO;
import com.tigshop.bean.dto.finance.OrderFinanceAftersalesUpdateDTO;
import com.tigshop.bean.model.finance.OrderFinanceAftersales;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.finance.OrderFinanceAftersalesVO;
import com.tigshop.service.common.BaseService;

/**
 * 售后申请表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderFinanceAftersalesService extends BaseService<OrderFinanceAftersales> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<OrderFinanceAftersalesVO, OrderFinanceAftersalesListDTO> list(OrderFinanceAftersalesListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderFinanceAftersalesVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderFinanceAftersalesCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderFinanceAftersalesUpdateDTO updateDTO);

    /**
     * 根据id获取详情
     * @param id
     * @return OrderFinanceAftersales
     */
    OrderFinanceAftersales getInfoById(Integer id);
}
