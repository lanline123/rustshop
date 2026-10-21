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

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserPointsLogDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.bean.query.user.UserPointsLogListPageQuery;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserPointsLog;
import com.tigshop.bean.vo.user.UserPointsLogPageVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.user.UserPointsLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserPointsLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Tigshop团队
 */
@Service
public class UserPointsLogServiceImpl extends BaseServiceImpl<UserPointsLogMapper, UserPointsLog> implements UserPointsLogService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private ConfigService configService;

    @Override
    public Integer getPoints(){
        // 查询用户积分
        User currentUser = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class).eq(User::getUserId, SecurityUtils.getCurrentAdminId())
        );
        return currentUser.getPoints();
    }

    @Override
    public Page<UserPointsLogDTO> list(UserPointsLogListPageQuery listDTO){
        // 分页
        Page<UserPointsLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserPointsLog> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.orderByDesc(UserPointsLog::getChangeTime);
        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, keyword));
            List<Integer> matchingUserIds = users.stream()
                    .map(User::getUserId)
                    .collect(toList());
            if (matchingUserIds.isEmpty()) {
                return new Page<>();
            }
            queryWrapper.in(UserPointsLog::getUserId, matchingUserIds);
        }

        if (listDTO.getUserId() != null) {
            queryWrapper.eq(UserPointsLog::getUserId, listDTO.getUserId());
        }
        // 执行查询
        Page<UserPointsLog> queryPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserPointsLog> userPointsLogsRecords = queryPage.getRecords();
        // 获取查询出的 uid
        List<Integer> userIds = userPointsLogsRecords.stream()
                .map(UserPointsLog::getUserId)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(userPointsLogsRecords)) {
            return new Page<>();
        }
        // 根据用户 id 去查询出用户信息
        List<User> userList = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getUserId, userIds));
        // 将用户信息存入 map 中
        Map<Integer, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<UserPointsLogDTO> userPointsDTO = userPointsLogsRecords.stream()
                .map(userPointsLog -> {
                    UserPointsLogDTO dto = new UserPointsLogDTO();
                    BeanUtils.copyProperties(userPointsLog, dto);
                    dto.setChangeTypeName(userPointsLog.getChangeType() == 1 ? "增加" : "减少");
                    if (userPointsLog.getChangeTime() != null) {
                        long timestampInSeconds = userPointsLog.getChangeTime();
                        Date date = new Date(timestampInSeconds * 1000L);
                        // 使用 SimpleDateFormat 格式化日期
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dto.setChangeTime(sdf.format(date));
                    }
                    User user = userMap.get(userPointsLog.getUserId());
                    if (user != null) {
                        dto.setUsername(user.getUsername());
                    }
                    return dto;
                })
                .collect(toList());

        return PageUtil.convertPage(queryPage, userPointsDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incPoints(Integer usePoints, Integer userId, String desc, Integer relationId) {
        UserPointsLog userPointsLog = new UserPointsLog();
        userPointsLog.setUserId(userId);
        userPointsLog.setPoints(usePoints);
        userPointsLog.setChangeTime(StringUtils.getCurrentTime());
        userPointsLog.setChangeDesc(desc);
        userPointsLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
        userPointsLog.setRelationId(relationId);

        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        if (this.save(userPointsLog)) {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getUserId, userId).setIncrBy(User::getPoints, usePoints);
            if (userMapper.update(updateWrapper) < 1) {
                throw new GlobalException(configVal + "增加失败");
            }
        } else {
            throw new GlobalException(configVal + "增加失败");
        }
    }

    @Override
    public void subPoints(Integer usePoints, Integer userId, String desc, Integer relationId) {
        UserPointsLog userPointsLog = new UserPointsLog();
        userPointsLog.setUserId(userId);
        userPointsLog.setPoints(usePoints);
        userPointsLog.setChangeTime(StringUtils.getCurrentTime() - 1);
        userPointsLog.setChangeDesc(desc);
        userPointsLog.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
        userPointsLog.setRelationId(relationId);

        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        if (this.save(userPointsLog)) {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getUserId, userId).setDecrBy(User::getPoints, usePoints);
            if (userMapper.update(updateWrapper) < 1) {
                throw new GlobalException(configVal + "减少失败");
            }
        } else {
            throw new GlobalException(configVal + "减少失败");
        }
    }

    @Override
    public UserPointsLogPageVO listPage(UserPointsLogListPageQuery pageQuery) {
        Page<UserPointsLogDTO> page = this.list(pageQuery);

        // 查询用户积分
        User currentUser = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class).eq(User::getUserId, pageQuery.getUserId())
        );

        return new UserPointsLogPageVO(currentUser.getPoints(), page.getRecords(), page.getTotal());
    }
}
