package com.tigshop.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.user.MemberTag;
import com.tigshop.bean.model.user.MemberTagGroup;
import com.tigshop.bean.model.user.UserMemberTag;
import com.tigshop.bean.param.user.UserMemberTagBatchParam;
import com.tigshop.bean.param.user.UserMemberTagParam;
import com.tigshop.bean.vo.user.UserMemberTagVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.user.MemberTagGroupMapper;
import com.tigshop.mapper.user.MemberTagMapper;
import com.tigshop.mapper.user.UserMemberTagMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.ShopMemberSettingService;
import com.tigshop.service.user.UserMemberTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户-标签关联管理。店铺隔离 + 冻结守卫（SHOP-MEMBER-20260708）。
 *
 * <p>同一用户在不同店铺可拥有各自 shopId 的标签，绑定/解绑只作用于当前店铺 shopId。
 */
@Service
@RequiredArgsConstructor
public class UserMemberTagServiceImpl extends BaseServiceImpl<UserMemberTagMapper, UserMemberTag>
        implements UserMemberTagService {

    private final MemberTagMapper memberTagMapper;
    private final MemberTagGroupMapper memberTagGroupMapper;
    private final ShopMemberSettingService shopMemberSettingService;

    private int currentShopId() {
        Integer s = HeaderUtils.getShopId();
        return s == null ? 0 : s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTags(UserMemberTagParam param) {
        shopMemberSettingService.assertEnabledForShop();
        int shopId = currentShopId();
        List<UserMemberTag> existing = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserMemberTag>()
                        .eq(UserMemberTag::getShopId, shopId)
                        .eq(UserMemberTag::getUserId, param.getUserId()));
        Set<Long> existingTagIds = existing.stream()
                .map(UserMemberTag::getTagId).collect(Collectors.toSet());
        Set<Long> newTagIds = new java.util.HashSet<>(param.getTagIds());

        // 删除不在新列表中的旧标签
        existing.stream()
                .filter(ut -> !newTagIds.contains(ut.getTagId()))
                .forEach(ut -> getBaseMapper().deleteById(ut.getId()));

        // 新增不存在的标签
        Long now = StringUtils.getCurrentTime();
        newTagIds.stream()
                .filter(tagId -> !existingTagIds.contains(tagId))
                .forEach(tagId -> {
                    UserMemberTag ut = new UserMemberTag();
                    ut.setShopId(shopId);
                    ut.setUserId(param.getUserId());
                    ut.setTagId(tagId);
                    ut.setCreateTime(now);
                    getBaseMapper().insert(ut);
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindTags(UserMemberTagParam param) {
        shopMemberSettingService.assertEnabledForShop();
        int shopId = currentShopId();
        getBaseMapper().delete(
                new LambdaQueryWrapper<UserMemberTag>()
                        .eq(UserMemberTag::getShopId, shopId)
                        .eq(UserMemberTag::getUserId, param.getUserId())
                        .in(UserMemberTag::getTagId, param.getTagIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchBindTags(UserMemberTagBatchParam param) {
        shopMemberSettingService.assertEnabledForShop();
        int shopId = currentShopId();
        Set<Long> newTagIds = new java.util.HashSet<>(param.getTagIds());
        Long now = StringUtils.getCurrentTime();

        for (Long userId : param.getUserIds()) {
            List<UserMemberTag> existing = getBaseMapper().selectList(
                    new LambdaQueryWrapper<UserMemberTag>()
                            .eq(UserMemberTag::getShopId, shopId)
                            .eq(UserMemberTag::getUserId, userId));
            Set<Long> existingTagIds = existing.stream()
                    .map(UserMemberTag::getTagId).collect(Collectors.toSet());

            // 删除不在新列表中的旧标签
            existing.stream()
                    .filter(ut -> !newTagIds.contains(ut.getTagId()))
                    .forEach(ut -> getBaseMapper().deleteById(ut.getId()));

            // 新增不存在的标签
            newTagIds.stream()
                    .filter(tagId -> !existingTagIds.contains(tagId))
                    .forEach(tagId -> {
                        UserMemberTag ut = new UserMemberTag();
                        ut.setShopId(shopId);
                        ut.setUserId(userId);
                        ut.setTagId(tagId);
                        ut.setCreateTime(now);
                        getBaseMapper().insert(ut);
                    });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUnbindTags(UserMemberTagBatchParam param) {
        shopMemberSettingService.assertEnabledForShop();
        int shopId = currentShopId();
        getBaseMapper().delete(
                new LambdaQueryWrapper<UserMemberTag>()
                        .eq(UserMemberTag::getShopId, shopId)
                        .in(UserMemberTag::getUserId, param.getUserIds())
                        .in(UserMemberTag::getTagId, param.getTagIds()));
    }

    @Override
    public List<UserMemberTagVO> listByUser(Long userId) {
        int shopId = currentShopId();
        List<UserMemberTag> userTags = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserMemberTag>()
                        .eq(UserMemberTag::getShopId, shopId)
                        .eq(UserMemberTag::getUserId, userId));
        if (userTags.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = userTags.stream().map(UserMemberTag::getTagId).toList();
        List<MemberTag> tags = memberTagMapper.selectList(
                new LambdaQueryWrapper<MemberTag>()
                        .eq(MemberTag::getShopId, shopId)
                        .in(MemberTag::getId, tagIds));
        Map<Long, MemberTag> tagMap = tags.stream()
                .collect(Collectors.toMap(MemberTag::getId, t -> t));

        Set<Long> groupIds = tags.stream().map(MemberTag::getGroupId).collect(Collectors.toSet());
        Map<Long, String> groupNameMap;
        if (groupIds.isEmpty()) {
            groupNameMap = Collections.emptyMap();
        } else {
            List<MemberTagGroup> groups = memberTagGroupMapper.selectList(
                    new LambdaQueryWrapper<MemberTagGroup>()
                            .eq(MemberTagGroup::getShopId, shopId)
                            .in(MemberTagGroup::getId, groupIds));
            groupNameMap = groups.stream()
                    .collect(Collectors.toMap(MemberTagGroup::getId, MemberTagGroup::getGroupName));
        }

        return userTags.stream().map(ut -> {
            UserMemberTagVO vo = new UserMemberTagVO();
            vo.setTagId(ut.getTagId());
            vo.setCreateTime(ut.getCreateTime() != null ? ut.getCreateTime().toString() : null);
            MemberTag tag = tagMap.get(ut.getTagId());
            if (tag != null) {
                vo.setTagName(tag.getTagName());
                vo.setGroupId(tag.getGroupId());
                vo.setGroupName(groupNameMap.getOrDefault(tag.getGroupId(), ""));
            }
            return vo;
        }).toList();
    }

    @Override
    public List<Long> resolveUserIdsByTags(java.util.Collection<Long> tagIds, Integer matchMode) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserMemberTag> matched = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserMemberTag>()
                        .in(UserMemberTag::getTagId, tagIds)
                        .select(UserMemberTag::getUserId, UserMemberTag::getTagId));
        if (Integer.valueOf(1).equals(matchMode)) {
            // 满足全部：用户拥有的标签集合需包含传入的所有 tagIds
            Set<Long> required = new java.util.HashSet<>(tagIds);
            return matched.stream()
                    .collect(Collectors.groupingBy(UserMemberTag::getUserId,
                            Collectors.mapping(UserMemberTag::getTagId, Collectors.toSet())))
                    .entrySet().stream()
                    .filter(e -> e.getValue().containsAll(required))
                    .map(Map.Entry::getKey)
                    .toList();
        }
        // 满足任一（默认）
        return matched.stream().map(UserMemberTag::getUserId).distinct().toList();
    }

    @Override
    public boolean userMatchesTags(Long userId, java.util.Collection<Long> tagIds, Integer matchMode) {
        if (userId == null || tagIds == null || tagIds.isEmpty()) {
            return false;
        }
        List<UserMemberTag> userTags = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserMemberTag>()
                        .eq(UserMemberTag::getUserId, userId)
                        .in(UserMemberTag::getTagId, tagIds)
                        .select(UserMemberTag::getTagId));
        Set<Long> owned = userTags.stream().map(UserMemberTag::getTagId).collect(Collectors.toSet());
        if (Integer.valueOf(1).equals(matchMode)) {
            return owned.containsAll(new java.util.HashSet<>(tagIds));
        }
        return !owned.isEmpty();
    }

    @Override
    public Set<Long> listTagIdsByUser(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return getBaseMapper().selectList(
                        new LambdaQueryWrapper<UserMemberTag>()
                                .eq(UserMemberTag::getUserId, userId)
                                .select(UserMemberTag::getTagId))
                .stream()
                .map(UserMemberTag::getTagId)
                .collect(Collectors.toSet());
    }
}
