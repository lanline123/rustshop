package com.tigshop.service.deposit.impl;

import org.springframework.security.access.prepost.PreAuthorize;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositPaymentAuditListDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeApplyDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeAuditDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeListDTO;
import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.enums.deposit.DepositPayeeAccountType;
import com.tigshop.bean.enums.deposit.DepositRechargeStatus;
import com.tigshop.bean.enums.deposit.DepositRefundStatus;
import com.tigshop.bean.enums.deposit.DepositRefundType;
import com.tigshop.bean.model.deposit.ShopDepositRecharge;
import com.tigshop.bean.model.deposit.ShopDepositRefund;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.vo.deposit.DepositPaymentAuditVO;
import com.tigshop.bean.vo.deposit.DepositPayeeAccountVO;
import com.tigshop.bean.vo.deposit.DepositRechargeVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.deposit.ShopDepositRechargeMapper;
import com.tigshop.mapper.deposit.ShopDepositRefundMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.deposit.DepositAccountService;
import com.tigshop.service.deposit.DepositLifecycleService;
import com.tigshop.service.deposit.DepositPayeeAccountService;
import com.tigshop.service.deposit.DepositRechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 店铺保证金充值服务实现。
 *
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class DepositRechargeServiceImpl
        extends BaseServiceImpl<ShopDepositRechargeMapper, ShopDepositRecharge>
        implements DepositRechargeService {

    private final DepositAccountService depositAccountService;
    private final DepositLifecycleService depositLifecycleService;
    private final DepositPayeeAccountService depositPayeeAccountService;
    private final ShopMapper shopMapper;
    private final VendorMapper vendorMapper;
    private final MerchantMapper merchantMapper;
    private final ShopDepositRefundMapper shopDepositRefundMapper;

    @Override
    public Integer apply(Integer shopId, DepositRechargeApplyDTO applyDTO) {
        return applyInternal(DepositOwnerType.SHOP.getCode(), shopId, applyDTO);
    }

    @Override
    public Integer vendorApply(Integer vendorId, DepositRechargeApplyDTO applyDTO) {
        return applyInternal(DepositOwnerType.SUPPLIER.getCode(), vendorId, applyDTO);
    }

    /**
     * 充值申请统一内部入口：店铺/供应商两端仅 owner 维度不同，逻辑一致。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为shopId，供应商为vendorId）
     */
    private Integer applyInternal(Integer ownerType, Integer ownerId, DepositRechargeApplyDTO applyDTO) {
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者不存在");
        }
        if (applyDTO == null || applyDTO.getAmount() == null
                || applyDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        long now = nowSeconds();
        ShopDepositRecharge recharge = new ShopDepositRecharge();
        recharge.setOwnerType(ownerType);
        recharge.setOwnerId(ownerId);
        recharge.setAmount(applyDTO.getAmount());
        // 校验并快照收款账户（财务核对用，必填）
        if (applyDTO.getPayeeAccountId() == null || applyDTO.getPayeeAccountId() <= 0) {
            throw new GlobalException("请选择收款账户");
        }
        DepositPayeeAccountVO payee = depositPayeeAccountService.detail(applyDTO.getPayeeAccountId());
        if (payee == null) {
            throw new GlobalException("收款账户不存在");
        }
        if (payee.getStatus() != null && !Integer.valueOf(1).equals(payee.getStatus())) {
            throw new GlobalException("收款账户已停用");
        }
        recharge.setPayeeAccountId(payee.getId());
        recharge.setPayeeAccountType(payee.getAccountType());
        recharge.setPayeeSnapshot(JSON.toJSONString(payee));
        recharge.setPayVoucher(applyDTO.getPayVoucher());
        recharge.setRemark(applyDTO.getRemark());
        recharge.setStatus(DepositRechargeStatus.PENDING_REVIEW.getCode());
        recharge.setAuditAdminId(0);
        recharge.setAuditTime(0L);
        recharge.setAddTime(now);
        recharge.setUpdateTime(now);
        this.save(recharge);
        return recharge.getShopDepositRechargeId();
    }

    @Override
    public Page<DepositRechargeVO> list(DepositRechargeListDTO listDTO) {
        // 组织名称/主体类型筛选：店铺与供应商 id 空间独立，按 owner 类型分段解析命中 id 集合，
        // 避免跨类型 id 串号；命中集合为空说明该类型被排除。
        List<Integer> ownerTypes = resolveOwnerTypes(listDTO.getOwnerType());
        List<OwnerSegment> segments = new ArrayList<>();
        for (Integer ownerType : ownerTypes) {
            Set<Integer> idSet = resolveOwnerIdConstraint(ownerType, listDTO.getOwnerName(),
                    listDTO.getSubjectType());
            if (idSet != null && idSet.isEmpty()) {
                continue;
            }
            segments.add(new OwnerSegment(ownerType, idSet));
        }
        if (segments.isEmpty()) {
            return new Page<>(listDTO.getPage(), listDTO.getSize(), 0);
        }

        Page<ShopDepositRecharge> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<ShopDepositRecharge> queryWrapper = new LambdaQueryWrapper<>();
        applyOwnerSegments(queryWrapper, segments);
        // 持有者id精确筛选：优先 ownerId，兼容旧入参 shopId（与分段条件 AND 叠加）
        Integer ownerId = listDTO.getOwnerId() != null ? listDTO.getOwnerId() : listDTO.getShopId();
        queryWrapper.eq(ownerId != null && ownerId > 0,
                ShopDepositRecharge::getOwnerId, ownerId);
        queryWrapper.eq(listDTO.getPayeeAccountType() != null,
                ShopDepositRecharge::getPayeeAccountType, listDTO.getPayeeAccountType());
        queryWrapper.eq(listDTO.getStatus() != null,
                ShopDepositRecharge::getStatus, listDTO.getStatus());
        queryWrapper.ge(listDTO.getStartTime() != null,
                ShopDepositRecharge::getAddTime, listDTO.getStartTime());
        queryWrapper.le(listDTO.getEndTime() != null,
                ShopDepositRecharge::getAddTime, listDTO.getEndTime());
        queryWrapper.orderByDesc(ShopDepositRecharge::getShopDepositRechargeId);
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        Page<ShopDepositRecharge> rechargePage = this.page(page, queryWrapper);
        List<DepositRechargeVO> voList = rechargePage.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageUtil.convertPage(rechargePage, voList);
    }

    // ====================== owner 维度解析辅助（组织名称/主体类型筛选） ======================

    /** 查询的目标 owner 类型集合：null=全部(店铺+供应商)，否则单一类型。 */
    private List<Integer> resolveOwnerTypes(Integer ownerType) {
        if (ownerType == null) {
            return List.of(DepositOwnerType.SHOP.getCode(), DepositOwnerType.SUPPLIER.getCode());
        }
        return List.of(ownerType);
    }

    private boolean isSupplier(Integer ownerType) {
        return DepositOwnerType.SUPPLIER.getCode().equals(ownerType);
    }

    /**
     * 解析某一 owner 类型下，名称/主体类型筛选命中的 ownerId 集合。
     *
     * @return null=该类型无任何筛选约束（全量）；非空集=约束命中的 ownerId；空集=该类型被排除。
     */
    private Set<Integer> resolveOwnerIdConstraint(Integer ownerType, String name, Integer subjectType) {
        List<Set<Integer>> constraints = new ArrayList<>();
        boolean hasName = StrUtil.isNotBlank(name);
        boolean hasSubjectType = subjectType != null;
        if (isSupplier(ownerType)) {
            if (hasName) {
                constraints.add(vendorIdsByName(name));
            }
            if (hasSubjectType) {
                constraints.add(vendorIdsBySubjectType(subjectType));
            }
        } else {
            if (hasName) {
                constraints.add(shopIdsByName(name));
            }
            if (hasSubjectType) {
                constraints.add(shopIdsByMerchantType(subjectType));
            }
        }
        if (constraints.isEmpty()) {
            return null;
        }
        return intersect(constraints);
    }

    private void applyOwnerSegments(LambdaQueryWrapper<ShopDepositRecharge> wrapper, List<OwnerSegment> segments) {
        boolean allUnbounded = segments.stream().allMatch(s -> s.idSet() == null);
        if (allUnbounded) {
            List<Integer> types = segments.stream().map(OwnerSegment::ownerType).toList();
            if (types.size() == 1) {
                wrapper.eq(ShopDepositRecharge::getOwnerType, types.get(0));
            } else {
                wrapper.in(ShopDepositRecharge::getOwnerType, types);
            }
            return;
        }
        if (segments.size() == 1) {
            OwnerSegment seg = segments.get(0);
            wrapper.eq(ShopDepositRecharge::getOwnerType, seg.ownerType());
            if (seg.idSet() != null) {
                wrapper.in(ShopDepositRecharge::getOwnerId, seg.idSet());
            }
            return;
        }
        wrapper.and(outer -> {
            boolean first = true;
            for (OwnerSegment seg : segments) {
                if (first) {
                    outer.and(o -> {
                        o.eq(ShopDepositRecharge::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDepositRecharge::getOwnerId, seg.idSet());
                        }
                    });
                    first = false;
                } else {
                    outer.or(o -> {
                        o.eq(ShopDepositRecharge::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDepositRecharge::getOwnerId, seg.idSet());
                        }
                    });
                }
            }
        });
    }

    private Set<Integer> intersect(List<Set<Integer>> sets) {
        Set<Integer> result = null;
        for (Set<Integer> set : sets) {
            if (result == null) {
                result = new HashSet<>(set);
            } else {
                result.retainAll(set);
            }
            if (result.isEmpty()) {
                return Collections.emptySet();
            }
        }
        return result == null ? Collections.emptySet() : result;
    }

    private Set<Integer> shopIdsByName(String name) {
        List<Shop> shops = shopMapper.selectList(Wrappers.<Shop>lambdaQuery()
                .select(Shop::getShopId)
                .like(Shop::getShopTitle, name));
        return shops.stream().map(Shop::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<Integer> vendorIdsByName(String name) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .like(Vendor::getVendorName, name));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 按商户性质（merchant.type 1个人 2企业）解析店铺id集合。 */
    private Set<Integer> shopIdsByMerchantType(Integer subjectType) {
        List<Merchant> merchants = merchantMapper.selectList(Wrappers.<Merchant>lambdaQuery()
                .select(Merchant::getMerchantId)
                .eq(Merchant::getType, subjectType));
        Set<Integer> merchantIds = merchants.stream()
                .map(Merchant::getMerchantId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Shop> shops = shopMapper.selectList(Wrappers.<Shop>lambdaQuery()
                .select(Shop::getShopId)
                .in(Shop::getMerchantId, merchantIds));
        return shops.stream().map(Shop::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 按供应商性质（vendor.type 1个人 2企业）解析供应商id集合。 */
    private Set<Integer> vendorIdsBySubjectType(Integer subjectType) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .eq(Vendor::getType, subjectType));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private record OwnerSegment(Integer ownerType, Set<Integer> idSet) {
    }

    @Override
    public Page<DepositPaymentAuditVO> paymentAuditList(Integer ownerType, Integer ownerId,
                                                        DepositPaymentAuditListDTO listDTO) {
        int pageNo = listDTO.getPage();
        int pageSize = listDTO.getSize();
        Page<DepositPaymentAuditVO> resultPage = new Page<>(pageNo, pageSize);
        // owner 未登录/非法：返回空分页，避免越权全量
        if (ownerId == null || ownerId <= 0) {
            resultPage.setTotal(0L);
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        Integer auditType = listDTO.getAuditType();
        List<DepositPaymentAuditVO> merged = new ArrayList<>();
        // 充值行（auditType 不传或=1 时纳入）
        if (auditType == null || Integer.valueOf(1).equals(auditType)) {
            merged.addAll(loadRechargeRows(ownerType, ownerId, listDTO));
        }
        // 退还行（auditType 不传或=2 时纳入）
        if (auditType == null || Integer.valueOf(2).equals(auditType)) {
            merged.addAll(loadRefundRows(ownerType, ownerId, listDTO));
        }

        // 按提交时间倒序混合
        merged.sort(Comparator.comparing(
                DepositPaymentAuditVO::getAddTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        long total = merged.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, merged.size());
        int toIndex = Math.min(fromIndex + pageSize, merged.size());
        resultPage.setTotal(total);
        resultPage.setRecords(new ArrayList<>(merged.subList(fromIndex, toIndex)));
        return resultPage;
    }

    /**
     * 查询当前 owner 的充值单并映射为款项审核行（owner 锁定 + 状态/时间筛选）。
     */
    private List<DepositPaymentAuditVO> loadRechargeRows(Integer ownerType, Integer ownerId,
                                                         DepositPaymentAuditListDTO listDTO) {
        LambdaQueryWrapper<ShopDepositRecharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDepositRecharge::getOwnerType, ownerType);
        wrapper.eq(ShopDepositRecharge::getOwnerId, ownerId);
        wrapper.eq(listDTO.getStatus() != null, ShopDepositRecharge::getStatus, listDTO.getStatus());
        wrapper.ge(listDTO.getStartTime() != null, ShopDepositRecharge::getAddTime, listDTO.getStartTime());
        wrapper.le(listDTO.getEndTime() != null, ShopDepositRecharge::getAddTime, listDTO.getEndTime());
        List<ShopDepositRecharge> list = this.list(wrapper);
        List<DepositPaymentAuditVO> rows = new ArrayList<>(list.size());
        for (ShopDepositRecharge r : list) {
            rows.add(toAuditVO(r));
        }
        return rows;
    }

    /**
     * 充值单 → 款项审核行VO。
     */
    private DepositPaymentAuditVO toAuditVO(ShopDepositRecharge r) {
        DepositPaymentAuditVO vo = new DepositPaymentAuditVO();
        vo.setAuditType(1);
        vo.setAuditTypeText("保证金充值");
        vo.setBizId(r.getShopDepositRechargeId());
        vo.setAmount(r.getAmount());
        vo.setStatus(r.getStatus());
        vo.setStatusText(DepositRechargeStatus.getDescByCode(r.getStatus()));
        vo.setRemark(r.getRemark());
        vo.setPayVoucher(r.getPayVoucher());
        vo.setAuditRemark(r.getAuditRemark());
        vo.setAuditTime(r.getAuditTime());
        vo.setAuditTimeText(TigUtils.handelTime(r.getAuditTime()));
        vo.setAddTime(r.getAddTime());
        vo.setAddTimeText(TigUtils.handelTime(r.getAddTime()));
        return vo;
    }

    /**
     * 查询当前 owner 的退还单并映射为款项审核行（owner 锁定 + 状态/时间筛选）。
     */
    private List<DepositPaymentAuditVO> loadRefundRows(Integer ownerType, Integer ownerId,
                                                       DepositPaymentAuditListDTO listDTO) {
        LambdaQueryWrapper<ShopDepositRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDepositRefund::getOwnerType, ownerType);
        wrapper.eq(ShopDepositRefund::getOwnerId, ownerId);
        wrapper.eq(listDTO.getStatus() != null, ShopDepositRefund::getStatus, listDTO.getStatus());
        wrapper.ge(listDTO.getStartTime() != null, ShopDepositRefund::getAddTime, listDTO.getStartTime());
        wrapper.le(listDTO.getEndTime() != null, ShopDepositRefund::getAddTime, listDTO.getEndTime());
        List<ShopDepositRefund> list = shopDepositRefundMapper.selectList(wrapper);
        List<DepositPaymentAuditVO> rows = new ArrayList<>(list.size());
        for (ShopDepositRefund r : list) {
            rows.add(toAuditVO(r));
        }
        return rows;
    }

    /**
     * 退还单 → 款项审核行VO。
     */
    private DepositPaymentAuditVO toAuditVO(ShopDepositRefund r) {
        DepositPaymentAuditVO vo = new DepositPaymentAuditVO();
        vo.setAuditType(2);
        vo.setAuditTypeText("保证金退还");
        vo.setBizId(r.getShopDepositRefundId());
        vo.setAmount(r.getAmount());
        vo.setStatus(r.getStatus());
        vo.setStatusText(DepositRefundStatus.getDescByCode(r.getStatus()));
        vo.setRefundType(r.getType());
        vo.setRefundTypeText(DepositRefundType.getDescByCode(r.getType()));
        vo.setRemark(r.getReason());
        vo.setPayVoucher(r.getPayVoucher());
        vo.setAuditRemark(r.getAuditRemark());
        vo.setAuditTime(r.getAuditTime());
        vo.setAuditTimeText(TigUtils.handelTime(r.getAuditTime()));
        vo.setAddTime(r.getAddTime());
        vo.setAddTimeText(TigUtils.handelTime(r.getAddTime()));
        return vo;
    }

    @Override
    public DepositPaymentAuditVO paymentAuditDetail(Integer ownerType, Integer ownerId,
                                                    Integer auditType, Integer bizId) {
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者不存在");
        }
        if (bizId == null || bizId <= 0) {
            throw new GlobalException("单据不存在");
        }
        if (Integer.valueOf(1).equals(auditType)) {
            ShopDepositRecharge recharge = this.getById(bizId);
            if (recharge == null
                    || !ownerType.equals(recharge.getOwnerType())
                    || !ownerId.equals(recharge.getOwnerId())) {
                throw new GlobalException("充值单不存在");
            }
            return toAuditVO(recharge);
        }
        if (Integer.valueOf(2).equals(auditType)) {
            ShopDepositRefund refund = shopDepositRefundMapper.selectById(bizId);
            if (refund == null
                    || !ownerType.equals(refund.getOwnerType())
                    || !ownerId.equals(refund.getOwnerId())) {
                throw new GlobalException("退还单不存在");
            }
            return toAuditVO(refund);
        }
        throw new GlobalException("审核类型非法");
    }

    @Override
    public DepositRechargeVO detail(Integer id) {
        if (id == null) {
            return null;
        }
        ShopDepositRecharge recharge = this.getById(id);
        if (recharge == null) {
            throw new GlobalException("充值单不存在");
        }
        return toVO(recharge);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositRechargeAudit')")
    public void audit(Integer adminId, DepositRechargeAuditDTO auditDTO) {
        if (auditDTO == null || auditDTO.getId() == null) {
            throw new GlobalException("充值单不存在");
        }
        ShopDepositRecharge recharge = this.getById(auditDTO.getId());
        if (recharge == null) {
            throw new GlobalException("充值单不存在");
        }
        if (!DepositRechargeStatus.PENDING_REVIEW.getCode().equals(recharge.getStatus())) {
            throw new GlobalException("充值单状态非法", HttpStatus.DEPOSIT_RECHARGE_STATUS_INVALID);
        }
        boolean pass = Boolean.TRUE.equals(auditDTO.getPass());
        long now = nowSeconds();
        if (pass) {
            // 审核通过：入账（唯一改 paid 入口）+ 单据置已入账。按单据 owner 维度入账。
            depositAccountService.changeDeposit(recharge.getOwnerType(), recharge.getOwnerId(),
                    recharge.getAmount(), DepositLogType.RECHARGE, recharge.getShopDepositRechargeId(),
                    0, adminId, auditDTO.getAuditRemark());
            recharge.setStatus(DepositRechargeStatus.CREDITED.getCode());
            // 缴足即时对账：补足(pending<=0)则清双缓冲/解除处罚/恢复下架商品，
            // 避免停留旧处罚态需等定时任务，也确保后续扣除能重新启动补缴缓冲。restore 内部行锁+二次确认，幂等安全。
            if (depositAccountService.pending(recharge.getOwnerType(), recharge.getOwnerId())
                    .compareTo(BigDecimal.ZERO) <= 0) {
                depositLifecycleService.restore(recharge.getOwnerType(), recharge.getOwnerId());
            }
        } else {
            recharge.setStatus(DepositRechargeStatus.REJECTED.getCode());
        }
        recharge.setAuditAdminId(adminId == null ? 0 : adminId);
        recharge.setAuditTime(now);
        recharge.setAuditRemark(auditDTO.getAuditRemark());
        recharge.setUpdateTime(now);
        this.updateById(recharge);
    }

    private DepositRechargeVO toVO(ShopDepositRecharge recharge) {
        DepositRechargeVO vo = new DepositRechargeVO();
        BeanUtils.copyProperties(recharge, vo);
        vo.setOwnerType(recharge.getOwnerType());
        vo.setOwnerId(recharge.getOwnerId());
        vo.setOwnerTypeText(DepositOwnerType.getDescByCode(recharge.getOwnerType()));
        // 兼容字段：shopId 同 ownerId 值；shopName 仅店铺维度回填
        vo.setShopId(recharge.getOwnerId());
        vo.setStatusText(DepositRechargeStatus.getDescByCode(recharge.getStatus()));
        vo.setAddTimeText(TigUtils.handelTime(recharge.getAddTime()));
        vo.setAuditTimeText(TigUtils.handelTime(recharge.getAuditTime()));
        fillOwnerName(vo, recharge.getOwnerType(), recharge.getOwnerId());
        fillPayeeAccount(vo, recharge.getPayeeSnapshot());
        return vo;
    }

    /**
     * 从快照回填收款账户展示字段（财务核对用，历史值不随账户后续改动变化）。
     */
    private void fillPayeeAccount(DepositRechargeVO vo, String payeeSnapshot) {
        if (StrUtil.isBlank(payeeSnapshot)) {
            return;
        }
        DepositPayeeAccountVO payee = JSON.parseObject(payeeSnapshot, DepositPayeeAccountVO.class);
        if (payee == null) {
            return;
        }
        vo.setPayeeAccountType(payee.getAccountType());
        vo.setPayeeAccountTypeText(DepositPayeeAccountType.getDescByCode(payee.getAccountType()));
        vo.setPayeeAccountName(payee.getAccountName());
        vo.setPayeeAccountNo(payee.getAccountNo());
        vo.setPayeeBankName(payee.getBankName());
        vo.setPayeeBankBranch(payee.getBankBranch());
    }

    /**
     * 按 owner_type 回填持有者名称：店铺取 shopTitle，供应商取 vendorName。
     * 同时为店铺维度回填兼容字段 shopName。
     */
    private void fillOwnerName(DepositRechargeVO vo, Integer ownerType, Integer ownerId) {
        if (ownerId == null || ownerId <= 0) {
            return;
        }
        if (DepositOwnerType.SUPPLIER.getCode().equals(ownerType)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            if (vendor != null) {
                vo.setOwnerName(vendor.getVendorName());
                vo.setSubjectType(vendor.getType());
                vo.setSubjectTypeText(subjectTypeText(vendor.getType()));
            }
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            if (shop != null) {
                vo.setOwnerName(shop.getShopTitle());
                vo.setShopName(shop.getShopTitle());
                if (shop.getMerchantId() != null && shop.getMerchantId() > 0) {
                    Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
                    if (merchant != null) {
                        vo.setSubjectType(merchant.getType());
                        vo.setSubjectTypeText(subjectTypeText(merchant.getType()));
                    }
                }
            }
        }
    }

    /** 主体类型文案：1个人 2企业。 */
    private String subjectTypeText(Integer subjectType) {
        if (subjectType == null) {
            return null;
        }
        return switch (subjectType) {
            case 1 -> "个人";
            case 2 -> "企业";
            default -> null;
        };
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
