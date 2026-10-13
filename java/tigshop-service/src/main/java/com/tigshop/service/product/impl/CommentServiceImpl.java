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

package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.CommentReplyDTO;
import com.tigshop.bean.dto.product.CommentUpdateDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductRelated;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.product.comment.CommentEvaluateParam;
import com.tigshop.bean.param.product.comment.CommentSaveParam;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.vo.product.CommentSubNumVO;
import com.tigshop.bean.vo.product.CommentVO;
import com.tigshop.bean.vo.product.ProductCommentStatisticVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.AnonymousMaskUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.XssUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.CommentMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductRelatedMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.CommentService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.user.UserPointsLogService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUsername;

/**
 * 评价管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommentServiceImpl extends BaseServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ProductRelatedMapper productRelatedMapper;

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final ConfigService configService;

    private final OrderItemMapper orderItemMapper;

    private final UserRankService userRankService;

    private final TranslatePackageImpl translatePackage;
    private final MessageHelper messageHelper;

    private final ProductMapper productMapper;

    private final CommentMapper commentMapper;

    private final AdminLogService adminLogService;

    private final UserPointsLogService userPointsLogService;

    private final TigshopProperties tigshopProperties;

    private final ShopMapper shopMapper;

    private final GalleryPicService galleryPicService;

    @Override
    public Page<CommentVO> list(CommentListPageQuery pageQuery) {

        Page<Comment> page = buildSortOrder(pageQuery);

        // 1. 查询评论
        Page<Comment> commentPage = this.lambdaQuery()
                .eq(pageQuery.getIsShowed() != null && pageQuery.getIsShowed() != -1, Comment::getIsShowed, pageQuery.getIsShowed())
                .eq(pageQuery.getProductId() != null, Comment::getProductId, pageQuery.getProductId())
                .eq(pageQuery.getShopId() != null, Comment::getShopId, pageQuery.getShopId())
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Comment::getContent, pageQuery.getKeyword())
                .eq(pageQuery.getUserId() != null, Comment::getUserId, pageQuery.getUserId())
                .ge(pageQuery.getType() != null && pageQuery.getType() == 2, Comment::getCommentRank, 4)
                .eq(pageQuery.getType() != null && pageQuery.getType() == 3, Comment::getCommentRank, 3)
                .le(pageQuery.getType() != null && pageQuery.getType() == 4, Comment::getCommentRank, 2)
                .eq(getShopId() != null && getShopId() > 0, Comment::getShopId, getShopId())
                .eq(Comment::getParentId, 0)
                .orderByDesc(Comment::getAddTime)
                .page(page);

        List<Comment> commentPageRecords = commentPage.getRecords();
        if (CollUtil.isEmpty(commentPageRecords)) {
            return new Page<>();
        }

        // 2. 查询产品信息
        List<Integer> productIds = commentPageRecords.stream().map(Comment::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Integer, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // 查询子评论
        List<Integer> commentIds = commentPageRecords.stream().map(Comment::getCommentId).toList();
        Map<Integer, List<Comment>> replyMap;
        if (CollUtil.isNotEmpty(commentIds)) {
            List<Comment> replies = this.lambdaQuery().in(Comment::getParentId, commentIds).list();
            replyMap = replies.stream().collect(Collectors.groupingBy(Comment::getParentId));
        } else {
            replyMap = Collections.emptyMap();
        }

        // 批量预取订单项（避免循环内逐条 selectById 造成 N+1）
        Map<Integer, OrderItem> orderItemMap = batchGetOrderItemMap(commentPageRecords);

        // 转换为VO
        List<CommentVO> commentVOList = commentPageRecords.stream()
                .map(comment -> {
                    comment.setAvatar(getDefaultAvatar(comment.getAvatar()));
                    Product product = productMap.get(comment.getProductId());
                    List<Comment> replyComments = replyMap.getOrDefault(comment.getCommentId(), Collections.emptyList());
                    List<String> commentTag = JsonUtil.jsonToList(comment.getCommentTag(), String.class);
                    if (CollUtil.isNotEmpty(commentTag)) {
                        commentTag = commentTag.stream().map(translatePackage::translate).toList();
                    }
                    comment.setCommentTag(JSON.toJSONString(commentTag));
                    CommentVO commentVO = new CommentVO(comment, product, replyComments);
                    OrderItem orderItem = orderItemMap.get(comment.getOrderItemId());
                    if (orderItem != null) {
                        commentVO.setSkuId(orderItem.getSkuId());
                        if (JSONUtil.isTypeJSONArray(orderItem.getSkuData())) {
                            commentVO.setSkuData(JSONUtil.toList(orderItem.getSkuData(), ProductSkuDTO.SkuData.class));
                        }
                    }
                    return commentVO;
                }).toList();

        return PageUtil.convertPage(commentPage, commentVOList);
    }

    @Override
    public Page<CommentVO> listForClient(CommentListPageQuery pageQuery) {
        if (!isProductCommentEnabled()) {
            return new Page<>();
        }

        Page<CommentVO> commentPage = list(pageQuery);
        if (CollUtil.isNotEmpty(commentPage.getRecords())) {
            commentPage.getRecords().forEach(this::maskAnonymousUsername);
        }
        return commentPage;
    }

    @Override
    public boolean isProductCommentEnabled() {
        // 配置缺失时按开启处理，避免后端未初始化配置导致评价功能整体消失
        return !"0".equals(configService.getConfigVal(SettingsEnum.PRODUCT_COMMENT_ENABLED));
    }

    private boolean isAnonymousCommentEnabled() {
        return !"0".equals(configService.getConfigVal(SettingsEnum.ANONYMOUS_COMMENT_ENABLED));
    }

    /**
     * 匿名评价的用户名脱敏（D1146）。
     *
     * <p>只看每条记录自己的 isAnonymous，不看匿名开关当前值：匿名开关关闭后，
     * 历史匿名评价仍需保持脱敏，不得恢复真名。
     *
     * <p>商家回复（parentId > 0）的 isAnonymous 恒为 0，因此不会被误脱敏。
     */
    private void maskAnonymousUsername(CommentVO commentVO) {
        if (Constants.YES == ObjectUtil.defaultIfNull(commentVO.getIsAnonymous(), Constants.NO)) {
            commentVO.setUsername(AnonymousMaskUtils.maskName(commentVO.getUsername()));
        }
        if (CollUtil.isEmpty(commentVO.getReplies())) {
            return;
        }
        commentVO.getReplies().stream()
                .filter(reply -> Constants.YES == ObjectUtil.defaultIfNull(reply.getIsAnonymous(), Constants.NO))
                .forEach(reply -> reply.setUsername(AnonymousMaskUtils.maskName(reply.getUsername())));
    }

    @Override
    public Page<CommentVO> listByShopId(CommentListPageQuery pageQuery) {
        Page<Comment> page = buildSortOrder(pageQuery);

        // 1. 查询评论
        Page<Comment> commentPage = this.lambdaQuery()
                .eq(pageQuery.getIsShowed() != null && pageQuery.getIsShowed() != -1, Comment::getIsShowed, pageQuery.getIsShowed())
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Comment::getContent, pageQuery.getKeyword())
                .eq(pageQuery.getUserId() != null, Comment::getUserId, pageQuery.getUserId())
                .ge(pageQuery.getType() != null && pageQuery.getType() == 2, Comment::getCommentRank, 4)
                .eq(pageQuery.getType() != null && pageQuery.getType() == 3, Comment::getCommentRank, 3)
                .le(pageQuery.getType() != null && pageQuery.getType() == 4, Comment::getCommentRank, 2)
                .eq(Comment::getShopId, pageQuery.getShopId())
                .eq(Comment::getParentId, 0)
                .orderByDesc(Comment::getAddTime)
                .page(page);

        List<Comment> commentPageRecords = commentPage.getRecords();
        if (CollUtil.isEmpty(commentPageRecords)) {
            return new Page<>();
        }

        // 2. 查询产品信息
        List<Integer> productIds = commentPageRecords.stream().map(Comment::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Integer, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // 查询子评论
        List<Integer> commentIds = commentPageRecords.stream().map(Comment::getCommentId).toList();
        Map<Integer, List<Comment>> replyMap;
        if (CollUtil.isNotEmpty(commentIds)) {
            List<Comment> replies = this.lambdaQuery().in(Comment::getParentId, commentIds).list();
            replyMap = replies.stream().collect(Collectors.groupingBy(Comment::getParentId));
        } else {
            replyMap = Collections.emptyMap();
        }

        // 批量预取订单项（避免循环内逐条 selectById 造成 N+1）
        Map<Integer, OrderItem> orderItemMap = batchGetOrderItemMap(commentPageRecords);

        // 转换为VO
        List<CommentVO> commentVOList = commentPageRecords.stream()
                .map(comment -> {
                    comment.setAvatar(getDefaultAvatar(comment.getAvatar()));
                    Product product = productMap.get(comment.getProductId());
                    List<Comment> replyComments = replyMap.getOrDefault(comment.getCommentId(), Collections.emptyList());
                    CommentVO commentVO = new CommentVO(comment, product, replyComments);
                    OrderItem orderItem = orderItemMap.get(comment.getOrderItemId());
                    if (orderItem != null) {
                        commentVO.setSkuId(orderItem.getSkuId());
                        if (JSONUtil.isTypeJSONArray(orderItem.getSkuData())) {
                            commentVO.setSkuData(JSONUtil.toList(orderItem.getSkuData(), ProductSkuDTO.SkuData.class));
                        }
                    }
                    return commentVO;
                }).toList();

        return PageUtil.convertPage(commentPage, commentVOList);
    }

    @Override
    public CommentVO detail(Integer id) {
        Comment comment = this.getById(id);
        Assert.notNull(comment, () -> new GlobalException("评价不存在"));
        comment.setAvatar(getDefaultAvatar(comment.getAvatar()));

        List<Comment> replyComments = this.lambdaQuery()
                .eq(Comment::getParentId, comment.getCommentId())
                .list();

        return new CommentVO(comment, null, replyComments);
    }

    private List<CommentVO.ShowPic> getCommentShowPicsByJson(String showPics) {
        if (showPics != null && !StringUtils.isEmpty(showPics)
                && !"[]".equals(showPics)) {
            try {
                if (showPics.trim().startsWith("[")) {
                    return JSONUtil.toList(showPics, CommentVO.ShowPic.class);
                } else if (showPics.trim().startsWith("{")) {
                    CommentVO.ShowPic pic = JSONUtil.toBean(showPics, CommentVO.ShowPic.class);
                    return Collections.singletonList(pic);
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.error("解析 showPics JSON 出错: {}", e.getMessage(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void create(CommentSaveParam param) {
        Comment comment = param.createComment();

        // 查询商品所属店铺
        Product product = productMapper.selectById(comment.getProductId());
        comment.setShopId(product.getShopId());

        this.save(comment);
    }

    @Override
    public boolean update(CommentUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new GlobalException("评价参数错误");
        }
        Integer userId = updateDTO.getUserId();
        updateDTO.setUserId(userId == null ? 0 : userId);
        Integer shopId = updateDTO.getShopId();
        updateDTO.setShopId(shopId == null ? 0 : shopId);
        List<CommentUpdateDTO.ShowPic> showPics = updateDTO.getShowPics();
        if (showPics != null && !showPics.isEmpty()) {
            updateDTO.setIsShowed(1);
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(updateDTO, comment);

        if (updateDTO.getAddTime() != null) {
            //转成时间戳
            comment.setAddTime(DateUtil.parse(updateDTO.getAddTime()).getTime() / 1000);
        } else {
            comment.setAddTime(StringUtils.getCurrentTime());
        }
        if (updateDTO.getCommentTag() != null && !updateDTO.getCommentTag().isEmpty()) {
            comment.setCommentTag(JsonUtil.toJson(updateDTO.getCommentTag()));
        }
        if (updateDTO.getShowPics() != null && !updateDTO.getShowPics().isEmpty()) {
            comment.setShowPics(JsonUtil.toJson(updateDTO.getShowPics()));
        } else {
            comment.setShowPics("[]");
        }
        //新增日志
        adminLogService.createByLogInfo(StrUtil.format("更新评论晒单:{}", comment.getContent()));
        return this.updateById(comment);
    }

    @Override
    public ProductCommentStatisticVO getProductCommentDetail(Integer productId, Integer shopId) {
        // D1146：评价功能关闭时返回零值统计对象，不能返回 null（前端多处直接解构 total / goodPercent）
        if (!isProductCommentEnabled()) {
            return new ProductCommentStatisticVO(Collections.emptyList());
        }

        // 查看有没有被关联的商品，返回ids
        List<ProductRelated> productRelatedList = productRelatedMapper.selectList(
                Wrappers.lambdaQuery(ProductRelated.class).eq(ProductRelated::getProductId, productId)
        );

        // 取出related_product_id
        List<Integer> relatedProductIds = productRelatedList.stream().map(ProductRelated::getRelatedProductId).collect(Collectors.toList());
        relatedProductIds.addFirst(productId);

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .in(Comment::getProductId, relatedProductIds)
                        .eq(shopId != null, Comment::getShopId, shopId)
        );

        return new ProductCommentStatisticVO(comments);
    }

    @Override
    public CommentSubNumVO subNum(Integer userId) {
        CommentSubNumVO commentSubNumVO = new CommentSubNumVO();

        // D1146：评价功能关闭时全部返回 0，避免会员中心角标残留
        if (!isProductCommentEnabled()) {
            commentSubNumVO.setAwaitComment(0L);
            commentSubNumVO.setCommented(0L);
            commentSubNumVO.setShowPics(0L);
            return commentSubNumVO;
        }

        // 待评价订单
        LambdaQueryWrapper<Order> queryWrapperOrder = new LambdaQueryWrapper<>();
        queryWrapperOrder.eq(Order::getUserId, userId)
                .eq(Order::getIsDel, 0)
                .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                .eq(Order::getCommentStatus, OrderStatusEnum.PENDING.getCode());
        Long awaitCommentCount = orderMapper.selectCount(queryWrapperOrder);
        commentSubNumVO.setAwaitComment(awaitCommentCount);
        // 已评价
        LambdaQueryWrapper<Comment> queryWrapperComment = new LambdaQueryWrapper<>();
        queryWrapperComment.eq(Comment::getUserId, userId)
                .eq(Comment::getParentId, 0);
        Long commentedCount = this.count(queryWrapperComment);
        commentSubNumVO.setCommented(commentedCount);
        //待晒单
        LambdaQueryWrapper<Order> queryWrapperOrderComment = new LambdaQueryWrapper<>();
        queryWrapperOrderComment.eq(Order::getUserId, userId)
                .eq(Order::getIsDel, 0);
        List<Order> orderList = orderMapper.selectList(queryWrapperOrderComment);
        //从orderList取出order_id
        List<Integer> orderIdList = orderList.stream()
                .map(Order::getOrderId)
                .toList();
        if (!orderIdList.isEmpty()) {
            LambdaQueryWrapper<Comment> queryWrapperCommentOrder = new LambdaQueryWrapper<>();
            queryWrapperCommentOrder.in(Comment::getOrderId, orderIdList)
                    .eq(Comment::getUserId, userId)
                    .eq(Comment::getParentId, 0)
                    .eq(Comment::getIsShowed, 0);
            Long showPicsCount = this.count(queryWrapperCommentOrder);
            commentSubNumVO.setShowPics(showPicsCount);
        } else {
            commentSubNumVO.setShowPics(0L);
        }

        return commentSubNumVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(CommentEvaluateParam param) {
        // D1146：评价功能关闭时拒绝提交，防止绕过前端隐藏直接调接口
        Assert.isTrue(isProductCommentEnabled(), () -> new GlobalException("评价功能已关闭"));

        // D1146：匿名评价关闭时强制归零，不报错（前端此时不该出现勾选框）
        if (!isAnonymousCommentEnabled()) {
            param.setIsAnonymous(Constants.NO);
        }

        Integer userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        Assert.notNull(user, () -> new GlobalException(messageHelper.get("error.user.not.exist")));

        // XSS过滤评论内容
        if (param.getContent() != null) {
            param.setContent(XssUtils.clean(param.getContent()));
        }

        // 前端传过来的为空
        param.setUserId(userId);

        // 判断是否已评价
        Comment commentFind = this.lambdaQuery()
                .eq(Comment::getUserId, param.getUserId())
                .eq(Comment::getOrderItemId, param.getOrderItemId())
                .eq(Comment::getOrderId, param.getOrderId())
                .eq(Comment::getParentId, 0)
                .one();
        int isShowed;

        if (commentFind == null) {
            Comment comment = param.createComment(user);
            String commentCheck = configService.getConfigVal(SettingsEnum.COMMENT_CHECK);
            comment.setStatus("1".equals(commentCheck) ? Constants.YES : Constants.NO);
            this.save(comment);
            isShowed = comment.getIsShowed();
        } else {
            Assert.isFalse(commentFind.getIsShowed() > 0, () -> new GlobalException(messageHelper.get("error.comment.already.commented")));
            Assert.isFalse(commentFind.getIsShowed() <= 0 && ObjectUtil.isEmpty(param.getShowPics()), () -> new GlobalException(messageHelper.get("error.comment.show.pics.required")));
            commentFind.setShowPics(JSONUtil.toJsonStr(param.getShowPics()));
            commentFind.setIsShowed(Constants.YES);
            this.updateById(commentFind);
            isShowed = Constants.YES;
        }

        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);

        // 评论送积分
        String commentSendPointVal = configService.getConfigVal(SettingsEnum.COMMENT_SEND_POINT);
        int commentSendPoint = Integer.parseInt(commentSendPointVal);
        if (commentSendPoint > 0 && commentFind == null) {
            userPointsLogService.incPoints(commentSendPoint, userId, "评论送" + configVal, null);
        }

        // 晒单送积分
        String showSendPointVal = configService.getConfigVal(SettingsEnum.SHOW_SEND_POINT);
        int showSendPoint = Integer.parseInt(showSendPointVal);
        if (showSendPoint > 0 && Constants.YES == isShowed) {
            userPointsLogService.incPoints(showSendPoint, userId, "晒单送"  + configVal, null);
        }

        userRankService.updateUserRank(user);

        // 更新订单是否全部评论状态
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, param.getOrderId())
                        .eq(OrderItem::getUserId, param.getUserId())
        );

        List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();
        long commentCount = this.lambdaQuery()
                .eq(Comment::getOrderId, param.getOrderId())
                .eq(Comment::getParentId, 0)
                .eq(Comment::getUserId, param.getUserId())
                .in(Comment::getOrderItemId, orderItemIds)
                .count();
        if (commentCount == orderItemIds.size()) {
            // 该订单的商品已全部评价
            Order order = orderMapper.selectById(param.getOrderId());
            order.setCommentStatus(1);
            orderMapper.updateById(order);
        }

    }

    @Override
    public boolean replyComment(CommentReplyDTO dto, Integer adminId) {
        // XSS过滤回复内容
        if (dto.getContent() != null) {
            dto.setContent(XssUtils.clean(dto.getContent()));
        }

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, dto.getCommentId());
        Comment comment = this.getOne(queryWrapper);
        String username = getCurrentUsername();
        if (comment != null) {
            comment.setContent(dto.getContent());
            comment.setUsername(username);
            comment.setAddTime(StringUtils.getCurrentTime());
            comment.setUserId(adminId);
            return this.updateById(comment);
        } else {
            Comment commentReply = new Comment();
            commentReply.setContent(dto.getContent());
            commentReply.setUsername(username);
            commentReply.setAddTime(StringUtils.getCurrentTime());
            commentReply.setUserId(adminId);
            commentReply.setParentId(dto.getCommentId());
            boolean res = this.save(commentReply);
            if (res) {
                //修改评论状态
                LambdaQueryWrapper<Comment> queryWrapperComment = new LambdaQueryWrapper<>();
                queryWrapperComment.eq(Comment::getCommentId, dto.getCommentId());
                Comment commentUpdate = this.getOne(queryWrapperComment);
                commentUpdate.setStatus(1);
                return this.updateById(commentUpdate);
            }
            return false;
        }
    }

    private Map<String, Object> selectCommentInfo(List<Integer> relatedProductIdList) {
        QueryWrapper<Comment> queryWrapper = Wrappers.query(Comment.class)
                .in("product_id", relatedProductIdList)
                .groupBy("product_id")
                .select("COUNT(*) AS total, AVG(comment_rank) AS average_rank, (COUNT(CASE WHEN comment_rank >=4 THEN 1 END) / COUNT(*) * 100) AS good_percent");

        return this.getMap(queryWrapper);
    }

    /**
     * 按当前页评论批量预取订单项，key = OrderItem.itemId（即 selectById 使用的主键）。
     * 过滤 null/≤0 的 orderItemId，与循环内原 selectById 逐条查询语义等价。
     */
    private Map<Integer, OrderItem> batchGetOrderItemMap(List<Comment> comments) {
        List<Integer> orderItemIds = comments.stream()
                .map(Comment::getOrderItemId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(orderItemIds)) {
            return Collections.emptyMap();
        }
        List<OrderItem> orderItems = orderItemMapper.selectBatchIds(orderItemIds);
        return orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, Function.identity(), (a, b) -> a));
    }

    private String getDefaultAvatar(String avatar) {
        if (StrUtil.isEmpty(avatar)) {
            return configService.getConfigByCode(SettingsEnum.DEFAULT_AVATAR.getBizCode()).getBizVal();
        }
        return avatar;
    }

}
