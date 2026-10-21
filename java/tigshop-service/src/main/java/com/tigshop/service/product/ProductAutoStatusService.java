// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product;

import com.tigshop.bean.dto.product.ProductAutoStatusCreateDTO;
import com.tigshop.bean.model.product.ProductAutoStatus;
import com.tigshop.service.common.BaseService;

/**
 * 商品自动上下架服务接口
 *
 * @author Tigshop团队
 * @create 2025年12月2日
 */
public interface ProductAutoStatusService extends BaseService<ProductAutoStatus> {


    /**
     * 创建商品自动上下架任务
     *
     * @param dto 创建参数
     * @return 是否成功
     */
    boolean create(ProductAutoStatusCreateDTO dto);


    /**
     * 执行自动上下架任务
     * 定时任务调用此方法
     */
    void executeAutoStatusTasks();


    /**
     * 取消商品的自动上下架任务（指定门店）
     *
     * @param productId 商品ID
     * @param shopId 门店ID：0-平台商品，>0-门店商品
     * @return 是否成功
     */
    boolean cancelProductTasks(Integer productId, Integer shopId);


    void doExecuteTask(ProductAutoStatus task);
}

