// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserCompanyAuditDTO;
import com.tigshop.bean.dto.user.UserCompanyCreateDTO;
import com.tigshop.bean.dto.user.UserCompanyListDTO;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.user.UserCompanyAudit;
import com.tigshop.bean.enums.user.UserCompanyType;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCompany;
import com.tigshop.bean.vo.user.UserCompanyApplyVO;
import com.tigshop.bean.vo.user.UserCompanyItemVO;
import com.tigshop.bean.vo.user.UserCompanyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.user.UserCompanyMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.user.UserCompanyService;
import com.tigshop.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.user.UserCompanyConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 会员企业认证服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserCompanyServiceImpl extends BaseServiceImpl<UserCompanyMapper, UserCompany> implements UserCompanyService {

    private final UserService userService;
    private final RegionService regionService;
    private final MessageService messageService;

    @Override
    public Page<UserCompanyVO> list(UserCompanyListDTO listDTO) {
        // 分页
        Page<UserCompany> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getMobile, keyword);
            List<User> userList = userService.list(userQueryWrapper);
            List<Integer> userIds = userList == null ? List.of() : userList.stream()
                    .map(User::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            queryWrapper.and(wrapper -> {
                if (!userIds.isEmpty()) {
                    wrapper.in(UserCompany::getUserId, userIds)
                            .or()
                            .like(UserCompany::getContactMobile, keyword);
                } else {
                    wrapper.like(UserCompany::getContactMobile, keyword);
                }
            });
        }

        if (listDTO.getStatus() != null && listDTO.getStatus() != 0) {
            queryWrapper.eq(UserCompany::getStatus, listDTO.getStatus());
        }

        // 执行查询
        Page<UserCompany> userCompanyPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserCompany> userCompanyPageRecords = userCompanyPage.getRecords();
        // 转换为VO
        List<UserCompanyVO> userCompanyVOList = userCompanyPageRecords.stream()
                .map(userCompany -> {
                    UserCompanyVO userCompanyVO = new UserCompanyVO();
                    BeanUtils.copyProperties(userCompany, userCompanyVO);
                    //查询用户数据
                    UserCompanyVO.UserVO userVO = new UserCompanyVO.UserVO();
                    User user = userService.lambdaQuery().eq(User::getUserId, userCompany.getUserId()).one();
                    if (user != null) {
                        BeanUtils.copyProperties(user, userVO);
                        userCompanyVO.setUsername(user.getUsername());
                        userCompanyVO.setIsCompanyAuth(user.getIsCompanyAuth());
                    }
                    userCompanyVO.setUser(userVO);
                    userCompanyVO.setStatusText(UserCompanyAudit.getStatusName(userCompany.getStatus()));
                    userCompanyVO.setTypeText(UserCompanyType.getTypeName(userCompany.getType()));
                    // 兼容之前的 companyData 下划线的内容
                    String userCompanyJson = JSONUtil.toJsonStr(JSONUtil.toBean(userCompany.getCompanyData(), UserCompanyCreateDTO.CompanyDataVO.class));
                    userCompanyVO.setCompanyData(JsonUtil.fromJson(userCompanyJson, JSONObject.class));
                    userCompanyVO.setAddTime(TigUtils.handelTime(userCompany.getAddTime()));
                    userCompanyVO.setAuditTime(TigUtils.handelTime(userCompany.getAuditTime()));
                    return userCompanyVO;
                }).toList();
        return PageUtil.convertPage(userCompanyPage, userCompanyVOList);
    }

    @Override
    public UserCompanyItemVO detail(Integer id) {
        if (id != null) {
            UserCompany userCompany = this.getById(id);
            if (userCompany == null) {
                throw new GlobalException("企业认证信息不存在");
            }
            return buildDetailVO(userCompany);
        }
        return null;
    }

    @Override
    public UserCompanyItemVO clientDetail(Integer id, Integer userId) {
        if (id == null || userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        UserCompany userCompany = this.lambdaQuery()
                .eq(UserCompany::getId, id)
                .eq(UserCompany::getUserId, userId)
                .one();
        if (userCompany == null) {
            throw new GlobalException("企业认证信息不存在");
        }
        return buildDetailVO(userCompany);
    }

    private UserCompanyItemVO buildDetailVO(UserCompany userCompany) {
        UserCompanyItemVO userCompanyItemVO = new UserCompanyItemVO();
        BeanUtils.copyProperties(userCompany, userCompanyItemVO);
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getUserId, userCompany.getUserId());
        User user = userService.getOne(userQueryWrapper);
        Optional.ofNullable(user).ifPresent((copyUser)->{
            UserCompanyItemVO.UserVO userVO = new UserCompanyItemVO.UserVO();
            BeanUtils.copyProperties(copyUser, userVO);
            userCompanyItemVO.setUser(userVO);
        });
        userCompanyItemVO.setStatusText(UserCompanyAudit.getStatusName(userCompany.getStatus()));
        userCompanyItemVO.setTypeText(UserCompanyType.getTypeName(userCompany.getType()));
        userCompanyItemVO.setAddTime(TigUtils.handelTime(userCompany.getAddTime()));
        userCompanyItemVO.setAuditTime(TigUtils.handelTime(userCompany.getAuditTime()));
        UserCompanyItemVO.CompanyDataVO companyDataVO = JsonUtil.fromJson(userCompany.getCompanyData(), UserCompanyItemVO.CompanyDataVO.class);
        if (companyDataVO.getLicenseAddrProvince() != null && !companyDataVO.getLicenseAddrProvince().isEmpty()) {
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Region::getRegionId, companyDataVO.getLicenseAddrProvince());
            List<Region> regions = regionService.list(queryWrapper);
            if (regions != null && !regions.isEmpty()) {
                Map<Integer, String> regionMap = regions.stream().collect(Collectors.toMap(Region::getRegionId,
                        Region::getRegionName));
                StringBuilder licenseAddrProvinceName = new StringBuilder();
                for (Integer regionId : companyDataVO.getLicenseAddrProvince()) {
                    if (regionMap.get(regionId) != null) {
                        licenseAddrProvinceName.append(regionMap.get(regionId));
                    }
                }
                companyDataVO.setLicenseAddrProvinceName(licenseAddrProvinceName.toString());
            }
        }
        userCompanyItemVO.setCompanyData(companyDataVO);
        return userCompanyItemVO;
    }

    @Override
    public boolean create(UserCompanyCreateDTO createDTO) {
        if (createDTO != null) {
            UserCompany userCompany = new UserCompany();
            BeanUtils.copyProperties(createDTO, userCompany);
            return this.save(userCompany);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(UserCompanyAuditDTO auditDTO) {

        UserCompany userCompany = this.getById(auditDTO.getId());
        try {
            Assert.notNull(userCompany, () -> new GlobalException("未知的会员企业认证信息"));
            Assert.isTrue(Objects.equals(UserCompanyAudit.PENDING.getCode(), userCompany.getStatus()), () -> new GlobalException(USER_COMPANY_STATUS_ONLY_WAIT));
            Assert.isFalse(Objects.equals(auditDTO.getStatus(), UserCompanyAudit.FAIL.getCode()) && StringUtils.isEmpty(auditDTO.getAuditRemark()), () -> new GlobalException(USER_COMPANY_AUDIT_REMARK_NOT_NULL));

            if (Objects.equals(auditDTO.getStatus(), UserCompanyAudit.PASS.getCode())) {
                userCompany.setAuditTime(StringUtils.getCurrentTime());
            }

            userCompany.setStatus(auditDTO.getStatus());
            userCompany.setAuditRemark(auditDTO.getAuditRemark() != null ? auditDTO.getAuditRemark() : "");
            this.updateById(userCompany);

            // 更新会员信息
            if (Objects.equals(auditDTO.getStatus(), UserCompanyAudit.PASS.getCode())) {
                User user = userService.getById(userCompany.getUserId());
                Assert.notNull(user, () -> new GlobalException(USER_NOT_EXIST));

                user.setIsCompanyAuth(1);
                userService.updateById(user);
            }
        }catch (Exception e){
            throw e;
        }


        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCompanyVO companyAudit(UserCompanyCreateDTO createDTO) {
        Integer userId = getCurrentUserId();
        UserCompany userCompany = new UserCompany();
        userCompany.setUserId(userId);
        userCompany.setType(createDTO.getType());
        userCompany.setContactName(createDTO.getCompanyData().getCorporateName() != null ?
                createDTO.getCompanyData().getCorporateName() : "");
        userCompany.setContactMobile(createDTO.getCompanyData().getContactPhone() != null ?
                createDTO.getCompanyData().getContactPhone() : "");
        userCompany.setCompanyName(createDTO.getCompanyData().getCompanyName() != null ?
                createDTO.getCompanyData().getCompanyName() : "");
        userCompany.setCompanyData(JsonUtil.toJson(createDTO.getCompanyData()));
        userCompany.setAddTime(StringUtils.getCurrentTime());

        if (Objects.equals(createDTO.getStatus(), UserCompanyAudit.PASS.getCode())) {
            userCompany.setAuditTime(StringUtils.getCurrentTime());
        }
        //创建数据
        this.save(userCompany);
        if (Objects.equals(createDTO.getStatus(), UserCompanyAudit.PASS.getCode())) {
            User user = userService.getById(userId);
            if (user == null) {
                throw new GlobalException(USER_NOT_EXIST);
            }
            user.setIsCompanyAuth(1);
            userService.updateById(user);
        }

        try {
            messageService.sendUserMessage(userCompany.getUserId(), -1, MessageTypeEnum.USER_COMPANY_AUTH);
        } catch (Exception ex) {
            log.error("企业认证通知 USER_COMPANY_AUTH 发送失败，不影响认证主流程，userId={}: {}", userCompany.getUserId(), ex.getMessage(), ex);
        }
        UserCompanyVO userCompanyVO = new UserCompanyVO();
        UserCompany userCompanyData = this.getById(userCompany.getId());
        BeanUtils.copyProperties(userCompanyData, userCompanyVO);
        userCompanyVO.setAddTime(TigUtils.handelTime(userCompanyData.getAddTime()));
        // 兼容之前的 companyData 下划线的内容
        String userCompanyJson = JSONUtil.toJsonStr(JSONUtil.toBean(userCompany.getCompanyData(), UserCompanyCreateDTO.CompanyDataVO.class));
        userCompanyVO.setCompanyData(JsonUtil.fromJson(userCompanyJson, JSONObject.class));
        return userCompanyVO;
    }

    @Override
    public UserCompanyApplyVO myApply() {
        Integer userId = getCurrentUserId();
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getUserId, userId);
        queryWrapper.orderByDesc(UserCompany::getId);
        queryWrapper.last("limit 1");
        UserCompany userCompany = this.getOne(queryWrapper);
        UserCompanyApplyVO userCompanyApplyVO = new UserCompanyApplyVO();
        if (userCompany != null) {
            BeanUtils.copyProperties(userCompany, userCompanyApplyVO);
            userCompanyApplyVO.setTypeText(UserCompanyType.getTypeName(userCompany.getType()));
            userCompanyApplyVO.setStatusText(UserCompanyAudit.getStatusName(userCompany.getStatus()));
        }
        return userCompanyApplyVO;
    }
}
