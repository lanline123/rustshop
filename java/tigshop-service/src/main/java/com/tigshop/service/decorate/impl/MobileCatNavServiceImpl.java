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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavCreateDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavListDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavUpdateDTO;
import com.tigshop.bean.dto.setting.GalleryPicDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.model.decorate.MobileCatNav;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.decorate.MobileCatNavVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TransactionAfterCommitExecutor;
import com.tigshop.mapper.decorate.MobileCatNavMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.HomeDecorateCache;
import com.tigshop.service.decorate.MobileCatNavService;
import com.tigshop.service.product.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页分类栏服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class MobileCatNavServiceImpl extends BaseServiceImpl<MobileCatNavMapper, MobileCatNav> implements MobileCatNavService {
    @Resource
    CategoryService categoryService;
    @Resource
    private HomeDecorateCache homeDecorateCache;

    /**
     * 分类栏 navList 内嵌于首页装修缓存，写操作提交后失效首页装修快照并跨进程广播。
     */
    private void invalidateHomeDecorateAfterCommit() {
        TransactionAfterCommitExecutor.execute(() -> homeDecorateCache.invalidateAndBroadcast());
    }

    @Override
    public Page<MobileCatNavVO> list(MobileCatNavListDTO listDTO) {
        // 分页
        Page<MobileCatNav> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<MobileCatNav> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq(MobileCatNav::getIsShow, isShow);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(MobileCatNav::getCategoryId, keyword);
        }
        // 执行查询
        Page<MobileCatNav> mobileCatNavPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<MobileCatNav> mobileCatNavPageRecords = mobileCatNavPage.getRecords();

        if (CollUtil.isEmpty(mobileCatNavPageRecords)) {
            return PageUtil.convertPage(mobileCatNavPage, List.of());
        }

        // 获取所有 ID
        List<Integer> categoryIds = mobileCatNavPageRecords.stream()
                .map(MobileCatNav::getCategoryId)
                .distinct()
                .toList();

        // 调用方法获取分类信息
        List<Category> categoryList = categoryService.getCategoryByIds(categoryIds);
        Map<Integer, Category> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

        // 转换为VO
        List<MobileCatNavVO> mobileCatNavVOList = mobileCatNavPageRecords.stream()
                .map(mobileCatNav -> {
                    MobileCatNavVO mobileCatNavVO = new MobileCatNavVO();
                    BeanUtils.copyProperties(mobileCatNav, mobileCatNavVO);

                    Category category = categoryMap.get(mobileCatNavVO.getCategoryId());
                    if (category != null) {
                        // 将分类名称设置到对象中
                        mobileCatNavVO.setCategoryName(category.getCategoryName());
                    }
                    return mobileCatNavVO;
                }).toList();
        return PageUtil.convertPage(mobileCatNavPage, mobileCatNavVOList);
    }

    @Override
    public MobileCatNavVO detail(Integer id) {
        if (id != null) {
            MobileCatNav mobileCatNav = this.getById(id);
            MobileCatNavVO mobileCatNavVO = new MobileCatNavVO();
            BeanUtils.copyProperties(mobileCatNav, mobileCatNavVO);

            mobileCatNavVO.setImgUrl(JsonUtil.fromJson(mobileCatNav.getImgUrl(), GalleryPicDTO[].class));
            mobileCatNavVO.setBrandIds(JsonUtil.fromJson(mobileCatNav.getBrandIds(), Integer[].class));
            mobileCatNavVO.setChildCatIds(JsonUtil.fromJson(mobileCatNav.getChildCatIds(), Integer[].class));

            return mobileCatNavVO;
        }
        return null;
    }

    @Override
    public boolean create(MobileCatNavCreateDTO createDTO) {
        if (createDTO != null) {
            MobileCatNav mobileCatNav = new MobileCatNav();
            BeanUtils.copyProperties(createDTO, mobileCatNav);
            mobileCatNav.setImgUrl(JsonUtil.toJson(createDTO.getImgUrl() == null ? new GalleryPicDTO[0] : createDTO.getImgUrl()));
            mobileCatNav.setBrandIds(JsonUtil.toJson(createDTO.getBrandIds()));
            mobileCatNav.setChildCatIds(JsonUtil.toJson(createDTO.getChildCatIds() == null ? new Integer[0] : createDTO.getChildCatIds()));
            boolean result = this.save(mobileCatNav);
            invalidateHomeDecorateAfterCommit();
            return result;
        }
        return false;
    }

    @Override
    public boolean update(MobileCatNavUpdateDTO updateDTO) {
        if (updateDTO != null) {
            MobileCatNav mobileCatNav = new MobileCatNav();
            BeanUtils.copyProperties(updateDTO, mobileCatNav);

            mobileCatNav.setImgUrl(JsonUtil.toJson(updateDTO.getImgUrl()));
            mobileCatNav.setBrandIds(JsonUtil.toJson(updateDTO.getBrandIds()));
            mobileCatNav.setChildCatIds(JsonUtil.toJson(updateDTO.getChildCatIds()));

            boolean result = this.updateById(mobileCatNav);
            invalidateHomeDecorateAfterCommit();
            return result;
        }
        return false;
    }

    @Override
    public boolean del(Integer id) {
        boolean removed = super.del(id);
        if (removed) {
            invalidateHomeDecorateAfterCommit();
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        boolean result = super.batch(batchDTO);
        if (result) {
            invalidateHomeDecorateAfterCommit();
        }
        return result;
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        boolean updated = super.updateField(updateField, allowFields);
        if (updated) {
            invalidateHomeDecorateAfterCommit();
        }
        return updated;
    }
}
