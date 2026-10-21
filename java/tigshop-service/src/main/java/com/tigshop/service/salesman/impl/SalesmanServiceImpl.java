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

package com.tigshop.service.salesman.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserDTO;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.RefundLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanCustomer;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.salesman.SalesmanEditParam;
import com.tigshop.bean.param.salesman.SalesmanSaveParam;
import com.tigshop.bean.param.salesman.CustomerRemarkUpdateParam;
import com.tigshop.bean.query.salesman.CustomerListPageQuery;
import com.tigshop.bean.query.salesman.CustomerApiPageQuery;
import com.tigshop.bean.query.salesman.SalesmanPageQuery;
import com.tigshop.bean.query.salesman.SalesmanRankingPageQuery;
import com.tigshop.bean.query.salesman.SalesmanStatisticalDetailPageQuery;
import com.tigshop.bean.vo.finance.RefundLogVO;
import com.tigshop.bean.vo.salesman.*;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RefundLogService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanGroupService;
import com.tigshop.service.salesman.SalesmanService;
import com.tigshop.service.setting.ConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 分销员借口实现
 *
 * @author kidd
 * @create 2025/6/21
 */
@RequiredArgsConstructor
@Service
public class SalesmanServiceImpl extends BaseServiceImpl<SalesmanMapper, Salesman> implements SalesmanService {

    private final UserMapper userMapper;

    private final SalesmanGroupService salesmanGroupService;

    private final SalesmanConfigService salesmanConfigService;

    private final SalesmanOrderMapper salesmanOrderMapper;

    private final SalesmanCustomerServiceImpl salesmanCustomerService;

    private final RefundLogService refundLogService;

    private final OrderService orderService;

    private final ConfigService configService;
    private final RefundApplyMapper refundApplyMapper;


    @Override
    public Page<SalesmanPageVO> list(SalesmanPageQuery pageQuery) {
        Page<Salesman> page = buildSortOrder(pageQuery);

        List<Integer> searchUserIds = null;
        List<Integer> searchPUserIds = null;
        long addTimeStart = 0;
        long addTimeEnd = 0;

        if (StringUtils.isNotEmpty(pageQuery.getMobile())) {
            List<User> users = userMapper.selectList(
                    Wrappers.lambdaQuery(User.class)
                            .eq(User::getMobile, pageQuery.getMobile())
                            .or()
                            .like(User::getUsername, pageQuery.getMobile())
                            .or()
                            .like(User::getNickname, pageQuery.getMobile())
            );

            if (CollUtil.isNotEmpty(users)) {
                searchUserIds = users.stream().map(User::getUserId).toList();
            } else {
                searchUserIds = Collections.singletonList(-1);
            }
        }

        if (StringUtils.isNotEmpty(pageQuery.getPidMobile())) {
            List<User> pUsers = userMapper.selectList(
                    Wrappers.lambdaQuery(User.class).eq(User::getMobile, pageQuery.getPidMobile())
            );

            if (null != pUsers) {
                searchPUserIds = pUsers.stream().map(User::getUserId).toList();
            } else {
                searchPUserIds = Collections.singletonList(-1);
            }
        }

        if (StringUtils.isNotBlank(pageQuery.getAddTimeStart())) {
            addTimeStart = TigUtils.toTimestampYmd(pageQuery.getAddTimeStart());
        }

        if (StringUtils.isNotBlank(pageQuery.getAddTimeEnd())) {
            addTimeEnd = TigUtils.toTimestampYmd(pageQuery.getAddTimeEnd());
        }

        Page<Salesman> salesmanPage = this.lambdaQuery()
                .in(searchUserIds != null, Salesman::getUserId, searchUserIds)
                .ge(StringUtils.isNotBlank(pageQuery.getAddTimeStart()) && addTimeStart != 0, Salesman::getAddTime, addTimeStart)
                .le(StringUtils.isNotBlank(pageQuery.getAddTimeEnd()) && addTimeEnd != 0, Salesman::getAddTime, addTimeEnd + (24 * 60 * 60))
                .in(null != searchPUserIds, Salesman::getPid, searchPUserIds)
                .eq(null != pageQuery.getGroupId(), Salesman::getGroupId, pageQuery.getGroupId())
                .eq(null != pageQuery.getLevel(), Salesman::getLevel, pageQuery.getLevel())
                .page(page);

        List<Salesman> salesmanRecords = salesmanPage.getRecords();
        if (CollUtil.isEmpty(salesmanRecords)) {
            return new Page<>();
        }

        // 查询销售员用户信息
        List<Integer> userIds = salesmanRecords.stream().map(Salesman::getUserId).toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 查询销售员组信息
        List<Integer> groupIds = salesmanRecords.stream().map(Salesman::getGroupId).toList();
        List<SalesmanGroup> groupList = salesmanGroupService.lambdaQuery().in(SalesmanGroup::getGroupId, groupIds).list();
        Map<Integer, SalesmanGroup> groupMap = groupList.stream().collect(Collectors.toMap(SalesmanGroup::getGroupId, Function.identity()));

        // 查询销售员上级用户信息
        List<Integer> pIds = salesmanRecords.stream().map(Salesman::getPid).toList();
        List<Salesman> pSalesmans = this.lambdaQuery().in(Salesman::getSalesmanId, pIds).list();
        List<Integer> pUserIds = pSalesmans.stream().map(Salesman::getUserId).toList();
        List<User> pUsers = new ArrayList<>();
        if (CollUtil.isNotEmpty(pUserIds)) {
            pUsers = userMapper.selectBatchIds(pUserIds);
        }
        Map<Integer, User> pUserMap = pUsers.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        Map<Integer, User> pSalesmanMap = pSalesmans.stream().collect(Collectors.toMap(Salesman::getSalesmanId, salesman -> pUserMap.get(salesman.getUserId())));

        //获得config配置
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        Map<Integer, String> levelMap = salesmanConfig.getLevel().stream().collect(Collectors.toMap(SalesmanConfigVO.Level::getId, SalesmanConfigVO.Level::getName));

        List<SalesmanPageVO> resultRecords = salesmanRecords.stream().map(salesman -> {
            SalesmanPageVO salesmanPageVO = new SalesmanPageVO(salesman, userMap.get(salesman.getUserId()), groupMap.get(salesman.getGroupId()), pSalesmanMap.get(salesman.getPid()));

            salesmanPageVO.setLevelText(levelMap.get(salesman.getLevel()));

            // 查询总客户数
            long count = salesmanCustomerService.lambdaQuery().eq(SalesmanCustomer::getSalesmanId, salesman.getSalesmanId()).count();
            salesmanPageVO.setTotalCustomer((int) count);

            return salesmanPageVO;
        }).toList();

        return PageUtil.convertPage(salesmanPage, resultRecords);
    }

    public Page<SalesmanVO> old_list(SalesmanPageQuery pageQuery) {
        // 分页
        Page<Salesman> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Salesman> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, pageQuery.getSortField(), pageQuery.getSortOrder());

        if (pageQuery.getMobile() != null && StringUtils.isNotEmpty(pageQuery.getMobile())) {
            List<User> users = userMapper.selectList(new QueryWrapper<User>()
                    .like("mobile", pageQuery.getMobile())
                    .or()
                    .like("username", pageQuery.getMobile())
            );
            if (users != null && !users.isEmpty()) {
                queryWrapper.in(Salesman::getUserId, users.stream().map(User::getUserId).toList());
            } else {
                queryWrapper.eq(Salesman::getUserId, -1);
            }
        }
        //根据pid_mobile参数筛选分销员
        if (pageQuery.getPidMobile() != null && StringUtils.isNotEmpty(pageQuery.getPidMobile())) {
            List<User> users = userMapper.selectList(new QueryWrapper<User>()
                    .like("mobile", pageQuery.getPidMobile())
            );
            if (users != null && !users.isEmpty()) {
                queryWrapper.in(Salesman::getPid, users.stream().map(User::getUserId).toList());
            } else {
                queryWrapper.eq(Salesman::getPid, -1);
            }
        }

        if (getShopId() != null) {
            queryWrapper.eq(Salesman::getShopId, getShopId());
        }

        if (pageQuery.getLevel() != null) {
            queryWrapper.eq(Salesman::getLevel, pageQuery.getLevel());
        }

        if (pageQuery.getGroupId() != null) {
            queryWrapper.eq(Salesman::getGroupId, pageQuery.getGroupId());
        }

        if (pageQuery.getSalesmanId() != null) {
            queryWrapper.eq(Salesman::getSalesmanId, pageQuery.getSalesmanId());
        }

        if (pageQuery.getUserId() != null) {
            queryWrapper.eq(Salesman::getUserId, pageQuery.getUserId());
        }

        if (pageQuery.getAddTimeStart() != null && StringUtils.isNotEmpty(pageQuery.getAddTimeStart())) {
            queryWrapper.ge(Salesman::getAddTime, DateUtil.parse(pageQuery.getAddTimeStart()).getTime() / 1000);
        }

        if (pageQuery.getAddTimeEnd() != null && StringUtils.isNotEmpty(pageQuery.getAddTimeEnd())) {
            queryWrapper.le(Salesman::getAddTime, DateUtil.parse(pageQuery.getAddTimeEnd()).getTime() / 1000);
        }

        // 筛选字段未找到（待处理）
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<User> users = userMapper.selectList(new QueryWrapper<User>()
                    .like("mobile", keyword)
                    .or()
                    .like("username", keyword)
            );
            List<Integer> list = users.stream().map(User::getUserId).toList();
            if (ObjectUtil.isNotEmpty(list)) {
                queryWrapper.in(Salesman::getUserId, list);
            }
        }

        // 执行查询
        Page<Salesman> salesmanPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Salesman> salesmanPageRecords = salesmanPage.getRecords();
        //从records中获得group_id大于0的
        List<Salesman> groupIdGreaterThanZero = salesmanPageRecords.stream()
                .filter(salesman -> salesman.getGroupId() > 0)
                .toList();
        //从group服务中查询分组信息
        List<Integer> list = groupIdGreaterThanZero.stream()
                .map(Salesman::getGroupId)
                .toList();
        Map<Integer, SalesmanGroup> groupMap = null;
        if (!list.isEmpty()) {
            List<SalesmanGroup> groupList = salesmanGroupService.list(new QueryWrapper<SalesmanGroup>().in("group_id", list));
            groupMap = groupList.stream().collect(Collectors.toMap(SalesmanGroup::getGroupId, salesmanGroup -> salesmanGroup));
        } else {
            groupMap = new HashMap<>();
        }
        //从records获得用户ids
        List<Integer> userIds = new java.util.ArrayList<>(salesmanPageRecords.stream()
                .map(Salesman::getUserId)
                .toList());

        //取出所有pids
        List<Integer> pids = salesmanPageRecords.stream()
                .map(Salesman::getPid).filter(pid -> pid > 0)
                .toList();
        Map<Integer, Salesman> pidSalesmanMap;
        if (!pids.isEmpty()) {
            List<Salesman> pidSalesman = this.list(new QueryWrapper<Salesman>().in("salesman_id", pids));
            pidSalesmanMap = pidSalesman.stream().collect(Collectors.toMap(Salesman::getSalesmanId, salesman -> salesman));
            //取出pidSalesman里的userId存入到userIds中
            userIds.addAll(pidSalesman.stream()
                    .map(Salesman::getUserId)
                    .toList());
        } else {
            pidSalesmanMap = new HashMap<>();
        }

        //从User服务获得用户信息
        Map<Integer, User> userMap = null;
        if (!userIds.isEmpty()) {
            List<User> userList = userMapper.selectBatchIds(userIds);
            userMap = userList.stream().collect(Collectors.toMap(User::getUserId, user -> user));
        } else {
            userMap = new HashMap<>();
        }

        //获得config配置
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        Map<Integer, String> levelMap = salesmanConfig.getLevel().stream().collect(Collectors.toMap(SalesmanConfigVO.Level::getId, SalesmanConfigVO.Level::getName));
        // 转换为VO
        Map<Integer, SalesmanGroup> finalGroupMap = groupMap;
        Map<Integer, User> finalUserMap = userMap;
        List<SalesmanVO> salesmanVOList = salesmanPageRecords.stream()
                .map(salesman -> {
                    SalesmanVO salesmanVO = new SalesmanVO();
                    BeanUtils.copyProperties(salesman, salesmanVO);
                    User user = finalUserMap.get(salesman.getUserId());
                    UserBaseVO userBaseVO = new UserBaseVO();
                    if (user != null) {
                        BeanUtils.copyProperties(user, userBaseVO);
                        userBaseVO.setAvatar(getUserAvatar(user.getAvatar()));
                        if (user.getDistributionRegisterTime() != null && user.getDistributionRegisterTime() > 0) {
                            userBaseVO.setDistributionRegisterTime(DateUtil.format(DateUtil.date(user.getDistributionRegisterTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
                        }
                    }

                    salesmanVO.setBaseUserInfo(userBaseVO);

                    if (salesman.getPid() > 0) {
                        SalesmanVO.PidUserInfo pidUserInfo = new SalesmanVO.PidUserInfo();
                        BeanUtils.copyProperties(pidSalesmanMap.get(salesman.getPid()), pidUserInfo);
                        User pUser = finalUserMap.get(pidUserInfo.getUserId());
                        UserBaseVO pUserBaseVO = new UserBaseVO();
                        BeanUtils.copyProperties(pUser, pUserBaseVO);
                        pUserBaseVO.setAvatar(getUserAvatar(pUser.getAvatar()));
                        pidUserInfo.setBaseUserInfo(pUserBaseVO);
                        salesmanVO.setPidUserInfo(pidUserInfo);
                    }

                    if (salesman.getGroupId() > 0) {
                        salesmanVO.setGroupInfo(finalGroupMap.get(salesman.getGroupId()));
                    }
                    salesmanVO.setAddTime(DateUtil.format(DateUtil.date(salesman.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
                    if (salesman.getLevel() > 0) {
                        salesmanVO.setLevelText(levelMap.get(salesman.getLevel()));
                    }
                    long count = salesmanCustomerService.count(new LambdaQueryWrapper<SalesmanCustomer>().eq(SalesmanCustomer::getSalesmanId, salesman.getSalesmanId()));
                    salesmanVO.setTotalCustomer((int) count);
                    return salesmanVO;
                }).toList();
        return PageUtil.convertPage(salesmanPage, salesmanVOList);
    }

    @Override
    public SalesmanDetailVO detail(Integer id) {
        //获得config配置
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        Map<Integer, String> levelMap = salesmanConfig.getLevel().stream().collect(Collectors.toMap(SalesmanConfigVO.Level::getId, SalesmanConfigVO.Level::getName));

        Salesman salesman = this.getById(id);
        SalesmanVO salesmanVO = new SalesmanVO();
        BeanUtils.copyProperties(salesman, salesmanVO);

        User user = userMapper.selectById(salesman.getUserId());
        UserBaseVO userBaseVO = new UserBaseVO();
        BeanUtils.copyProperties(user, userBaseVO);
        salesmanVO.setBaseUserInfo(userBaseVO);

        if (salesman.getPid() > 0) {
            Salesman pidSalesman = this.getById(salesman.getPid());
            if(Objects.nonNull(pidSalesman)){
                SalesmanVO.PidUserInfo pidUserInfo = new SalesmanVO.PidUserInfo();
                BeanUtils.copyProperties(pidSalesman, pidUserInfo);
                User pUser = userMapper.selectById(pidUserInfo.getUserId());
                if(pUser!=null){
                    UserBaseVO pUserBaseVO = new UserBaseVO();
                    BeanUtils.copyProperties(pUser, pUserBaseVO);
                    pidUserInfo.setBaseUserInfo(pUserBaseVO);
                }
                salesmanVO.setPidUserInfo(pidUserInfo);
            }
        }

        if (salesman.getGroupId() > 0) {
            SalesmanGroup salesmanGroup = salesmanGroupService.getById(salesman.getGroupId());
            salesmanVO.setGroupInfo(salesmanGroup);
        }

        salesmanVO.setAddTime(TigUtils.handelTime(salesman.getAddTime()));
        if (salesman.getLevel() > 0) {
            salesmanVO.setLevelText(levelMap.get(salesman.getLevel()));
        }

        SalesmanDetailVO.Statistical statistical = getStatistical(salesmanVO, salesmanConfig);

        return new SalesmanDetailVO(salesmanVO, statistical);
    }

    private SalesmanDetailVO.Statistical getStatistical(SalesmanVO salesmanVO, SalesmanConfigVO salesmanConfig) {
        BigDecimal saleAmount = salesmanVO.getSaleAmount();

        Long orderNum = salesmanOrderMapper.selectCount(
                Wrappers.lambdaQuery(SalesmanOrder.class).eq(SalesmanOrder::getSalesmanId, salesmanVO.getSalesmanId())
        );

        Long customerNum = salesmanCustomerService.lambdaQuery().eq(SalesmanCustomer::getSalesmanId, salesmanVO.getSalesmanId()).count();
        Long inviteNum = this.lambdaQuery().eq(Salesman::getPid, salesmanVO.getSalesmanId()).count();

        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(
                Wrappers.lambdaQuery(SalesmanOrder.class).eq(SalesmanOrder::getSalesmanId, salesmanVO.getSalesmanId())
        );
        // 佣金金额（已经结算的）
        BigDecimal commissionAmount = salesmanOrders.stream().filter(order -> order.getStatus() == 1).map(SalesmanOrder::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 商品交易金额（不含已失效/已退款的：仅待结算+已结算）
        BigDecimal productTransactionAmount = salesmanOrders.stream()
                .filter(order -> order.getStatus() != 2)
                .map(SalesmanOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SalesmanDetailVO.Statistical.builder()
                .saleAmount(saleAmount)
                .orderNum(orderNum)
                .customerNum(customerNum)
                .inviteNum(inviteNum)
                .commissionAmount(commissionAmount)
                .productCommissionAmount(productTransactionAmount)
                .build();
    }

    @Override
    public void create(SalesmanSaveParam param) {
        long salesmanCount = this.lambdaQuery().eq(Salesman::getUserId, param.getUserId()).count();
        Assert.isTrue(salesmanCount == 0, () -> new GlobalException("该用户已添加"));

        Salesman salesman = param.createSalesman();
        this.save(salesman);
    }

    @Override
    public void update(SalesmanEditParam param) {
        Salesman salesman = this.getById(param.getSalesmanId());
        Assert.notNull(salesman, () -> new GlobalException("未知的分销员"));

        Salesman updateSalesman = param.updateSalesman(salesman);
        this.updateById(updateSalesman);
    }

    @Override
    public Page<SalesmanStatisticalVO> statistical(SalesmanStatisticalDetailPageQuery pageQuery, HttpServletResponse response) {
        Integer shopId = getShopId();
        Integer userId = 0;
        if (StringUtils.isNotEmpty(pageQuery.getMobile())) {
            UserDTO user = getUserByMobileOrUsername(pageQuery.getMobile());
            if (user == null) {
                return new Page<>();
            }
            userId = user.getUserId();
        }

        Page<Salesman> page = buildSortOrder(pageQuery);
        Page<Salesman> salesmanPage = this.lambdaQuery()
                .eq(shopId != null, Salesman::getShopId, shopId)
                .eq(userId > 0, Salesman::getUserId, userId)
                .eq(pageQuery.getGroupId() != null, Salesman::getGroupId, pageQuery.getGroupId())
                .eq(pageQuery.getLevel() != null, Salesman::getLevel, pageQuery.getLevel())
                .page(page);

        // 获取查询结果
        List<Salesman> salesmanRecords = salesmanPage.getRecords();

        if (CollUtil.isEmpty(salesmanRecords)) {
            return new Page<>();
        }

        //查询 salesman_config 信息
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail(SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode());
        Map<Integer, String> levelNameMap = salesmanConfig.getLevel().stream().collect(Collectors.toMap(SalesmanConfigVO.Level::getId, SalesmanConfigVO.Level::getName));

        // 查询销售员用户信息
        List<Integer> userIds = salesmanRecords.stream().map(Salesman::getUserId).toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 查询销售员组信息
        List<Integer> groupIds = salesmanRecords.stream().map(Salesman::getGroupId).toList();
        List<SalesmanGroup> groupList = salesmanGroupService.lambdaQuery().in(SalesmanGroup::getGroupId, groupIds).list();
        Map<Integer, SalesmanGroup> groupMap = groupList.stream().collect(Collectors.toMap(SalesmanGroup::getGroupId, Function.identity()));

        List<Integer> salesmanIds = salesmanRecords.stream().map(Salesman::getSalesmanId).toList();
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(
                Wrappers.lambdaQuery(SalesmanOrder.class).in(SalesmanOrder::getSalesmanId, salesmanIds)
        );
        Map<Integer, List<SalesmanOrder>> orderIdToSalesmanListMap = salesmanOrders.stream().collect(Collectors.groupingBy(SalesmanOrder::getOrderId));

        // 获取支付订单
        List<Order> paidOrders = new ArrayList<>();
        if (CollUtil.isNotEmpty(salesmanOrders)) {
            List<Integer> orderIds = new ArrayList<>(orderIdToSalesmanListMap.keySet());
            paidOrders = orderService.lambdaQuery()
                    .in(Order::getOrderId, orderIds)
                    .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
                    .list();
        }

        Map<Integer, List<SalesmanOrder>> paidOrderMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesmanOrders)) {
            paidOrderMap = paidOrders.stream()
                    .flatMap(order -> {
                        List<SalesmanOrder> salesmanOrderList = orderIdToSalesmanListMap.get(order.getOrderId());
                        return salesmanOrderList != null ? salesmanOrderList.stream() : Stream.empty();
                    })
                    .collect(Collectors.groupingBy(SalesmanOrder::getSalesmanId));
        }

        // 获取退款订单
        List<RefundLog> refundLogs = new ArrayList<>();
        if (CollUtil.isNotEmpty(salesmanOrders)) {
            List<Integer> orderIds = new ArrayList<>(orderIdToSalesmanListMap.keySet());
            refundLogs = refundLogService.lambdaQuery()
                    .in(RefundLog::getOrderId, orderIds)
                    .list();
        }

        Map<Integer, List<RefundLogVO>> refundLogMap = new HashMap<>();
        if (CollUtil.isNotEmpty(refundLogs)) {
            refundLogMap = refundLogs.stream()
                    .map(log -> {
                        SalesmanOrder salesmanOrder = salesmanOrderMapper.selectOne(
                                new LambdaQueryWrapper<SalesmanOrder>()
                                        .eq(SalesmanOrder::getOrderId, log.getOrderId()).last("limit 1"));
                        RefundLogVO refundLogVO = new RefundLogVO();
                        BeanUtils.copyProperties(log, refundLogVO);
                        BigDecimal refundBalance = refundApplyMapper.selectById(log.getRefundApplyId()).getRefundBalance();
                        refundLogVO.setRefundAmount(refundBalance);
                        refundLogVO.setSalesmanId(salesmanOrder.getSalesmanId());
                        return refundLogVO;
                    })
                    .collect(Collectors.groupingBy(RefundLogVO::getSalesmanId));
        }

        // 获取这段时间新增客户数
        List<SalesmanCustomer> salesmanCustomers = salesmanCustomerService.lambdaQuery()
                .in(SalesmanCustomer::getSalesmanId, salesmanIds)
                .list();

        List<SalesmanOrder> settlementSalesmanOrders = salesmanOrderMapper.selectList(
                Wrappers.lambdaQuery(SalesmanOrder.class).in(SalesmanOrder::getSalesmanId, salesmanIds).eq(SalesmanOrder::getStatus, Constants.YES)
        );

        Map<Integer, List<SalesmanOrder>> finalPaidOrderMap = paidOrderMap;
        Map<Integer, List<RefundLogVO>> finalRefundLogMap = refundLogMap;
        List<SalesmanStatisticalVO> resultRecords = salesmanRecords.stream()
                .map(salesman -> {
                    User user = userMap.get(salesman.getUserId());
                    SalesmanGroup group = groupMap.get(salesman.getGroupId());
                    List<SalesmanOrder> currSalesmanOrders = salesmanOrders.stream().filter(salesmanOrder -> salesmanOrder.getSalesmanId().equals(salesman.getSalesmanId())).toList();
                    SalesmanStatisticalVO result = new SalesmanStatisticalVO(salesman, user, group, currSalesmanOrders);

                    result.setLevelText(levelNameMap.get(salesman.getLevel()));

                    List<SalesmanOrder> currPaidOrders = finalPaidOrderMap.getOrDefault(salesman.getSalesmanId(), new ArrayList<>());
                    result.setOrderCount(currPaidOrders.size());

                    BigDecimal payMoney = currPaidOrders.stream().map(SalesmanOrder::getOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.setPayMoney(payMoney);

                    long totalCustomer = salesmanCustomers.stream().filter(customer -> customer.getSalesmanId().equals(salesman.getSalesmanId())).count();
                    result.setTotalCustomer(totalCustomer);

                    List<RefundLogVO> currRefundLogs = finalRefundLogMap.getOrDefault(salesman.getSalesmanId(), new ArrayList<>());
                    result.setRefundCount(currRefundLogs.size());

                    BigDecimal refundAmount = currRefundLogs.stream().map(RefundLogVO::getRefundAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.setRefundAmount(refundAmount);

                    BigDecimal totalCommission = settlementSalesmanOrders.stream()
                            .filter(salesmanOrder -> salesmanOrder.getSalesmanId().equals(salesman.getSalesmanId()))
                            .map(SalesmanOrder::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.setTotalCommission(totalCommission);


                    BigDecimal totalAmountCommission = currSalesmanOrders.stream()
                            .map(SalesmanOrder::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.setTotalAmountCommission(totalAmountCommission);

                    return result;
                })
                .toList();

        if (pageQuery.getIsExport() != null && pageQuery.getIsExport() > 0) {
            //导出数据
            export(resultRecords, response);
        }

        return PageUtil.convertPage(salesmanPage, resultRecords);
    }

    @Override
    public CommissionDetailsVO commissionDetails(Integer salesmanId) {
        SalesmanVO salesmanVO = detail(salesmanId).getSalesmanVO();
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(
                Wrappers.lambdaQuery(SalesmanOrder.class).eq(SalesmanOrder::getSalesmanId, salesmanId)
        );

        return new CommissionDetailsVO(salesmanVO, salesmanOrders);
    }

    private void export(List<SalesmanStatisticalVO> salesmanStatisticalVOList, HttpServletResponse response) {
        List<SalesmanStatisticalExportVO> exportVO = new ArrayList<>();
        if (salesmanStatisticalVOList != null) {
            for (SalesmanStatisticalVO salesmanStatisticalVO : salesmanStatisticalVOList) {
                SalesmanStatisticalExportVO salesmanStatisticalExportVO = new SalesmanStatisticalExportVO();
                salesmanStatisticalExportVO.setMobile(salesmanStatisticalVO.getBaseUserInfo().getMobile());
                salesmanStatisticalExportVO.setNickname(salesmanStatisticalVO.getBaseUserInfo().getNickname());
                salesmanStatisticalExportVO.setLevelText(salesmanStatisticalVO.getLevelText());
                salesmanStatisticalExportVO.setGroupName(salesmanStatisticalVO.getGroupInfo().getGroupName());
                salesmanStatisticalExportVO.setSaleAmount(salesmanStatisticalVO.getSaleAmount());
                salesmanStatisticalExportVO.setPayMoney(salesmanStatisticalVO.getPayMoney());
                salesmanStatisticalExportVO.setRefundMoney(salesmanStatisticalVO.getRefundAmount());
                salesmanStatisticalExportVO.setOrderCount(salesmanStatisticalVO.getOrderCount());
                salesmanStatisticalExportVO.setRefundCount(salesmanStatisticalVO.getRefundCount());
                salesmanStatisticalExportVO.setTotalCommission(salesmanStatisticalVO.getTotalCommission());
                salesmanStatisticalExportVO.setTotalAmountCommission(salesmanStatisticalVO.getTotalAmountCommission());
                salesmanStatisticalExportVO.setTotalCustomer(salesmanStatisticalVO.getTotalCustomer());
                exportVO.add(salesmanStatisticalExportVO);
            }
        }
        ExcelUtils<SalesmanStatisticalExportVO> excelUtils = new ExcelUtils<>(exportVO, SalesmanStatisticalExportVO.class);
        excelUtils.exportExcelWeb(response);
    }

    @Override
    public Page<SalesmanRankingVO> ranking(SalesmanRankingPageQuery pageQuery) {
        pageQuery.handleSearchTime();

        //根据新增销售额来统计
        Page<SalesmanRankingVO> page = buildSortOrder(pageQuery);
        Page<SalesmanRankingVO> salesmanRankingPage = salesmanOrderMapper.ranking(page, pageQuery);

        List<SalesmanRankingVO> records = salesmanRankingPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        // 查询分销员用户
        List<Integer> salesmanIds = records.stream().map(SalesmanRankingVO::getSalesmanId).toList();
        List<Salesman> salesmenList = this.listByIds(salesmanIds);
        List<Integer> userIds = salesmenList.stream().map(Salesman::getUserId).toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        Map<Integer, User> salesmanUserMap = salesmenList.stream().collect(Collectors.toMap(Salesman::getSalesmanId, salesman -> userMap.get(salesman.getUserId())));


        // 获取支付客户数
        Map<Integer, Long> payCustomerNumMap = new HashMap<>();
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(
                Wrappers.lambdaQuery(SalesmanOrder.class)
                        .in(SalesmanOrder::getSalesmanId, salesmanIds)
                        .ge(SalesmanOrder::getAddTime, pageQuery.getSearchStartTime())
                        .le(SalesmanOrder::getAddTime, pageQuery.getSearchEndTime())
        );

        if (CollUtil.isNotEmpty(salesmanOrders)) {
            Map<Integer, List<SalesmanOrder>> orderIdToSalesmanListMap = salesmanOrders.stream().collect(Collectors.groupingBy(SalesmanOrder::getOrderId));

            List<Integer> orderIds = new ArrayList<>(orderIdToSalesmanListMap.keySet());

            List<Order> paidOrders = orderService.lambdaQuery()
                    .in(Order::getOrderId, orderIds)
                    .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
                    .list();

            payCustomerNumMap = paidOrders.stream()
                    // 先根据 userId 去重
                    .collect(Collectors.toMap(
                            Order::getUserId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ))
                    .values().stream()
                    .flatMap(order -> {
                        List<SalesmanOrder> relatedSalesmanOrders = orderIdToSalesmanListMap.get(order.getOrderId());
                        return relatedSalesmanOrders != null ? relatedSalesmanOrders.stream() : Stream.empty();
                    })
                    .collect(Collectors.toMap(
                            SalesmanOrder::getOrderId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ))
                    .values().stream()
                    .collect(Collectors.groupingBy(
                            SalesmanOrder::getSalesmanId,
                            Collectors.counting()
                    ));
        }

        Map<Integer, Long> finalPayCustomerNumMap = payCustomerNumMap;
        records.forEach(record -> {
            User user = salesmanUserMap.get(record.getSalesmanId());
            Long totalPayCustomers = finalPayCustomerNumMap.get(record.getSalesmanId());
            record.assembleData(user, totalPayCustomers);
        });

        return salesmanRankingPage;
    }

    @Override
    public SalesmanUserDetailVO detailForApi() {
        Salesman salesman = this.lambdaQuery()
                .eq(Salesman::getUserId, SecurityUtils.getCurrentUserId())
                .one();
        Assert.notNull(salesman, "当前用户不是分销员");

        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        Assert.notNull(salesmanConfig, "没有对应的商户配置");

        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(Wrappers.query(SalesmanOrder.class).eq("salesman_id", salesman.getSalesmanId()));

        Order sumSelfBuyOrder = orderService.query().eq("user_id", salesman.getUserId()).select("sum(paid_amount) as paid_amount").one();
        BigDecimal sumSelfBuy = sumSelfBuyOrder != null ? sumSelfBuyOrder.getPaidAmount() : BigDecimal.ZERO;

        SalesmanUserDetailVO salesmanUserDetailVO = new SalesmanUserDetailVO();
        BeanUtils.copyProperties(salesman, salesmanUserDetailVO);

        for (SalesmanConfigVO.Level level : salesmanConfig.getLevel()) {
            if (level.getId().equals(salesman.getLevel())) {
                salesmanUserDetailVO.setLevelName(level.getName());
                salesmanUserDetailVO.setCondition(level.getCondition());
            }
        }

        List<SalesmanCustomer> salesmanCustomers = salesmanCustomerService.lambdaQuery().eq(SalesmanCustomer::getSalesmanId, salesman.getSalesmanId()).list();

        return new SalesmanUserDetailVO(salesman, salesmanConfig, salesmanOrders, salesmanCustomers, sumSelfBuy);
    }

    @Override
    public SalesmanVO getSalesman(Integer id) {
        //获得config配置
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        Map<Integer, String> levelMap = salesmanConfig.getLevel().stream().collect(Collectors.toMap(SalesmanConfigVO.Level::getId, SalesmanConfigVO.Level::getName));

        Salesman salesman = this.getById(id);
        if (salesman == null) {
            return null;
        }
        SalesmanVO salesmanVO = new SalesmanVO();
        BeanUtils.copyProperties(salesman, salesmanVO);

        User user = userMapper.selectById(salesman.getUserId());
        UserBaseVO userBaseVO = new UserBaseVO();
        BeanUtils.copyProperties(user, userBaseVO);
        salesmanVO.setBaseUserInfo(userBaseVO);

        if (salesman.getPid() > 0) {
            SalesmanVO.PidUserInfo pidUserInfo = new SalesmanVO.PidUserInfo();
            BeanUtils.copyProperties(this.getById(salesman.getPid()), pidUserInfo);
            User pUser = userMapper.selectById(pidUserInfo.getUserId());
            UserBaseVO pUserBaseVO = new UserBaseVO();
            BeanUtils.copyProperties(pUser, pUserBaseVO);
            pidUserInfo.setBaseUserInfo(pUserBaseVO);
            salesmanVO.setPidUserInfo(pidUserInfo);
        }

        if (salesman.getGroupId() > 0) {
            SalesmanGroup salesmanGroup = salesmanGroupService.getById(salesman.getGroupId());
            salesmanVO.setGroupInfo(salesmanGroup);
        }

        salesmanVO.setAddTime(DateUtil.format(DateUtil.date(salesman.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
        if (salesman.getLevel() > 0) {
            salesmanVO.setLevelText(levelMap.get(salesman.getLevel()));
        }

        SalesmanDetailVO.Statistical statistical = getStatistical(salesmanVO, salesmanConfig);

        return salesmanVO;
    }

    // 根据手机号或用户名查询用户信息
    @Override
    public UserDTO getUserByMobileOrUsername(String search) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getMobile, search).or().eq(User::getUsername, search);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return null;
        }
        UserDTO userdto = new UserDTO();
        BeanUtils.copyProperties(user, userdto);
        return userdto;
    }

    @Override
    public Page<SalesmanSelectVO> salesmanList(String nickname) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .like(ObjectUtil.isNotEmpty(nickname), User::getNickname, nickname)
                .or()
                .like(ObjectUtil.isNotEmpty(nickname), User::getUsername, nickname)
                .or()
                .like(ObjectUtil.isNotEmpty(nickname), User::getMobile, nickname)
        );
        List<SalesmanSelectVO> salesmanVos = new ArrayList<>();
        for (User user : users) {
            Salesman one = getOne(new LambdaQueryWrapper<Salesman>().eq(Salesman::getUserId, user.getUserId()));
            if (one != null) {
                SalesmanSelectVO salesmanVO = new SalesmanSelectVO();
                salesmanVO.setSalesmanId(one.getSalesmanId());
                salesmanVO.setUserId(one.getUserId());
                UserBaseVO target = new UserBaseVO();
                BeanUtils.copyProperties(user, target);
                salesmanVO.setUser(target);
                target.setNickname(target.getNickname() == null ? user.getUsername() : target.getNickname());
                salesmanVos.add(salesmanVO);
            }
        }
        Page<SalesmanSelectVO> page = new Page<>(0, 0);
        page.setRecords(salesmanVos);
        return page;
    }

    @Override
    public Page<CustomerListVO> customerList(CustomerListPageQuery pageQuery) {
        Page<Object> page = Page.of(pageQuery.getPage(), pageQuery.getSize());
        List<CustomerListVO> customerListVOS = getBaseMapper().customerList(page, pageQuery);
        for (CustomerListVO customerListVO : customerListVOS) {
            customerListVO.setAvatar(getUserAvatar(customerListVO.getAvatar()));
        }
        return PageUtil.convertPage(page, customerListVOS);
    }

    @Override
    public Page<CustomerListVO> customerApiList(CustomerApiPageQuery pageQuery) {
        Page<Object> page = Page.of(pageQuery.getPage(), pageQuery.getSize());
        pageQuery.setCustomerType(pageQuery.normalizedCustomerType());
        List<CustomerListVO> records = "salesman".equals(pageQuery.getCustomerType())
                ? getBaseMapper().customerApiSalesmanList(page, pageQuery)
                : getBaseMapper().customerApiList(page, pageQuery);
        records.forEach(record -> record.setAvatar(getUserAvatar(record.getAvatar())));
        return PageUtil.convertPage(page, records);
    }

    @Override
    public CustomerDetailVO customerApiDetail(Integer salesmanId, Integer userId, String startTime, String endTime, Integer shopId) {
        CustomerListVO record = getBaseMapper().customerApiDetail(salesmanId, userId, startTime, endTime, shopId);
        if (record == null) {
            record = getBaseMapper().customerApiSalesmanDetail(salesmanId, userId, startTime, endTime, shopId);
        }
        Assert.notNull(record, "客户不存在");
        CustomerDetailVO detail = new CustomerDetailVO();
        BeanUtils.copyProperties(record, detail);
        detail.setAvatar(getUserAvatar(detail.getAvatar()));
        if (detail.getOrderCount() == null) {
            detail.setOrderCount(0L);
        }
        if (detail.getOrderAmount() == null) {
            detail.setOrderAmount(BigDecimal.ZERO);
        }
        detail.setAverageOrderAmount(detail.getOrderCount() == null || detail.getOrderCount() == 0 || detail.getOrderAmount() == null
                ? BigDecimal.ZERO
                : detail.getOrderAmount().divide(BigDecimal.valueOf(detail.getOrderCount()), 2, java.math.RoundingMode.HALF_UP));
        return detail;
    }

    @Override
    public void updateCustomerRemark(Integer salesmanId, CustomerRemarkUpdateParam param) {
        SalesmanCustomer binding = salesmanCustomerService.lambdaQuery()
                .eq(SalesmanCustomer::getSalesmanId, salesmanId)
                .eq(SalesmanCustomer::getUserId, param.getUserId())
                .one();
        Assert.notNull(binding, "客户不存在");
        binding.setRemark(param.getRemark().trim());
        salesmanCustomerService.updateById(binding);
    }

    public String getUserAvatar(String avatar) {
        if (avatar == null || StringUtils.isEmpty(avatar)) {
            //从配置获取默认头像
            avatar = configService.getConfigByCode(SettingsEnum.DEFAULT_AVATAR.getBizCode()).getBizVal();
        }
        return avatar;
    }


}
