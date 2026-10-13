// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.CategoryDTO;
import com.tigshop.bean.dto.product.CategoryListDTO;
import com.tigshop.bean.dto.product.MoveCatDTO;
import com.tigshop.bean.model.product.Category;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 商品分类接口
 *
 * @author Jayce
 * @create 2024年10月31日 16:49
 */
public interface CategoryService extends BaseService<Category> {
    /**
     * 商品分类列表
     *
     * @param category 分类
     * @return ListResVO
     */
    Page<CategoryDTO> list(CategoryListDTO category);

    /**
     *
     * @param parentId
     * @return
     */
    String getParentName(Integer parentId);
    /**
     * 商品分类详情
     *
     * @param id 分类id
     * @return ItemVO
     */
    CategoryDTO detail(Integer id);

    /**
     * 创建商品分类
     *
     * @param category 分类信息
     */
    void create(CategoryDTO category);

    /**
     * 更新商品分类
     *
     * @param category 分类信息
     */
    void update(CategoryDTO category);

    /**
     * 获取所有商品分类
     */
    List<Tree<Integer>> getAllCategory();

    /**
     * 根据分类id获取子分类树
     *
     * @param categoryId 分类id
     * @return List<Tree < Integer>>
     */
    List<Tree<Integer>> getChildrenCategoryTreeById(Integer categoryId);

    /**
     * 获取热门分类
     *
     * @return List<Category>
     */
    List<Category> getHotCategory();

    /**
     * 获取所有子分类id (包括自身)
     *
     * @param categoryId 分类id
     * @return List<Integer>
     */
    List<Integer> getCategoryAllChildIds(Integer categoryId);

    /**
     * 根据父分类id获取子分类
     *
     * @param parentId 父分类id
     * @param size     获取数量
     * @return List<Category>
     */
    List<Category> getCategoryByParentId(Integer parentId, int size);

    List<Category> getCategoryByIds(List<Integer> ids);

    boolean moveCat(MoveCatDTO dto);

    /**
     * 根据分类名称组，获取最后一个分类主键
     * @param categoryNames 分类名称组
     * @param isAutoCat 是否自动创建分类
     */
    Integer getCategoryIds(String[] categoryNames, Integer isAutoCat);

    /**
     * 获取所有子分类id (包括自身)
     *
     * @param categoryId 分类id
     * @return List<Integer>
     */
    List<Integer> getRelatedCategoryIds(Integer categoryId);

    /**
     * 批量删除分类（事务）
     * @param ids 分类ID列表
     */
    void batchDel(List<Integer> ids);
}
