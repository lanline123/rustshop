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

package com.tigshop.service.salesman.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.salesman.SalesmanMaterial;
import com.tigshop.bean.model.salesman.SalesmanMaterialCategory;
import com.tigshop.bean.param.salesman.SalesmanMaterialCategoryEditParam;
import com.tigshop.bean.param.salesman.SalesmanMaterialCategorySaveParam;
import com.tigshop.bean.query.salesman.SalesmanMaterialCategoryPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialCategoryVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.salesman.SalesmanMaterialCategoryMapper;
import com.tigshop.mapper.salesman.SalesmanMaterialMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.salesman.SalesmanMaterialCategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 素材分类服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class SalesmanMaterialCategoryServiceImpl extends BaseServiceImpl<SalesmanMaterialCategoryMapper, SalesmanMaterialCategory> implements SalesmanMaterialCategoryService {

    @Resource
    SalesmanMaterialMapper salesmanMaterialMapper;

    @Override
    public Page<SalesmanMaterialCategoryVO> list(SalesmanMaterialCategoryPageQuery query) {
        // 分页
        Page<SalesmanMaterialCategory> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<SalesmanMaterialCategory> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(SalesmanMaterialCategory::getShopId, getShopId());
        // 排序字段
        buildSortOrder(page, query.getSortField(), query.getSortOrder());

        // 搜索关键字
        String keyword = query.getCategoryName();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(SalesmanMaterialCategory::getCategoryName, keyword);
        }
        // 执行查询
        Page<SalesmanMaterialCategory> salesmanMaterialCategoryPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<SalesmanMaterialCategory> salesmanMaterialCategoryPageRecords = salesmanMaterialCategoryPage.getRecords();
        // 转换为VO
        List<SalesmanMaterialCategoryVO> salesmanMaterialCategoryVOList = salesmanMaterialCategoryPageRecords.stream()
                .map(salesmanMaterialCategory -> {
                    SalesmanMaterialCategoryVO salesmanMaterialCategoryVO = new SalesmanMaterialCategoryVO();
                    BeanUtils.copyProperties(salesmanMaterialCategory, salesmanMaterialCategoryVO);
                    if (salesmanMaterialCategory.getAddTime() != null && salesmanMaterialCategory.getAddTime() > 0) {
                        Date addTime = DateUtil.date(salesmanMaterialCategory.getAddTime() * 1000);
                        salesmanMaterialCategoryVO.setAddTime(DateUtil.format(addTime, DATE_FORMAT));
                    }
                    return salesmanMaterialCategoryVO;
                }).toList();
        return PageUtil.convertPage(salesmanMaterialCategoryPage, salesmanMaterialCategoryVOList);
    }

    @Override
    public SalesmanMaterialCategoryVO detail(Integer id) {
        if (id != null) {
            SalesmanMaterialCategory salesmanMaterialCategory = this.getById(id);
            SalesmanMaterialCategoryVO salesmanMaterialCategoryVO = new SalesmanMaterialCategoryVO();
            BeanUtils.copyProperties(salesmanMaterialCategory, salesmanMaterialCategoryVO);
            return salesmanMaterialCategoryVO;
        }
        return null;
    }

    @Override
    public boolean create(SalesmanMaterialCategorySaveParam param) {
        Long count = this.lambdaQuery()
                .eq(SalesmanMaterialCategory::getCategoryName, param.getCategoryName())
                .eq(SalesmanMaterialCategory::getShopId, getShopId())
                .count();
        if (count > 0) {
            throw new GlobalException("分类名称已存在");
        }

        SalesmanMaterialCategory salesmanMaterialCategory = new SalesmanMaterialCategory();
        BeanUtils.copyProperties(param, salesmanMaterialCategory);
        salesmanMaterialCategory.setShopId(getShopId());
        salesmanMaterialCategory.setAddTime(StringUtils.getCurrentTime());
        return this.save(salesmanMaterialCategory);
    }

    @Override
    public boolean update(SalesmanMaterialCategoryEditParam param) {
        if (param != null) {
            Long count = this.lambdaQuery()
                    .eq(SalesmanMaterialCategory::getCategoryName, param.getCategoryName())
                    .eq(SalesmanMaterialCategory::getShopId, getShopId())
                    .ne(SalesmanMaterialCategory::getCategoryId, param.getCategoryId())
                    .count();
            if (count > 0) {
                throw new GlobalException("分类名称已存在");
            }
            SalesmanMaterialCategory salesmanMaterialCategory = new SalesmanMaterialCategory();
            BeanUtils.copyProperties(param, salesmanMaterialCategory);
            return this.updateById(salesmanMaterialCategory);
        }
        return false;
    }

    @Override
    public void delete(Integer id) {
        //判断是否有关联素材
        long count = salesmanMaterialMapper.selectCount(new LambdaQueryWrapper<SalesmanMaterial>().eq(SalesmanMaterial::getCategoryId, id));
        if (count > 0) {
            throw new GlobalException("请先删除该分类下的素材");
        }
        del(id);
    }
}
