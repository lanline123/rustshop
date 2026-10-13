// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleListDTO;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleSaveDTO;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleDetailVO;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleVO;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 公众号关键字自动回复服务（M2）。
 *
 * <p>对应需求 §5 与接口契约 §2：
 * <ul>
 *   <li>规则全局上限 200；单规则关键字 1..10、回复 1..5；规则名 ≤60、关键字 ≤30；</li>
 *   <li>保存时主表 upsert + 子表「先按 rule_id 清空再批量插入」，整体同事务；</li>
 *   <li>匹配规则「全匹配优先于半匹配」，同优先级按 {@code rule.created_at} 升序取首条。</li>
 * </ul>
 *
 * <p>写操作（save / del / setEnabled）成功后主动清除 {@link WechatKeywordRuleCache}，
 * 便于 Dispatcher 在下一次匹配时重新 reload 快照，避免因缓存 30s TTL 造成后台变更延迟生效。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
public interface WechatKeywordRuleService {

    /**
     * 管理后台列表分页。
     *
     * <p>直接返回 MyBatis-Plus {@link Page}，由前端按 {records,total,size,current,pages}
     * 契约消费；行内聚合字段 keywordCount / replyCount 由
     * {@code WechatKeywordRuleMapper.pageWithCount} 通过子查询拼接。
     *
     * @param dto 查询参数（page/size/ruleName/enabled）
     * @return 分页结果
     */
    Page<KeywordRuleVO> page(KeywordRuleListDTO dto);

    /**
     * 规则详情（包含按 sort_order 升序展开的关键字与回复内容）。
     * 规则不存在时抛 {@code GlobalException}。
     *
     * @param id 规则 ID
     * @return 详情 VO
     */
    KeywordRuleDetailVO detail(Long id);

    /**
     * 新增 / 编辑（按是否携带 id 区分）。
     *
     * <p>执行顺序：
     * <ol>
     *   <li>业务校验（关键字 / 回复数量、长度、重复、图文 URL 格式）；</li>
     *   <li>规则名全局唯一（忽略大小写，编辑时排除自身）；</li>
     *   <li>新增时校验规则总数 ≤ 200；</li>
     *   <li>主表 insert/updateById；</li>
     *   <li>子表按 ruleId delete → 批量 insert；</li>
     *   <li>清除匹配缓存。</li>
     * </ol>
     * 全流程位于同一事务内。
     *
     * @param dto 保存参数
     * @return 规则 id
     */
    Long save(KeywordRuleSaveDTO dto);

    /**
     * 批量删除规则（级联删除关键字与回复子表行）。空/null ids 视为无操作直接返回。
     *
     * @param ids 规则 id 集合
     */
    void del(List<Long> ids);

    /**
     * 批量启停规则。空/null ids 或 enabled=null 视为无操作直接返回。
     *
     * @param ids     规则 id 集合
     * @param enabled 0=停用 1=启用
     */
    void setEnabled(List<Long> ids, Integer enabled);

    /**
     * 供 Dispatcher 使用：根据用户消息文本匹配首条命中规则并返回可直接用于发送的回复集合。
     *
     * <p>匹配规则（需求 §5.4.1）：
     * <ol>
     *   <li>仅参与启用中的规则；</li>
     *   <li>扫描全部启用规则的关键字，「全匹配」优先于「半匹配」；</li>
     *   <li>同优先级下按 {@code rule.createdAt} 升序取首条；</li>
     *   <li>同一条消息最多命中一条规则；未命中返回 {@code null}。</li>
     * </ol>
     * 全匹配与半匹配均不区分大小写；半匹配使用 {@link String#contains} 语义。
     *
     * @param content 用户消息文本（预先 trim，空串不匹配）
     * @return 命中规则 + 其回复集合（按 sort_order 升序）；未命中返回 null
     */
    HitRule findFirstHitRule(String content);

    /**
     * 命中规则的中间结构，供 Dispatcher 决策 reply_mode（随机一条 / 全部）后交给渲染层。
     */
    @Data
    @AllArgsConstructor
    class HitRule {

        /** 命中规则 id */
        private Long ruleId;

        /** 命中规则名称（用于日志/审计） */
        private String ruleName;

        /** 回复模式：1=随机一条 2=回复全部 */
        private Integer replyMode;

        /** 规则下全部回复载荷，按 sort_order 升序，Dispatcher 依 replyMode 挑选 */
        private List<ReplyPayloadVO> replies;
    }
}
