package com.tigshop.service.wholesale.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.user.UserDTO;
import com.tigshop.bean.dto.user.UserListDTO;
import com.tigshop.bean.dto.wholesale.WholesalerAddDTO;
import com.tigshop.bean.dto.wholesale.WholesalerImportDTO;
import com.tigshop.bean.dto.wholesale.WholesalerRemoveByGradeDTO;
import com.tigshop.bean.dto.wholesale.WholesalerRemoveDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.FromTag;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.query.wholesale.WholesalerPageQuery;
import com.tigshop.bean.vo.wholesale.WholesalerImportResultVO;
import com.tigshop.bean.vo.wholesale.WholesalerSelectableUserVO;
import com.tigshop.bean.vo.wholesale.WholesalerVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.wholesale.WholesalerMapper;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.wholesale.WholesaleSettingService;
import com.tigshop.service.wholesale.WholesalerGradeService;
import com.tigshop.service.wholesale.WholesalerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 批发商身份 Service 实现。
 *
 * <p>关键设计：
 * <ul>
 *   <li>缓存层：复用项目 {@link StringRedisTemplate}（与 CategoryFeeRuleMatchServiceImpl 同惯例）；
 *       key {@code wholesale:isBuyer:{userId}}，值 "1"/"0"，TTL 10 分钟；全量失效用 SCAN+DEL。</li>
 *   <li>列表回填（CR-VOS-1）：不写 JOIN SQL，wholesaler 主表分页后按 IN 批量回填 user/user_rank/grade。</li>
 *   <li>keyword/fromTag（CR-VOS-2）：先在 user 表按 username/mobile/email/nickname LIKE + fromTag 过滤出 userIds，
 *       再用 IN 限定 wholesaler 分页；空集直接返回空页。</li>
 *   <li>错误码全部走 {@link HttpStatus} WHOLESALE_* 常量。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WholesalerServiceImpl implements WholesalerService {

    /** 批发商身份缓存 key 前缀（C 端高频判定） */
    private static final String CACHE_KEY_PREFIX = "wholesale:isBuyer:";
    private static final long CACHE_TTL_SECONDS = 600L;
    private static final int BATCH_SIZE = 500;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneOffset ZONE = ZoneOffset.ofHours(8);

    private final WholesalerMapper wholesalerMapper;
    private final WholesalerGradeService gradeService;
    private final WholesaleSettingService settingService;
    private final UserService userService;
    private final UserRankService userRankService;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ConfigService configService;

    // ---------------------------------------------------------------------
    // C 端高频判定 + 缓存
    // ---------------------------------------------------------------------

    @Override
    public boolean isWholesaleBuyer(Integer userId) {
        // 短路 1：批发功能未启用 → false，不写缓存（开关变化由 invalidateAllBuyerCache 处理）
        if (!settingService.isEnabled()) {
            return false;
        }
        // 短路 2：非法 userId
        if (userId == null || userId <= 0) {
            return false;
        }
        String key = CACHE_KEY_PREFIX + userId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if ("1".equals(cached)) {
                return true;
            }
            if ("0".equals(cached)) {
                return false;
            }
        } catch (Exception e) {
            log.warn("读取批发商身份缓存失败 userId={}", userId, e);
        }

        Wholesaler w = queryActive(userId);
        boolean isBuyer = w != null;
        try {
            stringRedisTemplate.opsForValue().set(key, isBuyer ? "1" : "0", CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入批发商身份缓存失败 userId={}", userId, e);
        }
        return isBuyer;
    }

    @Override
    public void invalidateBuyerCache(Integer userId) {
        if (userId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(CACHE_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("失效批发商身份缓存失败 userId={}", userId, e);
        }
    }

    @Override
    public void invalidateAllBuyerCache() {
        // SCAN match wholesale:isBuyer:* + DEL（与 CategoryFeeRuleMatchServiceImpl.deleteByPattern 同惯例，避免 KEYS 阻塞）
        String pattern = CACHE_KEY_PREFIX + "*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("全量失效批发商身份缓存失败 pattern={}", pattern, e);
        }
    }

    @Override
    public Wholesaler getByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return queryActive(userId);
    }

    @Override
    public Integer getGradeIdByUserId(Integer userId) {
        Wholesaler w = getByUserId(userId);
        return w != null ? w.getGradeId() : 0;
    }

    /** 查询启用中的批发商身份（is_delete 由 @TableLogic 自动过滤） */
    private Wholesaler queryActive(Integer userId) {
        LambdaQueryWrapper<Wholesaler> qw = new LambdaQueryWrapper<>();
        qw.eq(Wholesaler::getUserId, userId).eq(Wholesaler::getStatus, 1);
        return wholesalerMapper.selectOne(qw);
    }

    // ---------------------------------------------------------------------
    // 后台分页（CR-VOS-1 回填 + CR-VOS-2 联合搜索/筛选）
    // ---------------------------------------------------------------------

    @Override
    public IPage<WholesalerVO> page(WholesalerPageQuery query) {
        int pageNo = query.getPage() != null && query.getPage() > 0 ? query.getPage() : 1;
        int pageSize = query.getSize() != null && query.getSize() > 0 ? Math.min(query.getSize(), 100) : 20;
        Page<Wholesaler> page = new Page<>(pageNo, pageSize);

        // CR-VOS-2：keyword + fromTag 须先按 user 过滤出 userIds，再 IN 限定 wholesaler
        boolean hasUserFilter = hasText(query.getKeyword()) || query.getFromTag() != null;
        Set<Integer> filteredUserIds = null;
        if (hasUserFilter) {
            filteredUserIds = searchUserIds(query.getKeyword(), query.getFromTag());
            if (filteredUserIds.isEmpty()) {
                return new Page<>(pageNo, pageSize, 0);
            }
        }

        LambdaQueryWrapper<Wholesaler> qw = new LambdaQueryWrapper<>();
        if (filteredUserIds != null) {
            qw.in(Wholesaler::getUserId, filteredUserIds);
        }
        if (query.getGradeId() != null) {
            qw.eq(Wholesaler::getGradeId, query.getGradeId());
        }
        if (query.getStatus() != null) {
            qw.eq(Wholesaler::getStatus, query.getStatus());
        }
        if (query.getSource() != null) {
            qw.eq(Wholesaler::getSource, query.getSource());
        }
        Long start = parseTimeToUnix(query.getAddTimeStart());
        Long end = parseTimeToUnix(query.getAddTimeEnd());
        if (start != null) {
            qw.ge(Wholesaler::getCreateTime, start);
        }
        if (end != null) {
            qw.le(Wholesaler::getCreateTime, end);
        }
        qw.orderByDesc(Wholesaler::getWholesalerId);

        IPage<Wholesaler> raw = wholesalerMapper.selectPage(page, qw);
        List<Wholesaler> records = raw.getRecords();
        if (records.isEmpty()) {
            return raw.convert(w -> new WholesalerVO());
        }

        // CR-VOS-1：批量回填 user / user_rank / wholesaler_grade（IN 查询，不写 JOIN SQL）
        List<Integer> userIds = records.stream().map(Wholesaler::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, User> userMap = mapUsersByIds(userIds);

        List<Integer> rankIds = userMap.values().stream()
                .map(User::getRankId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, UserRank> rankMap = mapRanksByIds(rankIds);

        List<Integer> gradeIds = records.stream()
                .map(Wholesaler::getGradeId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, WholesalerGrade> gradeMap = gradeService.mapByIds(gradeIds);

        // 默认头像兜底：会员未设置头像时回填配置项默认头像（与评论/IM/分销员等模块同惯例）
        String defaultAvatar = resolveDefaultAvatar();

        return raw.convert(w -> toVO(w, userMap, rankMap, gradeMap, defaultAvatar));
    }

    // ---------------------------------------------------------------------
    // 可选为批发商的会员分页（添加批发商选品用，2026-06-11）
    // ---------------------------------------------------------------------

    @Override
    public IPage<WholesalerSelectableUserVO> selectableUserPage(UserListDTO query) {
        // 复用会员列表查询（含全部筛选 + mobile @Sensitive 仅 demo 脱敏），分页口径完全一致
        Page<UserDTO> userPage = userService.list(query);
        List<UserDTO> users = userPage.getRecords();

        Page<WholesalerSelectableUserVO> result =
                new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        if (users == null || users.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 收集本页 userId，批量查有效批发商（is_delete 由 @TableLogic 过滤；含禁用态 status=0）
        List<Integer> userIds = users.stream().map(UserDTO::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, Wholesaler> wholesalerMap = userIds.isEmpty() ? Collections.emptyMap()
                : wholesalerMapper.selectList(new LambdaQueryWrapper<Wholesaler>()
                        .in(Wholesaler::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(Wholesaler::getUserId, w -> w, (a, b) -> a));

        // 批发等级名称回填
        List<Integer> gradeIds = wholesalerMap.values().stream()
                .map(Wholesaler::getGradeId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, WholesalerGrade> gradeMap = gradeService.mapByIds(gradeIds);

        List<WholesalerSelectableUserVO> records = users.stream().map(u -> {
            WholesalerSelectableUserVO vo = new WholesalerSelectableUserVO();
            BeanUtils.copyProperties(u, vo);
            Wholesaler w = wholesalerMap.get(u.getUserId());
            vo.setIsWholesaler(w != null);
            if (w != null) {
                vo.setGradeId(w.getGradeId());
                WholesalerGrade grade = w.getGradeId() != null ? gradeMap.get(w.getGradeId()) : null;
                vo.setGradeName(grade != null ? grade.getGradeName() : null);
            }
            return vo;
        }).collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    /**
     * 取会员默认头像配置（{@link SettingsEnum#DEFAULT_AVATAR}）；配置缺失时返回空串，不抛异常。
     */
    private String resolveDefaultAvatar() {
        ConfigPO config = configService.getConfigByCode(SettingsEnum.DEFAULT_AVATAR.getBizCode());
        return config != null && config.getBizVal() != null ? config.getBizVal() : "";
    }

    private WholesalerVO toVO(Wholesaler w, Map<Integer, User> userMap,
                              Map<Integer, UserRank> rankMap, Map<Integer, WholesalerGrade> gradeMap,
                              String defaultAvatar) {
        WholesalerVO vo = new WholesalerVO();
        // wholesaler 主表字段
        vo.setWholesalerId(w.getWholesalerId());
        vo.setUserId(w.getUserId());
        vo.setGradeId(w.getGradeId());
        vo.setStatus(w.getStatus());
        vo.setSource(w.getSource());
        vo.setSourceName(sourceName(w.getSource()));
        vo.setRemark(w.getRemark());
        vo.setAddTime(formatTime(w.getCreateTime()));

        // user 表回填（CR-VOS-1）
        User u = userMap.get(w.getUserId());
        if (u != null) {
            vo.setUsername(u.getUsername());
            // 不再写死脱敏（2026-06-11 用户决策）：返回原值，由 WholesalerVO 上的 @Sensitive
            // 仅在 demo 站（tigshop.is-demo=1）脱敏；正常环境返回完整手机号/邮箱
            vo.setMobile(u.getMobile());
            vo.setEmail(u.getEmail());
            vo.setNickname(u.getNickname());
            vo.setAvatar(StrUtil.isEmpty(u.getAvatar()) ? defaultAvatar : u.getAvatar());
            vo.setRankId(u.getRankId());
            vo.setIsCompanyAuth(u.getIsCompanyAuth());
            vo.setAuthStatusName(Integer.valueOf(1).equals(u.getIsCompanyAuth()) ? "已认证" : "未认证");
            vo.setFromTag(u.getFromTag());
            vo.setFromTagName(u.getFromTag() != null ? FromTag.getFromTagName(u.getFromTag()) : "");
            vo.setUserStatus(u.getStatus());

            // user_rank 回填
            if (u.getRankId() != null) {
                UserRank r = rankMap.get(u.getRankId());
                if (r != null) {
                    vo.setRankName(r.getRankName());
                }
            }
        }

        // wholesaler_grade 回填
        WholesalerGrade g = gradeMap.get(w.getGradeId());
        if (g != null) {
            vo.setGradeName(g.getGradeName());
        }
        return vo;
    }

    /** CR-VOS-2：user 表按 username/mobile/email/nickname 联合 LIKE + fromTag 过滤，返回 userIds */
    private Set<Integer> searchUserIds(String keyword, Integer fromTag) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.select(User::getUserId);
        if (hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getMobile, kw)
                    .or().like(User::getEmail, kw)
                    .or().like(User::getNickname, kw));
        }
        if (fromTag != null) {
            qw.eq(User::getFromTag, fromTag);
        }
        return userMapper.selectList(qw).stream()
                .map(User::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Map<Integer, User> mapUsersByIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u, (a, b) -> a));
    }

    private Map<Integer, UserRank> mapRanksByIds(List<Integer> rankIds) {
        if (rankIds == null || rankIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRankService.listByIds(rankIds).stream()
                .collect(Collectors.toMap(UserRank::getRankId, r -> r, (a, b) -> a));
    }

    // ---------------------------------------------------------------------
    // 添加 / 批量导入
    // ---------------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(WholesalerAddDTO dto) {
        // 不强制批发总开关已启用：允许总开关关闭时先备好批发商身份；C 端 isWholesaleBuyer
        // 仍被总开关短路，故开关开启后才真正生效（用户决策 2026-06-11）
        if (userService.getById(dto.getUserId()) == null) {
            throw new GlobalException("用户不存在", HttpStatus.WHOLESALE_USER_NOT_FOUND);
        }
        gradeService.requireEnabled(dto.getGradeId());

        long now = Instant.now().getEpochSecond();
        String remark = dto.getRemark() != null ? dto.getRemark() : "";

        // 判重含软删行：uk_user_id 不含 is_delete，软删行仍占 user_id，直接 insert 会撞唯一键。
        // 命中未删行 → 已是批发商；命中软删行 → 复活该行（保留 wholesaler_id 与历史）；无行 → insert。
        Wholesaler existing = wholesalerMapper.selectByUserIdIncludeDeleted(dto.getUserId());
        if (existing != null) {
            if (existing.getIsDelete() == null || existing.getIsDelete() == 0) {
                throw new GlobalException("该用户已是批发商", HttpStatus.WHOLESALE_USER_ALREADY_EXISTS);
            }
            wholesalerMapper.reviveById(existing.getWholesalerId(), dto.getGradeId(), 3, remark, now);
            invalidateBuyerCache(dto.getUserId());
            return existing.getWholesalerId();
        }

        Wholesaler w = new Wholesaler();
        w.setUserId(dto.getUserId());
        w.setGradeId(dto.getGradeId());
        w.setStatus(1);
        w.setSource(3); // 手动添加
        w.setShopId(0);
        w.setRemark(remark);
        w.setIsDelete(0);
        w.setCreateTime(now);
        w.setUpdateTime(now);
        wholesalerMapper.insert(w);

        invalidateBuyerCache(dto.getUserId());
        return w.getWholesalerId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WholesalerImportResultVO importBatch(WholesalerImportDTO dto) {
        // 不强制批发总开关已启用（同 add()，用户决策 2026-06-11）：允许提前导入批发商身份
        // gradeId 未传 → 回退到配置的默认批发等级（管理后台导入默认用默认等级）
        if (dto.getGradeId() == null || dto.getGradeId() <= 0) {
            Integer defaultGradeId = settingService.getDefaultGradeId();
            if (defaultGradeId == null || defaultGradeId == 0) {
                throw new GlobalException("未配置默认批发等级", HttpStatus.WHOLESALE_DEFAULT_GRADE_NOT_CONFIGURED);
            }
            dto.setGradeId(defaultGradeId);
        }
        gradeService.requireEnabled(dto.getGradeId());

        // 决定目标 userIds（优先级：全体 > 按会员等级 > 自选会员）
        boolean importAll = Boolean.TRUE.equals(dto.getImportAll());
        boolean byRank = !importAll && dto.getRankIds() != null && !dto.getRankIds().isEmpty();
        List<Integer> userIds;
        if (importAll) {
            userIds = listAllActiveUserIds();
        } else if (byRank) {
            // 按会员等级导入：取这些等级下启用中的会员
            userIds = listActiveUserIdsByRanks(dto.getRankIds());
        } else {
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                throw new GlobalException("userIds 不能为空", HttpStatus.BAD_REQUEST);
            }
            // 去重，保留顺序
            userIds = dto.getUserIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }

        boolean skip = "SKIP".equalsIgnoreCase(dto.getExistsStrategy());

        // 校验 userId 实存于 user 表（全体 / 按等级 来源即 user 表，无需再校验）
        Set<Integer> existUserIds = (importAll || byRank) ? new HashSet<>(userIds) : loadExistingUserIds(userIds);
        // 含软删的已存在批发商（userId -> 记录，含 is_delete）：区分 未删已存在 / 软删复活 / 全新
        Map<Integer, Wholesaler> existingMap = mapWholesalersByUserIdsIncludeDeleted(userIds);

        WholesalerImportResultVO result = new WholesalerImportResultVO();
        result.setTotalRequested(userIds.size());
        result.setSuccessCount(0);
        result.setSkippedCount(0);
        result.setFailedCount(0);
        result.setFailedDetails(new ArrayList<>());

        long now = Instant.now().getEpochSecond();
        List<Wholesaler> toInsert = new ArrayList<>();
        List<Integer> toOverwriteUserIds = new ArrayList<>();
        List<Integer> toReviveUserIds = new ArrayList<>();

        for (Integer uid : userIds) {
            if (!existUserIds.contains(uid)) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getFailedDetails().add(failItem(uid, "用户不存在"));
                continue;
            }
            Wholesaler ew = existingMap.get(uid);
            if (ew != null && (ew.getIsDelete() == null || ew.getIsDelete() == 0)) {
                // 未删的已存在批发商（C15）：按策略处理，均计入 skipped（不算新增）
                result.setSkippedCount(result.getSkippedCount() + 1);
                if (!skip) {
                    toOverwriteUserIds.add(uid); // OVERWRITE_GRADE：覆盖等级
                }
                continue;
            }
            if (ew != null) {
                // 软删行 → 复活（重置为有效批发商，保留 wholesaler_id），计入成功新增
                toReviveUserIds.add(uid);
                continue;
            }
            Wholesaler w = new Wholesaler();
            w.setUserId(uid);
            w.setGradeId(dto.getGradeId());
            w.setStatus(1);
            w.setSource(2); // 后台导入
            w.setShopId(0);
            w.setRemark("");
            w.setIsDelete(0);
            w.setCreateTime(now);
            w.setUpdateTime(now);
            toInsert.add(w);
        }

        // 分批插入
        for (int i = 0; i < toInsert.size(); i += BATCH_SIZE) {
            List<Wholesaler> chunk = toInsert.subList(i, Math.min(i + BATCH_SIZE, toInsert.size()));
            chunk.forEach(wholesalerMapper::insert);
        }

        // 分批复活软删行（is_delete=0 + status=1 + 新 grade，后台导入 source=2）
        for (int i = 0; i < toReviveUserIds.size(); i += BATCH_SIZE) {
            List<Integer> chunk = toReviveUserIds.subList(i, Math.min(i + BATCH_SIZE, toReviveUserIds.size()));
            wholesalerMapper.reviveByUserIds(chunk, dto.getGradeId(), 2, now);
        }
        // 复活也是恢复为有效批发商，计入成功新增
        result.setSuccessCount(toInsert.size() + toReviveUserIds.size());

        // 分批覆盖等级（OVERWRITE_GRADE）
        for (int i = 0; i < toOverwriteUserIds.size(); i += BATCH_SIZE) {
            List<Integer> chunk = toOverwriteUserIds.subList(i, Math.min(i + BATCH_SIZE, toOverwriteUserIds.size()));
            wholesalerMapper.updateGradeByUserIds(chunk, dto.getGradeId(), now);
        }

        // 缓存失效：所有被改动的 userId
        toInsert.forEach(w -> invalidateBuyerCache(w.getUserId()));
        toReviveUserIds.forEach(this::invalidateBuyerCache);
        toOverwriteUserIds.forEach(this::invalidateBuyerCache);
        return result;
    }

    /** importAll=true 时取全部启用会员（status=1） */
    private List<Integer> listAllActiveUserIds() {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.select(User::getUserId).eq(User::getStatus, 1);
        return userMapper.selectList(qw).stream()
                .map(User::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /** 按会员等级取启用中的会员 userIds（参考优惠券发放 RANK 模式） */
    private List<Integer> listActiveUserIdsByRanks(List<Integer> rankIds) {
        List<Integer> distinct = rankIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.select(User::getUserId).eq(User::getStatus, 1).in(User::getRankId, distinct);
        return userMapper.selectList(qw).stream()
                .map(User::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /** 分批查询 user 表，返回真实存在的 userIds */
    private Set<Integer> loadExistingUserIds(List<Integer> userIds) {
        Set<Integer> all = new HashSet<>();
        for (int i = 0; i < userIds.size(); i += BATCH_SIZE) {
            List<Integer> chunk = userIds.subList(i, Math.min(i + BATCH_SIZE, userIds.size()));
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.select(User::getUserId).in(User::getUserId, chunk);
            userMapper.selectList(qw).forEach(u -> all.add(u.getUserId()));
        }
        return all;
    }

    /**
     * 分批查询 wholesaler 表（<b>含软删行</b>），返回 userId -> 批发商记录的映射。
     *
     * <p>用于导入时区分「未删已存在 / 软删可复活 / 全新」三态；含软删是为避免 uk_user_id 唯一键冲突。
     */
    private Map<Integer, Wholesaler> mapWholesalersByUserIdsIncludeDeleted(List<Integer> userIds) {
        Map<Integer, Wholesaler> map = new HashMap<>();
        for (int i = 0; i < userIds.size(); i += BATCH_SIZE) {
            List<Integer> chunk = userIds.subList(i, Math.min(i + BATCH_SIZE, userIds.size()));
            wholesalerMapper.selectByUserIdsIncludeDeleted(chunk)
                    .forEach(w -> map.put(w.getUserId(), w));
        }
        return map;
    }

    // ---------------------------------------------------------------------
    // 启停 / 改等级
    // ---------------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setStatus(Integer wholesalerId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new GlobalException("status 必须为 0 或 1", HttpStatus.BAD_REQUEST);
        }
        Wholesaler w = wholesalerMapper.selectById(wholesalerId);
        if (w == null) {
            throw new GlobalException("批发商不存在", HttpStatus.NOT_FOUND);
        }
        w.setStatus(status);
        w.setUpdateTime(Instant.now().getEpochSecond());
        wholesalerMapper.updateById(w);
        invalidateBuyerCache(w.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeGrade(Integer wholesalerId, Integer gradeId) {
        gradeService.requireEnabled(gradeId);
        Wholesaler w = wholesalerMapper.selectById(wholesalerId);
        if (w == null) {
            throw new GlobalException("批发商不存在", HttpStatus.NOT_FOUND);
        }
        w.setGradeId(gradeId);
        w.setUpdateTime(Instant.now().getEpochSecond());
        wholesalerMapper.updateById(w);
        invalidateBuyerCache(w.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeByGrade(WholesalerRemoveByGradeDTO dto) {
        boolean removeAll = Boolean.TRUE.equals(dto.getRemoveAll());
        // removeAll=true 时忽略 wholesalerIds；否则必须显式指定
        List<Integer> wholesalerIds = removeAll ? null : dto.getWholesalerIds();
        if (!removeAll && (wholesalerIds == null || wholesalerIds.isEmpty())) {
            throw new GlobalException("请选择要移除的批发商或勾选全部", HttpStatus.BAD_REQUEST);
        }

        // 跨等级误删保护：固定按 gradeId + is_delete=0 过滤，仅命中真正属于该等级的记录
        List<Wholesaler> targets = wholesalerMapper.selectByGradeForRemove(dto.getGradeId(), wholesalerIds);
        if (targets.isEmpty()) {
            return 0;
        }

        // 软删：is_delete=1 + 更新 update_time（@TableLogic 使 updateById 改不动 is_delete，走 XML 自定义 UPDATE）
        long now = Instant.now().getEpochSecond();
        wholesalerMapper.softDeleteByGrade(dto.getGradeId(), wholesalerIds, now);

        // 逐个受影响 user_id 失效批发商身份缓存
        targets.stream().map(Wholesaler::getUserId).filter(Objects::nonNull).distinct()
                .forEach(this::invalidateBuyerCache);
        return targets.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remove(WholesalerRemoveDTO dto) {
        List<Integer> wholesalerIds = dto.getWholesalerIds() == null ? null
                : dto.getWholesalerIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (wholesalerIds == null || wholesalerIds.isEmpty()) {
            throw new GlobalException("请选择要移除的批发商", HttpStatus.BAD_REQUEST);
        }

        // 先查出未删除的目标（拿 user_id 失效缓存）；已删除/不存在的不会命中
        List<Wholesaler> targets = wholesalerMapper.selectByIdsForRemove(wholesalerIds);
        if (targets.isEmpty()) {
            return 0;
        }

        // 软删：is_delete=1 + 更新 update_time（@TableLogic 使 updateById 改不动 is_delete，走 XML 自定义 UPDATE）
        long now = Instant.now().getEpochSecond();
        wholesalerMapper.softDeleteByIds(wholesalerIds, now);

        // 逐个受影响 user_id 失效批发商身份缓存
        targets.stream().map(Wholesaler::getUserId).filter(Objects::nonNull).distinct()
                .forEach(this::invalidateBuyerCache);
        return targets.size();
    }

    // ---------------------------------------------------------------------
    // 注册自动转批发钩子（失败不影响主流程）
    // ---------------------------------------------------------------------

    @Override
    public void autoRegisterIfEnabled(Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return;
            }
            if (!settingService.isEnabled() || !settingService.isAutoRegister()) {
                return;
            }
            Integer defaultGradeId = settingService.getDefaultGradeId();
            if (defaultGradeId == null || defaultGradeId == 0) {
                return;
            }
            // 默认等级不存在/被禁用 → 跳过（不抛错）
            gradeService.requireEnabled(defaultGradeId);

            // 幂等：已存在则不重复
            LambdaQueryWrapper<Wholesaler> qw = new LambdaQueryWrapper<>();
            qw.eq(Wholesaler::getUserId, userId);
            if (wholesalerMapper.selectCount(qw) > 0) {
                return;
            }
            long now = Instant.now().getEpochSecond();
            Wholesaler w = new Wholesaler();
            w.setUserId(userId);
            w.setGradeId(defaultGradeId);
            w.setStatus(1);
            w.setSource(1); // 注册自动
            w.setShopId(0);
            w.setRemark("");
            w.setIsDelete(0);
            w.setCreateTime(now);
            w.setUpdateTime(now);
            wholesalerMapper.insert(w);
            invalidateBuyerCache(userId);
        } catch (Exception e) {
            // 批发模块异常绝不影响用户注册主流程
            log.warn("注册自动转批发失败 userId={}", userId, e);
        }
    }

    // ---------------------------------------------------------------------
    // 私有工具
    // ---------------------------------------------------------------------

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String sourceName(Integer source) {
        if (source == null) {
            return "";
        }
        switch (source) {
            case 1:
                return "注册自动";
            case 2:
                return "后台导入";
            case 3:
                return "手动添加";
            default:
                return "";
        }
    }

    private Long parseTimeToUnix(String s) {
        if (!hasText(s)) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.trim(), TIME_FMT).toEpochSecond(ZONE);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatTime(Long ts) {
        if (ts == null || ts == 0L) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(ts, 0, ZONE).format(TIME_FMT);
    }

    private WholesalerImportResultVO.FailedItem failItem(Integer userId, String reason) {
        WholesalerImportResultVO.FailedItem f = new WholesalerImportResultVO.FailedItem();
        f.setUserId(userId);
        f.setReason(reason);
        return f;
    }
}
