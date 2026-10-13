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

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.PcNavigationCreateDTO;
import com.tigshop.bean.dto.decorate.PcNavigationListDTO;
import com.tigshop.bean.dto.decorate.PcNavigationUpdateDTO;
import com.tigshop.bean.model.decorate.PcNavigation;
import com.tigshop.bean.vo.common.NavVO;
import com.tigshop.bean.vo.decorate.PcNavigationVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 首页分类栏服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PcNavigationService extends BaseService<PcNavigation> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<PcNavigationVO> list(PcNavigationListDTO listDTO);

    /**
     * 详情
     */
    PcNavigationVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(PcNavigationCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(PcNavigationUpdateDTO updateDTO);

    /**
     * 获取所有导航
     * @param type 导航类型
     * @return List<Tree<Integer>>
     */
    NavVO getAllNav(Integer type);

    /**
     * 选择链接地址
     * @return map
     */
    List<Map<String, String>> selectLink();


    /**
     * 获取上级导航
     * @param type  导航类型
     * @return 导航信息
     */
    List<Tree<Integer>> getParentNav(Integer type, Integer parentId);
}
