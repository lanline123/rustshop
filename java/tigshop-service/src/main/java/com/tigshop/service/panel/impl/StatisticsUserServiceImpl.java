package com.tigshop.service.panel.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.panel.AddUserExportDTO;
import com.tigshop.bean.dto.panel.UserConsumptionRankingDTO;
import com.tigshop.bean.dto.panel.UserStatisticsPanelDTO;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.query.panel.UserTrendsQuery;
import com.tigshop.bean.vo.panel.AxisVO;
import com.tigshop.bean.vo.panel.UserConsumptionRankingVO;
import com.tigshop.bean.vo.panel.UserStatisticsPanelVO;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.log.StatisticsBaseService;
import com.tigshop.service.panel.StatisticsUserService;
import com.tigshop.service.salesman.OverviewService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 访问
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@RequiredArgsConstructor
@Service
public class StatisticsUserServiceImpl implements StatisticsUserService {

    private final OverviewService overviewService;

    private final UserMapper userMapper;

    private final StatisticsBaseService statisticsBaseService;

    private final OrderMapper orderMapper;

    private final UserRechargeOrderMapper userRechargeOrderMapper;

    @Override
    public UserStatisticsPanelVO getUserStatisticsPanel(UserStatisticsPanelDTO userStatisticsPanelDTO, HttpServletResponse response) {

        String[] startEndTime = new String[]{userStatisticsPanelDTO.getStartTime(), userStatisticsPanelDTO.getEndTime()};

        // 获取环比时间区间
        String[] prevDate = StringUtils.getPrevDate(startEndTime, 4);
        //访客数
        // 本期访问量
        Integer visitNum = statisticsBaseService.getVisitNum(startEndTime, 0, 0, 0);
        //上期访问量
        Integer prevVisitNum = statisticsBaseService.getVisitNum(prevDate, 0, 0, 0);
        //环比增长率
        BigDecimal visitGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(prevVisitNum), BigDecimal.valueOf(visitNum));

        //浏览量
        Integer viewNum = statisticsBaseService.getVisitNum(startEndTime, 1, 0, 0);
        Integer prevViewNum = statisticsBaseService.getVisitNum(prevDate, 1, 0, 0);
        BigDecimal viewGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(prevViewNum), BigDecimal.valueOf(viewNum));

        //新增用户数
        Long addUserNum = userMapper.selectCount(new QueryWrapper<User>().between("reg_time", TigUtils.toTimestampYmdHms(startEndTime[0] + " 00:00:00"), TigUtils.toTimestampYmdHms(startEndTime[1] + " 23:59:59")));
        Long prevAddUserNum = userMapper.selectCount(new QueryWrapper<User>().between("reg_time", TigUtils.toTimestampYmdHms(prevDate[0] + " 00:00:00"), TigUtils.toTimestampYmdHms(prevDate[1] + " 23:59:59")));
        BigDecimal addUserGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(prevAddUserNum), BigDecimal.valueOf(addUserNum));

        //成交用户数
        Long dealUserNum = orderMapper.selectCount(new QueryWrapper<Order>().eq(userStatisticsPanelDTO.getShopId() > 0, "shop_id", userStatisticsPanelDTO.getShopId()).ge("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", TigUtils.toTimestampYmdHms(startEndTime[0] + " 00:00:00"), TigUtils.toTimestampYmdHms(startEndTime[1] + " 23:59:59"))
                .select("distinct user_id")
        );
        Long prevDealUserNum = orderMapper.selectCount(new QueryWrapper<Order>().eq(userStatisticsPanelDTO.getShopId() > 0, "shop_id", userStatisticsPanelDTO.getShopId()).ge("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", TigUtils.toTimestampYmdHms(prevDate[0] + " 00:00:00"), TigUtils.toTimestampYmdHms(prevDate[1] + " 23:59:59"))
                .select("distinct user_id")
        );
        BigDecimal dealUserGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(dealUserNum), BigDecimal.valueOf(prevDealUserNum));

        //充值用户数
        Long rechargeUserNum = userRechargeOrderMapper.selectCount(new QueryWrapper<UserRechargeOrder>().between("add_time", TigUtils.toTimestampYmdHms(startEndTime[0] + " 00:00:00"), TigUtils.toTimestampYmdHms(startEndTime[1] + " 23:59:59"))
                .select("distinct user_id")
        );
        Long prevRechargeUserNum = userRechargeOrderMapper.selectCount(new QueryWrapper<UserRechargeOrder>().between("add_time", TigUtils.toTimestampYmdHms(prevDate[0] + " 00:00:00"), TigUtils.toTimestampYmdHms(prevDate[1] + " 23:59:59"))
                .select("distinct user_id")
        );
        BigDecimal rechargeUserGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(rechargeUserNum), BigDecimal.valueOf(prevRechargeUserNum));

        //访客-支付转化率
        BigDecimal visitToUser = BigDecimal.ZERO;
        if (!ObjectUtils.isEmpty(viewNum) && !ObjectUtils.isEmpty(addUserNum) && viewNum != 0) {
            visitToUser = TigUtils.toDecimalConvert(BigDecimal.valueOf(addUserNum).divide(BigDecimal.valueOf(viewNum), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }
        BigDecimal prevVisitToUser = BigDecimal.ZERO;
        if (!ObjectUtils.isEmpty(prevViewNum) && !ObjectUtils.isEmpty(prevAddUserNum) && prevViewNum != 0) {
            prevVisitToUser = TigUtils.toDecimalConvert(BigDecimal.valueOf(prevAddUserNum).divide(BigDecimal.valueOf(prevViewNum), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }
        BigDecimal visitToUserRate = prevVisitToUser.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : TigUtils.getGrowthRate(prevVisitToUser, visitToUser);
        UserStatisticsPanelVO userStatisticsPanelVO = new UserStatisticsPanelVO();
        userStatisticsPanelVO.setVisitNum(visitNum);
        userStatisticsPanelVO.setVisitGrowthRate(visitGrowthRate);
        userStatisticsPanelVO.setViewNum(viewNum);
        userStatisticsPanelVO.setViewGrowthRate(viewGrowthRate);
        userStatisticsPanelVO.setAddUserNum(addUserNum);
        userStatisticsPanelVO.setAddUserGrowthRate(addUserGrowthRate);
        userStatisticsPanelVO.setDealUserNum(dealUserNum);
        userStatisticsPanelVO.setDealUserGrowthRate(dealUserGrowthRate);
        userStatisticsPanelVO.setVisitToUser(visitToUser);
        userStatisticsPanelVO.setVisitToUserRate(visitToUserRate);
        userStatisticsPanelVO.setRechargeUserNum(rechargeUserNum);
        userStatisticsPanelVO.setRechargeUserGrowthRate(rechargeUserGrowthRate);

        if (userStatisticsPanelDTO.getIsExport() != null && userStatisticsPanelDTO.getIsExport() == 1) {
            ExcelUtils<UserStatisticsPanelVO> excelUtils = new ExcelUtils<>(List.of(userStatisticsPanelVO), UserStatisticsPanelVO.class);
            excelUtils.exportExcelWeb(response);
        }
        return userStatisticsPanelVO;
    }

    @Override
    public AxisVO getAddUserTrends(UserTrendsQuery query, HttpServletResponse response) {

        Long[] startEndTime = query.transStartEndTime();
        List<User> regTimeList = userMapper.selectList(
                new LambdaQueryWrapper<User>().between(User::getRegTime, startEndTime[0], startEndTime[1]).orderByAsc(User::getRegTime)
        );

        String[] startEndTimeStr = query.transStartEndTimeStr();
        List<String> horizontalAxis = overviewService.getHorizontalAxis(query.getDateType(), startEndTimeStr[0], startEndTimeStr[1]);

        List<JSONObject> regTimeJson = regTimeList.stream().map(user -> JSON.parseObject(JSON.toJSONString(user))).toList();
        List<BigDecimal> longitudinalAxis = overviewService.getLongitudinalAxis(horizontalAxis, regTimeJson, query.getDateType(), 1);

        if (query.getIsExport() != null && query.getIsExport() == 1) {
            List<AddUserExportDTO> addUserExportDtos = new ArrayList<>();
            for (int i = 0; i < horizontalAxis.size(); i++) {
                AddUserExportDTO addUserExportDTO = new AddUserExportDTO();
                addUserExportDTO.setDate(horizontalAxis.get(i));
                addUserExportDTO.setCount(longitudinalAxis.get(i));
                addUserExportDtos.add(addUserExportDTO);
            }
            ExcelUtils<AddUserExportDTO> excelUtils = new ExcelUtils<>(addUserExportDtos, AddUserExportDTO.class);
            excelUtils.exportExcelWeb(response);
        }

        AxisVO addUserTrendsVO = new AxisVO();
        addUserTrendsVO.setHorizontalAxis(horizontalAxis);
        addUserTrendsVO.setLongitudinalAxis(longitudinalAxis);
        return addUserTrendsVO;
    }

    @Override
    public Page<UserConsumptionRankingVO> getUserConsumptionRanking(UserConsumptionRankingDTO userConsumptionRankingDTO, HttpServletResponse response) {
        MPJLambdaWrapper<Order> list = new MPJLambdaWrapper<Order>().leftJoin(User.class, User::getUserId, Order::getUserId)
                .select("t1.username,t1.mobile,COUNT(t.order_id) as order_num,SUM(t.total_amount) AS order_amount")
                .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
                .eq(userConsumptionRankingDTO.getShopId() != null, Order::getShopId, userConsumptionRankingDTO.getShopId())
                .between(StrUtil.isNotBlank(userConsumptionRankingDTO.getStartTime()) && StrUtil.isNotBlank(userConsumptionRankingDTO.getStartTime()), Order::getAddTime, TigUtils.toTimestampYmd(userConsumptionRankingDTO.getStartTime()), TigUtils.toTimestampYmd(userConsumptionRankingDTO.getEndTime()));
        if (StrUtil.isNotBlank(userConsumptionRankingDTO.getKeyword())) {
            list.and(i -> i.like(User::getUsername, userConsumptionRankingDTO.getKeyword()).or().like(User::getMobile, userConsumptionRankingDTO.getKeyword()));
        }
        //排序字段映射
        String sortField = userConsumptionRankingDTO.getSortField();
        if(StringUtils.isNotBlank(sortField)){
            if(Objects.equals(sortField,"orderNum") || Objects.equals(sortField,"order_num")){
                sortField = "order_num";
            }else if(Objects.equals(sortField,"orderAmount") || Objects.equals(sortField,"order_amount")){
                sortField = "order_amount";
            }
        }
        list.groupBy("t1.user_id").orderBy(StrUtil.isNotBlank(sortField), Objects.equals(userConsumptionRankingDTO.getSortOrder(), "asc"), sortField);
        Page<UserConsumptionRankingVO> page = new Page<>(userConsumptionRankingDTO.getPage(), userConsumptionRankingDTO.getSize());
        Page<UserConsumptionRankingVO> userConsumptionRankingPage = orderMapper.selectJoinPage(page, UserConsumptionRankingVO.class, list);
        if (userConsumptionRankingDTO.getIsExport() != null && userConsumptionRankingDTO.getIsExport() == 1) {
            ExcelUtils<UserConsumptionRankingVO> excelUtils = new ExcelUtils<>(userConsumptionRankingPage.getRecords(), UserConsumptionRankingVO.class);
            excelUtils.exportExcelWeb(response);
        }
        return userConsumptionRankingPage;
    }
}
