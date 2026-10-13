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

package com.tigshop.service.order;

import com.tigshop.bean.dto.order.AftersalesLogCreateDTO;
import com.tigshop.bean.dto.order.AftersalesLogListDTO;
import com.tigshop.bean.dto.order.AftersalesLogUpdateDTO;
import com.tigshop.bean.model.order.AftersalesLog;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.AftersalesLogVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 售后记录服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface AftersalesLogService extends BaseService<AftersalesLog> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<AftersalesLogVO, AftersalesLogListDTO> list(AftersalesLogListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    AftersalesLogVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(AftersalesLogCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(AftersalesLogUpdateDTO updateDTO);

    /**
     * 添加售后记录
     * @param aftersalesId 售后Id
     * @param logInfo 日志详情
     * @return boolean
     */
    boolean addAftersaleLog(Integer aftersalesId, String logInfo);

    /**
     * 添加售后记录（显式传入店铺/供应商信息，避免异步场景依赖请求头）
     *
     * @param aftersalesId 售后Id
     * @param logInfo 日志详情
     * @param shopId 店铺Id
     * @param vendorId 供应商Id
     * @return boolean
     */
    boolean addAftersaleLog(Integer aftersalesId, String logInfo, Integer shopId, Integer vendorId);

    /**
     * 以管理后台身份添加系统自动售后记录
     *
     * @param aftersalesId 售后Id
     * @param logInfo 日志详情
     * @param shopId 店铺Id
     * @param vendorId 供应商Id
     * @return boolean
     */
    boolean addSystemAftersaleLog(Integer aftersalesId, String logInfo, Integer shopId, Integer vendorId);

    /**
     * 获取售后日志记录
     * @param id 售后表主键id
     * @return List
     */
    List<AftersalesLogVO> getDetailLog(Integer id);
}
