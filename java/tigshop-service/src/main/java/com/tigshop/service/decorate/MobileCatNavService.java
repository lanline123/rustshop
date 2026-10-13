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

package com.tigshop.service.decorate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.MobileCatNavCreateDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavListDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavUpdateDTO;
import com.tigshop.bean.model.decorate.MobileCatNav;
import com.tigshop.bean.vo.decorate.MobileCatNavVO;
import com.tigshop.service.common.BaseService;

/**
 * 首页分类栏服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface MobileCatNavService extends BaseService<MobileCatNav> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<MobileCatNavVO> list(MobileCatNavListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    MobileCatNavVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(MobileCatNavCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(MobileCatNavUpdateDTO updateDTO);
}
