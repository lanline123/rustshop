package com.tigshop.service.user.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.user.MemberTag;
import com.tigshop.bean.model.user.MemberTagGroup;
import com.tigshop.bean.model.user.UserMemberTag;
import com.tigshop.bean.param.user.MemberTagGroupCreateParam;
import com.tigshop.bean.param.user.MemberTagGroupQueryPage;
import com.tigshop.bean.param.user.MemberTagGroupUpdateParam;
import com.tigshop.bean.param.user.MemberTagItemParam;
import com.tigshop.bean.vo.user.MemberTagGroupDetailVO;
import com.tigshop.bean.vo.user.MemberTagGroupListVO;
import com.tigshop.bean.vo.user.MemberTagVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.user.MemberTagGroupMapper;
import com.tigshop.mapper.user.MemberTagMapper;
import com.tigshop.mapper.user.UserMemberTagMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.MemberTagGroupService;
import com.tigshop.service.user.ShopMemberSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会员标签组管理。
 *
 * <p>店铺隔离（SHOP-MEMBER-20260708）：所有查询/写入按 {@code shopId = HeaderUtils.getShopId()} 收敛，
 * 平台端(=0)仅见/操作平台标签，店铺端仅见/操作自身 shopId，互不可见。
 * 店铺端写操作受总开关冻结守卫约束。
 */
@Service
@RequiredArgsConstructor
public class MemberTagGroupServiceImpl extends BaseServiceImpl<MemberTagGroupMapper, MemberTagGroup>
        implements MemberTagGroupService {

    private final MemberTagMapper memberTagMapper;
    private final UserMemberTagMapper userMemberTagMapper;
    private final ShopMemberSettingService shopMemberSettingService;

    /** 当前上下文店铺ID：平台=0，店铺/门店=自身 */
    private int currentShopId() {
        Integer s = HeaderUtils.getShopId();
        return s == null ? 0 : s;
    }

    @Override
    public Page<MemberTagGroupListVO> list(MemberTagGroupQueryPage queryPage) {
        int shopId = currentShopId();
        Page<MemberTagGroup> page = new Page<>(queryPage.getPage(), queryPage.getSize());
        LambdaQueryWrapper<MemberTagGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberTagGroup::getShopId, shopId);
        buildSortOrder(page, queryPage.getSortField(), queryPage.getSortOrder());
        // 置顶优先：在 page orders 最前面插入 is_top DESC
        List<OrderItem> orders = new java.util.ArrayList<>(page.orders());
        orders.addFirst(OrderItem.desc("is_top"));
        page.setOrders(orders);

        if (queryPage.getGroupId() != null) {
            queryWrapper.eq(MemberTagGroup::getId, queryPage.getGroupId());
        }
        if (StrUtil.isNotEmpty(queryPage.getKeyword())) {
            String keyword = queryPage.getKeyword();
            List<Long> matchedGroupIds = memberTagMapper.selectList(
                    new LambdaQueryWrapper<MemberTag>()
                            .eq(MemberTag::getShopId, shopId)
                            .like(MemberTag::getTagName, keyword))
                    .stream().map(MemberTag::getGroupId).distinct().toList();
            queryWrapper.and(w -> {
                w.like(MemberTagGroup::getGroupName, keyword);
                if (!matchedGroupIds.isEmpty()) {
                    w.or().in(MemberTagGroup::getId, matchedGroupIds);
                }
            });
        }
        if (StrUtil.isNotEmpty(queryPage.getGroupName())) {
            queryWrapper.like(MemberTagGroup::getGroupName, queryPage.getGroupName());
        }

        Page<MemberTagGroup> groupPage = this.page(page, queryWrapper);
        List<MemberTagGroup> groups = groupPage.getRecords();
        if (groups.isEmpty()) {
            Page<MemberTagGroupListVO> emptyPage = new Page<>();
            BeanUtils.copyProperties(groupPage, emptyPage);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        List<Long> groupIds = groups.stream().map(MemberTagGroup::getId).toList();

        // 批量查询组下所有标签
        List<MemberTag> allTags = memberTagMapper.selectList(
                new LambdaQueryWrapper<MemberTag>()
                        .eq(MemberTag::getShopId, shopId)
                        .in(MemberTag::getGroupId, groupIds));
        Map<Long, String> groupTagValuesMap = allTags.stream()
                .collect(Collectors.groupingBy(MemberTag::getGroupId,
                        Collectors.mapping(MemberTag::getTagName, Collectors.joining("，"))));
        // 按组分组标签列表（带ID，供前端选择）
        Map<Long, List<MemberTagVO>> groupTagListMap = allTags.stream()
                .collect(Collectors.groupingBy(MemberTag::getGroupId,
                        Collectors.mapping(tag -> {
                            MemberTagVO tagVO = new MemberTagVO();
                            tagVO.setId(tag.getId());
                            tagVO.setGroupId(tag.getGroupId());
                            tagVO.setTagName(tag.getTagName());
                            tagVO.setCreateTime(tag.getCreateTime() != null ? tag.getCreateTime().toString() : null);
                            return tagVO;
                        }, Collectors.toList())));

        // 批量统计每个组的客户人数（通过标签关联表去重 user_id）
        List<Long> allTagIds = allTags.stream().map(MemberTag::getId).toList();
        Map<Long, Long> groupCustomerCountMap;
        if (allTagIds.isEmpty()) {
            groupCustomerCountMap = Collections.emptyMap();
        } else {
            Map<Long, Long> tagIdToGroupId = allTags.stream()
                    .collect(Collectors.toMap(MemberTag::getId, MemberTag::getGroupId));
            List<UserMemberTag> userTags = userMemberTagMapper.selectList(
                    new LambdaQueryWrapper<UserMemberTag>()
                            .eq(UserMemberTag::getShopId, shopId)
                            .in(UserMemberTag::getTagId, allTagIds));
            groupCustomerCountMap = userTags.stream()
                    .collect(Collectors.groupingBy(
                            ut -> tagIdToGroupId.get(ut.getTagId()),
                            Collectors.mapping(UserMemberTag::getUserId, Collectors.collectingAndThen(
                                    Collectors.toSet(), Set::size))))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue()));
        }

        List<MemberTagGroupListVO> voList = groups.stream().map(group -> {
            MemberTagGroupListVO vo = new MemberTagGroupListVO();
            vo.setId(group.getId());
            vo.setGroupName(group.getGroupName());
            vo.setTagValues(groupTagValuesMap.getOrDefault(group.getId(), ""));
            vo.setTags(groupTagListMap.getOrDefault(group.getId(), Collections.emptyList()));
            vo.setIsTop(group.getIsTop() != null ? group.getIsTop() : 0);
            vo.setCustomerCount(groupCustomerCountMap.getOrDefault(group.getId(), 0L));
            vo.setCreateTime(group.getCreateTime() != null ? group.getCreateTime().toString() : null);
            return vo;
        }).toList();

        Page<MemberTagGroupListVO> voPage = new Page<>();
        BeanUtils.copyProperties(groupPage, voPage);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public MemberTagGroupDetailVO detail(Long id) {
        int shopId = currentShopId();
        MemberTagGroup group = getBaseMapper().selectById(id);
        if (group == null || group.getShopId() == null || group.getShopId() != shopId) {
            throw new GlobalException("标签组不存在");
        }
        MemberTagGroupDetailVO vo = new MemberTagGroupDetailVO();
        vo.setId(group.getId());
        vo.setGroupName(group.getGroupName());
        vo.setCreateTime(group.getCreateTime() != null ? group.getCreateTime().toString() : null);

        List<MemberTag> tags = memberTagMapper.selectList(
                new LambdaQueryWrapper<MemberTag>()
                        .eq(MemberTag::getShopId, shopId)
                        .eq(MemberTag::getGroupId, id));
        List<MemberTagVO> tagVOList = tags.stream().map(tag -> {
            MemberTagVO tagVO = new MemberTagVO();
            tagVO.setId(tag.getId());
            tagVO.setGroupId(tag.getGroupId());
            tagVO.setTagName(tag.getTagName());
            tagVO.setCreateTime(tag.getCreateTime() != null ? tag.getCreateTime().toString() : null);
            return tagVO;
        }).toList();
        vo.setTags(tagVOList);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(MemberTagGroupCreateParam param) {
        int shopId = currentShopId();
        MemberTagGroup group = new MemberTagGroup();
        group.setShopId(shopId);
        group.setGroupName(param.getGroupName());
        getBaseMapper().insert(group);

        List<MemberTag> tags = param.getTagNames().stream().map(tagName -> {
            MemberTag tag = new MemberTag();
            tag.setShopId(shopId);
            tag.setGroupId(group.getId());
            tag.setTagName(tagName.trim());
            return tag;
        }).toList();
        tags.forEach(memberTagMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MemberTagGroupUpdateParam param) {
        int shopId = currentShopId();
        MemberTagGroup group = getBaseMapper().selectById(param.getId());
        if (group == null || group.getShopId() == null || group.getShopId() != shopId) {
            throw new GlobalException("标签组不存在");
        }
        group.setGroupName(param.getGroupName());
        getBaseMapper().updateById(group);

        // 全量同步标签：基于 ID 对比差异，支持改名
        List<MemberTag> existingTags = memberTagMapper.selectList(
                new LambdaQueryWrapper<MemberTag>()
                        .eq(MemberTag::getShopId, shopId)
                        .eq(MemberTag::getGroupId, param.getId()));
        Map<Long, MemberTag> existingMap = existingTags.stream()
                .collect(Collectors.toMap(MemberTag::getId, t -> t));

        Set<Long> submittedIds = param.getTags().stream()
                .map(MemberTagItemParam::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 删除不在提交列表中的标签
        existingTags.stream()
                .filter(t -> !submittedIds.contains(t.getId()))
                .forEach(t -> {
                    memberTagMapper.deleteById(t.getId());
                    userMemberTagMapper.delete(
                            new LambdaQueryWrapper<UserMemberTag>()
                                    .eq(UserMemberTag::getShopId, shopId)
                                    .eq(UserMemberTag::getTagId, t.getId()));
                });

        // 有 ID → 已有标签，更新名称；无 ID → 新增标签
        for (MemberTagItemParam item : param.getTags()) {
            String tagName = item.getTagName().trim();
            if (item.getId() != null) {
                MemberTag existing = existingMap.get(item.getId());
                if (existing != null && !existing.getTagName().equals(tagName)) {
                    existing.setTagName(tagName);
                    memberTagMapper.updateById(existing);
                }
            } else {
                MemberTag tag = new MemberTag();
                tag.setShopId(shopId);
                tag.setGroupId(param.getId());
                tag.setTagName(tagName);
                memberTagMapper.insert(tag);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delGroup(Long id) {
        int shopId = currentShopId();
        MemberTagGroup group = getBaseMapper().selectById(id);
        if (group == null || group.getShopId() == null || group.getShopId() != shopId) {
            throw new GlobalException("标签组不存在");
        }

        // 查出组下所有标签ID
        List<MemberTag> tags = memberTagMapper.selectList(
                new LambdaQueryWrapper<MemberTag>()
                        .eq(MemberTag::getShopId, shopId)
                        .eq(MemberTag::getGroupId, id));
        List<Long> tagIds = tags.stream().map(MemberTag::getId).toList();

        // 清理用户关联
        if (!tagIds.isEmpty()) {
            userMemberTagMapper.delete(
                    new LambdaQueryWrapper<UserMemberTag>()
                            .eq(UserMemberTag::getShopId, shopId)
                            .in(UserMemberTag::getTagId, tagIds));
        }

        // 逻辑删除标签
        tags.forEach(tag -> memberTagMapper.deleteById(tag.getId()));
        // 逻辑删除标签组
        getBaseMapper().deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTop(Long id) {
        int shopId = currentShopId();
        MemberTagGroup group = getBaseMapper().selectById(id);
        if (group == null || group.getShopId() == null || group.getShopId() != shopId) {
            throw new GlobalException("标签组不存在");
        }
        // 取当前店铺最大置顶权重，新置顶组 = max + 1，确保排在最前面
        Integer maxTop = getBaseMapper().selectMaxIsTop(shopId);
        int nextTop = (maxTop == null ? 0 : maxTop) + 1;
        group.setIsTop(nextTop);
        getBaseMapper().updateById(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTop(Long id) {
        int shopId = currentShopId();
        MemberTagGroup group = getBaseMapper().selectById(id);
        if (group == null || group.getShopId() == null || group.getShopId() != shopId) {
            throw new GlobalException("标签组不存在");
        }
        group.setIsTop(0);
        getBaseMapper().updateById(group);
    }
}
