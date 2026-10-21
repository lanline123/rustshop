package com.tigshop.service.shop.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryCreateDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryListDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryUpdateDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.ShopProductCategory;
import com.tigshop.bean.vo.shop.ShopParentTreeVO;
import com.tigshop.bean.vo.shop.ShopProductCategoryVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.shop.ShopProductCategoryMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.shop.ShopProductCategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.ResultTextConstants.INVALID_FIELD_VALUE;
import static com.tigshop.common.constant.shop.ShopProductCategoryConstants.CATEGORY_NOT_EXIST;
import static com.tigshop.common.constant.shop.ShopProductCategoryConstants.SHOP_NOT_NULL;

/**
 * 店铺产品分类表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ShopProductCategoryServiceImpl extends BaseServiceImpl<ShopProductCategoryMapper, ShopProductCategory>
        implements ShopProductCategoryService {
    @Resource
    private AdminLogService adminLogService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    TranslatePackageImpl translatePackage;
    @Resource
    MessageHelper messageHelper;

    @Override
    public List<Tree<Integer>> tree(Integer shopId) {
        if (shopId == null) {
            throw new GlobalException(messageHelper.get("error.shop.select.required"));
        }

        LambdaQueryWrapper<ShopProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopProductCategory::getShopId, shopId);
        queryWrapper.eq(ShopProductCategory::getIsShow, 1);
        queryWrapper.orderByAsc(ShopProductCategory::getParentId);
        queryWrapper.orderByAsc(ShopProductCategory::getSortOrder);
        queryWrapper.orderByAsc(ShopProductCategory::getCategoryId);
        List<ShopProductCategory> shopProductCategories = this.list(queryWrapper);

        return convertTreeList(shopProductCategories, 0);
    }

    @Override
    public List<Tree<Integer>> tree(Integer categoryId, Integer shopId) {
        if (shopId == null) {
            throw new GlobalException(messageHelper.get("error.shop.select.required"));
        }

        LambdaQueryWrapper<ShopProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null && categoryId != 0, ShopProductCategory::getParentId, categoryId);
        queryWrapper.eq(ShopProductCategory::getShopId, shopId);
        queryWrapper.eq(ShopProductCategory::getIsShow, 1);
        queryWrapper.orderByAsc(ShopProductCategory::getParentId);
        queryWrapper.orderByAsc(ShopProductCategory::getSortOrder);
        queryWrapper.orderByAsc(ShopProductCategory::getCategoryId);
        List<ShopProductCategory> shopProductCategories = this.list(queryWrapper);

        return convertTreeList(shopProductCategories, categoryId);
    }

    @Override
    public List<ShopParentTreeVO> parentTree(Integer id) {
        List<ShopParentTreeVO> result = new ArrayList<>();
        Integer parentId = id;
        while (parentId > 0) {
            ShopProductCategory category = this.getById(parentId);
            if (category != null) {
                parentId = category.getParentId();
                ShopParentTreeVO vo = new ShopParentTreeVO();
                vo.setCategoryId(category.getCategoryId());
                vo.setParentId(category.getParentId());
                vo.setCategoryName(category.getCategoryName());
                result.add(vo);
            } else {
                parentId = 0;
            }
            if (result.size() > 5) {
                break;
            }
        }
        result = new ArrayList<>(result);
        java.util.Collections.reverse(result);

        for (ShopParentTreeVO vo : result) {
            LambdaQueryWrapper<ShopProductCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShopProductCategory::getParentId, vo.getParentId());
            queryWrapper.eq(ShopProductCategory::getIsShow, 1);
            queryWrapper.orderByAsc(ShopProductCategory::getSortOrder);
            List<ShopProductCategory> siblings = this.list(queryWrapper);
            List<ShopParentTreeVO> siblingVOList = new ArrayList<>();
            for (ShopProductCategory sibling : siblings) {
                ShopParentTreeVO siblingVO = new ShopParentTreeVO();
                siblingVO.setCategoryId(sibling.getCategoryId());
                siblingVO.setParentId(sibling.getParentId());
                siblingVO.setCategoryName(sibling.getCategoryName());
                siblingVOList.add(siblingVO);
            }
            vo.setCatList(siblingVOList);
        }
        return result;

    }

    @Override
    public Page<ShopProductCategoryVO> list(ShopProductCategoryListDTO listDTO) {
        // 分页
        Page<ShopProductCategory> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ShopProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        queryWrapper.eq(listDTO.getParentId() != null, ShopProductCategory::getParentId, listDTO.getParentId());

        if (listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            queryWrapper.eq(ShopProductCategory::getShopId, listDTO.getShopId());
        }

        if (listDTO.getKeyword() != null && StringUtils.isEmpty(listDTO.getKeyword())) {
            queryWrapper.like(ShopProductCategory::getCategoryName, listDTO.getKeyword());
        }

        // 执行查询
        Page<ShopProductCategory> shopPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShopProductCategory> shopPageRecords = shopPage.getRecords();
        // 转换为VO
        List<ShopProductCategoryVO> shopVOList = shopPageRecords.stream()
                .map(shopProductCategory -> {
                    ShopProductCategoryVO shopProductCategoryVO = new ShopProductCategoryVO();
                    BeanUtils.copyProperties(shopProductCategory, shopProductCategoryVO);
                    shopProductCategoryVO.setAddTime(TigUtils.handelTime(shopProductCategory.getAddTime()));
                    shopProductCategoryVO.setHasChildren(lambdaQuery().eq(ShopProductCategory::getParentId, shopProductCategory.getCategoryId()).count() > 0 ? 1 : 0);
                    return shopProductCategoryVO;
                })
                .toList();
        return PageUtil.convertPage(shopPage, shopVOList);
    }

    @Override
    public ShopProductCategoryVO detail(Integer id) {
        ShopProductCategory shopProductCategory = this.getById(id);
        if (shopProductCategory == null) {
            throw new GlobalException(messageHelper.get("error.category.not.exist"));
        }
        ShopProductCategoryVO shopProductCategoryVO = new ShopProductCategoryVO();
        BeanUtils.copyProperties(shopProductCategory, shopProductCategoryVO);
        shopProductCategoryVO.setAddTime(TigUtils.handelTime(shopProductCategory.getAddTime()));
        return shopProductCategoryVO;
    }

    @Override
    public boolean create(ShopProductCategoryCreateDTO createDTO) {
        if (createDTO == null) {
            throw new GlobalException("参数错误");
        }
        // 获取当前期望创建的分类的父类
        Integer parentId = createDTO.getParentId();
        // 判断祖类是否大于0，大于则提示不允许
        ShopProductCategory sp = this.getById(parentId);
        if (sp != null) {
            if (sp.getParentId() != null && sp.getParentId() > 0) {
                throw new GlobalException("只能创建二级分类");
            }
        }
        ShopProductCategory shopProductCategory = new ShopProductCategory();
        BeanUtils.copyProperties(createDTO, shopProductCategory);
        shopProductCategory.setAddTime(StringUtils.getCurrentTime());
        boolean res = this.save(shopProductCategory);
        if (res) {
            adminLogService.createByLogInfo("新增分类：" + createDTO.getCategoryName());
        }
        return res;
    }

    @Override
    public boolean update(ShopProductCategoryUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShopProductCategory sp = this.getById(updateDTO.getParentId());
            if (sp != null) {
                if (sp.getParentId() != null && sp.getParentId() > 0) {
                    throw new GlobalException("只能修改为二级分类");
                }
            }
            ShopProductCategory shopProductCategory = new ShopProductCategory();
            BeanUtils.copyProperties(updateDTO, shopProductCategory);
            boolean res = this.updateById(shopProductCategory);
            if (res) {
                adminLogService.createByLogInfo("修改分类：" + updateDTO.getCategoryName());
            }
            return res;
        }
        return false;
    }

    @Override
    public boolean del(Integer id, Integer shopId) {
        if (id != null) {
            delHandle(shopId, id);
        }
        return false;
    }

    @Override
    public boolean batch(BatchDTO batchDTO, Integer shopId) {
        if ("del".equals(batchDTO.getType())) {
            //循环batchDTO.getIds()
            for (Integer id : batchDTO.getIds()) {
                delHandle(shopId, id);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String fieldId, String[] allowFields, Integer shopId) {
        // 校验字段名
        String field = updateField.getField();
        // 转换为下划线格式
        field = StrUtil.toUnderlineCase(field);
        if (!Arrays.asList(allowFields).contains(field)) {
            throw new GlobalException(messageHelper.get("error.field.value.invalid"));
        }
        ShopProductCategory shopProductCategory = this.getById(updateField.getId());
        if (!Objects.equals(shopProductCategory.getShopId(), shopId)) {
            throw new GlobalException(messageHelper.get("error.category.not.exist"));
        }

        // 构造更新条件
        UpdateWrapper<ShopProductCategory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(fieldId, updateField.getId())
                .set(field, updateField.getVal());
        return this.update(updateWrapper);
    }

    @Override
    public List<ShopProductCategory> getCategoryByParentId(Integer parentId, Integer shopId) {
        LambdaQueryWrapper<ShopProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopProductCategory::getParentId, parentId)
                .eq(ShopProductCategory::getShopId, shopId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Integer> getCategoryAllChildIds(Integer categoryId, Integer shopId) {
        List<ShopProductCategory> allCategories = this.list(new LambdaQueryWrapper<ShopProductCategory>().eq(ShopProductCategory::getShopId, shopId));

        // 构建 parentId -> children 列表的 map
        Map<Integer, List<Integer>> parentToChildren = allCategories.stream()
                .collect(Collectors.groupingBy(
                        ShopProductCategory::getParentId,
                        Collectors.mapping(ShopProductCategory::getCategoryId, Collectors.toList())
                ));

        // 用 DFS 查找所有子分类
        List<Integer> result = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(categoryId);

        while (!stack.isEmpty()) {
            Integer current = stack.pop();
            result.add(current);

            List<Integer> children = parentToChildren.get(current);
            if (children != null) {
                for (Integer childId : children) {
                    stack.push(childId);
                }
            }
        }

        return result;
    }

    private void delHandle(Integer shopId, Integer id) {
        ShopProductCategory shopProductCategory = this.getById(id);
        if (!Objects.equals(shopProductCategory.getShopId(), shopId)) {
            throw new GlobalException(messageHelper.get("error.category.not.exist"));
        }
        boolean res = this.removeById(id);
        if (res) {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getShopCategoryId, id);
            List<Product> products = productMapper.selectList(queryWrapper);
            if (!products.isEmpty()) {
                for (Product product : products) {
                    product.setShopCategoryId(0);
                    productMapper.updateById(product);
                }
            }
        }
    }

    public List<Tree<Integer>> convertTreeList(List<ShopProductCategory> shopProductCategories, Integer rootId) {
        if (shopProductCategories.isEmpty()) {
            return null;
        }
        // 构建树形结构
        TreeNodeConfig config = new TreeNodeConfig();
        //默认id，可以不设置
        config.setIdKey("categoryId");
        //父id
        config.setParentIdKey("parentId");
        //分类名称
        config.setNameKey("categoryName");
        //最大递归深度
        config.setDeep(5);
        return TreeUtil.build(shopProductCategories, rootId, config, (object, treeNode) -> {
            treeNode.putExtra("categoryId", object.getCategoryId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("categoryName", translatePackage.translate(object.getCategoryName(),1));
            treeNode.putExtra("categoryPic", object.getCategoryPic());
            treeNode.putExtra("isStore", 1);
        });
    }
}
