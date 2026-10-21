// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.home;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.ConfigCustomerDTO;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.enums.decorate.DecoratePageType;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.model.setting.FriendLinks;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
import com.tigshop.bean.vo.o2o.O2oDecorateVO;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.SeckillListVO;
import com.tigshop.common.constant.decorate.InitDecorateDataConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.decorate.DecorateDiscreteService;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.FriendLinksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页控制器
 *
 * @author Tigshop团队
 * @create 2025年01月20日 15:34
 */
@RestController
@RequestMapping("/api/home/home")
@Tag(name = "首页")
public class HomeController {
    @Resource
    private FriendLinksService friendLinksService;

    @Resource
    private DecorateDiscreteService decorateDiscreteService;

    @Resource
    private DecorateService decorateService;

    @Resource
    private ConfigService configService;

    @Resource
    private SeckillService seckillService;

    @Resource
    private CouponService couponService;


    @GetMapping("/friendLinks")
    @Operation(summary = "友情链接")
    public List<FriendLinks> friendLinks() {
        return friendLinksService.list();
    }

    @GetMapping("/mobileNav")
    @Operation(summary = "移动端导航")
    public Object mobileNav(@RequestParam(value = "decorateSn", defaultValue = "mobileNav", required = false) String decorateSn,
                            @RequestParam(value = "isNew", required = false, defaultValue = "0") Integer isNew) {
        if (Integer.valueOf(1).equals(isNew)) {
            Decorate decorate = decorateService.getOne(new LambdaQueryWrapper<Decorate>()
                    .eq(Decorate::getShopId, 0)
                    .eq(Decorate::getDecorateType, DecorateType.H5.getCode())
                    .eq(Decorate::getPageType, 7)
                    .eq(Decorate::getIsHome, 1)
                    .eq(Decorate::getStatus, 1)
                    .last(" limit 1"));
            String data = (decorate != null && decorate.getData() != null && !decorate.getData().isEmpty())
                    ? decorate.getData()
                    : InitDecorateDataConstants.NAV_H5_DECORATE_DATA;
            return JSON.parseObject(data);
        }
        return decorateDiscreteService.detail(decorateSn);
    }

    @GetMapping("/index")
    @Operation(summary = "首页")
    public DecorateModuleListVO index(@RequestParam(value = "previewId", required = false) Integer previewId) {
        DecorateModuleListVO decorate;
        if (previewId != null && previewId > 0) {
            // 预览 pageType = 1 是首页
            decorate = decorateService.getAppPreviewDecorate(previewId,1);
        } else {
            // 获取首页发布版
            decorate = decorateService.getAppHomeDecorate();
        }
        return decorate;
    }

    @GetMapping("/pcIndex")
    @Operation(summary = "PC端首页")
    public DecorateModuleListVO pcIndex(@RequestParam(value = "previewId", required = false) Integer previewId,
                                        @RequestParam(value = "id", required = false) Integer id,
                                        @RequestParam(value = "decorateType",required = false) Integer decorateType) {
        DecorateModuleListVO decorate;
        if ((previewId != null && previewId > 0) || (id != null && id > 0)) {
            // 预览
            if(DecorateType.CUSTOM.getCode().equals(decorateType)){
                decorate = decorateService.customDecorate(previewId);
            }else{
                // pageType = 1 是首页
                decorate = decorateService.getPcPreviewDecorate(previewId, id,1);
            }
        } else {
            // 获取首页发布版
            decorate = decorateService.getPcHomeDecorate();
        }
        return decorate;
    }

    @GetMapping("/pcLogin")
    @Operation(summary = "PC登录页装修")
    public DecorateModuleListVO pcLogin(@RequestParam(value = "previewId", required = false) Integer previewId,
                                        @RequestParam(value = "id", required = false) Integer id) {
        if (previewId != null && previewId > 0) {
            // pageType = 5 是 PC 登录页装修
            return decorateService.getPcPreviewDecorate(previewId, id, 5);
        }
        return decorateService.getPcLoginDecorate();
    }

    @GetMapping("/pcSearch")
    @Operation(summary = "PC搜索页装修")
    public DecorateModuleListVO pcSearch(@RequestParam(value = "previewId", required = false) Integer previewId) {
        return decorateService.getPcSearchDecorate(previewId);
    }

    @GetMapping("/pcHeader")
    @Operation(summary = "PC头部装修配置")
    public Map<String, Object> pcHeader(@RequestParam(value = "previewId", required = false) Integer previewId) {
        Map<String, Object> result = new HashMap<>();
        result.put("pcHeader", decorateService.getPcHeader(previewId));
        return result;
    }

    @GetMapping("/o2oIndex")
    @Operation(summary = "门店首页装修")
    public O2oDecorateVO o2oIndex(@RequestParam(value = "shopId",required = true) Integer shopId) {
        List<Decorate> list = decorateService.list(new LambdaQueryWrapper<Decorate>().eq(Decorate::getShopId, shopId)
                .eq(Decorate::getStatus, 1)
                .eq(Decorate::getIsHome, 1)
                .eq(Decorate::getDecorateType, DecorateType.H5.getCode())
                .eq(Decorate::getPageType, Decorate.resolveO2oHomePageType(shopId))
                .last(" limit 1"));
        if(list.isEmpty()){
            Decorate defaultO2OH5 = Decorate.createDefaultO2OH5(shopId);
            decorateService.save(defaultO2OH5);
            O2oDecorateVO o2oDecorateVO = new O2oDecorateVO();
            o2oDecorateVO.setData(JSON.parse(defaultO2OH5.getData()));
            return o2oDecorateVO;
        }else{
            O2oDecorateVO o2oDecorateVO = new O2oDecorateVO();
            try{
                o2oDecorateVO.setData(JSON.parse(list.get(0).getData()));
            }catch (Exception e){
                throw new GlobalException("数据格式错误");
            }
            return o2oDecorateVO;
        }
    }

    @GetMapping("/getCustomerServiceConfig")
    @Operation(summary = "获取前台客户配置")
    public ConfigCustomerDTO getConfigCustomer() {
        return configService.getConfigCustomer();
    }

    @GetMapping("/getSeckill")
    @Operation(summary = "获取秒杀")
    public Page<SeckillListVO> getSeckill(@RequestParam(value = "size", defaultValue = "15", required = false) Integer size,
                                          @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                          @RequestParam(value = "unStarted", defaultValue = "0", required = false) Integer unStarted) {
        return seckillService.getSeckillProductList(size, page, unStarted);
    }

    @GetMapping("/getCoupon")
    @Operation(summary = "首页优惠券")
    public List<CouponClientVO> getCoupon(CouponPageQuery pageQuery) {
        pageQuery.setIsShow(1);
        pageQuery.setValidDate(1);
        pageQuery.setReceiveFlag(1);
        return couponService.getList(pageQuery).getRecords();
    }

    @GetMapping("/memberDecorate")
    @Operation(summary = "个人中心")
    public Object memberDecorate(@RequestParam(value = "decorateSn", defaultValue = "memberDecorate") String decorateSn) {
        // 固定返回旧版离散装修；新旧版路由由前端按 useNewMemberCenter 判断，新版整页装修走 /h5Member
        return decorateDiscreteService.detail(decorateSn);
    }

    @GetMapping("/h5Category")
    @Operation(summary = "H5分类页装修")
    public DecorateModuleListVO h5Category(@RequestParam(value = "previewId", required = false) Integer previewId) {
        if (previewId != null && previewId > 0) {
            // pageType = 1 是 H5分类页装修
            return decorateService.getAppPreviewDecorate(previewId,4);
        }
        return decorateService.getDecorateModule(DecorateType.H5.getCode(), 4, true, 1);
    }

    @GetMapping("/h5ProductDetail")
    @Operation(summary = "H5商品详情页装修")
    public DecorateModuleListVO h5ProductDetail(@RequestParam(value = "previewId", required = false) Integer previewId) {
        if (previewId != null && previewId > 0) {
            // pageType = 8 是 H5商品详情页装修
            return decorateService.getAppPreviewDecorate(previewId, DecoratePageType.H5_PRODUCT_DETAIL.getCode());
        }
        return decorateService.getDecorateModule(DecorateType.H5.getCode(), DecoratePageType.H5_PRODUCT_DETAIL.getCode(), true, 1);
    }

    @GetMapping("/h5Member")
    @Operation(summary = "H5个人中心装修")
    public DecorateModuleListVO h5Member(@RequestParam(value = "previewId", required = false) Integer previewId) {
        if (previewId != null && previewId > 0) {
            // pageType = 2 是 H5个人中心装修
            return decorateService.getAppPreviewDecorate(previewId,2);
        }
        return decorateService.getDecorateModule(DecorateType.H5.getCode(), 2, true, 1);
    }

    @GetMapping("/h5Search")
    @Operation(summary = "移动端搜索页装修")
    public DecorateModuleListVO h5Search(@RequestParam(value = "previewId", required = false) Integer previewId) {
        // H5(移动) = DecorateType.H5，pageType = 6 = 搜索页
        return decorateService.getH5SearchDecorate(previewId);
    }

    @GetMapping("/mobileLogin")
    @Operation(summary = "移动端登录页装修")
    public DecorateModuleListVO mobileLogin(@RequestParam(value = "previewId", required = false) Integer previewId) {
        if (previewId != null && previewId > 0) {
            // pageType = 5 为登录页
            return decorateService.getAppPreviewDecorate(previewId, 5);
        }
        // H5(移动) = DecorateType.H5，pageType = 5 = 登录页，取已发布版
        return decorateService.getDecorateModule(DecorateType.H5.getCode(), 5, true, 1);
    }

    @GetMapping("/getRecommend")
    @Operation(summary = "首页")
    public Object getRecommend(@RequestParam(name = "decorateId", defaultValue = "0") int decorateId,
                               @RequestParam(name = "moduleIndex") String moduleIndex,
                               @RequestParam(name = "page", defaultValue = "1") int page,
                               @RequestParam(name = "previewId", defaultValue = "0") int previewId) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", 10);

        Object result = null;
        if (previewId > 0) {
            result = decorateService.getPreviewDecorateModuleData(decorateId, moduleIndex, params);
        } else {
            result = decorateService.getDecorateModuleData(decorateId, moduleIndex, params);
        }

        return result;
    }
}