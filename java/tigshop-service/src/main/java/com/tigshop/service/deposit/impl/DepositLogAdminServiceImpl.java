package com.tigshop.service.deposit.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.deposit.DepositLogListDTO;
import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.enums.deposit.DepositPayeeAccountType;
import com.tigshop.bean.enums.merchant.MerchantStatus;
import com.tigshop.bean.model.deposit.ShopDepositLog;
import com.tigshop.bean.model.deposit.ShopDepositRecharge;
import com.tigshop.bean.model.deposit.ShopDepositRefund;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.vo.deposit.DepositLogVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.deposit.ShopDepositLogMapper;
import com.tigshop.mapper.deposit.ShopDepositRechargeMapper;
import com.tigshop.mapper.deposit.ShopDepositRefundMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.deposit.DepositLogAdminService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 平台端保证金全局交易明细服务实现（统一 owner 维度：店铺 + 供应商）。
 *
 * <p>名称筛选按 owner_type 分派来源：店铺行匹配店铺名/商户名，供应商行匹配供应商名。
 * 列表通过分段解析 ownerId 约束集合后组合 OR 条件，保持单表（shop_deposit_log）SQL 分页与排序。</p>
 *
 * @author Tigshop团队
 */
@Service
public class DepositLogAdminServiceImpl extends BaseServiceImpl<ShopDepositLogMapper, ShopDepositLog>
        implements DepositLogAdminService {

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private VendorMapper vendorMapper;

    @Resource
    private ShopDepositRechargeMapper shopDepositRechargeMapper;

    @Resource
    private ShopDepositRefundMapper shopDepositRefundMapper;

    @Override
    public Page<DepositLogVO> list(DepositLogListDTO listDTO) {
        long[] range = resolveTimeRange(listDTO);
        Long startTime = range[0] > 0 ? range[0] : null;
        Long endTime = range[1] > 0 ? range[1] : null;

        List<Integer> ownerTypes = resolveOwnerTypes(listDTO.getOwnerType());
        // 统一名称搜索：优先 ownerName，回退兼容字段 shopTitle
        String name = StrUtil.isNotBlank(listDTO.getOwnerName()) ? listDTO.getOwnerName() : listDTO.getShopTitle();

        List<OwnerSegment> segments = new ArrayList<>();
        for (Integer ownerType : ownerTypes) {
            Set<Integer> idSet = resolveOwnerIdConstraint(ownerType, name, listDTO.getMerchantName(),
                    listDTO.getSubjectType());
            if (idSet != null && idSet.isEmpty()) {
                continue;
            }
            segments.add(new OwnerSegment(ownerType, idSet));
        }
        if (segments.isEmpty()) {
            return new Page<>(listDTO.getPage(), listDTO.getSize(), 0);
        }

        MPJLambdaWrapper<ShopDepositLog> wrapper = new MPJLambdaWrapper<>();
        applyOwnerSegments(wrapper, segments);
        // 类型筛选：提现出账(2) 与 退还出账(3) 暂合并为一类，传 2 或 3 均查 2+3；其余精确匹配。
        Integer typeFilter = listDTO.getType();
        if (typeFilter != null) {
            if (DepositLogType.isWithdrawCategory(typeFilter)) {
                wrapper.in(ShopDepositLog::getType, DepositLogType.WITHDRAW.getCode(), DepositLogType.REFUND.getCode());
            } else {
                wrapper.eq(ShopDepositLog::getType, typeFilter);
            }
        }
        // 支付方式（收款账户类型）筛选：仅充值类流水有收款方式，relatedId 指向充值单。
        // 约束为 type=充值 AND relatedId IN(命中该收款方式的充值单)；命中集合为空时返回空页。
        if (listDTO.getPayeeAccountType() != null) {
            Set<Integer> rechargeIds = rechargeIdsByPayeeType(listDTO.getPayeeAccountType());
            if (rechargeIds.isEmpty()) {
                return new Page<>(listDTO.getPage(), listDTO.getSize(), 0);
            }
            wrapper.eq(ShopDepositLog::getType, DepositLogType.RECHARGE.getCode());
            wrapper.in(ShopDepositLog::getRelatedId, rechargeIds);
        }
        wrapper.ge(startTime != null, ShopDepositLog::getAddTime, startTime);
        wrapper.le(endTime != null, ShopDepositLog::getAddTime, endTime);
        // 交易单号：按关联订单id / 关联单id 精确匹配
        String tradeNoStr = listDTO.getTradeNo() == null ? "" : listDTO.getTradeNo().trim();
        if (!tradeNoStr.isEmpty() && tradeNoStr.matches("\\d+")) {
            Integer tradeNo = Integer.valueOf(tradeNoStr);
            wrapper.and(w -> w.eq(ShopDepositLog::getOrderId, tradeNo)
                    .or().eq(ShopDepositLog::getRelatedId, tradeNo));
        }

        Page<ShopDepositLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        Page<ShopDepositLog> logPage = this.page(page, wrapper);
        List<DepositLogVO> voList = logPage.getRecords().stream()
                .map(this::toLogVO)
                .toList();
        return PageUtil.convertPage(logPage, voList);
    }

    @Override
    public DepositLogVO detail(Integer id) {
        if (id == null || id <= 0) {
            throw new GlobalException("交易明细id非法");
        }
        ShopDepositLog log = this.getById(id);
        if (log == null) {
            throw new GlobalException("交易明细不存在");
        }
        return toLogVO(log);
    }

    // ====================== owner 维度解析辅助 ======================

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
     * 解析某一 owner 类型下，名称/商户筛选命中的 ownerId 集合。
     *
     * @return null=该类型无任何筛选约束（全量）；非空集=约束命中的 ownerId；空集=该类型被筛选排除。
     */
    private Set<Integer> resolveOwnerIdConstraint(Integer ownerType, String name, String merchantName,
                                                  Integer subjectType) {
        List<Set<Integer>> constraints = new ArrayList<>();
        boolean hasName = StrUtil.isNotBlank(name);
        boolean hasMerchant = StrUtil.isNotBlank(merchantName);
        boolean hasSubjectType = subjectType != null;

        if (isSupplier(ownerType)) {
            // 供应商无商户概念：传了商户名筛选则直接排除供应商行
            if (hasMerchant) {
                return Collections.emptySet();
            }
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
            if (hasMerchant) {
                constraints.add(shopIdsByMerchantName(merchantName));
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

    private void applyOwnerSegments(MPJLambdaWrapper<ShopDepositLog> wrapper, List<OwnerSegment> segments) {
        boolean allUnbounded = segments.stream().allMatch(s -> s.idSet() == null);
        if (allUnbounded) {
            List<Integer> types = segments.stream().map(OwnerSegment::ownerType).toList();
            if (types.size() == 1) {
                wrapper.eq(ShopDepositLog::getOwnerType, types.get(0));
            } else {
                wrapper.in(ShopDepositLog::getOwnerType, types);
            }
            return;
        }
        if (segments.size() == 1) {
            OwnerSegment seg = segments.get(0);
            wrapper.eq(ShopDepositLog::getOwnerType, seg.ownerType());
            if (seg.idSet() != null) {
                wrapper.in(ShopDepositLog::getOwnerId, seg.idSet());
            }
            return;
        }
        wrapper.and(outer -> {
            boolean first = true;
            for (OwnerSegment seg : segments) {
                if (first) {
                    outer.and(o -> {
                        o.eq(ShopDepositLog::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDepositLog::getOwnerId, seg.idSet());
                        }
                    });
                    first = false;
                } else {
                    outer.or(o -> {
                        o.eq(ShopDepositLog::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDepositLog::getOwnerId, seg.idSet());
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

    private Set<Integer> shopIdsByMerchantName(String merchantName) {
        List<Merchant> merchants = merchantMapper.selectList(Wrappers.<Merchant>lambdaQuery()
                .select(Merchant::getMerchantId)
                .and(w -> w.like(Merchant::getCompanyName, merchantName)
                        .or().like(Merchant::getCorporateName, merchantName)));
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

    /** 按收款账户类型解析充值单id集合（用于支付方式筛选）。 */
    private Set<Integer> rechargeIdsByPayeeType(Integer payeeAccountType) {
        List<ShopDepositRecharge> recharges = shopDepositRechargeMapper.selectList(
                Wrappers.<ShopDepositRecharge>lambdaQuery()
                        .select(ShopDepositRecharge::getShopDepositRechargeId)
                        .eq(ShopDepositRecharge::getPayeeAccountType, payeeAccountType));
        return recharges.stream()
                .map(ShopDepositRecharge::getShopDepositRechargeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    // ====================== VO 装配 ======================

    private DepositLogVO toLogVO(ShopDepositLog log) {
        DepositLogVO vo = new DepositLogVO();
        vo.setShopDepositLogId(log.getShopDepositLogId());
        vo.setOwnerType(log.getOwnerType());
        vo.setOwnerTypeText(DepositOwnerType.getDescByCode(log.getOwnerType()));
        vo.setOwnerId(log.getOwnerId());
        vo.setType(DepositLogType.normalizeCode(log.getType()));
        vo.setTypeText(DepositLogType.getDescByCode(log.getType()));
        vo.setChangeAmount(log.getChangeAmount());
        vo.setBeforePaid(log.getBeforePaid());
        vo.setAfterPaid(log.getAfterPaid());
        vo.setRelatedId(log.getRelatedId());
        vo.setOrderId(log.getOrderId());
        vo.setAdminId(log.getAdminId());
        vo.setRemark(log.getRemark());
        vo.setAddTime(TigUtils.handelTime(log.getAddTime()));
        fillOwnerName(vo, log.getOwnerType(), log.getOwnerId());
        fillRelated(vo, log.getType(), log.getRelatedId());
        // 平台扣除：从保证金账户余额扣，无收款账户，支付方式记为"保证金余额"
        if (DepositLogType.DEDUCT.getCode().equals(log.getType())) {
            vo.setPayeeAccountTypeText("保证金余额");
        }
        return vo;
    }

    /** 按 owner_type 分派填充展示名：店铺取 shop+merchant，供应商取 vendor。 */
    private void fillOwnerName(DepositLogVO vo, Integer ownerType, Integer ownerId) {
        if (ownerId == null || ownerId <= 0) {
            return;
        }
        if (isSupplier(ownerType)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            if (vendor != null) {
                vo.setOwnerName(vendor.getVendorName());
                vo.setSubjectType(vendor.getType());
                vo.setSubjectTypeText(subjectTypeText(vendor.getType()));
            }
            return;
        }
        Shop shop = shopMapper.selectById(ownerId);
        if (shop == null) {
            return;
        }
        vo.setShopId(ownerId);
        vo.setShopTitle(shop.getShopTitle());
        vo.setOwnerName(shop.getShopTitle());
        if (shop.getMerchantId() != null && shop.getMerchantId() > 0) {
            Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
            if (merchant != null) {
                String merchantName = StrUtil.isNotBlank(merchant.getCompanyName())
                        ? merchant.getCompanyName() : merchant.getCorporateName();
                vo.setMerchantName(merchantName);
                vo.setSubjectType(merchant.getType());
                vo.setSubjectTypeText(subjectTypeText(merchant.getType()));
                Integer mStatus = merchant.getStatus();
                vo.setMerchantStatus(mStatus);
                if (mStatus != null && (mStatus == 1 || mStatus == 2)) {
                    vo.setMerchantStatusText(MerchantStatus.getStatusName(mStatus));
                }
            }
        }
    }

    /** 主体类型文案：1个人 2企业。 */
    private String subjectTypeText(Integer subjectType) {
        if (subjectType == null) {
            return "";
        }
        return subjectType == 2 ? "企业" : subjectType == 1 ? "个人" : "";
    }

    /**
     * 按流水类型 + 关联单id 回填凭证 / 支付方式 / 关联单备注。
     * 充值类取充值单，提现/退还类取退还单，扣款/调整类无关联单。
     */
    private void fillRelated(DepositLogVO vo, Integer type, Integer relatedId) {
        if (type == null || relatedId == null || relatedId <= 0) {
            return;
        }
        if (DepositLogType.RECHARGE.getCode().equals(type)) {
            ShopDepositRecharge recharge = shopDepositRechargeMapper.selectById(relatedId);
            if (recharge != null) {
                vo.setPayVoucher(recharge.getPayVoucher());
                vo.setPayeeAccountType(recharge.getPayeeAccountType());
                vo.setPayeeAccountTypeText(
                        DepositPayeeAccountType.getDescByCode(recharge.getPayeeAccountType()));
                vo.setRelatedRemark(recharge.getRemark());
                vo.setRelatedAuditRemark(recharge.getAuditRemark());
            }
        } else if (DepositLogType.WITHDRAW.getCode().equals(type)
                || DepositLogType.REFUND.getCode().equals(type)) {
            ShopDepositRefund refund = shopDepositRefundMapper.selectById(relatedId);
            if (refund != null) {
                vo.setPayVoucher(refund.getPayVoucher());
                vo.setRelatedRemark(refund.getReason());
                vo.setRelatedAuditRemark(refund.getAuditRemark());
            }
        }
    }

    /**
     * 计算交易时间区间。显式 startTime/endTime 优先；否则按 dateType 快捷区间。
     * 返回 [start, end]（秒级时间戳），0 表示不限制。
     */
    private long[] resolveTimeRange(DepositLogListDTO listDTO) {
        if (listDTO.getStartTime() != null || listDTO.getEndTime() != null) {
            return new long[]{
                    listDTO.getStartTime() == null ? 0L : listDTO.getStartTime(),
                    listDTO.getEndTime() == null ? 0L : listDTO.getEndTime()
            };
        }
        String dateType = listDTO.getDateType();
        if (StrUtil.isBlank(dateType)) {
            return new long[]{0L, 0L};
        }
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        long startOfTodaySec = today.atStartOfDay(zone).toEpochSecond();
        long nowSec = System.currentTimeMillis() / 1000L;
        return switch (dateType) {
            case "today" -> new long[]{startOfTodaySec, nowSec};
            case "yesterday" -> new long[]{
                    today.minusDays(1).atStartOfDay(zone).toEpochSecond(),
                    startOfTodaySec - 1
            };
            case "last7" -> new long[]{nowSec - 7L * 86400L, nowSec};
            case "last30" -> new long[]{nowSec - 30L * 86400L, nowSec};
            case "last180" -> new long[]{nowSec - 180L * 86400L, nowSec};
            default -> new long[]{0L, 0L};
        };
    }

    /** 列表分段：owner 类型 + 命中的 ownerId 约束集合（null=全量）。 */
    private record OwnerSegment(Integer ownerType, Set<Integer> idSet) {
    }
}
