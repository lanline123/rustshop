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
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserMessageLogCreateDTO;
import com.tigshop.bean.enums.user.SendUserTypeEnum;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.bean.model.user.UserMessageLog;
import com.tigshop.bean.param.user.UserMessageLogRecallParam;
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.vo.user.UserMessageLogPageVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.user.UserMessageLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserMessageLogService;
import com.tigshop.service.user.UserMessageService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.user.MemberAudienceService;
import com.tigshop.bean.dto.user.MemberAudienceDTO;
import com.tigshop.bean.dto.user.SendUserTargetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;


/**
 * @author Tigshop团队
 */
@RequiredArgsConstructor
@Service
public class UserMessageLogServiceImpl extends BaseServiceImpl<UserMessageLogMapper, UserMessageLog> implements UserMessageLogService {

    private final UserService userService;

    private final UserMessageService userMessageService;

    private final MemberAudienceService memberAudienceService;

    @Override
    public Page<UserMessageLogPageVO> list(UserMessageListPageQuery pageQuery) {
        Page<UserMessageLog> page = buildSortOrder(pageQuery);

        //分页查询
        Page<UserMessageLog> userMessageLogPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), UserMessageLog::getMessageTitle, pageQuery.getKeyword())
                .page(page);
        List<UserMessageLog> userMessageLogs = userMessageLogPage.getRecords();

        if (CollUtil.isEmpty(userMessageLogs)) {
            return new Page<>();
        }

        List<UserMessageLogPageVO> resultRecords = userMessageLogs.stream().map(UserMessageLogPageVO::new).toList();

        return PageUtil.convertPage(userMessageLogPage, resultRecords);
    }

    @Override
    public UserMessageLog detail(Integer id) {
        if (id == null) {
            throw new GlobalException(SERVICE_ERROR);
        }
        return this.getOne(new QueryWrapper<UserMessageLog>().eq("message_log_id", id));
    }

    @Transactional
    @Override
    public boolean create(UserMessageLogCreateDTO dto) {
        List<Integer> userIds = findUserIds(dto);

        UserMessageLog userMessageLog = dto.buildUserMessageLog();
        this.save(userMessageLog);

        Long currentTime = StringUtils.getCurrentTime();
        List<UserMessage> userMessages = userIds.stream()
                .map(userId -> UserMessage.builder()
                        .messageLogId(userMessageLog.getMessageLogId())
                        .userId(userId)
                        .userRank(0)
                        .isRead(Constants.NO)
                        .title(userMessageLog.getMessageTitle())
                        .content(userMessageLog.getMessageContent())
                        .link(userMessageLog.getMessageLink())
                        .addTime(currentTime)
                        .build()
                )
                .toList();
        userMessageService.saveBatch(userMessages);

        return true;
    }

    private List<Integer> findUserIds(UserMessageLogCreateDTO dto) {
        Integer sendUserType = dto.getSendUserType();
        List<Integer> userIds = new ArrayList<>();

        // 统一人群结构存在时一律以它为准，sendUserType 仅作来源标记
        if (dto.getSendUserTarget() != null) {
            // 站内信不落库、不走 resolveWriteScope，三组互斥需在此显式归一
            SendUserTargetDTO target = memberAudienceService.normalizeExclusive(dto.getSendUserTarget());
            memberAudienceService.validateTarget(target);
            userIds = memberAudienceService.resolveUserIds(target).stream()
                    .map(Long::intValue)
                    .toList();
            Assert.notEmpty(userIds, () -> new GlobalException("该会员选择类型下没有会员"));
            return userIds;
        }

        if (SendUserTypeEnum.ALL.getCode().equals(sendUserType)) {
            List<User> users = userService.lambdaQuery().list();
            userIds = users.stream().map(User::getUserId).toList();

        } else if (SendUserTypeEnum.SINGLE.getCode().equals(sendUserType)) {
            // user_ids: 110
            userIds.add(Integer.valueOf(dto.getUserIds()));

        } else if (SendUserTypeEnum.RANK.getCode().equals(sendUserType)) {
            List<User> users = userService.lambdaQuery()
                    .eq(User::getRankId, dto.getUserRank())
                    .list();
            userIds = users.stream().map(User::getUserId).toList();

        } else if (SendUserTypeEnum.PART.getCode().equals(sendUserType)) {
            // user_ids: "aaa,bbb"
            List<String> usernames = StrUtil.split(dto.getUserIds(), StrUtil.COMMA);
            List<User> users = userService.lambdaQuery()
                    .in(User::getUsername, usernames)
                    .list();
            Assert.isTrue(usernames.size() == users.size(), () -> new GlobalException("请检查输入的每个会员名称是否正确"));
            userIds = users.stream().map(User::getUserId).toList();

        } else if (SendUserTypeEnum.TAG.getCode().equals(sendUserType)) {
            // 二期：按会员标签人群圈选解析目标用户
            MemberAudienceDTO audience = dto.getMemberAudience();
            if (audience != null) {
                userIds = memberAudienceService.resolveUserIds(audience).stream()
                        .map(Long::intValue)
                        .toList();
            }
        }

        Assert.notEmpty(userIds, () -> new GlobalException("该会员选择类型下没有会员"));
        return userIds;
    }

    @Override
    public void recall(UserMessageLogRecallParam param) {
        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setIsRecall(1);
        userMessageLog.setMessageLogId(param.getId());
        this.updateById(userMessageLog);
        // 删除use_message表数据
        userMessageService.lambdaUpdate()
                .eq(UserMessage::getMessageLogId, param.getId())
                .remove();
    }

}

