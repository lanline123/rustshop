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
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyCreateAccountDataDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyCreateDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyListDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyUpdateDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.model.finance.UserWithdrawApply;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.finance.UserWithdrawApplyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.UserWithdrawApplyMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserWithdrawApplyService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.user.UserBalanceService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.UserWithdrawApplyConstants.*;

/**
 * 提现申请服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class UserWithdrawApplyServiceImpl extends BaseServiceImpl<UserWithdrawApplyMapper, UserWithdrawApply> implements UserWithdrawApplyService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserBalanceService userBalanceService;
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    private com.tigshop.service.setting.ConfigService configService;

    @Override
    public Page<UserWithdrawApplyVO> list(UserWithdrawApplyListDTO listDTO) {
        // 分页
        Page<UserWithdrawApply> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserWithdrawApply> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 状态
        Integer status = listDTO.getStatus();
        if (status != null && status > -1) {
            queryWrapper.eq(UserWithdrawApply::getStatus, status);
        }
        // D1107：按钱包类型筛选（0余额/1佣金）
        if (listDTO.getWalletType() != null) {
            queryWrapper.eq(UserWithdrawApply::getWalletType, listDTO.getWalletType());
        }
        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            // 根据 userId 查 User 表的 Username 是否 like 这个 keyword
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, keyword));
            if (!CollUtil.isEmpty(users)) {
                List<Integer> matchingUserIds = users.stream()
                        .map(User::getUserId)
                        .collect(Collectors.toList());
                if (!matchingUserIds.isEmpty()) {
                    queryWrapper.in(UserWithdrawApply::getUserId, matchingUserIds);
                }
            }
        }
        // 执行查询
        Page<UserWithdrawApply> userWithdrawApplyPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserWithdrawApply> userWithdrawApplyPageRecords = userWithdrawApplyPage.getRecords();

        if (CollUtil.isEmpty(userWithdrawApplyPageRecords)) {
            return PageUtil.convertPage(userWithdrawApplyPage, List.of());
        }

        // 获取所有 ID
        List<Integer> userIds = userWithdrawApplyPageRecords.stream()
                .map(UserWithdrawApply::getUserId)
                .distinct()
                .toList();

        // 调用方法获取会员信息
        List<User> users = userMapper.selectBatchIds(userIds);
        // 将分类信息存入 Map，方便后续查找
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 转换为VO
        List<UserWithdrawApplyVO> userWithdrawApplyVOList = userWithdrawApplyPageRecords.stream()
                .map(userWithdrawApply -> {
                    UserWithdrawApplyVO userWithdrawApplyVO = new UserWithdrawApplyVO();
                    BeanUtils.copyProperties(userWithdrawApply, userWithdrawApplyVO);

                    User user = userMap.get(userWithdrawApply.getUserId());
                    itemDetail(userWithdrawApply, userWithdrawApplyVO, user);

                    return userWithdrawApplyVO;
                }).toList();
        return PageUtil.convertPage(userWithdrawApplyPage, userWithdrawApplyVOList);
    }

    @Override
    public UserWithdrawApplyVO detail(Integer id) {
        if (id != null) {
            UserWithdrawApply userWithdrawApply = this.getById(id);
            UserWithdrawApplyVO userWithdrawApplyVO = new UserWithdrawApplyVO();
            BeanUtils.copyProperties(userWithdrawApply, userWithdrawApplyVO);

            User user = userMapper.selectById(userWithdrawApply.getUserId());
            itemDetail(userWithdrawApply, userWithdrawApplyVO, user);

            return userWithdrawApplyVO;
        }
        return null;
    }

    private void itemDetail(UserWithdrawApply userWithdrawApply, UserWithdrawApplyVO userWithdrawApplyVO, User user) {
        // 将分类名称设置到对象中
        userWithdrawApplyVO.setUsername(user != null ? user.getUsername() : "--");

        String accountDataJson = userWithdrawApply.getAccountData();
        if (StringUtils.isNotEmpty(accountDataJson) && !"[]".equals(accountDataJson)){
            UserWithdrawApplyCreateAccountDataDTO userWithdrawApplyCreateAccountDataDTO =
                    JSONUtil.toBean(accountDataJson, UserWithdrawApplyCreateAccountDataDTO.class);
            userWithdrawApplyVO.setAccountData(userWithdrawApplyCreateAccountDataDTO);
        }

        DateTime date = DateUtil.date(userWithdrawApply.getAddTime() * 1000);
        userWithdrawApplyVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

        // 将分类名称设置到对象中
        userWithdrawApplyVO.setStatusType(STATUS_TYPE.get(userWithdrawApply.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(UserWithdrawApplyCreateDTO createDTO) {
        if (createDTO != null) {
            UserWithdrawApply userWithdrawApply = new UserWithdrawApply();
            BeanUtils.copyProperties(createDTO, userWithdrawApply);

            userWithdrawApply.setAccountData(JSONUtil.toJsonStr(createDTO.getAccountData()));
            userWithdrawApply.setAddTime(StringUtils.getCurrentTime());

            //创建时默认为0
            userWithdrawApply.setStatus(0);
            //获取会员信息
            User user = userMapper.selectById(createDTO.getUserId());

            if (user.getBalance().compareTo(createDTO.getAmount()) < 0) {
                //抛出错误余额不足
                throw new GlobalException(USER_BALANCE_NOT_ENOUGH);
            }
            if (!this.save(userWithdrawApply)) {
                return false;
            }
            //减少会员余额-增加冻结余额
            userBalanceService.incFrozenBalance(userWithdrawApply.getAmount(), userWithdrawApply.getUserId(), "增加冻结余额");
            userBalanceService.decBalance(userWithdrawApply.getAmount(), userWithdrawApply.getUserId(), "减少余额");

            // 发送后台消息  -- 提现申请
            AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
            adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.WITHDRAWAL_REQUEST.getCatId());
            adminMsgCreateDTO.setTitle("您有新的提现申请,申请用户：" + user.getUsername());
            adminMsgCreateDTO.setContent("用户【" + user.getUsername() + "】申请提现,申请金额：" + userWithdrawApply.getAmount() + configService.getCurrencyName());
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("withdrawApplyId", userWithdrawApply.getId());
            adminMsgCreateDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(adminMsgCreateDTO);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserWithdrawApplyUpdateDTO updateDTO) {
        // 获取数据库的数据判断是否能进行售后
        UserWithdrawApply userWithdrawApply = this.getById(updateDTO.getId());
        Assert.notNull(userMapper.selectById(userWithdrawApply.getUserId()), () -> new GlobalException("用户不存在"));
        Assert.notNull(userWithdrawApply, () -> new GlobalException("提现申请不存在"));

        Integer status = userWithdrawApply.getStatus();
        Assert.isTrue(status <= 0, () -> new GlobalException("该笔提现申请已完成，不能修改"));

        // 实例化一个新的售后对象进行更新
        UserWithdrawApply uwa = UserWithdrawApply.builder()
                .status(updateDTO.getStatus())
                .id(updateDTO.getId())
                .postscript(updateDTO.getPostscript())
                .build();
        if (updateDTO.getStatus() == 1) {
            uwa.setFinishedTime(StringUtils.getCurrentTime());
        }
        this.updateById(uwa);

        // D1107：按钱包类型操作对应账户（0余额/1佣金）
        boolean isCommission = userWithdrawApply.getWalletType() != null && userWithdrawApply.getWalletType() == 1;
        BigDecimal amount = userWithdrawApply.getAmount();
        Integer applyUserId = userWithdrawApply.getUserId();
        if (updateDTO.getStatus() == 1) {
            // 审核通过：扣减对应账户的冻结
            if (isCommission) {
                userBalanceService.decFrozenCommissionBalance(amount, applyUserId, "佣金提现审核通过扣减冻结");
            } else {
                userBalanceService.decFrozenBalance(amount, applyUserId, "提现审核通过扣减冻结余额");
            }
        } else {
            // 拒绝：返还可用 + 扣减冻结
            if (isCommission) {
                userBalanceService.incCommissionBalance(amount, applyUserId, "佣金提现审核拒绝返回");
                userBalanceService.decFrozenCommissionBalance(amount, applyUserId, "佣金提现审核拒绝扣减冻结");
            } else {
                userBalanceService.incBalance(amount, applyUserId, "提现审核拒绝返回余额");
                userBalanceService.decFrozenBalance(amount, applyUserId, "提现审核拒绝扣减冻结余额");
            }
        }
    }

    @Override
    public BigDecimal getUserWithdrawTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserWithdrawApply> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserWithdrawApply::getAddTime, startTimeDate.getTime() / 1000)
                    .le(UserWithdrawApply::getAddTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的余额变动
        queryWrapper.select(UserWithdrawApply::getAmount)
                .eq(UserWithdrawApply::getStatus, 1);
        List<UserWithdrawApply> userWithdrawApply = this.list(queryWrapper);

        // 计算余额变动总和
        return userWithdrawApply.stream()
                .map(UserWithdrawApply::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 删除单条提现申请：待处理（status=0）的申请涉及冻结资金，禁止删除（D1107 bug d3aac37b）。
     */
    @Override
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        UserWithdrawApply apply = this.getById(id);
        if (apply != null && apply.getStatus() != null && apply.getStatus() == 0) {
            throw new GlobalException("待处理的提现申请不允许删除");
        }
        return super.del(id);
    }

    /**
     * 批量操作：删除类型时，若选中项包含待处理（status=0）的申请，则整批拒绝（D1107 bug d3aac37b）。
     */
    @Override
    public boolean batch(BatchDTO batchDTO) {
        if (batchDTO != null && "del".equals(batchDTO.getType()) && CollUtil.isNotEmpty(batchDTO.getIds())) {
            long pendingCount = this.count(new LambdaQueryWrapper<UserWithdrawApply>()
                    .in(UserWithdrawApply::getId, batchDTO.getIds())
                    .eq(UserWithdrawApply::getStatus, 0));
            if (pendingCount > 0) {
                throw new GlobalException("待处理的提现申请不允许删除");
            }
        }
        return super.batch(batchDTO);
    }
}
