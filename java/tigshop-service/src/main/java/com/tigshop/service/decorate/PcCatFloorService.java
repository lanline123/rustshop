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
import com.tigshop.bean.dto.decorate.PcCatFloorCreateDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorListDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorUpdateDTO;
import com.tigshop.bean.model.decorate.PcCatFloor;
import com.tigshop.bean.vo.decorate.PcCatFloorListVO;
import com.tigshop.bean.vo.decorate.PcCatFloorVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 首页分类栏服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PcCatFloorService extends BaseService<PcCatFloor> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<PcCatFloorVO> list(PcCatFloorListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    PcCatFloorVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     */
    void create(PcCatFloorCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     */
    void update(PcCatFloorUpdateDTO updateDTO);

    List<PcCatFloorListVO> getCatFloor();
}
