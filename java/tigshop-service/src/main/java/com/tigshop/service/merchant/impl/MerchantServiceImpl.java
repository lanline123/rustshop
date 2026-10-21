// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.merchant.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.internal.bind.DefaultDateTypeAdapter;
import com.tigshop.bean.dto.merchant.MerchantAdminDataDTO;
import com.tigshop.bean.dto.merchant.MerchantCreateDTO;
import com.tigshop.bean.enums.common.DateTypeEnum;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.dto.merchant.MerchantUpdateDTO;
import com.tigshop.bean.enums.merchant.MerchantStatus;
import com.tigshop.bean.enums.merchant.MerchantApplyStatusEnum;
import com.tigshop.bean.model.merchant.MerchantApply;
import com.tigshop.bean.enums.merchant.MerchantType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.ImageWatermarkUtil;
import com.tigshop.common.utils.MultipartFileUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.merchant.MerchantApplyMapper;
import com.tigshop.mapper.merchant.MerchantUserMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.shop.ShopFundsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.authority.AdminUserConstants.PASSWORD;
import static com.tigshop.common.constant.merchant.MerchantConstants.MERCHANT_USER_NOT_EXIST;
import static com.tigshop.common.constant.merchant.MerchantConstants.MERCHANT_USER_NOT_FOUND;
import static com.tigshop.common.enums.DataType.ARTICLE_CONTENT_DESC;

/**
 * 商家表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class MerchantServiceImpl extends BaseServiceImpl<MerchantMapper, Merchant> implements MerchantService {
    /** 文案与内置字体子集绑定，修改文案须同步更新字体。 */
    private static final String LICENSE_WATERMARK_TEXT = "营业执照信息公示专用";
    private static final long LICENSE_PIC_MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> LICENSE_PIC_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/bmp");

    @Resource
    private GalleryPicService galleryPicService;
    @Resource
    private MerchantApplyMapper merchantApplyMapper;

    @Override
    public String uploadLicensePic(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException("营业执照图片不能为空");
        }
        if (file.getSize() > LICENSE_PIC_MAX_SIZE) {
            throw new GlobalException("图片大小超过 10MB 限制");
        }
        String contentType = file.getContentType();
        if (contentType == null || !LICENSE_PIC_CONTENT_TYPES.contains(contentType.trim().toLowerCase(Locale.ROOT))) {
            throw new GlobalException("图片规格不符合要求");
        }
        byte[] watermarked;
        try {
            watermarked = ImageWatermarkUtil.tileText(file.getBytes(), LICENSE_WATERMARK_TEXT);
        } catch (IOException e) {
            throw new GlobalException("图片规格不符合要求");
        }
        MultipartFile stamped = new MultipartFileUtil.CustomMultipartFile(
                watermarked, StringUtils.randomFileName() + ".jpeg", "image/jpeg");
        // 复用不落相册记录的重载，存储的文件已经带有水印。
        return galleryPicService.uploadImg(stamped);
    }

    @Override
    public String getApprovedLicensePic(Integer merchantId) {
        if (merchantId == null || merchantId <= 0) {
            return "";
        }
        Merchant merchant = this.getById(merchantId);
        if (merchant == null || Integer.valueOf(1).equals(merchant.getIsDelete())) {
            return "";
        }
        if (merchant.getMerchantApplyId() != null && merchant.getMerchantApplyId() > 0) {
            MerchantApply apply = merchantApplyMapper.selectById(merchant.getMerchantApplyId());
            if (apply == null || !MerchantApplyStatusEnum.AUDIT_PASS.getCode().equals(apply.getStatus())) {
                return "";
            }
        }
        // 入驻通过时复制到商户表；平台直接创建及后续维护也使用这份有效资料。
        if (StrUtil.isBlank(merchant.getMerchantData())) {
            return "";
        }
        JSONObject data = JSON.parseObject(merchant.getMerchantData());
        JSONArray pictures = data == null ? null : data.getJSONArray("businessLicenseImg");
        if (pictures != null) {
            for (int i = 0; i < pictures.size(); i++) {
                JSONObject picture = pictures.getJSONObject(i);
                if (picture == null) {
                    continue;
                }
                String url = picture.getString("picUrl");
                if (StrUtil.isBlank(url)) {
                    url = picture.getString("url");
                }
                if (StrUtil.isNotBlank(url)) {
                    return url;
                }
            }
        }
        return "";
    }

    @Resource
    private UserMapper userMapper;
    @Resource
    private MerchantUserService merchantUserService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    ShopFundsService shopFundsService;
    @Resource
    ConfigService configService;
    @Resource
    private MerchantUserMapper merchantUserMapper;
    @Resource
    TranslatePackageImpl translatePackage;
    @Resource
    MessageHelper messageHelper;
    @Resource
    AdminRoleService adminRoleService;
    @Resource
    private RegionService regionService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private TigshopProperties tigshopProperties;

    @Override
    public Page<MerchantVO> list(MerchantListPageQuery pageQuery) {
        Page<Merchant> page = buildSortOrder(pageQuery);

        Page<Merchant> merchantPage = this.page(page, Wrappers.lambdaQuery(Merchant.class)
                        .and(wrapper -> wrapper.isNull(Merchant::getIsDelete)
                                .or()
                                .eq(Merchant::getIsDelete, 0))
                .eq(null != pageQuery.getMerchantId(), Merchant::getMerchantId, pageQuery.getMerchantId())
                .and(StrUtil.isNotBlank(pageQuery.getKeyword()), wrapper -> wrapper
                        .like(Merchant::getCompanyName, pageQuery.getKeyword())
                        .or()
                        .like(Merchant::getCorporateName, pageQuery.getKeyword())
                )
        );

        List<Merchant> merchantRecords = merchantPage.getRecords();
        if (CollUtil.isEmpty(merchantRecords)) {
            return PageUtil.convertPage(merchantPage, Collections.emptyList());
        }

        // 查询出所有用户
        List<Integer> userIds = merchantRecords.stream().map(Merchant::getUserId).distinct().toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 查询出所有商户关联
        List<Integer> merchantIds = merchantRecords.stream().map(Merchant::getMerchantId).distinct().toList();
        List<MerchantUser> merchantUserList = merchantUserMapper.selectList(
                Wrappers.lambdaQuery(MerchantUser.class)
                        .in(MerchantUser::getMerchantId, merchantIds)
                        .eq(MerchantUser::getIsAdmin, 1)
        );
        Map<Integer, MerchantUser> merchantUserMap = merchantUserList.stream().collect(Collectors.toMap(MerchantUser::getMerchantId, Function.identity(), (existing, replacement) -> existing));

        // 然后去AdminUser表中查出数据
        List<Integer> adminUserIds = merchantUserList.stream().map(MerchantUser::getAdminUserId).distinct().toList();
        List<AdminUser> adminUsers = adminUserService.getAdminUserByIds(adminUserIds);
        Map<Integer, AdminUser> adminUserMap = adminUsers.stream().collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));

        // 转换为VO
        List<MerchantVO> merchantVOList = merchantRecords.stream()
                .map(merchant -> {
                    MerchantVO merchantVO = new MerchantVO();
                    BeanUtils.copyProperties(merchant, merchantVO);
                    long timestampInSeconds = merchant.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    merchantVO.setAddTime(sdf.format(date));
                    MerchantAdminDataDTO merchantUserData = new MerchantAdminDataDTO();
                    MerchantUser merchantUser = merchantUserMap.get(merchant.getMerchantId());
                    if (merchantUser != null) {
                        AdminUser adminUser = adminUserMap.get(merchantUser.getAdminUserId());
                        BeanUtils.copyProperties(merchantUser, merchantUserData);
                        if (adminUser != null) {
                            merchantUserData.setUsername(adminUser.getUsername());
                            merchantUserData.setAdminId(adminUser.getAdminId());
                        }
                        merchantVO.setAdmin(merchantUserData);
                    }
                    //存储uer数据
                    User user = userMap.get(merchant.getUserId());
                    merchantVO.setUser(user);
                    //从shop表中查出关联个数
                    Long shopCount = shopFundsService.getCountByMerchantId(merchant.getMerchantId());
                    merchantVO.setShopCount(shopCount);
                    merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));
                    merchantVO.setStatusText(MerchantStatus.getStatusName(merchant.getStatus()));

                    if (merchantVO.getAdmin() != null) {
                        User user1 = new User();
                        user1.setUsername(merchantVO.getAdmin().getUsername());
                        merchantVO.setUser(user1);
                    }
                    return merchantVO;
                }).toList();
        return PageUtil.convertPage(merchantPage, merchantVOList);
    }

    @Override
    public MerchantVO detail(Integer id) {
        if (id != null) {
            Merchant merchant = this.getById(id);
            MerchantVO merchantVO = new MerchantVO();
            BeanUtils.copyProperties(merchant, merchantVO);
            JSONObject merchantData = JSON.parseObject(merchant.getMerchantData());
            merchantVO.setMerchantData(merchantData);
            JSONObject baseData = JSON.parseObject(merchant.getBaseData());
            merchantVO.setBaseData(baseData);
            merchantVO.setShopData(JSON.parseObject(merchant.getShopData()));
            User user = userMapper.selectById(merchant.getUserId());
            merchantVO.setUser(user);
            long timestampInSeconds = merchant.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            merchantVO.setAddTime(sdf.format(date));
            merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));
            merchantVO.setStatusText(MerchantStatus.getStatusName(merchant.getStatus()));

            // merchantData: 新增 fullDetailedAddress 字段（地区中文名 + 详细地址）
            if (merchantData != null) {
                JSONArray businessAddress = merchantData.getJSONArray("businessAddress");
                String detailedAddress = merchantData.getString("detailedAddress");
                if (businessAddress != null && !businessAddress.isEmpty()) {
                    List<Integer> regionIds = businessAddress.toJavaList(Integer.class);
                    LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.in(Region::getRegionId, regionIds);
                    List<Region> regions = regionService.list(queryWrapper);
                    Map<Integer, String> regionMap = regions.stream()
                            .collect(Collectors.toMap(Region::getRegionId, Region::getRegionName));
                    StringBuilder fullAddress = new StringBuilder();
                    for (Integer regionId : regionIds) {
                        String regionName = regionMap.get(regionId);
                        if (regionName != null) {
                            fullAddress.append(regionName).append(" ");
                        }
                    }
                    if (StrUtil.isNotBlank(detailedAddress)) {
                        fullAddress.append(detailedAddress);
                    }
                    merchantData.put("fullDetailedAddress", fullAddress.toString().trim());
                }
            }

            // baseData: 新增 fullBusinessLicenseAddress 字段（地区中文名 + 营业执照地址）
            if (baseData != null) {
                JSONArray licenseAddrProvince = baseData.getJSONArray("licenseAddrProvince");
                String businessLicenseAddress = baseData.getString("businessLicenseAddress");
                if (licenseAddrProvince != null && !licenseAddrProvince.isEmpty()) {
                    List<Integer> regionIds = licenseAddrProvince.toJavaList(Integer.class);
                    LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.in(Region::getRegionId, regionIds);
                    List<Region> regions = regionService.list(queryWrapper);
                    Map<Integer, String> regionMap = regions.stream()
                            .collect(Collectors.toMap(Region::getRegionId, Region::getRegionName));
                    StringBuilder fullAddress = new StringBuilder();
                    for (Integer regionId : regionIds) {
                        String regionName = regionMap.get(regionId);
                        if (regionName != null) {
                            fullAddress.append(regionName).append(" ");
                        }
                    }
                    if (StrUtil.isNotBlank(businessLicenseAddress)) {
                        fullAddress.append(businessLicenseAddress);
                    }
                    baseData.put("fullBusinessLicenseAddress", fullAddress.toString().trim());
                }
            }

            return merchantVO;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(MerchantCreateDTO createDTO) {
        if (createDTO != null) {
            Merchant merchant = new Merchant();
            //获取数据
            MerchantCreateDTO.AdminInfo adminInfo = createDTO.getAdmin();
            MerchantCreateDTO.MerchantData merchantData = createDTO.getMerchantData();
            MerchantCreateDTO.BaseData baseData = createDTO.getBaseData();

            //赋值
            merchant.setAddTime(StringUtils.getCurrentTime());
            merchant.setMerchantData(JsonUtil.toJson(merchantData));
            merchant.setBaseData(JsonUtil.toJson(baseData));
            merchant.setType(baseData.getType());
            merchant.setCompanyName(baseData.getCompanyName());
            merchant.setCorporateName(baseData.getCorporateName());
            merchant.setMerchantApplyId(0);
            //将数据存入merchant表
            this.save(merchant);
            MerchantUser merchantUser = new MerchantUser();
            if (adminInfo.getType() == 1) {
                //获取用户数据
                User user = userMapper.selectById(adminInfo.getUserId());
                if (user == null) {
                    throw new GlobalException(messageHelper.get("error.member.not.found"));
                }
                String mobile = ObjectUtil.isEmpty(user.getMobile()) ? createDTO.getMerchantData().getContactPhone() : user.getMobile();
                if (ObjectUtil.isEmpty(mobile)) {
                    throw new GlobalException("关联会员或者商户手机号为空");
                }
                AdminUser adminUser = adminUserService.getAdminUserByUseMobile(mobile);
                //判断adminUser是否为空
                Integer adminId;
                if (adminUser == null) {
                    AdminUser adminUserCreate = getAdminUser(user);
                    adminUserCreate.setMobile(mobile);
                    adminUserService.save(adminUserCreate);
                    adminId = adminUserCreate.getAdminId();
                } else {
                    adminId = adminUser.getAdminId();
                }
                //操作merchant_user表
                merchantUser.setMerchantId(merchant.getMerchantId());
                merchantUser.setIsAdmin(1);
                merchantUser.setUserId(user.getUserId());
                merchantUserService.updateMerchantUser(merchantUser, adminId);
            } else {
                //更新admin_user表的merchant_id字段
                AdminUser adminUser = adminUserService.getById(adminInfo.getAdminId());
                if (adminUser == null) {
                    throw new GlobalException(messageHelper.get("error.merchant.admin.not.exist"));
                }
                adminUser.setMerchantId(merchant.getMerchantId());
                adminUserService.updateById(adminUser);
                //更新merchant_user表的merchant_id
                merchantUser.setMerchantId(merchant.getMerchantId());
                merchantUser.setIsAdmin(1);
                merchantUserService.updateMerchantUser(merchantUser, adminInfo.getAdminId());
            }
            return true;
        }
        return false;
    }

    private AdminUser getAdminUser(User user) {
        AdminUser adminUserCreate = new AdminUser();
        adminUserCreate.setUsername(user.getUsername());
        adminUserCreate.setUserId(user.getUserId());
        adminUserCreate.setMobile(user.getMobile());
        adminUserCreate.setEmail(user.getEmail());
        adminUserCreate.setPassword(PASSWORD);
        adminUserCreate.setAdminType("shop");
        adminUserCreate.setAvatar("");
        adminUserCreate.setRoleId(1);

        adminUserCreate.setMerchantId(0);
        adminUserCreate.setShopId(0);
        adminUserCreate.setIsUsing(0);
        adminUserCreate.setSuppliersId(0);
        adminUserCreate.setAddTime(StringUtils.getCurrentTime());

        if (ObjectUtil.isEmpty(adminUserCreate.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            adminUserCreate.setAvatar(format);
        }
        if (ObjectUtil.isNotEmpty(adminUserCreate.getPassword())) {
            adminUserCreate.setPassword(passwordEncoder.encode(adminUserCreate.getPassword()));
        }
        if (adminUserCreate.getRoleId() > 0) {
            adminUserCreate.setAuthList(adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>()
                            .eq(AdminRole::getRoleId, adminUserCreate.getRoleId()))
                    .getAuthorityList()
            );
        }
        return adminUserCreate;
    }

    @Override
    public boolean update(MerchantUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Merchant merchant = this.getById(updateDTO.getMerchantId());
            Assert.notNull(merchant, () -> new GlobalException("商户不存在"));
            //获取数据
            MerchantUpdateDTO.MerchantData merchantData = updateDTO.getMerchantData();
            MerchantUpdateDTO.BaseData baseData = updateDTO.getBaseData();

            //赋值
            merchant.setMerchantData(JsonUtil.toJson(merchantData));
            merchant.setBaseData(JsonUtil.toJson(baseData));
            merchant.setType(baseData.getType());
            merchant.setCompanyName(baseData.getCompanyName());
            merchant.setCorporateName(baseData.getCorporateName());
            return this.updateById(merchant);
        }
        return false;
    }

    @Override
    public MerchantVO getMerchantVO(Integer id) {
        Merchant merchant = this.getById(id);
        if (merchant == null) {
            return new MerchantVO();
        }
        MerchantVO merchantVO = new MerchantVO();
        BeanUtils.copyProperties(merchant, merchantVO);
        long meTimestampInSeconds = merchant.getAddTime();
        Date meDate = new Date(meTimestampInSeconds * 1000L);
        SimpleDateFormat meSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        merchantVO.setAddTime(meSdf.format(meDate));
        merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));
        merchantVO.setStatusText(MerchantStatus.getStatusName(merchant.getStatus()));
        MerchantUser merchantUser = merchantUserService.getDetailByMerchantId(merchant.getMerchantId(), false);
        MerchantAdminDataDTO merchantUserData = new MerchantAdminDataDTO();
        if (merchantUser != null) {
            BeanUtils.copyProperties(merchantUser, merchantUserData);
            AdminUser adminUser = adminUserService.getById(merchantUser.getAdminUserId());
            if (adminUser != null) {
                merchantUserData.setUsername(adminUser.getUsername());
                merchantUserData.setAdminId(adminUser.getAdminId());
            } else {
                merchantUserData.setUsername(null);
                merchantUserData.setAdminId(null);
            }
            merchantVO.setAdmin(merchantUserData);
        } else {
            merchantVO.setAdmin(null);
        }
        merchantVO.setUser(null);
        merchantVO.setShopCount(null);

        merchantVO.setMerchantData(JSON.parseObject(merchant.getMerchantData()));
        merchantVO.setBaseData(JSON.parseObject(merchant.getBaseData()));
        merchantVO.setShopData(JSON.parseObject(merchant.getShopData()));
        return merchantVO;
    }

    @Override
    public String applyShopAgreement() {
        String bizVal = configService.getConfigByCode(SettingsEnum.SHOP_AGREEMENT.getBizCode()).getBizVal();
        return translatePackage.translate(bizVal, DataType.ARTICLE_CONTENT_DESC.getCode());
    }

    @Override
    public void delMerchant(Integer id) {
        LambdaQueryWrapper<Shop> shopQuery = Wrappers.lambdaQuery(Shop.class)
                .eq(Shop::getMerchantId, id)
                .and(w -> w.isNull(Shop::getIsDelete).or().eq(Shop::getIsDelete, 0));
        if (tigshopProperties.getIsO2o() == 1) {
            // O2O 场景只校验门店/自提点，避免将普通店铺误判为“门店未处理”
            shopQuery.in(Shop::getShopType, ShopTypeEnum.STORE.getCode(), ShopTypeEnum.PICKUP.getCode());
        }
        List<Shop> shops = shopMapper.selectList(shopQuery);
        if (tigshopProperties.getIsO2o() == 1) {
            Assert.isTrue(CollUtil.isEmpty(shops), () -> new GlobalException("商户下有门店，请先处理门店"));
        } else {
            Assert.isTrue(CollUtil.isEmpty(shops), () -> new GlobalException("商户下有店铺，请先处理店铺"));
        }
        Merchant merchant = new Merchant();
        merchant.setMerchantId(id);
        merchant.setStatus(MerchantStatus.CANCEL.getCode());
        merchant.setIsDelete(1);
        this.updateById(merchant);
    }

}
