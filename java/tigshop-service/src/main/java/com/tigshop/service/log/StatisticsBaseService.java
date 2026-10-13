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

package com.tigshop.service.log;

import com.tigshop.bean.dto.log.StatisticsBaseCreateDTO;
import com.tigshop.bean.model.log.StatisticsBase;
import com.tigshop.service.common.BaseService;

/**
 * 统计基础表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface StatisticsBaseService extends BaseService<StatisticsBase> {

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(StatisticsBaseCreateDTO createDTO);

    /**
     * 获取访问量或访客数
     *
     * @param data        时间范围，格式为["startDate", "endDate"]
     * @param accessFlag  是否返回浏览量（1：是，0：否）
     * @param productFlag 是否按商品统计（暂未使用）
     * @param shopId      店铺ID，默认为0
     * @return 访问量或访客数
     */
    Integer getVisitNum(String[] data, Integer accessFlag, Integer productFlag, Integer shopId);

}
