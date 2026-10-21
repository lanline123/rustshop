// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.setting.wechat;

import com.tigshop.bean.model.setting.wechat.WechatKeywordRuleReply;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 公众号关键字回复-规则回复子表映射（对应需求 §8.4 wechat_keyword_rule_reply）。
 *
 * <p>所有自定义 SQL 均通过注解实现，避免再引入独立 XML 文件。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Mapper
public interface WechatKeywordRuleReplyMapper extends BaseMapper<WechatKeywordRuleReply> {

    /**
     * 查询某规则下全部回复项，按 sort_order 升序、id 升序稳定排序。
     *
     * @param ruleId 规则 ID
     * @return 回复项列表
     */
    @Select("SELECT id, rule_id, sort_order, msg_type, content_text, "
            + "news_title, news_cover_url, news_description, news_url "
            + "FROM wechat_keyword_rule_reply "
            + "WHERE rule_id = #{ruleId} "
            + "ORDER BY sort_order ASC, id ASC")
    List<WechatKeywordRuleReply> selectByRuleIdOrdered(@Param("ruleId") Long ruleId);

    /**
     * 批量查询多个规则下的回复项，按 rule_id、sort_order、id 升序返回，供匹配命中后批量渲染。
     *
     * <p>调用方需保证 {@code ruleIds} 非空，否则建议跳过调用。
     *
     * @param ruleIds 规则 ID 集合
     * @return 回复项列表（可能为空）
     */
    @Select("<script>"
            + "SELECT id, rule_id, sort_order, msg_type, content_text, "
            + "news_title, news_cover_url, news_description, news_url "
            + "FROM wechat_keyword_rule_reply "
            + "WHERE rule_id IN "
            + "<foreach collection='ruleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "ORDER BY rule_id ASC, sort_order ASC, id ASC"
            + "</script>")
    List<WechatKeywordRuleReply> selectByRuleIdsOrdered(@Param("ruleIds") Collection<Long> ruleIds);

    /**
     * 统计某规则下回复项数量，供需求 §4.3 单规则上限 5 条校验。
     *
     * @param ruleId 规则 ID
     * @return 回复项数量
     */
    @Select("SELECT COUNT(1) FROM wechat_keyword_rule_reply WHERE rule_id = #{ruleId}")
    int countByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 删除某规则下全部回复项，供更新与删除规则时先清后插。
     *
     * @param ruleId 规则 ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM wechat_keyword_rule_reply WHERE rule_id = #{ruleId}")
    int deleteByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 批量删除多个规则下全部回复项，供批量删除规则使用。
     *
     * @param ruleIds 规则 ID 集合，调用方保证非空
     * @return 受影响行数
     */
    @Delete("<script>"
            + "DELETE FROM wechat_keyword_rule_reply WHERE rule_id IN "
            + "<foreach collection='ruleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    int deleteByRuleIds(@Param("ruleIds") Collection<Long> ruleIds);
}
