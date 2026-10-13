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

package com.tigshop.adminapi.controller.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import com.tigshop.bean.param.product.ProductEditParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.query.product.ProductSkuListQuery;
import com.tigshop.bean.query.product.ProductSkuPageQuery;
import com.tigshop.bean.vo.product.ProductConfigVO;
import com.tigshop.bean.vo.product.ProductDescImageVO;
import com.tigshop.bean.vo.product.ProductPostVO;
import com.tigshop.bean.vo.product.ProductPromoteVO;
import com.tigshop.bean.vo.product.ProductShippingTplListVO;
import com.tigshop.bean.vo.product.ProductSkuVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.product.ProductEsService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.GrouponPromotionItemService;
import com.tigshop.service.setting.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.CheckFieldConstants.PRODUCT_FIELDS;

/**
 * 商品控制器
 *
 * @author Tigshop团队
 * @create 2024年11月21日 14:30
 */
@RestController
@RequestMapping("/adminapi/product/product")
@Tag(name = "商品")
@PreAuthorize("@pms.hasPermission('productManage')")
@Validated
public class ProductController {
    @Resource
    private ProductService productService;

    @Resource
    private AdminLogService adminLogService;

    @Resource
    private ConfigService configService;
    @Resource
    private TigshopProperties tigshopProperties;

    @Resource
    private ProductEsService productEsService;

    @Resource
    private GrouponPromotionItemService grouponPromotionItemService;

    @Resource
    private ProductSkuService productSkuService;

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    @PreAuthorize("@pms.hasPermission('decorate','productManage')")
    public Page<ProductListResDTO> list(ProductListDTO listDTO) {
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            listDTO.setShopId(headerShopId);
        }
        if(Boolean.TRUE.equals(listDTO.getOnlyShowMe())){
            //仅仅展示自己
            listDTO.setShopId(headerShopId);
        }
        return productService.list(listDTO);
    }

    @GetMapping("/getWaitingCheckedCount")
    @Operation(summary = "待审核商品数量")
    public Long getWaitingCheckedCount() {
        return productService.getWaitingCheckedCount();
    }

    @GetMapping("/detail")
    @Operation(summary = "商品详情")
    public ProductDetailDTO detail(@RequestParam Integer id) {
        return productService.detail(id);
    }

    @GetMapping("/descImages")
    @Operation(summary = "商品描述图片列表（供配置描述图本地化 dataType=7）")
    public List<ProductDescImageVO> descImages(@RequestParam Integer productId) {
        return productService.getDescImages(productId);
    }

    @GetMapping("/skuList")
    @Operation(summary = "分页获取商品SKU列表（对应商品详情 productList，默认每页50条，支持关键字/库存过滤与排序）")
    public Page<ProductSkuDTO> skuList(ProductSkuListQuery query) {
        return productSkuService.getProductSkusPage(query);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String create(@Valid @RequestBody ProductSaveParam param) {
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            param.setShopId(headerShopId);
        }
        //新增日志
        adminLogService.createByLogInfo(StrUtil.format("创建商品:{}", param.getProductName()));
        return productService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新商品")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String update(@RequestBody @Validated ProductEditParam param) {
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            param.setShopId(headerShopId);
        }
        if (tigshopProperties.getIsO2o() == 0 && Objects.equals(param.getProductStatus(), Constants.NO)){
            grouponPromotionItemService.remove(Wrappers.lambdaQuery(GrouponPromotionItem.class).eq(GrouponPromotionItem::getProductId, param.getProductId()));
        }
        boolean update = productService.update(param);
        if (update){
            adminLogService.createByLogInfo(StrUtil.format("更新商品:{}", param.getProductName()));
            Integer shopId = HeaderUtils.getShopId();
            if (shopId != null && shopId > 0) {
                String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
                if ("1".equals(shopProductNeedCheck)) {
                    return "商品已修改，等待管理员审核";
                }
            }
            return "商品管理更新成功";
        }else{
            return "商品管理更新失败";
        }
    }

    @Operation(summary = "商品配置")
    @GetMapping("/config")
    public ProductConfigVO config(@RequestParam(required = false) Integer shopId) {
        return productService.config(shopId);
    }

    @GetMapping("shippingTplList")
    @Operation(summary = "运费模板")
    public List<ProductShippingTplListVO> shippingTplList() {
        return productService.getShippingTplListByShopId();
    }

    @PostMapping("getParticiple")
    @Operation(summary = "更新关键词")
    public String getParticiple(@RequestBody ProductKeyDTO dto) {
        return productService.getParticiple(dto);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String updateField(@RequestBody UpdateFieldProductDTO updateField) {
        return productService.updateFieldProduct(updateField, PRODUCT_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(description = "批量操作", summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public void batchOperation(@RequestBody @Validated ProductBatchOperationDTO dto) {
        productService.batchOperation(dto);
    }

    @PostMapping("/recycle")
    @Operation(description = "产品回收", summary = "产品回收")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public boolean recycle(@RequestBody @Validated ProductRecycleDTO dto) {
        return productService.recycle(dto);
    }

    @PostMapping("/copy")
    @Operation(description = "产品复制", summary = "产品复制")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String copy(@RequestBody @Validated ProductCopyDTO dto) {
        return productService.copy(dto);
    }

    @PostMapping("/audit")
    @Operation(description = "产品审核", summary = "产品审核")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public boolean audit(@RequestBody @Validated ProductAuditDTO dto) {
        return productService.audit(dto);
    }

    @PostMapping("/auditAgain")
    @Operation(description = "产品再次审核", summary = "产品再次审核")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public boolean auditAgain(@RequestBody @Validated ProductAuditDTO dto) {
        return productService.auditAgain(dto.getId());
    }


    @Operation(description = "获取推广数据", summary = "获取推广数据")
    @GetMapping("/getPromoteData")
    public ProductPromoteVO getPromoteData(@RequestParam("productId") Integer productId) {
        return productService.getProductPromote(productId);
    }

    @Operation(description = "商品推广", summary = "商品推广（包含H5、PC、小程序）")
    @GetMapping("/post")
    public ProductPostVO post(@RequestParam("productId") Integer productId) {
        return productService.post(productId);
    }

    @PostMapping("/initAllToEs")
    @Operation(description = "初始化所有商品到ES", summary = "初始化所有商品到ES")
    public void initAllToEs() {
        productEsService.syncAllProductsStream(1000);
    }

    @GetMapping("/selectProductList")
    @Operation(summary = "sku商品列表")
    public Page<ProductSkuVO> selectProductList(ProductSkuPageQuery query){
        query.setShopId(HeaderUtils.getShopId());
        return productService.selectProductList(query);
    }

    @PostMapping("/saveShareInfo")
    @Operation(summary = "保存商品分享信息")
    public boolean saveShareInfo(@RequestBody @jakarta.validation.Valid com.tigshop.bean.dto.product.ProductShareInfoDTO dto) {
        return productService.saveShareInfo(dto);
    }

    @GetMapping("/shareInfo")
    @Operation(summary = "获取商品分享信息")
    public com.tigshop.bean.vo.product.ProductShareInfoVO shareInfo(@RequestParam("productId") Integer productId) {
        return productService.getShareInfo(productId);
    }

}
