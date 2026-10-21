// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.CategoryDTO;
import com.tigshop.bean.dto.product.CategoryListDTO;
import com.tigshop.bean.dto.product.MoveCatDTO;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SeoIdentificationUtil;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.utils.TransactionAfterCommitExecutor;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.CategoryTreeCache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.CAT_FLOOR;
import static com.tigshop.common.constant.Constants.SEO_LINK_CATEGORY;

/**
 * 商品分类接口实现类
 *
 * @author Tigshop团队
 * @create 2024年10月31日 17:00
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends BaseServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;
    private final TranslatePackageImpl translatePackage;
    private final MessageHelper messageHelper;
    private final RedisCache redisCache;
    private final TigshopProperties tigshopProperties;
    private final CategoryTreeCache categoryTreeCache;



    @Override
    public Page<CategoryDTO> list(CategoryListDTO dto) {
        // 分页参数
        Page<Category> page = new Page<>(dto.getPage(), dto.getSize());

        // 创建查询构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        // 获取parentId，并检查是否为空。
        Integer parentId = dto.getParentId();

        if (parentId != null && parentId >= 0) {
            queryWrapper.eq(Category::getParentId, parentId);
        }

        // 排序字段
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());

        // 分页查询
        Page<Category> categoryPage = this.page(page, queryWrapper);

        // 转换记录为 DTO
        Map<Integer, Long> childCountMap = countChildrenByParentIds(
                categoryPage.getRecords().stream().map(Category::getCategoryId).collect(Collectors.toSet())
        );
        List<CategoryDTO> recordsDTO = categoryPage.getRecords().stream()
                .map(category -> convertToDTO(category, childCountMap))
                .toList();

        return PageUtil.convertPage(categoryPage, recordsDTO);
    }

    /**
     * 批量统计各分类的直接子分类数量，避免 list 分页 N+1。
     */
    private Map<Integer, Long> countChildrenByParentIds(Set<Integer> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Map.of();
        }
        return this.lambdaQuery()
                .in(Category::getParentId, parentIds)
                .select(Category::getParentId)
                .list()
                .stream()
                .collect(Collectors.groupingBy(Category::getParentId, Collectors.counting()));
    }

    /**
     * 将 Category 对象转换为 CategoryDTO 对象
     *
     * @param category Category 对象
     * @return CategoryDTO
     */
    private CategoryDTO convertToDTO(Category category, Map<Integer, Long> childCountMap) {
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        dto.setHasChildren(childCountMap.getOrDefault(category.getCategoryId(), 0L));
        return dto;
    }

    /**
     * 根据分类 ID 获取分类数量
     *
     * @param categoryId 分类 ID
     * @return Long
     */
    public Long getCountById(Integer categoryId) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, categoryId);
        return this.count(queryWrapper);
    }

    /**
     * 根据 parentId 查询父级分类名称
     *
     * @param parentId 父级分类 ID
     * @return String
     */
    @Override
    public String getParentName(Integer parentId) {
        return Optional.ofNullable(this.getById(parentId))
                .map(Category::getCategoryName)
                .orElse(null);
    }

    @Override
    public CategoryDTO detail(Integer id) {
        Category category = this.getById(id);
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

    @Override
    public void create(CategoryDTO dto) {
        // 校验同一父级下分类名称的唯一性
        checkCategoryNameUnique(dto.getCategoryName(), dto.getParentId(), null);

        Category category = convertToEntity(dto);
        save(category);

        invalidateCategoryCachesAfterCommit();
    }

    @Override
    public void update(CategoryDTO dto) {
        // 校验同一父级下分类名称的唯一性（排除自身）
        checkCategoryNameUnique(dto.getCategoryName(), dto.getParentId(), dto.getCategoryId());

        redisCache.deleteObject(SEO_LINK_CATEGORY + dto.getCategoryId());
        updateById(convertToEntity(dto));
        invalidateCategoryCachesAfterCommit();
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateFieldDTO, String[] allowFields) {
        boolean updated = super.updateField(updateFieldDTO, allowFields);
        if (updated) {
            invalidateCategoryCachesAfterCommit();
        }
        return updated;
    }

    @Override
    public boolean del(Integer id) {
        boolean removed = super.del(id);
        if (removed) {
            invalidateCategoryCachesAfterCommit();
        }
        return removed;
    }

    /**
     * 分类变更提交后失效进程内分类树快照，并清理 PC 阶梯菜单等 Redis 缓存。
     */
    private void invalidateCategoryCachesAfterCommit() {
        TransactionAfterCommitExecutor.execute(() -> {
            categoryTreeCache.invalidateAndBroadcast();
            flushCatFloorRedisOnly();
        });
    }

    /**
     * 清理 PC 阶梯菜单与 SEO 链接相关 Redis 缓存（分类树本身改由 {@link CategoryTreeCache} 维护）。
     */
    private void flushCatFloorRedisOnly() {
        redisCache.deleteObject(CAT_FLOOR);
        redisCache.deleteKeysMatchingPattern(CAT_FLOOR + "*");
        redisCache.deleteObject(SEO_LINK_CATEGORY + "getCatFloor");
        redisCache.deleteKeysMatchingPattern(SEO_LINK_CATEGORY + "getCatFloor*");
    }

    /**
     * 校验同一父级下分类名称的唯一性
     *
     * @param categoryName 分类名称
     * @param parentId     父级ID
     * @param excludeId    排除的分类ID（更新时排除自身）
     */
    private void checkCategoryNameUnique(String categoryName, Integer parentId, Integer excludeId) {
        if (StrUtil.isBlank(categoryName)) {
            return;
        }
        // parentId 为空时默认为顶级分类
        Integer actualParentId = parentId == null ? 0 : parentId;

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getCategoryName, categoryName)
                .eq(Category::getParentId, actualParentId);

        // 更新时排除自身
        if (excludeId != null) {
            queryWrapper.ne(Category::getCategoryId, excludeId);
        }

        if (this.count(queryWrapper) > 0) {
            throw new GlobalException("同一级分类下已存在名称为【" + categoryName + "】的分类");
        }
    }

    /**
     * 转换为实体对象
     *
     * @param dto 数据传输对象
     * @return Category
     */
    private Category convertToEntity(CategoryDTO dto) {
        if (!SeoIdentificationUtil.isValidSeoIdentification(dto.getSeoIdentification())) {
            throw new GlobalException("请输入正确的SEO标识");
        }
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        return category;
    }

    @Override
    public List<Tree<Integer>> getAllCategory() {
        CategoryTreeCache.Snapshot snapshot = visibleTreeSnapshot();
        return categoryTreeCache.copyAllTrees(snapshot);
    }

    @Override
    public List<Tree<Integer>> getChildrenCategoryTreeById(Integer categoryId) {
        CategoryTreeCache.Snapshot snapshot = visibleTreeSnapshot();
        return categoryTreeCache.sliceChildren(snapshot, categoryId);
    }

    private CategoryTreeCache.Snapshot visibleTreeSnapshot() {
        return categoryTreeCache.snapshot(this::loadVisibleTreeSnapshot, currentLocaleKey());
    }

    private String currentLocaleKey() {
        return CategoryTreeCache.normalizeLocaleKey(ThreadLocalUtil.getValue());
    }

    private CategoryTreeCache.Snapshot loadVisibleTreeSnapshot() {
        List<Category> categories = this.lambdaQuery().eq(Category::getIsShow, Constants.YES).list();
        List<Tree<Integer>> trees = convertTreeList(categories);
        if (trees.isEmpty()) {
            return CategoryTreeCache.Snapshot.empty();
        }
        Map<Integer, Tree<Integer>> treeMap = new HashMap<>();
        for (Tree<Integer> tree : trees) {
            buildTreeMap(tree, treeMap);
        }
        return new CategoryTreeCache.Snapshot(trees, treeMap);
    }

    /**
     * 转换为树形结构
     *
     * @param categoryList 分类列表
     * @return List<Tree < String>>
     */
    public List<Tree<Integer>> convertTreeList(List<Category> categoryList) {
        if (categoryList.isEmpty()) {
            return Collections.emptyList();
        }

        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("categoryId");
        config.setParentIdKey("parentId");
        config.setNameKey("categoryName");
        config.setWeightKey("sortOrder");
        config.setDeep(4);

        return TreeUtil.build(categoryList, 0, config, (object, treeNode) -> {
            treeNode.setId(object.getCategoryId());
            treeNode.setParentId(object.getParentId());
            treeNode.setName(translatePackage.translate(object.getCategoryName()));
            treeNode.setWeight(object.getSortOrder());
            treeNode.putExtra("isShow", object.getIsShow());
            treeNode.putExtra("categoryPic", object.getCategoryPic());
        });
    }

    @Override
    public List<Category> getHotCategory() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getIsHot, 1)
                .orderByDesc(Category::getSortOrder)
                .last("limit 20");
        return this.list(queryWrapper);
    }

    @Override
    public List<Integer> getCategoryAllChildIds(Integer categoryId) {
        CategoryTreeCache.Snapshot snapshot = visibleTreeSnapshot();
        Tree<Integer> node = snapshot.getTreeMap().get(categoryId);
        if (node != null) {
            List<Integer> result = new ArrayList<>();
            collectAllIdsSafe(node, result, new HashSet<>());
            return result;
        }
        // 快照仅含 is_show=1；隐藏分类或不存在时与旧语义对齐
        Category category = this.getById(categoryId);
        if (category == null) {
            return List.of();
        }
        return List.of(categoryId);
    }

    @Override
    public List<Category> getCategoryByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Category::getCategoryId, ids);
        return this.list(queryWrapper);
    }

    @Override
    public boolean moveCat(MoveCatDTO dto) {
        if (dto.getId() == null || dto.getTargetCatId() == null) {
            throw new GlobalException("参数错误");
        }
        productMapper.update(new Product(), new LambdaUpdateWrapper<Product>()
                .eq(Product::getCategoryId, dto.getId())
                .set(Product::getCategoryId, dto.getTargetCatId()));
        invalidateCategoryCachesAfterCommit();
        return true;
    }

    @Override
    public Integer getCategoryIds(String[] categoryNames, Integer isAutoCat) {
        int categoryId = 0;
        int parentId = 0;
        for (String categoryName : categoryNames) {
            String currentCategoryName = StrUtil.trim(categoryName);
            if (StrUtil.isBlank(currentCategoryName)) {
                continue;
            }

            // 必须基于当前父级匹配分类，保证层级路径正确
            Category category = this.lambdaQuery()
                    .eq(Category::getCategoryName, currentCategoryName)
                    .eq(Category::getParentId, parentId)
                    .one();

            if (category == null) {
                if (isAutoCat == 1) {
                    category = Category.builder()
                            .categoryName(currentCategoryName)
                            .parentId(parentId)
                            .build();
                    this.save(category);
                    invalidateCategoryCachesAfterCommit();
                } else {
                    return 0;
                }
            }

            categoryId = category.getCategoryId();
            // 无论是已存在还是新建，都要推进到下一层父级
            parentId = categoryId;
        }

        return categoryId;
    }

    @Override
    public List<Category> getCategoryByParentId(Integer parentId, int size) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, parentId)
                .eq(Category::getIsShow, 1)
                .orderByAsc(Category::getSortOrder)
                .last("limit " + size);
        return this.list(queryWrapper);
    }


    // 把整个树 flatten 成 Map，便于通过 ID 查找节点
    private static void buildTreeMap(Tree<Integer> tree, Map<Integer, Tree<Integer>> map) {
        map.put(tree.getId(), tree);
        if (tree.getChildren() != null) {
            for (Tree<Integer> child : tree.getChildren()) {
                buildTreeMap(child, map);
            }
        }
    }

    static void collectAllIdsSafe(Tree<Integer> node, List<Integer> list, Set<Integer> visited) {
        if (!visited.add(node.getId())) {
            return; // 已经访问过，说明有环，终止
        }
        list.add(node.getId());
        if (node.getChildren() != null) {
            for (Tree<Integer> child : node.getChildren()) {
                collectAllIdsSafe(child, list, visited);
            }
        }
    }

    @Override
    public List<Integer> getRelatedCategoryIds(Integer categoryId) {
        CategoryTreeCache.Snapshot snapshot = visibleTreeSnapshot();
        Map<Integer, Tree<Integer>> treeMap = snapshot.getTreeMap();

        // 1. 向上找到祖先节点
        Tree<Integer> current = treeMap.get(categoryId);
        if (current == null) {
            return new ArrayList<>();
        }

        Set<Integer> visitedUp = new HashSet<>();
        Tree<Integer> ancestor = current;
        while (ancestor.getParentId() != null && !ancestor.getParentId().equals(0)) {
            if (!visitedUp.add(ancestor.getId())) {
                // 出现环，强制终止
                break;
            }
            Tree<Integer> parent = treeMap.get(ancestor.getParentId());
            if (parent == null) {
                break;
            }
            ancestor = parent;
        }

        // 2. 从祖先节点开始向下递归收集所有子节点的 ID
        List<Integer> result = new ArrayList<>();
        collectAllIdsSafe(ancestor, result, new HashSet<>());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDel(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 查询所有要删除的分类
        List<Category> categories = this.listByIds(ids);
        // 构建 ID -> Category 映射
        Map<Integer, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));
        // 按层级深度排序：深度大的（子类）排在前面先删除
        List<Integer> sortedIds = categories.stream()
                .sorted((c1, c2) -> {
                    int depth1 = calculateDepth(c1, categoryMap);
                    int depth2 = calculateDepth(c2, categoryMap);
                    return Integer.compare(depth2, depth1);
                })
                .map(Category::getCategoryId)
                .toList();

        // 先检查所有分类是否可删除
        for (Integer id : sortedIds) {
            // 检查是否有子分类（不在本次删除列表中的子分类）
            Category child = this.lambdaQuery()
                    .eq(Category::getParentId, id)
                    .notIn(Category::getCategoryId, ids)
                    .last("limit 1").one();
            if (child != null) {
                Category current = categoryMap.get(id);
                String catName = current != null ? current.getCategoryName() : String.valueOf(id);
                throw new GlobalException(messageHelper.get("error.category.has.children", catName));
            }
            // 检查是否有商品
            Long count = productMapper.selectCount(
                    new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
            if (count > 0) {
                Category current = categoryMap.get(id);
                String catName = current != null ? current.getCategoryName() : String.valueOf(id);
                throw new GlobalException(messageHelper.get("error.category.has.products", catName, count));
            }
        }
        // 全部检查通过，执行删除
        for (Integer id : sortedIds) {
            super.del(id);
        }
        invalidateCategoryCachesAfterCommit();
    }

    /**
     * 计算分类深度
     */
    private int calculateDepth(Category category, Map<Integer, Category> categoryMap) {
        int depth = 0;
        Integer parentId = category.getParentId();
        while (parentId != null && parentId != 0 && categoryMap.containsKey(parentId)) {
            depth++;
            parentId = categoryMap.get(parentId).getParentId();
        }
        return depth;
    }

}
