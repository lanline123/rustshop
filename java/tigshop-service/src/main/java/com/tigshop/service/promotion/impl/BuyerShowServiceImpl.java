package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.promotion.BuyerShowDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserFollow;
import com.tigshop.bean.model.user.VirtualUserInfo;
import com.tigshop.bean.param.promotion.buyershow.*;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowCommentPageClientQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowPageClientQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowPageQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowVideoFeedClientQuery;
import com.tigshop.bean.vo.promotion.buyershow.*;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.promotion.BuyerShowMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.user.UserFollowService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.user.VirtualUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
* @author wzh
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class BuyerShowServiceImpl extends BaseServiceImpl<BuyerShowMapper, BuyerShow>
    implements BuyerShowService {

    private final BuyerShowVideoService buyerShowVideoService;

    private final BuyerShowGalleryService buyerShowGalleryService;

    private final GalleryPicService galleryPicService;

    private final BuyerShowGroupService buyerShowGroupService;

    private final BuyerShowCommentService buyerShowCommentService;

    private final UserService userService;

    private final BuyerShowLikeService buyerShowLikeService;

    private final ProductService productService;

    private final ProductSkuService productSkuService;

    private final CommentLikeService commentLikeService;

    private final VirtualUserInfoService virtualUserInfoService;

    private final ConfigService configService;

    private final BuyerShowFavoriteService buyerShowFavoriteService;

    private final UserFollowService userFollowService;

    /**
     * 视频流发布时间格式，与前端契约一致
     */
    private static final String FEED_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * 素材默认排序值。buyer_show_video.sort_order、buyer_show_gallery.sort_order 均为 NOT NULL DEFAULT 0，
     * 此处仅作历史脏数据的防御性归一化，与 videoFeed SQL 中 COALESCE 默认值保持一致。
     */
    private static final int DEFAULT_SORT_ORDER = 0;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(BuyerShowSaveParam param) {
        //1、校验
        if (CollUtil.isEmpty(param.getImgList()) && CollUtil.isEmpty(param.getBuyerShowVideoList())) {
            throw new GlobalException("买家秀图片和视频不能同时为空");
        }

        //2、构建并保存买家秀实体信息
        BuyerShow buyerShow = new BuyerShow();
        BeanUtils.copyProperties(param, buyerShow);
        buyerShow.setRangeData(JSONUtil.toJsonStr(param.getRangeData()));
        buyerShow.setGroupData(CollUtil.isNotEmpty(param.getGroupData()) ? JSONUtil.toJsonStr(param.getGroupData()) : null);
        //2.1、买家秀默认保存信息
        buyerShow.setAddTime(StringUtils.getCurrentTime());
        buyerShow.setStatus(Constants.YES);
        buyerShow.setIsShow(Constants.YES);
        buyerShow.setUserId(0);
        this.save(buyerShow);

        //3、保存买家秀视频信息
        this.saveOrUpdateBuyerShowVideo(param.getBuyerShowVideoList(), buyerShow.getBuyerShowId());

        //4、保存买家秀图片信息
        this.saveOrUpdateBuyerShowGallery(param.getImgList(), buyerShow.getBuyerShowId());
    }

    @Override
    public void info() throws Exception {

        for (int i = 0; i < 100; i++) {
            VirtualUserInfo virtualUserInfo = new VirtualUserInfo();
            virtualUserInfo.setNickname(RandomInfoUtil.getRandomNickname());

            String photoUrl = RandomInfoUtil.getRandomPhoto();
            MultipartFile multipartFile = MultipartFileUtil.urlToMultipartFile(photoUrl);
            String buyerPhoto = galleryPicService.uploadImg(multipartFile);
            virtualUserInfo.setPhoto(buyerPhoto);
            virtualUserInfoService.save(virtualUserInfo);

            // 每次循环后睡眠10秒
            try {
                // 10秒 = 10000毫秒
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // 处理中断异常，可以选择恢复中断状态或记录日志
                Thread.currentThread().interrupt();
                // 或者记录日志：logger.error("线程睡眠被中断", e);
                break; // 如果被中断，可以选择退出循环
            }
        }
    }

    @Override
    public BuyerVO getBuyerInfo() {
        VirtualUserInfo virtualUserInfo = virtualUserInfoService.lambdaQuery()
                .last("ORDER BY RAND() LIMIT 1")
                .one();
        return new BuyerVO(virtualUserInfo.getPhoto(), virtualUserInfo.getNickname());
    }

    @Override
    public Page<BuyerShowPageVO> list(BuyerShowPageQuery query) {
        // 分页
        Page<BuyerShow> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<BuyerShow> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, query.getSortField(), query.getSortOrder());

        queryWrapper.eq(query.getStatus() != null && query.getStatus() == -1, BuyerShow::getStatus, 0)
                .ne(query.getStatus() != null && query.getStatus() > -1, BuyerShow::getStatus, 0)
                .eq(query.getStatus() != null && query.getStatus() > -1, BuyerShow::getIsShow, query.getStatus())
                .like(StrUtil.isNotBlank(query.getKeyword()), BuyerShow::getContent, query.getKeyword());
        if (query.getGoodsId() != null) {
            queryWrapper.apply("JSON_CONTAINS(group_data, {0})", query.getGoodsId().toString());
        }
        if (StrUtil.isNotBlank(query.getStartTime()) && StrUtil.isNotBlank(query.getEndTime())) {
            queryWrapper.between(BuyerShow::getAddTime,
                    TigUtils.toTimestampYmd(query.getStartTime()), TigUtils.toTimestampYmd(query.getEndTime()));
        }

        // 执行查询
        Page<BuyerShow> buyerShowPage = this.page(page, queryWrapper);

        List<BuyerShow> buyerShowList = buyerShowPage.getRecords();
        if (CollUtil.isEmpty(buyerShowList)) {
            return new Page<>();
        }
        List<Integer> buyerShowIds = buyerShowList.stream().map(BuyerShow::getBuyerShowId).toList();

        BuyerShowDTO buyerShowDTO = this.getBuyerShowDTO(buyerShowList);

        //获取评论数
        Map<Integer, Long> buyerShowCommentMap = buyerShowCommentService.lambdaQuery()
                .in(BuyerShowComment::getBuyerShowId, buyerShowIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(BuyerShowComment::getBuyerShowId, Collectors.counting()));

        List<BuyerShowGroup> buyerShowGroupList = buyerShowGroupService.list();

        List<BuyerShowPageVO> buyerShowPageVOList = buyerShowList.stream()
                .map(buyerShow -> {
                    BuyerShowPageVO buyerShowPageVO = new BuyerShowPageVO();
                    BeanUtils.copyProperties(buyerShow, buyerShowPageVO);
                    if (buyerShow.getUserId() > 0) {
                        User user = buyerShowDTO.getUserMap().get(buyerShow.getUserId());
                        if (user != null) {
                            buyerShowPageVO.setBuyerName(user.getName());
                        }
                    }
                    if (CollUtil.isNotEmpty(buyerShowDTO.getBuyerShowLikeList())) {
                        long count = buyerShowDTO.getBuyerShowLikeList().stream()
                                .filter(buyerShowLike -> buyerShowLike.getBuyerShowId().equals(buyerShow.getBuyerShowId()))
                                .count();
                        buyerShowPageVO.setLikeCount(count);
                    }
                    String type = buyerShow.getUserId() == 0 ? "(系统)" : "(客户)";
                    buyerShowPageVO.setBuyerName(buyerShow.getUserId()==0 ? buyerShow.getBuyerName() :
                            buyerShowPageVO.getBuyerName() + type);
                    buyerShowPageVO.setImgList(buyerShowDTO.getBuyerShowGalleryMap().getOrDefault(buyerShow.getBuyerShowId(), new ArrayList<>()));
                    buyerShowPageVO.setVideoList(buyerShowDTO.getBuyerShowVideoMap().getOrDefault(buyerShow.getBuyerShowId(), new ArrayList<>()));
                    buyerShowPageVO.setCommentCount(buyerShowCommentMap.getOrDefault(buyerShow.getBuyerShowId(), 0L));
                    buyerShowPageVO.setAddTime(TigUtils.handelTime(buyerShow.getAddTime()));
                    buyerShowPageVO.setProductCount(JSONUtil.toList(buyerShow.getRangeData(), Integer.class).size());
                    String statusText = buyerShow.getStatus() == 0 ? "待审核" : buyerShow.getIsShow() == 1 ? "公开" : "隐藏";
                    buyerShowPageVO.setStatusText(statusText);
                    if (StrUtil.isNotBlank(buyerShow.getGroupData())) {
                        List<Integer> groupIds = JSONUtil.toList(buyerShow.getGroupData(), Integer.class);
                        List<String> groupNameList = buyerShowGroupList.stream()
                                .filter(buyerShowGroup -> groupIds.contains(buyerShowGroup.getBuyerShowGroupId()))
                                .map(BuyerShowGroup::getGroupName)
                                .toList();
                        buyerShowPageVO.setGroupList(groupNameList);
                    }

                    return buyerShowPageVO;
                }).toList();


        return PageUtil.convertPage(buyerShowPage, buyerShowPageVOList);
    }

    @Override
    public BuyerShowVO detail(Integer buyerShowId) {
        BuyerShowVO buyerShowVO = new BuyerShowVO();

        BuyerShow buyerShow = this.getById(buyerShowId);
        Assert.notNull(buyerShow, "买家秀不存在");
        BeanUtils.copyProperties(buyerShow, buyerShowVO);
        if (buyerShow.getUserId() > 0) {
            User user = userService.getById(buyerShow.getUserId());
            if (user != null) {
                buyerShowVO.setBuyerName(user.getName());
                buyerShowVO.setBuyerPhoto(user.getAvatar());
            }
        }
        buyerShowVO.setRangeData(JSONUtil.toList(buyerShow.getRangeData(), Integer.class));
        List<BuyerShowGallery> buyerShowGalleryList = buyerShowGalleryService.lambdaQuery()
                .eq(BuyerShowGallery::getBuyerShowId, buyerShowId)
                .list();
        buyerShowVO.setImgList(buyerShowGalleryList);

        List<BuyerShowVideo> buyerShowVideoList = buyerShowVideoService.lambdaQuery()
                .eq(BuyerShowVideo::getBuyerShowId, buyerShowId)
                .list();
        buyerShowVO.setVideoList(buyerShowVideoList);

        if (StrUtil.isNotBlank(buyerShow.getGroupData())) {
            List<Integer> groupIdList = JSONUtil.toList(buyerShow.getGroupData(), Integer.class);
            List<String> groupNameList = buyerShowGroupService.lambdaQuery()
                    .in(BuyerShowGroup::getBuyerShowGroupId, groupIdList)
                    .list()
                    .stream()
                    .map(BuyerShowGroup::getGroupName)
                    .toList();
            buyerShowVO.setGroupNameList(groupNameList);
            buyerShowVO.setGroupIdList(groupIdList);
        }

        return buyerShowVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BuyerShowUpdateParam param) {
        //1、校验
        if (CollUtil.isEmpty(param.getImgList()) && CollUtil.isEmpty(param.getBuyerShowVideoList())) {
            throw new GlobalException("买家秀图片和视频不能同时为空");
        }

        BuyerShow buyerShow = this.getById(param.getBuyerShowId());
        if (buyerShow == null) {
            throw new GlobalException("买家秀不存在");
        }

        //2、更新
        BeanUtils.copyProperties(param, buyerShow);
        buyerShow.setRangeData(JSONUtil.toJsonStr(param.getRangeData()));
        buyerShow.setGroupData(CollUtil.isNotEmpty(param.getGroupData()) ? JSONUtil.toJsonStr(param.getGroupData()) : null);

        //3、保存买家秀视频信息
        this.saveOrUpdateBuyerShowVideo(param.getBuyerShowVideoList(), buyerShow.getBuyerShowId());

        //4、保存买家秀图片信息
        this.saveOrUpdateBuyerShowGallery(param.getImgList(), buyerShow.getBuyerShowId());

        this.updateById(buyerShow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        return deleteAll(List.of(id));
    }

    static boolean isDeletableStatus(Integer status) {
        return Objects.equals(status, 1) || Objects.equals(status, 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDel(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new GlobalException("买家秀主键不能为空");
        }

        return deleteAll(ids);
    }

    private boolean deleteAll(List<Integer> ids) {
        List<Integer> buyerShowIds = ids.stream()
                .distinct()
                .sorted()
                .toList();
        List<BuyerShow> buyerShows = this.lambdaQuery()
                .in(BuyerShow::getBuyerShowId, buyerShowIds)
                .orderByAsc(BuyerShow::getBuyerShowId)
                .last("FOR UPDATE")
                .list();
        if (buyerShows.size() != buyerShowIds.size()) {
            throw new GlobalException("买家秀不存在");
        }
        if (buyerShows.stream().map(BuyerShow::getStatus).anyMatch(status -> !isDeletableStatus(status))) {
            throw new GlobalException("待审核买家秀不允许删除");
        }

        List<Integer> commentIds = buyerShowCommentService.lambdaQuery()
                .in(BuyerShowComment::getBuyerShowId, buyerShowIds)
                .list()
                .stream()
                .map(BuyerShowComment::getBuyerShowCommentId)
                .toList();
        if (CollUtil.isNotEmpty(commentIds)) {
            commentLikeService.lambdaUpdate()
                    .in(CommentLike::getCommentId, commentIds)
                    .remove();
        }
        buyerShowCommentService.lambdaUpdate()
                .in(BuyerShowComment::getBuyerShowId, buyerShowIds)
                .remove();
        buyerShowGalleryService.lambdaUpdate()
                .in(BuyerShowGallery::getBuyerShowId, buyerShowIds)
                .remove();
        buyerShowVideoService.lambdaUpdate()
                .in(BuyerShowVideo::getBuyerShowId, buyerShowIds)
                .remove();
        buyerShowLikeService.lambdaUpdate()
                .in(BuyerShowLike::getBuyerShowId, buyerShowIds)
                .remove();
        buyerShowFavoriteService.lambdaUpdate()
                .in(BuyerShowFavorite::getBuyerShowId, buyerShowIds)
                .remove();

        return this.removeByIds(buyerShowIds);
    }

    @Override
    public void audit(BuyerShowAuditParam param) {
        this.lambdaUpdate()
                .eq(BuyerShow::getBuyerShowId, param.getBuyerShowId())
                .set(BuyerShow::getStatus, param.getStatus())
                .set(BuyerShow::getIsShow, param.getStatus() == 1 ? Constants.YES : Constants.NO)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("audit".equals(batchDTO.getType())) {
            this.lambdaUpdate()
                    .in(BuyerShow::getBuyerShowId, batchDTO.getIds())
                    .set(BuyerShow::getStatus, batchDTO.getValue())
                    .set(BuyerShow::getIsShow, batchDTO.getValue() == 1 ? Constants.YES : Constants.NO)
                    .update();
            return true;
        }
        return false;
    }

    @Override
    public void showOrHide(BuyerShowShowOrHideParam param) {
        this.lambdaUpdate()
                .eq(BuyerShow::getBuyerShowId, param.getBuyerShowId())
                .set(BuyerShow::getIsShow, param.getIsShow())
                .update();
    }

    @Override
    public void comment(BuyerShowCommentParam param) {
        BuyerShowComment buyerShowComment = new BuyerShowComment();
        BeanUtils.copyProperties(param, buyerShowComment);
        buyerShowComment.setAddTime(StringUtils.getCurrentTime());
        buyerShowComment.setStatus(Constants.YES);
        buyerShowComment.setIsTop(Constants.NO);
        buyerShowComment.setIsShow(Constants.YES);
        //非真实用户评论userId为0
        buyerShowComment.setUserId(0);
        buyerShowCommentService.save(buyerShowComment);
    }

    @Override
    public Page<BuyerShowPageClientVO> clientList(BuyerShowPageClientQuery query) {
        Integer userId = getCurrentUserId();

        // 分页
        Page<BuyerShow> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<BuyerShow> queryWrapper = new LambdaQueryWrapper<>();

        // 排序固定与视频流同一口径，不接受 sortField/sortOrder 覆盖：宫格次序一旦与视频流不同，
        // 从宫格点入视频流时 startId 就会锚定到错误位置，导致首屏之后的数据被游标截断
        page.setOrders(buildClientFeedOrders());

        queryWrapper.and(wrapper -> wrapper.eq(BuyerShow::getUserId, userId)
                .or()
                .eq(BuyerShow::getIsShow, Constants.YES))
                .like(StrUtil.isNotBlank(query.getKeyword()), BuyerShow::getContent, query.getKeyword());
        if (query.getGoodsId() != null) {
            queryWrapper.apply("JSON_CONTAINS(group_data, {0})", query.getGoodsId().toString());
        }

        if (query.getProductId() != null) {
            queryWrapper.apply("JSON_CONTAINS(range_data, {0})", query.getProductId().toString());
        }

        // 执行查询
        Page<BuyerShow> buyerShowPage = this.page(page, queryWrapper);
        List<BuyerShow> buyerShowList = buyerShowPage.getRecords();
        if (CollUtil.isEmpty(buyerShowList)) {
            return new Page<>();
        }
        BuyerShowDTO buyerShowDTO = this.getBuyerShowDTO(buyerShowList);

        List<BuyerShowPageClientVO> buyerShowPageClientVOList = buyerShowList.stream()
                .map(buyerShow -> {
                    BuyerShowPageClientVO buyerShowPageClientVO = new BuyerShowPageClientVO();
                    BeanUtils.copyProperties(buyerShow, buyerShowPageClientVO);
                    if (buyerShow.getUserId() > 0) {
                        User user = buyerShowDTO.getUserMap().get(buyerShow.getUserId());
                        if (user != null) {
                            buyerShowPageClientVO.setBuyerName(user.getName());
                            buyerShowPageClientVO.setBuyerPhoto(user.getAvatar());
                        }
                    }
                    if (CollUtil.isNotEmpty(buyerShowDTO.getBuyerShowLikeList())) {
                        long isLike = buyerShowDTO.getBuyerShowLikeList().stream()
                                .filter(buyerShowLike -> buyerShowLike.getBuyerShowId().equals(buyerShow.getBuyerShowId())
                                        && buyerShowLike.getUserId().equals(userId))
                                .count();
                        buyerShowPageClientVO.setIsLike(isLike > 0 ? Constants.YES : Constants.NO);

                        long count = buyerShowDTO.getBuyerShowLikeList().stream()
                                .filter(buyerShowLike -> buyerShowLike.getBuyerShowId().equals(buyerShow.getBuyerShowId()))
                                .count();
                        buyerShowPageClientVO.setLikeCount(count);
                    }
                    List<BuyerShowGallery> imgList = buyerShowDTO.getBuyerShowGalleryMap().getOrDefault(buyerShow.getBuyerShowId(), new ArrayList<>());
                    List<BuyerShowVideo> videoList = buyerShowDTO.getBuyerShowVideoMap().getOrDefault(buyerShow.getBuyerShowId(), new ArrayList<>());
                    buyerShowPageClientVO.setImgList(imgList);
                    buyerShowPageClientVO.setVideoList(videoList);
                    buyerShowPageClientVO.setIsVideo(isVideoBuyerShow(imgList, videoList) ? Constants.YES : Constants.NO);
                    return buyerShowPageClientVO;
                }).toList();

        return PageUtil.convertPage(buyerShowPage, buyerShowPageClientVOList);
    }

    @Override
    public BuyerShowVideoFeedPageClientVO videoFeed(BuyerShowVideoFeedClientQuery query) {
        // 游客场景统一归一为 0，后续可见性判定与点赞/收藏态均以 >0 为登录标志
        Integer currentUserId = getCurrentUserId();
        Integer userId = currentUserId == null ? 0 : currentUserId;
        int size = query.getSize();

        // cursor 与 startId 均为买家秀 id：cursor 续页（严格取锚点之后）；startId 首屏（含锚点自身，保证点击项在首位）。
        // nextCursor 保持纯数字 id，前端原样回传，后端据此反查复合游标锚点(sort_order, add_time, buyer_show_id)。
        Integer cursorId = parseCursor(query.getCursor());
        boolean inclusive = cursorId == null && query.getStartId() != null;
        BuyerShow anchor = buildAnchor(cursorId != null ? cursorId : query.getStartId());

        // 额外取一条判断是否还有下一页，禁止环绕补齐导致前端重复消费已返回的数据
        VideoFeedPageWindow pageWindow = buildVideoFeedPageWindow(
                fetchVideoFeed(userId, query, anchor, inclusive, null, size + 1), size);
        List<BuyerShow> rows = pageWindow.rows();
        boolean hasMore = pageWindow.hasMore();

        BuyerShowVideoFeedPageClientVO result = new BuyerShowVideoFeedPageClientVO();
        if (CollUtil.isEmpty(rows)) {
            result.setList(List.of());
            result.setNextCursor("");
            result.setHasMore(false);
            return result;
        }

        List<Integer> buyerShowIds = rows.stream().map(BuyerShow::getBuyerShowId).toList();
        BuyerShowDTO buyerShowDTO = this.getBuyerShowDTO(rows);

        List<BuyerShowFavorite> favoriteList = buyerShowFavoriteService.lambdaQuery()
                .in(BuyerShowFavorite::getBuyerShowId, buyerShowIds)
                .list();

        Map<Integer, Long> commentCountMap = buyerShowCommentService.lambdaQuery()
                .in(BuyerShowComment::getBuyerShowId, buyerShowIds)
                .and(wrapper -> wrapper.eq(BuyerShowComment::getUserId, userId)
                        .or()
                        .eq(BuyerShowComment::getIsShow, Constants.YES))
                .list()
                .stream()
                .collect(Collectors.groupingBy(BuyerShowComment::getBuyerShowId, Collectors.counting()));

        Map<Integer, Product> productMap = this.getVideoFeedProductMap(rows);
        Set<Integer> followedAuthorIds = this.getFollowedAuthorIds(rows, userId);

        List<BuyerShowVideoFeedItemClientVO> list = rows.stream()
                .map(buyerShow -> this.toVideoFeedItem(buyerShow, userId, buyerShowDTO, favoriteList,
                        commentCountMap, productMap, followedAuthorIds))
                .toList();

        result.setList(list);
        result.setHasMore(hasMore);
        result.setNextCursor(hasMore ? String.valueOf(rows.get(rows.size() - 1).getBuyerShowId()) : "");
        return result;
    }

    /**
     * 将 size+1 条查询结果裁剪为当前页，并用额外记录判断下一页是否存在。
     * 包级可见，供分页回归测试复用。
     */
    static VideoFeedPageWindow buildVideoFeedPageWindow(List<BuyerShow> fetchedRows, int size) {
        if (fetchedRows.size() <= size) {
            return new VideoFeedPageWindow(List.copyOf(fetchedRows), false);
        }
        return new VideoFeedPageWindow(List.copyOf(fetchedRows.subList(0, size)), true);
    }

    record VideoFeedPageWindow(List<BuyerShow> rows, boolean hasMore) {
    }

    /**
     * C 端买家秀列表排序，必须与 BuyerShowMapper.xml selectVideoFeed 的 ORDER BY 保持一致，
     * 否则列表点入视频流时 startId 锚点会落在错误位置。
     * 包级可见，供排序一致性回归测试复用。
     */
    static List<OrderItem> buildClientFeedOrders() {
        return List.of(OrderItem.asc("sort_order"),
                OrderItem.desc("add_time"),
                OrderItem.desc("buyer_show_id"));
    }

    @Override
    public void like(BuyerShowLikeClientParam param) {
        if (Objects.equals(param.getIsLike(), Constants.YES)){
            BuyerShowLike buyerShowLike = new BuyerShowLike();
            buyerShowLike.setBuyerShowId(param.getBuyerShowId());
            buyerShowLike.setUserId(getCurrentUserId());
            buyerShowLike.setAddTime(StringUtils.getCurrentTime());
            buyerShowLikeService.save(buyerShowLike);
        }else{
            buyerShowLikeService.lambdaUpdate()
                    .eq(BuyerShowLike::getBuyerShowId, param.getBuyerShowId())
                    .eq(BuyerShowLike::getUserId, getCurrentUserId())
                    .remove();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(BuyerShowFavoriteClientParam param) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        BuyerShow buyerShow = this.getById(param.getBuyerShowId());
        if (buyerShow == null) {
            throw new GlobalException("买家秀不存在");
        }

        boolean exists = buyerShowFavoriteService.lambdaQuery()
                .eq(BuyerShowFavorite::getBuyerShowId, param.getBuyerShowId())
                .eq(BuyerShowFavorite::getUserId, userId)
                .count() > 0;

        if (Objects.equals(param.getIsFavorite(), Constants.YES)) {
            if (!exists) {
                BuyerShowFavorite favorite = new BuyerShowFavorite();
                favorite.setBuyerShowId(param.getBuyerShowId());
                favorite.setUserId(userId);
                favorite.setAddTime(StringUtils.getCurrentTime());
                buyerShowFavoriteService.save(favorite);
            }
            return;
        }

        if (exists) {
            buyerShowFavoriteService.lambdaUpdate()
                    .eq(BuyerShowFavorite::getBuyerShowId, param.getBuyerShowId())
                    .eq(BuyerShowFavorite::getUserId, userId)
                    .remove();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followAuthor(BuyerShowFollowAuthorClientParam param) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        if (userId.equals(param.getAuthorId())) {
            throw new GlobalException("不能关注自己");
        }

        boolean exists = userFollowService.lambdaQuery()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getAuthorId, param.getAuthorId())
                .count() > 0;

        if (Objects.equals(param.getIsFollow(), Constants.YES)) {
            if (!exists) {
                UserFollow userFollow = new UserFollow();
                userFollow.setUserId(userId);
                userFollow.setAuthorId(param.getAuthorId());
                userFollow.setAddTime(StringUtils.getCurrentTime());
                userFollowService.save(userFollow);
            }
            return;
        }

        if (exists) {
            userFollowService.lambdaUpdate()
                    .eq(UserFollow::getUserId, userId)
                    .eq(UserFollow::getAuthorId, param.getAuthorId())
                    .remove();
        }
    }

    @Override
    public void clientAdd(BuyerShowSaveClientParam param) {
        //1、校验
        if (CollUtil.isEmpty(param.getImgList()) && CollUtil.isEmpty(param.getBuyerShowVideoList())) {
            throw new GlobalException("买家秀图片和视频不能同时为空");
        }
        BuyerShow buyerShow = new BuyerShow();
        BeanUtils.copyProperties(param, buyerShow);
        Integer userId = getCurrentUserId();
        User user = userService.getById(userId);

        List<Integer> buyerShowGroupIds = buyerShowGroupService.getBuyerShowGroupByProductIds(param.getRangeData());
        buyerShow.setGroupData(CollUtil.isNotEmpty(buyerShowGroupIds) ? JSONUtil.toJsonStr(buyerShowGroupIds) : null);
        buyerShow.setBuyerName(user.getName());
        buyerShow.setBuyerPhoto(user.getAvatar());
        buyerShow.setRangeData(JSONUtil.toJsonStr(param.getRangeData()));
        buyerShow.setStatus(Constants.NO);
        buyerShow.setIsShow(Constants.NO);
        buyerShow.setUserId(userId);
        buyerShow.setAddTime(StringUtils.getCurrentTime());
        buyerShow.setSortOrder(0);
        this.save(buyerShow);

        //3、保存买家秀视频信息
        this.saveOrUpdateBuyerShowVideo(param.getBuyerShowVideoList(), buyerShow.getBuyerShowId());

        //4、保存买家秀图片信息
        this.saveOrUpdateBuyerShowGallery(param.getImgList(), buyerShow.getBuyerShowId());
    }

    @Override
    public BuyerShowClientVO clientDetail(Integer buyerShowId) {
        BuyerShowClientVO buyerShowClientVO = new BuyerShowClientVO();

        BuyerShow buyerShow = this.getById(buyerShowId);
        Assert.notNull(buyerShow, "买家秀不存在");
        BeanUtils.copyProperties(buyerShow, buyerShowClientVO);

        List<BuyerShowGallery> buyerShowGalleryList = buyerShowGalleryService.lambdaQuery()
                .eq(BuyerShowGallery::getBuyerShowId, buyerShowId)
                .list();
        buyerShowClientVO.setImgList(buyerShowGalleryList);

        List<BuyerShowVideo> buyerShowVideoList = buyerShowVideoService.lambdaQuery()
                .eq(BuyerShowVideo::getBuyerShowId, buyerShowId)
                .list();
        buyerShowClientVO.setVideoList(buyerShowVideoList);

        if (buyerShow.getUserId() > 0) {
            User user = userService.getById(buyerShow.getUserId());
            if (user != null) {
                buyerShowClientVO.setBuyerName(user.getName());
                buyerShowClientVO.setBuyerPhoto(user.getAvatar());
            }
        }
        buyerShowClientVO.setAddTime(TigUtils.handelTime(buyerShow.getAddTime()));

        Long commentCount = buyerShowCommentService.lambdaQuery()
                .eq(BuyerShowComment::getBuyerShowId, buyerShowId)
                .and(wrapper -> wrapper.eq(BuyerShowComment::getUserId, getCurrentUserId())
                        .or()
                        .eq(BuyerShowComment::getIsShow, Constants.YES))
                .count();
        buyerShowClientVO.setCommentCount(commentCount);

        Long likeCount = buyerShowLikeService.lambdaQuery()
                .eq(BuyerShowLike::getBuyerShowId, buyerShowId)
                .count();
        buyerShowClientVO.setLikeCount(likeCount);

        Long isLike = buyerShowLikeService.lambdaQuery()
                .eq(BuyerShowLike::getBuyerShowId, buyerShowId)
                .eq(BuyerShowLike::getUserId, getCurrentUserId())
                .count();
        buyerShowClientVO.setIsLike(isLike > 0 ? Constants.YES : Constants.NO);

        List<Product> productByIds = productService.getProductByIds(JSONUtil.toList(buyerShow.getRangeData(), Integer.class));
        if (CollUtil.isNotEmpty(productByIds)) {
            List<Integer> productIds = productByIds.stream().map(Product::getProductId).toList();
            Map<Integer, Integer> skuStockByProductId = productSkuService.getProductSkusByProductIds(productIds).stream()
                    .collect(Collectors.groupingBy(ProductSkuDTO::getProductId,
                            Collectors.summingInt(sku -> Optional.ofNullable(sku.getSkuStock()).orElse(0))));
            productByIds.forEach(product -> {
                Integer skuStock = skuStockByProductId.get(product.getProductId());
                if (skuStock != null) {
                    product.setProductStock(skuStock);
                }
                // 买家秀客户端沿用门店库存字段展示售罄状态；此处不查询门店，直接映射商品库存。
                product.setStoreProductStock(product.getProductStock());
            });
        }
        buyerShowClientVO.setProductList(productByIds);
        return buyerShowClientVO;
    }

    @Override
    public String getSharePageHtml(Integer buyerShowId) {
        String name = "买家秀";
        String desc = "";
        String pic = "";
        try {
            BuyerShow buyerShow = this.getById(buyerShowId);
            if (buyerShow != null) {
                desc = StrUtil.isNotBlank(buyerShow.getContent()) ? buyerShow.getContent() : "";
                if (buyerShow.getUserId() != null && buyerShow.getUserId() > 0) {
                    User user = userService.getById(buyerShow.getUserId());
                    if (user != null && StrUtil.isNotBlank(user.getName())) {
                        name = user.getName() + "的买家秀";
                    }
                }
                // 首图优先取图片，缺失则回退视频封面
                BuyerShowGallery gallery = buyerShowGalleryService.lambdaQuery()
                        .eq(BuyerShowGallery::getBuyerShowId, buyerShowId)
                        .orderByAsc(BuyerShowGallery::getSortOrder)
                        .last("limit 1")
                        .one();
                if (gallery != null && StrUtil.isNotBlank(gallery.getPicUrl())) {
                    pic = gallery.getPicUrl();
                }
                if (StrUtil.isBlank(pic)) {
                    BuyerShowVideo video = buyerShowVideoService.lambdaQuery()
                            .eq(BuyerShowVideo::getBuyerShowId, buyerShowId)
                            .last("limit 1")
                            .one();
                    if (video != null && StrUtil.isNotBlank(video.getVideoCover())) {
                        pic = video.getVideoCover();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[getSharePageHtml] 查询买家秀失败: id={}, {}", buyerShowId, e.getMessage());
        }

        String h5Domain = "";
        try {
            String configVal = configService.getConfigVal(SettingsEnum.H5_DOMAIN);
            if (StrUtil.isNotBlank(configVal)) {
                h5Domain = configVal;
            }
        } catch (Exception e) {
            log.warn("[getSharePageHtml] 获取H5域名失败: {}", e.getMessage());
        }

        String targetUrl = h5Domain + "/pages/buyerShow/buyerShowDetail?buyerShowId=" + buyerShowId;
        String ogImage = ShareHtmlUtil.toAbsoluteUrl(pic, h5Domain);
        return ShareHtmlUtil.buildOgHtml(name, desc, ogImage, targetUrl, "article");
    }

    @Override
    public void clientComment(BuyerShowCommentClientParam param) {
        BuyerShowComment buyerShowComment = new BuyerShowComment();
        BeanUtils.copyProperties(param, buyerShowComment);
        buyerShowComment.setStatus(Constants.NO);
        buyerShowComment.setIsTop(Constants.NO);
        buyerShowComment.setIsShow(Constants.NO);
        Integer userId = getCurrentUserId();
        buyerShowComment.setUserId(userId);
        User user = userService.getById(userId);
        if (user != null) {
            buyerShowComment.setUserName(user.getUsername());
            buyerShowComment.setUserPhoto(user.getAvatar());
        }
        buyerShowComment.setAddTime(StringUtils.getCurrentTime());
        buyerShowCommentService.save(buyerShowComment);
    }

    @Override
    public Page<BuyerShowCommentPageClientVO> clientCommentPageList(BuyerShowCommentPageClientQuery query) {
        Integer userId = getCurrentUserId();
        // 分页
        Page<BuyerShowComment> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<BuyerShowComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BuyerShowComment::getBuyerShowId, query.getBuyerShowId())
                .eq(BuyerShowComment::getParentId, query.getParentId())
                .and(wrapper -> wrapper.eq(BuyerShowComment::getUserId, userId)
                        .or()
                        .eq(BuyerShowComment::getIsShow, Constants.YES))
                .orderByDesc(BuyerShowComment::getIsTop, BuyerShowComment::getAddTime);

        Page<BuyerShowComment> buyerShowCommentPage = buyerShowCommentService.page(page, queryWrapper);
        List<BuyerShowComment> buyerShowCommentList = buyerShowCommentPage.getRecords();
        if (CollUtil.isEmpty(buyerShowCommentList)) {
            return new Page<>();
        }

        List<Integer> buyerShowCommentIds = buyerShowCommentList.stream().map(BuyerShowComment::getBuyerShowCommentId).toList();

        //当查回复评论时无需查回复数量
        Map<Integer, Long> buyerShowCommentReplyMap = query.getParentId() > 0 ? new HashMap<>() : buyerShowCommentService.lambdaQuery()
                .in(BuyerShowComment::getParentId, buyerShowCommentIds)
                .and(wrapper -> wrapper.eq(BuyerShowComment::getUserId, userId)
                        .or()
                        .eq(BuyerShowComment::getIsShow, Constants.YES))
                .list()
                .stream()
                .collect(Collectors.groupingBy(BuyerShowComment::getParentId, Collectors.counting()));

        //获取用户信息
        List<Integer> userIds = buyerShowCommentList.stream()
                .map(BuyerShowComment::getUserId)
                .filter(uId -> uId > 0)
                .toList();

        Map<Integer, User> userMap = CollUtil.isEmpty(userIds) ? new HashMap<>() : userService.lambdaQuery()
                .in(User::getUserId, userIds)
                .list()
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));


        //获取点赞数
        List<CommentLike> commentLikeList = commentLikeService.lambdaQuery()
                .in(CommentLike::getCommentId, buyerShowCommentIds)
                .list();

        BuyerShow buyerShow = this.getById(query.getBuyerShowId());

        List<BuyerShowCommentPageClientVO> buyerShowCommentPageClientVOList = buyerShowCommentList.stream()
                .map(buyerShowComment -> {
                    BuyerShowCommentPageClientVO buyerShowCommentPageClientVO = new BuyerShowCommentPageClientVO();
                    BeanUtils.copyProperties(buyerShowComment, buyerShowCommentPageClientVO);
                    buyerShowCommentPageClientVO.setImages(parseCommentImages(buyerShowComment.getCommentPic()));
                    buyerShowCommentPageClientVO.setAddTime(TigUtils.handelTime(buyerShowComment.getAddTime()));
                    buyerShowCommentPageClientVO.setReplyCount(buyerShowCommentReplyMap.getOrDefault(buyerShowComment.getBuyerShowCommentId(), 0L));
                    if (buyerShowComment.getUserId() > 0) {
                        User user = userMap.get(buyerShowComment.getUserId());
                        if (user != null) {
                            buyerShowCommentPageClientVO.setUserName(user.getName());
                            buyerShowCommentPageClientVO.setUserPhoto(user.getAvatar());
                        }
                    }
                    buyerShowCommentPageClientVO.setIsAuthor(buyerShowComment.getUserId() != 0 && buyerShowComment.getUserId().equals(buyerShow.getUserId()) ? Constants.YES : Constants.NO);
                    buyerShowCommentPageClientVO.setIsMe(buyerShowComment.getUserId() != 0 && buyerShowComment.getUserId().equals(userId) ? Constants.YES : Constants.NO);
                    if (CollUtil.isNotEmpty(commentLikeList)) {
                        long isLike = commentLikeList.stream()
                                .filter(commentLike -> commentLike.getCommentId().equals(buyerShowComment.getBuyerShowCommentId())
                                        && commentLike.getUserId().equals(userId))
                                .count();
                        buyerShowCommentPageClientVO.setIsLike(isLike > 0 ? Constants.YES : Constants.NO);

                        long count = commentLikeList.stream()
                                .filter(commentLike -> commentLike.getCommentId().equals(buyerShowComment.getBuyerShowCommentId()))
                                .count();
                        buyerShowCommentPageClientVO.setLikeCount(count);
                    }
                    return buyerShowCommentPageClientVO;
                }).toList();


        return PageUtil.convertPage(buyerShowCommentPage, buyerShowCommentPageClientVOList);
    }

    @Override
    public void commentLike(BuyerShowCommentLikeClientParam param) {
        if (Objects.equals(param.getIsLike(), Constants.YES)) {
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(param.getBuyerShowCommentId());
            commentLike.setUserId(getCurrentUserId());
            commentLike.setAddTime(StringUtils.getCurrentTime());
            commentLikeService.save(commentLike);
        } else {
            commentLikeService.lambdaUpdate()
                    .eq(CommentLike::getCommentId, param.getBuyerShowCommentId())
                    .eq(CommentLike::getUserId, getCurrentUserId())
                    .remove();
        }
    }

    /**
     * 视频流游标查询透传
     */
    private List<BuyerShow> fetchVideoFeed(Integer userId, BuyerShowVideoFeedClientQuery query, BuyerShow anchor,
                                           boolean inclusive, List<Integer> excludeIds, int limit) {
        return baseMapper.selectVideoFeed(userId, query.getKeyword(), query.getGroupId(),
                anchor == null ? null : anchor.getSortOrder(),
                anchor == null ? null : anchor.getAddTime(),
                anchor == null ? null : anchor.getBuyerShowId(),
                inclusive, excludeIds, limit);
    }

    /**
     * 根据买家秀 id 反查复合游标锚点；id 为空或记录不存在时返回 null（视为从头开始）
     */
    private BuyerShow buildAnchor(Integer anchorId) {
        if (anchorId == null) {
            return null;
        }
        BuyerShow row = this.getById(anchorId);
        if (row == null || row.getSortOrder() == null || row.getAddTime() == null) {
            return null;
        }
        return row;
    }

    /**
     * 解析前端回传的游标（纯数字买家秀 id），非法或空时返回 null（视为从头开始）
     */
    private Integer parseCursor(String cursor) {
        if (StrUtil.isBlank(cursor)) {
            return null;
        }
        try {
            return Integer.parseInt(cursor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 批量拉取视频流各买家秀关联商品，返回 productId -> Product。失败时降级为空 Map，不阻塞视频流。
     */
    private Map<Integer, Product> getVideoFeedProductMap(List<BuyerShow> rows) {
        Set<Integer> productIds = new HashSet<>();
        for (BuyerShow buyerShow : rows) {
            productIds.addAll(this.parseRangeData(buyerShow.getRangeData()));
        }
        if (productIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return productService.getProductByIds(new ArrayList<>(productIds)).stream()
                    .collect(Collectors.toMap(Product::getProductId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            log.error("视频流批量获取商品信息失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 批量判断当前用户对各作者的关注状态，返回已关注的作者 id 集合。未登录时返回空集合。
     */
    private Set<Integer> getFollowedAuthorIds(List<BuyerShow> rows, Integer userId) {
        if (userId == null || userId <= 0) {
            return new HashSet<>();
        }
        List<Integer> authorIds = rows.stream()
                .map(BuyerShow::getUserId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0 && !id.equals(userId))
                .distinct()
                .toList();
        if (CollUtil.isEmpty(authorIds)) {
            return new HashSet<>();
        }
        return userFollowService.lambdaQuery()
                .eq(UserFollow::getUserId, userId)
                .in(UserFollow::getAuthorId, authorIds)
                .list()
                .stream()
                .map(UserFollow::getAuthorId)
                .collect(Collectors.toSet());
    }

    private BuyerShowVideoFeedItemClientVO toVideoFeedItem(BuyerShow buyerShow,
                                                           Integer userId,
                                                           BuyerShowDTO buyerShowDTO,
                                                           List<BuyerShowFavorite> favoriteList,
                                                           Map<Integer, Long> commentCountMap,
                                                           Map<Integer, Product> productMap,
                                                           Set<Integer> followedAuthorIds) {
        Integer buyerShowId = buyerShow.getBuyerShowId();
        BuyerShowVideoFeedItemClientVO vo = new BuyerShowVideoFeedItemClientVO();
        vo.setId(buyerShowId);
        vo.setContent(buyerShow.getContent());
        vo.setCreateTime(TigUtils.handelTime(buyerShow.getAddTime(), FEED_TIME_FORMAT));
        vo.setCommentCount(commentCountMap.getOrDefault(buyerShowId, 0L));

        List<BuyerShowLike> buyerShowLikeList = buyerShowDTO.getBuyerShowLikeList();
        vo.setLikeCount(buyerShowLikeList.stream()
                .filter(like -> like.getBuyerShowId().equals(buyerShowId))
                .count());
        vo.setIsLike(userId > 0 && buyerShowLikeList.stream()
                .anyMatch(like -> like.getBuyerShowId().equals(buyerShowId) && like.getUserId().equals(userId))
                ? Constants.YES : Constants.NO);

        vo.setFavoriteCount(favoriteList.stream()
                .filter(favorite -> favorite.getBuyerShowId().equals(buyerShowId))
                .count());
        vo.setIsFavorite(userId > 0 && favoriteList.stream()
                .anyMatch(favorite -> favorite.getBuyerShowId().equals(buyerShowId) && favorite.getUserId().equals(userId))
                ? Constants.YES : Constants.NO);

        // 视频取排序第一个
        List<BuyerShowVideo> videoList = buyerShowDTO.getBuyerShowVideoMap().getOrDefault(buyerShowId, new ArrayList<>());
        videoList.stream()
                .min(Comparator.comparingInt(v -> normSortOrder(v.getSortOrder())))
                .ifPresent(first -> {
                    BuyerShowVideoFeedItemClientVO.Video video = new BuyerShowVideoFeedItemClientVO.Video();
                    video.setUrl(first.getVideoUrl());
                    video.setCover(first.getVideoCover());
                    // width/height/duration 暂无采集，返回 null
                    vo.setVideo(video);
                });

        // 该买家秀自带图片（按 sortOrder 稳定排序，顺序与详情页一致；width/height 暂无采集返回 null）
        vo.setImages(buyerShowDTO.getBuyerShowGalleryMap().getOrDefault(buyerShowId, new ArrayList<>()).stream()
                .sorted(Comparator.comparingInt(g -> normSortOrder(g.getSortOrder())))
                .map(gallery -> {
                    BuyerShowVideoFeedItemClientVO.Image image = new BuyerShowVideoFeedItemClientVO.Image();
                    image.setUrl(gallery.getPicUrl());
                    return image;
                })
                .toList());

        // 作者
        BuyerShowVideoFeedItemClientVO.Author author = new BuyerShowVideoFeedItemClientVO.Author();
        author.setId(buyerShow.getUserId());
        author.setName(buyerShow.getBuyerName());
        author.setAvatar(buyerShow.getBuyerPhoto());
        if (buyerShow.getUserId() != null && buyerShow.getUserId() > 0) {
            User user = buyerShowDTO.getUserMap().get(buyerShow.getUserId());
            if (user != null) {
                author.setName(user.getName());
                author.setAvatar(user.getAvatar());
            }
        }
        author.setIsFollow(followedAuthorIds.contains(buyerShow.getUserId()) ? Constants.YES : Constants.NO);
        vo.setAuthor(author);

        // 关联商品
        List<BuyerShowVideoFeedItemClientVO.Product> products = new ArrayList<>();
        for (Integer productId : this.parseRangeData(buyerShow.getRangeData())) {
            Product product = productMap.get(productId);
            if (product == null) {
                continue;
            }
            BuyerShowVideoFeedItemClientVO.Product item = new BuyerShowVideoFeedItemClientVO.Product();
            item.setProductId(product.getProductId());
            item.setName(product.getProductName());
            item.setPicUrl(product.getPicUrl());
            item.setStock(product.getProductStock());
            item.setPrice(this.formatPrice(product.getProductPrice()));
            products.add(item);
        }
        vo.setProducts(products);
        return vo;
    }

    /**
     * 解析买家秀关联商品（range_data 存 JSON 数组），空或非法时返回空列表
     */
    private List<Integer> parseRangeData(String rangeData) {
        if (StrUtil.isBlank(rangeData)) {
            return List.of();
        }
        try {
            return JSONUtil.toList(rangeData, Integer.class);
        } catch (Exception e) {
            log.warn("解析买家秀关联商品失败: rangeData={}, {}", rangeData, e.getMessage());
            return List.of();
        }
    }

    /**
     * 解析评论配图（comment_pic 存 JSON 数组），无则返回空列表；
     * 若非 JSON 数组但为非空字符串，则作为单图兜底。
     */
    private List<String> parseCommentImages(String commentPic) {
        if (StrUtil.isBlank(commentPic)) {
            return List.of();
        }
        String trimmed = commentPic.trim();
        if (trimmed.startsWith("[")) {
            try {
                return JSONUtil.toList(trimmed, String.class).stream()
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            } catch (Exception ignored) {
                // 非合法 JSON 数组，按单图兜底
            }
        }
        return List.of(commentPic);
    }

    /**
     * 价格格式化为两位小数字符串
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00";
        }
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * sort_order 归一化：null 视为默认值，与 videoFeed SQL 中 COALESCE 保持一致
     */
    private int normSortOrder(Integer sortOrder) {
        return sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
    }

    /**
     * 视频类判定（唯一口径）：图片与视频按 sort_order 混排后第一条是否为视频。
     * 规则：无视频→false；无图片→true；否则比较各自最小 sort_order，视频 &lt;= 图片时判为视频（平局视频优先）。
     * 与 videoFeed SQL 的过滤条件保持一致。
     */
    private boolean isVideoBuyerShow(List<BuyerShowGallery> imgList, List<BuyerShowVideo> videoList) {
        if (CollUtil.isEmpty(videoList)) {
            return false;
        }
        if (CollUtil.isEmpty(imgList)) {
            return true;
        }
        int videoFirstSort = videoList.stream().mapToInt(v -> normSortOrder(v.getSortOrder())).min().orElse(DEFAULT_SORT_ORDER);
        int imgFirstSort = imgList.stream().mapToInt(g -> normSortOrder(g.getSortOrder())).min().orElse(DEFAULT_SORT_ORDER);
        return videoFirstSort <= imgFirstSort;
    }

    protected BuyerShowDTO getBuyerShowDTO(List<BuyerShow> buyerShowList) {

        List<Integer> buyerShowIds = buyerShowList.stream().map(BuyerShow::getBuyerShowId).toList();

        List<Integer> userIds = buyerShowList.stream()
                .map(BuyerShow::getUserId)
                .filter(userId -> userId > 0)
                .toList();

        Map<Integer, User> userMap = CollUtil.isEmpty(userIds) ? new HashMap<>() : userService.lambdaQuery()
                .in(User::getUserId, userIds)
                .list()
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        //获取点赞数
        List<BuyerShowLike> buyerShowLikeList = buyerShowLikeService.lambdaQuery()
                .in(BuyerShowLike::getBuyerShowId, buyerShowIds)
                .list();

        //获取视频信息
        Map<Integer, List<BuyerShowVideo>> buyerShowVideoMap = buyerShowVideoService.lambdaQuery()
                .in(BuyerShowVideo::getBuyerShowId, buyerShowIds)
                // 数据库未指定排序时，同一 sort_order 的素材返回顺序不稳定，导致视频流首视频/图片数组漂移
                .orderByAsc(BuyerShowVideo::getSortOrder, BuyerShowVideo::getId)
                .list()
                .stream()
                .collect(Collectors.groupingBy(BuyerShowVideo::getBuyerShowId));
        //获取相册信息
        Map<Integer, List<BuyerShowGallery>> buyerShowGalleryMap = buyerShowGalleryService.lambdaQuery()
                .in(BuyerShowGallery::getBuyerShowId, buyerShowIds)
                // 与视频保持同一稳定排序口径，避免同一 sort_order 下图片数组顺序随机
                .orderByAsc(BuyerShowGallery::getSortOrder, BuyerShowGallery::getPicId)
                .list()
                .stream()
                .collect(Collectors.groupingBy(BuyerShowGallery::getBuyerShowId));


        return BuyerShowDTO.builder()
                .buyerShowLikeList(buyerShowLikeList)
                .buyerShowVideoMap(buyerShowVideoMap)
                .buyerShowGalleryMap(buyerShowGalleryMap)
                .userMap(userMap)
                .build();
    }


    /**
     * 保存或更新买家秀视频信息
     *
     * @param buyerShowVideoList 视频信息
     * @param buyerShowId        买家秀id
     */
    protected void saveOrUpdateBuyerShowVideo(List<BuyerShowVideo> buyerShowVideoList, Integer buyerShowId) {
        // 1. 删除视频
        buyerShowVideoService.lambdaUpdate().eq(BuyerShowVideo::getBuyerShowId, buyerShowId).remove();

        if (CollUtil.isEmpty(buyerShowVideoList)) {
            return;
        }

        // 2. 新增视频
        for (int i = 0; i < buyerShowVideoList.size(); i++) {
            BuyerShowVideo buyerShowVideo = buyerShowVideoList.get(i);
            buyerShowVideo.setBuyerShowId(buyerShowId);
            buyerShowVideo.setVideoId(buyerShowVideo.getVideoId() == null ? 0 : buyerShowVideo.getVideoId());
            // 前端未指定排序时按列表下标兜底，与图片的下标排序同一量纲，供视频类判定混排
            buyerShowVideo.setSortOrder(buyerShowVideo.getSortOrder() == null ? i : buyerShowVideo.getSortOrder());
            buyerShowVideo.setId(null);
            buyerShowVideoService.save(buyerShowVideo);
        }
    }

    /**
     * 保存或更新买家秀图片
     *
     * @param imgList     图片信息
     * @param buyerShowId 买家秀id
     */
    protected void saveOrUpdateBuyerShowGallery(List<GalleryPic> imgList, Integer buyerShowId) {
        // 1、删除图片
        buyerShowGalleryService.lambdaUpdate().eq(BuyerShowGallery::getBuyerShowId, buyerShowId).remove();

        if (CollUtil.isEmpty(imgList)) {
            return;
        }

        // 2、新增图片
        List<BuyerShowGallery> buyerShowGalleryList = IntStream.range(0, imgList.size())
                .mapToObj(i -> {
                    GalleryPic galleryPic = imgList.get(i);
                    BuyerShowGallery buyerShowGallery = new BuyerShowGallery();
                    buyerShowGallery.setBuyerShowId(buyerShowId);
                    String picUrl = galleryPic.getPicUrl();
                    buyerShowGallery.setPicOriginal(picUrl);
                    buyerShowGallery.setPicThumb(galleryPic.getPicThumb());
                    if (buyerShowId == null) {
                        buyerShowGallery.setPicUrl(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));
                        buyerShowGallery.setPicLarge(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_800")));
                    } else {
                        buyerShowGallery.setPicUrl(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));
                        buyerShowGallery.setPicLarge(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));
                    }
                    buyerShowGallery.setSortOrder(i);
                    return buyerShowGallery;
                }).toList();
        buyerShowGalleryService.saveBatch(buyerShowGalleryList);
    }
}
