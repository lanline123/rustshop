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

package com.tigshop.service.decorate.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.PcNavigationCreateDTO;
import com.tigshop.bean.dto.decorate.PcNavigationListDTO;
import com.tigshop.bean.dto.decorate.PcNavigationUpdateDTO;
import com.tigshop.bean.enums.decorate.PcNavigationType;
import com.tigshop.bean.model.decorate.PcNavigation;
import com.tigshop.bean.vo.common.NavVO;
import com.tigshop.bean.vo.decorate.PcNavigationVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.decorate.PcNavigationMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.PcNavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 首页分类栏服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class PcNavigationServiceImpl extends BaseServiceImpl<PcNavigationMapper, PcNavigation> implements PcNavigationService {

    private final TranslatePackage translatePackage;

    private final TigshopProperties tigshopProperties;

    @Override
    public Page<PcNavigationVO> list(PcNavigationListDTO listDTO) {
        // 分页
        Page<PcNavigation> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<PcNavigation> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer type = listDTO.getType();
        if (type != null && PcNavigationType.isValidCode(type)) {
            queryWrapper.eq(PcNavigation::getType, type);
            if (type == 2 && listDTO.getParentId() != null && listDTO.getParentId() >= 0) {
                queryWrapper.eq(PcNavigation::getParentId, listDTO.getParentId());
            }
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(PcNavigation::getTitle, keyword);
        }
        // 执行查询
        Page<PcNavigation> pcNavigationPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<PcNavigation> pcNavigationPageRecords = pcNavigationPage.getRecords();
        // 转换为VO
        List<PcNavigationVO> pcNavigationVOList = pcNavigationPageRecords.stream()
                .map(pcNavigation -> {
                    PcNavigationVO pcNavigationVO = new PcNavigationVO();
                    BeanUtils.copyProperties(pcNavigation, pcNavigationVO);
                    Integer id = pcNavigation.getId();
                    // 如果是顶部站点地图导航，则需要获取是否有子导航
                    pcNavigationVO.setHasChildren(0L);
                    if (type != null && type == 2) {
                        Long count = this.lambdaQuery().eq(PcNavigation::getParentId, id).count();
                        pcNavigationVO.setHasChildren(count);
                    }
                    // 链接需要转换为JSON对象
                    String link = pcNavigation.getLink();
                    if (StrUtil.isNotBlank(link) && !"[]".equals(link)) {
                        pcNavigationVO.setLink(JsonUtil.fromJson(link, JSONObject.class));
                    } else {
                        pcNavigationVO.setLink(new JSONObject());
                    }
                    PcNavigationType typeCode = PcNavigationType.getTypeCode(pcNavigation.getType());
                    String typeName = typeCode == null ? "" : typeCode.getName();
                    pcNavigationVO.setTypeName(typeName);
                    return pcNavigationVO;
                }).toList();
        return PageUtil.convertPage(pcNavigationPage, pcNavigationVOList);
    }

    @Override
    public PcNavigationVO detail(Integer id) {
        PcNavigation pcNavigation = this.getById(id);
        PcNavigationVO pcNavigationVO = new PcNavigationVO();
        BeanUtils.copyProperties(pcNavigation, pcNavigationVO);
        PcNavigationType typeCode = PcNavigationType.getTypeCode(pcNavigation.getType());
        String typeName = typeCode == null ? "" : typeCode.getName();
        pcNavigationVO.setTypeName(typeName);

        String link = pcNavigation.getLink();
        if (StrUtil.isNotBlank(link) && !"[]".equals(link)) {
            pcNavigationVO.setLink(JsonUtil.fromJson(link, JSONObject.class));
        } else {
            pcNavigationVO.setLink(new JSONObject());
        }
        return pcNavigationVO;
    }

    @Override
    public boolean create(PcNavigationCreateDTO createDTO) {
        if (createDTO != null) {
            PcNavigation pcNavigation = new PcNavigation();
            BeanUtils.copyProperties(createDTO, pcNavigation);
            this.getLink(createDTO.getType(), createDTO.getLink(), pcNavigation);
            return this.save(pcNavigation);
        }
        return false;
    }

    @Override
    public boolean update(PcNavigationUpdateDTO updateDTO) {
        if (updateDTO != null) {
            PcNavigation pcNavigation = new PcNavigation();
            BeanUtils.copyProperties(updateDTO, pcNavigation);
            this.getLink(updateDTO.getType(), updateDTO.getLink(), pcNavigation);
            return this.updateById(pcNavigation);
        }
        return false;
    }

    private void getLink(Integer type, Object link, PcNavigation pcNavigation) {
        if (link == null) {
            pcNavigation.setLink("[]");
        } else {
            JSONObject jsonObject = JSONUtil.parseObj(link);
            String path = jsonObject.getStr("path");
            if (PcNavigationType.TYPE_BOTTOM.getCode().equals(type)
                    && StrUtil.isNotBlank(path)
                    && !"article".equals(path)) {
                throw new GlobalException("底部导航只能指向文章");
            }
            pcNavigation.setLink(JSONUtil.toJsonStr(link));
        }
    }

    @Override
    public NavVO getAllNav(Integer type) {
        LambdaQueryWrapper<PcNavigation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PcNavigation::getIsShow, 1);
        if (type != null && type > 0) {
            queryWrapper.eq(PcNavigation::getType, type);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            // 42是商家入驻，O2O模式下也支持入驻功能
            queryWrapper.ne(PcNavigation::getId, 42);
        }
        queryWrapper.orderByAsc(PcNavigation::getSortOrder).orderByAsc(PcNavigation::getParentId);

        List<Tree<Integer>> trees = convertTreeList(this.list(queryWrapper));
        List<Tree<Integer>> mainNav = new ArrayList<>();
        List<Tree<Integer>> topBarNav = new ArrayList<>();
        List<Tree<Integer>> bottomNav = new ArrayList<>();
        List<Tree<Integer>> sidebarNav = new ArrayList<>();
        for (Tree<Integer> tree : trees) {
            int treeType = (int) tree.get("type");
            PcNavigationType pcNavigationType = PcNavigationType.getTypeCode(treeType);
            if (pcNavigationType != null) {
                switch (pcNavigationType) {
                    case TYPE_MAIN -> mainNav.add(tree);
                    case TYPE_TOP_BAR -> topBarNav.add(tree);
                    case TYPE_BOTTOM -> bottomNav.add(tree);
                    case TYPE_SIDEBAR -> sidebarNav.add(tree);
                }
            }
        }
        return NavVO.builder()
                .mainNav(mainNav)
                .topBarNav(topBarNav)
                .bottomNav(bottomNav)
                .sidebarNav(sidebarNav)
                .build();
    }

    @Override
    public List<Map<String, String>> selectLink() {
        List<Map<String, String>> data = new ArrayList<>();

        Map<String, String> item1 = new HashMap<>();
        item1.put("name", "商城首页");
        item1.put("app_link", "");
        item1.put("link", "/");
        data.add(item1);

        Map<String, String> item2 = new HashMap<>();
        item2.put("name", "分类页面（仅分类）");
        item2.put("app_link", "");
        item2.put("link", "list");
        data.add(item2);

        Map<String, String> item3 = new HashMap<>();
        item3.put("name", "限时秒杀");
        item3.put("app_link", "");
        item3.put("link", "seckill/list");
        data.add(item3);

        Map<String, String> item4 = new HashMap<>();
        item4.put("name", "购物车");
        item4.put("app_link", "");
        item4.put("link", "cart");
        data.add(item4);

        Map<String, String> item5 = new HashMap<>();
        item5.put("name", "搜索页面");
        item5.put("app_link", "");
        item5.put("link", "search");
        data.add(item5);

        Map<String, String> item6 = new HashMap<>();
        item6.put("name", "会员首页");
        item6.put("app_link", "");
        item6.put("link", "member");
        data.add(item6);

        Map<String, String> itemSavings = new HashMap<>();
        itemSavings.put("name", "储值中心");
        itemSavings.put("app_link", "");
        itemSavings.put("link", "user/account/index");
        data.add(itemSavings);


        Map<String, String> item7 = new HashMap<>();
        item7.put("name", "我的优惠券");
        item7.put("app_link", "");
        item7.put("link", "member/coupon/list");
        data.add(item7);

        Map<String, String> itemBrowsingHistory = new HashMap<>();
        itemBrowsingHistory.put("name", "我的足迹");
        itemBrowsingHistory.put("app_link", "");
        itemBrowsingHistory.put("link", "member/browsingHistory/list");
        data.add(itemBrowsingHistory);

        Map<String, String> item8 = new HashMap<>();
        item8.put("name", "我的订单");
        item8.put("app_link", "");
        item8.put("link", "member/order/list");
        data.add(item8);

        Map<String, String> item9 = new HashMap<>();
        item9.put("name", "待评价订单");
        item9.put("app_link", "");
        item9.put("link", "member/comment/list");
        data.add(item9);

        Map<String, String> item10 = new HashMap<>();
        item10.put("name", "收货地址");
        item10.put("app_link", "");
        item10.put("link", "member/address/list");
        data.add(item10);

        Map<String, String> item11 = new HashMap<>();
        item11.put("name", "退换货");
        item11.put("app_link", "");
        item11.put("link", "member/return/list");
        data.add(item11);

        Map<String, String> item12 = new HashMap<>();
        item12.put("name", "账户余额");
        item12.put("app_link", "");
        item12.put("link", "member/account/detail");
        data.add(item12);

        Map<String, String> item13 = new HashMap<>();
        item13.put("name", "我的积分");
        item13.put("app_link", "");
        item13.put("link", "member/point/list");
        data.add(item13);

        Map<String, String> item14 = new HashMap<>();
        item14.put("name", "收藏商品");
        item14.put("app_link", "");
        item14.put("link", "member/collectProduct/list");
        data.add(item14);

        Map<String, String> item15 = new HashMap<>();
        item15.put("name", "留言咨询");
        item15.put("app_link", "");
        item15.put("link", "member/feedback/list");
        data.add(item15);

        Map<String, String> item16 = new HashMap<>();
        item16.put("name", "站内消息");
        item16.put("app_link", "");
        item16.put("link", "member/userMessage/list");
        data.add(item16);

        Map<String, String> item17 = new HashMap<>();
        item17.put("name", "发票管理");
        item17.put("app_link", "");
        item17.put("link", "member/orderInvoice/list");
        data.add(item17);

        Map<String, String> item18 = new HashMap<>();
        item18.put("name", "账号信息");
        item18.put("app_link", "");
        item18.put("link", "member/profile/info");
        data.add(item18);

        Map<String, String> item19 = new HashMap<>();
        item19.put("name", "积分商城");
        item19.put("app_link", "");
        item19.put("link", "exchange/list");
        data.add(item19);

        if (tigshopProperties.getIsMerchant() == 1) {
            Map<String, String> item20 = new HashMap<>();
            item20.put("name", "店铺列表");
            item20.put("app_link", "");
            item20.put("link", "shop/list");
            data.add(item20);
        }

        // 拼团
        Map<String, String> itemGroup = new HashMap<>();
        itemGroup.put("name", "拼团");
        itemGroup.put("app_link", "");
        itemGroup.put("link", "joining/list");
        data.add(itemGroup);

        Map<String, String> item21 = new HashMap<>();
            item21.put("name", "买家秀");
            item21.put("app_link", "");
            item21.put("link", "buyerShow");
            data.add(item21);

        Map<String, String> item22 = new HashMap<>();
        item22.put("name", "礼品卡兑换");
        item22.put("app_link", "");
        item22.put("link", "giftCard/exchange");
        data.add(item22);

        Map<String, String> item23 = new HashMap<>();
            item23.put("name", "抽奖大转盘");
            item23.put("app_link", "");
            item23.put("link", "lottery/index");
            data.add(item23);



        return data;
    }

    @Override
    public List<Tree<Integer>> getParentNav(Integer type, Integer parentId) {
        LambdaQueryWrapper<PcNavigation> queryWrapper = new LambdaQueryWrapper<>();

        if (type > 0) {
            queryWrapper.eq(PcNavigation::getType, type);
        }
        queryWrapper.eq(PcNavigation::getIsShow, 1);

        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            queryWrapper.ne(PcNavigation::getId, 42);
        }
        queryWrapper.orderByAsc(PcNavigation::getParentId);
        queryWrapper.orderByAsc(PcNavigation::getSortOrder);
        List<PcNavigation> pcNavigationList = this.list(queryWrapper);
        List<Tree<Integer>> trees = new ArrayList<>();
        if (CollUtil.isNotEmpty(pcNavigationList)) {
            trees = this.convertTreeList(pcNavigationList);
            trees = this.flattenTree(trees);
        }

        return trees;
    }

    public List<Tree<Integer>> flattenTree(List<Tree<Integer>> navList) {
        List<Tree<Integer>> flattenedList = new ArrayList<>();
        if (CollUtil.isEmpty(navList)) {
            return flattenedList;
        }
        for (Tree<Integer> nav : navList) {
            List<Tree<Integer>> children = nav.getChildren();
            if (ObjectUtil.isNotEmpty(children)) {
                for (Tree<Integer> child : children) {
                    // 移除子节点的子节点
                    child.setChildren(null);
                }
            }
            flattenedList.add(nav);
        }
        return flattenedList;
    }

    /**
     * 转换为树形结构
     *
     * @param navigationList 导航列表
     * @return List<Tree < Integer>>
     */
    public List<Tree<Integer>> convertTreeList(List<PcNavigation> navigationList) {
        if (navigationList.isEmpty()) {
            return Collections.emptyList();
        }
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parentId");
        config.setNameKey("title");
        config.setWeightKey("sortOrder");
        config.setDeep(2);

        return TreeUtil.build(navigationList, 0, config, (object, treeNode) -> {
            treeNode.putExtra("id", object.getId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("title", translatePackage.translate(object.getTitle()));
            treeNode.putExtra("sortOrder", object.getSortOrder());
            treeNode.putExtra("type", object.getType());
            treeNode.putExtra("isShow", object.getIsShow());
            treeNode.putExtra("isBlank", object.getIsBlank());
            // 处理link字段，支持JSON对象和JSON数组
            Object linkObj = JsonUtil.fromJson(object.getLink(), Object.class);
            treeNode.putExtra("link", linkObj);
            treeNode.putExtra("icon", object.getIcon());
        });
    }
}
