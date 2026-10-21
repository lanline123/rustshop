package com.tigshop.service.shop;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryCreateDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryListDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryUpdateDTO;
import com.tigshop.bean.model.shop.ShopProductCategory;
import com.tigshop.bean.vo.shop.ShopParentTreeVO;
import com.tigshop.bean.vo.shop.ShopProductCategoryVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 店铺产品分类表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShopProductCategoryService extends BaseService<ShopProductCategory> {

    /**
     * 获取店铺产品分类树
     * @param shopId 店铺id
     * @return List
     */
    List<Tree<Integer>> tree(Integer shopId);

    List<Tree<Integer>> tree(Integer categoryId, Integer shopId);

    /**
     * 获取父级分类
     * @param id 父级分类id
     * @return List
     */
    List<ShopParentTreeVO> parentTree(Integer id);

    /**
     * 获取列表
     *
     * @param listDTO listDTO
     * @return ListResVO
     */
    Page<ShopProductCategoryVO> list(ShopProductCategoryListDTO listDTO);

    /**
     * 获取详情
     */
    ShopProductCategoryVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ShopProductCategoryCreateDTO createDTO);

    /**
     * 更新
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ShopProductCategoryUpdateDTO updateDTO);

    /**
     * 删除
     * @param id 主键
     * @param shopId 店铺id
     * @return boolean
     */
    boolean del(Integer id, Integer shopId);

    /**
     * 批量操作
     * @param batchDTO 批量操作的参数
     * @param shopId 店铺id
     * @return boolean
     */
    boolean batch(BatchDTO batchDTO, Integer shopId);

    /**
     * 更新字段
     * @param updateField updateField
     * @param fieldId 主键字段
     * @param allowFields 允许更新的字段
     * @param shopId 店铺id
     * @return boolean
     */
    boolean updateField(UpdateFieldDTO updateField, String fieldId, String[] allowFields, Integer shopId);

    /**
     * 根据父级分类id获取分类
     * @param parentId 父级分类id
     * @param shopId 店铺id
     * @return List
     */
    List<ShopProductCategory> getCategoryByParentId(Integer parentId, Integer shopId);

    /**
     * 根据分类id获取所有子分类id
     * @param categoryId
     * @param shopId
     * @return
     */
    List<Integer> getCategoryAllChildIds(Integer categoryId, Integer shopId);
}
