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
import com.tigshop.bean.dto.product.AttributesTplDTO;
import com.tigshop.bean.model.product.AttributesTpl;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 商品属性模板服务
 *
 * @author Jayce
 * @create 2024年11月04日 17:15
 */
public interface AttributesTplService extends BaseService<AttributesTpl> {
    boolean create(AttributesTplDTO dto);

    boolean update(AttributesTplDTO dto);

    Page<AttributesTplDTO> list(BasePage page);

    AttributesTplDTO detail(Integer id);

    List<AttributesTplDTO> listByCategory(Integer categoryId);
}
