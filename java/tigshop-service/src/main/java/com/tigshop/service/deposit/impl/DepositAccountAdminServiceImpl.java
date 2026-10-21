package com.tigshop.service.deposit.impl;

import org.springframework.security.access.prepost.PreAuthorize;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.deposit.DepositAccountListDTO;
import com.tigshop.bean.dto.deposit.DepositAdjustDTO;
import com.tigshop.bean.dto.deposit.DepositDeductDTO;
import com.tigshop.bean.dto.deposit.DepositSendWarningDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.deposit.DepositAdjustSource;
import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.enums.deposit.DepositPayeeAccountType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.deposit.ShopDeposit;
import com.tigshop.bean.model.deposit.ShopDepositAdjustLog;
import com.tigshop.bean.model.deposit.ShopDepositLog;
import com.tigshop.bean.model.deposit.ShopDepositRecharge;
import com.tigshop.bean.model.deposit.ShopDepositRefund;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.enums.vendor.VendorStatusEnum;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.vo.deposit.DepositAccountDetailVO;
import com.tigshop.bean.vo.deposit.DepositAccountVO;
import com.tigshop.bean.vo.deposit.DepositAdjustLogVO;
import com.tigshop.bean.vo.deposit.DepositLogVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.deposit.ShopDepositAdjustLogMapper;
import com.tigshop.mapper.deposit.ShopDepositLogMapper;
import com.tigshop.mapper.deposit.ShopDepositMapper;
import com.tigshop.mapper.deposit.ShopDepositRechargeMapper;
import com.tigshop.mapper.deposit.ShopDepositRefundMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.mapper.vendor.product.VendorProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.deposit.DepositAccountAdminService;
import com.tigshop.service.deposit.DepositAccountService;
import com.tigshop.service.deposit.DepositLifecycleService;
import com.tigshop.service.msg.AdminMsgService;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 平台端保证金账户管理服务实现（统一 owner 维度：店铺 + 供应商）。
 *
 * <p>本类只读调用地基 {@link DepositAccountService}（computeRequired/getPaid/三数/changeDeposit），
 * 手动调整应缴在本类内用 Mapper 事务实现，不修改地基记账核心。</p>
 *
 * <p>owner 维度：名称/经营类目/入驻时间来源按 owner_type 分派——店铺行 join shop + product 在售类目；
 * 供应商行 join vendor + vendor_product 在售类目。列表通过分段解析 ownerId 约束集合后组合 OR 条件，
 * 保持单表（shop_deposit）SQL 分页与排序不变。</p>
 *
 * @author Tigshop团队
 */
@Service
public class DepositAccountAdminServiceImpl extends BaseServiceImpl<ShopDepositMapper, ShopDeposit>
        implements DepositAccountAdminService {

    /** 保证金状态：正常（已缴 ≥ 应缴）。 */
    private static final int STATUS_NORMAL = 1;
    /** 保证金状态：不足（0 < 已缴 < 应缴）。 */
    private static final int STATUS_INSUFFICIENT = 2;
    /** 保证金状态：冻结（账户被惩罚/限制，penalty_status != 0）。 */
    private static final int STATUS_FROZEN = 3;
    /** 保证金状态：未缴纳（应缴 > 0 且已缴 = 0）。 */
    private static final int STATUS_UNPAID = 4;

    /** 经营类目展示最多拼接的类目名称数量。 */
    private static final int MAX_CATEGORY_NAMES = 5;
    /** 类目下钻收集的最大节点数（防御异常数据导致的过深递归）。 */
    private static final int MAX_CATEGORY_NODES = 500;

    /** 供应商在售商品：在售状态 product_state=1。 */
    private static final int VENDOR_PRODUCT_ON_SALE = 1;
    /** 供应商在售商品：审核通过 audit_state=1。 */
    private static final int VENDOR_PRODUCT_AUDIT_PASS = 1;

    @Resource
    private DepositAccountService depositAccountService;

    @Resource
    private DepositLifecycleService depositLifecycleService;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private VendorMapper vendorMapper;

    @Resource
    private VendorProductMapper vendorProductMapper;

    @Resource
    private ShopDepositLogMapper shopDepositLogMapper;

    @Resource
    private ShopDepositAdjustLogMapper shopDepositAdjustLogMapper;

    @Resource
    private ShopDepositRechargeMapper shopDepositRechargeMapper;

    @Resource
    private ShopDepositRefundMapper shopDepositRefundMapper;

    @Resource
    private AdminMsgService adminMsgService;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public Page<DepositAccountVO> list(DepositAccountListDTO listDTO) {
        List<Integer> ownerTypes = resolveOwnerTypes(listDTO.getOwnerType());
        // 统一名称搜索：优先 ownerName，回退兼容字段 shopTitle
        String name = StrUtil.isNotBlank(listDTO.getOwnerName()) ? listDTO.getOwnerName() : listDTO.getShopTitle();

        // 按 owner_type 分段解析可命中的 ownerId 约束集合：null=该类型无约束(全量)，空集=该类型被排除
        List<OwnerSegment> segments = new ArrayList<>();
        for (Integer ownerType : ownerTypes) {
            Set<Integer> idSet = resolveOwnerIdConstraint(ownerType, name, listDTO);
            if (idSet != null && idSet.isEmpty()) {
                continue;
            }
            segments.add(new OwnerSegment(ownerType, idSet));
        }
        if (segments.isEmpty()) {
            return new Page<>(listDTO.getPage(), listDTO.getSize(), 0);
        }

        MPJLambdaWrapper<ShopDeposit> wrapper = new MPJLambdaWrapper<>();
        applyOwnerSegments(wrapper, segments);

        Map<Integer, String> categoryNameCache = new HashMap<>();
        // 保证金状态依赖实时应缴（Java 侧计算），无法在 SQL 内过滤，故按状态筛选时取全量再内存分页
        if (listDTO.getDepositStatus() != null) {
            List<ShopDeposit> all = this.list(wrapper);
            List<DepositAccountVO> filtered = all.stream()
                    .map(acc -> toAccountVO(acc, categoryNameCache))
                    .filter(vo -> Objects.equals(vo.getDepositStatus(), listDTO.getDepositStatus()))
                    .collect(Collectors.toList());
            return manualPaginate(filtered, listDTO.getPage(), listDTO.getSize());
        }

        Page<ShopDeposit> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        Page<ShopDeposit> accountPage = this.page(page, wrapper);
        List<DepositAccountVO> voList = accountPage.getRecords().stream()
                .map(acc -> toAccountVO(acc, categoryNameCache))
                .toList();
        return PageUtil.convertPage(accountPage, voList);
    }

    @Override
    public DepositAccountDetailVO detail(Integer ownerType, Integer ownerId) {
        int ot = resolveOwnerType(ownerType);
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者id非法");
        }
        ShopDeposit account = selectByOwner(ot, ownerId);
        BigDecimal paid = depositAccountService.getPaid(ot, ownerId);
        BigDecimal required = depositAccountService.computeRequired(ot, ownerId);
        BigDecimal pending = max0(required.subtract(paid));
        Integer penaltyStatus = account == null ? 0 : account.getPenaltyStatus();

        DepositAccountDetailVO vo = new DepositAccountDetailVO();
        vo.setOwnerType(ot);
        vo.setOwnerTypeText(DepositOwnerType.getDescByCode(ot));
        vo.setOwnerId(ownerId);
        if (isSupplier(ot)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            if (vendor == null) {
                throw new GlobalException("供应商不存在");
            }
            vo.setOwnerName(vendor.getVendorName());
            vo.setOpenTime(TigUtils.handelTime(vendor.getAddTime()));
            vo.setBusinessCategory(businessCategory(ot, ownerId, new HashMap<>()));
            vo.setSubjectType(vendor.getType());
            vo.setSubjectTypeText(subjectTypeText(vendor.getType()));
            vo.setVendorStatus(vendor.getStatus());
            vo.setVendorStatusText(vendorStatusText(vendor.getStatus()));
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            if (shop == null) {
                throw new GlobalException("店铺不存在");
            }
            vo.setOwnerName(shop.getShopTitle());
            vo.setShopId(ownerId);
            vo.setShopTitle(shop.getShopTitle());
            vo.setShopType(shop.getShopType());
            vo.setShopTypeText(shopTypeText(shop.getShopType()));
            vo.setShopStatus(shop.getStatus());
            vo.setShopStatusText(shopStatusText(shop.getStatus()));
            vo.setBusinessCategory(businessCategory(ot, ownerId, new HashMap<>()));
            vo.setOpenTime(TigUtils.handelTime(shop.getAddTime()));
            vo.setMerchantName(merchantName(shop.getMerchantId()));
            if (shop.getMerchantId() != null && shop.getMerchantId() > 0) {
                Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
                if (merchant != null) {
                    vo.setSubjectType(merchant.getType());
                    vo.setSubjectTypeText(subjectTypeText(merchant.getType()));
                }
            }
        }
        vo.setRequired(required);
        vo.setPaid(paid);
        vo.setFrozen(paid.min(required));
        vo.setAvailable(max0(paid.subtract(required)));
        vo.setPending(pending);
        int status = resolveStatus(penaltyStatus, paid, required);
        vo.setDepositStatus(status);
        vo.setDepositStatusText(statusText(status));
        return vo;
    }

    @Override
    public Page<DepositLogVO> logList(Integer ownerType, Integer ownerId, BasePage pageParam) {
        int ot = resolveOwnerType(ownerType);
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者id非法");
        }
        Page<ShopDepositLog> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        Page<ShopDepositLog> logPage = shopDepositLogMapper.selectPage(page,
                Wrappers.<ShopDepositLog>lambdaQuery()
                        .eq(ShopDepositLog::getOwnerType, ot)
                        .eq(ShopDepositLog::getOwnerId, ownerId)
                        .orderByDesc(ShopDepositLog::getShopDepositLogId));
        OwnerName ownerName = resolveOwnerName(ot, ownerId);
        List<DepositLogVO> voList = logPage.getRecords().stream()
                .map(log -> toLogVO(log, ownerName))
                .toList();
        return PageUtil.convertPage(logPage, voList);
    }

    @Override
    public Page<DepositAdjustLogVO> adjustLogList(Integer ownerType, Integer ownerId, BasePage pageParam) {
        int ot = resolveOwnerType(ownerType);
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者id非法");
        }
        Page<ShopDepositAdjustLog> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        Page<ShopDepositAdjustLog> logPage = shopDepositAdjustLogMapper.selectPage(page,
                Wrappers.<ShopDepositAdjustLog>lambdaQuery()
                        .eq(ShopDepositAdjustLog::getOwnerType, ot)
                        .eq(ShopDepositAdjustLog::getOwnerId, ownerId)
                        .orderByDesc(ShopDepositAdjustLog::getShopDepositAdjustId));
        OwnerName ownerName = resolveOwnerName(ot, ownerId);
        Map<Integer, AdminUser> adminMap = loadAdmins(logPage.getRecords().stream()
                .map(ShopDepositAdjustLog::getAdminId).collect(Collectors.toList()));
        List<DepositAdjustLogVO> voList = logPage.getRecords().stream().map(log -> {
            DepositAdjustLogVO vo = new DepositAdjustLogVO();
            vo.setShopDepositAdjustId(log.getShopDepositAdjustId());
            vo.setOwnerType(log.getOwnerType());
            vo.setOwnerTypeText(DepositOwnerType.getDescByCode(log.getOwnerType()));
            vo.setOwnerId(log.getOwnerId());
            vo.setOwnerName(ownerName.name());
            if (!isSupplier(log.getOwnerType())) {
                vo.setShopId(log.getOwnerId());
            }
            vo.setBeforeRequired(log.getBeforeRequired());
            vo.setAfterRequired(log.getAfterRequired());
            vo.setSource(log.getSource());
            vo.setSourceText(DepositAdjustSource.getDescByCode(log.getSource()));
            vo.setAdminId(log.getAdminId());
            AdminUser admin = log.getAdminId() == null ? null : adminMap.get(log.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getUsername());
                vo.setAdminAvatar(admin.getAvatar());
            } else if (log.getAdminId() != null && log.getAdminId() == 0) {
                // adminId=0 表示系统自动调整（如类目变更），展示"系统"
                vo.setAdminName("系统");
            }
            vo.setReason(log.getReason());
            vo.setAddTime(TigUtils.handelTime(log.getAddTime()));
            return vo;
        }).toList();
        return PageUtil.convertPage(logPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositAdjust')")
    public void adjust(DepositAdjustDTO dto, Integer adminId) {
        Integer ownerId = resolveOwnerId(dto == null ? null : dto.getOwnerId(), dto == null ? null : dto.getShopId());
        if (dto == null || ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者id非法");
        }
        int ownerType = resolveOwnerType(dto.getOwnerType());
        Integer adjustType = dto.getAdjustType();
        boolean toManual = Objects.equals(adjustType, DepositAdjustSource.MANUAL.getCode());
        boolean toAuto = Objects.equals(adjustType, DepositAdjustSource.BY_CATEGORY_MAX.getCode());
        if (!toManual && !toAuto) {
            throw new GlobalException("调整方式非法");
        }
        if (toManual && (dto.getRequiredManual() == null
                || dto.getRequiredManual().compareTo(BigDecimal.ZERO) < 0)) {
            throw new GlobalException("手动应缴金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        // 加锁确保账户行存在并锁定（getOrCreate 为 REQUIRED 事务，加入当前事务）
        depositAccountService.getOrCreate(ownerType, ownerId);
        BigDecimal beforeRequired = depositAccountService.computeRequired(ownerType, ownerId);

        long now = System.currentTimeMillis() / 1000L;
        BigDecimal afterRequired;
        int source;
        if (toManual) {
            this.update(Wrappers.<ShopDeposit>lambdaUpdate()
                    .eq(ShopDeposit::getOwnerType, ownerType)
                    .eq(ShopDeposit::getOwnerId, ownerId)
                    .set(ShopDeposit::getRequiredManual, dto.getRequiredManual())
                    .set(ShopDeposit::getUpdateTime, now));
            afterRequired = dto.getRequiredManual();
            source = DepositAdjustSource.MANUAL.getCode();
        } else {
            this.update(Wrappers.<ShopDeposit>lambdaUpdate()
                    .eq(ShopDeposit::getOwnerType, ownerType)
                    .eq(ShopDeposit::getOwnerId, ownerId)
                    .set(ShopDeposit::getRequiredManual, null)
                    .set(ShopDeposit::getUpdateTime, now));
            afterRequired = depositAccountService.computeRequired(ownerType, ownerId);
            source = DepositAdjustSource.BY_CATEGORY_MAX.getCode();
        }

        // 同步应缴快照（手动=固定值；按类目最高=当前实时应缴），作为后续类目变更 diff 的基准
        this.update(Wrappers.<ShopDeposit>lambdaUpdate()
                .eq(ShopDeposit::getOwnerType, ownerType)
                .eq(ShopDeposit::getOwnerId, ownerId)
                .set(ShopDeposit::getRequiredSnapshot, afterRequired)
                .set(ShopDeposit::getUpdateTime, now));

        ShopDepositAdjustLog log = new ShopDepositAdjustLog();
        log.setOwnerType(ownerType);
        log.setOwnerId(ownerId);
        log.setBeforeRequired(beforeRequired);
        log.setAfterRequired(afterRequired);
        log.setSource(source);
        log.setAdminId(adminId == null ? 0 : adminId);
        log.setReason(dto.getReason());
        log.setAddTime(now);
        shopDepositAdjustLogMapper.insert(log);

        // 应缴变动后对账：应缴下调致足额(pending<=0)则即时恢复(清双缓冲/解除处罚/恢复下架商品)；
        // 否则应缴上调(after>before 且 before>0)启动补缴缓冲。restore/onSupplementTrigger 均幂等、非阻断。
        if (depositAccountService.pending(ownerType, ownerId).compareTo(BigDecimal.ZERO) <= 0) {
            depositLifecycleService.restore(ownerType, ownerId);
        } else if (afterRequired != null && afterRequired.compareTo(beforeRequired) > 0
                && beforeRequired.compareTo(BigDecimal.ZERO) > 0) {
            depositLifecycleService.onSupplementTrigger(ownerType, ownerId);
        }
    }

    @Override
    @PreAuthorize("@pms.hasPermission('depositDeduct')")
    public void deduct(DepositDeductDTO dto, Integer adminId) {
        Integer ownerId = resolveOwnerId(dto == null ? null : dto.getOwnerId(), dto == null ? null : dto.getShopId());
        if (dto == null || ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者id非法");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException("扣除金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        int ownerType = resolveOwnerType(dto.getOwnerType());
        Integer orderId = dto.getOrderId() == null ? 0 : dto.getOrderId();
        // 走地基记账核心：DEDUCT 出账，余额不足由地基抛 DEPOSIT_BALANCE_NOT_ENOUGH
        depositAccountService.changeDeposit(ownerType, ownerId, dto.getAmount(), DepositLogType.DEDUCT,
                0, orderId, adminId == null ? 0 : adminId, dto.getReason());

        // 赔付扣除致 paid 减少、产生新缺口（pending>0）→ 启动补缴缓冲（幂等、非阻断）
        if (depositAccountService.pending(ownerType, ownerId).compareTo(BigDecimal.ZERO) > 0) {
            depositLifecycleService.onSupplementTrigger(ownerType, ownerId);
        }
    }

    @Override
    @PreAuthorize("@pms.hasPermission('depositSendWarning')")
    public void sendWarning(DepositSendWarningDTO dto, Integer adminId) {
        if (dto == null) {
            throw new GlobalException("参数非法");
        }
        int ownerType = resolveOwnerType(dto.getOwnerType());
        Integer ownerId = resolveOwnerId(dto.getOwnerId(), dto.getShopId());
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者id非法");
        }
        // 校验持有者存在并确定文案中的主体称谓
        String ownerLabel;
        if (isSupplier(ownerType)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            if (vendor == null) {
                throw new GlobalException("供应商不存在");
            }
            ownerLabel = "供应商";
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            if (shop == null) {
                throw new GlobalException("店铺不存在");
            }
            ownerLabel = "店铺";
        }

        BigDecimal pending = depositAccountService.pending(ownerType, ownerId);
        String title = StrUtil.isNotBlank(dto.getTitle()) ? dto.getTitle() : "保证金预警通知";
        String content;
        if (StrUtil.isNotBlank(dto.getContent())) {
            content = dto.getContent();
        } else if (pending.compareTo(BigDecimal.ZERO) > 0) {
            content = "您的" + ownerLabel + "保证金待补缴 " + pending.stripTrailingZeros().toPlainString()
                    + " 元，请尽快补足，以免影响正常经营。";
        } else {
            content = "您的" + ownerLabel + "保证金存在异常，请前往财务中心-保证金查看并处理。";
        }

        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("ownerType", ownerType);
        relatedData.put("ownerId", ownerId);

        AdminMsgCreateDTO msg = new AdminMsgCreateDTO();
        // 独立「保证金预警」消息分类
        msg.setMsgType(AdminMsgTypeEnum.DEPOSIT_WARNING.getCatId());
        // admin_id=0 表示整店铺/供应商可见（与其它站内信一致）；msgTypeArr 未读统计按 admin_id=0 过滤，
        // 不可写入发预警的平台管理员 id，否则该消息被排除、分类未读数漏计。
        msg.setAdminId(0);
        msg.setTitle(title);
        msg.setContent(content);
        // 按 owner 维度定向：供应商发往 vendorId，店铺发往 shopId。
        // 站内信列表对店铺身份按 (shopId, vendorId=0) 过滤、对供应商身份按 vendorId 过滤，
        // 故两侧 id 都显式置 0，确保各自身份只看到属于自己的消息。
        if (isSupplier(ownerType)) {
            msg.setVendorId(ownerId);
            msg.setShopId(0);
        } else {
            msg.setShopId(ownerId);
            msg.setVendorId(0);
            // 兼容旧前端：店铺消息仍透出 shopId 供跳转
            relatedData.put("shopId", ownerId);
        }
        msg.setRelatedData(relatedData);
        adminMsgService.createMessage(msg);
    }

    // ====================== owner 维度解析辅助 ======================

    /** 列表查询的目标 owner 类型集合：null=全部(店铺+供应商)，否则单一类型。 */
    private List<Integer> resolveOwnerTypes(Integer ownerType) {
        if (ownerType == null) {
            return List.of(DepositOwnerType.SHOP.getCode(), DepositOwnerType.SUPPLIER.getCode());
        }
        return List.of(ownerType);
    }

    /** owner 类型默认值：空视为店铺(1)，兼容历史仅传 shopId 的调用。 */
    private int resolveOwnerType(Integer ownerType) {
        return ownerType == null ? DepositOwnerType.SHOP.getCode() : ownerType;
    }

    /** owner id 解析：优先 ownerId，回退兼容字段 shopId。 */
    private Integer resolveOwnerId(Integer ownerId, Integer shopId) {
        return ownerId != null ? ownerId : shopId;
    }

    private boolean isSupplier(Integer ownerType) {
        return DepositOwnerType.SUPPLIER.getCode().equals(ownerType);
    }

    /**
     * 解析某一 owner 类型下，名称/商户/经营类目/入驻时间筛选命中的 ownerId 集合。
     *
     * @return null=该类型无任何筛选约束（全量）；非空集=约束命中的 ownerId；空集=该类型被筛选排除。
     */
    private Set<Integer> resolveOwnerIdConstraint(Integer ownerType, String name, DepositAccountListDTO listDTO) {
        List<Set<Integer>> constraints = new ArrayList<>();
        boolean hasName = StrUtil.isNotBlank(name);
        boolean hasMerchant = StrUtil.isNotBlank(listDTO.getMerchantName());
        boolean hasJoinTime = listDTO.getJoinTimeStart() != null || listDTO.getJoinTimeEnd() != null;
        boolean hasCategory = listDTO.getCategoryId() != null && listDTO.getCategoryId() > 0;
        boolean hasSubjectType = listDTO.getSubjectType() != null;

        if (isSupplier(ownerType)) {
            // 供应商无商户概念：传了商户名筛选则直接排除供应商行
            if (hasMerchant) {
                return Collections.emptySet();
            }
            if (hasName) {
                constraints.add(vendorIdsByName(name));
            }
            if (hasJoinTime) {
                constraints.add(vendorIdsByJoinTime(listDTO.getJoinTimeStart(), listDTO.getJoinTimeEnd()));
            }
            if (hasCategory) {
                constraints.add(vendorIdsByCategory(listDTO.getCategoryId()));
            }
            if (hasSubjectType) {
                constraints.add(vendorIdsBySubjectType(listDTO.getSubjectType()));
            }
        } else {
            if (hasName) {
                constraints.add(shopIdsByName(name));
            }
            if (hasMerchant) {
                constraints.add(shopIdsByMerchantName(listDTO.getMerchantName()));
            }
            if (hasJoinTime) {
                constraints.add(shopIdsByJoinTime(listDTO.getJoinTimeStart(), listDTO.getJoinTimeEnd()));
            }
            if (hasCategory) {
                constraints.add(new HashSet<>(shopIdsByCategory(listDTO.getCategoryId())));
            }
            if (hasSubjectType) {
                constraints.add(shopIdsByMerchantType(listDTO.getSubjectType()));
            }
        }
        if (constraints.isEmpty()) {
            return null;
        }
        return intersect(constraints);
    }

    /** 将分段 owner 约束组装为 wrapper 查询条件，保持单表 shop_deposit 分页/排序。 */
    private void applyOwnerSegments(MPJLambdaWrapper<ShopDeposit> wrapper, List<OwnerSegment> segments) {
        boolean allUnbounded = segments.stream().allMatch(s -> s.idSet() == null);
        if (allUnbounded) {
            // 无名称/类目/时间约束：仅按 owner_type 过滤
            List<Integer> types = segments.stream().map(OwnerSegment::ownerType).toList();
            if (types.size() == 1) {
                wrapper.eq(ShopDeposit::getOwnerType, types.get(0));
            } else {
                wrapper.in(ShopDeposit::getOwnerType, types);
            }
            return;
        }
        if (segments.size() == 1) {
            OwnerSegment seg = segments.get(0);
            wrapper.eq(ShopDeposit::getOwnerType, seg.ownerType());
            if (seg.idSet() != null) {
                wrapper.in(ShopDeposit::getOwnerId, seg.idSet());
            }
            return;
        }
        // 多段且至少一段带 ownerId 约束：组合为 (type1 AND id IN s1) OR (type2 AND id IN s2)
        wrapper.and(outer -> {
            boolean first = true;
            for (OwnerSegment seg : segments) {
                if (first) {
                    outer.and(o -> {
                        o.eq(ShopDeposit::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDeposit::getOwnerId, seg.idSet());
                        }
                    });
                    first = false;
                } else {
                    outer.or(o -> {
                        o.eq(ShopDeposit::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDeposit::getOwnerId, seg.idSet());
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

    // ====================== 店铺维度筛选来源 ======================

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

    /** 按主体类型（商户性质 1个人 2企业）取对应店铺id集合。 */
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

    private Set<Integer> shopIdsByJoinTime(Long start, Long end) {
        List<Shop> shops = shopMapper.selectList(Wrappers.<Shop>lambdaQuery()
                .select(Shop::getShopId)
                .ge(start != null, Shop::getAddTime, start)
                .le(end != null, Shop::getAddTime, end));
        return shops.stream().map(Shop::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    // ====================== 供应商维度筛选来源 ======================

    private Set<Integer> vendorIdsByName(String name) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .like(Vendor::getVendorName, name));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 按主体类型（供应商性质 1个人 2企业）取对应供应商id集合。 */
    private Set<Integer> vendorIdsBySubjectType(Integer subjectType) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .eq(Vendor::getType, subjectType));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<Integer> vendorIdsByJoinTime(Long start, Long end) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .ge(start != null, Vendor::getAddTime, start)
                .le(end != null, Vendor::getAddTime, end));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 取指定类目及其所有下级类目下，有在售（审核通过、未删）商品的供应商id集合。 */
    private Set<Integer> vendorIdsByCategory(Integer categoryId) {
        Set<Integer> categoryIds = collectCategoryAndDescendants(categoryId);
        if (categoryIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<VendorProduct> products = vendorProductMapper.selectList(Wrappers.<VendorProduct>lambdaQuery()
                .select(VendorProduct::getVendorId)
                .in(VendorProduct::getProductCategoryId, categoryIds)
                .eq(VendorProduct::getProductState, VENDOR_PRODUCT_ON_SALE)
                .eq(VendorProduct::getAuditState, VENDOR_PRODUCT_AUDIT_PASS));
        return products.stream()
                .map(VendorProduct::getVendorId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
    }

    // ====================== VO 装配 ======================

    private DepositAccountVO toAccountVO(ShopDeposit acc, Map<Integer, String> categoryNameCache) {
        Integer ownerType = acc.getOwnerType();
        Integer ownerId = acc.getOwnerId();
        BigDecimal paid = acc.getPaid() == null ? BigDecimal.ZERO : acc.getPaid();
        BigDecimal required = depositAccountService.computeRequired(ownerType, ownerId);
        BigDecimal pending = max0(required.subtract(paid));

        DepositAccountVO vo = new DepositAccountVO();
        vo.setOwnerType(ownerType);
        vo.setOwnerTypeText(DepositOwnerType.getDescByCode(ownerType));
        vo.setOwnerId(ownerId);
        if (isSupplier(ownerType)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            vo.setOwnerName(vendor == null ? null : vendor.getVendorName());
            if (vendor != null) {
                vo.setSubjectType(vendor.getType());
                vo.setSubjectTypeText(subjectTypeText(vendor.getType()));
            }
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            vo.setOwnerName(shop == null ? null : shop.getShopTitle());
            vo.setShopId(ownerId);
            vo.setShopTitle(shop == null ? null : shop.getShopTitle());
            if (shop != null && shop.getMerchantId() != null && shop.getMerchantId() > 0) {
                Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
                if (merchant != null) {
                    vo.setMerchantName(StrUtil.isNotBlank(merchant.getCompanyName())
                            ? merchant.getCompanyName() : merchant.getCorporateName());
                    vo.setSubjectType(merchant.getType());
                    vo.setSubjectTypeText(subjectTypeText(merchant.getType()));
                }
            }
        }
        vo.setBusinessCategory(businessCategory(ownerType, ownerId, categoryNameCache));
        vo.setRequired(required);
        vo.setPaid(paid);
        vo.setPending(pending);
        int status = resolveStatus(acc.getPenaltyStatus(), paid, required);
        vo.setDepositStatus(status);
        vo.setDepositStatusText(statusText(status));
        return vo;
    }

    private DepositLogVO toLogVO(ShopDepositLog log, OwnerName ownerName) {
        DepositLogVO vo = new DepositLogVO();
        vo.setShopDepositLogId(log.getShopDepositLogId());
        vo.setOwnerType(log.getOwnerType());
        vo.setOwnerTypeText(DepositOwnerType.getDescByCode(log.getOwnerType()));
        vo.setOwnerId(log.getOwnerId());
        vo.setOwnerName(ownerName.name());
        if (!isSupplier(log.getOwnerType())) {
            vo.setShopId(log.getOwnerId());
            vo.setShopTitle(ownerName.shopTitle());
            vo.setMerchantName(ownerName.merchantName());
        }
        vo.setType(log.getType());
        vo.setTypeText(DepositLogType.getDescByCode(log.getType()));
        vo.setChangeAmount(log.getChangeAmount());
        vo.setBeforePaid(log.getBeforePaid());
        vo.setAfterPaid(log.getAfterPaid());
        vo.setRelatedId(log.getRelatedId());
        vo.setOrderId(log.getOrderId());
        vo.setAdminId(log.getAdminId());
        vo.setRemark(log.getRemark());
        vo.setAddTime(TigUtils.handelTime(log.getAddTime()));
        fillRelated(vo, log.getType(), log.getRelatedId());
        return vo;
    }

    /**
     * 按流水类型 + 关联单id 回填凭证 / 支付方式 / 关联单备注（与全局明细列表口径一致）。
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

    /** 解析 owner 展示名（店铺名/供应商名）及店铺兼容字段。 */
    private OwnerName resolveOwnerName(Integer ownerType, Integer ownerId) {
        if (isSupplier(ownerType)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            return new OwnerName(vendor == null ? null : vendor.getVendorName(), null, null);
        }
        Shop shop = shopMapper.selectById(ownerId);
        if (shop == null) {
            return new OwnerName(null, null, null);
        }
        String mName = merchantName(shop.getMerchantId());
        return new OwnerName(shop.getShopTitle(), shop.getShopTitle(), mName);
    }

    // ====================== 状态/文案 ======================

    /**
     * 保证金状态判定：冻结 > 未缴纳 > 不足 > 正常。
     */
    private int resolveStatus(Integer penaltyStatus, BigDecimal paid, BigDecimal required) {
        if (penaltyStatus != null && penaltyStatus != 0) {
            return STATUS_FROZEN;
        }
        BigDecimal p = paid == null ? BigDecimal.ZERO : paid;
        BigDecimal r = required == null ? BigDecimal.ZERO : required;
        if (r.compareTo(BigDecimal.ZERO) > 0 && p.compareTo(BigDecimal.ZERO) <= 0) {
            return STATUS_UNPAID;
        }
        if (p.compareTo(r) < 0) {
            return STATUS_INSUFFICIENT;
        }
        return STATUS_NORMAL;
    }

    private String statusText(int status) {
        return switch (status) {
            case STATUS_INSUFFICIENT -> "不足";
            case STATUS_FROZEN -> "冻结";
            case STATUS_UNPAID -> "未缴纳";
            default -> "正常";
        };
    }

    /** 主体类型（商户性质）文案：1个人 2企业。 */
    private String subjectTypeText(Integer subjectType) {
        if (subjectType == null) {
            return "";
        }
        return subjectType == 2 ? "企业" : subjectType == 1 ? "个人" : "";
    }

    private String shopTypeText(Integer shopType) {
        if (shopType == null) {
            return "";
        }
        return switch (shopType) {
            case 1 -> "店铺";
            case 2 -> "门店";
            case 3 -> "自提点";
            default -> "";
        };
    }

    /** 店铺状态文案：1开业 4暂停运营 10关店；口径与 ShopType 枚举一致，未知值返回 ""。 */
    private String shopStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        try {
            return ShopType.getTypeName(status);
        } catch (Exception e) {
            return "";
        }
    }

    /** 供应商状态文案：1开启 2关闭；口径与 VendorStatusEnum 一致，未知值返回 ""。 */
    private String vendorStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        for (VendorStatusEnum e : VendorStatusEnum.values()) {
            if (e.getCode() == status) {
                return e.getDescription();
            }
        }
        return "";
    }

    private String merchantName(Integer merchantId) {
        if (merchantId == null || merchantId <= 0) {
            return null;
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            return null;
        }
        if (StrUtil.isNotBlank(merchant.getCompanyName())) {
            return merchant.getCompanyName();
        }
        return merchant.getCorporateName();
    }

    /**
     * 经营类目展示：owner 在售商品类目名称，去重后顿号拼接（最多 {@link #MAX_CATEGORY_NAMES} 个）。
     * 来源按 owner_type 分派：店铺取 product 在售类目，供应商取 vendor_product 在售类目。
     */
    private String businessCategory(Integer ownerType, Integer ownerId, Map<Integer, String> categoryNameCache) {
        List<Integer> categoryIds = isSupplier(ownerType)
                ? baseMapper.selectVendorOnSaleCategoryIds(ownerId)
                : baseMapper.selectOnSaleCategoryIds(ownerId);
        if (categoryIds == null || categoryIds.isEmpty()) {
            return "";
        }
        Set<String> names = new LinkedHashSet<>();
        for (Integer categoryId : categoryIds) {
            if (categoryId == null || categoryId <= 0) {
                continue;
            }
            String name = categoryNameCache.computeIfAbsent(categoryId, id -> {
                Category category = categoryMapper.selectById(id);
                return category == null ? null : category.getCategoryName();
            });
            if (StrUtil.isNotBlank(name)) {
                names.add(name);
            }
            if (names.size() >= MAX_CATEGORY_NAMES) {
                break;
            }
        }
        return String.join("、", names);
    }

    /**
     * 取指定类目及其所有下级类目下，有在售商品的店铺id集合。
     */
    private List<Integer> shopIdsByCategory(Integer categoryId) {
        Set<Integer> categoryIds = collectCategoryAndDescendants(categoryId);
        if (categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Product> products = productMapper.selectList(Wrappers.<Product>lambdaQuery()
                .select(Product::getShopId)
                .in(Product::getCategoryId, categoryIds)
                .eq(Product::getProductStatus, 1)
                .and(w -> w.isNull(Product::getIsDelete).or().eq(Product::getIsDelete, 0)));
        return products.stream()
                .map(Product::getShopId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private Set<Integer> collectCategoryAndDescendants(Integer categoryId) {
        Set<Integer> result = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(categoryId);
        while (!queue.isEmpty() && result.size() < MAX_CATEGORY_NODES) {
            Integer current = queue.poll();
            if (current == null || current <= 0 || !result.add(current)) {
                continue;
            }
            List<Category> children = categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                    .select(Category::getCategoryId)
                    .eq(Category::getParentId, current));
            for (Category child : children) {
                if (child.getCategoryId() != null && !result.contains(child.getCategoryId())) {
                    queue.add(child.getCategoryId());
                }
            }
        }
        return result;
    }

    private ShopDeposit selectByOwner(Integer ownerType, Integer ownerId) {
        return baseMapper.selectOne(Wrappers.<ShopDeposit>lambdaQuery()
                .eq(ShopDeposit::getOwnerType, ownerType)
                .eq(ShopDeposit::getOwnerId, ownerId));
    }

    private Page<DepositAccountVO> manualPaginate(List<DepositAccountVO> all, Integer pageNo, Integer pageSize) {
        int total = all.size();
        Page<DepositAccountVO> result = new Page<>(pageNo, pageSize, total);
        int from = (pageNo - 1) * pageSize;
        if (from >= total || from < 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        int to = Math.min(from + pageSize, total);
        result.setRecords(new ArrayList<>(all.subList(from, to)));
        return result;
    }

    /** 批量加载操作管理员（id→AdminUser），用于回填操作人名称/头像。 */
    private Map<Integer, AdminUser> loadAdmins(List<Integer> adminIds) {
        Set<Integer> ids = adminIds.stream()
                .filter(id -> id != null && id > 0).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(AdminUser::getAdminId, a -> a, (a, b) -> a));
    }

    private static BigDecimal max0(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }

    /** 列表分段：owner 类型 + 命中的 ownerId 约束集合（null=全量）。 */
    private record OwnerSegment(Integer ownerType, Set<Integer> idSet) {
    }

    /** owner 展示名解析结果：name=统一展示名；shopTitle/merchantName 仅店铺行有值。 */
    private record OwnerName(String name, String shopTitle, String merchantName) {
    }
}
