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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.finance.*;
import com.tigshop.bean.enums.order.PayMethodType;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.Statement;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.VendorWithdraw;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.query.finance.StatementListPageQuery;
import com.tigshop.bean.query.finance.StatementStatisticsQuery;
import com.tigshop.bean.vo.finance.statement.*;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.mapper.finance.StatementMapper;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.shop.ShopWithdrawMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.mapper.vendor.VendorWithdrawMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.order.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.tigshop.bean.enums.order.AftersalesStatusEnum.COMPLETE;

/**
 * 对账单服务实现类
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 10:17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatementServiceImpl extends BaseServiceImpl<StatementMapper, Statement> implements StatementService {

    private final MessageHelper messageHelper;
    private final com.tigshop.common.core.TranslatePackage translatePackage;
    private final OrderMapper orderMapper;
    private final ShopWithdrawMapper shopWithdrawMapper;
    private final ShopMapper shopMapper;
    private final VendorMapper vendorMapper;
    private final VendorWithdrawMapper vendorWithdrawMapper;
    private final OrderItemService orderItemService;
    private final RecordRateService recordRateService;
    private final AftersalesMapper aftersalesMapper;
    private final StatementOutputMapper statementOutputMapper;
    private final com.tigshop.service.salesman.SalesmanOrderService salesmanOrderService;

    @Override
    public Page<StatementListVO> getStatementList(StatementListPageQuery query) {
        Page<Statement> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Statement> queryWrapper = new LambdaQueryWrapper<>();

        // 查询订单编号或者支付流水号
        queryWrapper.like(query.getRecordSn() != null, Statement::getRecordSn, query.getRecordSn());

        // 查询入账/结算时间或下单时间
        String startDateTime = query.getStartDateTime();
        String endDateTime = query.getEndDateTime();
        if (startDateTime != null && endDateTime != null) {
            Long startTime = StringUtils.dateToTimestampExample(startDateTime);
            Long endTime = StringUtils.dateToTimestampExample(endDateTime);
            // 如果两个时间戳相等
            if (startTime.equals(endTime)) {
                LocalDate date = LocalDate.parse(startDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // 00:00:00
                startTime = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
                // 23:59:59
                endTime = date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            // 查询入账/结算时间
            // 查询的时间类型，1：入账（结算）时间2：下单时间
            Integer timeType = query.getTimeType();
            if (StatementTimeType.SETTLEMENT_TIME.getCode().equals(timeType)) {
                Long finalStartTime = startTime;
                Long finalEndTime = endTime;
                queryWrapper.and(wrapper ->
                        wrapper.ge(Statement::getSettlementTime, finalStartTime).le(Statement::getSettlementTime, finalEndTime));

                // 查询下单时间
            } else if (StatementTimeType.RECORD_TIME.getCode().equals(timeType)) {
                Long finalStartTime1 = startTime;
                Long finalEndTime1 = endTime;
                queryWrapper.and(wrapper ->
                        wrapper.ge(Statement::getRecordTime, finalStartTime1).le(Statement::getRecordTime, finalEndTime1));
            }
        }

        // 筛选账户类型
        queryWrapper.eq(query.getAccountType() != null, Statement::getAccountType, query.getAccountType());

        // 筛选类型
        queryWrapper.eq(query.getType() != null && query.getType() != 0, Statement::getType, query.getType());

        // 筛选支付方式
        queryWrapper.eq(query.getPaymentType() != null, Statement::getPaymentType, query.getPaymentType());


        Integer getShopId = getShopId() == null ? 0 : getShopId();
        Integer getVendorId = getVendorId() == null ? 0 : getVendorId();
        if (query.getSource() == null) {
            if ("shop".equals(getAdminType())) {
                // 筛选店铺id
                queryWrapper.eq(Statement::getShopId, getShopId);
            }
            if ("vendor".equals(getAdminType())) {
                // 筛选供应商id
                queryWrapper.eq(Statement::getVendorId, getVendorId);
            }
            if("admin".equals(getAdminType())){
                queryWrapper.eq(query.getShopId() != null, Statement::getShopId, query.getShopId());
            }
        } else if ("shop".equals(query.getSource())) {
            if (getShopId > 0) {
                queryWrapper.eq(Statement::getShopId, getShopId);
            } else {
                if("admin".equals(getAdminType())){
                    queryWrapper.eq(query.getShopId() != null, Statement::getShopId, query.getShopId());
                }
                queryWrapper.gt(Statement::getShopId, getShopId);
            }
        } else if ("vendor".equals(query.getSource())) {
            if (getVendorId > 0) {
                queryWrapper.eq(Statement::getVendorId, getVendorId);
            } else {
                queryWrapper.gt(Statement::getVendorId, getVendorId);
            }
            queryWrapper.eq(query.getShopId() != null, Statement::getShopId, query.getShopId());
        }

        // 排序
        this.buildSortOrder(page, query.getSortField(), query.getSortOrder());

        // 提交查询
        Page<Statement> statementPage = this.page(page, queryWrapper);
        List<StatementListVO> statementList = statementPage.getRecords().stream().map(statement -> {
            StatementListVO statementListVO = new StatementListVO();
            BeanUtils.copyProperties(statement, statementListVO);
            Integer shopId = statement.getShopId();
            if (shopId != null) {
                statementListVO.setShopName(Optional.ofNullable(shopMapper.selectById(shopId)).map(Shop::getShopTitle).orElse(""));
            }
            Integer vendorId = statement.getVendorId();
            if (vendorId != null) {
                statementListVO.setVendorName(Optional.ofNullable(vendorMapper.selectOnboardedById(vendorId)).map(Vendor::getVendorName).orElse(""));
            }
            statementListVO.setAccountTypeName(Objects.requireNonNull(AccountType.fromCode(statement.getAccountType())).getDescription());
            statementListVO.setEntryTypeName(Objects.requireNonNull(EntryType.fromCode(statement.getEntryType())).getDescription());
            PayMethodType typeDescription = PayMethodType.getTypeDescription(statement.getPaymentType());
            if (typeDescription == null) {
                statementListVO.setPaymentTypeName("");
                statementListVO.setPaymentType("");
            } else {
                String desc = typeDescription.getDescription();
                statementListVO.setPaymentTypeName(desc);
                statementListVO.setPaymentType(desc);
            }
            statementListVO.setTypeName(Objects.requireNonNull(StatementType.fromCode(statement.getType())).getDescription());
            statementListVO.setSettlementTime(TigUtils.handelTime(statement.getSettlementTime()));
            statementListVO.setRecordTime(TigUtils.handelTime(statement.getRecordTime()));
            return statementListVO;
        }).toList();
        return PageUtil.convertPage(statementPage, statementList);
    }

    public Statement buildStatement(StatementSaveParam param) {
        DateTime dt = DateTime.now();
        Statement statement = new Statement();
        statement.setAccountType(param.getAccountType());
        statement.setType(param.getType());
        statement.setEntryType(param.getEntryType());
        statement.setPaymentType(param.getPaymentType());
        statement.setGmtCreate(DateUtil.now());
        statement.setStatementYear(dt.year());
        statement.setStatementMonth(dt.month() + 1);
        statement.setStatementDay(dt.dayOfMonth());
        statement.setSettlementTime(StringUtils.getCurrentTime());
        // 分录生成即视为已入账；线上分账定时任务按该状态 + settlementTime 筛选到期单据
        statement.setSettlementStatus(SettlementStatus.SETTLED.getCode());
        return statement;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStatement(StatementSaveParam param) {
        // 验证关联的单据是否可用
        Integer recordId = param.getRecordId();
        Assert.notNull(recordId, () -> new GlobalException(messageHelper.get("error.statement.record.id.required")));
        Assert.isTrue(recordId > 0, () -> new GlobalException(messageHelper.get("error.statement.record.id.required")));

        // 验证对账单类型
        Integer type = param.getType();
        Assert.notNull(type, () -> new GlobalException(messageHelper.get("error.statement.type.required")));
        StatementType statementType = StatementType.fromCode(type);
        Assert.notNull(statementType, () -> new GlobalException(messageHelper.get("error.statement.type.unsupported", type)));

        // 通过对账单类型获取单据的账单信息
        switch (Objects.requireNonNull(statementType)) {
            // 获取订单的账单信息
            case ORDER -> getOrderStatementInfo(param);
            // 获取订单退款的账单信息
            case ORDER_REFUND -> getOrderRefundStatementInfo(param);
            // 获取店铺的提现账单信息
            case SHOP_WITHDRAWAL -> getShopStatementInfo(param);
            // 获取门店的提现账单信息，等O2O完成后通过shop的类型区分，这里暂时不用考虑
            case STORE_WITHDRAWAL -> getShopStatementInfo(param);
            // 获取供应商的账单信息
            case SUPPLIER_WITHDRAWAL -> getSupplierStatementInfo(param);
            // 实际上这个类型不会单独处理
            case HANDLING_FEE, SERVICE_FEE, DISTRIBUTION_COMMISSION -> {
            }
        }
    }

    /**
     * 查询服务费/手续费
     *
     * @param recordId   单据id
     * @param recordType 单价类型
     */
    @Override
    public RecordRate getRecordRate(int recordId, int recordType) {

        return recordRateService.lambdaQuery()
                .eq(RecordRate::getRecordId, recordId)
                .eq(RecordRate::getRecordType, recordType)
                .orderByDesc(RecordRate::getRecordRateId)
                .last("limit 1")
                .one();
    }

    @Override
    public List<StatementExportVO> exportStatement(StatementListPageQuery query) {
        query.setSize(9999);
        Page<StatementListVO> statementList = this.getStatementList(query);
        if (CollUtil.isEmpty(statementList.getRecords())){
            return Collections.emptyList();
        }
        ArrayList<StatementExportVO> statementExportVOS = new ArrayList<>();
        for (StatementListVO record : statementList.getRecords()) {
            StatementExportVO statementExportVO = new StatementExportVO();
            BeanUtils.copyProperties(record, statementExportVO);
            statementExportVO.setShopName(StrUtil.isEmpty(record.getShopName()) ? "暂无门店" : record.getShopName());
            statementExportVO.setVendorName(StrUtil.isEmpty(record.getVendorName()) ? "暂无供应商" : record.getVendorName());
            statementExportVO.setPaymentTypeName(StrUtil.isEmpty(record.getPaymentTypeName()) ? "其他" : record.getPaymentTypeName());
            statementExportVO.setAccountBalance(record.getAccountBalance() == null ?
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) :
                    record.getAccountBalance().setScale(2, RoundingMode.HALF_UP));

            statementExportVO.setAmount(record.getAmount() == null ?
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) :
                    record.getAmount().setScale(2, RoundingMode.HALF_UP));
            statementExportVOS.add(statementExportVO);
        }

        return statementExportVOS;
    }

    @Override
    public List<StatementStatisticsExportVO> exportStatementStatistics(StatementStatisticsQuery query) {
        String statementDateType = query.getStatementDateType();
        DateComponentType dateComponentType = DateComponentType.getType(statementDateType);
        Assert.notNull(dateComponentType, () -> new GlobalException("日期类型错误"));
        String statementDate = query.getStatementDate();
        LambdaQueryWrapper<Statement> baseWrapper = buildStatisticsWrapper(query);
        List<StatementStatisticsVO> statementStatisticsVOList = new ArrayList<>();
        switch (Objects.requireNonNull(dateComponentType)) {
            case DAY -> {
                String[] day = statementDate.split("-");
                Assert.isTrue(day.length == 3, () -> new GlobalException("日期格式错误"));
                LambdaQueryWrapper<Statement> wrapper = baseWrapper.clone()
                        .eq(Statement::getStatementYear, day[0])
                        .eq(Statement::getStatementMonth, statementDate.split("-")[1])
                        .eq(Statement::getStatementDay, statementDate.split("-")[2]);
                List<Statement> statementList = this.list(wrapper);

                AccountType accountType = AccountType.fromCode(query.getAccountType());
                if (accountType == null) {
                    accountType = AccountType.ACCOUNT_BALANCE;
                }
                StatementStatisticsVO build = StatementStatisticsVO.builder()
                        .statementDate(statementDate)
                        .income(this.getIncome(statementList))
                        .expenditure(this.getExpenditure(statementList))
                        .incomeCount(this.getIncomeCount(statementList))
                        .expenditureCount(this.getExpenditureCount(statementList))
                        .accountType(accountType.getCode())
                        .accountTypeText(accountType.getDescription()).build();

                statementStatisticsVOList.add(build);
            }
            case MONTH -> {
                String[] day = statementDate.split("-");
                Assert.isTrue(day.length == 2, "日期格式错误");
                LambdaQueryWrapper<Statement> wrapper = baseWrapper.clone()
                        .eq(Statement::getStatementYear, day[0])
                        .eq(Statement::getStatementMonth, statementDate.split("-")[1]);
                List<Statement> statementList = this.list(wrapper);

                AccountType accountType = AccountType.fromCode(query.getAccountType());
                if (accountType == null) {
                    accountType = AccountType.ACCOUNT_BALANCE;
                }
                StatementStatisticsVO build = StatementStatisticsVO.builder()
                        .statementDate(statementDate)
                        .income(this.getIncome(statementList))
                        .expenditure(this.getExpenditure(statementList))
                        .incomeCount(this.getIncomeCount(statementList))
                        .expenditureCount(this.getExpenditureCount(statementList))
                        .accountType(accountType.getCode())
                        .accountTypeText(accountType.getDescription()).build();

                statementStatisticsVOList.add(build);
            }
            case YEAR -> {
                LambdaQueryWrapper<Statement> wrapper = baseWrapper.clone()
                        .eq(Statement::getStatementYear, statementDate);
                List<Statement> statementList = this.list(wrapper);

                AccountType accountType = AccountType.fromCode(query.getAccountType());
                if (accountType == null) {
                    accountType = AccountType.ACCOUNT_BALANCE;
                }
                StatementStatisticsVO build = StatementStatisticsVO.builder()
                        .statementDate(statementDate)
                        .income(this.getIncome(statementList))
                        .expenditure(this.getExpenditure(statementList))
                        .incomeCount(this.getIncomeCount(statementList))
                        .expenditureCount(this.getExpenditureCount(statementList))
                        .accountType(accountType.getCode())
                        .accountTypeText(accountType.getDescription()).build();

                statementStatisticsVOList.add(build);
            }
        }
        return statementStatisticsVOList.stream().map(statementStatisticsVO -> {
            StatementStatisticsExportVO statementStatisticsExportVO = new StatementStatisticsExportVO();
            BeanUtils.copyProperties(statementStatisticsVO, statementStatisticsExportVO);
            return statementStatisticsExportVO;
        }).toList();
    }

    @Override
    public List<StatementStatisticsVO> getStatementStatisticsList(StatementStatisticsQuery query) {
        String statementDateType = query.getStatementDateType();
        DateComponentType dateComponentType = DateComponentType.getType(statementDateType);
        Assert.notNull(dateComponentType, () -> new GlobalException("日期类型错误"));
        String statementDate = query.getStatementDate();
        LambdaQueryWrapper<Statement> baseWrapper = buildStatisticsWrapper(query);
        List<StatementStatisticsVO> statementStatisticsVOList = new ArrayList<>();
        switch (Objects.requireNonNull(dateComponentType)) {
            case DAY -> {
                List<String> monthDays = TigUtils.getMonthDays(statementDate);
                for (String monthDay : monthDays) {
                    String[] day = monthDay.split("-");
                    LambdaQueryWrapper<Statement> wrapper = baseWrapper.clone()
                            .eq(Statement::getStatementYear, day[0])
                            .eq(Statement::getStatementMonth, monthDay.split("-")[1])
                            .eq(Statement::getStatementDay, monthDay.split("-")[2]);
                    List<Statement> statementList = this.list(wrapper);

                    AccountType accountType = AccountType.fromCode(query.getAccountType());
                    if (accountType == null) {
                        accountType = AccountType.ACCOUNT_BALANCE;
                    }
                    StatementStatisticsVO build = StatementStatisticsVO.builder()
                            .statementDate(monthDay)
                            .income(this.getIncome(statementList))
                            .expenditure(this.getExpenditure(statementList))
                            .incomeCount(this.getIncomeCount(statementList))
                            .expenditureCount(this.getExpenditureCount(statementList))
                            .accountType(accountType.getCode())
                            .accountTypeText(accountType.getDescription()).build();

                    statementStatisticsVOList.add(build);
                }
            }
            case MONTH -> {
                List<String> monthByYear = TigUtils.getMonthByYear(statementDate);

                for (String month : monthByYear) {
                    String[] day = month.split("-");
                    LambdaQueryWrapper<Statement> wrapper = baseWrapper.clone()
                            .eq(Statement::getStatementYear, day[0])
                            .eq(Statement::getStatementMonth, month.split("-")[1]);
                    List<Statement> statementList = this.list(wrapper);

                    AccountType accountType = AccountType.fromCode(query.getAccountType());
                    if (accountType == null) {
                        accountType = AccountType.ACCOUNT_BALANCE;
                    }
                    StatementStatisticsVO build = StatementStatisticsVO.builder()
                            .statementDate(month)
                            .income(this.getIncome(statementList))
                            .expenditure(this.getExpenditure(statementList))
                            .incomeCount(this.getIncomeCount(statementList))
                            .expenditureCount(this.getExpenditureCount(statementList))
                            .accountType(accountType.getCode())
                            .accountTypeText(accountType.getDescription()).build();

                    statementStatisticsVOList.add(build);
                }
            }
            case YEAR -> {
                LambdaQueryWrapper<Statement> wrapper = baseWrapper.clone()
                        .eq(Statement::getStatementYear, statementDate);
                List<Statement> statementList = this.list(wrapper);

                AccountType accountType = AccountType.fromCode(query.getAccountType());
                if (accountType == null) {
                    accountType = AccountType.ACCOUNT_BALANCE;
                }
                StatementStatisticsVO build = StatementStatisticsVO.builder()
                        .statementDate(statementDate)
                        .income(this.getIncome(statementList))
                        .expenditure(this.getExpenditure(statementList))
                        .incomeCount(this.getIncomeCount(statementList))
                        .expenditureCount(this.getExpenditureCount(statementList))
                        .accountType(accountType.getCode())
                        .accountTypeText(accountType.getDescription()).build();

                statementStatisticsVOList.add(build);
            }
        }
        Collections.reverse(statementStatisticsVOList);
        return statementStatisticsVOList;
    }

    @Override
    public StatementQueryContentVO getStatementQueryContent() {
        // 将枚举转换为 List 集合
        List<Map<String, Object>> statementTypeList = Arrays.stream(StatementType.values()).toList().stream().map(type -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("description", translatePackage.translate(type.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
            return map;
        }).toList();

        List<Map<String, Object>> statementTimeList = Arrays.stream(StatementTimeType.values()).toList().stream().map(type -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("description", translatePackage.translate(type.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
            return map;
        }).toList();
        List<Map<String, Object>> accountTypeList = Arrays.stream(AccountType.values()).toList().stream().map(type -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("description", translatePackage.translate(type.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
            return map;
        }).toList();
        List<Map<String, Object>> entryTypeList = Arrays.stream(EntryType.values()).toList().stream().map(type -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("description", translatePackage.translate(type.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
            return map;
        }).toList();
        List<Map<String, Object>> payMethodTypeList = Arrays.stream(PayMethodType.values()).toList().stream().map(type -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("description", translatePackage.translate(type.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
            return map;
        }).toList();
        List<Map<String, Object>> dateComponentTypeList = Arrays.stream(DateComponentType.values()).toList().stream().map(type -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("description", translatePackage.translate(type.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
            return map;
        }).toList();


        StatementQueryContentVO vo = new StatementQueryContentVO();
        vo.setStatementType(statementTypeList);
        vo.setStatementTimeType(statementTimeList);
        vo.setAccountType(accountTypeList);
        vo.setEntryType(entryTypeList);
        vo.setPayMethodType(payMethodTypeList);
        vo.setDateComponentType(dateComponentTypeList);

        return vo;
    }

    /**
     * 构建统计查询的过滤条件（按 source / 身份 / accountType 过滤）
     */
    private LambdaQueryWrapper<Statement> buildStatisticsWrapper(StatementStatisticsQuery query) {
        LambdaQueryWrapper<Statement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getAccountType() != null, Statement::getAccountType, query.getAccountType());

        Integer headerShopId = getShopId() == null ? 0 : getShopId();
        Integer headerVendorId = getVendorId() == null ? 0 : getVendorId();

        if (query.getSource() == null) {
            if ("shop".equals(getAdminType())) {
                wrapper.eq(Statement::getShopId, headerShopId);
            }
            if ("vendor".equals(getAdminType())) {
                wrapper.eq(Statement::getVendorId, headerVendorId);
            }
            if ("admin".equals(getAdminType())) {
                wrapper.eq(query.getShopId() != null, Statement::getShopId, query.getShopId());
            }
        } else if ("shop".equals(query.getSource())) {
            if (headerShopId > 0) {
                wrapper.eq(Statement::getShopId, headerShopId);
            } else {
                if ("admin".equals(getAdminType())) {
                    wrapper.eq(query.getShopId() != null, Statement::getShopId, query.getShopId());
                }
                wrapper.gt(Statement::getShopId, headerShopId);
            }
        } else if ("vendor".equals(query.getSource())) {
            if (headerVendorId > 0) {
                wrapper.eq(Statement::getVendorId, headerVendorId);
            } else {
                wrapper.gt(Statement::getVendorId, headerVendorId);
            }
            wrapper.eq(query.getShopId() != null, Statement::getShopId, query.getShopId());
        }
        return wrapper;
    }

    /**
     * 获取支出金额
     *
     * @param statementList 列表
     * @return BigDecimal
     */
    private BigDecimal getExpenditure(List<Statement> statementList) {
        return statementList.stream()
                .filter(s -> s.getAmount() != null && s.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(Statement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();
    }

    /**
     * 获取收入金额
     *
     * @param statementList 列表
     * @return BigDecimal
     */
    private BigDecimal getIncome(List<Statement> statementList) {
        return statementList.stream()
                .filter(s -> s.getAmount() != null && s.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(Statement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取收入笔数
     *
     * @param statementList 列表
     * @return 数量
     */
    private Long getIncomeCount(List<Statement> statementList) {
        return statementList.stream()
                .filter(s -> s.getAmount() != null && s.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .count();
    }

    /**
     * 获取支出笔数
     *
     * @param statementList 列表
     * @return 数量
     */
    private Long getExpenditureCount(List<Statement> statementList) {
        return statementList.stream()
                .filter(s -> s.getAmount() != null && s.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .count();
    }

    /**
     * 获取供应商的账单信息
     *
     * @param param Statement
     */
    private void getSupplierStatementInfo(StatementSaveParam param) {
        Integer recordId = param.getRecordId();
        Vendor vendor = vendorMapper.selectOnboardedById(param.getVendorId());
        VendorWithdraw vendorWithdraw = vendorWithdrawMapper.selectById(recordId);
        Assert.notNull(vendorWithdraw, () -> new GlobalException(messageHelper.get("error.statement.withdraw.not.exist")));

        Assert.notNull(vendor, () -> new GlobalException(messageHelper.get("error.statement.vendor.not.exist")));

        Statement statement = buildStatement(param);
        statement.setRecordId(recordId);
        statement.setRecordSn(vendorWithdraw.getWithdrawSn());
        statement.setRecordTime(vendorWithdraw.getAddTime());
        statement.setShopId(null);
        statement.setVendorId(vendor.getVendorId());
        BigDecimal vendorMoney = vendor.getVendorMoney();
        RecordRate recordRate = this.getRecordRate(recordId, StatementType.SUPPLIER_WITHDRAWAL.getCode());
        // 供应商提现减去手续费
        BigDecimal serviceFee = vendorWithdraw.getAmount().multiply(recordRate.getSupplierWithdrawalFee())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);


        statement.setAccountBalance(vendorMoney.add(serviceFee));
        statement.setAmount(vendorWithdraw.getAmount().subtract(serviceFee).negate());
        statement.setType(StatementType.SUPPLIER_WITHDRAWAL.getCode());
        this.save(statement);

        Statement statement1 = BeanUtil.copyProperties(statement, Statement.class);
        statement1.setStatementId(null);
        statement1.setAccountBalance(vendorMoney);
        statement1.setAmount(serviceFee.negate());
        statement1.setType(StatementType.HANDLING_FEE.getCode());
        this.save(statement1);
    }

    /**
     * 获取店铺的账单信息
     *
     * @param param Statement
     */
    private void getShopStatementInfo(StatementSaveParam param) {
        Integer recordId = param.getRecordId();
        Shop shop = shopMapper.selectById(param.getShopId());
        Assert.notNull(shop, () -> new GlobalException(messageHelper.get("error.statement.shop.not.exist")));
        ShopWithdraw shopWithdraw = shopWithdrawMapper.selectById(recordId);
        Assert.notNull(shopWithdraw, () -> new GlobalException(messageHelper.get("error.statement.withdraw.not.exist")));

        // 店铺提现减去金额
        Statement statement = buildStatement(param);
        statement.setRecordId(recordId);
        statement.setRecordSn(shopWithdraw.getWithdrawSn());
        statement.setRecordTime(shopWithdraw.getAddTime());
        statement.setShopId(shop.getShopId());
        statement.setVendorId(null);

        BigDecimal shopMoney = shop.getShopMoney();
        RecordRate recordRate = this.getRecordRate(recordId, StatementType.SHOP_WITHDRAWAL.getCode());
        // 店铺提现减去手续费
        BigDecimal serviceFee = shopWithdraw.getAmount().multiply(recordRate.getSupplierWithdrawalFee())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 减去提现金额后
        statement.setAccountBalance(shopMoney.add(serviceFee));
        statement.setAmount(shopWithdraw.getAmount().subtract(serviceFee).negate());
        statement.setType(StatementType.SHOP_WITHDRAWAL.getCode());
        this.save(statement);

        Statement statement1 = BeanUtil.copyProperties(statement, Statement.class);

        statement1.setStatementId(null);
        statement1.setAccountBalance(shopMoney);
        statement1.setAmount(serviceFee.negate());
        statement1.setType(StatementType.HANDLING_FEE.getCode());
        this.save(statement1);
    }

    /**
     * 获取订单退款的账单信息
     *
     * @param param 账单参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void getOrderRefundStatementInfo(StatementSaveParam param) {
        Statement statement = buildStatement(param);
        Integer recordId = param.getRecordId();
        Aftersales aftersales = aftersalesMapper.selectById(recordId);
        Assert.notNull(aftersales, () -> new GlobalException(messageHelper.get("error.statement.order.not.exist")));
        statement.setRecordId(recordId);
        statement.setRecordSn(aftersales.getAftersalesSn());
        statement.setRecordTime(aftersales.getAddTime());
        RecordRate recordRate = this.getRecordRate(aftersales.getOrderId(), StatementType.ORDER.getCode());
        if (recordRate == null) {
            return;
        }
        BigDecimal shopServiceFee = recordRate.getShopServiceFee();
        if (shopServiceFee == null) {
            shopServiceFee = BigDecimal.ZERO;
        }
        if (aftersales.getVendorId() != null && aftersales.getVendorId() > 0) {
            BigDecimal totalSupplyPrice = aftersalesMapper.selectTotalAmount(recordId, COMPLETE.getCode());
            BigDecimal refundAmount = aftersales.getRefundAmount().subtract(totalSupplyPrice);

            Shop shop = shopMapper.selectById(aftersales.getShopId());
            BigDecimal accountBalanceShop = Optional.ofNullable(shop).map(Shop::getShopMoney).orElse(BigDecimal.ZERO);
            // 店铺获利退
            saveStatement(statement, refundAmount.negate(), aftersales.getShopId(), 0, StatementType.ORDER_REFUND, accountBalanceShop);

            Vendor vendor = vendorMapper.selectOnboardedById(aftersales.getVendorId());
            // 原本的余额
            BigDecimal accountBalanceVendor = vendor.getVendorMoney();
            // 供应商增加供货价
            saveStatement(statement, totalSupplyPrice.negate(), 0, aftersales.getVendorId(), StatementType.ORDER_REFUND, accountBalanceVendor);

            // 店铺服务费
            BigDecimal shopFee = calcServiceFee(refundAmount, shopServiceFee);
            BigDecimal newAccountBalanceShop = accountBalanceShop.subtract(refundAmount);
            saveStatement(statement, shopFee, aftersales.getShopId(), 0, StatementType.SERVICE_FEE, newAccountBalanceShop);

            // 供应商服务费
            BigDecimal supplierFee = calcServiceFee(totalSupplyPrice, recordRate.getSupplierServiceFee());
            BigDecimal newAccountBalanceVendor = accountBalanceVendor.subtract(totalSupplyPrice);
            saveStatement(statement, supplierFee, 0, aftersales.getVendorId(), StatementType.SERVICE_FEE, newAccountBalanceVendor);
        } else {
            BigDecimal refundAmount = aftersales.getRefundAmount();
            Shop shop = shopMapper.selectById(aftersales.getShopId());
            BigDecimal accountBalanceShop = shop.getShopMoney();
            // 店铺获利退款
            saveStatement(statement, refundAmount.negate(), aftersales.getShopId(), 0, StatementType.ORDER_REFUND, accountBalanceShop);

            // 服务费
            BigDecimal shopFee = calcServiceFee(refundAmount, shopServiceFee);
            BigDecimal newAccountBalanceShop = accountBalanceShop.subtract(refundAmount);
            saveStatement(statement, shopFee, aftersales.getShopId(), 0, StatementType.SERVICE_FEE, newAccountBalanceShop);
        }
    }

    /**
     * 获取订单的账单信息
     *
     * @param param Statement.StatementBuilder
     */
    @Transactional(rollbackFor = Exception.class)
    public void getOrderStatementInfo(StatementSaveParam param) {
        Statement baseStatement = buildStatement(param);

        Integer recordId = param.getRecordId();
        Order order = orderMapper.selectById(recordId);
        Assert.notNull(order, () -> new GlobalException(messageHelper.get("error.statement.order.not.exist")));

        baseStatement.setRecordId(recordId);
        baseStatement.setRecordSn(order.getOrderSn());
        baseStatement.setRecordTime(order.getAddTime());

        RecordRate recordRate = this.getRecordRate(order.getOrderId(), StatementType.ORDER.getCode());

        if (recordRate == null) {
            return;
        }

        int vendorId = order.getVendorId() == null ? 0 : order.getVendorId();
        if (vendorId > 0) {
            handleVendorOrder(order, baseStatement, recordRate);
        } else if (order.getShopId() > 0) {
            handleShopOrder(order, baseStatement, recordRate);
        }
    }

    /**
     * 处理供应商订单逻辑
     */
    private void handleVendorOrder(Order order, Statement baseStatement, RecordRate recordRate) {
        // 获取退款信息
        List<Aftersales> aftersales = aftersalesMapper.selectList(Wrappers.lambdaQuery(Aftersales.class)
                .eq(Aftersales::getOrderId, order.getOrderId())
                .eq(Aftersales::getStatus, COMPLETE.getCode()));

        BigDecimal vendorKOrder = BigDecimal.ZERO;
        BigDecimal shopKOrder = BigDecimal.ZERO;

        BigDecimal vendorKFee = BigDecimal.ZERO;
        BigDecimal shopKFee = BigDecimal.ZERO;
        for (Aftersales aftersale : aftersales) {
            // 排查是否在订单完成前有退货
            List<StatementOutput> statementOutputs = statementOutputMapper.selectList(Wrappers.lambdaQuery(StatementOutput.class)
                    .eq(StatementOutput::getRecordId, aftersale.getAftersaleId())
                    .eq(StatementOutput::getRecordType, StatementType.ORDER_REFUND.getCode()));

            for (StatementOutput statementOutput : statementOutputs) {
                if (statementOutput.getVendorId() > 0) {
                    vendorKOrder = vendorKOrder.add(statementOutput.getExpenditure());
                }
                if (statementOutput.getShopId() > 0) {
                    shopKOrder = shopKOrder.add(statementOutput.getExpenditure());
                }
            }

            List<StatementOutput> serviceStatementOutputs = statementOutputMapper.selectList(Wrappers.lambdaQuery(StatementOutput.class)
                    .eq(StatementOutput::getRecordId, aftersale.getAftersaleId())
                    .eq(StatementOutput::getRecordType, StatementType.SERVICE_FEE.getCode()));

            for (StatementOutput statementOutput : serviceStatementOutputs) {
                if (statementOutput.getVendorId() > 0) {
                    vendorKFee = vendorKFee.add(statementOutput.getIncome());
                }
                if (statementOutput.getShopId() > 0) {
                    shopKFee = shopKFee.add(statementOutput.getIncome());
                }
            }
        }

        // 供应商的钱
        BigDecimal totalSupplyPrice = calculateSupplyPrice(order.getOrderId());
        // 店铺的钱
        BigDecimal shopPaidAmount = order.getPaidAmount().subtract(totalSupplyPrice);

        if (order.getShopId() > 0) {
            Shop shop = shopMapper.selectById(order.getShopId());
            BigDecimal accountBalanceShop = shop.getShopMoney();
            saveStatement(baseStatement, shopPaidAmount.subtract(shopKOrder), order.getShopId(), 0, StatementType.ORDER, accountBalanceShop);
            if (shopPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 店铺服务费
                BigDecimal shopFee = calcServiceFee(shopPaidAmount, recordRate.getShopServiceFee()).subtract(shopKFee);
                // 新的余额
                BigDecimal newAccountBalanceShop = accountBalanceShop.add(shopPaidAmount);
                saveStatement(baseStatement, shopFee.negate(), order.getShopId(), 0, StatementType.SERVICE_FEE, newAccountBalanceShop);

                // 分销佣金扣减
                BigDecimal commission = salesmanOrderService.getTotalCommissionByOrderId(order.getOrderId());
                if (commission.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal afterFeeBalance = newAccountBalanceShop.subtract(shopFee);
                    saveStatement(baseStatement, commission.negate(), order.getShopId(), 0,
                            StatementType.DISTRIBUTION_COMMISSION, afterFeeBalance);
                }
            }
        }
        if (order.getVendorId() > 0) {
            // 供应商增加供货价
            Vendor vendor = vendorMapper.selectOnboardedById(order.getVendorId());
            // 原本的余额
            BigDecimal accountBalanceVendor = vendor.getVendorMoney();
            saveStatement(baseStatement, totalSupplyPrice.subtract(vendorKOrder), 0, order.getVendorId(), StatementType.ORDER, accountBalanceVendor);

            // 供应商服务费
            BigDecimal supplierFee = calcServiceFee(totalSupplyPrice, recordRate.getSupplierServiceFee()).subtract(vendorKFee);
            BigDecimal newAccountBalanceVendor = accountBalanceVendor.add(totalSupplyPrice);
            saveStatement(baseStatement, supplierFee.negate(), 0, order.getVendorId(), StatementType.SERVICE_FEE, newAccountBalanceVendor);
        }
    }

    /**
     * 处理普通店铺订单逻辑
     */
    private void handleShopOrder(Order order, Statement baseStatement, RecordRate recordRate) {
        BigDecimal paidAmount = order.getPaidAmount();
        Shop shop = shopMapper.selectById(order.getShopId());
        BigDecimal accountBalanceShop = shop.getShopMoney();
        // 店铺获利
        saveStatement(baseStatement, paidAmount, order.getShopId(), 0, StatementType.ORDER, accountBalanceShop);

        // 服务费
        BigDecimal newAccountBalanceShop = accountBalanceShop.add(paidAmount);
        BigDecimal shopFee = calcServiceFee(paidAmount, recordRate.getShopServiceFee());
        saveStatement(baseStatement, shopFee.negate(), order.getShopId(), 0, StatementType.SERVICE_FEE, newAccountBalanceShop);

        // 分销佣金扣减
        BigDecimal commission = salesmanOrderService.getTotalCommissionByOrderId(order.getOrderId());
        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal afterFeeBalance = newAccountBalanceShop.subtract(shopFee);
            saveStatement(baseStatement, commission.negate(), order.getShopId(), 0,
                    StatementType.DISTRIBUTION_COMMISSION, afterFeeBalance);
        }
    }

    /**
     * 计算订单供货价
     */
    private BigDecimal calculateSupplyPrice(Integer orderId) {
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );

        return items.stream()
                .map(item -> item.getVendorProductSupplyPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算服务费
     */
    private BigDecimal calcServiceFee(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 保存 Statement
     */
    private void saveStatement(Statement base, BigDecimal amount, Integer shopId, Integer vendorId, StatementType type, BigDecimal accountBalance) {
        if (vendorId > 0) {
            // 新的余额
            BigDecimal newAccountBalance = accountBalance.add(amount);
            Statement statement = new Statement();
            BeanUtils.copyProperties(base, statement);
            statement.setStatementId(null);
            statement.setAmount(amount);
            statement.setShopId(shopId);
            statement.setVendorId(vendorId);
            statement.setType(type.getCode());
            statement.setAccountBalance(newAccountBalance);
            this.save(statement);
        }
        if (shopId > 0) {
            BigDecimal newAccountBalance = accountBalance.add(amount);
            Statement statement = new Statement();
            BeanUtils.copyProperties(base, statement);
            statement.setStatementId(null);
            statement.setAmount(amount);
            statement.setShopId(shopId);
            statement.setVendorId(vendorId);
            statement.setType(type.getCode());
            statement.setAccountBalance(newAccountBalance);
            this.save(statement);
        }
    }


}
