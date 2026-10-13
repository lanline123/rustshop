// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.order;

import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.order.AftersalesItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 售后商品映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface AftersalesItemMapper extends BaseMapper<AftersalesItem> {

    /**
     * 获取有效售后商品列表
     */
    List<AftersalesItem> validAftersalesItems(@Param("orderItemIds") List<Integer> orderItemIds);

}
