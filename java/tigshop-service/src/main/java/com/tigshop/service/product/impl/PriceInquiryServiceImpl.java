package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.PriceInquiryDTO;
import com.tigshop.bean.dto.product.PriceInquiryReplyDTO;
import com.tigshop.bean.model.product.PriceInquiry;
import com.tigshop.bean.model.product.PriceInquiryReplyRecord;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.product.PriceInquiryDetailVO;
import com.tigshop.bean.vo.product.PriceInquiryReplyRecordVO;
import com.tigshop.bean.vo.product.PriceInquiryVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.PriceInquiryMapper;
import com.tigshop.mapper.product.PriceInquiryReplyRecordMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.PriceInquiryService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.service.user.UserMessageService;
import com.tigshop.service.user.UserService;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Tigshop
 */
@Slf4j
@Service
public class PriceInquiryServiceImpl extends BaseServiceImpl<PriceInquiryMapper, PriceInquiry>
        implements PriceInquiryService {

    @Resource
    private ProductService productService;

    @Resource
    private UserService userService;

    @Resource
    private PriceInquiryReplyRecordMapper priceInquiryReplyRecordMapper;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private GalleryPicService galleryPicService;

    @Override
    public Page<PriceInquiryVO> list(PriceInquiryDTO listDTO) {
        // 分页
        Page<PriceInquiry> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<PriceInquiry> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        if (StrUtil.isNotBlank(listDTO.getKeyword())) {
            String keyword = listDTO.getKeyword();
            List<Integer> matchedProductIds = productService.lambdaQuery()
                    .like(Product::getProductName, keyword)
                    .or().like(Product::getProductSn, keyword)
                    .or().like(Product::getProductTsn, keyword)
                    .select(Product::getProductId)
                    .list()
                    .stream()
                    .map(Product::getProductId)
                    .toList();
            if (CollUtil.isEmpty(matchedProductIds)) {
                return PageUtil.convertPage(page, List.of());
            }
            queryWrapper.in(PriceInquiry::getProductId, matchedProductIds);
        }

        queryWrapper.like(StrUtil.isNotBlank(listDTO.getMobile()), PriceInquiry::getMobile, listDTO.getMobile())
                .eq(listDTO.getStatus() != null, PriceInquiry::getStatus, listDTO.getStatus())
                .eq(listDTO.getUserId() != null, PriceInquiry::getUserId, listDTO.getUserId())
                .eq(listDTO.getUserIsDel() != null, PriceInquiry::getUserIsDel, listDTO.getUserIsDel())
                .eq(listDTO.getShopId() != null, PriceInquiry::getShopId, listDTO.getShopId());

        Page<PriceInquiry> pageInfo = this.page(page, queryWrapper);

        List<PriceInquiry> records = pageInfo.getRecords();

        if (CollUtil.isEmpty(records)){
            return PageUtil.convertPage(pageInfo, List.of());
        }

        List<Integer> productId = records.stream().map(PriceInquiry::getProductId).toList();

        Map<Integer, Product> productMap = productService.lambdaQuery()
                .in(Product::getProductId, productId)
                .list()
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<PriceInquiryVO> priceInquiryVOList = records.stream().map(record -> {
            PriceInquiryVO priceInquiryVO = BeanUtil.copyProperties(record, PriceInquiryVO.class);
            priceInquiryVO.setCreateTime(TigUtils.handelTime(record.getCreateTime()));
            Product product = productMap.get(record.getProductId());
            if (product != null){
                PriceInquiryVO.ProductVO productVO = new PriceInquiryVO.ProductVO();
                productVO.setPicThumb(product.getPicUrl());
                productVO.setProductName(product.getProductName());
                productVO.setProductId(product.getProductId());
                productVO.setProductSn(product.getProductSn());
                priceInquiryVO.setProduct(productVO);
            }
            return priceInquiryVO;
        }).toList();

        return PageUtil.convertPage(pageInfo, priceInquiryVOList);
    }

    @Override
    public PriceInquiryVO detail(Integer id) {
        PriceInquiry priceInquiry = this.getById(id);
        if (priceInquiry == null) {
            throw new GlobalException("询价信息不存在");
        }
        Product product = productService.getById(priceInquiry.getProductId());
        PriceInquiryVO detailVO = buildPriceInquiryVO(priceInquiry, product);
        detailVO.setReplyRecords(buildReplyRecords(priceInquiry));
        return detailVO;
    }

    @Override
    public PriceInquiryDetailVO detail(Integer id, Integer userId) {
        PriceInquiry priceInquiry = this.lambdaQuery()
                .eq(PriceInquiry::getId, id)
                .eq(userId != null && userId > 0, PriceInquiry::getUserId, userId)
                .eq(userId != null && userId > 0, PriceInquiry::getUserIsDel, 0)
                .one();
        if (priceInquiry == null) {
            throw new GlobalException("询价信息不存在");
        }

        PriceInquiryDetailVO detailVO = BeanUtil.copyProperties(priceInquiry, PriceInquiryDetailVO.class);
        detailVO.setCreateTime(TigUtils.handelTime(priceInquiry.getCreateTime()));

        Product product = productService.getById(priceInquiry.getProductId());
        if (product != null) {
            PriceInquiryDetailVO.ProductVO productVO = new PriceInquiryDetailVO.ProductVO();
            productVO.setPicThumb(product.getPicUrl());
            productVO.setProductName(product.getProductName());
            productVO.setProductId(product.getProductId());
            productVO.setProductSn(product.getProductSn());
            productVO.setProductPrice(product.getProductPrice());
            productVO.setMarketPrice(product.getMarketPrice());
            detailVO.setProduct(productVO);
        }

        User user = priceInquiry.getUserId() != null && priceInquiry.getUserId() > 0
                ? userService.getById(priceInquiry.getUserId())
                : null;
        if (user != null) {
            detailVO.setContactName(user.getName());
            if (StrUtil.isBlank(detailVO.getMobile())) {
                detailVO.setMobile(user.getMobile());
            }
        } else {
            detailVO.setContactName("");
        }
        detailVO.setReplyRecords(buildReplyRecords(priceInquiry));

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyByAdmin(PriceInquiryReplyDTO dto) {
        PriceInquiry priceInquiry = this.getById(dto.getId());
        if (priceInquiry == null) {
            throw new GlobalException("询价信息不存在");
        }

        this.lambdaUpdate()
                .eq(PriceInquiry::getId, dto.getId())
                .set(PriceInquiry::getStatus, 1)
                .set(PriceInquiry::getRemark, dto.getRemark())
                .update();

        saveReplyRecord(
                priceInquiry.getId(),
                1,
                StrUtil.blankToDefault(SecurityUtils.getCurrentUsername(), "商家"),
                dto.getRemark(),
                SecurityUtils.getCurrentAdminId(),
                0
        );

        sendInquiryReplyMessage(priceInquiry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyByUser(PriceInquiryReplyDTO dto, Integer userId) {
        PriceInquiry priceInquiry = this.lambdaQuery()
                .eq(PriceInquiry::getId, dto.getId())
                .eq(PriceInquiry::getUserId, userId)
                .eq(PriceInquiry::getUserIsDel, 0)
                .one();
        if (priceInquiry == null) {
            throw new GlobalException("询价信息不存在");
        }

        User user = userService.getById(userId);
        String replyName = user != null && StrUtil.isNotBlank(user.getName())
                ? user.getName()
                : "用户";
        this.lambdaUpdate()
                .eq(PriceInquiry::getId, priceInquiry.getId())
                .set(PriceInquiry::getStatus, 0)
                .update();
        saveReplyRecord(priceInquiry.getId(), 2, replyName, dto.getRemark(), 0, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUser(Integer id, Integer userId) {
        boolean updated = this.lambdaUpdate()
                .eq(PriceInquiry::getId, id)
                .eq(PriceInquiry::getUserId, userId)
                .eq(PriceInquiry::getUserIsDel, 0)
                .set(PriceInquiry::getUserIsDel, 1)
                .update();
        if (!updated) {
            throw new GlobalException("询价信息不存在");
        }
    }

    private PriceInquiryVO buildPriceInquiryVO(PriceInquiry record, Product product) {
        PriceInquiryVO priceInquiryVO = BeanUtil.copyProperties(record, PriceInquiryVO.class);
        priceInquiryVO.setCreateTime(TigUtils.handelTime(record.getCreateTime()));
        if (product != null) {
            PriceInquiryVO.ProductVO productVO = new PriceInquiryVO.ProductVO();
            productVO.setPicThumb(product.getPicUrl());
            productVO.setProductName(product.getProductName());
            productVO.setProductId(product.getProductId());
            productVO.setProductSn(product.getProductSn());
            priceInquiryVO.setProduct(productVO);
        }
        return priceInquiryVO;
    }

    private List<PriceInquiryReplyRecordVO> buildReplyRecords(PriceInquiry priceInquiry) {
        List<PriceInquiryReplyRecord> records = priceInquiryReplyRecordMapper.selectList(
                new LambdaQueryWrapper<PriceInquiryReplyRecord>()
                        .eq(PriceInquiryReplyRecord::getInquiryId, priceInquiry.getId())
                        .orderByAsc(PriceInquiryReplyRecord::getCreateTime, PriceInquiryReplyRecord::getId)
        );
        if (CollUtil.isEmpty(records)) {
            if (StrUtil.isBlank(priceInquiry.getRemark())) {
                return Collections.emptyList();
            }
            PriceInquiryReplyRecordVO fallback = new PriceInquiryReplyRecordVO();
            fallback.setReplyName("商家");
            fallback.setCreateTime("");
            fallback.setContent(priceInquiry.getRemark());
            fallback.setReplyType(1);
            return List.of(fallback);
        }
        return records.stream().map(record -> {
            PriceInquiryReplyRecordVO vo = new PriceInquiryReplyRecordVO();
            vo.setReplyName(record.getReplyName());
            vo.setCreateTime(TigUtils.handelTime(record.getCreateTime()));
            vo.setContent(record.getContent());
            vo.setReplyType(record.getReplyType());
            return vo;
        }).toList();
    }

    private void saveReplyRecord(Integer inquiryId, Integer replyType, String replyName, String content, Integer adminId, Integer userId) {
        PriceInquiryReplyRecord record = new PriceInquiryReplyRecord();
        record.setInquiryId(inquiryId);
        record.setReplyType(replyType);
        record.setReplyName(replyName);
        record.setContent(content);
        record.setAdminId(adminId);
        record.setUserId(userId);
        record.setCreateTime(System.currentTimeMillis() / 1000);
        priceInquiryReplyRecordMapper.insert(record);
    }

    private void sendInquiryReplyMessage(PriceInquiry priceInquiry) {
        if (priceInquiry.getUserId() == null || priceInquiry.getUserId() <= 0) {
            return;
        }
        try {
            Product product = productService.getById(priceInquiry.getProductId());
            String productName = product != null ? product.getProductName() : "商品";

            String link = JSONUtil.toJsonStr(Map.of(
                    "path", "inquiry",
                    "id", priceInquiry.getId()
            ));

            UserMessage userMessage = UserMessage.builder()
                    .userId(priceInquiry.getUserId())
                    .title("询价回复通知")
                    .content("您咨询的商品【" + productName + "】已收到商家回复，请查看。")
                    .link(link)
                    .isRead(0)
                    .addTime(System.currentTimeMillis() / 1000)
                    .type(0L)
                    .build();
            userMessageService.save(userMessage);
        } catch (Exception e) {
            log.error("发送询价回复站内信失败, inquiryId={}", priceInquiry.getId(), e);
        }
    }
}

