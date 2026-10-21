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

package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ServicesDTO;
import com.tigshop.bean.model.product.Services;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.common.BaseService;

/**
 * 商品服务服务
 *
 * @author Jayce
 * @create 2024年11月06日 13:51
 */
public interface ServicesService extends BaseService<Services> {
    /**
     * 获取商品服务列表
     *
     * @param basePage 分页参数
     * @return 商品服务列表
     */
    Page<ServicesDTO> list(BasePage basePage);

    /**
     * 创建商品服务
     *
     * @param servicesDTO 商品服务
     * @return 是否创建成功
     */
    boolean create(ServicesDTO servicesDTO);

    /**
     * 更新商品服务
     *
     * @param servicesDTO 商品服务
     * @return 是否更新成功
     */
    boolean update(ServicesDTO servicesDTO);

    /**
     * 获取商品服务详情
     *
     * @param id 商品服务ID
     * @return 商品服务详情
     */
    ServicesDTO detail(Integer id);
}
