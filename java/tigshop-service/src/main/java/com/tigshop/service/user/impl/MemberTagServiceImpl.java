package com.tigshop.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.user.MemberTag;
import com.tigshop.bean.model.user.MemberTagGroup;
import com.tigshop.bean.model.user.UserMemberTag;
import com.tigshop.bean.param.user.MemberTagCreateParam;
import com.tigshop.bean.param.user.MemberTagUpdateParam;
import com.tigshop.bean.vo.user.MemberTagVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.user.MemberTagGroupMapper;
import com.tigshop.mapper.user.MemberTagMapper;
import com.tigshop.mapper.user.UserMemberTagMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.MemberTagService;
import com.tigshop.service.user.ShopMemberSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会员标签管理。店铺隔离 + 冻结守卫（SHOP-MEMBER-20260708）。
 */
@Service
@RequiredArgsConstructor
public class MemberTagServiceImpl extends BaseServiceImpl<MemberTagMapper, MemberTag>
        implements MemberTagService {

    private final MemberTagGroupMapper memberTagGroupMapper;
    private final UserMemberTagMapper userMemberTagMapper;
    private final ShopMemberSettingService shopMemberSettingService;

    private int currentShopId() {
        Integer s = HeaderUtils.getShopId();
        return s == null ? 0 : s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(MemberTagCreateParam param) {
        int shopId = currentShopId();
        MemberTagGroup group = memberTagGroupMapper.selectById(param.getGroupId());
        if (group == null || group.getShopId() == null || group.getShopId() != shopId) {
            throw new GlobalException("标签组不存在");
        }
        MemberTag tag = new MemberTag();
        tag.setShopId(shopId);
        tag.setGroupId(param.getGroupId());
        tag.setTagName(param.getTagName().trim());
        getBaseMapper().insert(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MemberTagUpdateParam param) {
        int shopId = currentShopId();
        MemberTag tag = getBaseMapper().selectById(param.getId());
        if (tag == null || tag.getShopId() == null || tag.getShopId() != shopId) {
            throw new GlobalException("标签不存在");
        }
        tag.setTagName(param.getTagName().trim());
        getBaseMapper().updateById(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delTag(Long id) {
        int shopId = currentShopId();
        MemberTag tag = getBaseMapper().selectById(id);
        if (tag == null || tag.getShopId() == null || tag.getShopId() != shopId) {
            throw new GlobalException("标签不存在");
        }
        // 清理用户关联
        userMemberTagMapper.delete(
                new LambdaQueryWrapper<UserMemberTag>()
                        .eq(UserMemberTag::getShopId, shopId)
                        .eq(UserMemberTag::getTagId, id));
        // 逻辑删除标签
        getBaseMapper().deleteById(id);
    }

    @Override
    public List<MemberTagVO> listByGroupId(Long groupId) {
        int shopId = currentShopId();
        List<MemberTag> tags = getBaseMapper().selectList(
                new LambdaQueryWrapper<MemberTag>()
                        .eq(MemberTag::getShopId, shopId)
                        .eq(MemberTag::getGroupId, groupId));
        return tags.stream().map(tag -> {
            MemberTagVO vo = new MemberTagVO();
            vo.setId(tag.getId());
            vo.setGroupId(tag.getGroupId());
            vo.setTagName(tag.getTagName());
            vo.setCreateTime(tag.getCreateTime() != null ? tag.getCreateTime().toString() : null);
            return vo;
        }).toList();
    }
}
