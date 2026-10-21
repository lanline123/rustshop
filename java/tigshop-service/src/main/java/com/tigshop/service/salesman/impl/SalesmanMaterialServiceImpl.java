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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanMaterial;
import com.tigshop.bean.model.salesman.SalesmanMaterialCategory;
import com.tigshop.bean.param.salesman.SalesmanMaterialEditParam;
import com.tigshop.bean.param.salesman.SalesmanMaterialSaveParam;
import com.tigshop.bean.query.salesman.SalesmanMaterialApiPageQuery;
import com.tigshop.bean.query.salesman.SalesmanMaterialPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.salesman.SalesmanMaterialMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.salesman.SalesmanMaterialCategoryService;
import com.tigshop.service.salesman.SalesmanMaterialService;
import com.tigshop.service.salesman.SalesmanService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 分销员分组服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class SalesmanMaterialServiceImpl extends BaseServiceImpl<SalesmanMaterialMapper, SalesmanMaterial> implements SalesmanMaterialService {

    @Resource
    SalesmanMaterialCategoryService salesmanMaterialCategoryService;

    @Resource
    ProductService productService;

    @Resource
    SalesmanService salesmanService;

    @Override
    public Page<SalesmanMaterialVO> list(SalesmanMaterialPageQuery query) {
        // 分页
        Page<SalesmanMaterial> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<SalesmanMaterial> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(SalesmanMaterial::getShopId, getShopId());
        // 排序字段
        buildSortOrder(page, query.getSortField(), query.getSortOrder());

        if (query.getCategoryId() != null) {
            queryWrapper.eq(SalesmanMaterial::getCategoryId, query.getCategoryId());
        }

        if ("user".equals(query.getFrom())) {
            queryWrapper.eq(SalesmanMaterial::getIsAvailable, 1);
        }

        // 执行查询
        Page<SalesmanMaterial> salesmanMaterialPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<SalesmanMaterial> salesmanMaterialPageRecords = salesmanMaterialPage.getRecords();
        //取出所有分类id
        List<Integer> categoryIds = salesmanMaterialPageRecords.stream()
                .map(SalesmanMaterial::getCategoryId)
                .distinct()
                .toList();
        //从category服务中取出
        List<SalesmanMaterialCategory> categorys = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(categoryIds)) {
            categorys = salesmanMaterialCategoryService.list(new QueryWrapper<SalesmanMaterialCategory>().in("category_id", categoryIds));
        }
        //按category_id组装成map
        Map<Integer, SalesmanMaterialCategory> categoryMap = categorys.stream()
                .collect(Collectors.toMap(SalesmanMaterialCategory::getCategoryId, salesmanMaterialCategory -> salesmanMaterialCategory));

        //从recrods中取出productIds
        List<Integer> productIds = salesmanMaterialPageRecords.stream()
                .map(SalesmanMaterial::getProductId)
                .distinct()
                .toList();
        List<Product> products = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(productIds)) {
            products = productService.listByIds(productIds);
        }
        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        // 转换为VO
        List<SalesmanMaterialVO> salesmanMaterialVOList = salesmanMaterialPageRecords.stream()
                .map(salesmanMaterial -> {
                    SalesmanMaterialVO salesmanMaterialVO = new SalesmanMaterialVO();
                    BeanUtils.copyProperties(salesmanMaterial, salesmanMaterialVO);
                    if (salesmanMaterial.getCategoryId() != null && salesmanMaterial.getCategoryId() > 0) {
                        salesmanMaterialVO.setCategory(categoryMap.get(salesmanMaterial.getCategoryId()));
                    }
                    if (salesmanMaterial.getProductId() != null && salesmanMaterial.getProductId() > 0) {
                        salesmanMaterialVO.setProduct(productMap.get(salesmanMaterial.getProductId()));
                    }
                    return salesmanMaterialVO;
                }).toList();
        return PageUtil.convertPage(salesmanMaterialPage, salesmanMaterialVOList);
    }

    @Override
    public SalesmanMaterialVO detail(Integer id) {
        if (id != null) {
            SalesmanMaterial salesmanMaterial = this.getById(id);
            SalesmanMaterialVO salesmanMaterialVO = new SalesmanMaterialVO();
            BeanUtils.copyProperties(salesmanMaterial, salesmanMaterialVO);

            if (salesmanMaterial.getCategoryId() != null && salesmanMaterial.getCategoryId() > 0) {
                salesmanMaterialVO.setCategory(salesmanMaterialCategoryService.getById(salesmanMaterial.getCategoryId()));
            }
            if (salesmanMaterial.getProductId() != null && salesmanMaterial.getProductId() > 0) {
                salesmanMaterialVO.setProduct(productService.getById(salesmanMaterial.getProductId()));
            }
            List<SalesmanMaterialVO.Pic> pics = JSONUtil.toList(salesmanMaterial.getPics(), SalesmanMaterialVO.Pic.class);
            salesmanMaterialVO.setPics(pics);
            return salesmanMaterialVO;
        }
        return null;
    }

    @Override
    public boolean create(SalesmanMaterialSaveParam param) {
        if (param != null) {
            SalesmanMaterial salesmanMaterial = new SalesmanMaterial();
            BeanUtils.copyProperties(param, salesmanMaterial);
            salesmanMaterial.setAddTime(StringUtils.getCurrentTime());
            salesmanMaterial.setShopId(getShopId());
            salesmanMaterial.setPics(JSONUtil.toJsonStr(param.getPics()));
            return this.save(salesmanMaterial);
        }
        return false;
    }

    @Override
    public boolean update(SalesmanMaterialEditParam param) {
        if (param != null) {
            SalesmanMaterial salesmanMaterial = new SalesmanMaterial();
            BeanUtils.copyProperties(param, salesmanMaterial);
            salesmanMaterial.setPics(JSONUtil.toJsonStr(param.getPics()));
            return this.updateById(salesmanMaterial);
        }
        return false;
    }

    @Override
    public List<SalesmanMaterialCategory> getAllCategory() {
        Salesman salesman = salesmanService.lambdaQuery()
                .eq(Salesman::getUserId, getCurrentUserId())
                .last("limit 1")
                .one();
        Integer shopId = -1;
        if (salesman != null) {
            shopId = salesman.getShopId();
        }
        return salesmanMaterialCategoryService.list(
                new LambdaQueryWrapper<SalesmanMaterialCategory>()
                        .eq(SalesmanMaterialCategory::getShopId, shopId)
                        .orderByDesc(SalesmanMaterialCategory::getSortOrder));
    }

    @Override
    public Page<SalesmanMaterialVO> apiPage(SalesmanMaterialApiPageQuery pageQuery) {
        pageQuery.initDefaultParam();

        // 分页
        Page<SalesmanMaterial> page = buildSortOrder(pageQuery);

        Salesman salesman = salesmanService.lambdaQuery()
                .eq(Salesman::getUserId, getCurrentUserId())
                .last("limit 1")
                .one();
        if (salesman != null) {
            pageQuery.setShopId(salesman.getShopId());
        } else {
            pageQuery.setShopId(-1);
        }

        Page<SalesmanMaterialVO> resultPage = this.baseMapper.apiPage(page, pageQuery);


        List<SalesmanMaterialVO> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> productIds = records.stream().map(SalesmanMaterialVO::getProductId).toList();
        List<Product> products = productService.listByIds(productIds);
        Map<Integer, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, product -> product));

        records.forEach(record ->
        {
            record.setProduct(productMap.get(record.getProductId()));
            record.setPics(JSONUtil.toList(record.getPicsJson(), SalesmanMaterialVO.Pic.class));
        });

        return resultPage;
    }
}
