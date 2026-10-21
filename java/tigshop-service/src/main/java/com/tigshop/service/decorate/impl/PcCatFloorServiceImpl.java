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

package com.tigshop.service.decorate.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorCreateDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorListDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorUpdateDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.model.decorate.PcCatFloor;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.decorate.PcCatFloorListCategoryListDTO;
import com.tigshop.bean.vo.decorate.PcCatFloorListVO;
import com.tigshop.bean.vo.decorate.PcCatFloorVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.decorate.PcCatFloorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.PcCatFloorService;
import com.tigshop.service.product.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.tigshop.common.constant.Constants.CAT_FLOOR;
import static com.tigshop.common.constant.Constants.SEO_LINK_CATEGORY;

/**
 * 首页分类栏服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class PcCatFloorServiceImpl extends BaseServiceImpl<PcCatFloorMapper, PcCatFloor> implements PcCatFloorService {

    private final CategoryService categoryService;
    private final TranslatePackageImpl translatePackage;
    private final RedisCache redisCache;

    @Override
    public Page<PcCatFloorVO> list(PcCatFloorListDTO listDTO) {
        // 分页
        Page<PcCatFloor> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<PcCatFloor> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq(PcCatFloor::getIsShow, isShow);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(PcCatFloor::getCatFloorName, keyword);
        }
        // 执行查询
        Page<PcCatFloor> pcCatFloorPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<PcCatFloor> pcCatFloorPageRecords = pcCatFloorPage.getRecords();
        // 转换为VO
        List<PcCatFloorVO> pcCatFloorVOList = pcCatFloorPageRecords.stream()
                .map(pcCatFloor -> {
                    PcCatFloorVO pcCatFloorVO = new PcCatFloorVO();
                    BeanUtils.copyProperties(pcCatFloor, pcCatFloorVO);
                    return pcCatFloorVO;
                }).toList();
        return PageUtil.convertPage(pcCatFloorPage, pcCatFloorVOList);
    }

    @Override
    public PcCatFloorVO detail(Integer id) {
        if (id != null) {
            PcCatFloor pcCatFloor = this.getById(id);
            PcCatFloorVO pcCatFloorVO = new PcCatFloorVO();
            BeanUtils.copyProperties(pcCatFloor, pcCatFloorVO);

            pcCatFloorVO.setBrandIds(JsonUtil.fromJson(pcCatFloor.getBrandIds(), Integer[].class));
            pcCatFloorVO.setCategoryIds(JsonUtil.fromJson(pcCatFloor.getCategoryIds(), Integer[].class));
            pcCatFloorVO.setCategoryNames(JsonUtil.fromJson(pcCatFloor.getCategoryNames(), String[].class));

            return pcCatFloorVO;
        }
        return null;
    }

    @Override
    public void create(PcCatFloorCreateDTO createDTO) {
        PcCatFloor pcCatFloor = new PcCatFloor();
        BeanUtils.copyProperties(createDTO, pcCatFloor);
        pcCatFloor.setBrandIds(JsonUtil.toJson(createDTO.getBrandIds()));
        Assert.notNull(createDTO.getCategoryIds(), "分类不能为空");
        pcCatFloor.setCategoryIds(JsonUtil.toJson(createDTO.getCategoryIds()));
        pcCatFloor.setCategoryNames(JsonUtil.toJson(createDTO.getCategoryNames()));
        pcCatFloor.setCatFloorName(getCatFloorName(Arrays.asList(createDTO.getCategoryIds()), Arrays.asList(createDTO.getCategoryNames())));
        this.save(pcCatFloor);

        evictCatFloorResponseCaches();
    }

    @Override
    public void update(PcCatFloorUpdateDTO updateDTO) {
        PcCatFloor pcCatFloor = new PcCatFloor();
        BeanUtils.copyProperties(updateDTO, pcCatFloor);
        pcCatFloor.setBrandIds(JsonUtil.toJson(updateDTO.getBrandIds()));
        pcCatFloor.setCategoryIds(JsonUtil.toJson(updateDTO.getCategoryIds()));
        pcCatFloor.setCategoryNames(JsonUtil.toJson(updateDTO.getCategoryNames()));
        pcCatFloor.setCatFloorName(getCatFloorName(Arrays.asList(updateDTO.getCategoryIds()), Arrays.asList(updateDTO.getCategoryNames())));
        this.updateById(pcCatFloor);

        evictCatFloorResponseCaches();
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateFieldDTO, String[] allowFields) {

        evictCatFloorResponseCaches();

        return super.updateField(updateFieldDTO, allowFields);
    }

    /**
     * 阶梯菜单接口有两层缓存：业务 cat_floor* 与 SeoResponseAdvice 写入的 seo_link_category::getCatFloor*，
     * 需一并删除，否则跨境 PC 会直接返回旧的 SEO 处理结果。
     */
    private void evictCatFloorResponseCaches() {
        redisCache.deleteObject(CAT_FLOOR);
        redisCache.deleteKeysMatchingPattern(CAT_FLOOR + "*");
        redisCache.deleteObject(SEO_LINK_CATEGORY + "getCatFloor");
        redisCache.deleteKeysMatchingPattern(SEO_LINK_CATEGORY + "getCatFloor*");
    }

    public String getCatFloorName(List<Integer> categoryIds, List<String> categoryNames) {
        StringBuilder catFloorName = new StringBuilder();

        for (int i = 0; i < categoryIds.size(); i++) {
            Integer categoryId = categoryIds.get(i);
            String categoryNameFromInput = categoryNames.get(i);

            // 通过 categoryId 查询数据库获取分类名
            String cateName = categoryService.getById(categoryId).getCategoryName();

            String displayName;
            if (categoryNameFromInput != null && !categoryNameFromInput.isEmpty()) {
                displayName = "-".equals(categoryNameFromInput) ? "[" + cateName + "]" : categoryNameFromInput;
            } else {
                displayName = cateName;
            }

            if (i > 0) {
                catFloorName.append("，");
            }
            catFloorName.append(displayName);
        }
        return catFloorName.toString();
    }

    /**
     * 构建跨境翻译后的楼层标题：与 {@link #getCatFloorName} 的拼接规则一致
     * （自定义名优先，"-" 表示用 [分类名]，其余用分类名），差异在于每段单独 translate。
     * 直接对已拼接的整串 translate 命中不了词条，故按分类粒度重建。
     *
     * @param categoryIds   分类 id 列表（决定顺序）
     * @param categoryNames 与 id 对齐的自定义名列表（可能为空/"-"）
     * @param realNameById  分类 id -> 当前分类名（来源 getCategoryByIds，缺失时为 null）
     */
    private String buildTranslatedCatFloorName(List<Integer> categoryIds, List<String> categoryNames,
                                               Map<Integer, String> realNameById) {
        StringBuilder catFloorName = new StringBuilder();
        for (int i = 0; i < categoryIds.size(); i++) {
            Integer categoryId = categoryIds.get(i);
            String customName = i < categoryNames.size() ? categoryNames.get(i) : null;
            String realName = realNameById.get(categoryId);
            String segment;
            if (customName != null && !customName.isEmpty()) {
                segment = "-".equals(customName)
                        ? "[" + translatePackage.translate(realName != null ? realName : "") + "]"
                        : translatePackage.translate(customName);
            } else {
                segment = translatePackage.translate(realName != null ? realName : "");
            }
            if (i > 0) {
                catFloorName.append("，");
            }
            catFloorName.append(segment);
        }
        return catFloorName.toString();
    }


    @Override
    public List<PcCatFloorListVO> getCatFloor() {
        LambdaQueryWrapper<PcCatFloor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PcCatFloor::getIsShow, 1);
        // 按 sort_order 升序排序
        queryWrapper.orderByAsc(PcCatFloor::getSortOrder);
        List<PcCatFloor> pcCatFloorList = this.list(queryWrapper);
        List<PcCatFloorListVO> pcCatFloorListVO = new ArrayList<>();

        Set<Integer> queriedParentIds = new HashSet<>();

        for (PcCatFloor pcCatFloor : pcCatFloorList) {
            PcCatFloorListVO catFloorListVO = new PcCatFloorListVO();
            catFloorListVO.setCatFloorId(pcCatFloor.getCatFloorId());
            catFloorListVO.setFloorIcoFont(pcCatFloor.getFloorIcoFont());

            // 根据 categoryIds 获取分类信息，categoryIds 是一个 JSON 字符串，需要解析成数组
            List<Integer> categoryIds = List.of(JsonUtil.fromJson(pcCatFloor.getCategoryIds(), Integer[].class));
            List<Category> categoryList = categoryService.getCategoryByIds(categoryIds);

            // 根据 categoryNames 获取分类名称，categoryNames 是一个 JSON 字符串，需要解析成数组
            List<String> categoryNames = List.of(JsonUtil.fromJson(pcCatFloor.getCategoryNames(), String[].class));

            // 楼层标题跨境翻译：组合串整体 translate 命中不了词条，按分类粒度逐段翻译后再拼接
            Map<Integer, String> realNameById = new HashMap<>();
            for (Category category : categoryList) {
                realNameById.put(category.getCategoryId(), category.getCategoryName());
            }
            catFloorListVO.setCatFloorName(buildTranslatedCatFloorName(categoryIds, categoryNames, realNameById));

            // 将 categoryIds 和 categoryNames 映射到一个 Map 中
            Map<Integer, String> categoryNameMap = new HashMap<>();
            for (int i = 0; i < categoryIds.size(); i++) {
                categoryNameMap.put(categoryIds.get(i), categoryNames.get(i));
            }
            // 初始化 catList
            List<PcCatFloorListCategoryListDTO> catList = new ArrayList<>();

            // 循环赋值给 catList
            for (Category category : categoryList) {
                PcCatFloorListCategoryListDTO categoryListDTO = new PcCatFloorListCategoryListDTO();
                categoryListDTO.setId(category.getCategoryId());
                categoryListDTO.setPic(category.getCategoryPic());
                categoryListDTO.setChildren(null);
                // 使用 Map 来获取对应的 categoryName
                String categoryName = categoryNameMap.get(category.getCategoryId());
                if (categoryName != null && !categoryName.isEmpty()) {
                    categoryListDTO.setName(translatePackage.translate(categoryName));
                } else {
                    // 如果没有找到对应的 name，使用 category.getCategoryName()
                    categoryListDTO.setName(translatePackage.translate(category.getCategoryName()));
                }

                // 如果是父级分类，获取子分类
                if (category.getParentId() == 0) {
                    // 检查该父级分类 ID 是否已经查询过
                    if (!queriedParentIds.contains(category.getCategoryId())) {
                        List<Tree<Integer>> children = categoryService.getChildrenCategoryTreeById(category.getCategoryId());
                        if (children != null) {
                            categoryListDTO.setChildren(children);
                        }
                        // 将查询过的父级分类 ID 添加到集合中
                        queriedParentIds.add(category.getCategoryId());
                    }
                }

                // 将 categoryListDTO 添加到 catList
                catList.add(categoryListDTO);
            }

            // 将初始化好的 catList 设置到 VO 中
            catFloorListVO.setCatList(catList);

            // 将 catFloorListVO 添加到结果列表中
            pcCatFloorListVO.add(catFloorListVO);
        }
        return pcCatFloorListVO;
    }

}
