package com.tigshop.service.decorate.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.tigshop.bean.bo.decorate.discrete.OpenAdvertisingBO;
import com.tigshop.bean.dto.decorate.DecorateDiscreteMenuItemDTO;
import com.tigshop.bean.enums.decorate.DecorateDiscreteTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.decorate.DecorateDiscrete;
import com.tigshop.bean.param.decorate.DecorateDiscreteUpdateParam;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.decorate.DecorateMemberDataVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.common.utils.TransactionAfterCommitExecutor;
import com.tigshop.mapper.decorate.DecorateDiscreteMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateDiscreteCache;
import com.tigshop.service.decorate.DecorateDiscreteService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 装修组件服务实现
 *
 * @author Kidd
 * @since 2025/6/30
 */
@Service
@RequiredArgsConstructor
public class DecorateDiscreteServiceImpl extends BaseServiceImpl<DecorateDiscreteMapper, DecorateDiscrete> implements DecorateDiscreteService {

    private final TigshopProperties tigshopProperties;
    private final ConfigService configService;
    //翻译数据
    private final TranslatePackage translatePackage;
    private final DecorateDiscreteCache decorateDiscreteCache;

    @Override
    public DecorateDiscreteVO detail(String decorateSn) {
        Integer shopId = HeaderUtils.getShopId() != null ? HeaderUtils.getShopId() : Integer.valueOf(0);
        String localeCode = ThreadLocalUtil.getValue();
        String cacheKey = decorateSn + ":" + shopId + ":" + (localeCode == null ? "" : localeCode);
        return decorateDiscreteCache.get(cacheKey, () -> loadDetail(decorateSn));
    }

    private DecorateDiscreteVO loadDetail(String decorateSn) {
        Integer shopId = HeaderUtils.getShopId() != null ? HeaderUtils.getShopId() : Integer.valueOf(0);
        DecorateDiscrete decorateDiscrete = this.lambdaQuery()
                .eq(DecorateDiscrete::getDecorateSn, decorateSn)
                .eq(DecorateDiscrete::getShopId, shopId)
                .one();

        if (DecorateDiscreteTypeEnum.OPEN_ADVERTISING.getCode().equals(decorateSn) && decorateDiscrete == null) {
            return null;
        }

        if (decorateDiscrete == null) {
            return new DecorateDiscreteVO();
        }
        if ("investmentPromotionHomepage".equals(decorateSn)) {
            //去除dataString
            String dataString = decorateDiscrete.getData();
            // 翻译招商页dataString中的picUrl, title, content字段（dataType=13）
            List<Map> dataList = JSONUtil.toList(dataString, Map.class);
            if (dataList != null && !dataList.isEmpty()) {
                for (Map<String, Object> map : dataList) {
                    // 翻译 title、content
                    Object title = map.get("title");
                    if (title != null) {
                        map.put("title", translatePackage.translate(title.toString(), 13));
                    }
                    Object content = map.get("content");
                    if (content != null) {
                        map.put("content", translatePackage.translate(content.toString(), 13));
                    }
                    // 处理主图 image
                    Object imageUrl = map.get("image");
                    if (imageUrl != null && imageUrl instanceof String && !((String) imageUrl).isEmpty()) {
                        map.put("image", translatePackage.translate((String) imageUrl, 13));
                    }
                    // 处理images array里的picUrl字段
                    Object imagesObj = map.get("images");
                    if (imagesObj instanceof List images) {
                        for (Object imageItem : images) {
                            if (imageItem instanceof Map imageMap) {
                                Object picUrl = imageMap.get("picUrl");
                                if (picUrl != null) {
                                    imageMap.put("picUrl", translatePackage.translate(picUrl.toString(), 13));
                                }
                            }
                        }
                    }
                }
                // 用翻译后的数据覆盖dataString
                dataString = JSONUtil.toJsonStr(dataList);
                decorateDiscrete.setData(dataString);
            }
        } else if ("investmentPlan".equals(decorateSn)) {
            // 解析dataString，翻译title和content字段（dataType=13）
            String dataString = decorateDiscrete.getData();
            // 解析为list
            List<Map> dataList = JSONUtil.toList(dataString, Map.class);
            if (dataList != null && !dataList.isEmpty()) {
                translateTitleContentBatch(dataList, 13);
                // 用翻译后的数据覆盖dataString
                dataString = JSONUtil.toJsonStr(dataList);
                decorateDiscrete.setData(dataString);
            }
        } else if ("investmentCriteria".equals(decorateSn)) {
            //翻译sn为investmentCriteria的
            // 解析dataString，翻译title和content字段（dataType=13）
            String dataString = decorateDiscrete.getData();
            // 解析为list
            List<Map> dataList = JSONUtil.toList(dataString, Map.class);
            if (dataList != null && !dataList.isEmpty()) {
                translateTitleContentBatch(dataList, 13);
                // 用翻译后的数据覆盖dataString
                dataString = JSONUtil.toJsonStr(dataList);
                decorateDiscrete.setData(dataString);
            }
        } else if ("settlingProcess".equals(decorateSn)) {
            //翻译sn为investmentCriteria的
            // 解析dataString，翻译title和content字段（dataType=13）
            String dataString = decorateDiscrete.getData();
            // 解析为list
            List<Map> dataList = JSONUtil.toList(dataString, Map.class);
            if (dataList != null && !dataList.isEmpty()) {
                translateTitleContentBatch(dataList, 13);
                // 用翻译后的数据覆盖dataString
                dataString = JSONUtil.toJsonStr(dataList);
                decorateDiscrete.setData(dataString);
            }
        } else if ("memberDecorate".equals(decorateSn)) {
            String dataString = decorateDiscrete.getData();
            List<Map> dataList = JSONUtil.toList(dataString, Map.class);
            if (dataList != null && !dataList.isEmpty()) {
                // 处理第一个元素的 bannerList
                Map firstItem = dataList.get(0);
                // 找到module.bannerList.picUrl并翻译
                Object moduleObj = firstItem.get("module");
                if (moduleObj instanceof Map moduleMap) {
                    Object bannerListObj = moduleMap.get("bannerList");
                    if (bannerListObj instanceof Map bannerListMap) {
                        Object picUrlObj = bannerListMap.get("picUrl");
                        if (picUrlObj != null) {
                            bannerListMap.put("picUrl", translatePackage.translate(picUrlObj.toString(), 14));
                        }
                    }
                }

                // 遍历所有元素，找到type为menus的项，删除picTitle为"资讯中心"和"站内消息"的菜单项
                for (Map<String, Object> item : dataList) {
                    if ("menus".equals(item.get("type"))) {
                        Object moduleObject = item.get("module");
                        if (moduleObject instanceof Map moduleMap) {
                            Object menuListObj = moduleMap.get("menuList");
                            if (menuListObj instanceof List menuList) {
                                // 过滤掉picTitle为"资讯中心"或"站内消息"的项
                                menuList.removeIf(menuItem -> {
                                    if (menuItem instanceof Map menuItemMap) {
                                        Object picTitle = menuItemMap.get("picTitle");
                                        return "资讯中心".equals(picTitle) || "站内消息".equals(picTitle);
                                    }
                                    return false;
                                });

                                // 检查是否存在picTitle为"消息中心"的菜单项，如果不存在则添加
                                boolean hasMessageCenter = menuList.stream().anyMatch(menuItem -> {
                                    if (menuItem instanceof Map menuItemMap) {
                                        Object picTitle = menuItemMap.get("picTitle");
                                        return "消息中心".equals(picTitle);
                                    }
                                    return false;
                                });

                                if (!hasMessageCenter) {
                                    // 添加消息中心配置
                                    Map<String, Object> messageCenterItem = new java.util.HashMap<>();
                                    messageCenterItem.put("type", "default");
                                    messageCenterItem.put("picTitle", "消息中心");
                                    messageCenterItem.put("picThumb", "https://oss.tigshop.com/static/user/xiaoxi.png");
                                    messageCenterItem.put("picUrl", "https://oss.tigshop.com/static/user/xiaoxi.png");

                                    Map<String, Object> picLink = new java.util.HashMap<>();
                                    picLink.put("path", "default");
                                    picLink.put("label", "消息中心");
                                    picLink.put("name", "消息中心");
                                    picLink.put("link", "/pages/im/page");

                                    messageCenterItem.put("picLink", picLink);
                                    menuList.add(messageCenterItem);
                                }
                            }
                        }
                    }
                }

                // 用翻译后的数据覆盖dataString
                dataString = JSONUtil.toJsonStr(dataList);
                decorateDiscrete.setData(dataString);
            }
        }
        return new DecorateDiscreteVO(decorateDiscrete);
    }

    /**
     * 批量翻译列表中各项的 title/content 字段（海外版一次性查回减少逐字段往返；非海外版恒等返回）。
     */
    private void translateTitleContentBatch(List<Map> dataList, int dataType) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        String localeCode = ThreadLocalUtil.getValue();
        Set<String> texts = new HashSet<>();
        for (Map<String, Object> map : dataList) {
            Object title = map.get("title");
            if (title != null) {
                texts.add(title.toString());
            }
            Object content = map.get("content");
            if (content != null) {
                texts.add(content.toString());
            }
        }
        Map<String, String> translated = translatePackage.translateBatch(texts, dataType, localeCode);
        for (Map<String, Object> map : dataList) {
            Object title = map.get("title");
            if (title != null) {
                map.put("title", translated.getOrDefault(title.toString(), title.toString()));
            }
            Object content = map.get("content");
            if (content != null) {
                map.put("content", translated.getOrDefault(content.toString(), content.toString()));
            }
        }
    }

    @Override
    public void update(DecorateDiscreteUpdateParam param) {
        Integer shopId = HeaderUtils.getShopId() != null ? HeaderUtils.getShopId() : Integer.valueOf(0);
        DecorateDiscrete decorateDiscrete = this.lambdaQuery()
                .eq(DecorateDiscrete::getDecorateSn, param.getDecorateSn())
                .eq(DecorateDiscrete::getShopId, shopId)
                .one();

        if (decorateDiscrete == null) {
            this.saveDecorateDiscrete(param);
        } else {
            this.editDecorateDiscrete(param, decorateDiscrete);
        }
        TransactionAfterCommitExecutor.execute(() -> decorateDiscreteCache.invalidateAndBroadcast());
    }

    private void editDecorateDiscrete(DecorateDiscreteUpdateParam param, DecorateDiscrete decorateDiscrete) {
        DecorateDiscrete editDecorateDiscrete = param.createEditDecorateDiscrete(decorateDiscrete);
        this.updateById(editDecorateDiscrete);
    }

    private void saveDecorateDiscrete(DecorateDiscreteUpdateParam param) {
        DecorateDiscrete decorateDiscrete = param.createSaveDecorateDiscrete();
        this.save(decorateDiscrete);
    }

    @Override
    public DecorateMemberDataVO<List<DecorateDiscreteMenuItemDTO>> getMemberDecorateData() {
        List<DecorateDiscreteMenuItemDTO> menus = new ArrayList<>();
        // 添加所有菜单项
        menus.add(new DecorateDiscreteMenuItemDTO("default", "账号管理",
                "https://oss.tigshop.com/static/user/zhanghaoguanli.png",
                "https://oss.tigshop.com/static/user/zhanghaoguanli.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "账号管理", "账号管理", "/pages/user/profile/index")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "收货地址",
                "https://oss.tigshop.com/static/user/shouhuodizhi.png",
                "https://oss.tigshop.com/static/user/shouhuodizhi.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "收货地址", "收货地址", "/pages/address/list")));

        if (tigshopProperties.getIsO2o() == 1 || tigshopProperties.getIsB2b() == 1 || tigshopProperties.getIsOverseas() == 1
                || tigshopProperties.getIsMerchant() == 1 || tigshopProperties.getIsVendor() == 1) {
            //单商户不能有这个
            menus.add(new DecorateDiscreteMenuItemDTO("default", "商家入驻",
                    "https://oss.tigshop.com/static/user/shangjiaruzhu.png",
                    "https://oss.tigshop.com/static/user/shangjiaruzhu.png",
                    new DecorateDiscreteMenuItemDTO.PicLink("default", "商家入驻", "商家入驻", "/pages/user/merchantEnter/principalType")));
        }


//        menus.add(new DecorateDiscreteMenuItemDTO("default", "站内消息",
//                "https://oss.tigshop.com/static/user/xiaoxi.png",
//                "https://oss.tigshop.com/static/user/xiaoxi.png",
//                new DecorateDiscreteMenuItemDTO.PicLink("default", "站内消息", "站内消息", "/pages/user/messageLog/index")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "消息中心",
                "https://oss.tigshop.com/static/user/xiaoxi.png",
                "https://oss.tigshop.com/static/user/xiaoxi.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "消息中心", "消息中心", "/pages/im/page")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "帮助中心",
                "https://oss.tigshop.com/static/user/issue.png",
                "https://oss.tigshop.com/static/user/issue.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "帮助中心", "帮助中心", "/pages/article/issue/list")));

//        menus.add(new DecorateDiscreteMenuItemDTO("default", "资讯中心",
//                "https://oss.tigshop.com/static/user/news.png",
//                "https://oss.tigshop.com/static/user/news.png",
//                new DecorateDiscreteMenuItemDTO.PicLink("default", "资讯中心", "资讯中心", "/pages/article/news/list?id=bzgg")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "分销员中心",
                "https://oss.tigshop.com/static/user/salesmanIco.png",
                "https://oss.tigshop.com/static/user/salesmanIco.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "分销员中心", "分销员中心", "/pages/salesman/index")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "积分商城",
                "https://oss.tigshop.com/static/user/jifen.png",
                "https://oss.tigshop.com/static/user/jifen.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "积分商城", "积分商城", "/pages/exchange/list")));

        String closeAuth = configService.getConfigVal(SettingsEnum.CLOSE_AUTH);
        if (tigshopProperties.getIsB2b() == 1 && "1".equals(closeAuth)) {
            menus.add(new DecorateDiscreteMenuItemDTO("default", "实名认证",
                    "https://oss.tigshop.com/static/user/realName.png",
                    "https://oss.tigshop.com/static/user/realName.png",
                    new DecorateDiscreteMenuItemDTO.PicLink("default", "实名认证", "实名认证", "/pages/user/userCertification/index")));
        }
        menus.add(new DecorateDiscreteMenuItemDTO("default", "发票管理",
                "https://oss.tigshop.com/static/user/fapiao.png",
                "https://oss.tigshop.com/static/user/fapiao.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "发票管理", "发票管理", "/pages/user/invoiceManagement/index")));



        menus.add(new DecorateDiscreteMenuItemDTO("default", "我的拼团",
                "https://oss.tigshop.com/static/user/fapiao.png",
                "https://oss.tigshop.com/static/user/fapiao.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "我的拼团", "我的拼团", "/pages/groupon/mylist")));


        menus.add(new DecorateDiscreteMenuItemDTO("default", "礼品卡兑换",
                    "https://oss.tigshop.com/img/gallery/202512/1764901595uU5xE2RU8l6KvoVc07.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                    "https://oss.tigshop.com/img/gallery/202512/1764901595uU5xE2RU8l6KvoVc07.jpeg",
                    new DecorateDiscreteMenuItemDTO.PicLink("default", "礼品卡兑换", "礼品卡兑换", "/pages/giftCard/exchange")));


        menus.add(new DecorateDiscreteMenuItemDTO("default", "抽奖大转盘",
                "https://oss.tigshop.com/img/gallery/202601/1767592704zTlxMzGIJZOuFnoFZ6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                "https://oss.tigshop.com/img/gallery/202601/1767592704zTlxMzGIJZOuFnoFZ6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "抽奖大转盘", "抽奖大转盘", "/pages/lottery/index")));


        return new DecorateMemberDataVO<>(menus);
    }

    @Override
    public DecorateDiscreteVO getShopHead(Integer shopId) {
        LambdaQueryWrapper<DecorateDiscrete> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DecorateDiscrete::getShopId, shopId);
        queryWrapper.eq(DecorateDiscrete::getDecorateSn, "shopHead");
        DecorateDiscrete decorateDiscrete = this.getOne(queryWrapper);
        if (decorateDiscrete == null) {
            return new DecorateDiscreteVO();
        }
        DecorateDiscreteVO decorateDiscreteVO = new DecorateDiscreteVO();
        BeanUtils.copyProperties(decorateDiscrete, decorateDiscreteVO);
        JsonNode data = JsonUtil.fromJson(decorateDiscrete.getData(), JsonNode.class);
        decorateDiscreteVO.setData(data);
        return decorateDiscreteVO;
    }

    @Override
    public OpenAdvertisingBO getOpenAdvertising() {
        DecorateDiscrete decorateDiscrete = this.lambdaQuery().eq(DecorateDiscrete::getDecorateSn, DecorateDiscreteTypeEnum.OPEN_ADVERTISING.getCode()).one();

        if (decorateDiscrete == null) {
            return new OpenAdvertisingBO();
        }

        return JSON.parseObject(decorateDiscrete.getData(), OpenAdvertisingBO.class);
    }
}