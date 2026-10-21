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
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.DecorateShareInfoDTO;
import com.tigshop.bean.vo.decorate.DecorateShareInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.tigshop.bean.dto.decorate.*;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.model.decorate.MobileCatNav;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.dto.setting.GalleryPicDTO;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.bean.vo.decorate.MobileCatNavVO;
import com.tigshop.common.constant.decorate.InitDecorateDataConstants;
import com.tigshop.common.constant.decorate.ProductPageDecorateDefaults;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.utils.TransactionAfterCommitExecutor;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.decorate.DecorateMapper;
import com.tigshop.mapper.lang.LocalesMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateDiscreteService;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.decorate.HomeDecorateCache;
import com.tigshop.service.decorate.MobileCatNavService;
import com.tigshop.service.product.BrandService;
import com.tigshop.service.product.CategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.constant.decorate.DecorateConstants.TEMPLATE_NOT_EXIST;

/**
 * 页面管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@Slf4j
public class DecorateServiceImpl extends BaseServiceImpl<DecorateMapper, Decorate> implements DecorateService {
    private static final Object PRODUCT_PAGE_DEFAULT_INIT_LOCK = new Object();

    @Resource
    private MessageHelper messageHelper;
    @Resource
    private MobileCatNavService mobileCatNavService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private BrandService brandService;

    @Resource
    private LocalesMapper localesMapper;
    @Lazy
    @Resource
    private DecorateDiscreteService decorateDiscreteService;
    @Autowired
    private TigshopProperties tigshopProperties;
    @Resource
    private HomeDecorateCache homeDecorateCache;

    @Override
    public Page<DecorateVO> list(DecorateListDTO listDTO) {
        // 分页
        Page<Decorate> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Decorate::getShopId, HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
        if (listDTO.getParentId() != null && listDTO.getParentId() > -1) {
            queryWrapper.eq(Decorate::getParentId, listDTO.getParentId());
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        // is_home=1 的记录始终置顶（作为首要排序键，原有排序作为次要）
        // 注：MyBatis-Plus 分页插件会把 page.orders 前置于 wrapper 的 orderBy，
        // 所以必须把 is_home DESC 放进 page.orders 才能生效
        List<OrderItem> orders = new ArrayList<>();
        orders.add(OrderItem.desc("is_home"));
        if (page.orders() != null) {
            orders.addAll(page.orders());
        }
        page.setOrders(orders);

        // 页面端类型：支持多值，过滤掉非法编码后用 IN 查询
        List<Integer> decorateTypes = listDTO.getDecorateType();
        List<Integer> validDecorateTypes = CollUtil.isEmpty(decorateTypes) ? Collections.emptyList()
                : decorateTypes.stream()
                .filter(Objects::nonNull)
                .filter(DecorateType::isValidCode)
                .distinct()
                .toList();
        if (!validDecorateTypes.isEmpty()) {
            queryWrapper.in(Decorate::getDecorateType, validDecorateTypes);
        }

        // 页面用途类型：支持多值，用 IN 查询
        List<Integer> pageTypes = CollUtil.isEmpty(listDTO.getPageType()) ? Collections.emptyList()
                : listDTO.getPageType().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!pageTypes.isEmpty()) {
            queryWrapper.in(Decorate::getPageType, pageTypes);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Decorate::getDecorateTitle, keyword);
        }
        if (listDTO.getStatus() != null) {
            queryWrapper.eq(Decorate::getStatus, listDTO.getStatus());
        }
        // 执行查询
        Page<Decorate> decoratePage = this.page(page, queryWrapper);

        // 自动初始化默认页面仅在单一 decorateType 且单一 pageType 时触发（多值场景无法生成唯一默认页，跳过）
        Integer singleDecorateType = validDecorateTypes.size() == 1 ? validDecorateTypes.get(0) : null;
        Integer singlePageType = pageTypes.size() == 1 ? pageTypes.get(0) : null;
        // 查询为空时按 decorateType + pageType 自动初始化默认页面
        if (decoratePage.getRecords().isEmpty()
                && singleDecorateType != null
                && singlePageType != null) {
            String defaultTitle = null;
            String defaultData = null;
            if (Integer.valueOf(1).equals(singleDecorateType)) {
                // H5 默认页
                if (Integer.valueOf(2).equals(singlePageType)) {
                    defaultTitle = "个人中心页";
                    defaultData = getH5DefaultDecorateData(singleDecorateType, singlePageType);
                } else if (Integer.valueOf(3).equals(singlePageType)) {
                    defaultTitle = "自定义活动页";
                    defaultData = InitDecorateDataConstants.CUSTOM_ACTIVITY_H5_DECORATE_DATA;
                } else if (Integer.valueOf(4).equals(singlePageType)) {
                    defaultTitle = "分类页";
                    defaultData = InitDecorateDataConstants.CATEGORY_HOME_H5_DECORATE_DATA;
                } else if (Integer.valueOf(6).equals(singlePageType)) {
                    defaultTitle = "移动端搜索页";
                    defaultData = getH5DefaultDecorateData(singleDecorateType, singlePageType);
                } else if (Integer.valueOf(7).equals(singlePageType)) {
                    defaultTitle = "主导航栏";
                    defaultData = buildNavH5DefaultData();
                } else if (Integer.valueOf(8).equals(singlePageType)) {
                    defaultTitle = "商品详情页";
                    defaultData = getH5DefaultDecorateData(singleDecorateType, singlePageType);
                } else if (Integer.valueOf(5).equals(singlePageType)) {
                    // 移动端登录页（对齐 PC 登录页：进列表即自动生成一条可编辑记录）
                    defaultTitle = "登录页";
                    defaultData = InitDecorateDataConstants.LOGIN_H5_DECORATE_DATA;
                }
            } else if (Integer.valueOf(2).equals(singleDecorateType)
                    && Integer.valueOf(6).equals(singlePageType)) {
                // PC 搜索页
                defaultTitle = "搜索页";
                defaultData = InitDecorateDataConstants.SEARCH_PC_DECORATE_DATA;
            }
            if (defaultTitle != null) {
                Decorate defaultDecorate = new Decorate();
                defaultDecorate.setDecorateTitle(defaultTitle);
                defaultDecorate.setData(defaultData);
                if (Integer.valueOf(6).equals(singlePageType) || Integer.valueOf(8).equals(singlePageType)) {
                    // 搜索页和商品详情页由代码常量实例化；草稿同步写入，确保首次打开即可编辑和预览。
                    defaultDecorate.setDraftData(defaultData);
                }
                defaultDecorate.setDecorateType(singleDecorateType);
                defaultDecorate.setPageType(singlePageType);
                defaultDecorate.setIsHome(1);
                defaultDecorate.setShopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
                defaultDecorate.setStatus(1);
                defaultDecorate.setSortOrder(0);
                defaultDecorate.setBindedH5Id(0);
                defaultDecorate.setParentId(0);
                defaultDecorate.setLocaleId(0);
                defaultDecorate.setUpdateTime(StringUtils.getCurrentTime());
                boolean codeManagedDefault = Integer.valueOf(1).equals(singleDecorateType)
                        && (Integer.valueOf(6).equals(singlePageType) || Integer.valueOf(8).equals(singlePageType));
                if (codeManagedDefault) {
                    // 列表筛选无结果不等于数据库无记录；保存前按页面身份二次确认，并避免单实例并发重复初始化。
                    synchronized (PRODUCT_PAGE_DEFAULT_INIT_LOCK) {
                        boolean exists = this.count(new LambdaQueryWrapper<Decorate>()
                                .eq(Decorate::getDecorateType, singleDecorateType)
                                .eq(Decorate::getPageType, singlePageType)
                                .eq(Decorate::getShopId, defaultDecorate.getShopId())
                                .eq(Decorate::getLocaleId, 0)
                                .eq(Decorate::getParentId, 0)) > 0;
                        if (!exists) {
                            this.save(defaultDecorate);
                            invalidateHomeDecorateAfterCommit();
                        }
                    }
                } else {
                    this.save(defaultDecorate);
                }
                decoratePage = this.page(page, queryWrapper);
            }
        }

        // PC 登录页（decorateType=2 + pageType=5）空列表自动初始化：迁移旧离散 pcUserLogin 背景，回退内置默认
        if (decoratePage.getRecords().isEmpty()
                && Integer.valueOf(2).equals(singleDecorateType)
                && Integer.valueOf(5).equals(singlePageType)) {
            Decorate defaultDecorate = new Decorate();
            defaultDecorate.setDecorateTitle("PC登录页");
            defaultDecorate.setData(buildPcLoginDefaultData());
            defaultDecorate.setDecorateType(2);
            defaultDecorate.setPageType(5);
            defaultDecorate.setIsHome(1);
            defaultDecorate.setShopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
            defaultDecorate.setStatus(1);
            defaultDecorate.setSortOrder(0);
            defaultDecorate.setBindedH5Id(0);
            defaultDecorate.setParentId(0);
            defaultDecorate.setLocaleId(0);
            defaultDecorate.setUpdateTime(StringUtils.getCurrentTime());
            this.save(defaultDecorate);
            decoratePage = this.page(page, queryWrapper);
        }

        // 获取查询结果
        List<Decorate> decoratePageRecords = decoratePage.getRecords();
        if (CollUtil.isEmpty(decoratePageRecords)) {
            return PageUtil.convertPage(decoratePage, List.of());
        }

        // 批量预载子集：一次 parentId IN 查询并按 parentId 分组，替代每条 decorate 单独查子集的 N 次往返
        List<Integer> parentIds = decoratePageRecords.stream()
                .map(Decorate::getDecorateId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, List<Decorate>> childrenByParentId = CollUtil.isEmpty(parentIds)
                ? Collections.emptyMap()
                : this.list(new LambdaQueryWrapper<Decorate>().in(Decorate::getParentId, parentIds))
                .stream()
                .collect(Collectors.groupingBy(Decorate::getParentId));

        // 批量预载子集语言：一次 selectBatchIds，替代每个子集 selectById 的 N×M 次往返
        Set<Integer> localeIds = childrenByParentId.values().stream()
                .flatMap(List::stream)
                .map(Decorate::getLocaleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Locales> localesMap = localeIds.isEmpty()
                ? Collections.emptyMap()
                : localesMapper.selectBatchIds(localeIds).stream()
                .collect(Collectors.toMap(Locales::getId, Function.identity(), (a, b) -> a));

        // 批量预载绑定 H5 页标题：一次 listByIds，替代每条 getById 的 2N 次往返
        Set<Integer> bindedIds = decoratePageRecords.stream()
                .map(Decorate::getBindedH5Id)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Integer, Decorate> bindedMap = bindedIds.isEmpty()
                ? Collections.emptyMap()
                : this.listByIds(bindedIds).stream()
                .collect(Collectors.toMap(Decorate::getDecorateId, Function.identity(), (a, b) -> a));

        // 转换为VO（全部走内存，无逐行 DB 往返）
        List<DecorateVO> decorateVOList = decoratePageRecords.stream()
                .map(decorate -> {
                    DecorateVO decorateVO = new DecorateVO();
                    BeanUtils.copyProperties(decorate, decorateVO);
                    // 模板新旧取自 data.pageModule.homeStyleVersion，缺失兜底 old（旧版）
                    decorateVO.setHomeStyleVersion(resolveHomeStyleVersion(decorate));

                    DateTime date = DateUtil.date(decorate.getUpdateTime() * 1000);
                    decorateVO.setUpdateTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

                    //获得parentId为自己的子集
                    List<Decorate> childList = childrenByParentId.get(decorate.getDecorateId());
                    if (CollUtil.isNotEmpty(childList)) {
                        List<DecorateVO.ChildrenVo> children = childList.stream().map(childDecorate -> {
                            DecorateVO.ChildrenVo childrenVo = new DecorateVO.ChildrenVo();
                            childrenVo.setDecorateId(childDecorate.getDecorateId());
                            childrenVo.setLocaleId(childDecorate.getLocaleId());
                            Locales locales = localesMap.get(childDecorate.getLocaleId());
                            if (locales != null) {
                                childrenVo.setLanguage(locales.getLanguage());
                            }
                            return childrenVo;
                        }).toList();
                        decorateVO.setChildren(children);
                    }

                    if (decorate.getBindedH5Id() != null && decorate.getBindedH5Id() > 0) {
                        Decorate bindingDecorate = bindedMap.get(decorate.getBindedH5Id());
                        decorateVO.setBingdingH5Name(bindingDecorate != null ? bindingDecorate.getDecorateTitle() : "");
                    }

                    return decorateVO;
                }).toList();
        return PageUtil.convertPage(decoratePage, decorateVOList);
    }

    /**
     * 构造移动端主导航初始化数据：
     * 优先迁移旧版 discrete(mobileNav) 历史数据，其次回退到内置默认导航
     */
    private String buildNavH5DefaultData() {
        try {
            DecorateDiscreteVO discrete = decorateDiscreteService.detail("mobileNav");
            if (discrete != null && discrete.getData() != null) {
                JSONObject discreteData = JSONUtil.parseObj(discrete.getData());
                // 兼容 navList / nav_list 两种历史 key
                JSONArray navList = discreteData.getJSONArray("navList");
                if (navList == null || navList.isEmpty()) {
                    navList = discreteData.getJSONArray("nav_list");
                }
                if (navList != null && !navList.isEmpty()) {
                    JSONObject result = new JSONObject();
                    result.set("navList", normalizeNavList(navList));
                    result.set("enableAnimation", false);
                    return result.toString();
                }
            }
        } catch (Exception e) {
            // 历史数据解析失败时回退到内置默认导航
        }
        return InitDecorateDataConstants.NAV_H5_DECORATE_DATA;
    }

    /**
     * 构造 PC 登录页初始化数据：
     * 优先迁移旧版 discrete(pcUserLogin) 的背景图/背景色，其次回退到内置默认（现代版）
     */
    private String buildPcLoginDefaultData() {
        try {
            DecorateDiscreteVO discrete = decorateDiscreteService.detail("pcUserLogin");
            if (discrete != null && discrete.getData() != null) {
                JSONObject discreteData = JSONUtil.parseObj(discrete.getData());
                JSONObject backgroundPic = discreteData.getJSONObject("backgroundPic");
                String backgroundColor = discreteData.getStr("backgroundColor", "");
                boolean hasBgPic = backgroundPic != null && StrUtil.isNotBlank(backgroundPic.getStr("picUrl"));
                if (hasBgPic || StrUtil.isNotBlank(backgroundColor)) {
                    JSONObject module = new JSONObject();
                    module.set("loginMode", 2);
                    module.set("backgroundPic", backgroundPic != null ? backgroundPic : new JSONObject());
                    module.set("backgroundColor", backgroundColor);

                    JSONObject moduleItem = new JSONObject();
                    moduleItem.set("type", "pcLogin");
                    moduleItem.set("url", "pcLogin");
                    moduleItem.set("label", "登录页");
                    moduleItem.set("module", module);
                    moduleItem.set("isShow", true);
                    moduleItem.set("moduleIndex", System.currentTimeMillis());

                    JSONArray moduleList = new JSONArray();
                    moduleList.add(moduleItem);

                    JSONObject pageModule = new JSONObject();
                    pageModule.set("type", "page");
                    pageModule.set("backgroundColor", "");

                    JSONObject result = new JSONObject();
                    result.set("pageModule", pageModule);
                    result.set("moduleList", moduleList);
                    return result.toString();
                }
            }
        } catch (Exception e) {
            // 历史数据解析失败时回退到内置默认
        }
        return InitDecorateDataConstants.PC_LOGIN_DECORATE_DATA;
    }

    /**
     * 将历史导航项的字段统一为当前结构（兼容下划线命名的旧数据）
     */
    private JSONArray normalizeNavList(JSONArray navList) {
        JSONArray result = new JSONArray();
        for (Object o : navList) {
            JSONObject item = JSONUtil.parseObj(o);
            JSONObject n = new JSONObject();
            n.set("picTitle", pickField(item, "picTitle", "pic_title"));
            n.set("picThumb", pickField(item, "picThumb", "pic_thumb"));
            n.set("picUrl", pickField(item, "picUrl", "pic_url"));
            n.set("picActiveThumb", pickField(item, "picActiveThumb", "pic_active_thumb"));
            n.set("picActiveUrl", pickField(item, "picActiveUrl", "pic_active_url"));
            n.set("picName", pickField(item, "picName", "pic_name"));
            n.set("picId", pickField(item, "picId", "pic_id"));
            Object link = item.get("picLink");
            if (link == null) {
                link = item.get("pic_link");
            }
            n.set("picLink", link);
            result.add(n);
        }
        return result;
    }

    private Object pickField(JSONObject item, String camel, String snake) {
        Object v = item.get(camel);
        return v != null ? v : item.get(snake);
    }

    @Override
    public DecorateVO detail(Integer id, Integer localeId, Integer parentId, Integer decorateType) {
        Decorate decorate;
        if (id == null || id == 0) {
            //根据父级 语言 类型查询一个
            decorate = this.getOne(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, parentId).eq(Decorate::getLocaleId, localeId).eq(Decorate::getDecorateType, decorateType));
        } else {
            decorate = this.getById(id);
        }

        if (decorate == null) {
            return new DecorateVO();
        }
        DecorateVO decorateVO = new DecorateVO();
        BeanUtils.copyProperties(decorate, decorateVO);
        // 模板新旧取自 data.pageModule.homeStyleVersion，缺失兜底 old（旧版）
        decorateVO.setHomeStyleVersion(resolveHomeStyleVersion(decorate));
        String defaultData = getH5DefaultDecorateData(decorate.getDecorateType(), decorate.getPageType());
        String data = decorate.getData();
        if (StrUtil.isNotEmpty(data)) {
            decorateVO.setData(JsonUtil.fromJson(data, JSONObject.class));
        } else if (StrUtil.isNotEmpty(defaultData)) {
            decorateVO.setData(JsonUtil.fromJson(defaultData, JSONObject.class));
        }
        String draftData = decorate.getDraftData();
        if (StrUtil.isNotEmpty(draftData)) {
            decorateVO.setDraftData(JsonUtil.fromJson(draftData, JSONObject.class));
        } else if (StrUtil.isNotEmpty(data)) {
            // 已发布但草稿为空时，以已发布数据作为编辑起点，仍关联当前 decorateId。
            decorateVO.setDraftData(JsonUtil.fromJson(data, JSONObject.class));
        } else if (StrUtil.isNotEmpty(defaultData)) {
            // 空记录不追加虚拟列表项；返回内置默认草稿并沿用真实 decorateId，保存时更新当前记录。
            decorateVO.setDraftData(JsonUtil.fromJson(defaultData, JSONObject.class));
        }
        //获得parentId为自己的子集
        List<Decorate> childList = this.list(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, decorate.getDecorateId()));
        if (childList != null && !childList.isEmpty()) {
            List<DecorateVO.ChildrenVo> children = childList.stream().map(childDecorate -> {
                DecorateVO.ChildrenVo childrenVo = new DecorateVO.ChildrenVo();
                childrenVo.setDecorateId(childDecorate.getDecorateId());
                childrenVo.setLocaleId(childDecorate.getLocaleId());
                Locales locales = localesMapper.selectById(childDecorate.getLocaleId());
                if (locales != null) {
                    childrenVo.setLanguage(locales.getLanguage());
                }
                return childrenVo;
            }).toList();
            decorateVO.setChildren(children);
        }
        return decorateVO;
    }

    /**
     * 解析模板样式版本（新旧标签）。
     * 真相源为前端存入的页面数据 data.pageModule.homeStyleVersion（"new"/"old"）；
     * data 为空时回退 draft_data。
     * 空模板数据（data 与 draft_data 均为空，即尚未保存任何页面内容的新建模板）兜底为 "new"（新版）；
     * 有数据但字段缺失或解析异常仍兜底为 "old"（旧版，兼容历史存量页面）。
     */
    private String resolveHomeStyleVersion(Decorate decorate) {
        if (decorate == null) {
            return "old";
        }
        String json = StrUtil.isNotEmpty(decorate.getData()) ? decorate.getData() : decorate.getDraftData();
        if (StrUtil.isEmpty(json)) {
            // 空模板数据（尚未保存任何页面内容）默认按新版返回
            return "new";
        }
        try {
            JSONObject pageModule = JSONUtil.parseObj(json).getJSONObject("pageModule");
            String version = pageModule != null ? pageModule.getStr("homeStyleVersion") : null;
            return StrUtil.isNotEmpty(version) ? version : "old";
        } catch (Exception e) {
            return "old";
        }
    }

    @Override
    public Integer create(DecorateCreateDTO createDTO) {
        if (createDTO != null) {
            Decorate decorate = new Decorate();
            BeanUtils.copyProperties(createDTO, decorate);
            decorate.setUpdateTime(System.currentTimeMillis() / 1000);
            decorate.setShopId(getShopId());
            markMobileLoginAsHome(decorate);
            this.save(decorate);
            // IdType.AUTO：save 后主键自动回填，返回新建装修页ID供前端跳转与后续保存使用
            return decorate.getDecorateId();
        }
        return null;
    }

    @Override
    public boolean update(DecorateUpdateDTO updateDTO) {
        if (updateDTO != null) {
            boolean result;
            if (updateDTO.getDecorateId() != null && updateDTO.getDecorateId() > 0) {
                Decorate existing = this.getById(updateDTO.getDecorateId());
                Decorate decorate = new Decorate();
                BeanUtils.copyProperties(updateDTO, decorate);
                decorate.setUpdateTime(System.currentTimeMillis() / 1000);
                markMobileLoginAsHome(decorate,
                        existing != null ? existing.getDecorateType() : null,
                        existing != null ? existing.getPageType() : null,
                        existing != null ? existing.getShopId() : null);
                result = this.updateById(decorate);
            } else {
                Decorate decorate = new Decorate();
                Decorate parentDecorate = this.getById(updateDTO.getParentId());
                decorate.setParentId(updateDTO.getParentId());
                decorate.setDecorateTitle(parentDecorate.getDecorateTitle());
                decorate.setDecorateType(parentDecorate.getDecorateType());
                decorate.setIsHome(0);
                decorate.setShopId(parentDecorate.getShopId());
                decorate.setLocaleId(updateDTO.getLocaleId());
                decorate.setData(updateDTO.getData());
                decorate.setUpdateTime(System.currentTimeMillis() / 1000);
                //创建
                result = this.save(decorate);
            }
            invalidateHomeDecorateAfterCommit();
            return result;
        }
        return false;
    }

    /**
     * 移动端登录页是平台级唯一入口，必须作为生效首页保存。
     * 管理端新增/编辑请求可能不携带 isHome，不能依赖前端补传该字段。
     */
    private void markMobileLoginAsHome(Decorate decorate) {
        markMobileLoginAsHome(decorate, null, null, null);
    }

    private void markMobileLoginAsHome(Decorate decorate, Integer existingDecorateType,
                                       Integer existingPageType, Integer existingShopId) {
        if (decorate != null
                && Objects.equals(valueOrDefault(decorate.getDecorateType(), existingDecorateType), DecorateType.H5.getCode())
                && Objects.equals(valueOrDefault(decorate.getPageType(), existingPageType), 5)
                && Objects.equals(valueOrDefault(decorate.getShopId(), existingShopId), 0)) {
            decorate.setIsHome(1);
        }
    }

    private Integer valueOrDefault(Integer value, Integer fallback) {
        return value != null ? value : fallback;
    }

    @Override
    public boolean copy(Integer id) {
        if (id == null) {
            return false;
        }
        Decorate decorate = this.getById(id);
        if (decorate == null) {
            return false;
        }
        decorate.setDecorateId(null);
        decorate.setIsHome(0);
        decorate.setDecorateTitle(generateCopyTitle(decorate));
        decorate.setUpdateTime(System.currentTimeMillis() / 1000);

        return this.save(decorate);
    }

    /**
     * 生成复制副本标题，避免「-复制-复制」无限叠加。
     * 规则：先剥离原标题末尾已有的「-复制」/「-复制N」后缀取基础名，
     * 再在同店铺 + 同装修类型 + 同页面类型范围内取已有副本的最大序号，
     * 首个副本为「基础名-复制」，之后递增为「基础名-复制2」「基础名-复制3」……
     *
     * @param source 被复制的原始页面
     * @return 不与同范围内现有副本冲突的新标题
     */
    private String generateCopyTitle(Decorate source) {
        final String suffix = "-复制";
        String rawTitle = StrUtil.nullToEmpty(source.getDecorateTitle());
        // 去掉末尾连续的「-复制」「-复制N」后缀，取基础名，防止叠加
        String baseTitle = rawTitle.replaceAll("(" + suffix + "\\d*)+$", "");
        if (StrUtil.isBlank(baseTitle)) {
            baseTitle = StrUtil.isBlank(rawTitle) ? "页面" : rawTitle;
        }
        String prefix = baseTitle + suffix;

        // 同店铺 + 同装修类型 + 同页面类型范围内，已存在的同前缀副本
        List<Decorate> existList = this.list(new LambdaQueryWrapper<Decorate>()
                .eq(Decorate::getShopId, source.getShopId())
                .eq(Decorate::getDecorateType, source.getDecorateType())
                .eq(source.getPageType() != null, Decorate::getPageType, source.getPageType())
                .likeRight(Decorate::getDecorateTitle, prefix)
                .select(Decorate::getDecorateTitle));

        int maxSeq = 0;
        for (Decorate item : existList) {
            String title = item.getDecorateTitle();
            if (title == null || !title.startsWith(prefix)) {
                continue;
            }
            String seqStr = title.substring(prefix.length());
            if (seqStr.isEmpty()) {
                // 「基础名-复制」视为序号 1
                maxSeq = Math.max(maxSeq, 1);
            } else if (seqStr.matches("\\d{1,8}")) {
                maxSeq = Math.max(maxSeq, Integer.parseInt(seqStr));
            }
            // 其它如「基础名-复制版」不计入序号
        }

        int nextSeq = maxSeq + 1;
        return nextSeq == 1 ? prefix : prefix + nextSeq;
    }

    @Override
    public boolean setHome(Integer id, Integer pageType) {
        if (id != null) {

            Decorate decorate = this.getById(id);
            //将decorate_type为同类型的is_home设置为0
            LambdaQueryWrapper<Decorate> resetWrapper = new LambdaQueryWrapper<>();
            resetWrapper.eq(Decorate::getDecorateType, decorate.getDecorateType());
            resetWrapper.eq(Decorate::getShopId, HeaderUtils.getShopId());
            if (pageType != null) {
                resetWrapper.eq(Decorate::getPageType, pageType);
            }
            Decorate resetEntity = new Decorate();
            resetEntity.setIsHome(0);
            this.update(resetEntity, resetWrapper);

            decorate.setIsHome(1);
            if (pageType != null) {
                decorate.setPageType(pageType);
            }
            boolean result = this.updateById(decorate);
            invalidateHomeDecorateAfterCommit();
            return result;
        }
        return false;
    }

    @Override
    public boolean saveDecorateToDraft(Integer id, JSONObject data, Integer parentId, Integer localeId) {

        if (id != null) {
            Decorate decorate = this.getById(id);
            decorate.setDraftData(JsonUtil.toJson(data));
            return this.updateById(decorate);
        } else {
            Decorate parentDecorate = this.getById(parentId);
            Decorate decorateOne = this.getOne(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, parentId).eq(Decorate::getLocaleId, localeId).eq(Decorate::getDecorateType, parentDecorate.getDecorateType()));
            if (decorateOne != null) {
                decorateOne.setDraftData(JsonUtil.toJson(data));
                return this.updateById(decorateOne);
            } else {
                Decorate decorate = new Decorate();
                decorate.setParentId(parentId);
                decorate.setDecorateTitle(parentDecorate.getDecorateTitle());
                decorate.setDecorateType(parentDecorate.getDecorateType());
                decorate.setIsHome(0);
                decorate.setShopId(parentDecorate.getShopId());
                decorate.setLocaleId(localeId);
                decorate.setDraftData(JsonUtil.toJson(data));
                return this.save(decorate);
            }
        }
    }

    @Override
    public boolean publishDecorate(Integer id, JSONObject data, Integer parentId, Integer localeId) {
        boolean result;
        if (id != null && id > 0) {
            Decorate decorate = this.getById(id);
            decorate.setData(JsonUtil.toJson(data));
            decorate.setStatus(1);
            decorate.setUpdateTime(System.currentTimeMillis() / 1000);
            decorate.setDraftData("");
            result = this.updateById(decorate);
        } else {
            Decorate parentDecorate = this.getById(parentId);

            Decorate decorateOne = this.getOne(new LambdaQueryWrapper<Decorate>()
                    .eq(Decorate::getParentId, parentId)
                    .eq(Decorate::getLocaleId, localeId)
                    .eq(Decorate::getDecorateType, parentDecorate.getDecorateType()));
            if (decorateOne != null) {
                decorateOne.setData(JsonUtil.toJson(data));
                result = this.updateById(decorateOne);
            } else {
                Decorate decorate = new Decorate();
                decorate.setParentId(parentId);
                decorate.setDecorateTitle(parentDecorate.getDecorateTitle());
                decorate.setDecorateType(parentDecorate.getDecorateType());
                decorate.setIsHome(0);
                decorate.setShopId(parentDecorate.getShopId());
                decorate.setLocaleId(localeId);
                decorate.setData(JsonUtil.toJson(data));
                result = this.save(decorate);
            }
        }
        invalidateHomeDecorateAfterCommit();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDecorate(Integer shopId) {
        Decorate decorate = new Decorate();
        decorate.setDecorateTitle("默认模板");
        if (tigshopProperties.getIsO2o() == 1) {
            decorate.setData(InitDecorateDataConstants.O2O_H5_DECORATE_DATA);
        } else {
            decorate.setData(InitDecorateDataConstants.DECORATE_DATA);
        }
        decorate.setDecorateType(1);
        decorate.setIsHome(1);
        decorate.setShopId(shopId);
        decorate.setStatus(1);
        decorate.setUpdateTime(StringUtils.getCurrentTime());
        this.save(decorate);
    }

    @Override
    public boolean del(Integer id) {
        boolean removed = super.del(id);
        if (removed) {
            invalidateHomeDecorateAfterCommit();
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        boolean result = super.batch(batchDTO);
        if (result) {
            invalidateHomeDecorateAfterCommit();
        }
        return result;
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        boolean updated = super.updateField(updateField, allowFields);
        if (updated) {
            invalidateHomeDecorateAfterCommit();
        }
        return updated;
    }

    @Override
    public boolean updateByIdRefreshHome(Decorate decorate) {
        boolean updated = this.updateById(decorate);
        if (updated) {
            invalidateHomeDecorateAfterCommit();
        }
        return updated;
    }

    @Override
    public JSONObject getPcIndexDecoratePageConfig(Integer id) {
        if (id != null) {
            Decorate decorate = this.getOne(new LambdaQueryWrapper<Decorate>().eq(Decorate::getDecorateId, id));
            return JsonUtil.fromJson(decorate.getData(), JSONObject.class).getJSONObject("pageModule");
        }
        Decorate decorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                // 自营
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getDecorateType, DecorateType.H5.getCode())
                // 页面用途：1商城；导航/分类/个人中心等页面同样是 is_home=1，必须按 page_type 区分
                .eq(Decorate::getPageType, 1)
                // 1是首页，0非首页
                .eq(Decorate::getIsHome, 1)
                // 1发布，0未发布
                .eq(Decorate::getStatus, 1)
                // 只取基准语言行，语言子行不参与
                .eq(Decorate::getLocaleId, 0)
                .last(" limit 1"));

        if (decorate == null) {
            return null;
        }
        JSONObject decorateData = JsonUtil.fromJson(decorate.getData(), JSONObject.class);
        return JsonUtil.fromJson(decorateData.get("pageModule").toString(), JSONObject.class);
    }

    @Override
    public JSONObject getShopIndexDecorateData(Integer shopId, Integer previewId) {
        Decorate decorate;
        if (previewId != null) {
            decorate = this.getById(previewId);
        } else {
            LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Decorate::getShopId, shopId);
            queryWrapper.eq(Decorate::getIsHome, 1);
            queryWrapper.eq(Decorate::getStatus, 1);
            queryWrapper.eq(Decorate::getDecorateType, 1);
            queryWrapper.last("limit 1");
            decorate = this.getOne(queryWrapper);
        }
        if (decorate == null) {
            throw new GlobalException(messageHelper.get("error.decorate.template.not.exist"));
        }

        if (HeaderUtils.getHeaderValue("X-Locale-Code") != null) {
            Locales locales = localesMapper.selectOne(new LambdaQueryWrapper<Locales>().eq(Locales::getLocaleCode, HeaderUtils.getHeaderValue("X-Locale-Code")));
            if (locales != null && locales.getIsDefault() != 1) {
                Decorate localesDecorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                        .eq(Decorate::getParentId, decorate.getDecorateId())
                        .eq(Decorate::getLocaleId, locales.getId())
                );
                if (localesDecorate != null) {
                    decorate = localesDecorate;
                }
            }
        }

        JSONObject decorateData = JSONUtil.parseObj(decorate.getData());
        decorateData.set("decorateId", decorate.getDecorateId());

        return decorateData;
    }

    @Override
    public DecorateModuleListVO getAppHomeDecorate() {
        return getDecorateModule(DecorateType.H5.getCode(), true, 1);
    }

    @Override
    public DecorateModuleListVO getPcHomeDecorate() {
        return getDecorateModule(DecorateType.PC.getCode(), true, 1);
    }

    @Override
    public DecorateModuleListVO getPcLoginDecorate() {
        try {
            return getDecorateModule(DecorateType.PC.getCode(), 5, true, 1);
        } catch (Exception e) {
            // 无生效记录兜底：返回内置默认（现代版 loginMode=2 + 空背景），保证登录页不因未配置而 500
            log.warn("getPcLoginDecorate 无生效记录，回退内置默认: {}", e.getMessage());
            DecorateModuleListVO vo = new DecorateModuleListVO();
            JsonNode entries = JsonUtil.convertKeysToCamelCase(JsonUtil.fromJson(InitDecorateDataConstants.PC_LOGIN_DECORATE_DATA, JsonNode.class));
            DecorateDataDTO decorateData = JsonUtil.fromJson(entries.toString(), DecorateDataDTO.class);
            if (decorateData != null) {
                vo.setModuleList(decorateData.getModuleList());
                vo.setPageModule(decorateData.getPageModule());
            }
            vo.setIsHome(1);
            return vo;
        }
    }

    @Override
    public DecorateModuleListVO getPcSearchDecorate(Integer previewId) {
        // 预览态：按 id 取搜索页草稿（与 pcIndex 预览一致，pageType=6）
        if (previewId != null && previewId > 0) {
            return getPcPreviewDecorate(previewId, null, 6);
        }
        // 发布态：PC 搜索页未配置装修时返回空 VO，避免 getDecorateModule 抛 TEMPLATE_NOT_EXIST（对齐 vortmall 优雅返回）
        boolean exists = this.count(new LambdaQueryWrapper<Decorate>()
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getDecorateType, DecorateType.PC.getCode())
                .eq(Decorate::getPageType, 6)
                .eq(Decorate::getIsHome, 1)
                .eq(Decorate::getStatus, 1)
                .eq(Decorate::getLocaleId, 0)) > 0;
        if (!exists) {
            return new DecorateModuleListVO();
        }
        return getDecorateModule(DecorateType.PC.getCode(), 6, true, 1);
    }

    @Override
    public DecorateModuleListVO getH5SearchDecorate(Integer previewId) {
        // 预览态：按 id 取搜索页草稿（与其他移动端离散装修页一致，pageType=6）
        if (previewId != null && previewId > 0) {
            return getAppPreviewDecorate(previewId, 6);
        }
        // 发布态统一走内置默认回退：无记录或 data 为空时按运行环境返回普通版/跨境版搜索装修。
        return getDecorateModule(DecorateType.H5.getCode(), 6, true, 1);
    }

    @Override
    public Object getPcHeader(Integer previewId) {
        String sourceData = null;
        // 预览态优先取草稿，草稿为空回退已发布数据
        if (previewId != null && previewId > 0) {
            Decorate previewDecorate = this.getById(previewId);
            if (previewDecorate != null) {
                sourceData = StrUtil.isNotBlank(previewDecorate.getDraftData())
                        ? previewDecorate.getDraftData()
                        : previewDecorate.getData();
            }
        }
        // 非预览或预览数据为空：取 PC 首页已发布装修
        if (StrUtil.isBlank(sourceData)) {
            Decorate pcHome = this.getOne(new LambdaQueryWrapper<Decorate>()
                    .eq(Decorate::getShopId, 0)
                    .eq(Decorate::getDecorateType, DecorateType.PC.getCode())
                    .eq(Decorate::getPageType, 1)
                    .eq(Decorate::getIsHome, 1)
                    .eq(Decorate::getStatus, 1)
                    .eq(Decorate::getLocaleId, 0)
                    .last(" limit 1"));
            if (pcHome != null) {
                Decorate localeDecorate = getLocaleDecorate(pcHome);
                sourceData = localeDecorate != null && StrUtil.isNotBlank(localeDecorate.getData())
                        ? localeDecorate.getData()
                        : pcHome.getData();
            }
        }
        if (StrUtil.isBlank(sourceData)) {
            return null;
        }
        try {
            // 直接解析、不走 convertKeysToCamelCase，避免 logoWidth/showMainNav 等数值被转成字符串
            JSONObject dataObj = JsonUtil.fromJson(sourceData, JSONObject.class);
            if (dataObj == null) {
                return null;
            }
            JSONObject pageModule = dataObj.getJSONObject("pageModule");
            if (pageModule == null) {
                return null;
            }
            return pageModule.get("pcHeader");
        } catch (Exception e) {
            // 解析失败按"未配置头部"处理，前台回退旧版头部
            return null;
        }
    }

    /**
     * 按请求头 {@code X-Locale-Code} 取基准装修对应的语言子行；默认语言或无子行时返回 null。
     */
    private Decorate getLocaleDecorate(Decorate baseDecorate) {
        String localeCode = HeaderUtils.getHeaderValue("X-Locale-Code");
        if (localeCode == null || baseDecorate == null || baseDecorate.getDecorateId() == null) {
            return null;
        }
        Locales locales = localesMapper.selectOne(new LambdaQueryWrapper<Locales>()
                .eq(Locales::getLocaleCode, localeCode));
        if (locales == null || Integer.valueOf(1).equals(locales.getIsDefault())) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<Decorate>()
                .eq(Decorate::getParentId, baseDecorate.getDecorateId())
                .eq(Decorate::getLocaleId, locales.getId())
                .last(" limit 1"));
    }

    /**
     * 首页装修缓存 key：装修类型 + 页面类型 + 是否首页 + 状态 + 语言码。
     * 语言码取自请求头 {@code X-Locale-Code}（为空表示默认语言），保证不同语言返回各自快照。
     */
    private String buildHomeCacheKey(Integer type, Integer pageType, Boolean isHome, Integer status) {
        String localeCode = HeaderUtils.getHeaderValue("X-Locale-Code");
        return type + ":" + pageType + ":" + isHome + ":" + status + ":" + (localeCode == null ? "" : localeCode);
    }

    /**
     * 装修写操作提交后失效进程内首页装修快照并跨进程广播。
     */
    private void invalidateHomeDecorateAfterCommit() {
        TransactionAfterCommitExecutor.execute(() -> homeDecorateCache.invalidateAndBroadcast());
    }


    @Override
    public DecorateModuleListVO getAppPreviewDecorate(Integer decorateId,Integer pageType) {
        return getPreviewDecorate(DecorateType.H5.getCode(), decorateId, null,  pageType);
    }

    /**
     * 获取预览的模块数据
     * @param decorateId
     * @param id 如果id != null则返回decorate date 数据，否则返回decorate draft_data
     * @return
     */
    @Override
    public DecorateModuleListVO getPcPreviewDecorate(Integer decorateId, Integer id, Integer pageType) {
        return getPreviewDecorate(null, decorateId, id,pageType);
    }

    @Override
    public DecorateModuleListVO getPreviewDecorate(Integer type, Integer decorateId, Integer id, Integer pageType) {

        //传了id 表示使用已发布的数据 data
        decorateId = decorateId == null ? id : decorateId;

        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            queryWrapper.eq(Decorate::getDecorateType, type);
        }
        // previewId 即 decorate_id（唯一主键），按主键查询已能唯一定位，无需再叠加 page_type 过滤；
        // 叠加 page_type 会导致非首页页面（如 page_type=3 自定义活动页）预览时误报"模板不存在"
        queryWrapper.eq(Decorate::getDecorateId, decorateId);
        Decorate decorate = this.getOne(queryWrapper);
        if (decorate == null) {
            throw new GlobalException(messageHelper.get("error.decorate.template.not.exist"));
        }
        DecorateModuleListVO decorateModuleListVO = new DecorateModuleListVO();
        decorateModuleListVO.setDecorateId(decorate.getDecorateId());


        String defaultData = getH5DefaultDecorateData(
                decorate.getDecorateType(),
                decorate.getPageType() != null ? decorate.getPageType() : pageType);
        String sourceData;
        if (id != null) {
            sourceData = StrUtil.isNotBlank(decorate.getData()) ? decorate.getData() : defaultData;
        } else if (StrUtil.isNotBlank(decorate.getDraftData())) {
            sourceData = decorate.getDraftData();
        } else if (StrUtil.isNotBlank(decorate.getData())) {
            sourceData = decorate.getData();
        } else {
            sourceData = defaultData;
        }
        JsonNode entries = JsonUtil.fromJson(sourceData, JsonNode.class);
        if(entries!=null){
            entries = JsonUtil.convertKeysToCamelCase(entries);
            if(entries.get("moduleList")!=null){
                decorateModuleListVO.setModuleList(JSONUtil.parseArray(entries.get("moduleList").toString()).toList(DecorateModuleListDTO.class));
            }
            if(Objects.nonNull(decorateModuleListVO.getModuleList())){
                decorateModuleListVO.getModuleList().removeIf(module -> module.getIsShow() != null && !module.getIsShow());
            }
            if (entries.has("pageModule") && entries.get("pageModule") != null) {
                decorateModuleListVO.setPageModule(JsonUtil.fromJson(entries.get("pageModule").toString(), Map.class));
            }
        }
        if (decorate.getBindedH5Id() != null) {
            decorateModuleListVO.setBindDecorateId(decorate.getBindedH5Id());
        }
        decorateModuleListVO.setIsHome(decorate.getIsHome());
        return decorateModuleListVO;
    }

    @Override
    public DecorateModuleListVO getDecorateModule(Integer type, Integer pageType, Boolean isHome, Integer status) {
        String cacheKey = buildHomeCacheKey(type, pageType, isHome, status);
        return homeDecorateCache.get(cacheKey, () -> loadDecorateModule(type, pageType, isHome, status));
    }

    private DecorateModuleListVO loadDecorateModule(Integer type, Integer pageType, Boolean isHome, Integer status) {
        String defaultData = getH5DefaultDecorateData(type, pageType);
        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Decorate::getDecorateType, type)
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getIsHome, isHome)
                .eq(Decorate::getStatus, status)
                .eq(Decorate::getLocaleId, 0);
        if (pageType != null) {
            queryWrapper.eq(Decorate::getPageType, pageType);
        }
        queryWrapper.last("limit 1");
        Decorate decorate = this.getOne(queryWrapper);

        if (decorate == null) {
            if (defaultData == null) {
                throw new GlobalException(messageHelper.get("error.decorate.template.not.exist"));
            }
            decorate = new Decorate();
            decorate.setData(defaultData);
            decorate.setIsHome(Boolean.TRUE.equals(isHome) ? 1 : 0);
            log.warn("H5装修无生效记录，回退内置默认: pageType={}", pageType);
        } else if (HeaderUtils.getHeaderValue("X-Locale-Code") != null) {
            Locales locales = localesMapper.selectOne(new LambdaQueryWrapper<Locales>().eq(Locales::getLocaleCode, HeaderUtils.getHeaderValue("X-Locale-Code")));
            if (locales != null && locales.getIsDefault() != 1) {
                Decorate localesDecorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                        .eq(Decorate::getParentId, decorate.getDecorateId())
                        .eq(Decorate::getLocaleId, locales.getId())
                );
                if (localesDecorate != null) {
                    decorate = localesDecorate;
                }
            }
        }

        String sourceData = decorate.getData();
        if (StrUtil.isBlank(sourceData) && defaultData != null) {
            sourceData = defaultData;
            log.warn("H5装修发布数据为空，回退内置默认: decorateId={}, pageType={}",
                    decorate.getDecorateId(), pageType);
        }

        JsonNode entries = JsonUtil.fromJson(sourceData, JsonNode.class);
        entries = JsonUtil.convertKeysToCamelCase(entries);

        DecorateDataDTO decorateData = JsonUtil.fromJson(entries.toString(), DecorateDataDTO.class);
        if (defaultData != null
                && (decorateData == null || CollUtil.isEmpty(decorateData.getModuleList()))
                && !Objects.equals(sourceData, defaultData)) {
            JsonNode defaultEntries = JsonUtil.convertKeysToCamelCase(
                    JsonUtil.fromJson(defaultData, JsonNode.class));
            decorateData = JsonUtil.fromJson(defaultEntries.toString(), DecorateDataDTO.class);
            log.warn("H5装修模块为空，回退内置默认: decorateId={}, pageType={}",
                    decorate.getDecorateId(), pageType);
        }

        if (decorateData != null) {
            decorateData.getModuleList().removeIf(module -> module.getIsShow() != null && !module.getIsShow());
        }

        DecorateModuleListVO decorateModuleListVO = new DecorateModuleListVO();
        decorateModuleListVO.setDecorateId(decorate.getDecorateId());
        decorateModuleListVO.setModuleList(decorateData != null ? decorateData.getModuleList() : null);
        decorateModuleListVO.setPageModule(decorateData != null ? decorateData.getPageModule() : null);
        if (CollUtil.isNotEmpty(decorateModuleListVO.getModuleList())) {
            for (DecorateModuleListDTO decorateModuleListDTO : decorateModuleListVO.getModuleList()) {
                if ("catNav".equals(decorateModuleListDTO.getType())) {
                    JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                    List<MobileCatNav> list = mobileCatNavService.list(new LambdaQueryWrapper<MobileCatNav>()
                            .eq(MobileCatNav::getIsShow, true)
                            .orderByAsc(MobileCatNav::getSortOrder)
                            .orderByAsc(MobileCatNav::getMobileCatNavId)
                    );
                    List<MobileCatNavVO> mobileCatNavVOList = buildMobileCatNavVOList(list);
                    module.set("navList", mobileCatNavVOList);
                    decorateModuleListDTO.setModule(module);
                }
                if ("specialOffer".equals(decorateModuleListDTO.getType())) {
                    JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                    JSONObject moduleTitle = module.getJSONObject("moduleTitle");
                    JSONArray dailyTime = moduleTitle.getJSONArray("dailyTime");
                    List<Long> list = dailyTime.stream().map(o -> Long.parseLong(o.toString())).toList();
                    moduleTitle.set("dailyTime", list);
                    module.set("moduleTitle", moduleTitle);
                    decorateModuleListDTO.setModule(module);
                }
            }
        }
        if (decorate.getBindedH5Id() != null) {
            decorateModuleListVO.setBindDecorateId(decorate.getBindedH5Id());
        }
        decorateModuleListVO.setIsHome(decorate.getIsHome());
        return decorateModuleListVO;
    }

    private String getH5DefaultDecorateData(Integer type, Integer pageType) {
        if (!Objects.equals(type, DecorateType.H5.getCode()) || pageType == null) {
            return null;
        }
        boolean overseas = tigshopProperties.getIsOverseas() == 1;
        return switch (pageType) {
            case 2 -> overseas
                    ? InitDecorateDataConstants.OVERSEAS_MEMBER_CENTER_H5_DECORATE_DATA
                    : InitDecorateDataConstants.MEMBER_CENTER_H5_DECORATE_DATA;
            case 4 -> InitDecorateDataConstants.CATEGORY_HOME_H5_DECORATE_DATA;
            case 6 -> overseas
                    ? ProductPageDecorateDefaults.OVERSEAS_SEARCH
                    : ProductPageDecorateDefaults.SEARCH;
            case 8 -> overseas
                    ? ProductPageDecorateDefaults.OVERSEAS_PRODUCT_DETAIL
                    : ProductPageDecorateDefaults.PRODUCT_DETAIL;
            default -> null;
        };
    }

    @Override
    public DecorateModuleListVO getDecorateModule(Integer type, Boolean isHome, Integer status) {
        String cacheKey = buildHomeCacheKey(type, 1, isHome, status);
        return homeDecorateCache.get(cacheKey, () -> loadHomeDecorateModule(type, isHome, status));
    }

    private DecorateModuleListVO loadHomeDecorateModule(Integer type, Boolean isHome, Integer status) {

        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Decorate::getDecorateType, type)
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getIsHome, isHome)
                .eq(Decorate::getStatus, status)
                .eq(Decorate::getLocaleId, 0)
                .eq(Decorate::getPageType,1)
                .last("limit 1");
        Decorate decorate = this.getOne(queryWrapper);

        if (HeaderUtils.getHeaderValue("X-Locale-Code") != null) {
            Locales locales = localesMapper.selectOne(new LambdaQueryWrapper<Locales>().eq(Locales::getLocaleCode, HeaderUtils.getHeaderValue("X-Locale-Code")));
            if (locales != null && locales.getIsDefault() != 1) {
                Decorate localesDecorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                        .eq(Decorate::getParentId, decorate.getDecorateId())
                        .eq(Decorate::getLocaleId, locales.getId())
                );
                if (localesDecorate != null) {
                    decorate = localesDecorate;
                }
            }
        }
        if (decorate == null) {
            throw new GlobalException(messageHelper.get("error.decorate.template.not.exist"));
        }

        JsonNode entries = JsonUtil.fromJson(decorate.getData(), JsonNode.class);
        entries = JsonUtil.convertKeysToCamelCase(entries);

        DecorateDataDTO decorateData = JsonUtil.fromJson(entries.toString(), DecorateDataDTO.class);


        //moduleList里过滤掉is_show=false的模块
        if (decorateData != null) {
            decorateData.getModuleList().removeIf(module -> module.getIsShow() != null && !module.getIsShow());
        }

        DecorateModuleListVO decorateModuleListVO = new DecorateModuleListVO();
        decorateModuleListVO.setDecorateId(decorate.getDecorateId());
        decorateModuleListVO.setModuleList(decorateData != null ? decorateData.getModuleList() : null);
        decorateModuleListVO.setPageModule(decorateData != null ? decorateData.getPageModule() : null);
        if (CollUtil.isNotEmpty(decorateModuleListVO.getModuleList())) {
            for (DecorateModuleListDTO decorateModuleListDTO : decorateModuleListVO.getModuleList()) {
                if ("catNav".equals(decorateModuleListDTO.getType())) {
                    JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                    List<MobileCatNav> list = mobileCatNavService.list(new LambdaQueryWrapper<MobileCatNav>()
                            .eq(MobileCatNav::getIsShow, true)
                            .orderByAsc(MobileCatNav::getSortOrder)
                            .orderByAsc(MobileCatNav::getMobileCatNavId)
                    );
                    List<MobileCatNavVO> mobileCatNavVOList = buildMobileCatNavVOList(list);
                    module.set("navList", mobileCatNavVOList);
                    decorateModuleListDTO.setModule(module);
                }
                if ("specialOffer".equals(decorateModuleListDTO.getType())) {
                    JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                    JSONObject moduleTitle = module.getJSONObject("moduleTitle");
                    JSONArray dailyTime = moduleTitle.getJSONArray("dailyTime");
                    List<Long> list = dailyTime.stream().map(o -> Long.parseLong(o.toString())).toList();
                    moduleTitle.set("dailyTime", list);
                    module.set("moduleTitle", moduleTitle);
                    decorateModuleListDTO.setModule(module);
                }
            }
        }
        if (decorate.getBindedH5Id() != null) {
            decorateModuleListVO.setBindDecorateId(decorate.getBindedH5Id());
        }
        return decorateModuleListVO;
    }

    @Override
    public JSONObject loadDraftData(Integer id) {
        DecorateVO one = detail(id, null, 0, 0);
        if (one == null) {
            throw new GlobalException("装修不存在");
        }
        return one.getDraftData();
    }

    @Override
    public Map<String, Object> getPreviewDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params) {
        Decorate decorate = getById(decorateId);
        if (decorate == null) {
            throw new GlobalException("模板不存在");
        }
        String draftData = decorate.getDraftData();
        JSONObject draftDataJson = JSONUtil.parseObj(draftData);
        JSONArray moduleList = draftDataJson.getJSONArray("moduleList");
        for (int i = 0; i < moduleList.size(); i++) {
            if (Objects.equals(moduleList.getJSONObject(i).getStr("moduleIndex"), moduleIndex)) {
                List<Integer> list = moduleList.getJSONObject(i).getJSONObject("module").getJSONArray("catIds").toList(Integer.class);
                return Map.of("catIds", list, "catList", formatData(moduleList.getJSONObject(i).getStr("type"), list, params, decorate));
            }
        }
        return Map.of();
    }

    @Override
    public Map<String, Object> getDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params) {
        Decorate decorate = getById(decorateId);
        if (decorate == null) {
            throw new GlobalException("模板不存在");
        }
        String data = decorate.getData();
        JSONObject dataJson = JSONUtil.parseObj(data);
        JSONArray moduleList = dataJson.getJSONArray("moduleList");
        for (int i = 0; i < moduleList.size(); i++) {
            if (Objects.equals(moduleList.getJSONObject(i).getStr("moduleIndex"), moduleIndex)) {
                List<Integer> list = moduleList.getJSONObject(i).getJSONObject("module").getJSONArray("catIds").toList(Integer.class);
                return Map.of("catIds", list, "catList", formatData(moduleList.getJSONObject(i).getStr("type"), list, params, decorate));
            }
        }
        return Map.of();
    }

    @Override
    public DecorateModuleListVO customDecorate(Integer previewId) {
        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Decorate::getDecorateType, DecorateType.CUSTOM.getCode())
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getLocaleId, 0)
                .last("limit 1");
        Decorate decorate = this.getOne(queryWrapper);

        if (HeaderUtils.getHeaderValue("X-Locale-Code") != null) {
            Locales locales = localesMapper.selectOne(new LambdaQueryWrapper<Locales>().eq(Locales::getLocaleCode, HeaderUtils.getHeaderValue("X-Locale-Code")));
            if (locales != null && locales.getIsDefault() != 1) {
                Decorate localesDecorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                        .eq(Decorate::getParentId, decorate.getDecorateId())
                        .eq(Decorate::getLocaleId, locales.getId())
                );
                if (localesDecorate != null) {
                    decorate = localesDecorate;
                }
            }
        }
        if (decorate == null) {
            throw new GlobalException(messageHelper.get("error.decorate.template.not.exist"));
        }

        JsonNode entries = JsonUtil.fromJson(decorate.getData(), JsonNode.class);
        entries = JsonUtil.convertKeysToCamelCase(entries);

        DecorateDataDTO decorateData = JsonUtil.fromJson(entries.toString(), DecorateDataDTO.class);


        //moduleList里过滤掉is_show=false的模块
        if (decorateData != null) {
            decorateData.getModuleList().removeIf(module -> module.getIsShow() != null && !module.getIsShow());
        }

        DecorateModuleListVO decorateModuleListVO = new DecorateModuleListVO();
        decorateModuleListVO.setDecorateId(decorate.getDecorateId());
        decorateModuleListVO.setModuleList(decorateData != null ? decorateData.getModuleList() : null);
        decorateModuleListVO.setPageModule(decorateData != null ? decorateData.getPageModule() : null);
        if (CollUtil.isNotEmpty(decorateModuleListVO.getModuleList())) {
            for (DecorateModuleListDTO decorateModuleListDTO : decorateModuleListVO.getModuleList()) {
                if ("catNav".equals(decorateModuleListDTO.getType())) {
                    JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                    List<MobileCatNav> list = mobileCatNavService.list(new LambdaQueryWrapper<MobileCatNav>()
                            .eq(MobileCatNav::getIsShow, true)
                            .orderByAsc(MobileCatNav::getSortOrder)
                            .orderByAsc(MobileCatNav::getMobileCatNavId)
                    );
                    List<MobileCatNavVO> mobileCatNavVOList = buildMobileCatNavVOList(list);
                    module.set("navList", mobileCatNavVOList);
                    decorateModuleListDTO.setModule(module);
                }
                if ("specialOffer".equals(decorateModuleListDTO.getType())) {
                    JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                    JSONObject moduleTitle = module.getJSONObject("moduleTitle");
                    JSONArray dailyTime = moduleTitle.getJSONArray("dailyTime");
                    List<Long> list = dailyTime.stream().map(o -> Long.parseLong(o.toString())).toList();
                    moduleTitle.set("dailyTime", list);
                    module.set("moduleTitle", moduleTitle);
                    decorateModuleListDTO.setModule(module);
                }
            }
        }
        if (decorate.getBindedH5Id() != null) {
            decorateModuleListVO.setBindDecorateId(decorate.getBindedH5Id());
        }
        return decorateModuleListVO;
    }


    public Object formatData(String type, List<Integer> module, Map<String, Object> params, Decorate decorate) {
        if (ObjectUtil.equals(type, "pcCatWide")) {
            return categoryService.getCategoryByIds(module);
        }
        return List.of();
    }

    private List<MobileCatNavVO> buildMobileCatNavVOList(List<MobileCatNav> list) {
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }

        Set<Integer> allCategoryIds = new HashSet<>();
        Set<Integer> allBrandIds = new HashSet<>();

        for (MobileCatNav nav : list) {
            allCategoryIds.add(nav.getCategoryId());
            Integer[] childCatIds = JsonUtil.fromJson(nav.getChildCatIds(), Integer[].class);
            if (childCatIds != null) {
                allCategoryIds.addAll(Arrays.asList(childCatIds));
            }
            Integer[] brandIds = JsonUtil.fromJson(nav.getBrandIds(), Integer[].class);
            if (brandIds != null) {
                allBrandIds.addAll(Arrays.asList(brandIds));
            }
        }

        Map<Integer, Category> categoryMap = CollUtil.isNotEmpty(allCategoryIds)
                ? categoryService.getCategoryByIds(new ArrayList<>(allCategoryIds)).stream()
                    .collect(Collectors.toMap(Category::getCategoryId, Function.identity(), (a, b) -> a))
                : new HashMap<>();

        Map<Integer, Brand> brandMap = CollUtil.isNotEmpty(allBrandIds)
                ? brandService.listByIds(allBrandIds).stream()
                    .collect(Collectors.toMap(Brand::getBrandId, Function.identity(), (a, b) -> a))
                : new HashMap<>();

        return list.stream().map(nav -> {
            MobileCatNavVO vo = new MobileCatNavVO();
            BeanUtils.copyProperties(nav, vo);

            vo.setImgUrl(JsonUtil.fromJson(nav.getImgUrl(), GalleryPicDTO[].class));
            Integer[] childCatIds = JsonUtil.fromJson(nav.getChildCatIds(), Integer[].class);
            vo.setChildCatIds(childCatIds);
            Integer[] brandIds = JsonUtil.fromJson(nav.getBrandIds(), Integer[].class);
            vo.setBrandIds(brandIds);

            Category category = categoryMap.get(nav.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }

            if (childCatIds != null) {
                vo.setChildCatInfo(Arrays.stream(childCatIds)
                        .map(categoryMap::get)
                        .filter(Objects::nonNull)
                        .map(cat -> {
                            MobileCatNavVO.ChildCatInfo info = new MobileCatNavVO.ChildCatInfo();
                            info.setCategoryId(cat.getCategoryId());
                            info.setCategoryName(cat.getCategoryName());
                            info.setCategoryPic(cat.getCategoryPic());
                            return info;
                        }).toList());
            }

            if (brandIds != null) {
                vo.setBrandInfo(Arrays.stream(brandIds)
                        .map(brandMap::get)
                        .filter(Objects::nonNull)
                        .map(brand -> {
                            MobileCatNavVO.BrandInfo info = new MobileCatNavVO.BrandInfo();
                            info.setBrandId(brand.getBrandId());
                            info.setBrandName(brand.getBrandName());
                            info.setBrandLogo(brand.getBrandLogo());
                            info.setStatusText("");
                            return info;
                        }).toList());
            }

            return vo;
        }).toList();
    }

    @Override
    public boolean saveShareInfo(DecorateShareInfoDTO dto) {
        Decorate decorate = this.getById(dto.getDecorateId());
        if (decorate == null) {
            throw new com.tigshop.common.exception.GlobalException("装修页面不存在");
        }
        JSONObject json = new JSONObject();
        json.set("shareTitle", dto.getShareTitle());
        json.set("shareDesc", dto.getShareDesc());
        json.set("shareImage", dto.getShareImage());
        decorate.setShareInfo(json.toString());
        return this.updateById(decorate);
    }

    @Override
    public DecorateShareInfoVO getShareInfo(Integer decorateId) {
        Decorate decorate = this.getById(decorateId);
        if (decorate == null) {
            throw new com.tigshop.common.exception.GlobalException("装修页面不存在");
        }
        DecorateShareInfoVO vo = DecorateShareInfoVO.builder().decorateId(decorateId).build();
        if (StrUtil.isNotBlank(decorate.getShareInfo())) {
            JSONObject json = JSONUtil.parseObj(decorate.getShareInfo());
            vo.setShareTitle(json.getStr("shareTitle"));
            vo.setShareDesc(json.getStr("shareDesc"));
            vo.setShareImage(json.getStr("shareImage"));
        }
        return vo;
    }
}
