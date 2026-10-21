// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleKeywordDTO;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleListDTO;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleSaveDTO;
import com.tigshop.bean.dto.setting.wechat.ReplyPayloadDTO;
import com.tigshop.bean.model.setting.wechat.WechatKeywordRule;
import com.tigshop.bean.model.setting.wechat.WechatKeywordRuleKeyword;
import com.tigshop.bean.model.setting.wechat.WechatKeywordRuleReply;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleDetailVO;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleVO;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.TransactionAfterCommitExecutor;
import com.tigshop.mapper.setting.wechat.WechatKeywordRuleKeywordMapper;
import com.tigshop.mapper.setting.wechat.WechatKeywordRuleMapper;
import com.tigshop.mapper.setting.wechat.WechatKeywordRuleReplyMapper;
import com.tigshop.service.setting.wechat.WechatKeywordRuleCache;
import com.tigshop.service.setting.wechat.WechatKeywordRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 公众号关键字自动回复服务实现（M2）。
 *
 * <p>关键设计：
 * <ul>
 *   <li>保存采用「主表 upsert + 子表先删后插」的级联写入策略，
 *       对标 {@code ShippingTplInfoServiceImpl#updateShippingTplInfo}；
 *       整体位于 {@link Transactional} 内，子表脏数据不会残留。</li>
 *   <li>匹配路径 {@link #findFirstHitRule(String)} 走
 *       {@link WechatKeywordRuleCache} 进程内 30s TTL 快照，
 *       避免每条消息打 3 次 DB；后台写操作通过
 *       {@link TransactionAfterCommitExecutor} 在 <b>事务提交之后</b> 触发
 *       {@link WechatKeywordRuleCache#invalidateAndBroadcast()}，
 *       同时失效当前 JVM + 通过 Redis Pub/Sub 广播到其它 JVM
 *       （{@code tigshop-adminapi} 与 {@code tigshop-api} 默认是两个独立进程），
 *       保证跨实例秒级生效。
 *       <b>严禁</b>把 {@code invalidate()} 放在事务内直接调用——那样会让
 *       并发入站消息的 loader 读到未提交的旧行并回填缓存（带 30s TTL），
 *       导致切换 replyMode / 启停需要等一段时间才生效。</li>
 *   <li>校验全部抛 {@link GlobalException}（默认 code=SERVICE_DATA_ERROR），
 *       文案风格对齐 {@code WechatAutoReplyServiceImpl} / {@code WechatMenuServiceImpl}。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WechatKeywordRuleServiceImpl implements WechatKeywordRuleService {

    // ====================== 业务上限常量（需求 §5.3） ======================

    /** 全局规则数上限 */
    private static final int MAX_RULES = 200;

    /** 单规则关键字数量下限 / 上限 */
    private static final int MIN_KEYWORDS = 1;
    private static final int MAX_KEYWORDS = 10;

    /** 单规则回复数量下限 / 上限 */
    private static final int MIN_REPLIES = 1;
    private static final int MAX_REPLIES = 5;

    /** 规则名最大长度 */
    private static final int MAX_RULE_NAME_LEN = 60;

    /** 关键字最大长度 */
    private static final int MAX_KEYWORD_LEN = 30;

    /** 关键字回复文本消息最大长度（关键字回复上下文为 300，不复用自动回复的 600） */
    private static final int MAX_CONTENT_LEN = 300;

    /** 图文标题最大长度 */
    private static final int MAX_NEWS_TITLE_LEN = 64;

    /** 图文摘要最大长度 */
    private static final int MAX_NEWS_DESC_LEN = 120;

    /** URL 字段最大长度（封面图、跳转链接） */
    private static final int MAX_URL_LEN = 500;

    /** 回复模式：1=随机一条 2=全部回复 */
    private static final int REPLY_MODE_RANDOM = 1;
    private static final int REPLY_MODE_ALL = 2;

    /** 启用状态：0=停用 1=启用 */
    private static final int ENABLED_OFF = 0;
    private static final int ENABLED_ON = 1;

    /** 匹配方式：1=全匹配 2=半匹配 */
    private static final int MATCH_TYPE_FULL = 1;
    private static final int MATCH_TYPE_PARTIAL = 2;

    /** 消息类型：1=文本 2=图文 */
    private static final int MSG_TYPE_TEXT = 1;
    private static final int MSG_TYPE_NEWS = 2;

    // ====================== 依赖注入 ======================

    private final WechatKeywordRuleMapper ruleMapper;

    private final WechatKeywordRuleKeywordMapper keywordMapper;

    private final WechatKeywordRuleReplyMapper replyMapper;

    private final WechatKeywordRuleCache cache;

    // ====================== Service 接口实现 ======================

    @Override
    public Page<KeywordRuleVO> page(KeywordRuleListDTO dto) {
        Page<KeywordRuleVO> page = new Page<>(dto.getPage(), dto.getSize());
        String ruleName = StrUtil.isBlank(dto.getRuleName()) ? null : dto.getRuleName().trim();
        Page<KeywordRuleVO> result = ruleMapper.pageWithCount(page, ruleName, dto.getEnabled());
        fillKeywordsForPage(result);
        return result;
    }

    /**
     * 为当前页每条 {@link KeywordRuleVO} 批量回填其关键字列表。
     * <p>采用「一次 IN 查询 + 按 rule_id 分组」的做法，相比在列表 SQL 中用
     * {@code GROUP_CONCAT} 更稳健：不会受 {@code group_concat_max_len}（默认 1024）限制，
     * 也无需前端再解析拼接串；N 通常 ≤ pageSize（15），性能开销可忽略。
     */
    private void fillKeywordsForPage(Page<KeywordRuleVO> page) {
        List<KeywordRuleVO> records = page.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return;
        }
        List<Long> ruleIds = records.stream()
                .map(KeywordRuleVO::getId)
                .collect(Collectors.toList());
        Map<Long, List<String>> kwMap = new LinkedHashMap<>();
        for (WechatKeywordRuleKeyword k : keywordMapper.selectByRuleIdsOrdered(ruleIds)) {
            kwMap.computeIfAbsent(k.getRuleId(), id -> new ArrayList<>()).add(k.getKeyword());
        }
        for (KeywordRuleVO vo : records) {
            vo.setKeywords(kwMap.getOrDefault(vo.getId(), new ArrayList<>()));
        }
    }

    @Override
    public KeywordRuleDetailVO detail(Long id) {
        if (id == null) {
            throw new GlobalException("规则ID不能为空");
        }
        WechatKeywordRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new GlobalException("规则不存在");
        }
        List<WechatKeywordRuleKeyword> keywords = keywordMapper.selectByRuleIdOrdered(id);
        List<WechatKeywordRuleReply> replies = replyMapper.selectByRuleIdOrdered(id);

        KeywordRuleDetailVO vo = new KeywordRuleDetailVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setReplyMode(rule.getReplyMode());
        vo.setEnabled(rule.getEnabled());
        vo.setCreatedAt(rule.getCreatedAt());
        vo.setUpdatedAt(rule.getUpdatedAt());
        vo.setKeywords(keywords.stream().map(k -> {
            KeywordRuleDetailVO.KeywordRuleKeywordItem item = new KeywordRuleDetailVO.KeywordRuleKeywordItem();
            item.setKeyword(k.getKeyword());
            item.setMatchType(k.getMatchType());
            return item;
        }).collect(Collectors.toList()));
        vo.setReplies(replies.stream().map(this::toReplyPayloadVO).collect(Collectors.toList()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long save(KeywordRuleSaveDTO dto) {
        if (dto == null) {
            throw new GlobalException("保存参数不能为空");
        }
        // 1. 业务校验（数量 / 长度 / 重复 / 图文字段）
        validateSave(dto);

        // 2. 规则名全局唯一（忽略大小写；编辑时排除自身 id）
        validateRuleNameUnique(dto.getRuleName().trim(), dto.getId());

        // 3. 新增时做 200 条全局上限校验
        if (dto.getId() == null) {
            int total = ruleMapper.countRules();
            if (total >= MAX_RULES) {
                throw new GlobalException("规则总数已达上限 " + MAX_RULES + " 条，无法继续新增");
            }
        }

        // 4. 主表 upsert
        LocalDateTime now = LocalDateTime.now();
        WechatKeywordRule rule;
        if (dto.getId() == null) {
            rule = new WechatKeywordRule();
            rule.setRuleName(dto.getRuleName().trim());
            rule.setReplyMode(dto.getReplyMode());
            rule.setEnabled(dto.getEnabled());
            rule.setCreatedAt(now);
            rule.setUpdatedAt(now);
            ruleMapper.insert(rule);
        } else {
            rule = ruleMapper.selectById(dto.getId());
            if (rule == null) {
                throw new GlobalException("规则不存在");
            }
            rule.setRuleName(dto.getRuleName().trim());
            rule.setReplyMode(dto.getReplyMode());
            rule.setEnabled(dto.getEnabled());
            rule.setUpdatedAt(now);
            ruleMapper.updateById(rule);
        }

        // 5. 子表：按 rule_id 先清后插（简单策略，避免 diff 复杂度；行数上限 10+5，性能可接受）
        Long ruleId = rule.getId();
        keywordMapper.deleteByRuleId(ruleId);
        replyMapper.deleteByRuleId(ruleId);

        List<KeywordRuleKeywordDTO> kwDtos = dto.getKeywords();
        for (int i = 0; i < kwDtos.size(); i++) {
            KeywordRuleKeywordDTO k = kwDtos.get(i);
            WechatKeywordRuleKeyword po = new WechatKeywordRuleKeyword();
            po.setRuleId(ruleId);
            po.setKeyword(k.getKeyword().trim());
            po.setMatchType(k.getMatchType());
            po.setSortOrder(i);
            keywordMapper.insert(po);
        }

        List<ReplyPayloadDTO> rpDtos = dto.getReplies();
        for (int i = 0; i < rpDtos.size(); i++) {
            ReplyPayloadDTO r = rpDtos.get(i);
            WechatKeywordRuleReply po = new WechatKeywordRuleReply();
            po.setRuleId(ruleId);
            po.setSortOrder(i);
            po.setMsgType(r.getMsgType());
            if (r.getMsgType() == MSG_TYPE_TEXT) {
                po.setContentText(r.getContent());
            } else {
                po.setNewsTitle(r.getNewsTitle());
                po.setNewsCoverUrl(r.getNewsCoverUrl());
                po.setNewsDescription(r.getNewsDescription());
                po.setNewsUrl(r.getNewsUrl());
            }
            replyMapper.insert(po);
        }

        // 6. 清除匹配缓存：必须在事务「提交之后」才触发失效。
        //    - 在事务内直接清空：期间并发的入站消息会走 loader 读到「事务未提交的旧行」，
        //      再把旧快照塞回缓存并打 30s TTL，导致切换 replyMode / 启停 后需要等一段时间才生效。
        //    - 用 invalidateAndBroadcast：本地失效 + Redis Pub/Sub 通知 tigshop-api / 其它 JVM 也失效，
        //      避免「adminapi 改完 api 端还在 30s TTL 内」的跨进程脏读窗口。
        final Long loggedRuleId = rule.getId();
        final Integer loggedReplyMode = rule.getReplyMode();
        TransactionAfterCommitExecutor.execute(() -> {
            cache.invalidateAndBroadcast();
            log.info("[KeywordRuleCache] save afterCommit 已清除并广播缓存失效 ruleId={} replyMode={}",
                    loggedRuleId, loggedReplyMode);
        });
        return ruleId;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void del(List<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        keywordMapper.deleteByRuleIds(ids);
        replyMapper.deleteByRuleIds(ids);
        ruleMapper.delete(new LambdaQueryWrapper<WechatKeywordRule>()
                .in(WechatKeywordRule::getId, ids));
        // 事务提交后本地失效 + 跨 JVM 广播（见 save 注释）
        TransactionAfterCommitExecutor.execute(cache::invalidateAndBroadcast);
    }

    @Override
    public void setEnabled(List<Long> ids, Integer enabled) {
        if (CollectionUtil.isEmpty(ids) || enabled == null) {
            return;
        }
        if (enabled != ENABLED_OFF && enabled != ENABLED_ON) {
            throw new GlobalException("启用状态仅支持 0=停用 / 1=启用");
        }
        LambdaUpdateWrapper<WechatKeywordRule> w = new LambdaUpdateWrapper<>();
        w.in(WechatKeywordRule::getId, ids)
                .set(WechatKeywordRule::getEnabled, enabled)
                .set(WechatKeywordRule::getUpdatedAt, LocalDateTime.now());
        ruleMapper.update(null, w);
        // 与 save / del 保持一致：若被外层事务包裹则延到 afterCommit 再失效；
        // 裸调用时（当前场景）走直接失效分支。本地失效同时通过 Redis Pub/Sub 广播到其它 JVM。
        TransactionAfterCommitExecutor.execute(cache::invalidateAndBroadcast);
    }

    @Override
    public HitRule findFirstHitRule(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        String trimmed = content.trim();
        String lowered = trimmed.toLowerCase();

        WechatKeywordRuleCache.Snapshot snap = cache.snapshot(this::loadSnapshot);
        List<WechatKeywordRuleCache.RuleEntry> rules = snap.getRules();
        if (CollectionUtil.isEmpty(rules)) {
            return null;
        }

        // Pass 1: 全匹配优先（需求 §5.4.1）；rules 已按 createdAt asc / id asc 排序，直接取首条命中
        for (WechatKeywordRuleCache.RuleEntry r : rules) {
            for (WechatKeywordRuleCache.KeywordEntry k : r.getKeywords()) {
                if (k.getMatchType() != null
                        && k.getMatchType() == MATCH_TYPE_FULL
                        && k.getKeyword() != null
                        && trimmed.equalsIgnoreCase(k.getKeyword())) {
                    return toHitRule(r);
                }
            }
        }
        // Pass 2: 半匹配（contains，不区分大小写）
        for (WechatKeywordRuleCache.RuleEntry r : rules) {
            for (WechatKeywordRuleCache.KeywordEntry k : r.getKeywords()) {
                if (k.getMatchType() != null
                        && k.getMatchType() == MATCH_TYPE_PARTIAL
                        && StrUtil.isNotBlank(k.getKeyword())
                        && lowered.contains(k.getKeyword().toLowerCase())) {
                    return toHitRule(r);
                }
            }
        }
        return null;
    }

    // ====================== 私有辅助：快照加载 ======================

    /**
     * 全量加载启用中的规则 + 其 keywords / replies，按 createdAt asc / id asc 排序。
     * 无启用规则时返回空快照；异常由 {@link WechatKeywordRuleCache#snapshot} 捕获。
     */
    private WechatKeywordRuleCache.Snapshot loadSnapshot() {
        log.info("[KeywordRuleCache] loadSnapshot 触发（缓存失效或首次加载）");
        List<WechatKeywordRule> enabledRules = ruleMapper.selectList(
                new LambdaQueryWrapper<WechatKeywordRule>()
                        .eq(WechatKeywordRule::getEnabled, ENABLED_ON)
                        .orderByAsc(WechatKeywordRule::getCreatedAt, WechatKeywordRule::getId));
        if (CollectionUtil.isEmpty(enabledRules)) {
            return WechatKeywordRuleCache.Snapshot.empty();
        }
        List<Long> ruleIds = enabledRules.stream()
                .map(WechatKeywordRule::getId)
                .collect(Collectors.toList());

        Map<Long, List<WechatKeywordRuleCache.KeywordEntry>> kwMap = new LinkedHashMap<>();
        for (WechatKeywordRuleKeyword k : keywordMapper.selectByRuleIdsOrdered(ruleIds)) {
            kwMap.computeIfAbsent(k.getRuleId(), id -> new ArrayList<>())
                    .add(new WechatKeywordRuleCache.KeywordEntry(k.getKeyword(), k.getMatchType()));
        }
        Map<Long, List<ReplyPayloadVO>> rpMap = new LinkedHashMap<>();
        for (WechatKeywordRuleReply r : replyMapper.selectByRuleIdsOrdered(ruleIds)) {
            rpMap.computeIfAbsent(r.getRuleId(), id -> new ArrayList<>())
                    .add(toReplyPayloadVO(r));
        }

        List<WechatKeywordRuleCache.RuleEntry> entries = new ArrayList<>(enabledRules.size());
        for (WechatKeywordRule rule : enabledRules) {
            List<WechatKeywordRuleCache.KeywordEntry> kws =
                    kwMap.getOrDefault(rule.getId(), new ArrayList<>());
            List<ReplyPayloadVO> rps =
                    rpMap.getOrDefault(rule.getId(), new ArrayList<>());
            // 跳过没有关键字或没有回复的规则（数据不完整时不应参与匹配）
            if (kws.isEmpty() || rps.isEmpty()) {
                continue;
            }
            entries.add(new WechatKeywordRuleCache.RuleEntry(
                    rule.getId(),
                    rule.getRuleName(),
                    rule.getReplyMode(),
                    rule.getCreatedAt(),
                    kws,
                    rps));
        }
        // 双保险：SQL 已按 createdAt asc 排，这里再按 createdAt asc / id asc 兜底
        entries.sort(Comparator
                .comparing(WechatKeywordRuleCache.RuleEntry::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(WechatKeywordRuleCache.RuleEntry::getRuleId,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        if (log.isInfoEnabled()) {
            String summary = entries.stream()
                    .map(e -> e.getRuleId() + ":mode=" + e.getReplyMode())
                    .collect(Collectors.joining(", "));
            log.info("[KeywordRuleCache] loadSnapshot 完成，count={} detail=[{}]", entries.size(), summary);
        }
        return new WechatKeywordRuleCache.Snapshot(entries);
    }

    private HitRule toHitRule(WechatKeywordRuleCache.RuleEntry r) {
        // 复制 replies 引用即可；ReplyPayloadVO 为只读载荷，Dispatcher 不会改写其字段
        return new HitRule(r.getRuleId(), r.getRuleName(), r.getReplyMode(), r.getReplies());
    }

    private ReplyPayloadVO toReplyPayloadVO(WechatKeywordRuleReply po) {
        ReplyPayloadVO vo = new ReplyPayloadVO();
        vo.setMsgType(po.getMsgType());
        vo.setContent(po.getContentText());
        vo.setNewsTitle(po.getNewsTitle());
        vo.setNewsCoverUrl(po.getNewsCoverUrl());
        vo.setNewsDescription(po.getNewsDescription());
        vo.setNewsUrl(po.getNewsUrl());
        return vo;
    }

    // ====================== 私有校验 ======================

    /**
     * 保存参数业务深度校验：
     * <ol>
     *   <li>规则名长度 ≤ {@value #MAX_RULE_NAME_LEN}；回复模式仅 1/2；启用状态仅 0/1。</li>
     *   <li>关键字数量 [1..10]；单关键字 ≤ {@value #MAX_KEYWORD_LEN}；
     *       matchType 仅 1/2；同 matchType 下 keyword 不可重复（忽略大小写）。</li>
     *   <li>回复数量 [1..5]；msgType 仅 1/2；
     *       文本：content 非空 && ≤ {@value #MAX_CONTENT_LEN}；
     *       图文：title/desc/coverUrl/newsUrl 全部非空，长度与 URL 合法性校验。</li>
     * </ol>
     */
    private void validateSave(KeywordRuleSaveDTO dto) {
        // 规则名
        if (StrUtil.isBlank(dto.getRuleName())) {
            throw new GlobalException("规则名称不能为空");
        }
        String trimmedName = dto.getRuleName().trim();
        if (trimmedName.isEmpty() || trimmedName.length() > MAX_RULE_NAME_LEN) {
            throw new GlobalException("规则名称长度需在 1.." + MAX_RULE_NAME_LEN + " 之间");
        }

        // 回复模式
        Integer replyMode = dto.getReplyMode();
        if (replyMode == null || (replyMode != REPLY_MODE_RANDOM && replyMode != REPLY_MODE_ALL)) {
            throw new GlobalException("回复模式仅支持 1=随机一条 / 2=全部回复");
        }

        // 启用状态
        Integer enabled = dto.getEnabled();
        if (enabled == null || (enabled != ENABLED_OFF && enabled != ENABLED_ON)) {
            throw new GlobalException("启用状态仅支持 0=停用 / 1=启用");
        }

        // 关键字列表
        List<KeywordRuleKeywordDTO> keywords = dto.getKeywords();
        if (keywords == null || keywords.size() < MIN_KEYWORDS || keywords.size() > MAX_KEYWORDS) {
            throw new GlobalException("关键字数量需在 " + MIN_KEYWORDS + ".." + MAX_KEYWORDS + " 之间");
        }
        Set<String> kwDedup = new HashSet<>();
        for (KeywordRuleKeywordDTO k : keywords) {
            if (k == null || StrUtil.isBlank(k.getKeyword())) {
                throw new GlobalException("关键字不能为空");
            }
            String trimmed = k.getKeyword().trim();
            if (trimmed.length() > MAX_KEYWORD_LEN) {
                throw new GlobalException("关键字「" + trimmed + "」长度不能超过 " + MAX_KEYWORD_LEN);
            }
            Integer mt = k.getMatchType();
            if (mt == null || (mt != MATCH_TYPE_FULL && mt != MATCH_TYPE_PARTIAL)) {
                throw new GlobalException("关键字「" + trimmed + "」匹配方式仅支持 1=全匹配 / 2=半匹配");
            }
            // 去重 key = matchType + 小写 keyword；避免同规则下「全匹配 hello」与「全匹配 Hello」重复
            String dedupKey = mt + "#" + trimmed.toLowerCase();
            if (!kwDedup.add(dedupKey)) {
                throw new GlobalException("同一匹配方式下关键字不可重复：「" + trimmed + "」");
            }
        }

        // 回复列表
        List<ReplyPayloadDTO> replies = dto.getReplies();
        if (replies == null || replies.size() < MIN_REPLIES || replies.size() > MAX_REPLIES) {
            throw new GlobalException("回复数量需在 " + MIN_REPLIES + ".." + MAX_REPLIES + " 之间");
        }
        for (int i = 0; i < replies.size(); i++) {
            validateReplyPayload(replies.get(i), i);
        }
    }

    /**
     * 单条回复载荷校验（关键字回复上下文：文本 ≤ 300，区别于自动回复 600）。
     */
    private void validateReplyPayload(ReplyPayloadDTO dto, int idx) {
        String pos = "第 " + (idx + 1) + " 条回复";
        if (dto == null || dto.getMsgType() == null) {
            throw new GlobalException(pos + "：消息类型不能为空");
        }
        Integer msgType = dto.getMsgType();
        if (msgType != MSG_TYPE_TEXT && msgType != MSG_TYPE_NEWS) {
            throw new GlobalException(pos + "：消息类型仅支持 1=文本 / 2=图文");
        }
        if (msgType == MSG_TYPE_TEXT) {
            if (StrUtil.isBlank(dto.getContent())) {
                throw new GlobalException(pos + "：文本内容不能为空");
            }
            if (dto.getContent().length() > MAX_CONTENT_LEN) {
                throw new GlobalException(pos + "：文本内容长度不能超过 " + MAX_CONTENT_LEN);
            }
            return;
        }
        // msgType == 2 图文
        if (StrUtil.isBlank(dto.getNewsTitle()) || dto.getNewsTitle().length() > MAX_NEWS_TITLE_LEN) {
            throw new GlobalException(pos + "：图文标题不能为空且长度不能超过 " + MAX_NEWS_TITLE_LEN);
        }
        if (StrUtil.isBlank(dto.getNewsDescription()) || dto.getNewsDescription().length() > MAX_NEWS_DESC_LEN) {
            throw new GlobalException(pos + "：图文摘要不能为空且长度不能超过 " + MAX_NEWS_DESC_LEN);
        }
        if (!isValidHttpUrl(dto.getNewsCoverUrl())) {
            throw new GlobalException(pos + "：图文封面图 URL 必须为合法的 http/https 链接，且长度不能超过 " + MAX_URL_LEN);
        }
        if (!isValidHttpUrl(dto.getNewsUrl())) {
            throw new GlobalException(pos + "：图文跳转链接必须为合法的 http/https 链接，且长度不能超过 " + MAX_URL_LEN);
        }
    }

    /**
     * 规则名唯一性校验（忽略大小写）。编辑时排除自身 id。
     * 使用 {@code LOWER(rule_name) = LOWER(?)} 表达式确保与 DDL 列排序规则解耦。
     */
    private void validateRuleNameUnique(String ruleName, Long excludeId) {
        LambdaQueryWrapper<WechatKeywordRule> w = new LambdaQueryWrapper<>();
        w.apply("LOWER(rule_name) = LOWER({0})", ruleName);
        if (excludeId != null) {
            w.ne(WechatKeywordRule::getId, excludeId);
        }
        w.last("LIMIT 1");
        WechatKeywordRule existed = ruleMapper.selectOne(w);
        if (existed != null) {
            throw new GlobalException("规则名称已存在：" + ruleName);
        }
    }

    private boolean isValidHttpUrl(String url) {
        return StrUtil.isNotBlank(url)
                && url.length() <= MAX_URL_LEN
                && (StrUtil.startWithIgnoreCase(url, "http://") || StrUtil.startWithIgnoreCase(url, "https://"));
    }
}
