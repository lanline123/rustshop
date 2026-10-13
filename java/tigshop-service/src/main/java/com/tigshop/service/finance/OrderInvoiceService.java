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
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceSaveParam;
import com.tigshop.bean.query.finance.orderinvoice.OrderInvoicePageQuery;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceEditParam;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.vo.finance.OrderInvoiceDetailVO;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.service.common.BaseService;

/**
 * 发票申请服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderInvoiceService extends BaseService<OrderInvoice> {

    /**
     * 列表
     */
    Page<OrderInvoiceVO> list(OrderInvoicePageQuery query);

    /**
     * 详情
     */
    OrderInvoiceVO detail(Integer id);

    /**
     * 创建
     */
    void create(OrderInvoiceSaveParam param);

    /**
     * 更新
     */
    void update(OrderInvoiceEditParam param);

    /**
     * 客户端发票更新
     *
     * @param updateDTO
     * @return
     */
    void clientUpdate(OrderInvoiceEditParam updateDTO);

    /**
     * 客户端发票创建
     */
    void clientCreate(OrderInvoiceSaveParam createDTO);

    /**
     * 前台详情
     *
     * @param id 发票id
     * @return ItemVO
     */
    OrderInvoiceDetailVO clientDetail(Integer id);
}
