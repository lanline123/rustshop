package com.tigshop.service.user;

import com.tigshop.bean.model.user.UserMemberTag;
import com.tigshop.bean.param.user.UserMemberTagBatchParam;
import com.tigshop.bean.param.user.UserMemberTagParam;
import com.tigshop.bean.vo.user.UserMemberTagVO;
import com.tigshop.service.common.BaseService;
import jakarta.validation.Valid;

import java.util.List;

public interface UserMemberTagService extends BaseService<UserMemberTag> {

    void bindTags(@Valid UserMemberTagParam param);

    void unbindTags(@Valid UserMemberTagParam param);

    void batchBindTags(@Valid UserMemberTagBatchParam param);

    void batchUnbindTags(@Valid UserMemberTagBatchParam param);

    List<UserMemberTagVO> listByUser(Long userId);

    /**
     * 按标签解析用户ID集合（供站内信群发、优惠券、促销等场景复用）
     *
     * @param tagIds    标签ID列表
     * @param matchMode 匹配方式：0=满足任一(默认) 1=满足全部
     * @return 命中的用户ID列表（去重）
     */
    List<Long> resolveUserIdsByTags(java.util.Collection<Long> tagIds, Integer matchMode);

    /**
     * 实时校验某个用户当前是否满足标签条件
     *
     * @param userId    用户ID
     * @param tagIds    标签ID列表
     * @param matchMode 匹配方式：0=满足任一(默认) 1=满足全部
     */
    boolean userMatchesTags(Long userId, java.util.Collection<Long> tagIds, Integer matchMode);

    /**
     * 取用户持有的全部标签ID。
     * <p>
     * 需要对同一用户连续判定多组标签条件时（如前台促销总线逐个活动判人群），用本方法一次取回后在内存比对，
     * 避免逐次调用 {@link #userMatchesTags} 按 tagId 反复查库。
     *
     * @return 标签ID集合；userId 为空时返回空集，不返回 null
     */
    java.util.Set<Long> listTagIdsByUser(Long userId);
}
