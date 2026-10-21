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

package com.tigshop.service.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.ImServantCreateDTO;
import com.tigshop.bean.dto.im.ImServantListDTO;
import com.tigshop.bean.dto.im.ImServantUpdateDTO;
import com.tigshop.bean.dto.im.ServantModifyStatusDTO;
import com.tigshop.bean.model.im.ImServant;
import com.tigshop.bean.vo.im.ImServantVO;
import com.tigshop.service.common.BaseService;

/**
 * 客服服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ImServantService extends BaseService<ImServant> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ImServantVO> list(ImServantListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ImServantVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ImServantCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ImServantUpdateDTO updateDTO);

    /**
     * 修改状态
     *
     * @param dto 修改状态参数
     * @return boolean
     */
    boolean modifyStatus(ServantModifyStatusDTO dto);

    /**
     * 获取客服状态
     *
     * @param servantId 客服ID
     * @param shopId    店铺ID
     * @return ImServantVO
     */
    ImServantVO getStatus(Integer servantId, Integer shopId);
}
