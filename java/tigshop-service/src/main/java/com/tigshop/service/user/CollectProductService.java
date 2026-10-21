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

package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.CollectProductListDTO;
import com.tigshop.bean.dto.user.CollectProductSaveDTO;
import com.tigshop.bean.dto.user.CollectProductUpdateDTO;
import com.tigshop.bean.model.user.CollectProduct;
import com.tigshop.bean.query.user.CollectProductCancelParam;
import com.tigshop.bean.vo.user.CollectProductVO;
import com.tigshop.service.common.BaseService;

/**
 * 商品收藏服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface CollectProductService extends BaseService<CollectProduct> {
    /**
     * 列表
     */
    Page<CollectProductVO> list(CollectProductListDTO listDTO);

    /**
     * 详情
     */
    CollectProductVO detail(Integer id);

    /**
     * 创建
     */
    void create(CollectProductSaveDTO saveDTO);

    /**
     * 更新
     */
    void update(CollectProductUpdateDTO updateDTO);

    /**
     * 取消
     */
    void cancel(CollectProductCancelParam param);
}
