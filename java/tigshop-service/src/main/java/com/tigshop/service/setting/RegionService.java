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

package com.tigshop.service.setting;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.RegionCreateDTO;
import com.tigshop.bean.dto.setting.RegionUpdateDTO;
import com.tigshop.bean.dto.setting.RegionListDTO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.vo.setting.RegionListVO;
import com.tigshop.bean.vo.sys.CityRegionVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 地区列表服务
 *
 * @author Tigshop团队
 * @create 2024年11月11日 14:38
 */
public interface RegionService extends BaseService<Region> {
    /**
     * 根据父级id获取地区列表
     *
     * @param parentId 父级id
     * @return 地区列表
     */
    List<Region> getRegionByParentId(Integer parentId);


    /**
     * 根据地区id获取地区名称列表
     *
     * @param regionIds 地区id列表
     * @return 地区名称列表
     */
    List<String> getRegionNamesByRegionIds(List<Integer> regionIds);

    /**
     * 获取所有地区
     * @return Map
     */
    List<Tree<Integer>> getAllRegionTree();

    /**
     * 地区列表
     *
     * @param dto 地区列表参数
     * @return 地区列表
     */
    Page<RegionListVO> list(RegionListDTO dto);

    /**
     * 新增地区
     *
     * @param dto 地区参数
     * @return 是否成功
     */
    boolean create(RegionCreateDTO dto);

    /** 更新地区基础信息。 */
    boolean updateRegion(RegionUpdateDTO dto);

    List<List<Region>> getRegionByIds(String regionIds);

    /**
     * 获取省份列表
     * @return 省份列表
     */
    List<Region> getProvinceList();

    /**
     * 获取用户选择的地区
     * @return 地区
     */
    Region getUserRegion();

    /**
     * 获取所有城市
     * @return
     */
    CityRegionVO getCityRegion();

    /**
     * 根据城市名称获取城市列表
     * @return
     */
    List<Region> getCityListByCityName(String cityName);

    /**
     * 根据regionId获取完整的地区路径（从当前节点到根节点）
     * @param regionId 地区ID
     * @return 从根节点到当前节点的完整路径列表
     */
    List<Region> getRegionPath(Integer regionId);
}
