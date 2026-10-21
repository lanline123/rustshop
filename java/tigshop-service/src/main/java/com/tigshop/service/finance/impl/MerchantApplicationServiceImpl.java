package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.profitsharing.MerchantApplicationRequest;
import com.tigshop.bean.dto.finance.profitsharing.MerchantApplicationResult;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.MerchantApplicationStatus;
import com.tigshop.bean.enums.finance.SubMerchantStatus;
import com.tigshop.bean.model.finance.MerchantApplication;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.finance.MerchantApplicationSubmitParam;
import com.tigshop.bean.query.finance.MerchantApplicationPageQuery;
import com.tigshop.bean.vo.finance.MerchantApplicationVO;
import com.tigshop.common.constant.setting.ConfigConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.finance.MerchantApplicationMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.MerchantApplicationMediaService;
import com.tigshop.service.finance.MerchantApplicationService;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.bean.enums.setting.SettingsEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 商户进件 Service 实现
 *
 * <p>重构后流程：店铺/供应商端提交申请（PENDING_REVIEW）→ 平台端审核通过（提交微信进件）/ 拒绝（REJECTED）。
 *
 * @author Tigshop团队
 */
@Service
@Slf4j
public class MerchantApplicationServiceImpl
        extends BaseServiceImpl<MerchantApplicationMapper, MerchantApplication>
        implements MerchantApplicationService {

    private static final String DEFAULT_CHANNEL = "WECHAT";
    private static final String CHANNEL_HUIFU = "HUIFU";
    private static final String CHANNEL_ALIPAY = "ALIPAY";
    private static final String APPLICANT_SHOP = "SHOP";
    private static final String APPLICANT_VENDOR = "VENDOR";
    private static final String MEDIA_OWNER_PLATFORM = "PLATFORM";
    /** 平台自营（shopId=0）为虚拟门店，不在 shop 表，展示名固定 */
    private static final String SELF_OPERATED_SHOP_NAME = "平台自营";
    private static final String APPROVE_LOCK_PREFIX = "merchant_application:approve:";
    private static final long APPROVE_LOCK_SECONDS = 600L;

    /** 已进入微信流程或已完成的状态，禁止重复申请 */
    private static final Set<String> IN_PROGRESS_STATUS = Set.of(
            MerchantApplicationStatus.SUBMITTED.getCode(),
            MerchantApplicationStatus.CHECKING.getCode(),
            MerchantApplicationStatus.AUDITING.getCode(),
            MerchantApplicationStatus.ACCOUNT_NEED_VERIFY.getCode(),
            MerchantApplicationStatus.NEED_SIGN.getCode(),
            MerchantApplicationStatus.FINISH.getCode());

    private final ShopMapper shopMapper;
    private final SubMerchantService subMerchantService;
    private final ProfitSharingClientRegistry profitSharingClientRegistry;
    private final ConfigService configService;
    private final MerchantApplicationMediaService merchantApplicationMediaService;
    private final RedisCache redisCache;
    private final Clock clock;

    @Autowired
    public MerchantApplicationServiceImpl(
            ShopMapper shopMapper,
            SubMerchantService subMerchantService,
            ProfitSharingClientRegistry profitSharingClientRegistry,
            ConfigService configService,
            MerchantApplicationMediaService merchantApplicationMediaService,
            RedisCache redisCache) {
        this(shopMapper,
                subMerchantService, profitSharingClientRegistry, configService,
                merchantApplicationMediaService, redisCache, Clock.systemDefaultZone());
    }

    MerchantApplicationServiceImpl(
            ShopMapper shopMapper,
            SubMerchantService subMerchantService,
            ProfitSharingClientRegistry profitSharingClientRegistry,
            ConfigService configService,
            MerchantApplicationMediaService merchantApplicationMediaService,
            RedisCache redisCache,
            Clock clock) {
        this.shopMapper = shopMapper;
        this.subMerchantService = subMerchantService;
        this.profitSharingClientRegistry = profitSharingClientRegistry;
        this.configService = configService;
        this.merchantApplicationMediaService = merchantApplicationMediaService;
        this.redisCache = redisCache;
        this.clock = clock;
    }

    @Override
    public Page<MerchantApplicationVO> listPage(MerchantApplicationPageQuery query) {
        Page<MerchantApplication> pageParam = buildSortOrder(query);
        LambdaQueryWrapper<MerchantApplication> wrapper = new LambdaQueryWrapper<>();
        if (!isPlatformAdmin()) {
            // 店铺/供应商主账号的授权列表是 all，注解拦不住，这里强制收口到本主体，忽略入参 shopId
            ApplicantContext applicant = resolveApplicant();
            wrapper.eq(MerchantApplication::getApplicantType, applicant.type())
                    .eq(MerchantApplication::getApplicantId, applicant.id());
        } else if (query.getShopId() != null) {
            wrapper.eq(MerchantApplication::getShopId, query.getShopId());
        }
        if (StrUtil.isNotBlank(query.getApplicantType())) {
            wrapper.eq(MerchantApplication::getApplicantType, query.getApplicantType());
        }
        if (StrUtil.isNotBlank(query.getStatus())) {
            wrapper.eq(MerchantApplication::getStatus, query.getStatus());
        }
        if (StrUtil.isNotBlank(query.getStatusList())) {
            List<String> statusList = StrUtil.split(query.getStatusList(), ',').stream()
                    .map(StrUtil::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
            if (!statusList.isEmpty()) {
                wrapper.in(MerchantApplication::getStatus, statusList);
            }
        }
        wrapper.orderByDesc(MerchantApplication::getId);
        Page<MerchantApplication> entityPage = this.page(pageParam, wrapper);

        Page<MerchantApplicationVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<MerchantApplicationVO> voList = entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillShopNames(voList);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplicationVO apply(MerchantApplicationSubmitParam param) {
        ApplicantContext applicant = resolveApplicant();
        validateEnumValues(param);
        if (!APPLICANT_VENDOR.equals(applicant.type()) && !"2401".equals(param.getOrganizationType())) {
            validateLicensedApplication(param);
        }
        validateStoreUrl(param);
        trimMediaValues(param);
        String payChannel = resolveChannel(param.getPayChannel());
        if (DEFAULT_CHANNEL.equals(payChannel)) {
            validateMediaValues(param);
        }

        // 查询该主体最新一条申请：已进入微信流程或已完成的不允许重复申请；
        // 草稿/待审核/已驳回 可复用同一条记录再次编辑提交。
        MerchantApplication entity = lambdaQuery()
                .eq(MerchantApplication::getApplicantType, applicant.type())
                .eq(MerchantApplication::getApplicantId, applicant.id())
                .orderByDesc(MerchantApplication::getId)
                .last("limit 1")
                .one();
        if (entity != null && IN_PROGRESS_STATUS.contains(entity.getStatus())) {
            throw new GlobalException("已有进行中或已完成的进件申请，无法重复提交");
        }

        if (DEFAULT_CHANNEL.equals(payChannel)) {
            normalizeMediaValues(param, applicant.type(), applicant.id());
        }

        String businessCode = "MA_" + applicant.type() + "_" + applicant.id() + "_" + System.currentTimeMillis();
        MerchantApplicationRequest request = buildRequest(param, businessCode);

        if (entity == null) {
            entity = new MerchantApplication();
            entity.setCreateTime(now());
        }
        entity.setApplicantType(applicant.type());
        entity.setApplicantId(applicant.id());
        entity.setApplicantName(applicant.name());
        entity.setShopId(APPLICANT_SHOP.equals(applicant.type()) ? applicant.id() : 0L);
        entity.setPayChannel(payChannel);
        entity.setBusinessCode(businessCode);
        entity.setOrganizationType(param.getOrganizationType());
        entity.setMerchantName(param.getMerchantName());
        entity.setContactName(param.getContactName());
        entity.setContactPhone(param.getContactPhone());
        entity.setRequestSnapshot(JSONUtil.toJsonStr(request));
        entity.setStatus(MerchantApplicationStatus.PENDING_REVIEW.getCode());
        // 重新提交时清除上一次的驳回原因、审核信息与遗留申请单号
        entity.setRejectReason(null);
        entity.setApplymentId(null);
        entity.setAuditAdminId(null);
        entity.setAuditTime(null);
        entity.setModifyTime(now());

        persistApplication(entity);
        if (DEFAULT_CHANNEL.equals(payChannel)) {
            boolean tracked = merchantApplicationMediaService.bindTempMedia(
                    entity.getId(), applicant.type(), applicant.id(), SecurityUtils.getCurrentAdminId(),
                    param.getLicenseCopy(), param.getIdCardCopy(), param.getIdCardNational());
            if (!tracked) {
                throw new GlobalException("申请证件缺少上传记录，请重新上传");
            }
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplicationVO submit(MerchantApplicationSubmitParam param) {
        // 0 是平台自营这一虚拟门店，它同样需要进件为二级商户才能在服务商模式下收款
        if (param.getShopId() == null || param.getShopId() < 0) {
            throw new GlobalException("请选择店铺");
        }
        validateEnumValues(param);
        if (!"2401".equals(param.getOrganizationType())) {
            validateLicensedApplication(param);
        }
        validateStoreUrl(param);
        trimMediaValues(param);
        String payChannel = resolveChannel(param.getPayChannel());
        if (DEFAULT_CHANNEL.equals(payChannel)) {
            validateMediaValues(param);
        }

        // 查询该店铺非终态的进件申请：草稿允许再次编辑后重新提交（复用原记录），
        // 其它进行中状态（已提交/审核中等）仍拦截，避免重复进件。
        List<MerchantApplication> activeList = lambdaQuery()
                .eq(MerchantApplication::getShopId, param.getShopId())
                .notIn(MerchantApplication::getStatus,
                        MerchantApplicationStatus.FINISH.getCode(), MerchantApplicationStatus.REJECTED.getCode())
                .list();
        MerchantApplication draft = null;
        for (MerchantApplication active : activeList) {
            if (MerchantApplicationStatus.DRAFT.getCode().equals(active.getStatus())) {
                if (draft == null || (active.getId() != null && draft.getId() != null && active.getId() > draft.getId())) {
                    draft = active;
                }
            } else {
                throw new GlobalException("该店铺已有进行中的进件申请");
            }
        }

        if (DEFAULT_CHANNEL.equals(payChannel)) {
            normalizeMediaValues(param, MEDIA_OWNER_PLATFORM, param.getShopId());
        }

        String businessCode = "MA_" + param.getShopId() + "_" + System.currentTimeMillis();
        MerchantApplicationRequest request = buildRequest(param, businessCode);

        String shopName = resolveShopName(param.getShopId() == null ? null : param.getShopId().intValue());

        boolean isResubmit = draft != null;
        MerchantApplication entity = isResubmit ? draft : new MerchantApplication();
        entity.setShopId(param.getShopId());
        entity.setApplicantType(APPLICANT_SHOP);
        entity.setApplicantId(param.getShopId());
        entity.setApplicantName(shopName);
        entity.setPayChannel(payChannel);
        entity.setBusinessCode(businessCode);
        entity.setOrganizationType(param.getOrganizationType());
        entity.setMerchantName(param.getMerchantName());
        entity.setContactName(param.getContactName());
        entity.setContactPhone(param.getContactPhone());
        entity.setRequestSnapshot(JSONUtil.toJsonStr(request));
        // 重新提交时清除上一次的渠道异常原因和遗留申请单号
        entity.setRejectReason(null);
        entity.setApplymentId(null);
        if (!isResubmit) {
            entity.setCreateTime(new Date());
        }
        entity.setModifyTime(now());
        entity.setStatus(MerchantApplicationStatus.DRAFT.getCode());
        persistApplication(entity);

        boolean tracked = DEFAULT_CHANNEL.equals(payChannel)
                && merchantApplicationMediaService.bindTempMedia(
                        entity.getId(), APPLICANT_SHOP, param.getShopId(), SecurityUtils.getCurrentAdminId(),
                        param.getLicenseCopy(), param.getIdCardCopy(), param.getIdCardNational());
        if ("2401".equals(param.getOrganizationType()) && !tracked) {
            throw new GlobalException("申请证件缺少上传记录，请重新上传");
        }
        if (tracked) {
            try {
                List<String> freshMediaIds = merchantApplicationMediaService
                        .reuploadBoundMedia(entity.getId());
                applyReuploadedMedia(request, freshMediaIds);
            } catch (GlobalException exception) {
                if (!isExternalReuploadFailure(exception)) {
                    throw exception;
                }
                merchantApplicationMediaService.extendBoundMediaExpirationInCurrentTransaction(
                        entity.getId());
                log.error("提交进件媒体重传失败, applicationId={}", entity.getId());
                entity.setStatus(MerchantApplicationStatus.DRAFT.getCode());
                entity.setRejectReason("进件媒体重传失败，请稍后重试");
                entity.setModifyTime(now());
                persistApplication(entity);
                return toVO(entity);
            }
        }

        try {
            MerchantApplicationResult result = clientFor(entity).submitApplication(request);
            if (isSuccessfulApplicationResult(result)) {
                entity.setApplymentId(result.getApplymentId());
                MerchantApplicationStatus resultStatus = MerchantApplicationStatus.fromCode(result.getStatus());
                entity.setStatus(resultStatus != null ? resultStatus.getCode() : MerchantApplicationStatus.SUBMITTED.getCode());
            } else {
                entity.setStatus(MerchantApplicationStatus.REJECTED.getCode());
                entity.setRejectReason(result != null ? result.getErrorMessage() : "提交失败");
                if (tracked) {
                    merchantApplicationMediaService.extendBoundMediaExpirationInCurrentTransaction(
                            entity.getId());
                }
            }
        } catch (Exception e) {
            log.error("提交进件失败, applicationId={}", entity.getId());
            entity.setStatus(MerchantApplicationStatus.DRAFT.getCode());
            entity.setRejectReason(buildChannelErrorReason(e));
            if (tracked) {
                merchantApplicationMediaService.extendBoundMediaExpirationInCurrentTransaction(
                        entity.getId());
            }
        }

        entity.setModifyTime(now());
        persistApplication(entity);
        // 提交渠道成功后不再清理证件：审核追溯需要长期回看营业执照与身份证（业务方要求永久保留）
        return toVO(entity);
    }

    @Override
    public MerchantApplicationVO current() {
        return current(null);
    }

    @Override
    public MerchantApplicationVO current(String payChannel) {
        ApplicantContext applicant = resolveApplicant();
        MerchantApplication entity = lambdaQuery()
                .eq(MerchantApplication::getApplicantType, applicant.type())
                .eq(MerchantApplication::getApplicantId, applicant.id())
                .eq(StrUtil.isNotBlank(payChannel), MerchantApplication::getPayChannel, resolveChannel(payChannel))
                .eq(StrUtil.isNotBlank(payChannel), MerchantApplication::getStatus, MerchantApplicationStatus.FINISH.getCode())
                .orderByDesc(MerchantApplication::getId)
                .last("limit 1")
                .one();
        if (entity == null && StrUtil.isNotBlank(payChannel)) {
            entity = lambdaQuery()
                    .eq(MerchantApplication::getApplicantType, applicant.type())
                    .eq(MerchantApplication::getApplicantId, applicant.id())
                    .eq(MerchantApplication::getPayChannel, resolveChannel(payChannel))
                    .orderByDesc(MerchantApplication::getId)
                    .last("limit 1")
                    .one();
        }
        if (entity == null) {
            return null;
        }
        MerchantApplicationVO vo = toVO(entity);
        fillShopNames(List.of(vo));
        return vo;
    }

    @Override
    public MerchantApplicationVO detail(Long id) {
        MerchantApplication entity = findReadable(id);
        if (entity == null) {
            throw new GlobalException("进件申请不存在");
        }
        MerchantApplicationVO vo = toVO(entity);
        fillShopNames(List.of(vo));
        return vo;
    }

    @Override
    public MerchantApplicationMediaService.MediaContent loadMediaContent(Long id, Integer roleCode) {
        MerchantApplication entity = findReadable(id);
        if (entity == null) {
            return null;
        }
        return merchantApplicationMediaService.loadBoundMediaContent(
                entity.getId(), entity.getApplicantType(), entity.getApplicantId(), roleCode);
    }

    /**
     * 按当前登录主体读取申请单：平台账号可读全部；店铺/供应商只能读本主体的申请。
     * 越权与不存在统一返回 {@code null}，避免通过响应差异探测他人申请单是否存在。
     */
    private MerchantApplication findReadable(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        MerchantApplication entity = getById(id);
        if (entity == null || isPlatformAdmin()) {
            return entity;
        }
        ApplicantContext applicant = resolveApplicant();
        boolean owned = applicant.type().equals(entity.getApplicantType())
                && applicant.id().equals(entity.getApplicantId());
        if (!owned) {
            log.warn("进件申请越权读取被拦截, applicationId={}, applicantType={}, applicantId={}",
                    id, applicant.type(), applicant.id());
            return null;
        }
        return entity;
    }

    private boolean isPlatformAdmin() {
        return AdminTypeEnum.ADMIN.getCode().equals(HeaderUtils.getAdminType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        String lockKey = APPROVE_LOCK_PREFIX + id;
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisCache.tryLock(
                lockKey, lockValue, APPROVE_LOCK_SECONDS, TimeUnit.SECONDS);
        if (!locked) {
            throw new GlobalException("该申请正在处理中，请勿重复操作");
        }
        AtomicBoolean unlocked = new AtomicBoolean(false);
        boolean unlockRegistered = registerUnlockAfterCompletion(
                lockKey, lockValue, id, unlocked);
        try {
            MerchantApplication entity = getById(id);
            if (entity == null) {
                throw new GlobalException("进件申请不存在");
            }
            if (!MerchantApplicationStatus.PENDING_REVIEW.getCode().equals(entity.getStatus())) {
                throw new GlobalException("仅待审核状态的申请可以审核通过");
            }
            if (StrUtil.isBlank(entity.getRequestSnapshot())) {
                throw new GlobalException("申请数据缺失，无法提交进件");
            }

            MerchantApplicationRequest request =
                    JSONUtil.toBean(entity.getRequestSnapshot(), MerchantApplicationRequest.class);
            if (StrUtil.isBlank(request.getBusinessCode())) {
                request.setBusinessCode(entity.getBusinessCode());
            }

            // 店铺待审核申请可能来自旧表单；真正提交渠道前仍须执行当前资料规则。
            // 供应商本轮维持原审核流程，不追加主体字段约束。
            if (!APPLICANT_VENDOR.equals(entity.getApplicantType())) {
                MerchantApplicationSubmitParam validated = new MerchantApplicationSubmitParam();
                BeanUtils.copyProperties(request, validated);
                validated.setPayChannel(entity.getPayChannel());
                validateEnumValues(validated);
                if (!"2401".equals(validated.getOrganizationType())) {
                    validateLicensedApplication(validated);
                }
                validateStoreUrl(validated);
                trimMediaValues(validated);
                if (DEFAULT_CHANNEL.equals(resolveChannel(entity.getPayChannel()))) {
                    validateMediaValues(validated);
                }
                request = buildRequest(validated, request.getBusinessCode());
            }

            boolean tracked = DEFAULT_CHANNEL.equals(resolveChannel(entity.getPayChannel()));
            if (tracked) {
                List<String> freshMediaIds;
                try {
                    freshMediaIds = merchantApplicationMediaService.reuploadBoundMedia(id);
                } catch (GlobalException exception) {
                    if (!isExternalReuploadFailure(exception)) {
                        throw exception;
                    }
                    merchantApplicationMediaService.extendBoundMediaExpiration(id);
                    log.error("审核进件媒体重传失败, applicationId={}", id);
                    throw new GlobalException("进件媒体重传失败，请稍后重试");
                }
                applyReuploadedMedia(request, freshMediaIds);
            }

            MerchantApplicationResult result;
            try {
                result = clientFor(entity).submitApplication(request);
            } catch (Exception exception) {
                if (tracked) {
                    merchantApplicationMediaService.extendBoundMediaExpiration(id);
                }
                log.error("审核提交进件失败, applicationId={}, channel={}", id, entity.getPayChannel());
                throw new GlobalException(buildChannelErrorReason(exception));
            }
            if (!isSuccessfulApplicationResult(result)) {
                if (tracked) {
                    merchantApplicationMediaService.extendBoundMediaExpiration(id);
                }
                throw new GlobalException(buildChannelErrorReason(
                        result != null ? result.getErrorMessage() : null));
            }

            entity.setApplymentId(result.getApplymentId());
            MerchantApplicationStatus resultStatus = MerchantApplicationStatus.fromCode(result.getStatus());
            entity.setStatus(resultStatus != null
                    ? resultStatus.getCode() : MerchantApplicationStatus.SUBMITTED.getCode());
            entity.setAuditAdminId(SecurityUtils.getCurrentAdminId());
            entity.setAuditTime(now());
            entity.setRejectReason(null);
            entity.setModifyTime(now());
            if (!updateById(entity)) {
                throw new GlobalException("进件申请审核结果保存失败");
            }
            // 审核通过后不再清理证件：审核追溯需要长期回看营业执照与身份证（业务方要求永久保留）
        } finally {
            if (!unlockRegistered) {
                safeUnlock(lockKey, lockValue, id, unlocked);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String rejectReason) {
        MerchantApplication entity = getById(id);
        if (entity == null) {
            throw new GlobalException("进件申请不存在");
        }
        if (!MerchantApplicationStatus.PENDING_REVIEW.getCode().equals(entity.getStatus())) {
            throw new GlobalException("仅待审核状态的申请可以拒绝");
        }
        if (StrUtil.isBlank(rejectReason)) {
            throw new GlobalException("请填写驳回原因");
        }
        entity.setStatus(MerchantApplicationStatus.REJECTED.getCode());
        entity.setRejectReason(rejectReason);
        entity.setAuditAdminId(SecurityUtils.getCurrentAdminId());
        entity.setAuditTime(new Date());
        entity.setModifyTime(new Date());
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refresh(Long id) {
        MerchantApplication entity = getById(id);
        if (entity == null) {
            throw new GlobalException("进件申请不存在");
        }
        if (isTerminal(entity.getStatus())) {
            throw new GlobalException("该申请已处于终态，无需刷新");
        }
        if (StrUtil.isBlank(entity.getApplymentId())) {
            throw new GlobalException("缺少申请单号，无法查询状态");
        }

        MerchantApplicationResult result = clientFor(entity).queryApplicationStatus(entity.getApplymentId());
        if (result == null || !Boolean.TRUE.equals(result.getSuccess())) {
            log.warn("查询进件状态失败, applymentId={}", entity.getApplymentId());
            return;
        }

        MerchantApplicationStatus newStatus = MerchantApplicationStatus.fromCode(result.getStatus());
        if (newStatus != null) {
            entity.setStatus(newStatus.getCode());
        }
        entity.setSignUrl(result.getSignUrl());
        entity.setRejectReason(result.getRejectReason());
        entity.setModifyTime(new Date());

        if (MerchantApplicationStatus.FINISH.getCode().equals(entity.getStatus())
                && StrUtil.isNotBlank(result.getSubMchId())) {
            entity.setSubMchId(result.getSubMchId());
            createOrUpdateSubMerchant(entity, result.getSubMchId());
        }

        updateById(entity);
    }

    @Override
    public String uploadMedia(MultipartFile file, String payChannel) {
        String channel = resolveChannel(payChannel);
        if (DEFAULT_CHANNEL.equals(channel)) {
            return merchantApplicationMediaService.uploadMedia(file, SecurityUtils.getCurrentAdminId());
        }
        if (file == null || file.isEmpty()) {
            throw new GlobalException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            originalFilename = "upload.jpg";
        }
        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.endsWith(".jpg") && !lowerName.endsWith(".jpeg")
                && !lowerName.endsWith(".png") && !lowerName.endsWith(".bmp")) {
            throw new GlobalException("仅支持JPG/JPEG/PNG/BMP格式的图片");
        }
        String contentType = file.getContentType();
        Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/bmp");
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new GlobalException("不支持的文件类型");
        }
        long maxSize = 5L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new GlobalException("文件大小不能超过5MB");
        }
        try {
            return profitSharingClientRegistry.get(channel)
                    .uploadMedia(file.getBytes(), originalFilename);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传进件媒体文件失败", e);
            throw new GlobalException("上传媒体文件失败: " + e.getMessage());
        }
    }

    /**
     * 解析当前登录主体：供应商优先（vendor），否则店铺/门店（shop）。
     */
    private ApplicantContext resolveApplicant() {
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId >= 0) {
            return new ApplicantContext(APPLICANT_SHOP, shopId.longValue(), resolveShopName(shopId));
        }
        throw new GlobalException("无法识别当前登录主体，请使用店铺或供应商账号申请进件");
    }

    /**
     * 店铺展示名。平台自营是虚拟门店、不在 shop 表，用固定名称兜底，
     * 否则进件列表里自营那行会是空白。
     */
    private String resolveShopName(Integer shopId) {
        if (shopId == null || shopId <= 0) {
            return SELF_OPERATED_SHOP_NAME;
        }
        Shop shop = shopMapper.selectById(shopId);
        return shop != null ? StrUtil.nullToEmpty(shop.getShopTitle()) : "";
    }

    private void createOrUpdateSubMerchant(MerchantApplication application, String subMchId) {
        SubMerchant existing = subMerchantService.lambdaQuery()
                .eq(SubMerchant::getShopId, application.getShopId())
                .eq(SubMerchant::getPayChannel, application.getPayChannel())
                .one();
        Date now = new Date();
        BigDecimal maxRatio = resolveOnboardMaxSplitRatio(application.getPayChannel());
        if (existing != null) {
            existing.setSubMchId(subMchId);
            existing.setMerchantName(application.getMerchantName());
            existing.setStatus(SubMerchantStatus.ACTIVE.getCode());
            existing.setApplicationId(application.getId());
            if (maxRatio != null) {
                existing.setMaxSplitRatio(maxRatio);
            }
            existing.setModifyTime(now);
            subMerchantService.updateById(existing);
        } else {
            SubMerchant subMerchant = new SubMerchant();
            subMerchant.setShopId(application.getShopId());
            subMerchant.setPayChannel(application.getPayChannel());
            subMerchant.setSubMchId(subMchId);
            subMerchant.setMerchantName(application.getMerchantName());
            subMerchant.setStatus(SubMerchantStatus.ACTIVE.getCode());
            subMerchant.setApplicationId(application.getId());
            if (maxRatio != null) {
                subMerchant.setMaxSplitRatio(maxRatio);
            }
            subMerchant.setCreateTime(now);
            subMerchant.setModifyTime(now);
            subMerchantService.save(subMerchant);
        }
    }

    private String resolveChannel(String payChannel) {
        if (CHANNEL_HUIFU.equalsIgnoreCase(payChannel)) {
            return CHANNEL_HUIFU;
        }
        if (CHANNEL_ALIPAY.equalsIgnoreCase(payChannel)) {
            return CHANNEL_ALIPAY;
        }
        return DEFAULT_CHANNEL;
    }

    /**
     * 进件开通最大分账比例：汇付/支付宝按各自配置默认值回写子商户（支付宝 30%、汇付 20%；微信硬上限，返回 null 不回写）。
     */
    private BigDecimal resolveOnboardMaxSplitRatio(String payChannel) {
        SettingsEnum ratioKey;
        if (CHANNEL_HUIFU.equalsIgnoreCase(payChannel)) {
            ratioKey = SettingsEnum.HUIFU_PROFIT_SHARING_MAX_RATIO;
        } else if (CHANNEL_ALIPAY.equalsIgnoreCase(payChannel)) {
            ratioKey = SettingsEnum.ALIPAY_PROFIT_SHARING_MAX_RATIO;
        } else {
            return null;
        }
        String cfg = configService.getConfigVal(ratioKey);
        try {
            if (StrUtil.isNotBlank(cfg)) {
                return new BigDecimal(cfg.trim());
            }
        } catch (Exception ignored) {
            // fall through
        }
        // 支付宝兜底与官方上限一致 30%，汇付兜底 20%
        return CHANNEL_ALIPAY.equalsIgnoreCase(payChannel)
                ? new BigDecimal("30.00")
                : new BigDecimal("20.00");
    }

    private boolean isTerminal(String status) {
        return MerchantApplicationStatus.FINISH.getCode().equals(status)
                || MerchantApplicationStatus.REJECTED.getCode().equals(status);
    }

    private com.tigshop.service.finance.profitsharing.ProfitSharingApiClient clientFor(MerchantApplication application) {
        return profitSharingClientRegistry.get(StrUtil.blankToDefault(application.getPayChannel(), DEFAULT_CHANNEL));
    }

    /**
     * 将渠道提交异常转换为对运营更友好的失败原因文案。
     */
    private String buildChannelErrorReason(Exception e) {
        return buildChannelErrorReason(e != null ? e.getMessage() : null);
    }

    private String buildChannelErrorReason(String msg) {
        if (msg != null && msg.contains(ConfigConstants.CONFIG_NOT_EXIST)) {
            return "微信支付配置未完整设置，请先在『支付设置』中配置商户号、证书序列号、APIv3 密钥等参数后再提交进件";
        }
        if (StrUtil.isNotBlank(msg)) {
            return "微信进件失败：" + msg.trim();
        }
        return "渠道提交异常，请稍后重试或联系管理员";
    }

    /**
     * 原因标题：已驳回状态展示『驳回原因』，其它状态展示『失败原因』，无原因时返回 null。
     */
    private String resolveReasonLabel(String status, String reason) {
        if (StrUtil.isBlank(reason)) {
            return null;
        }
        return MerchantApplicationStatus.REJECTED.getCode().equals(status) ? "驳回原因" : "失败原因";
    }

    private void validateEnumValues(MerchantApplicationSubmitParam param) {
        Set<String> validOrgTypes = Set.of("2", "4", "2401");
        if (StrUtil.isBlank(param.getOrganizationType()) || !validOrgTypes.contains(param.getOrganizationType())) {
            throw new GlobalException("主体类型不合法，允许值: 2(企业), 4(个体工商户), 2401(个人)");
        }
        if (StrUtil.isNotBlank(param.getContactType()) && !Set.of("65", "66").contains(param.getContactType())) {
            throw new GlobalException("超管类型不合法，允许值: 65(经营者/法人), 66(经办人)");
        }
        if (StrUtil.isNotBlank(param.getBankAccountType()) && !Set.of("74", "75").contains(param.getBankAccountType())) {
            throw new GlobalException("银行账户类型不合法，允许值: 74(对公), 75(对私)");
        }
        if (StrUtil.isNotBlank(param.getSettlementId()) && !StrUtil.isNumeric(param.getSettlementId().trim())) {
            throw new GlobalException("结算规则ID必须为数字");
        }
        if ("2401".equals(param.getOrganizationType())) {
            validatePersonalApplication(param);
        }
    }

    /** 企业、个体工商户共用持照主体资料；选填邮箱和客服电话不参与必填判断。 */
    private void validateLicensedApplication(MerchantApplicationSubmitParam param) {
        if ("2".equals(param.getOrganizationType()) && DEFAULT_CHANNEL.equals(resolveChannel(param.getPayChannel()))) {
            if (param.getOwner() == null) {
                throw new GlobalException("请确认法人是否唯一受益人");
            }
            if (!param.getOwner()) {
                throw new GlobalException("当前不支持法人非唯一受益人进件，请先补齐受益人材料");
            }
        }
        if (StrUtil.hasBlank(param.getMerchantName(), param.getMerchantShortName(),
                param.getBusinessLicenseNumber(), param.getLegalPerson())) {
            throw new GlobalException("请填写商户名称、商户简称、营业执照号及法人/经营者姓名");
        }
        if (StrUtil.hasBlank(param.getIdCardName(), param.getIdCardNumber(),
                param.getCardPeriodBegin(), param.getCardPeriodEnd())) {
            throw new GlobalException("请填写法人/经营者身份证姓名、号码和有效期");
        }
        if (!param.getLegalPerson().equals(param.getIdCardName())) {
            throw new GlobalException("身份证姓名必须与法人/经营者姓名一致");
        }
        if (StrUtil.hasBlank(param.getBankAccountType(), param.getBankAccountName(),
                param.getBankAccountNumber(), param.getBankName())) {
            throw new GlobalException("请填写结算账户类型、开户名称、账号和开户银行");
        }
        if ("2".equals(param.getOrganizationType()) && !"74".equals(param.getBankAccountType())) {
            throw new GlobalException("企业主体仅支持对公结算账户");
        }
        String expectedAccountName = "74".equals(param.getBankAccountType())
                ? param.getMerchantName() : param.getIdCardName();
        if (!expectedAccountName.equals(param.getBankAccountName())) {
            throw new GlobalException("74".equals(param.getBankAccountType())
                    ? "对公账户名称必须与营业执照名称一致" : "对私账户开户名必须与经营者身份证姓名一致");
        }
        if (StrUtil.hasBlank(param.getContactType(), param.getContactName(),
                param.getContactIdNumber(), param.getContactPhone())) {
            throw new GlobalException("请填写超级管理员类型、姓名、身份证号和手机号");
        }
        if ("66".equals(param.getContactType()) && DEFAULT_CHANNEL.equals(resolveChannel(param.getPayChannel()))) {
            throw new GlobalException("暂不支持经办人进件，请选择法人/经营者作为超级管理员");
        }
        if ("65".equals(param.getContactType())
                && (!param.getIdCardName().equals(param.getContactName())
                || !param.getIdCardNumber().equals(param.getContactIdNumber()))) {
            throw new GlobalException("超级管理员姓名和身份证号必须与法人/经营者一致");
        }
    }

    private void validatePersonalApplication(MerchantApplicationSubmitParam param) {
        if (!DEFAULT_CHANNEL.equals(resolveChannel(param.getPayChannel()))) {
            throw new GlobalException("个人主体目前仅支持微信进件");
        }
        if (StrUtil.hasBlank(param.getIdCardName(), param.getIdCardNumber(),
                param.getCardPeriodBegin(), param.getCardPeriodEnd())) {
            throw new GlobalException("请填写个人身份证姓名、号码和有效期");
        }
        if (StrUtil.hasBlank(param.getMerchantName(), param.getMerchantShortName(),
                param.getContactPhone(), param.getBankAccountNumber(), param.getBankName())) {
            throw new GlobalException("请填写店铺名称、商户简称、手机号和结算银行卡信息");
        }
        if (!"75".equals(param.getBankAccountType())) {
            throw new GlobalException("个人主体仅支持经营者本人对私银行卡");
        }
        if (!param.getIdCardName().equals(param.getBankAccountName())) {
            throw new GlobalException("个人结算账户开户名必须与身份证姓名一致");
        }
        if (!"65".equals(param.getContactType())
                || !param.getIdCardName().equals(param.getContactName())
                || !param.getIdCardNumber().equals(param.getContactIdNumber())) {
            throw new GlobalException("个人主体的超级管理员必须为经营者本人");
        }
        // 切换主体时丢弃旧执照资料，个人申请快照与渠道请求均不保留营业执照。
        param.setBusinessLicenseNumber(null);
        param.setLegalPerson(null);
        param.setLicensePeriodBegin(null);
        param.setLicensePeriodEnd(null);
        param.setLicenseCopy(null);
    }

    private void validateStoreUrl(MerchantApplicationSubmitParam param) {
        String storeUrl = StrUtil.nullToEmpty(param.getStoreUrl()).trim();
        if (storeUrl.isBlank()) {
            throw new GlobalException("请填写店铺链接");
        }
        if (storeUrl.length() > 1024) {
            throw new GlobalException("店铺链接长度不能超过1024个字符");
        }
        try {
            URI uri = new URI(storeUrl);
            if (!("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    || StrUtil.isBlank(uri.getHost())) {
                throw new GlobalException("店铺链接必须为有效的HTTP或HTTPS地址");
            }
        } catch (URISyntaxException exception) {
            throw new GlobalException("店铺链接必须为有效的HTTP或HTTPS地址");
        }
        param.setStoreUrl(storeUrl);
    }

    private void validateMediaValues(MerchantApplicationSubmitParam param) {
        List<String> allMediaIds = List.of(
                StrUtil.nullToEmpty(param.getLicenseCopy()).trim(),
                StrUtil.nullToEmpty(param.getIdCardCopy()).trim(),
                StrUtil.nullToEmpty(param.getIdCardNational()).trim());
        boolean personal = "2401".equals(param.getOrganizationType());
        List<String> mediaIds = personal ? allMediaIds.subList(1, 3) : allMediaIds;
        if (mediaIds.stream().anyMatch(String::isBlank)) {
            throw new GlobalException(personal ? "身份证正反面均需上传" : "营业执照和身份证正反面均需上传");
        }
        if (mediaIds.stream().anyMatch(this::hasUnsupportedUriScheme)) {
            throw new GlobalException("进件媒体地址不受支持");
        }
        if (mediaIds.stream().distinct().count() != mediaIds.size()) {
            throw new GlobalException("进件证件不可重复");
        }
    }

    private void trimMediaValues(MerchantApplicationSubmitParam param) {
        param.setLicenseCopy(StrUtil.nullToEmpty(param.getLicenseCopy()).trim());
        param.setIdCardCopy(StrUtil.nullToEmpty(param.getIdCardCopy()).trim());
        param.setIdCardNational(StrUtil.nullToEmpty(param.getIdCardNational()).trim());
    }

    private void normalizeMediaValues(
            MerchantApplicationSubmitParam param,
            String applicantType,
            Long applicantId) {
        List<String> originalValues = List.of(
                param.getLicenseCopy(), param.getIdCardCopy(), param.getIdCardNational());
        if (originalValues.stream().noneMatch(this::isHttpUrl)) {
            return;
        }
        List<String> normalized = merchantApplicationMediaService.normalizeSubmissionMedia(
                applicantType, applicantId, SecurityUtils.getCurrentAdminId(),
                originalValues.get(0), originalValues.get(1), originalValues.get(2));
        int requiredStart = "2401".equals(param.getOrganizationType()) ? 1 : 0;
        if (normalized == null || normalized.size() != 3) {
            throw new GlobalException("进件媒体归一化失败，请重新上传");
        }
        List<String> required = normalized.subList(requiredStart, 3);
        if (required.stream().anyMatch(StrUtil::isBlank)
                || required.stream().anyMatch(this::isHttpUrl)
                || required.stream().distinct().count() != required.size()) {
            throw new GlobalException("进件媒体归一化失败，请重新上传");
        }
        param.setLicenseCopy(normalized.get(0));
        param.setIdCardCopy(normalized.get(1));
        param.setIdCardNational(normalized.get(2));
    }

    private boolean isHttpUrl(String value) {
        String normalized = StrUtil.nullToEmpty(value).toLowerCase(Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private boolean hasUnsupportedUriScheme(String value) {
        String normalized = StrUtil.nullToEmpty(value).toLowerCase(Locale.ROOT);
        int separatorIndex = normalized.indexOf("://");
        return separatorIndex > 0 && !isHttpUrl(normalized);
    }

    private Date now() {
        return Date.from(clock.instant());
    }

    private void applyReuploadedMedia(MerchantApplicationRequest request, List<String> mediaIds) {
        boolean personal = "2401".equals(request.getOrganizationType());
        int expectedSize = personal ? 2 : 3;
        if (mediaIds == null || mediaIds.size() != expectedSize
                || mediaIds.stream().anyMatch(StrUtil::isBlank)) {
            throw new GlobalException("进件证件重传结果不完整，请重新上传后再提交");
        }
        request.setLicenseCopy(personal ? null : mediaIds.get(0));
        request.setIdCardCopy(mediaIds.get(expectedSize - 2));
        request.setIdCardNational(mediaIds.get(expectedSize - 1));
    }

    private boolean isExternalReuploadFailure(GlobalException exception) {
        return exception instanceof MerchantApplicationMediaService.ReuploadFailedException;
    }

    private boolean isSuccessfulApplicationResult(MerchantApplicationResult result) {
        return result != null
                && Boolean.TRUE.equals(result.getSuccess())
                && StrUtil.isNotBlank(result.getApplymentId());
    }

    private boolean registerUnlockAfterCompletion(
            String lockKey,
            String lockValue,
            Long applicationId,
            AtomicBoolean unlocked) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    safeUnlock(lockKey, lockValue, applicationId, unlocked);
                }
            });
            return true;
        } catch (Exception exception) {
            log.error("进件申请审核锁事务回调注册失败, applicationId={}", applicationId);
            return false;
        }
    }

    private void safeUnlock(
            String lockKey,
            String lockValue,
            Long applicationId,
            AtomicBoolean unlocked) {
        if (!unlocked.compareAndSet(false, true)) {
            return;
        }
        try {
            redisCache.unlock(lockKey, lockValue);
        } catch (Exception exception) {
            log.error("进件申请审核锁释放失败, applicationId={}", applicationId);
        }
    }

    private void persistApplication(MerchantApplication entity) {
        int affectedRows = entity.getId() == null
                ? getBaseMapper().insert(entity)
                : getBaseMapper().updateById(entity);
        if (affectedRows != 1 || entity.getId() == null) {
            throw new GlobalException("进件申请保存失败");
        }
    }

    private MerchantApplicationRequest buildRequest(MerchantApplicationSubmitParam param, String businessCode) {
        MerchantApplicationRequest request = new MerchantApplicationRequest();
        request.setBusinessCode(businessCode);
        request.setOrganizationType(param.getOrganizationType());
        request.setOwner(param.getOwner());
        request.setContactName(param.getContactName());
        request.setContactIdNumber(param.getContactIdNumber());
        request.setContactPhone(param.getContactPhone());
        request.setContactEmail(param.getContactEmail());
        request.setContactType(param.getContactType());
        request.setMerchantName(param.getMerchantName());
        request.setMerchantShortName(param.getMerchantShortName());
        request.setStoreUrl(param.getStoreUrl());
        request.setBusinessLicenseNumber(param.getBusinessLicenseNumber());
        request.setLegalPerson(param.getLegalPerson());
        request.setLicenseCopy(param.getLicenseCopy());
        request.setLicensePeriodBegin(param.getLicensePeriodBegin());
        request.setLicensePeriodEnd(param.getLicensePeriodEnd());
        request.setIdCardCopy(param.getIdCardCopy());
        request.setIdCardNational(param.getIdCardNational());
        request.setIdCardName(param.getIdCardName());
        request.setIdCardNumber(param.getIdCardNumber());
        request.setCardPeriodBegin(param.getCardPeriodBegin());
        request.setCardPeriodEnd(param.getCardPeriodEnd());
        request.setBankAccountName(param.getBankAccountName());
        request.setBankAccountNumber(param.getBankAccountNumber());
        request.setBankName(param.getBankName());
        request.setBankBranchName(param.getBankBranchName());
        request.setBankAccountType(param.getBankAccountType());
        request.setSettlementId(param.getSettlementId());
        request.setQualificationType(param.getQualificationType());
        request.setServicePhone(param.getServicePhone());
        return request;
    }

    private void fillShopNames(List<MerchantApplicationVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Integer> shopIds = voList.stream()
                .filter(vo -> APPLICANT_SHOP.equals(vo.getApplicantType()) || vo.getApplicantType() == null)
                .map(MerchantApplicationVO::getShopId)
                .filter(id -> id != null && id > 0)
                .map(Long::intValue)
                .distinct()
                .collect(Collectors.toList());
        // 不能因 shopIds 为空提前返回：整页只有平台自营（shopId=0）时仍需回填展示名
        Map<Integer, String> shopNameMap = shopIds.isEmpty()
                ? Map.of()
                : shopMapper.selectBatchIds(shopIds).stream()
                        .collect(Collectors.toMap(Shop::getShopId, s -> StrUtil.nullToEmpty(s.getShopTitle()), (a, b) -> a));
        voList.forEach(vo -> {
            if (APPLICANT_SHOP.equals(vo.getApplicantType()) || vo.getApplicantType() == null) {
                if (vo.getShopId() == null) {
                    vo.setShopName("");
                } else if (vo.getShopId() == 0L) {
                    vo.setShopName(SELF_OPERATED_SHOP_NAME);
                } else {
                    vo.setShopName(shopNameMap.getOrDefault(vo.getShopId().intValue(), ""));
                }
            } else {
                vo.setShopName(StrUtil.nullToEmpty(vo.getApplicantName()));
            }
        });
    }

    private MerchantApplicationVO toVO(MerchantApplication entity) {
        MerchantApplicationVO vo = new MerchantApplicationVO();
        vo.setId(entity.getId());
        vo.setShopId(entity.getShopId());
        vo.setApplicantType(entity.getApplicantType());
        vo.setApplicantId(entity.getApplicantId());
        vo.setApplicantName(entity.getApplicantName());
        vo.setPayChannel(entity.getPayChannel());
        vo.setBusinessCode(entity.getBusinessCode());
        vo.setApplymentId(entity.getApplymentId());
        vo.setStatus(entity.getStatus());
        MerchantApplicationStatus status = MerchantApplicationStatus.fromCode(entity.getStatus());
        vo.setStatusDesc(status != null ? status.getDesc() : null);
        vo.setRejectReason(entity.getRejectReason());
        vo.setReasonLabel(resolveReasonLabel(entity.getStatus(), entity.getRejectReason()));
        vo.setAuditTime(entity.getAuditTime());
        vo.setSubMchId(entity.getSubMchId());
        vo.setSignUrl(entity.getSignUrl());
        vo.setMerchantName(entity.getMerchantName());
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setOrganizationType(entity.getOrganizationType());
        vo.setCreateTime(entity.getCreateTime());
        vo.setModifyTime(entity.getModifyTime());

        vo.setRequestSnapshot(entity.getRequestSnapshot());
        if (StrUtil.isNotBlank(entity.getRequestSnapshot())) {
            try {
                MerchantApplicationRequest snapshot =
                        JSONUtil.toBean(entity.getRequestSnapshot(), MerchantApplicationRequest.class);
                if (snapshot != null) {
                    vo.setBankAccountType(snapshot.getBankAccountType());
                    vo.setBankAccountName(snapshot.getBankAccountName());
                    vo.setBankAccountNumber(snapshot.getBankAccountNumber());
                    vo.setBankName(snapshot.getBankName());
                    vo.setBankBranchName(snapshot.getBankBranchName());
                    vo.setBusinessLicenseNumber(snapshot.getBusinessLicenseNumber());
                    vo.setLegalPerson(snapshot.getLegalPerson());
                    vo.setMerchantShortName(snapshot.getMerchantShortName());
                    vo.setStoreUrl(snapshot.getStoreUrl());
                }
            } catch (Exception e) {
                log.warn("解析 requestSnapshot 失败, applicationId={}", entity.getId(), e);
            }
        }
        return vo;
    }

    /**
     * 申请主体上下文
     */
    private record ApplicantContext(String type, Long id, String name) {
    }
}
