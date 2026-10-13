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

package com.tigshop.service.finance.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserInvoiceCreateDTO;
import com.tigshop.bean.dto.finance.UserInvoiceListDTO;
import com.tigshop.bean.dto.finance.UserInvoiceUpdateDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.finance.UserInvoiceStatus;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.UserInvoice;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.finance.UserInvoiceVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.mapper.finance.UserInvoiceMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserInvoiceService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.UserInvoiceConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 发票资质服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class UserInvoiceServiceImpl extends BaseServiceImpl<UserInvoiceMapper, UserInvoice> implements UserInvoiceService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    MessageHelper messageHelper;
    @Resource
    private ConfigService configService;
    @Override
    public Page<UserInvoiceVO> list(UserInvoiceListDTO listDTO) {
        // 分页
        Page<UserInvoice> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserInvoice> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer status = listDTO. getStatus();
        if (status != null && status > -1) {
            queryWrapper.eq(UserInvoice::getStatus, status);
        }
        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {

            // 会员名称搜索
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, keyword));
            List<Integer> matchingUserIds = users.stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());

            queryWrapper.and(wrapper -> {
                // 始终添加公司名称的模糊搜索条件
                wrapper.like(UserInvoice::getCompanyName, keyword);

                // 只有在 matchingUserIds 不为空时，才添加 in 条件
                if (!matchingUserIds.isEmpty()) {
                    wrapper.or().in(UserInvoice::getUserId, matchingUserIds);
                }
            });

        }
        // 执行查询
        Page<UserInvoice> userInvoicePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserInvoice> userInvoicePageRecords = userInvoicePage.getRecords();

        if(CollUtil.isEmpty(userInvoicePageRecords)){
            return PageUtil.convertPage(userInvoicePage, List.of());
        }

        // 获取所有 ID
        List<Integer> userIds = userInvoicePageRecords.stream()
                .map(UserInvoice::getUserId)
                .distinct()
                .toList();

        // 调用方法获取会员信息
        List<User> users = userMapper.selectBatchIds(userIds);
        // 将分类信息存入 Map，方便后续查找
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 转换为VO
        List<UserInvoiceVO> userInvoiceVOList = userInvoicePageRecords.stream()
                .map(userInvoice -> {
                    UserInvoiceVO userInvoiceVO = new UserInvoiceVO();
                    BeanUtils.copyProperties(userInvoice, userInvoiceVO);
                    userInvoiceVO.setAddTime(TigUtils.handelTime(userInvoice.getAddTime()));
                    User user = userMap.get(userInvoice.getUserId());
                    if (user != null){
                        userInvoiceVO.setUsername(user.getUsername()); // 将分类名称设置到对象中
                    }

                    userInvoiceVO.setStatusName(STATUS_NAME.get(userInvoice.getStatus()));

                    return userInvoiceVO;
                }).toList();
        return PageUtil.convertPage(userInvoicePage, userInvoiceVOList);
    }

    @Override
    public UserInvoiceVO detail(Integer id) {
        if (id != null) {
            UserInvoice userInvoice = this.getById(id);
            UserInvoiceVO userInvoiceVO = new UserInvoiceVO();
            BeanUtils.copyProperties(userInvoice, userInvoiceVO);

            User user = userMapper.selectById(userInvoiceVO.getUserId());
            if (user != null){
                // 将分类名称设置到对象中
                userInvoiceVO.setUsername(user.getUsername());
            }
            return userInvoiceVO;
        }
        return null;
    }

    @Override
    public boolean create(UserInvoiceCreateDTO createDTO) {
        if (createDTO != null) {
            UserInvoice userInvoice = new UserInvoice();
            BeanUtils.copyProperties(createDTO, userInvoice);
            userInvoice.setAddTime(StringUtils.getCurrentTime());
            return this.save(userInvoice);
        }
        return false;
    }

    @Override
    public boolean clientCreate(UserInvoiceCreateDTO createDTO) {
        if (createDTO != null) {
            // 服务端兜底校验：增值税专用发票功能关闭时拒绝提交，防止绕过前端隐藏直接调接口
            if (!"1".equals(configService.getConfigVal(SettingsEnum.INVOICE_ADDED))) {
                throw new GlobalException("增值税专用发票功能已关闭，无法提交增票资质");
            }
            Integer userId = getCurrentUserId();
            LambdaQueryWrapper<UserInvoice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserInvoice::getUserId, userId);
            queryWrapper.last("limit 1");
            if(this.getOne(queryWrapper) != null){
                throw new GlobalException(messageHelper.get("error.invoice.apply.repeat"));
            }
            UserInvoice userInvoice = new UserInvoice();
            BeanUtils.copyProperties(createDTO, userInvoice);
            userInvoice.setAddTime(StringUtils.getCurrentTime());
            userInvoice.setUserId(userId);
            userInvoice.setStatus(STATUS_AUDIT);
            this.save(userInvoice);
            //增票资质申请 -- 发送后台消息
            return createAdminMsg(userId, userInvoice, createDTO.getCompanyName());
        }
        return false;
    }

    @Override
    public boolean update(UserInvoiceUpdateDTO updateDTO) {
        if (updateDTO != null) {
            UserInvoice userInvoice = new UserInvoice();
            BeanUtils.copyProperties(updateDTO, userInvoice);
            return this.updateById(userInvoice);
        }
        return false;
    }

    @Override
    public boolean clientUpdate(UserInvoiceUpdateDTO updateDTO) {
        if (updateDTO != null) {
            // 服务端兜底校验：增值税专用发票功能关闭时拒绝提交，防止绕过前端隐藏直接调接口
            if (!"1".equals(configService.getConfigVal(SettingsEnum.INVOICE_ADDED))) {
                throw new GlobalException("增值税专用发票功能已关闭，无法提交增票资质");
            }
            Integer userId = getCurrentUserId();
            UserInvoice existing = this.lambdaQuery()
                    .eq(UserInvoice::getInvoiceId, updateDTO.getInvoiceId())
                    .eq(UserInvoice::getUserId, userId)
                    .one();
            if (existing == null) {
                throw new GlobalException("发票资质不存在");
            }
            updateDTO.setUserId(userId);
            UserInvoice userInvoice = new UserInvoice();
            BeanUtils.copyProperties(updateDTO, userInvoice);
            // 客户端提交的审核默认为待审核
            userInvoice.setStatus(STATUS_AUDIT);
            this.updateById(userInvoice);
            //增票资质申请 -- 发送后台消息
            return createAdminMsg(userId, userInvoice, updateDTO.getCompanyName());
        }
        return false;
    }

    //发送后台消息
    private boolean createAdminMsg(Integer userId, UserInvoice userInvoice, String companyName) {
        User user = userMapper.selectById(userId);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.INVOICE_QUALIFICATION_AUDIT.getCatId());
        adminMsgCreateDTO.setTitle("您有新的发票资质申请,申请单位：" + companyName);
        adminMsgCreateDTO.setContent("用户【" + user.getUsername() + "】提交了发票资质申请,请处理");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("invoiceId", userInvoice.getInvoiceId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);

        return true;
    }

    @Override
    public UserInvoiceVO getStatus() {
        Integer userId = getCurrentUserId();
        LambdaQueryWrapper<UserInvoice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInvoice::getUserId, userId);
        queryWrapper.orderByDesc(UserInvoice::getInvoiceId);
        queryWrapper.last("limit 1");
        UserInvoice userInvoice = this.getOne(queryWrapper);
        if(userInvoice != null) {
            if(Objects.equals(UserInvoiceStatus.AUDIT_PASS.getCode(), userInvoice.getStatus())) {
                UserInvoiceVO userInvoiceVO = new UserInvoiceVO();
                BeanUtils.copyProperties(userInvoice, userInvoiceVO);
                return userInvoiceVO;
            }
        }
        return null;
    }

    @Override
    public UserInvoiceVO clientDetail() {
        Integer userId = getCurrentUserId();
        LambdaQueryWrapper<UserInvoice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInvoice::getUserId, userId);
        queryWrapper.orderByDesc(UserInvoice::getInvoiceId);
        queryWrapper.last("limit 1");
        UserInvoice userInvoice = this.getOne(queryWrapper);

        UserInvoiceVO userInvoiceVO = new UserInvoiceVO();
        if(userInvoice != null) {
            BeanUtils.copyProperties(userInvoice, userInvoiceVO);
        } else {
            userInvoiceVO.setStatus(0);
        }
        return userInvoiceVO;
    }
}
