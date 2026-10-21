package com.tigshop.service.wholesale.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.wholesale.WholesalerGradeSaveDTO;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.vo.wholesale.WholesalerGradeVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.wholesale.WholesalerGradeMapper;
import com.tigshop.mapper.wholesale.WholesalerMapper;
import com.tigshop.service.wholesale.WholesaleSettingService;
import com.tigshop.service.wholesale.WholesalerGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 批发商等级 Service 实现。
 *
 * <p>错误码（api-contract §0.3，5300-5399 段）：消费 {@link com.tigshop.common.constant.HttpStatus} 中的 WHOLESALE_* 常量；
 * 等级名重复用项目既有 HTTP 409（CONFLICT）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Service
@RequiredArgsConstructor
public class WholesalerGradeServiceImpl implements WholesalerGradeService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SORT_ORDER = 50;

    private final WholesalerGradeMapper gradeMapper;
    private final WholesalerMapper wholesalerMapper;
    // 惰性获取：WholesaleSettingServiceImpl 已构造注入本类（gradeService），用 ObjectProvider 打破构造期循环依赖
    private final ObjectProvider<WholesaleSettingService> settingServiceProvider;

    @Override
    public List<WholesalerGradeVO> list(Integer statusFilter, boolean withUserCount) {
        // is_delete 由 @TableLogic 自动过滤
        LambdaQueryWrapper<WholesalerGrade> qw = new LambdaQueryWrapper<>();
        if (statusFilter != null) {
            qw.eq(WholesalerGrade::getStatus, statusFilter);
        }
        qw.orderByAsc(WholesalerGrade::getSortOrder).orderByAsc(WholesalerGrade::getGradeId);
        List<WholesalerGrade> grades = gradeMapper.selectList(qw);
        if (grades.isEmpty()) {
            return Collections.emptyList();
        }

        // 已绑定客户数：withUserCount=true 时一条聚合 SQL 解决
        Map<Integer, Long> userCountMap = withUserCount
                ? loadUserCountMap(grades.stream().map(WholesalerGrade::getGradeId).collect(Collectors.toList()))
                : Collections.emptyMap();

        // 默认等级标记：取批发设置 wholesaleDefaultGradeId（0/未配置 → 无默认），逐行回填 isDefault。
        WholesaleSettingService settingService = settingServiceProvider.getIfAvailable();
        Integer defaultGradeId = settingService != null ? settingService.getDefaultGradeId() : null;

        return grades.stream().map(g -> {
            WholesalerGradeVO vo = new WholesalerGradeVO();
            BeanUtils.copyProperties(g, vo);
            vo.setUserCount(userCountMap.getOrDefault(g.getGradeId(), 0L).intValue());
            vo.setIsDefault(defaultGradeId != null && defaultGradeId > 0 && defaultGradeId.equals(g.getGradeId()));
            vo.setAddTime(formatTime(g.getCreateTime()));
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Integer, Long> loadUserCountMap(List<Integer> gradeIds) {
        List<Map<String, Object>> rows = wholesalerMapper.countByGradeIds(gradeIds);
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("gradeId")).intValue(),
                r -> ((Number) r.get("userCount")).longValue()
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer save(WholesalerGradeSaveDTO dto) {
        if (dto.getGradeName() == null || dto.getGradeName().trim().isEmpty()) {
            throw new GlobalException("等级名不能为空", HttpStatus.BAD_REQUEST);
        }
        String gradeName = dto.getGradeName().trim();

        // 同名重复校验（编辑时排除自身）；is_delete 由 @TableLogic 自动过滤
        LambdaQueryWrapper<WholesalerGrade> qw = new LambdaQueryWrapper<>();
        qw.eq(WholesalerGrade::getGradeName, gradeName);
        if (dto.getGradeId() != null) {
            qw.ne(WholesalerGrade::getGradeId, dto.getGradeId());
        }
        if (gradeMapper.selectCount(qw) > 0) {
            throw new GlobalException("等级名已存在", HttpStatus.CONFLICT);
        }

        long now = Instant.now().getEpochSecond();
        WholesalerGrade entity;
        if (dto.getGradeId() == null) {
            entity = new WholesalerGrade();
            entity.setGradeName(gradeName);
            entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : DEFAULT_SORT_ORDER);
            entity.setRemark(dto.getRemark() != null ? dto.getRemark() : "");
            entity.setStatus(1);
            entity.setIsDelete(0);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            gradeMapper.insert(entity);
        } else {
            entity = gradeMapper.selectById(dto.getGradeId());
            if (entity == null) {
                throw new GlobalException("批发等级不存在", HttpStatus.WHOLESALE_GRADE_NOT_FOUND);
            }
            entity.setGradeName(gradeName);
            if (dto.getSortOrder() != null) {
                entity.setSortOrder(dto.getSortOrder());
            }
            if (dto.getRemark() != null) {
                entity.setRemark(dto.getRemark());
            }
            entity.setUpdateTime(now);
            gradeMapper.updateById(entity);
        }
        return entity.getGradeId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setStatus(Integer gradeId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new GlobalException("status 必须为 0 或 1", HttpStatus.BAD_REQUEST);
        }
        WholesalerGrade g = gradeMapper.selectById(gradeId);
        if (g == null) {
            throw new GlobalException("批发等级不存在", HttpStatus.WHOLESALE_GRADE_NOT_FOUND);
        }
        g.setStatus(status);
        g.setUpdateTime(Instant.now().getEpochSecond());
        gradeMapper.updateById(g);
        // 注（api-contract §1.A8 + step 文档）：禁用默认等级允许；isWholesaleBuyer 不受影响，
        // 仅自动转批发钩子在默认等级被禁用时 try-catch 跳过。
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer gradeId) {
        WholesalerGrade g = gradeMapper.selectById(gradeId);
        if (g == null) {
            throw new GlobalException("批发等级不存在", HttpStatus.WHOLESALE_GRADE_NOT_FOUND);
        }
        // 默认等级保护（CR-20260610-2210 用户决策 block_default）：删默认等级会让"注册自动转批发"失效，拦截
        WholesaleSettingService settingService = settingServiceProvider.getIfAvailable();
        Integer defaultGradeId = settingService != null ? settingService.getDefaultGradeId() : null;
        if (defaultGradeId != null && defaultGradeId > 0 && defaultGradeId.equals(gradeId)) {
            throw new GlobalException("默认批发等级不可删除，请先在批发设置中更换默认等级",
                    HttpStatus.WHOLESALE_GRADE_CANNOT_DELETE);
        }
        // 绑定批发商拦截（含禁用：countByGradeIds 口径为 wholesaler.is_delete=0，与列表 userCount 一致）
        List<Map<String, Object>> rows = wholesalerMapper.countByGradeIds(Collections.singletonList(gradeId));
        long userCount = rows.stream()
                .findFirst()
                .map(r -> ((Number) r.get("userCount")).longValue())
                .orElse(0L);
        if (userCount > 0) {
            throw new GlobalException("该等级下已有批发商用户。在删除该等级之前，必须先删除该等级下的所有批发商用户。",
                    HttpStatus.WHOLESALE_GRADE_HAS_BINDING);
        }
        // 先更新 update_time（updateById 会忽略 @TableLogic 的 is_delete 字段，仅落 update_time），
        // 再 deleteById 触发 @TableLogic 逻辑删除（UPDATE is_delete=1 WHERE grade_id=? AND is_delete=0）。
        g.setUpdateTime(Instant.now().getEpochSecond());
        gradeMapper.updateById(g);
        gradeMapper.deleteById(gradeId);
    }

    @Override
    public WholesalerGrade getById(Integer gradeId) {
        // selectById 已由 @TableLogic 过滤已删除记录
        return gradeMapper.selectById(gradeId);
    }

    @Override
    public void requireEnabled(Integer gradeId) {
        WholesalerGrade g = getById(gradeId);
        if (g == null) {
            throw new GlobalException("批发等级不存在", HttpStatus.WHOLESALE_GRADE_NOT_FOUND);
        }
        if (g.getStatus() == null || g.getStatus() != 1) {
            throw new GlobalException("批发等级已禁用", HttpStatus.WHOLESALE_GRADE_DISABLED);
        }
    }

    @Override
    public Map<Integer, WholesalerGrade> mapByIds(List<Integer> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WholesalerGrade> list = gradeMapper.selectBatchIds(gradeIds);
        return list.stream().collect(Collectors.toMap(WholesalerGrade::getGradeId, g -> g));
    }

    private String formatTime(Long unixSeconds) {
        if (unixSeconds == null || unixSeconds == 0L) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(unixSeconds, 0, ZoneOffset.ofHours(8)).format(TIME_FMT);
    }
}
