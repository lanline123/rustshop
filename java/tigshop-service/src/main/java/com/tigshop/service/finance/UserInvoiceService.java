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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserInvoiceCreateDTO;
import com.tigshop.bean.dto.finance.UserInvoiceListDTO;
import com.tigshop.bean.dto.finance.UserInvoiceUpdateDTO;
import com.tigshop.bean.model.finance.UserInvoice;
import com.tigshop.bean.vo.finance.UserInvoiceVO;
import com.tigshop.service.common.BaseService;

/**
 * 发票资质服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserInvoiceService extends BaseService<UserInvoice> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<UserInvoiceVO> list(UserInvoiceListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    UserInvoiceVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(UserInvoiceCreateDTO createDTO);

    /**
     * 前端创建
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean clientCreate(UserInvoiceCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(UserInvoiceUpdateDTO updateDTO);

    /**
     * 前端更新
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean clientUpdate(UserInvoiceUpdateDTO updateDTO);

    /**
     * 获取当前用户增票资质信息
     * @return ItemVO
     */
    UserInvoiceVO getStatus();

    /**
     * 获取当前用户增票资质信息
     * @return ItemVO
     */
    UserInvoiceVO clientDetail();
}
