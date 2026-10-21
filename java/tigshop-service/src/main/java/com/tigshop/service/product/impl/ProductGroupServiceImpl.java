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

package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductGroupCreateDTO;
import com.tigshop.bean.dto.product.ProductGroupListDTO;
import com.tigshop.bean.dto.product.ProductGroupUpdateDTO;
import com.tigshop.bean.model.product.ProductGroup;
import com.tigshop.bean.vo.product.ProductGroupVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ProductGroupMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductGroupService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 商品分组服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ProductGroupServiceImpl extends BaseServiceImpl<ProductGroupMapper, ProductGroup> implements ProductGroupService {
    @Override
    public Page<ProductGroupVO> list(ProductGroupListDTO listDTO) {
        // 分页
        Page<ProductGroup> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ProductGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductGroup::getShopId, getShopId());
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(ProductGroup::getProductGroupName, keyword);
        }
        if (ObjectUtil.isNotEmpty(listDTO.getProductGroupIds()) && listDTO.getProductGroupIds().split(",").length > 0) {
            queryWrapper.in(ProductGroup::getProductGroupId, Arrays.stream(listDTO.getProductGroupIds().split(",")).toList());
        }
        // 执行查询
        Page<ProductGroup> productGroupPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ProductGroup> productGroupPageRecords = productGroupPage.getRecords();
        // 转换为VO
        List<ProductGroupVO> productGroupVOList = productGroupPageRecords.stream()
                .map(productGroup -> {
                    ProductGroupVO productGroupVO = new ProductGroupVO();
                    BeanUtils.copyProperties(productGroup, productGroupVO);

                    productGroupVO.setProductIds(JSONUtil.toList(productGroup.getProductIds(), Integer.class));

                    DateTime addTime = DateUtil.date(productGroup.getAddTime() * 1000);
                    productGroupVO.setAddTime(DateUtil.format(addTime, DATE_FORMAT));
                    return productGroupVO;
                }).toList();
        return PageUtil.convertPage(productGroupPage, productGroupVOList);
    }

    @Override
    public ProductGroupVO detail(Integer id) {
        if (id != null) {
            ProductGroup productGroup = this.getById(id);
            ProductGroupVO productGroupVO = new ProductGroupVO();
            BeanUtils.copyProperties(productGroup, productGroupVO);
            productGroupVO.setProductIds(JSONUtil.toList(productGroup.getProductIds(), Integer.class));

            DateTime addTime = DateUtil.date(productGroup.getAddTime() * 1000);
            productGroupVO.setAddTime(DateUtil.format(addTime, DATE_FORMAT));
            return productGroupVO;
        }
        return null;
    }

    @Override
    public boolean create(ProductGroupCreateDTO createDTO) {
        if (createDTO != null) {
            ProductGroup productGroup = new ProductGroup();
            BeanUtils.copyProperties(createDTO, productGroup);
            productGroup.setProductIds(JSONUtil.toJsonStr(createDTO.getProductIds()));
            productGroup.setAddTime(StringUtils.getCurrentTime());
            productGroup.setShopId(getShopId());
            return this.save(productGroup);
        }
        return false;
    }

    @Override
    public boolean update(ProductGroupUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ProductGroup productGroup = new ProductGroup();
            BeanUtils.copyProperties(updateDTO, productGroup);
            String productIds = CollUtil.isNotEmpty(updateDTO.getProductIds()) ? JSONUtil.toJsonStr(updateDTO.getProductIds()) : JSONUtil.toJsonStr(new ArrayList<>());
            productGroup.setProductIds(productIds);
            return this.updateById(productGroup);
        }
        return false;
    }

    @Override
    public boolean updateGroupProductByName(Integer productId, String groupName) {
        if (productId == null || StringUtils.isEmpty(groupName)) {
            return true;
        }
        // 构造查询条件，获取相应的商品组
        LambdaQueryWrapper<ProductGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductGroup::getProductGroupName, groupName)
                .eq(ProductGroup::getShopId, HeaderUtils.getShopId());
        ProductGroup productGroup = this.getOne(queryWrapper);
        // 如果商品组不存在，则无需更新
        if (productGroup == null) {
            return true;
        }
        String productIds = productGroup.getProductIds();
        List<Integer> productIdList = new ArrayList<>(StringUtils.str2IntList(productIds));
        // 如果商品ID列表为空，更新当前商品ID
        if (productIds == null) {
            productIdList.add(productId);
        }
        // 如果商品ID列表中不包含当前商品ID，则添加
        if (!productIdList.contains(productId)) {
            productIdList.add(productId);
        }

        productGroup.setProductIds(JSONUtil.toJsonStr(productIdList));
        return this.updateById(productGroup);
    }
}
