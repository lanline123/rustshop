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
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.*;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.bean.enums.user.RankGrowthLogTypeEnum;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.user.*;
import com.tigshop.bean.query.user.UserRankEditParam;
import com.tigshop.bean.vo.user.*;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.user.UserGrowthPointsLogMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.mapper.user.UserRankMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.RankGrowthLogService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;
import static com.tigshop.common.constant.user.UserRankConstants.RANK_NAME_EXISTS;

/**
 * 会员等级服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class UserRankServiceImpl extends BaseServiceImpl<UserRankMapper, UserRank> implements UserRankService {

    private final TigshopProperties tigshopProperties;

    private final UserRankConfigMapper userRankConfigMapper;

    private final UserMapper userMapper;

    private final RankGrowthLogService rankGrowthLogService;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    private final RefundApplyMapper refundApplyMapper;

    private final AftersalesItemMapper aftersalesItemMapper;

    private final UserGrowthPointsLogMapper userGrowthPointsLogMapper;

    private final RabbitTemplate rabbitTemplate;

    //具体业务逻辑
    @Override
    public Page<UserRankListVO> list(UserRankListDTO dto) {
        //分页参数
        Page<UserRank> page = new Page<>(dto.getPage(), dto.getSize());
        // 创建查询构造器
        LambdaQueryWrapper<UserRank> queryWrapper = getUserRankQueryWrapper(dto);

        if (ObjectUtil.isEmpty(dto.getSortOrder())) {
            dto.setSortOrder("asc");
        }
        // 排序字段
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        if ((dto.getRankType() != null && dto.getRankType() > 0)) {
            queryWrapper.eq(UserRank::getRankType, dto.getRankType());
        }
        // 分页查询
        Page<UserRank> userRankBoPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserRank> userRankBoList = userRankBoPage.getRecords();
        List<UserRankListVO> userRankListVOList = new ArrayList<>();
        if (!userRankBoList.isEmpty()) {
            userRankListVOList = userRankBoList.stream().map(userRankBo -> {
                UserRankListVO userRankListVO = toUserRankListVO(userRankBo);
                // 查询用户数量
                long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                        .eq(User::getRankId, userRankBo.getRankId()));
                userRankListVO.setUserCount(Math.toIntExact(userCount));
                return userRankListVO;
            }).toList();
        } else {
            //获取默认配置
            UserRankListInitDTO userRankListInitDTO = getUserRankListInitDTO();
            UserRankListVO userRankListVO = new UserRankListVO();
            BeanUtils.copyProperties(userRankListInitDTO, userRankListVO);
            userRankListVOList.add(userRankListVO);
        }
        return PageUtil.convertPage(userRankBoPage, userRankListVOList);
    }

    @Override
    public Page<UserRankNotProListVO> listNotPro(UserRankListDTO dto) {
        //分页参数
        Page<UserRank> page = new Page<>(dto.getPage(), dto.getSize());
        // 创建查询构造器
        LambdaQueryWrapper<UserRank> queryWrapper = getUserRankQueryWrapper(dto);

        // 排序字段
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        // 分页查询
        Page<UserRank> userRankBoPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserRank> userRankBoList = userRankBoPage.getRecords();
        List<UserRankNotProListVO> userRankNotProListVOList = new ArrayList<>();
        if (!userRankBoList.isEmpty()) {
            userRankNotProListVOList = userRankBoList.stream().map(userRankBo -> {
                UserRankNotProListVO userRankNotProListVO = new UserRankNotProListVO();
                BeanUtils.copyProperties(userRankBo, userRankNotProListVO);
                // 查询用户数量
                long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                        .eq(User::getRankId, userRankBo.getRankId()));
                userRankNotProListVO.setUserCount(Math.toIntExact(userCount));
                return userRankNotProListVO;
            }).toList();
        } else {
            UserRankListNotProInitDTO userRankListNotProInitDTO = getUserRankListNotProInitDTO();
            UserRankNotProListVO userRankNotProListVO = new UserRankNotProListVO();
            BeanUtils.copyProperties(userRankListNotProInitDTO, userRankNotProListVO);
            userRankNotProListVOList.add(userRankNotProListVO);
        }
        return PageUtil.convertPage(userRankBoPage, userRankNotProListVOList);
    }

    //处理userRights
    public String getUserRights(UserRank userRank) {
        List<String> userRights = new ArrayList<>();

        // 拼接会员权益
        if (userRank.getFreeShipping() != 0) {
            userRights.add("会员包邮");
        }
        if (userRank.getDiscount() != null && userRank.getDiscount().compareTo(BigDecimal.ZERO) != 0) {
            userRights.add("会员折扣" + userRank.getDiscount() + "折");
        }
        if (!userRank.getRankPoint().isEmpty()) {
            userRights.add(userRank.getRankPoint() + "倍积分回馈");
        }
        // 处理 userRights 列表
        String userRightsString;
        if (!userRights.isEmpty()) {
            userRightsString = "享受" + String.join(" ", userRights);
        } else {
            userRightsString = "";
        }

        return userRightsString;
    }

    /**
     * 将会员等级实体转换为管理端展示对象，并将数据库中的权益 JSON 解析为数组。
     */
    private UserRankListVO toUserRankListVO(UserRank userRank) {
        UserRankListVO userRankListVO = new UserRankListVO();
        BeanUtils.copyProperties(userRank, userRankListVO);
        userRankListVO.setRights(parseRights(userRank.getRights()));
        userRankListVO.setUserRights(getUserRights(userRank));
        return userRankListVO;
    }

    static List<UserRankListVO.Right> parseRights(String rightsJson) {
        if (StrUtil.isBlank(rightsJson)) {
            return new ArrayList<>();
        }
        try {
            List<UserRankListVO.Right> rights = JSON.parseArray(rightsJson, UserRankListVO.Right.class);
            return rights != null ? rights : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    //获取初始等级配置
    @Override
    public RankConfigInitDTO getRankConfigInit() {
        RankConfigInitDTO rankConfigInitDTO = new RankConfigInitDTO();
        rankConfigInitDTO.setType(1);
        rankConfigInitDTO.setRankType(1);
        rankConfigInitDTO.setCode("rank_config");
        rankConfigInitDTO.setRankAfterMonth(12);
        rankConfigInitDTO.setUseMonth(12);
        RankConfigInitDTO.DataDTO dataDTO = new RankConfigInitDTO.DataDTO();
        dataDTO.setType(1);
        dataDTO.setRankAfterMonth(12);
        dataDTO.setUseMonth(12);
        rankConfigInitDTO.setData(dataDTO);
        return rankConfigInitDTO;
    }

    @Override
    public UserLevelInfoVO getRankInfo(Integer rankId) {
        UserRank one = getOne(new LambdaQueryWrapper<UserRank>().eq(UserRank::getRankId, rankId));
        UserLevelInfoVO userLevelInfoVO = new UserLevelInfoVO();
        Object userRank;
        Object rankConfig = null;
        Object growUpSetting = null;
        if (one == null) {
            // userRank
            UserRankListVO userRankListVO = new UserRankListVO();
            BeanUtils.copyProperties(getUserRankListInitDTO(), userRankListVO);
            userRank = userRankListVO;
            // grow_up_setting
            growUpSetting = getGrowUpSettingInit();
            // rankConfig
            RankConfigInitDTO rankConfigInitDTO = getRankConfigInit();
            UserRankConfigVO userRankConfigVO = new UserRankConfigVO();
            BeanUtils.copyProperties(rankConfigInitDTO, userRankConfigVO);
            userRankConfigVO.setData(JsonUtil.fromJson(JsonUtil.toJson(rankConfigInitDTO.getData()), JSONObject.class));
            rankConfig = userRankConfigVO;
        } else {
            userRank = one;
            UserRankConfig selectRankConfig = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>()
                    .eq(UserRankConfig::getCode, "rank_config"));
            if (selectRankConfig != null) {
                UserRankConfigVO userRankConfigVO = new UserRankConfigVO();
                BeanUtils.copyProperties(selectRankConfig, userRankConfigVO);
                userRankConfigVO.setData(JsonUtil.fromJson(selectRankConfig.getData(), JSONObject.class));
                rankConfig = userRankConfigVO;
            }

            UserRankConfig growSetting = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>()
                    .eq(UserRankConfig::getCode, "grow_setting"));
            if (growSetting != null && growSetting.getData() != null) {
                growUpSetting = growSetting.getData();
            }
        }
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(userRank);
        try {
            jsonObject.set("rights", JSONUtil.parseArray(jsonObject.getStr("rights")));
        } catch (Exception e) {

        }
        userLevelInfoVO.setRankType(1);
        userLevelInfoVO.setUserRank(jsonObject);
        userLevelInfoVO.setRankConfig(rankConfig);
        userLevelInfoVO.setGrowUpSetting(growUpSetting);
        return userLevelInfoVO;
    }

    //获取成长等级设置
    public GrowUpSettingInitDTO getGrowUpSettingInit() {
        GrowUpSettingInitDTO growUpSettingInitDTO = new GrowUpSettingInitDTO();
        growUpSettingInitDTO.setBuyOrder(1);
        growUpSettingInitDTO.setBuyOrderNumber(1);
        growUpSettingInitDTO.setBuyOrderGrowth(1);
        growUpSettingInitDTO.setEvpi(1);
        growUpSettingInitDTO.setEvpiGrowth(1);
        growUpSettingInitDTO.setBindPhone(1);
        growUpSettingInitDTO.setBindPhoneGrowth(1);
        return growUpSettingInitDTO;
    }

    //获取等级配置list
    public UserRankListInitDTO getUserRankListInitDTO() {
        UserRankListInitDTO userRankListInitDTO = new UserRankListInitDTO();
        userRankListInitDTO.setRankLevel(1);
        userRankListInitDTO.setRankName("黄金会员");
        userRankListInitDTO.setRankLogo("https://oss.tigshop.com/img/gallery/202501/1737524324ro1DJxNm3aQowZKnPU.png");
        userRankListInitDTO.setRankCardType(1);
        userRankListInitDTO.setRankCardType(1);
        userRankListInitDTO.setRankIco("card1");
        userRankListInitDTO.setRankBg("");
        userRankListInitDTO.setMinGrowthPoints(BigDecimal.ZERO);
        userRankListInitDTO.setDiscount(new BigDecimal(0));
        userRankListInitDTO.setRankPoint(0);
        userRankListInitDTO.setFreeShipping(0);
        userRankListInitDTO.setRights(new ArrayList<>());
        return userRankListInitDTO;
    }

    //非pro会员等级列表初始化
    public UserRankListNotProInitDTO getUserRankListNotProInitDTO() {
        UserRankListNotProInitDTO userRankListNotProInitDTO = new UserRankListNotProInitDTO();
        userRankListNotProInitDTO.setRankLevel(1);
        userRankListNotProInitDTO.setRankName("黄金会员");
        userRankListNotProInitDTO.setRankLogo("https://oss.tigshop.com/img/gallery/202501/1735803176Yh9mCaE2r9ebXK3bGm.png");
        return userRankListNotProInitDTO;
    }

    /**
     * 构建品牌查询条件
     *
     * @param dto 查询参数
     * @return QueryWrapper
     */
    private LambdaQueryWrapper<UserRank> getUserRankQueryWrapper(UserRankListDTO dto) {
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        // 模糊查询
        if (StrUtil.isNotEmpty(dto.getKeyword())) {
            queryWrapper.like(UserRank::getRankName, dto.getKeyword());
        }
        return queryWrapper;
    }

    @Override
    public boolean create(UserRankDTO userRankDTO) {
        checkUserRank(userRankDTO);
        UserRank userRank = new UserRank();
        BeanUtils.copyProperties(userRankDTO, userRank);
        return this.save(userRank);
    }

    /**
     * 检验用户等级名称是否重复
     *
     * @param userRankDTO 用户等级
     */
    public void checkUserRank(UserRankDTO userRankDTO) {
        long count = this.count(new LambdaQueryWrapper<UserRank>()
                .eq(UserRank::getRankName, userRankDTO.getRankName())
                .select(UserRank::getRankName)
        );
        if (count > 0) {
            throw new GlobalException(RANK_NAME_EXISTS, SERVICE_DATA_ERROR);
        }
    }

    @Override
    public UserRankDetailVO<?> detail() {
        Integer rankType = 1;
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(UserRank::getRankId);
        List<UserRank> userRankList = this.list(queryWrapper);
        if (tigshopProperties.getIsPro() > 0) {
            rankType = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "rank_config")).getRankType();
            UserRankConfigVO userRankConfigVO = new UserRankConfigVO();
            GrowUpSettingInitDTO growUpSettingInitDTO = getGrowUpSettingInit();
            List<UserRankListVO> userRankListVOList = new ArrayList<>();
            if (!userRankList.isEmpty()) {
                userRankListVOList = userRankList.stream().map(userRank -> {
                    return toUserRankListVO(userRank);
                }).toList();

                LambdaQueryWrapper<UserRankConfig> queryWrapperConfig = new LambdaQueryWrapper<>();
                queryWrapperConfig.eq(UserRankConfig::getCode, "rank_config");
                UserRankConfig userRankConfig = userRankConfigMapper.selectOne(queryWrapperConfig);

                if (userRankConfig != null) {
                    BeanUtils.copyProperties(userRankConfig, userRankConfigVO);
                    userRankConfigVO.setData(JsonUtil.fromJson(userRankConfig.getData(), JSONObject.class));
                } else {
                    RankConfigInitDTO rankConfigInitDTO = getRankConfigInit();
                    BeanUtils.copyProperties(rankConfigInitDTO, userRankConfigVO);
                    userRankConfigVO.setData(JsonUtil.fromJson(JsonUtil.toJson(rankConfigInitDTO.getData()), JSONObject.class));
                }

                if (rankType == 1) {
                    LambdaQueryWrapper<UserRankConfig> queryWrapperGrow = new LambdaQueryWrapper<>();
                    queryWrapperGrow.eq(UserRankConfig::getCode, "grow_config");
                    UserRankConfig userRankConfigGrow = userRankConfigMapper.selectOne(queryWrapperGrow);
                    if (userRankConfigGrow != null) {
                        growUpSettingInitDTO = JsonUtil.fromJson(userRankConfigGrow.getData(),
                                GrowUpSettingInitDTO.class);
                    } else {
                        growUpSettingInitDTO = getGrowUpSettingInit();
                    }
                }
            } else {
                UserRankListInitDTO userRankListInitDTO = getUserRankListInitDTO();
                UserRankListVO userRankListVO = new UserRankListVO();
                BeanUtils.copyProperties(userRankListInitDTO, userRankListVO);
                userRankListVOList.add(userRankListVO);
                growUpSettingInitDTO = getGrowUpSettingInit();
                RankConfigInitDTO rankConfigInitDTO = getRankConfigInit();
                userRankConfigVO = new UserRankConfigVO();
                BeanUtils.copyProperties(rankConfigInitDTO, userRankConfigVO);
                userRankConfigVO.setData(JsonUtil.fromJson(JsonUtil.toJson(rankConfigInitDTO.getData()), JSONObject.class));
            }
            UserRankDetailVO<UserRankListVO> vo = new UserRankDetailVO<>();
            vo.setRankType(rankType);
            vo.setUserRankList(userRankListVOList);
            vo.setUserRankConfig(userRankConfigVO);
            vo.setGrowUpSetting(growUpSettingInitDTO);
            return vo;
        } else {
            List<UserRankNotProListVO> userRankListVOList = new ArrayList<>();
            if (!userRankList.isEmpty()) {
                userRankListVOList = userRankList.stream().map(userRank -> {
                    UserRankNotProListVO userRankNotProListVO = new UserRankNotProListVO();
                    BeanUtils.copyProperties(userRank, userRankNotProListVO);
                    return userRankNotProListVO;
                }).toList();
            } else {
                UserRankListNotProInitDTO userRankListNotProInitDTO = getUserRankListNotProInitDTO();
                UserRankNotProListVO userRankNotProListVO = new UserRankNotProListVO();
                BeanUtils.copyProperties(userRankListNotProInitDTO, userRankNotProListVO);
                userRankListVOList.add(userRankNotProListVO);
            }
            UserRankDetailVO<UserRankNotProListVO> vo = new UserRankDetailVO<>();
            vo.setRankType(rankType);
            vo.setUserRankList(userRankListVOList);
            vo.setUserRankConfig(new UserRankConfigVO());
            vo.setGrowUpSetting("");
            return vo;
        }
    }

    @Transactional
    @Override
    public void update(UserRankEditParam param) {
        if (tigshopProperties.getIsPro() > 0) {
            param.validParam();
        } else {
            param.setRankType(0);
        }

        // 查询已有的用户等级数据，判断是否删除
        List<UserRank> allUserRanks = this.lambdaQuery().list();
        List<Integer> rankIds = param.getData().stream()
                .map(UserRankEditParam.UserRankData::getRankId)
                .filter(Objects::nonNull).toList();
        // 需要删除的用户等级
        List<UserRank> delUserRanks = allUserRanks.stream().filter(item -> !rankIds.contains(item.getRankId())).toList();
        if (CollUtil.isNotEmpty(delUserRanks)) {
            this.removeBatchByIds(delUserRanks);
        }

        // 需要新增的用户等级
        List<UserRank> saveUserRanks = param.listSaveUserRanks(tigshopProperties.getIsPro(), param.getRankType());
        if (CollUtil.isNotEmpty(saveUserRanks)) {
            this.saveBatch(saveUserRanks);
        }

        // 需要更新的用户等级
        List<UserRank> editUserRanks = param.listEditUserRanks(tigshopProperties.getIsPro(), param.getRankType());
        if (CollUtil.isNotEmpty(editUserRanks)) {
            this.updateBatchById(editUserRanks);
        }

        // 更新配置
        if (tigshopProperties.getIsPro() == 0) {
            return;
        }
        // 更新成长值设置
        UserRankConfig userRankConfig = userRankConfigMapper.selectOne(
                new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "rank_config")
        );

        if (userRankConfig != null) {
            userRankConfig.setRankType(param.getRankType());
            userRankConfig.setData(JsonUtil.toJson(param.getUserRankConfig()));
            userRankConfigMapper.updateById(userRankConfig);
        } else {
            UserRankConfig userRankConfigSave = new UserRankConfig();
            userRankConfigSave.setCode("rank_config");
            userRankConfigSave.setRankType(param.getRankType());
            userRankConfigSave.setData(JsonUtil.toJson(param.getUserRankConfig()));
            userRankConfigMapper.insert(userRankConfigSave);
        }
        // 更新等级贵族
        if (param.getGrowUpSetting() != null) {
            if (param.getRankType() == 1) {
                UserRankConfig userRankConfigGrow = userRankConfigMapper.selectOne(
                        new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "grow_config")
                );

                if (userRankConfigGrow != null) {
                    userRankConfigGrow.setData(JsonUtil.toJson(param.getGrowUpSetting()));
                    userRankConfigGrow.setRankType(1);
                    userRankConfigMapper.updateById(userRankConfigGrow);
                } else {
                    UserRankConfig userRankConfigGrowSave = new UserRankConfig();
                    userRankConfigGrowSave.setCode("grow_config");
                    userRankConfigGrowSave.setData(JsonUtil.toJson(param.getGrowUpSetting()));
                    userRankConfigGrowSave.setRankType(1);
                    userRankConfigMapper.insert(userRankConfigGrowSave);
                }
            }
        }
    }

    private void assembleRankData(UserRankEditParam.UserRankData userRankData, UserRank userRank) {
        userRank.setRankName(userRankData.getRankName());
        userRank.setRankCardType(1);
        userRank.setRankLevel(userRankData.getRankLevel());
        userRank.setRankLogo(userRankData.getRankLogo());
        if (tigshopProperties.getIsPro() > 0) {
            userRank.setMinGrowthPoints(userRankData.getMinGrowthPoints());
            userRank.setMaxGrowthPoints(userRankData.getMaxGrowthPoints());
            userRank.setDiscount(userRankData.getDiscount());
            userRank.setRankType(userRankData.getRankType());
            userRank.setRankIco(userRankData.getRankIco());
            userRank.setRankBg(userRankData.getRankBg());
            userRank.setRankLogo(userRankData.getRankLogo());
            userRank.setRankPoint(userRankData.getRankPoint());
            userRank.setFreeShipping(userRankData.getFreeShipping());
            userRank.setRankCardType(userRankData.getRankCardType());
            userRank.setRights(JsonUtil.toJson(userRankData.getRights() != null ? userRankData.getRights() :
                    new ArrayList<>()));

        }
    }

    @Override
    public List<UserRank> getUserRank() {
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(UserRank::getMinGrowthPoints);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRank(User user) {
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(UserRank::getMinGrowthPoints, user.getGrowthPoints())
                .orderByDesc(UserRank::getMinGrowthPoints)
                .last("limit 1");
        UserRank userRankCurrent = this.getOne(queryWrapper);
        if (userRankCurrent != null && !Objects.equals(userRankCurrent.getRankId(), user.getRankId())) {
            user.setRankId(userRankCurrent.getRankId());
            // 只回写等级字段，避免评论/晒单赠分后使用旧 user 对象整行更新覆盖最新积分。
            return userMapper.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getUserId, user.getUserId())
                    .set(User::getRankId, userRankCurrent.getRankId())) > 0;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void getRankGrowth(Integer userId) {
        UserRankConfig rankConfig = userRankConfigMapper.selectOne(
                Wrappers.lambdaQuery(UserRankConfig.class).eq(UserRankConfig::getCode, "rank_config")
        );
        if (rankConfig == null || rankConfig.getRankType() == 1) {
            growthConfig(userId);
        }
        if (rankConfig == null || rankConfig.getRankType() == 2) {
            consumeConfig(userId);
        }

    }

    private void consumeConfig(Integer userId) {
        // 查询客户已完成订单总金额
        List<Order> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
        );

        BigDecimal sumAmount = orders.stream().map(Order::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<UserRank> userRanks = this.lambdaQuery().orderByDesc(UserRank::getMinGrowthPoints).list();

        List<UserRank> standardUserRanks = userRanks.stream()
                .filter(userRank -> userRank.getMinGrowthPoints().compareTo(sumAmount) <= 0)
                .toList();

        if (CollUtil.isNotEmpty(standardUserRanks)) {
            UserRank userRank = standardUserRanks.getFirst();
            User user = new User();
            user.setUserId(userId);
            user.setRankId(userRank.getRankId());
            userMapper.updateById(user);
        }

    }

    private void growthConfig(Integer userId) {
        UserRankConfig userRankConfig = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>()
                .eq(UserRankConfig::getCode, "grow_config"));
        String data = userRankConfig.getData();
        GrowUpSettingInitDTO config = JSONUtil.toBean(data, GrowUpSettingInitDTO.class);
        if (config != null && config.getBuyOrder() == 1) {

            int growthPoints = 0;

            // 查询最新一条成长日志
            RankGrowthLog growthLog = rankGrowthLogService.lambdaQuery()
                    .eq(RankGrowthLog::getUserId, userId)
                    .eq(RankGrowthLog::getType, 1)
                    .eq(RankGrowthLog::getChangeType, 1)
                    .orderByDesc(RankGrowthLog::getId)
                    .last("limit 1")
                    .one();

            long nowSeconds = System.currentTimeMillis() / 1000;
            Long lastAwardTime = (growthLog != null) ? growthLog.getCreateTime() : null;

            // 该用户所有"门店拆单子单"(parentOrderId>0 且 isStoreSplited=1)，含状态与完成时间。
            // 拆单"大单"不在下面的普通统计里逐单计数，而是按"最后一个子单完成时间"单独计一次，
            // 既保证一个大单只加一份成长值，又用完成时间去重，避免用下单时间去重导致的漏加/重复加。
            List<Order> splitChildren = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .select(Order::getParentOrderId, Order::getOrderStatus, Order::getReceivedTime)
                    .eq(Order::getUserId, userId)
                    .eq(Order::getIsDel, 0)
                    .gt(Order::getParentOrderId, 0)
                    .eq(Order::getIsStoreSplited, 1));
            List<Integer> splitParentIds = splitChildren.stream()
                    .map(Order::getParentOrderId).distinct().toList();

            // 统计一：已完成的普通购买单元（非门店拆单子单、非门店拆单大单父单）——
            // 统一口径：订单已完成(确认收货)才加成长值，并按完成时间(receivedTime)去重。
            // 周期购/次卡派生子单(isStoreSplited=0)完成后正常计入。
            LambdaQueryWrapper<Order> orderNumQuery = new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getIsDel, 0)
                    .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                    .and(w -> w.le(Order::getParentOrderId, 0).or().ne(Order::getIsStoreSplited, 1));
            // 排除门店拆单大单父单，交给"统计二"按完成时间处理，避免重复计数
            if (!splitParentIds.isEmpty()) {
                orderNumQuery.notIn(Order::getOrderId, splitParentIds);
            }
            if (lastAwardTime != null) {
                orderNumQuery.gt(Order::getReceivedTime, lastAwardTime);
            }
            orderNumQuery.le(Order::getReceivedTime, nowSeconds);
            long orderNum = orderMapper.selectCount(orderNumQuery);

            // 统计二：门店拆单大单——按父单把子单分组，只有当一个大单的所有子单都完成时，
            // 取"最后一个子单的完成时间(receivedTime)"作为该大单的完成时间。仅统计完成时间在上次发放之后、
            // 且不晚于当前时刻的大单：既确保每个大单恰好加一次，又不会因下单时间被时间窗口漏掉。
            Map<Integer, List<Order>> childrenByParent = splitChildren.stream()
                    .collect(Collectors.groupingBy(Order::getParentOrderId));
            for (List<Order> children : childrenByParent.values()) {
                boolean hasPending = false;
                boolean hasCompleted = false;
                long lastDoneTime = 0L;
                for (Order child : children) {
                    int status = child.getOrderStatus();
                    if (status == OrderStatusEnum.COMPLETED.getCode()) {
                        hasCompleted = true;
                        Long rt = child.getReceivedTime();
                        if (rt != null && rt > lastDoneTime) {
                            lastDoneTime = rt;
                        }
                    } else if (status != OrderStatusEnum.CANCELLED.getCode()) {
                        // 还有"既未完成、也未取消"的子单 → 大单尚未整单完成
                        hasPending = true;
                        break;
                    }
                }
                // 仍有子单未完成、或全部被取消(没有有效完成的子单) → 该大单暂不计入
                if (hasPending || !hasCompleted) {
                    continue;
                }
                // 用"最后一个子单完成时间"去重：仅统计上次发放之后、且不晚于当前时刻完成的大单
                if ((lastAwardTime == null || lastDoneTime > lastAwardTime) && lastDoneTime <= nowSeconds) {
                    orderNum += 1;
                }
            }

            // 计算成长值
            if (orderNum >= config.getBuyOrderNumber()) {
                growthPoints = (int) ((orderNum / (double) config.getBuyOrderNumber()) * config.getBuyOrderGrowth());
            }

            // 写入成长日志
            if (growthPoints > 0) {
                RankGrowthLog log = new RankGrowthLog();
                log.setUserId(userId);
                log.setType(RankGrowthLogTypeEnum.GROWTH_TYPE_ORDER.getCode());
                log.setGrowthPoints(growthPoints);
                log.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
                log.setCreateTime(System.currentTimeMillis() / 1000);
                rankGrowthLogService.save(log);

                userGrowthPointsLogMapper.insert(UserGrowthPointsLog.builder()
                        .userId(userId)
                        .changeDesc("下单")
                        .points(growthPoints)
                        .changeType(ChangTypeEnum.INC_POINTS.getValue())
                        .changeTime(StringUtils.getCurrentTime())
                        .build());

                // 更新用户成长值
                User user = userMapper.selectById(userId);
                user.setGrowthPoints(user.getGrowthPoints() + growthPoints);
                userMapper.updateById(user);
            }
        }
        // 发送升级消息
        if (tigshopProperties.getIsPro() != 1) {
            return;
        }
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VIP_UPDATE_ROUTING_KEY, userId);
    }

    @Override
    public void reduceGrowth(Long refundId) {
        if (tigshopProperties.getIsPro() != 1) {
            return;
        }
        // 成长值配置为「永久有效」(rank_config.type==1) 时，退款不扣减成长值
        UserRankConfig rankConfig = userRankConfigMapper.selectOne(
                new LambdaQueryWrapper<UserRankConfig>()
                        .eq(UserRankConfig::getCode, "rank_config")
        );
        if (rankConfig != null && StrUtil.isNotBlank(rankConfig.getData())) {
            Integer rankValidityType = JSON.parseObject(rankConfig.getData()).getInteger("type");
            if (rankValidityType != null && rankValidityType == 1) {
                return;
            }
        }
        RefundApply refundApply = refundApplyMapper.selectById(refundId);
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));

        long afterSalesItemCount = aftersalesItemMapper.selectCount(
                Wrappers.lambdaQuery(AftersalesItem.class).eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId())
        );

        long orderItemCount = orderItemMapper.selectCount(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, refundApply.getOrderId())
        );

        if (afterSalesItemCount != orderItemCount) {
            // 部分商品退款不扣减
            return;
        }

        // 以下单时间判断该笔订单是否已计算入成长值表
        UserRankConfig growConfig = userRankConfigMapper.selectOne(
                new LambdaQueryWrapper<UserRankConfig>()
                        .eq(UserRankConfig::getCode, "grow_config")
        );
        if (growConfig != null && StrUtil.isNotBlank(growConfig.getData())) {
            String data = growConfig.getData();
            JSONObject growConfigData = JSON.parseObject(data);
            Object buyOrder = growConfigData.get("buyOrder");

            if (buyOrder != null) {
                RankGrowthLog growthUser = rankGrowthLogService.lambdaQuery()
                        .eq(RankGrowthLog::getType, RankGrowthLogTypeEnum.GROWTH_TYPE_ORDER.getCode())
                        .eq(RankGrowthLog::getUserId, refundApply.getUserId())
                        .eq(RankGrowthLog::getChangeType, 1)
                        .orderByDesc(RankGrowthLog::getId)
                        .last("limit 1")
                        .one();

                long growthTime = growthUser != null ? growthUser.getCreateTime() : 0;
                Order order = orderMapper.selectById(refundApply.getOrderId());
                if (order != null && order.getAddTime() <= growthTime) {
                    // 订单已计入成长值计算 -- 按配置比例扣减成长值
                    BigDecimal buyOrderGrowth = growConfigData.getBigDecimal("buyOrderGrowth");
                    BigDecimal buyOrderNumber = growConfigData.getBigDecimal("buyOrderNumber");
                    // 配置缺失或除数非法时跳过扣减，避免退款流程因成长值配置异常整体失败
                    if (buyOrderGrowth == null || buyOrderNumber == null
                            || buyOrderNumber.compareTo(BigDecimal.ZERO) <= 0) {
                        return;
                    }
                    BigDecimal changeGrowth = buyOrderGrowth.divide(buyOrderNumber, 2, RoundingMode.HALF_UP);

                    // 退款成功，减少成长值
                    RankGrowthLog rankGrowthLog = RankGrowthLog.builder()
                            .userId(refundApply.getUserId())
                            .type(RankGrowthLogTypeEnum.GROWTH_TYPE_REFUND.getCode())
                            .growthPoints(changeGrowth.intValue())
                            .changeType(2)
                            .build();
                    rankGrowthLogService.save(rankGrowthLog);

                    // 同步扣减用户成长值（不允许跌破 0）并触发等级重新评估（可能降级）
                    User user = userMapper.selectById(refundApply.getUserId());
                    if (user != null) {
                        int currentGrowth = user.getGrowthPoints() == null ? 0 : user.getGrowthPoints();
                        int newGrowth = Math.max(0, currentGrowth - changeGrowth.intValue());
                        user.setGrowthPoints(newGrowth);
                        userMapper.updateById(user);
                        this.updateUserRank(user);
                    }
                }
            }
        }

    }

    @Transactional
    @Override
    public void addUserGrowthPoints(Integer userId, RankGrowthLogTypeEnum rankGrowthLogTypeEnum) {
        if (tigshopProperties.getIsPro() != 1) {
            return;
        }

        UserRankConfig rankConfig = userRankConfigMapper.selectOne(
                Wrappers.lambdaQuery(UserRankConfig.class).eq(UserRankConfig::getCode, "rank_config")
        );

        if (rankConfig == null || rankConfig.getRankType() != 1) {
            return;
        }

        UserRankConfig growConfig = userRankConfigMapper.selectOne(
                Wrappers.lambdaQuery(UserRankConfig.class).eq(UserRankConfig::getCode, "grow_config")
        );
        UserGrowConfig userGrowConfig = JSON.parseObject(growConfig.getData(), UserGrowConfig.class);

        Integer growthPoints = this.getGrowthPoints(userId, userGrowConfig, rankGrowthLogTypeEnum);
        if (growthPoints > 0) {
            User user = userMapper.selectById(userId);
            // 修改客户成长值
            user.setGrowthPoints(user.getGrowthPoints() + growthPoints);
            userMapper.updateById(user);

            // 判断是否升级会员等级
            this.updateUserRank(user);

            // 增加会员成长值记录
            userGrowthPointsLogMapper.insert(UserGrowthPointsLog.builder()
                    .userId(userId)
                    .points(growthPoints)
                    .changeTime(StringUtils.getCurrentTime())
                    .changeDesc(rankGrowthLogTypeEnum.getDescription())
                    .changeType(1)
                    .build());

            // 增加会员成长记录
            rankGrowthLogService.save(
                    RankGrowthLog.builder()
                            .userId(userId)
                            .type(rankGrowthLogTypeEnum.getCode())
                            .growthPoints(growthPoints)
                            .changeType(1)
                            .createTime(StringUtils.getCurrentTime())
                            .build()
            );
        }
    }

    private Integer getGrowthPoints(Integer userId, UserGrowConfig userGrowConfig, RankGrowthLogTypeEnum rankGrowthLogTypeEnum) {
        if (RankGrowthLogTypeEnum.GROWTH_TYPE_BIND_PHONE.equals(rankGrowthLogTypeEnum)) {

            Long bindCount = rankGrowthLogService.lambdaQuery()
                            .eq(RankGrowthLog::getUserId, userId)
                            .eq(RankGrowthLog::getType, RankGrowthLogTypeEnum.GROWTH_TYPE_BIND_PHONE.getCode())
                            .count();

            return Constants.YES.equals(userGrowConfig.getBindPhone()) && bindCount == 0 ? userGrowConfig.getBindPhoneGrowth() : 0;
        }

        if (RankGrowthLogTypeEnum.GROWTH_TYPE_INFORMATION.equals(rankGrowthLogTypeEnum)) {

            Long informationCount = rankGrowthLogService.lambdaQuery()
                    .eq(RankGrowthLog::getUserId, userId)
                    .eq(RankGrowthLog::getType, RankGrowthLogTypeEnum.GROWTH_TYPE_INFORMATION.getCode())
                    .count();

            return Constants.YES.equals(userGrowConfig.getEvpi()) && informationCount == 0 ? userGrowConfig.getEvpiGrowth() : 0;
        }

        if (RankGrowthLogTypeEnum.GROWTH_TYPE_ORDER.equals(rankGrowthLogTypeEnum)) {
            if (Constants.YES.equals(userGrowConfig.getBuyOrder())) {
                // 统计上次的下单加成长值的记录到当前时间的订单数
                // 订单数 / 增加成长值订单数
            }
        }

        return 0;
    }
}
