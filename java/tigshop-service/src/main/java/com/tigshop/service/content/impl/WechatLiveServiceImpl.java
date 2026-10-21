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

package com.tigshop.service.content.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.content.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.*;
import com.tigshop.bean.model.content.WechatLive;
import com.tigshop.bean.model.content.WechatLiveGoods;
import com.tigshop.bean.model.content.WechatLiveRoomGoods;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreProductSku;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.vo.content.WechatLiveGoodsVO;
import com.tigshop.bean.vo.content.WechatLivePromoteVO;
import com.tigshop.bean.vo.content.WechatLivePushUrlVO;
import com.tigshop.bean.vo.content.WechatLiveVO;
import com.tigshop.bean.enums.content.WechatLiveAuditStatus;
import com.tigshop.bean.enums.content.WechatLiveStatus;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.mapper.content.WechatLiveMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.WechatLiveGoodsService;
import com.tigshop.service.content.WechatLiveOrderRelService;
import com.tigshop.service.content.WechatLiveRoomGoodsService;
import com.tigshop.service.content.WechatLiveService;
import com.tigshop.service.content.WechatLiveSyncLogService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.content.WechatLiveConstants.*;

/**
 * 微信直播服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class WechatLiveServiceImpl extends BaseServiceImpl<WechatLiveMapper, WechatLive> implements WechatLiveService {

    private static final String LIVE_PLAYER_PAGE_PATH_TEMPLATE =
            "plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id={}";

    @Resource
    private ConfigService configService;

    @Resource
    private WechatApiClient wechatApiClient;

    @Resource
    private WechatOAuthService wechatOAuthService;

    @Resource
    private ProductService productService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private WechatLiveGoodsService wechatLiveGoodsService;

    @Resource
    private com.tigshop.mapper.content.WechatLiveGoodsMapper wechatLiveGoodsMapper;

    @Resource
    private WechatLiveRoomGoodsService wechatLiveRoomGoodsService;

    @Resource
    private WechatLiveOrderRelService wechatLiveOrderRelService;

    @Resource
    private WechatLiveSyncLogService wechatLiveSyncLogService;


    @Override
    public Page<WechatLiveVO> list(WechatLiveListDTO listDTO) {
        // 分页
        Page<WechatLive> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<WechatLive> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(WechatLive::getWechatLiveTitle, keyword);
        }

        String wechatLiveTitle = listDTO.getWechatLiveTitle();
        if (StrUtil.isNotEmpty(wechatLiveTitle)) {
            queryWrapper.like(WechatLive::getWechatLiveTitle, wechatLiveTitle);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId > 0) {
            queryWrapper.eq(WechatLive::getShopId, shopId);
        }

        // 直播状态过滤
        if (listDTO.getLiveStatus() != null && WechatLiveStatus.isValidCode(listDTO.getLiveStatus())) {
            queryWrapper.eq(WechatLive::getLiveStatus, listDTO.getLiveStatus());
        }

        // 创建时间范围过滤
        String startTime = listDTO.getStartTime();
        String endTime = listDTO.getEndTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (StrUtil.isNotEmpty(startTime)) {
            queryWrapper.ge(WechatLive::getCreateTime, LocalDateTime.parse(startTime + " 00:00:00", formatter));
        }
        if (StrUtil.isNotEmpty(endTime)) {
            queryWrapper.le(WechatLive::getCreateTime, LocalDateTime.parse(endTime + " 23:59:59", formatter));
        }

        // 执行查询
        Page<WechatLive> wechatLivePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<WechatLive> wechatLivePageRecords = wechatLivePage.getRecords();
        // 转换为VO
        List<WechatLiveVO> wechatLiveVOList = wechatLivePageRecords.stream()
                .map(wechatLive -> {
                    WechatLiveVO wechatLiveVO = new WechatLiveVO();
                    BeanUtils.copyProperties(wechatLive, wechatLiveVO);
                    // 处理其他字段的转换
                    convert(wechatLive, wechatLiveVO);
                    fillStats(wechatLiveVO);
                    return wechatLiveVO;
                }).toList();
        return PageUtil.convertPage(wechatLivePage, wechatLiveVOList);
    }

    @Override
    public WechatLiveVO detail(Integer id) {
        if (id != null) {
            WechatLive wechatLive = this.getById(id);
            WechatLiveVO wechatLiveVO = new WechatLiveVO();
            BeanUtils.copyProperties(wechatLive, wechatLiveVO);
            // 处理格式转换
            convert(wechatLive, wechatLiveVO);
            fillStats(wechatLiveVO);
            return wechatLiveVO;
        }
        return null;
    }

    /**
     * 格式转换
     *
     * @param wechatLive   WechatLive
     * @param wechatLiveVO WechatLiveVO
     */
    public void convert(WechatLive wechatLive, WechatLiveVO wechatLiveVO) {
        wechatLiveVO.setStartTime(formatTimestamp(wechatLive.getStartTime()));
        wechatLiveVO.setEndTime(formatTimestamp(wechatLive.getEndTime()));
        wechatLiveVO.setLastUpdateTime(formatTimestamp(wechatLive.getLastUpdateTime()));
        wechatLiveVO.setActRangeExt(parseIntList(wechatLive.getActRangeExt()));
        wechatLiveVO.setProductData(parseIntList(wechatLive.getProductData()));
        wechatLiveVO.setLiveStatusText(WechatLiveStatus.getStatusName(wechatLive.getLiveStatus()));
        wechatLiveVO.setAuditStatusText(WechatLiveAuditStatus.getStatusName(wechatLive.getAuditStatus()));
    }

    public Long getCount(Integer id) {
        return this.count(new LambdaQueryWrapper<WechatLive>()
                .eq(WechatLive::getWechatLiveId, id)
                .select(WechatLive::getWechatLiveId));
    }

    @Override
    public boolean update(WechatLiveUpdateDTO updateDTO) {
        if (updateDTO == null) {
            return false;
        }
        WechatLive wechatLive = this.getById(updateDTO.getWechatLiveId());
        if (wechatLive == null) {
            throw new GlobalException(WECHAT_LIVE_NOT_EXISTS);
        }
        if (updateDTO.getWechatLiveTitle() != null) {
            wechatLive.setWechatLiveTitle(updateDTO.getWechatLiveTitle());
        }
        if (updateDTO.getName() != null) {
            wechatLive.setWechatLiveTitle(updateDTO.getName());
        }
        if (updateDTO.getActRange() != null) {
            wechatLive.setActRange(updateDTO.getActRange());
        }
        if (updateDTO.getActRangeExt() != null) {
            wechatLive.setActRangeExt(updateDTO.getActRangeExt().toString());
        }
        if (updateDTO.getIsFeedsPublic() != null) {
            wechatLive.setIsFeedsPublic(updateDTO.getIsFeedsPublic());
        }
        if (updateDTO.getCoverImg() != null) {
            wechatLive.setCoverImg(updateDTO.getCoverImg());
        }
        if (updateDTO.getShareImg() != null) {
            wechatLive.setShareImg(updateDTO.getShareImg());
        }
        if (updateDTO.getStartTime() != null) {
            wechatLive.setStartTime(parseToTimestamp(updateDTO.getStartTime()));
        }
        if (updateDTO.getEndTime() != null) {
            wechatLive.setEndTime(parseToTimestamp(updateDTO.getEndTime()));
        }
        if (updateDTO.getAnchorName() != null) {
            wechatLive.setAnchorName(updateDTO.getAnchorName());
        }
        if (updateDTO.getAnchorImg() != null) {
            wechatLive.setAnchorImg(updateDTO.getAnchorImg());
        }
        if (updateDTO.getLiveType() != null) {
            wechatLive.setLiveType(updateDTO.getLiveType());
        }
        if (updateDTO.getCloseLike() != null) {
            wechatLive.setCloseLike(updateDTO.getCloseLike());
        }
        if (updateDTO.getCloseGoods() != null) {
            wechatLive.setCloseGoods(updateDTO.getCloseGoods());
        }
        if (updateDTO.getCloseComment() != null) {
            wechatLive.setCloseComment(updateDTO.getCloseComment());
        }
        if (updateDTO.getCloseKf() != null) {
            wechatLive.setCloseKf(updateDTO.getCloseKf());
        }
        if (updateDTO.getCloseReplay() != null) {
            wechatLive.setCloseReplay(updateDTO.getCloseReplay());
        }
        if (updateDTO.getFeedsImg() != null) {
            wechatLive.setFeedsImg(updateDTO.getFeedsImg());
        }
        if (updateDTO.getAnchorId() != null) {
            wechatLive.setAnchorId(updateDTO.getAnchorId());
        }
        if (updateDTO.getAnchorOpenid() != null) {
            wechatLive.setAnchorOpenid(updateDTO.getAnchorOpenid());
        }
        if (updateDTO.getSubAnchorWechat() != null) {
            wechatLive.setSubAnchorWechat(updateDTO.getSubAnchorWechat());
        }
        return this.updateById(wechatLive);
    }

    @Override
    public boolean refresh() {
        String accessToken = wechatOAuthService.getMiniAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw new GlobalException(WECHAT_LIVE_ACCESS_TOKEN_FAIL);
        }

        //查询直播
        GetLiveInfoParam param = new GetLiveInfoParam();
        param.setStart(0);
        param.setLimit(10);
        GetLiveInfoResult result = wechatApiClient.getLiveInfo(accessToken, param);
        if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
            if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
            }
            throw new GlobalException(WECHAT_LIVE_GET_LIVE_INFO_FAIL);
        }

        //循环返回结果的room_info
        Integer shopId = getShopId();
        for (GetLiveInfoResult.RoomInfo roomInfo : result.getRoomInfoList()) {
            WechatLive existing = this.getOne(new LambdaQueryWrapper<WechatLive>()
                    .eq(WechatLive::getRoomId, roomInfo.getRoomId()));
            WechatLive wechatLive = existing != null ? existing : new WechatLive();
            fillRoomFieldsFromWechat(wechatLive, roomInfo, existing != null ? existing.getShopId() : shopId);
            if (existing != null) {
                this.updateById(wechatLive);
            } else {
                this.save(wechatLive);
            }
            syncRoomGoods(wechatLive, roomInfo.getGoods());
        }
        return true;
    }

    @Override
    public Integer create(WechatLiveRoomCreateDTO createDTO) {
        Integer roomId;

        WechatLive wechatLive = new WechatLive();
        wechatLive.setWechatLiveTitle(createDTO.getName());
        wechatLive.setLiveStatus(102);
        wechatLive.setLastUpdateTime(StringUtils.getCurrentTime());
        wechatLive.setShopId(getShopId());
        fillRoomFieldsFromCreateDTO(wechatLive, createDTO);
        fillRoomDefaultValues(wechatLive);

        try {
            roomId = createRemoteRoom(createDTO);
            wechatLive.setRoomId(roomId);
        } catch (Exception e) {
            //throw wrapWechatLiveException(e);
        }

        this.save(wechatLive);

        return wechatLive.getWechatLiveId();
    }

    @Override
    public boolean delete(Integer id) {
        WechatLive wechatLive = this.getById(id);
        if (wechatLive == null) {
            throw new GlobalException(WECHAT_LIVE_NOT_EXISTS);
        }

        // 删除本地记录
        this.removeById(id);

        try {
            String accessToken = getAccessToken();

            DeleteRoomParam deleteParam = new DeleteRoomParam();
            deleteParam.setId(wechatLive.getRoomId());
            WechatApiResult result = wechatApiClient.deleteRoom(accessToken, deleteParam);
            if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
                if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                    throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
                }
                throw new GlobalException(WECHAT_LIVE_DELETE_FAIL + (result != null ? result.getErrmsg() : ""));
            }
        } catch (Exception e) {
            //忽略
//            throw wrapWechatLiveException(e);
        }

        return true;
    }

    @Override
    public WechatLivePushUrlVO getPushUrl(Integer roomId) {
        WechatLive wechatLive = this.getOne(new LambdaQueryWrapper<WechatLive>()
                .eq(WechatLive::getRoomId, roomId));
        if (wechatLive == null) {
            throw new GlobalException(WECHAT_LIVE_ROOM_NOT_EXISTS);
        }

        try {
            String accessToken = getAccessToken();

            GetPushUrlResult result = wechatApiClient.getPushUrl(accessToken, roomId);
            if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
                if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                    throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
                }
                throw new GlobalException(WECHAT_LIVE_PUSH_URL_FAIL + (result != null ? result.getErrmsg() : ""));
            }

            WechatLivePushUrlVO pushUrlVO = new WechatLivePushUrlVO();
            pushUrlVO.setRoomId(roomId);
            pushUrlVO.setPushUrl(result.getPushUrl());
            pushUrlVO.setLiveCode(result.getLiveCode());
            pushUrlVO.setRoomName(wechatLive.getWechatLiveTitle());

            return pushUrlVO;
        } catch (Exception e) {
            throw wrapWechatLiveException(e);
        }
    }

    @Override
    public WechatLivePromoteVO getPromoteData(Integer roomId) {
        WechatLive wechatLive = getLiveRoom(roomId);

        String pagePath = buildLivePlayerPagePath(roomId);
        String qrcodeUrl = "";
        String posterUrl = "";
        String wechatShortLink = "";

        try {
            String accessToken = getAccessToken();

            GetSharedCodeResult result = getSharedCodeResult(accessToken, roomId);
            if (result != null && ObjectUtil.equal(result.getErrcode(), 0)) {
                pagePath = StrUtil.blankToDefault(result.getPagePath(), pagePath);
                qrcodeUrl = StrUtil.nullToEmpty(result.getCdnUrl());
                posterUrl = StrUtil.nullToEmpty(result.getPosterUrl());
            }

            wechatShortLink = buildLiveShortLink(accessToken, wechatLive.getWechatLiveTitle(), pagePath);
        } catch (Exception e) {
            log.warn("[直播推广] 获取微信推广数据失败，降级返回默认数据，roomId={}，原因={}", roomId, e.getMessage());
            // 降级：posterUrl 使用直播间封面图，其他保持空字符串
            posterUrl = StrUtil.nullToEmpty(wechatLive.getShareImg());
        }

        return WechatLivePromoteVO.builder()
                .roomId(roomId)
                .roomName(wechatLive.getWechatLiveTitle())
                .coverImg(wechatLive.getCoverImg())
                .pagePath(pagePath)
                .qrcodeUrl(qrcodeUrl)
                .posterUrl(posterUrl)
                .wechatShortLink(wechatShortLink)
                .build();
    }

    @Override
    public boolean syncFromWechat() {
        // 复用refresh方法
        return this.refresh();
    }

    @Override
    public boolean syncToWechat(Integer id) {
        WechatLive wechatLive = this.lambdaQuery().eq(WechatLive::getRoomId, id)
                .eq(WechatLive::getShopId, getShopId()).one();
        if (wechatLive == null) {
            throw new GlobalException(WECHAT_LIVE_NOT_EXISTS);
        }

        // 如果微信roomId存在，则更新；否则创建
        if (wechatLive.getRoomId() != null && wechatLive.getRoomId() > 0) {
            // 获取accessToken
            String accessToken = getAccessToken();

            // 编辑直播间
            EditRoomParam param = new EditRoomParam();
            param.setId(wechatLive.getRoomId());
            param.setName(wechatLive.getWechatLiveTitle());
            param.setCoverImg(wechatLive.getCoverImg());
            param.setShareImg(wechatLive.getShareImg());
            param.setCloseLike(wechatLive.getCloseLike());
            param.setCloseGoods(wechatLive.getCloseGoods());
            param.setCloseComment(wechatLive.getCloseComment());
            param.setCloseKf(wechatLive.getCloseKf());
            param.setIsFeedsPublic(wechatLive.getIsFeedsPublic());
            param.setFeedsImg(wechatLive.getFeedsImg());

            WechatApiResult result = wechatApiClient.editRoom(accessToken, param);
            if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
                if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                    throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
                }
                throw new GlobalException("同步到微信失败：" + (result != null ? result.getErrmsg() : ""));
            }
            wechatLive.setLastUpdateTime(StringUtils.getCurrentTime());
            this.updateById(wechatLive);
            return true;
        } else {
            WechatLiveRoomCreateDTO createDTO = new WechatLiveRoomCreateDTO();
            createDTO.setName(wechatLive.getWechatLiveTitle());
            createDTO.setCoverImg(wechatLive.getCoverImg());
            createDTO.setShareImg(wechatLive.getShareImg());
            createDTO.setStartTime(formatTimestamp(wechatLive.getStartTime()));
            createDTO.setEndTime(formatTimestamp(wechatLive.getEndTime()));
            createDTO.setAnchorName(wechatLive.getAnchorName());
            createDTO.setAnchorImg(wechatLive.getAnchorImg());
            createDTO.setLiveType(wechatLive.getLiveType());
            createDTO.setCloseLike(wechatLive.getCloseLike());
            createDTO.setCloseGoods(wechatLive.getCloseGoods());
            createDTO.setCloseComment(wechatLive.getCloseComment());
            createDTO.setCloseKf(wechatLive.getCloseKf());
            createDTO.setCloseReplay(wechatLive.getCloseReplay());
            createDTO.setIsFeedsPublic(wechatLive.getIsFeedsPublic());
            createDTO.setFeedsImg(wechatLive.getFeedsImg());
            createDTO.setAnchorId(wechatLive.getAnchorId());
            createDTO.setAnchorOpenid(wechatLive.getAnchorOpenid());
            createDTO.setSubAnchorWechat(wechatLive.getSubAnchorWechat());

            wechatLive.setRoomId(createRemoteRoom(createDTO));
            wechatLive.setLastUpdateTime(StringUtils.getCurrentTime());
            fillRoomDefaultValues(wechatLive);
            return this.updateById(wechatLive);
        }
    }

    /**
     * 获取微信accessToken
     *
     * @return accessToken
     */
    private String getAccessToken() {
        String accessToken = wechatOAuthService.getLiveMiniAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw new GlobalException(WECHAT_LIVE_ACCESS_TOKEN_FAIL);
        }
        return accessToken;
    }

    private GetSharedCodeResult getSharedCodeResult(String accessToken, Integer roomId) {
        try {
            return wechatApiClient.getSharedCode(accessToken, roomId, null);
        } catch (Exception e) {
            throw wrapWechatLiveException(e);
        }
    }

    private String buildLivePlayerPagePath(Integer roomId) {
        return StrUtil.format(LIVE_PLAYER_PAGE_PATH_TEMPLATE, roomId);
    }

    private String buildLiveShortLink(String accessToken, String roomName, String pagePath) {
        if (StrUtil.isBlank(pagePath)) {
            return "";
        }
        try {
            ShortLinkParam param = ShortLinkParam.builder()
                    .pageTitle(StrUtil.blankToDefault(roomName, "直播间推广"))
                    .pageUrl(pagePath)
                    .build();
            ShortLinkResult result = wechatApiClient.genwxashortlink(accessToken, param);
            if (result != null && ObjectUtil.equal(result.getErrcode(), 0) && StrUtil.isNotBlank(result.getLink())) {
                return result.getLink();
            }
        } catch (Exception e) {
            throw wrapWechatLiveException(e);
        }
        return pagePath;
    }

    private Integer createRemoteRoom(WechatLiveRoomCreateDTO createDTO) {
        String accessToken = getAccessToken();

        CreateRoomParam param = new CreateRoomParam();
        param.setName(createDTO.getName());
        param.setCoverImg(createDTO.getCoverImg());
        param.setShareImg(createDTO.getShareImg());
        param.setStartTime(parseToTimestamp(createDTO.getStartTime()));
        param.setEndTime(parseToTimestamp(createDTO.getEndTime()));
        param.setAnchorName(createDTO.getAnchorName());
        param.setLiveType(createDTO.getLiveType());
        param.setCloseLike(createDTO.getCloseLike());
        param.setCloseGoods(createDTO.getCloseGoods());
        param.setCloseComment(createDTO.getCloseComment());
        param.setCloseKf(createDTO.getCloseKf());
        param.setCloseReplay(createDTO.getCloseReplay());
        param.setIsFeedsPublic(createDTO.getIsFeedsPublic());
        param.setFeedsImg(createDTO.getFeedsImg());
        param.setAnchorId(createDTO.getAnchorId());
        param.setAnchorOpenid(createDTO.getAnchorOpenid());
        param.setSubAnchorWechat(createDTO.getSubAnchorWechat());

        CreateRoomResult result = wechatApiClient.createRoom(accessToken, param);
        if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
            if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
            }
            throw new GlobalException(WECHAT_LIVE_CREATE_FAIL + (result != null ? result.getErrmsg() : ""));
        }
        return result.getRoomId();
    }

    private void fillRoomFieldsFromCreateDTO(WechatLive wechatLive, WechatLiveRoomCreateDTO createDTO) {
        wechatLive.setCoverImg(createDTO.getCoverImg());
        wechatLive.setShareImg(createDTO.getShareImg());
        wechatLive.setStartTime(parseToTimestamp(createDTO.getStartTime()));
        wechatLive.setEndTime(parseToTimestamp(createDTO.getEndTime()));
        wechatLive.setAnchorName(createDTO.getAnchorName());
        wechatLive.setAnchorImg(createDTO.getAnchorImg());
        wechatLive.setLiveType(createDTO.getLiveType());
        wechatLive.setCloseLike(createDTO.getCloseLike());
        wechatLive.setCloseGoods(createDTO.getCloseGoods());
        wechatLive.setCloseComment(createDTO.getCloseComment());
        wechatLive.setCloseKf(createDTO.getCloseKf());
        wechatLive.setCloseReplay(createDTO.getCloseReplay());
        wechatLive.setIsFeedsPublic(createDTO.getIsFeedsPublic());
        wechatLive.setFeedsImg(createDTO.getFeedsImg());
        wechatLive.setAnchorId(createDTO.getAnchorId());
        wechatLive.setAnchorOpenid(createDTO.getAnchorOpenid());
        wechatLive.setSubAnchorWechat(createDTO.getSubAnchorWechat());
    }

    private void fillRoomFieldsFromWechat(WechatLive wechatLive, GetLiveInfoResult.RoomInfo roomInfo, Integer shopId) {
        wechatLive.setRoomId(roomInfo.getRoomId());
        wechatLive.setWechatLiveTitle(roomInfo.getName());
        wechatLive.setCoverImg(roomInfo.getCoverImg());
        wechatLive.setStartTime(roomInfo.getStartTime());
        wechatLive.setEndTime(roomInfo.getEndTime());
        wechatLive.setAnchorName(roomInfo.getAnchorName());
        wechatLive.setLiveStatus(roomInfo.getStatus());
        wechatLive.setShareImg(roomInfo.getShareImg());
        wechatLive.setLiveType(roomInfo.getLiveType());
        wechatLive.setCloseLike(roomInfo.getCloseLike());
        wechatLive.setCloseGoods(roomInfo.getCloseGoods());
        wechatLive.setCloseComment(roomInfo.getCloseComment());
        wechatLive.setCloseKf(roomInfo.getCloseKf());
        wechatLive.setCloseReplay(roomInfo.getCloseReplay());
        wechatLive.setIsFeedsPublic(roomInfo.getIsFeedsPublic());
        wechatLive.setFeedsImg(roomInfo.getFeedsImg());
        wechatLive.setCreatorOpenid(roomInfo.getCreatorOpenid());
        wechatLive.setProductData(extractProductIds(roomInfo.getGoods()).toString());
        wechatLive.setLastUpdateTime(StringUtils.getCurrentTime());
        if (shopId != null) {
            wechatLive.setShopId(shopId);
        }
        fillRoomDefaultValues(wechatLive);
    }

    private List<Integer> extractProductIds(List<GetLiveInfoResult.ProductInfo> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return Collections.emptyList();
        }
        return goodsList.stream()
                .map(GetLiveInfoResult.ProductInfo::getGoodsId)
                .filter(ObjectUtil::isNotNull)
                .toList();
    }

    private void fillRoomDefaultValues(WechatLive wechatLive) {
        if (wechatLive.getWechatLiveData() == null) {
            wechatLive.setWechatLiveData("");
        }
        if (wechatLive.getActRange() == null) {
            wechatLive.setActRange(0);
        }
        if (wechatLive.getActRangeExt() == null) {
            wechatLive.setActRangeExt("[]");
        }
        if (wechatLive.getShareImg() == null) {
            wechatLive.setShareImg("");
        }
        if (wechatLive.getCoverImg() == null) {
            wechatLive.setCoverImg("");
        }
        if (wechatLive.getAnchorId() == null) {
            wechatLive.setAnchorId("");
        }
        if (wechatLive.getAnchorOpenid() == null) {
            wechatLive.setAnchorOpenid("");
        }
        if (wechatLive.getSubAnchorWechat() == null) {
            wechatLive.setSubAnchorWechat("");
        }
        if (wechatLive.getAnchorName() == null) {
            wechatLive.setAnchorName("");
        }
        if (wechatLive.getAnchorImg() == null) {
            wechatLive.setAnchorImg("");
        }
        if (wechatLive.getCreatorOpenid() == null) {
            wechatLive.setCreatorOpenid("");
        }
        if (wechatLive.getFeedsImg() == null) {
            wechatLive.setFeedsImg("");
        }
        if (wechatLive.getProductData() == null) {
            wechatLive.setProductData("[]");
        }
        if (wechatLive.getThumbImg() == null) {
            wechatLive.setThumbImg("");
        }
        if (wechatLive.getLiveSn() == null) {
            wechatLive.setLiveSn("");
        }
        if (wechatLive.getLiveStatus() == null) {
            wechatLive.setLiveStatus(102);
        }
        if (wechatLive.getAuditStatus() == null) {
            wechatLive.setAuditStatus(0);
        }
        if (wechatLive.getStartTime() == null) {
            wechatLive.setStartTime(0L);
        }
        if (wechatLive.getEndTime() == null) {
            wechatLive.setEndTime(0L);
        }
        if (wechatLive.getLastUpdateTime() == null) {
            wechatLive.setLastUpdateTime(StringUtils.getCurrentTime());
        }
        if (wechatLive.getLiveType() == null) {
            wechatLive.setLiveType(0);
        }
        if (wechatLive.getCloseLike() == null) {
            wechatLive.setCloseLike(0);
        }
        if (wechatLive.getCloseGoods() == null) {
            wechatLive.setCloseGoods(0);
        }
        if (wechatLive.getCloseComment() == null) {
            wechatLive.setCloseComment(0);
        }
        if (wechatLive.getCloseKf() == null) {
            wechatLive.setCloseKf(0);
        }
        if (wechatLive.getCloseReplay() == null) {
            wechatLive.setCloseReplay(0);
        }
        if (wechatLive.getIsFeedsPublic() == null) {
            wechatLive.setIsFeedsPublic(0);
        }
        if (wechatLive.getShopId() == null) {
            wechatLive.setShopId(0);
        }
    }

    private GlobalException wrapWechatLiveException(Exception e) {
        if (isWechatPermissionError(e)) {
            return new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
        }
        if (e instanceof GlobalException) {
            return (GlobalException) e;
        }
        return new GlobalException(e.getMessage());
    }

    private boolean isWechatPermissionError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return false;
        }
        if (msg.contains("48001") || msg.contains("9410")) {
            return true;
        }
        return containsPermissionKeywords(msg);
    }

    private boolean isWechatPermissionResult(Number errcode, String errmsg) {
        if (errcode != null) {
            long code = errcode.longValue();
            if (code == 48001 || (code >= 9410000 && code <= 9419999)) {
                return true;
            }
        }
        return containsPermissionKeywords(errmsg);
    }

    private boolean containsPermissionKeywords(String text) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase();
        return lower.contains("unauthorized")
                || lower.contains("permission")
                || lower.contains("not bindlive")
                || lower.contains("no bindlive")
                || text.contains("权限")
                || text.contains("未授权")
                || text.contains("未开通");
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return "";
        }
        DateTime dateTime = DateUtil.date(timestamp * 1000);
        return DateUtil.format(dateTime, "yyyy-MM-dd HH:mm:ss");
    }

    private Long parseToTimestamp(String dateTimeStr) {
        if (StrUtil.isBlank(dateTimeStr)) {
            return 0L;
        }
        return DateUtil.parse(dateTimeStr, "yyyy-MM-dd HH:mm:ss").getTime() / 1000;
    }

    private List<Integer> parseIntList(String value) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        try {
            return StringUtils.str2IntList(value);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean importGoods(List<WechatLiveGoodsItemDTO> goodsItems) {
        if (goodsItems == null || goodsItems.isEmpty()) {
            throw new GlobalException("商品列表不能为空");
        }
        String accessToken = getAccessToken();
        Integer currentShopId = normalizeShopId(getShopId());
        for (WechatLiveGoodsItemDTO goodsItem : goodsItems) {
            if (goodsItem == null || goodsItem.getProductId() == null) {
                throw new GlobalException("商品ID不能为空");
            }
            Long productId = goodsItem.getProductId();
            Long productSkuId = normalizeProductSkuId(goodsItem.getProductSkuId());
            Product product = productService.getById(productId);
            if (product == null) {
                throw new GlobalException("商品不存在，productId=" + productId);
            }
            LiveGoodsSnapshot snapshot = resolveLiveGoodsSnapshot(product, productSkuId, currentShopId);
            WechatLiveGoods liveGoods = wechatLiveGoodsService.getGoodsByProductIdAndProductSkuIdAndShopId(productId, productSkuId, currentShopId);
            if (liveGoods == null) {
                liveGoods = new WechatLiveGoods();
            }
            if (liveGoods.getWechatGoodsId() == null) {
                liveGoods.setWechatGoodsId(createWechatLiveGoods(accessToken, snapshot));
            }
            liveGoods.setProductId(productId);
            liveGoods.setProductSkuId(productSkuId);
            liveGoods.setGoodsName(snapshot.getGoodsName());
            liveGoods.setCoverImg(snapshot.getCoverImg());
            liveGoods.setPrice(snapshot.getPrice());
            liveGoods.setPriceType(1);
            liveGoods.setUrl(snapshot.getUrl());
            liveGoods.setAuditStatus(2);
            liveGoods.setShopId(currentShopId);
            if (liveGoods.getId() == null) {
                wechatLiveGoodsService.save(liveGoods);
            } else {
                wechatLiveGoodsService.updateById(liveGoods);
            }
        }
        return true;
    }

    @Override
    public Page<WechatLiveGoodsVO> getGoodsLibrary(WechatLiveGoodsListDTO listDTO) {
        Page<WechatLiveGoods> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        String orderByClause = buildGoodsLibraryOrderBy(listDTO.getSortField(), listDTO.getSortOrder());
        Integer shopId = normalizeShopId(getShopId());
        String keyword = StrUtil.isBlank(listDTO.getKeyword()) ? null : listDTO.getKeyword();
        wechatLiveGoodsMapper.selectGoodsLibraryPage(page, shopId, keyword, orderByClause);
        List<WechatLiveGoodsVO> goodsVOList = page.getRecords().stream()
                .map(this::buildGoodsVO)
                .toList();
        return PageUtil.convertPage(page, goodsVOList);
    }

    /**
     * 构造直播商品库分页的排序子句。
     *
     * <p>排序字段、排序方向均采用白名单映射，禁止直接拼接用户输入，防止 SQL 注入。
     * 价格按直播商品快照价 {@code g.price} 排序；库存按实时库存排序
     * （有规格取 SKU 库存，无规格取商品库存）；其余情况默认按主键降序。</p>
     *
     * @param sortField 前端排序字段
     * @param sortOrder 前端排序方向（asc/desc）
     * @return 安全的 ORDER BY 子句
     */
    private String buildGoodsLibraryOrderBy(String sortField, String sortOrder) {
        String column;
        if ("price".equalsIgnoreCase(sortField)) {
            column = "g.price";
        } else if ("stock".equalsIgnoreCase(sortField) || "productStock".equalsIgnoreCase(sortField)) {
            column = "CASE WHEN g.product_sku_id > 0 THEN ps.sku_stock ELSE p.product_stock END";
        } else {
            column = "g.id";
        }
        String direction = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        return column + " " + direction;
    }

    @Override
    public Page<WechatLiveGoodsVO> queryGoods(WechatLiveGoodsQueryDTO queryDTO) {
        if (queryDTO == null || queryDTO.getRoomId() == null) {
            throw new GlobalException("直播间ID不能为空");
        }
        WechatLive wechatLive = getLiveRoom(queryDTO.getRoomId());
        Integer liveShopId = normalizeShopId(wechatLive.getShopId());

        Page<WechatLiveGoods> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        buildSortOrder(page, queryDTO.getSortField(), queryDTO.getSortOrder());
        LambdaQueryWrapper<WechatLiveGoods> goodsQueryWrapper = new LambdaQueryWrapper<WechatLiveGoods>()
                .eq(WechatLiveGoods::getShopId, liveShopId);

        if (queryDTO.getCategoryId() != null || StrUtil.isNotBlank(queryDTO.getKeyword())) {
            LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.select(Product::getProductId);
            if (queryDTO.getCategoryId() != null) {
                productQueryWrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
            }
            if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
                productQueryWrapper.and(wrapper -> wrapper
                        .like(Product::getProductName, queryDTO.getKeyword())
                        .or()
                        .like(Product::getProductSn, queryDTO.getKeyword()));
            }
            List<Long> matchedProductIds = productService.list(productQueryWrapper).stream()
                    .map(Product::getProductId)
                    .filter(Objects::nonNull)
                    .map(Long::valueOf)
                    .toList();
            if (matchedProductIds.isEmpty()) {
                return PageUtil.convertPage(new Page<>(queryDTO.getPage(), queryDTO.getSize()), Collections.emptyList());
            }
            goodsQueryWrapper.in(WechatLiveGoods::getProductId, matchedProductIds);
        }

        Page<WechatLiveGoods> goodsPage = wechatLiveGoodsService.page(page, goodsQueryWrapper);
        List<WechatLiveGoods> liveGoodsList = goodsPage.getRecords();
        if (liveGoodsList.isEmpty()) {
            return PageUtil.convertPage(goodsPage, Collections.emptyList());
        }

        Set<Long> pageGoodsIds = liveGoodsList.stream()
                .map(WechatLiveGoods::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> productIds = liveGoodsList.stream()
                .map(WechatLiveGoods::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> productSkuIds = liveGoodsList.stream()
                .map(WechatLiveGoods::getProductSkuId)
                .filter(Objects::nonNull)
                .filter(skuId -> skuId > 0)
                .collect(Collectors.toSet());

        Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                .filter(product -> product.getProductId() != null)
                .collect(Collectors.toMap(product -> Long.valueOf(product.getProductId()), item -> item, (left, right) -> left));
        Map<Long, ProductSku> productSkuMap = productSkuIds.isEmpty()
                ? Collections.emptyMap()
                : productSkuService.listByIds(productSkuIds).stream()
                .filter(productSku -> productSku.getSkuId() != null)
                .collect(Collectors.toMap(productSku -> Long.valueOf(productSku.getSkuId()), item -> item, (left, right) -> left));

        Set<Long> joinedGoodsIds = new HashSet<>(wechatLiveRoomGoodsService.list(new LambdaQueryWrapper<WechatLiveRoomGoods>()
                        .eq(WechatLiveRoomGoods::getRoomId, queryDTO.getRoomId())
                        .in(WechatLiveRoomGoods::getWechatLiveGoodsId, pageGoodsIds))
                .stream()
                .map(WechatLiveRoomGoods::getWechatLiveGoodsId)
                .filter(Objects::nonNull)
                .toList());

        List<WechatLiveGoodsVO> goodsVOList = liveGoodsList.stream()
                .map(liveGoods -> buildGoodsQueryVO(liveGoods, productMap, productSkuMap, joinedGoodsIds))
                .toList();
        return PageUtil.convertPage(goodsPage, goodsVOList);
    }

    @Override
    public List<WechatLiveGoodsVO> getRoomGoods(Integer roomId) {
        WechatLive wechatLive = getLiveRoom(roomId);
        List<WechatLiveRoomGoods> roomGoodsList = wechatLiveRoomGoodsService.getGoodsByRoomId(wechatLive.getRoomId());
        if (roomGoodsList.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, WechatLiveGoods> goodsMap = wechatLiveGoodsService.listByIds(roomGoodsList.stream()
                        .map(WechatLiveRoomGoods::getWechatLiveGoodsId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(WechatLiveGoods::getId, item -> item, (left, right) -> left));
        List<WechatLiveGoodsVO> goodsVOList = new ArrayList<>();
        for (WechatLiveRoomGoods roomGoods : roomGoodsList) {
            WechatLiveGoods liveGoods = goodsMap.get(roomGoods.getWechatLiveGoodsId());
            if (liveGoods == null) {
                continue;
            }
            WechatLiveGoodsVO vo = buildGoodsVO(liveGoods);
            vo.setRoomGoodsId(roomGoods.getId());
            vo.setRoomId(roomGoods.getRoomId());
            vo.setSort(roomGoods.getSort());
            vo.setIsShow(roomGoods.getIsShow());
            goodsVOList.add(vo);
        }
        return goodsVOList;
    }

    @Override
    public boolean addGoods(Integer roomId, List<Long> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            throw new GlobalException("直播商品ID列表不能为空");
        }
        WechatLive wechatLive = getLiveRoom(roomId);
        Integer liveShopId = normalizeShopId(wechatLive.getShopId());

        Map<Long, Integer> goodsOrderMap = new LinkedHashMap<>();
        for (Long goodsId : goodsIds) {
            if (goodsId != null) {
                goodsOrderMap.put(goodsId, goodsOrderMap.size() + 1);
            }
        }
        if (goodsOrderMap.isEmpty()) {
            throw new GlobalException("直播商品ID列表不能为空");
        }

        List<Integer> wechatGoodsIds = new ArrayList<>();
        List<WechatLiveRoomGoods> pendingSaveList = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : goodsOrderMap.entrySet()) {
            Long goodsId = entry.getKey();
            Integer sort = entry.getValue();
            WechatLiveGoods liveGoods = wechatLiveGoodsService.getGoodsByIdAndShopId(goodsId, liveShopId);
            if (liveGoods == null) {
                throw new GlobalException("直播商品不存在，goodsId=" + goodsId);
            }
            if (liveGoods.getWechatGoodsId() == null) {
                throw new GlobalException("直播商品缺少微信商品ID，goodsId=" + goodsId);
            }
            productSkuService.checkSkuEnabled(liveGoods.getProductId().intValue(),
                    liveGoods.getProductSkuId() == null ? 0 : liveGoods.getProductSkuId().intValue());
            WechatLiveRoomGoods roomGoods = wechatLiveRoomGoodsService.getGoodsByRoomIdAndGoodsId(roomId, goodsId);
            if (roomGoods == null) {
                roomGoods = new WechatLiveRoomGoods();
                roomGoods.setRoomId(roomId);
                roomGoods.setWechatLiveGoodsId(goodsId);
                roomGoods.setWechatGoodsId(liveGoods.getWechatGoodsId());
                roomGoods.setShopId(liveShopId);
                roomGoods.setIsShow(0);
                wechatGoodsIds.add(liveGoods.getWechatGoodsId());
            }
            roomGoods.setSort(sort);
            pendingSaveList.add(roomGoods);
        }

        if (!wechatGoodsIds.isEmpty()) {
            String accessToken = getAccessToken();
            AddGoodsParam param = new AddGoodsParam();
            param.setRoomId(roomId);
            param.setGoodsIds(wechatGoodsIds);
            AddGoodsResult result = wechatApiClient.addGoods(accessToken, param);
            if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
                if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                    throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
                }
                throw new GlobalException("添加商品到直播间失败：" + (result != null ? result.getErrmsg() : ""));
            }
        }

        for (WechatLiveRoomGoods roomGoods : pendingSaveList) {
            if (roomGoods.getId() == null) {
                wechatLiveRoomGoodsService.save(roomGoods);
            } else {
                wechatLiveRoomGoodsService.updateById(roomGoods);
            }
        }
        return true;
    }

    private Integer createWechatLiveGoods(String accessToken, LiveGoodsSnapshot snapshot) {
        String mediaId = uploadGoodsImageAsMediaId(accessToken, snapshot.getCoverImg(), snapshot.getGoodsName());

        BroadcastGoodsInfoParam goodsInfo = new BroadcastGoodsInfoParam();
        goodsInfo.setCoverImgUrl(mediaId);
        goodsInfo.setName(snapshot.getGoodsName());
        goodsInfo.setPriceType(1);
        goodsInfo.setPrice(snapshot.getPrice() == null ? 0D : BigDecimal.valueOf(snapshot.getPrice()).divide(BigDecimal.valueOf(100)).doubleValue());
        goodsInfo.setUrl(snapshot.getUrl());

        BroadcastGoodsAddParam addParam = new BroadcastGoodsAddParam();
        addParam.setGoodsInfo(goodsInfo);
        BroadcastGoodsAddResult addResult = wechatApiClient.addBroadcastGoods(accessToken, addParam);
        if (addResult == null || ObjectUtil.notEqual(addResult.getErrcode(), 0) || addResult.getGoodsId() == null) {
            if (addResult != null && isWechatPermissionResult(addResult.getErrcode(), addResult.getErrmsg())) {
                throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
            }
            throw new GlobalException("直播商品创建失败：" + (addResult != null ? addResult.getErrmsg() : ""));
        }
        return addResult.getGoodsId();
    }

    private String uploadGoodsImageAsMediaId(String accessToken, String imageUrl, String goodsName) {
        log.info("[直播商品图片上传] 开始处理，商品名称={}，原始imageUrl={}", goodsName, imageUrl);
        if (StrUtil.isBlank(imageUrl)) {
            throw new GlobalException("商品【" + goodsName + "】主图不能为空");
        }

        imageUrl = buildFullImageUrl(imageUrl);
        log.info("[直播商品图片上传] 完整图片地址，商品名称={}，imageUrl={}", goodsName, imageUrl);

        byte[] imageBytes = normalizeImageForWechat(imageUrl, goodsName);
        log.info("[直播商品图片上传] 图片处理完成，商品名称={}，大小={}KB", goodsName, imageBytes.length / 1024);

        String mediaId = uploadMaterialByUrl(
                "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=" + accessToken + "&type=image",
                imageBytes
        );
        if (StrUtil.isNotBlank(mediaId)) {
            log.info("[直播商品图片上传] 临时素材上传成功，商品名称={}，mediaId={}", goodsName, mediaId);
            return mediaId;
        }
        log.warn("[直播商品图片上传] 临时素材上传失败，尝试永久素材，商品名称={}", goodsName);

        mediaId = uploadMaterialByUrl(
                "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=" + accessToken + "&type=image",
                imageBytes
        );
        if (StrUtil.isBlank(mediaId)) {
            log.error("[直播商品图片上传] 永久素材上传也失败，商品名称={}", goodsName);
            throw new GlobalException("商品【" + goodsName + "】图片上传微信素材失败");
        }
        log.info("[直播商品图片上传] 永久素材上传成功，商品名称={}，mediaId={}", goodsName, mediaId);
        return mediaId;
    }

    /**
     * 补全图片URL，确保是完整的http(s)地址
     */
    private String buildFullImageUrl(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return imageUrl;
        }
        // 已经是完整URL，直接返回
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            log.debug("[图片URL] 已是完整地址：{}", imageUrl);
            return imageUrl;
        }
        String storageUrl = configService.getStorageUrl();
        log.info("[图片URL] 需要拼接前缀，storageUrl={}，imageUrl={}", storageUrl, imageUrl);
        if (StrUtil.isBlank(storageUrl)) {
            throw new GlobalException("存储域名未配置，无法拼接图片地址：" + imageUrl);
        }
        // 去掉storageUrl尾部和imageUrl头部多余的斜杠，避免双斜杠
        storageUrl = StrUtil.removeSuffix(storageUrl, "/");
        imageUrl = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        String fullUrl = storageUrl + imageUrl;
        log.info("[图片URL] 拼接结果：{}", fullUrl);
        return fullUrl;
    }

    private String uploadMaterialByUrl(String requestUrl, byte[] imageBytes) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("wechat_live_goods_", ".jpg");
            FileUtil.writeBytes(imageBytes, tempFile);
            String response = HttpRequest.post(requestUrl)
                    .form("media", tempFile)
                    .timeout(20000)
                    .execute()
                    .body();
            WechatUploadMaterialResult result = JSONUtil.toBean(response, WechatUploadMaterialResult.class);
            if (result == null || StrUtil.isBlank(result.getMediaId())) {
                log.warn("[素材上传] 微信返回无mediaId，response={}", response);
                return null;
            }
            return result.getMediaId();
        } catch (Exception e) {
            log.error("[素材上传] 上传异常，url={}，原因={}", requestUrl, e.getMessage());
            return null;
        } finally {
            if (tempFile != null) {
                FileUtil.del(tempFile);
            }
        }
    }

    private byte[] normalizeImageForWechat(String imageUrl, String goodsName) {
        try {
            log.info("[图片处理] 开始下载图片，商品名称={}，url={}", goodsName, imageUrl);
            byte[] sourceBytes = HttpRequest.get(imageUrl)
                    .timeout(20000)
                    .execute()
                    .bodyBytes();
            if (sourceBytes == null || sourceBytes.length == 0) {
                log.error("[图片处理] 下载为空，商品名称={}，url={}", goodsName, imageUrl);
                throw new GlobalException("商品【" + goodsName + "】图片下载为空，请检查图片地址是否可访问");
            }
            log.info("[图片处理] 下载完成，商品名称={}，原始大小={}KB", goodsName, sourceBytes.length / 1024);

            // 检测实际图片格式（可能扩展名与实际格式不一致）
            String detectedFormat = detectImageFormat(sourceBytes);
            log.info("[图片处理] 检测到图片实际格式={}，商品名称={}", detectedFormat, goodsName);

            BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourceBytes));
            if (source == null) {
                log.warn("[图片处理] ImageIO.read返回null，尝试ImageReader兜底，商品名称={}", goodsName);
                source = readImageWithReader(sourceBytes);
            }
            if (source == null) {
                log.error("[图片处理] 所有方式均无法解析图片，商品名称={}，url={}，字节数={}", goodsName, imageUrl, sourceBytes.length);
                throw new GlobalException("商品【" + goodsName + "】图片格式不支持或内容损坏");
            }

            // 如果不是RGB色彩模式，强制转为RGB
            if (source.getType() != BufferedImage.TYPE_INT_RGB && source.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                log.info("[图片处理] 色彩模式转换，原类型={}，商品名称={}", source.getType(), goodsName);
                BufferedImage rgbImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgbImage.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, source.getWidth(), source.getHeight());
                g.drawImage(source, 0, 0, null);
                g.dispose();
                source = rgbImage;
            }

            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            log.info("[图片处理] 原始尺寸={}x{}，商品名称={}", sourceWidth, sourceHeight, goodsName);

            // 目标最小300x300，大图等比缩小，小图等比放大
            int targetWidth, targetHeight;
            if (sourceWidth >= 300 && sourceHeight >= 300) {
                double scale = Math.min(1D, Math.min(300D / sourceWidth, 300D / sourceHeight));
                targetWidth = Math.max(300, (int) Math.round(sourceWidth * scale));
                targetHeight = Math.max(300, (int) Math.round(sourceHeight * scale));
            } else {
                double scale = Math.max(300D / sourceWidth, 300D / sourceHeight);
                targetWidth = (int) Math.round(sourceWidth * scale);
                targetHeight = (int) Math.round(sourceHeight * scale);
            }

            BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = target.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();

            // 先用默认质量压缩，超过2MB则逐步降低质量
            float quality = 0.9f;
            byte[] result = compressToJpeg(target, quality);
            while (result.length > 2 * 1024 * 1024 && quality > 0.1f) {
                quality -= 0.1f;
                result = compressToJpeg(target, quality);
            }
            if (result.length > 2 * 1024 * 1024) {
                throw new GlobalException("商品【" + goodsName + "】图片压缩后仍超过2MB");
            }
            return result;
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("商品【" + goodsName + "】图片处理失败，原因：" + e.getMessage());
        }
    }

    /**
     * 使用ImageReader兜底读取图片（处理CMYK等特殊格式）
     */
    private BufferedImage readImageWithReader(byte[] imageBytes) {
        try (javax.imageio.stream.ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            java.util.Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                javax.imageio.ImageReader reader = readers.next();
                try {
                    reader.setInput(iis, true, true);
                    javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
                    BufferedImage image = reader.read(0, param);
                    if (image != null) {
                        log.info("[图片处理] ImageReader兜底成功，format={}", reader.getFormatName());
                        return image;
                    }
                } catch (Exception e) {
                    log.warn("[图片处理] ImageReader尝试失败：{}", e.getMessage());
                } finally {
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            log.warn("[图片处理] ImageReader兜底异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 通过文件头魔数检测图片实际格式
     */
    private String detectImageFormat(byte[] data) {
        if (data == null || data.length < 12) {
            return "unknown";
        }
        // JPEG: FF D8 FF
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return "JPEG";
        }
        // PNG: 89 50 4E 47
        if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return "PNG";
        }
        // GIF: 47 49 46
        if (data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46) {
            return "GIF";
        }
        // WebP: RIFF....WEBP
        if (data[0] == 0x52 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x46
                && data[8] == 0x57 && data[9] == 0x45 && data[10] == 0x42 && data[11] == 0x50) {
            return "WEBP";
        }
        // BMP: 42 4D
        if (data[0] == 0x42 && data[1] == 0x4D) {
            return "BMP";
        }
        // TIFF: 49 49 2A 00 (little-endian) 或 4D 4D 00 2A (big-endian)
        if ((data[0] == 0x49 && data[1] == 0x49 && data[2] == 0x2A && data[3] == 0x00)
                || (data[0] == 0x4D && data[1] == 0x4D && data[2] == 0x00 && data[3] == 0x2A)) {
            return "TIFF";
        }
        // PSD: 38 42 50 53
        if (data[0] == 0x38 && data[1] == 0x42 && data[2] == 0x50 && data[3] == 0x53) {
            return "PSD";
        }
        return "unknown";
    }

    private byte[] compressToJpeg(BufferedImage image, float quality) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new GlobalException("图片压缩失败：" + e.getMessage());
        }
    }

    private String cropWechatGoodsName(String rawName) {
        if (StrUtil.isBlank(rawName)) {
            throw new GlobalException("商品名称不能为空");
        }
        String normalized = rawName.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (StrUtil.isBlank(normalized)) {
            throw new GlobalException("商品名称不能为空");
        }
        int usedUnits = 0;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            usedUnits += (ch <= 127 ? 1 : 2);
            if (usedUnits > 28) {
                throw new GlobalException("商品名称超出微信限制：最长14个汉字（或28个字符单位）");
            }
        }
        return normalized;
    }

    private String buildWechatGoodsPath(Integer productId) {
        return "pages/product/index?id=" + productId;
    }

    private Long getPriceCent(BigDecimal price) {
        if (price == null) {
            return 0L;
        }
        return price.multiply(BigDecimal.valueOf(100)).longValue();
    }

    @Override
    public boolean removeGoods(Integer roomId, Long goodsId) {
        getLiveRoom(roomId);
        WechatLiveRoomGoods roomGoods = wechatLiveRoomGoodsService.getGoodsByRoomIdAndGoodsId(roomId, goodsId);
        if (roomGoods == null) {
            return true;
        }

        String accessToken = getAccessToken();
        DeleteGoodsParam param = new DeleteGoodsParam();
        param.setRoomId(roomId);
        param.setGoodsId(roomGoods.getWechatGoodsId());
        WechatApiResult result = wechatApiClient.deleteGoods(accessToken, param);
        if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
            if (result != null && isWechatPermissionResult(result.getErrcode(), result.getErrmsg())) {
                throw new GlobalException(WECHAT_LIVE_PERMISSION_NOT_ENABLED);
            }
            throw new GlobalException("从直播间移除商品失败：" + (result != null ? result.getErrmsg() : ""));
        }

        wechatLiveRoomGoodsService.removeById(roomGoods.getId());
        return true;
    }

    private WechatLive getLiveRoom(Integer roomId) {
        WechatLive wechatLive = this.getOne(new LambdaQueryWrapper<WechatLive>()
                .eq(WechatLive::getRoomId, roomId)
                .eq(WechatLive::getShopId, getShopId()));
        if (wechatLive == null) {
            throw new GlobalException(WECHAT_LIVE_ROOM_NOT_EXISTS);
        }
        return wechatLive;
    }

    private WechatLiveGoodsVO buildGoodsVO(WechatLiveGoods liveGoods) {
        WechatLiveGoodsVO vo = new WechatLiveGoodsVO();
        BeanUtils.copyProperties(liveGoods, vo);
        return vo;
    }

    private WechatLiveGoodsVO buildGoodsQueryVO(WechatLiveGoods liveGoods,
                                                Map<Long, Product> productMap,
                                                Map<Long, ProductSku> productSkuMap,
                                                Set<Long> joinedGoodsIds) {
        WechatLiveGoodsVO vo = buildGoodsVO(liveGoods);
        Product product = productMap.get(liveGoods.getProductId());
        if (product != null && StrUtil.isNotBlank(product.getProductName())) {
            vo.setGoodsName(product.getProductName());
        }
        if (liveGoods.getProductSkuId() != null && liveGoods.getProductSkuId() > 0) {
            ProductSku productSku = productSkuMap.get(liveGoods.getProductSkuId());
            vo.setSkuData(productSku == null ? "" : productSku.getSkuData());
        } else {
            vo.setSkuData("");
        }
        vo.setIsJoinedRoom(joinedGoodsIds.contains(liveGoods.getId()) ? 1 : 0);
        return vo;
    }

    private LiveGoodsSnapshot resolveLiveGoodsSnapshot(Product product, Long productSkuId, Integer liveShopId) {
        Integer sourceShopId = normalizeShopId(product.getShopId());
        ProductSku productSku = getProductSku(product, productSkuId);
        BigDecimal price = productSku != null ? productSku.getSkuPrice() : product.getProductPrice();

        if (ObjectUtil.equal(sourceShopId, liveShopId)) {
            return buildLiveGoodsSnapshot(product, price);
        }
        if (!ObjectUtil.equal(sourceShopId, 0) || ObjectUtil.equal(liveShopId, 0)) {
            throw new GlobalException("直播间所属店铺与商品所属店铺不匹配，productId=" + product.getProductId());
        }

        return buildLiveGoodsSnapshot(product, price);
    }

    private ProductSku getProductSku(Product product, Long productSkuId) {
        if (productSkuId == null || productSkuId <= 0) {
            return null;
        }
        ProductSku productSku = productSkuService.getById(productSkuId);
        if (productSku == null || !ObjectUtil.equal(productSku.getProductId(), product.getProductId())) {
            throw new GlobalException("商品规格不存在，productId=" + product.getProductId() + ", skuId=" + productSkuId);
        }
        if (Objects.equals(productSku.getIsEnabled(), Constants.NO)) {
            throw new GlobalException("商品规格已禁用，请重新选择");
        }
        return productSku;
    }

    private LiveGoodsSnapshot buildLiveGoodsSnapshot(Product product, BigDecimal price) {
        LiveGoodsSnapshot snapshot = new LiveGoodsSnapshot();
        snapshot.setProductId(product.getProductId());
        snapshot.setGoodsName(cropWechatGoodsName(product.getProductName()));
        snapshot.setCoverImg(product.getPicUrl());
        snapshot.setPrice(getPriceCent(price));
        snapshot.setUrl(buildWechatGoodsPath(product.getProductId()));
        return snapshot;
    }

    private Long normalizeProductSkuId(Long productSkuId) {
        return productSkuId == null || productSkuId <= 0 ? 0L : productSkuId;
    }

    private Integer normalizeShopId(Integer shopId) {
        return shopId == null || shopId < 0 ? 0 : shopId;
    }

    private void syncRoomGoods(WechatLive wechatLive, List<GetLiveInfoResult.ProductInfo> goodsList) {
        if (wechatLive == null || goodsList == null) {
            return;
        }
        int sort = 1;
        for (GetLiveInfoResult.ProductInfo goods : goodsList) {
            Integer shopId = normalizeShopId(wechatLive.getShopId());
            WechatLiveGoods liveGoods = wechatLiveGoodsService.getGoodsByWechatGoodsIdAndShopId(goods.getGoodsId(), shopId);
            if (liveGoods == null) {
                liveGoods = new WechatLiveGoods();
                liveGoods.setShopId(shopId);
            }
            if (liveGoods.getProductSkuId() == null) {
                liveGoods.setProductSkuId(0L);
            }
            liveGoods.setWechatGoodsId(goods.getGoodsId());
            liveGoods.setGoodsName(goods.getName());
            liveGoods.setCoverImg(goods.getCoverImg());
            liveGoods.setPrice(goods.getPrice() != null ? goods.getPrice().longValue() : null);
            liveGoods.setPriceType(goods.getPriceType());
            liveGoods.setUrl(goods.getUrl());
            liveGoods.setAuditStatus(2);
            if (liveGoods.getId() == null) {
                wechatLiveGoodsService.save(liveGoods);
            } else {
                wechatLiveGoodsService.updateById(liveGoods);
            }

            WechatLiveRoomGoods roomGoods = wechatLiveRoomGoodsService.getGoodsByRoomIdAndWechatGoodsId(wechatLive.getRoomId(), goods.getGoodsId());
            if (roomGoods == null) {
                roomGoods = new WechatLiveRoomGoods();
                roomGoods.setRoomId(wechatLive.getRoomId());
                roomGoods.setWechatLiveGoodsId(liveGoods.getId());
                roomGoods.setWechatGoodsId(goods.getGoodsId());
                roomGoods.setShopId(shopId);
                roomGoods.setIsShow(0);
            }
            roomGoods.setSort(sort++);
            roomGoods.setWechatLiveGoodsId(liveGoods.getId());
            if (roomGoods.getId() == null) {
                wechatLiveRoomGoodsService.save(roomGoods);
            } else {
                wechatLiveRoomGoodsService.updateById(roomGoods);
            }
        }
    }

    private static class LiveGoodsSnapshot {
        private Integer productId;
        private String goodsName;
        private String coverImg;
        private Long price;
        private String url;

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public String getCoverImg() {
            return coverImg;
        }

        public void setCoverImg(String coverImg) {
            this.coverImg = coverImg;
        }

        public Long getPrice() {
            return price;
        }

        public void setPrice(Long price) {
            this.price = price;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private void fillStats(WechatLiveVO wechatLiveVO) {
        if (wechatLiveVO.getRoomId() == null) {
            return;
        }
        long orderCount = wechatLiveOrderRelService.count(new LambdaQueryWrapper<com.tigshop.bean.model.content.WechatLiveOrderRel>()
                .eq(com.tigshop.bean.model.content.WechatLiveOrderRel::getRoomId, wechatLiveVO.getRoomId()));
        long syncFailedCount = wechatLiveSyncLogService.count(new LambdaQueryWrapper<com.tigshop.bean.model.content.WechatLiveSyncLog>()
                .eq(com.tigshop.bean.model.content.WechatLiveSyncLog::getRoomId, wechatLiveVO.getRoomId())
                .eq(com.tigshop.bean.model.content.WechatLiveSyncLog::getSyncStatus, 2));
        wechatLiveVO.setOrderCount(orderCount);
        wechatLiveVO.setSyncFailedCount(syncFailedCount);
    }

    /**
     * 获取视频号当前的直播记录
     * @param id 视频号id，用于匹配直播记录
     * @return 匹配的直播记录，未匹配到返回null
     */
    @Override
    public GetFinderLiveRecordListResult.LiveRecord getFinderLiveRecord(String id) {
        String accessToken = getAccessToken();
        GetFinderLiveRecordListResult result = wechatApiClient.getFinderLiveRecordList(accessToken);
        if (result == null || (result.getErrcode() != null && result.getErrcode() != 0)) {
            String errMsg = result != null ? result.getErrmsg() : "未知错误";
            throw new GlobalException("获取视频号直播记录失败：" + errMsg);
        }
        if (result.getLiveList() == null || result.getLiveList().isEmpty()) {
            return null;
        }
        return result.getLiveList().stream()
                .filter(record -> record.getExportId() != null && record.getExportId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
