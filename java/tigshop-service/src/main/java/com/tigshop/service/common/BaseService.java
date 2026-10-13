// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.common;

import com.github.yulichang.base.MPJBaseService;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;

/**
 * @author Tigshop团队
 */
public interface BaseService<T> extends MPJBaseService<T> {

    /**
     * 更新字段
     * @param updateFieldDTO 更新字段参数
     * @param allowFields 允许更新的字段
     * @return 操作结果
     */
    boolean updateField(UpdateFieldDTO updateFieldDTO, String[] allowFields);

    /**
     * 批量操作
     * @param batchDTO 批量操作参数
     * @return 操作结果
     */
    boolean batch(BatchDTO batchDTO);

    /**
     * 删除通用方法
     * @param id id
     * @return boolean
     */
    boolean del(Integer id);
}
