package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.product.ProductGroup;
import com.tigshop.bean.model.promotion.BuyerShow;
import com.tigshop.bean.model.promotion.BuyerShowGroup;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowGroupSaveParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowGroupUpdateParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowGroupPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowGroupPageVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowGroupVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.promotion.BuyerShowGroupMapper;
import com.tigshop.mapper.promotion.BuyerShowMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductGroupService;
import com.tigshop.service.promotion.BuyerShowGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author wzh
*/
@RequiredArgsConstructor
@Service
public class BuyerShowGroupServiceImpl extends BaseServiceImpl<BuyerShowGroupMapper, BuyerShowGroup>
    implements BuyerShowGroupService {

    private final ProductGroupService productGroupService;

    private final BuyerShowMapper buyerShowMapper;

    private final com.tigshop.common.core.TranslatePackage translatePackage;

    /**
     * 关联商品分组名 List&lt;String&gt; 无法用 @JsonTranslate，逐条翻译（接口文案 dataType=1）。
     */
    private List<String> translateProductGroupNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return names;
        }
        return names.stream()
                .map(name -> translatePackage.translate(name, com.tigshop.common.enums.DataType.API.getCode()))
                .toList();
    }

    @Override
    public void create(BuyerShowGroupSaveParam param) {
        this.verifyGroupName(param.getGroupName(), null);

        BuyerShowGroup buyerShowGroup = new BuyerShowGroup();
        buyerShowGroup.setGroupName(param.getGroupName());
        buyerShowGroup.setShopId(getShopId());
        buyerShowGroup.setProductGroupId(JSONUtil.toJsonStr(param.getGroupIds()));
        this.save(buyerShowGroup);
    }

    @Override
    public void update(BuyerShowGroupUpdateParam param) {
        BuyerShowGroup buyerShowGroup = this.getById(param.getBuyerShowGroupId());
        Assert.notNull(buyerShowGroup, () -> new GlobalException("数据不存在"));

        this.verifyGroupName(param.getGroupName(), param.getBuyerShowGroupId());

        buyerShowGroup.setGroupName(param.getGroupName());
        buyerShowGroup.setProductGroupId(JSONUtil.toJsonStr(param.getGroupIds()));
        this.updateById(buyerShowGroup);
    }

    @Override
    public Page<BuyerShowGroupPageVO> list(BuyerShowGroupPageQuery query) {
        Page<BuyerShowGroup> page = new Page<>(query.getPage(), query.getSize());

        // 构造查询构造器
        LambdaQueryWrapper<BuyerShowGroup> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, query.getSortField(), query.getSortOrder());
        queryWrapper.eq(BuyerShowGroup::getShopId, getShopId());

        // 执行查询
        Page<BuyerShowGroup> buyerShowGroupPage = this.page(page, queryWrapper);
        List<BuyerShowGroup> buyerShowGroupList = buyerShowGroupPage.getRecords();
        if (CollUtil.isEmpty(buyerShowGroupList)) {
            return new Page<>();
        }
        List<Integer> productGroupIds = buyerShowGroupList.stream()
                .filter(buyerShowGroup -> StrUtil.isNotBlank(buyerShowGroup.getProductGroupId()))
                .flatMap(buyerShowGroup -> JSONUtil.toList(buyerShowGroup.getProductGroupId(), Integer.class).stream())
                .toList();

        List<ProductGroup> productGroupList = productGroupService.lambdaQuery()
                .in(ProductGroup::getProductGroupId, productGroupIds)
                .list();


        List<BuyerShowGroupPageVO> buyerShowGroupPageVOList = buyerShowGroupList.stream()
                .map(buyerShowGroup -> {
                    BuyerShowGroupPageVO buyerShowGroupPageVO = new BuyerShowGroupPageVO();
                    BeanUtils.copyProperties(buyerShowGroup, buyerShowGroupPageVO);
                    List<Integer> productGroupIdList = JSONUtil.toList(buyerShowGroup.getProductGroupId(), Integer.class);
                    List<String> productGroupName = productGroupList.stream()
                            .filter(productGroup -> productGroupIdList.contains(productGroup.getProductGroupId()))
                            .map(ProductGroup::getProductGroupName)
                            .toList();
                    buyerShowGroupPageVO.setProductGroupName(translateProductGroupNames(productGroupName));

                    //买家秀数量
                    Long count = buyerShowMapper.selectCount(
                            new LambdaQueryWrapper<BuyerShow>()
                                    .apply("JSON_CONTAINS(group_data, {0})", buyerShowGroup.getBuyerShowGroupId().toString())
                    );
                    buyerShowGroupPageVO.setBuyerShowCount(count);

                    return buyerShowGroupPageVO;
                }).toList();


        return PageUtil.convertPage(buyerShowGroupPage, buyerShowGroupPageVOList);
    }

    @Override
    public BuyerShowGroupVO detail(Integer buyerShowGroupId) {
        BuyerShowGroup buyerShowGroup = this.getById(buyerShowGroupId);
        Assert.notNull(buyerShowGroup, () -> new GlobalException("数据不存在"));
        BuyerShowGroupVO buyerShowGroupVO = new BuyerShowGroupVO();
        BeanUtils.copyProperties(buyerShowGroup, buyerShowGroupVO);
        buyerShowGroupVO.setGroupIds(JSONUtil.toList(buyerShowGroup.getProductGroupId(), Integer.class));
        List<String> productGroupName = productGroupService.lambdaQuery()
                .in(ProductGroup::getProductGroupId, buyerShowGroupVO.getGroupIds())
                .list()
                .stream()
                .map(ProductGroup::getProductGroupName)
                .toList();
        buyerShowGroupVO.setProductGroupName(translateProductGroupNames(productGroupName));
        return buyerShowGroupVO;
    }

    @Override
    public List<Integer> getBuyerShowGroupByProductIds(List<Integer> productIds) {
        List<ProductGroup> productGroupList = productGroupService.lambdaQuery()
                .and(qw -> {
                    for (Integer productId : productIds) {
                        qw.or().apply("JSON_CONTAINS(product_ids, {0})", productId.toString());
                    }
                }).list();
        if (CollUtil.isNotEmpty(productGroupList)) {
            List<Integer> productGroupIds = productGroupList.stream()
                    .map(ProductGroup::getProductGroupId).toList();
            if (CollUtil.isEmpty(productGroupIds)) {
                return List.of();
            }
            List<BuyerShowGroup> buyerShowGroupList = this.lambdaQuery()
                    .and(qw -> {
                        for (Integer productGroupId : productGroupIds) {
                            qw.or().apply("JSON_CONTAINS(product_group_id, {0})", productGroupId.toString());
                        }
                    }).list();
            if (CollUtil.isNotEmpty(buyerShowGroupList)){
                return buyerShowGroupList.stream().map(BuyerShowGroup::getBuyerShowGroupId).toList();
            }
        }
        return List.of();
    }


    protected void verifyGroupName(String groupName, Integer buyerShowGroupId) {
        Long count = this.lambdaQuery()
                .eq(BuyerShowGroup::getGroupName, groupName)
                .ne(buyerShowGroupId != null, BuyerShowGroup::getBuyerShowGroupId, buyerShowGroupId)
                .count();
        Assert.isTrue(count == 0, () -> new GlobalException("分组名称已存在"));
    }


}




