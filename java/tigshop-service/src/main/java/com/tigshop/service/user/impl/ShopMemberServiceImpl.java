package com.tigshop.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.user.ShopMemberImportExcelDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.user.UserTypeEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.ShopMember;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.user.ShopMemberAddParam;
import com.tigshop.bean.param.user.ShopMemberQueryPage;
import com.tigshop.bean.param.user.ShopMemberShopQueryPage;
import com.tigshop.bean.vo.user.ShopMemberImportErrorExcelVO;
import com.tigshop.bean.vo.user.ShopMemberListVO;
import com.tigshop.bean.vo.user.ShopMemberShopListVO;
import com.tigshop.bean.vo.user.UserImportSuccessListVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.user.ShopMemberMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.ShopMemberService;
import com.tigshop.service.user.ShopMemberSettingService;
import com.tigshop.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺会员绑定实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopMemberServiceImpl extends BaseServiceImpl<ShopMemberMapper, ShopMember>
        implements ShopMemberService {

    private final ShopMemberSettingService shopMemberSettingService;
    private final UserService userService;
    private final ShopService shopService;
    private final AdminUserService adminUserService;

    /** 当前上下文店铺ID：平台=0，店铺/门店=自身 */
    private int currentShopId() {
        Integer s = HeaderUtils.getShopId();
        return s == null ? 0 : s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordOnPaid(Order order) {
        if (order == null) {
            return;
        }
        // 下单记账不受平台「店铺会员总开关」控制：无论 shopMemberEnabled 是否开启，
        // 用户下单支付后都记入店铺会员表（总开关仅约束店铺端会员功能的展示/写操作）。
        Integer shopId = order.getShopId();
        Integer userId = order.getUserId();
        // 收银台(POS)订单常卖平台自营商品(order.shopId=0)，此时改用「收银台所属门店」归属会员，
        // 使收银台下单顾客也能进入门店会员列表。门店 shopId 由订单 posUserId 关联的管理员解析
        // （记账在事务提交后的异步线程执行，无法读取请求头，只能用订单上的 posUserId）。
        if ((shopId == null || shopId <= 0) && AdminTypeEnum.POS.getCode().equals(order.getOrderSource())) {
            shopId = resolvePosShopId(order);
        }
        // 平台自营(shopId=0)不生成店铺会员；无效用户跳过
        if (shopId == null || shopId <= 0 || userId == null || userId <= 0) {
            return;
        }
        BigDecimal amount = order.getPaidAmount() != null ? order.getPaidAmount()
                : (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        Long now = StringUtils.getCurrentTime();

        // 已存在 → 原子累加
        if (accumulate(shopId, userId, amount, now)) {
            return;
        }
        // 不存在 → 插入；并发下唯一键冲突则回退为原子累加
        try {
            ShopMember member = new ShopMember();
            member.setUserId(userId);
            member.setShopId(shopId);
            member.setOrderCount(1);
            member.setOrderAmount(amount);
            member.setJoinTime(now);
            member.setLastConsumeTime(now);
            member.setCreateTime(now);
            member.setUpdateTime(now);
            member.setIsDel(0);
            this.save(member);
        } catch (Exception e) {
            log.warn("[shop-member] insert conflict, fallback to accumulate. shopId={}, userId={}: {}",
                    shopId, userId, e.getMessage());
            accumulate(shopId, userId, amount, now);
        }
    }

    /**
     * 收银台订单回退归属：用订单操作管理员(posUserId)所属门店 shopId 作为会员归属店铺。
     *
     * @return 门店 shopId；posUserId 无效或管理员/门店缺失时返回 null（调用方将跳过记账）
     */
    private Integer resolvePosShopId(Order order) {
        Integer posUserId = order.getPosUserId();
        if (posUserId == null || posUserId <= 0) {
            return null;
        }
        AdminUser admin = adminUserService.getById(posUserId);
        return admin == null ? null : admin.getShopId();
    }

    /**
     * 原子累加：order_count+1、order_amount+amount、更新最近消费时间。
     *
     * @return true 表示命中已存在记录并更新成功
     */
    private boolean accumulate(Integer shopId, Integer userId, BigDecimal amount, Long now) {
        return this.lambdaUpdate()
                .setSql("order_count = order_count + 1")
                .setSql("order_amount = order_amount + " + amount.toPlainString())
                .set(ShopMember::getLastConsumeTime, now)
                .set(ShopMember::getUpdateTime, now)
                .eq(ShopMember::getShopId, shopId)
                .eq(ShopMember::getUserId, userId)
                .update();
    }

    // ==================== 会员列表 / 详情 ====================

    @Override
    public Page<ShopMemberListVO> memberList(ShopMemberQueryPage query) {
        int contextShopId = currentShopId();
        // 越权防线：店铺/门店端(contextShopId>0)强制按登录店铺隔离，忽略入参 shopId；
        // 仅平台端(contextShopId==0)才允许通过入参显式查看指定店铺。
        Integer effectiveShopId = contextShopId > 0
                ? contextShopId
                : (query.getShopId() != null ? query.getShopId() : contextShopId);

        Page<ShopMember> page = new Page<>(query.getPage(), query.getSize());
        MPJLambdaWrapper<ShopMember> qw = new MPJLambdaWrapper<>();
        qw.selectAll(ShopMember.class);
        qw.leftJoin(User.class, User::getUserId, ShopMember::getUserId);
        Integer userType = query.getUserType() == null
                ? UserTypeEnum.MEMBER.getCode() : query.getUserType();
        qw.eq(User::getUserType, userType);
        qw.eq(ShopMember::getShopId, effectiveShopId);
        qw.eq(query.getUserId() != null, ShopMember::getUserId, query.getUserId());
        qw.and(StrUtil.isNotEmpty(query.getKeyword()), w -> w
                .like(User::getNickname, query.getKeyword())
                .or().like(User::getMobile, query.getKeyword())
                .or().like(User::getEmail, query.getKeyword()));
        qw.ge(query.getOrderAmountMin() != null, ShopMember::getOrderAmount, query.getOrderAmountMin());
        qw.le(query.getOrderAmountMax() != null, ShopMember::getOrderAmount, query.getOrderAmountMax());
        qw.ge(query.getJoinTimeStart() != null, ShopMember::getJoinTime, query.getJoinTimeStart());
        qw.le(query.getJoinTimeEnd() != null, ShopMember::getJoinTime, query.getJoinTimeEnd());
        qw.ge(query.getLastConsumeTimeStart() != null, ShopMember::getLastConsumeTime, query.getLastConsumeTimeStart());
        qw.le(query.getLastConsumeTimeEnd() != null, ShopMember::getLastConsumeTime, query.getLastConsumeTimeEnd());
        qw.orderByDesc(ShopMember::getJoinTime);

        Page<ShopMember> result = this.page(page, qw);
        Page<ShopMemberListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ShopMember> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        List<Integer> userIds = records.stream().map(ShopMember::getUserId).distinct().toList();
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u, (a, b) -> a));

        boolean maskData = shouldMask(contextShopId);
        voPage.setRecords(records.stream()
                .map(m -> toVO(m, userMap.get(m.getUserId()), maskData))
                .toList());
        return voPage;
    }

    @Override
    public ShopMemberListVO memberDetail(Integer shopId, Integer userId) {
        int contextShopId = currentShopId();
        // 越权防线：店铺/门店端强制按登录店铺隔离，忽略入参 shopId；仅平台端允许显式指定。
        Integer effectiveShopId = contextShopId > 0
                ? contextShopId
                : (shopId != null ? shopId : contextShopId);
        ShopMember member = this.lambdaQuery()
                .eq(ShopMember::getShopId, effectiveShopId)
                .eq(ShopMember::getUserId, userId)
                .one();
        if (member == null) {
            throw new GlobalException("会员不存在");
        }
        User user = userService.getById(userId);
        return toVO(member, user, shouldMask(contextShopId));
    }

    /**
     * 是否对店铺端客户电话/姓名脱敏（D-B）。
     * 仅店铺端（contextShopId>0）生效：冻结态（总开关关）强制脱敏；
     * 正常态由平台「店铺客户信息脱敏」开关控制。平台端不脱敏。
     */
    private boolean shouldMask(int contextShopId) {
        if (contextShopId <= 0) {
            return false;
        }
        if (!shopMemberSettingService.isEnabled()) {
            return true; // 冻结态强制脱敏（安全防线）
        }
        return shopMemberSettingService.isDesensitizeEnabled();
    }

    private ShopMemberListVO toVO(ShopMember m, User u, boolean maskData) {
        ShopMemberListVO vo = new ShopMemberListVO();
        vo.setUserId(m.getUserId());
        vo.setOrderCount(m.getOrderCount());
        vo.setOrderAmount(m.getOrderAmount());
        vo.setJoinTime(m.getJoinTime());
        vo.setLastConsumeTime(m.getLastConsumeTime());
        if (u != null) {
            String nickname = StrUtil.isNotEmpty(u.getNickname()) ? u.getNickname() : u.getUsername();
            vo.setNickname(maskData ? maskName(nickname) : nickname);
            vo.setMobile(maskData ? maskMobile(u.getMobile()) : u.getMobile());
            // 空头像走与平台端一致的默认头像兜底（DEFAULT_AVATAR 配置），
            // 覆盖新建/导入及历史已入库会员，避免店铺端显示占位空白
            vo.setAvatar(userService.getUserAvatar(u.getAvatar()));
        }
        return vo;
    }

    private static String maskMobile(String mobile) {
        if (StrUtil.isEmpty(mobile)) {
            return mobile;
        }
        return DesensitizedUtil.mobilePhone(mobile);
    }

    private static String maskName(String name) {
        if (StrUtil.isEmpty(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name + "*";
        }
        return name.charAt(0) + "**";
    }

    // ==================== 平台端店铺列表 ====================

    @Override
    public Page<ShopMemberShopListVO> shopList(ShopMemberShopQueryPage query) {
        Page<Shop> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Shop> w = new LambdaQueryWrapper<>();
        w.like(StrUtil.isNotEmpty(query.getShopTitle()), Shop::getShopTitle, query.getShopTitle());
        w.eq(query.getShopType() != null, Shop::getShopType, query.getShopType());
        w.orderByDesc(Shop::getShopId);
        Page<Shop> shopPage = shopService.page(page, w);

        Page<ShopMemberShopListVO> voPage = new Page<>(shopPage.getCurrent(), shopPage.getSize(), shopPage.getTotal());
        List<Shop> shops = shopPage.getRecords();
        if (shops == null || shops.isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        List<Integer> shopIds = shops.stream().map(Shop::getShopId).toList();
        // GROUP BY 聚合计数，避免全量拉取会员行到内存（@TableLogic 自动带 is_del=0）。
        QueryWrapper<ShopMember> countQw = new QueryWrapper<>();
        countQw.select("shop_id AS shopId", "COUNT(*) AS cnt");
        countQw.in("shop_id", shopIds);
        countQw.apply("user_id IN (SELECT user_id FROM user WHERE user_type = {0})",
                UserTypeEnum.MEMBER.getCode());
        countQw.groupBy("shop_id");
        Map<Integer, Long> memberCountMap = this.listMaps(countQw).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("shopId")).intValue(),
                        row -> ((Number) row.get("cnt")).longValue(),
                        (a, b) -> a));

        voPage.setRecords(shops.stream().map(s -> {
            ShopMemberShopListVO vo = new ShopMemberShopListVO();
            vo.setShopId(s.getShopId());
            vo.setShopTitle(s.getShopTitle());
            vo.setShopLogo(s.getShopLogo());
            vo.setStatus(s.getStatus());
            vo.setShopType(s.getShopType());
            vo.setMemberCount(memberCountMap.getOrDefault(s.getShopId(), 0L));
            return vo;
        }).toList());
        return voPage;
    }

    // ==================== 店铺端 添加 / 导入 客户（CR-20260708-1530）====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addMember(ShopMemberAddParam param) {
        shopMemberSettingService.assertEnabledForShop();
        shopMemberSettingService.assertAddEnabled();
        int shopId = currentShopId();
        if (shopId <= 0) {
            throw new GlobalException("仅店铺端可新增客户");
        }
        String mobile = param.getMobile() == null ? null : param.getMobile().trim();
        if (StrUtil.isEmpty(mobile)) {
            throw new GlobalException("手机号不能为空");
        }
        User user = userService.getUserByMobile(mobile);
        if (user == null) {
            throw new GlobalException("找不到手机号为：" + mobile + " 的会员");
        }
        boolean bound = bindMember(shopId, user.getUserId());
        if (!bound) {
            throw new GlobalException("该客户已是本店会员");
        }
        return user.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = GlobalException.class)
    public List<UserImportSuccessListVO> importMembers(MultipartFile file) {
        shopMemberSettingService.assertEnabledForShop();
        shopMemberSettingService.assertImportEnabled();
        int shopId = currentShopId();
        if (shopId <= 0) {
            throw new GlobalException("仅店铺端可导入客户");
        }
        if (file == null) {
            throw new GlobalException("请上传文件");
        }
        List<ShopMemberImportExcelDTO> rows = new ExcelUtils<ShopMemberImportExcelDTO>(file, ShopMemberImportExcelDTO.class).importExcel();
        if (CollUtil.isEmpty(rows)) {
            throw new GlobalException("表格数据为空");
        }
        assertImportRowLimit(rows.size());
        // 一次性批量查用户，避免逐行 getUserByMobile 造成 N+1 查询
        Map<String, User> userByMobile = loadUsersByRows(rows);
        // 逐行校验：把校验失败的行剔除并记录，剩余通过行进入导入（不立即报错）
        List<ShopMemberImportErrorExcelVO> errors = new ArrayList<>();
        List<ShopMemberImportExcelDTO> validRows = new ArrayList<>();
        for (ShopMemberImportExcelDTO row : rows) {
            String err = validateImportRow(row, userByMobile);
            if (err != null) {
                ShopMemberImportErrorExcelVO e = new ShopMemberImportErrorExcelVO();
                BeanUtils.copyProperties(row, e);
                e.setErrorMsg(err);
                errors.add(e);
            } else {
                validRows.add(row);
            }
        }
        // 校验成功的行正常导入（复用已有会员，建立本店会员绑定；已是本店会员则跳过，不清零累计数据）
        List<UserImportSuccessListVO> success = new ArrayList<>();
        for (ShopMemberImportExcelDTO row : validRows) {
            String mobile = row.getMobile().trim();
            User user = userByMobile.get(mobile);
            bindMember(shopId, user.getUserId());
            UserImportSuccessListVO vo = new UserImportSuccessListVO();
            vo.setUserId(user.getUserId());
            vo.setUsername(user.getUsername());
            vo.setMobile(user.getMobile());
            vo.setEmail(user.getEmail());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            success.add(vo);
        }
        // 存在校验失败行：成功部分已入库（noRollbackFor 保证不回滚），按之前格式抛出异常告知失败情况
        if (CollUtil.isNotEmpty(errors)) {
            throw new GlobalException("导入失败:共导入" + rows.size() + "个,失败" + errors.size() + "个");
        }
        return success;
    }

    @Override
    public void getImportErrorData(MultipartFile file, HttpServletResponse response) {
        if (file == null) {
            throw new GlobalException("请上传文件");
        }
        List<ShopMemberImportExcelDTO> rows = new ExcelUtils<ShopMemberImportExcelDTO>(file, ShopMemberImportExcelDTO.class).importExcel();
        if (CollUtil.isEmpty(rows)) {
            throw new GlobalException("表格数据为空");
        }
        assertImportRowLimit(rows.size());
        List<ShopMemberImportErrorExcelVO> errors = validateImportRows(rows);
        new ExcelUtils<>(errors, ShopMemberImportErrorExcelVO.class).exportExcelWeb(response);
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        new ExcelUtils<>(Collections.<ShopMemberImportExcelDTO>emptyList(), ShopMemberImportExcelDTO.class)
                .exportExcelWeb(response);
    }

    /**
     * 绑定会员（0 累计数据的入会），已是本店会员则跳过。
     *
     * @return true=新建绑定；false=已是本店会员
     */
    private boolean bindMember(Integer shopId, Integer userId) {
        Long cnt = this.lambdaQuery()
                .eq(ShopMember::getShopId, shopId)
                .eq(ShopMember::getUserId, userId)
                .count();
        if (cnt != null && cnt > 0) {
            return false;
        }
        Long now = StringUtils.getCurrentTime();
        ShopMember m = new ShopMember();
        m.setUserId(userId);
        m.setShopId(shopId);
        m.setOrderCount(0);
        m.setOrderAmount(BigDecimal.ZERO);
        m.setJoinTime(now);
        m.setLastConsumeTime(null);
        m.setCreateTime(now);
        m.setUpdateTime(now);
        m.setIsDel(0);
        try {
            this.save(m);
            return true;
        } catch (Exception e) {
            log.warn("[shop-member] bind conflict. shopId={}, userId={}: {}", shopId, userId, e.getMessage());
            return false;
        }
    }

    /** 校验导入行：手机号必填 + 数字格式。错误行拷贝原字段 + errorMsg。 */
    private List<ShopMemberImportErrorExcelVO> validateImportRows(List<ShopMemberImportExcelDTO> rows) {
        Map<String, User> userByMobile = loadUsersByRows(rows);
        List<ShopMemberImportErrorExcelVO> errors = new ArrayList<>();
        for (ShopMemberImportExcelDTO row : rows) {
            String err = validateImportRow(row, userByMobile);
            if (err != null) {
                ShopMemberImportErrorExcelVO e = new ShopMemberImportErrorExcelVO();
                BeanUtils.copyProperties(row, e);
                e.setErrorMsg(err);
                errors.add(e);
            }
        }
        return errors;
    }

    /** 校验单行：手机号必填 + 数字格式 + 命中已有会员。通过返回 null，否则返回错误原因。 */
    private String validateImportRow(ShopMemberImportExcelDTO row, Map<String, User> userByMobile) {
        String mobile = row.getMobile() == null ? null : row.getMobile().trim();
        if (StrUtil.isEmpty(mobile)) {
            return "手机号不能为空";
        }
        if (!mobile.matches("^[1-9]\\d*$")) {
            return "手机号格式不正确";
        }
        if (userByMobile.get(mobile) == null) {
            return "找不到手机号为：" + mobile + " 的会员";
        }
        return null;
    }

    /** 导入行数上限，防止超大文件放大为内存/查询压力（DoS）。 */
    private static final int IMPORT_MAX_ROWS = 1000;

    private void assertImportRowLimit(int rowCount) {
        if (rowCount > IMPORT_MAX_ROWS) {
            throw new GlobalException("单次导入不能超过 " + IMPORT_MAX_ROWS + " 行，请拆分后重试");
        }
    }

    /** 从导入行提取去重后的合法手机号，一次性批量查用户，避免逐行 N+1。 */
    private Map<String, User> loadUsersByRows(List<ShopMemberImportExcelDTO> rows) {
        List<String> mobiles = rows.stream()
                .map(r -> r.getMobile() == null ? null : r.getMobile().trim())
                .filter(StrUtil::isNotEmpty)
                .distinct()
                .toList();
        return userService.getUsersByMobiles(mobiles);
    }
}
