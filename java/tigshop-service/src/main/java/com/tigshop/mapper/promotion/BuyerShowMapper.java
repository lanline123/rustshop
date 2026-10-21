package com.tigshop.mapper.promotion;

import com.tigshop.bean.model.promotion.BuyerShow;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author wzh
*/
@Mapper
public interface BuyerShowMapper extends BaseMapper<BuyerShow> {

    /**
     * 视频流游标查询：只返回视频类买家秀，排序口径 sort_order ASC, add_time DESC, buyer_show_id DESC。
     *
     * @param userId          当前用户ID（未登录传0）
     * @param keyword         关键词（可空）
     * @param groupId         分组ID（可空）
     * @param anchorSortOrder 游标锚点 sort_order（anchorId 非空时生效）
     * @param anchorAddTime   游标锚点 add_time（anchorId 非空时生效）
     * @param anchorId        游标锚点买家秀ID（为空表示从头开始）
     * @param inclusive       是否包含锚点自身（列表点入首屏 true，续页 false）
     * @param excludeIds      需排除的ID（环绕补齐时避免同页重复）
     * @param limit           返回条数
     * @return 买家秀列表
     */
    List<BuyerShow> selectVideoFeed(@Param("userId") Integer userId,
                                    @Param("keyword") String keyword,
                                    @Param("groupId") Long groupId,
                                    @Param("anchorSortOrder") Integer anchorSortOrder,
                                    @Param("anchorAddTime") Long anchorAddTime,
                                    @Param("anchorId") Integer anchorId,
                                    @Param("inclusive") boolean inclusive,
                                    @Param("excludeIds") List<Integer> excludeIds,
                                    @Param("limit") int limit);
}
