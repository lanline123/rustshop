// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseMapper;
import com.tigshop.bean.model.promotion.Seckill;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 限时秒杀映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface SeckillMapper extends MPJBaseMapper<Seckill>, BaseMapper<Seckill> {

    /**
     * 秒杀列表
     */
    Page<Seckill> listForDecorate(@Param("page") Page<Seckill> page, @Param("currentTime") Long currentTime);
}
