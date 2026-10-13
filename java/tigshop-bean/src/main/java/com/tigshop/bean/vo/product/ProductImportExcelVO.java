package com.tigshop.bean.vo.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.common.utils.SpecImportUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * 商品批量导入/导出 Excel 行（写出），列与 {@link ProductImportExcelPrueVO} 一致。
 *
 * @author kidd
 * @since 2025/3/26 16:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class ProductImportExcelVO {

    @ExcelProperty("商品编码")
    @Schema(description = "商品编码")
    private String productSn;

    @ExcelProperty("商品名称")
    @Schema(description = "商品名称")
    private String productName;

    @ExcelProperty("商品图片")
    @Schema(description = "商品图片")
    private String picUrl;

    @ExcelProperty("商品分类")
    @Schema(description = "商品分类")
    private String categoryName;

    @ExcelProperty("配送方式")
    @Schema(description = "配送方式")
    private String shippingType;

    @ExcelProperty("快递运费模板")
    @Schema(description = "快递运费模板")
    private String shippingTpl;

    @ExcelProperty("是否上架（默认1）")
    @Schema(description = "是否上架")
    private String productStatus;

    @ExcelProperty("商品规格")
    @Schema(description = "商品规格")
    private String specName;

    @ExcelProperty("商品规格图")
    @Schema(description = "商品规格图")
    private String specPicUrl;

    @ExcelProperty("规格编码")
    @Schema(description = "规格编码")
    private String skuSn;

    @ExcelProperty("规格条码")
    @Schema(description = "规格条码")
    private String skuTsn;

    @ExcelProperty("商品详情图")
    @Schema(description = "商品详情图")
    private String descPicUrl;

    @ExcelProperty("商品售价")
    @Schema(description = "商品售价")
    private String productPrice;

    @ExcelProperty("库存")
    @Schema(description = "库存")
    private String productStock;

    @ExcelProperty("市场价")
    @Schema(description = "市场价")
    private String marketPrice;

    @ExcelProperty("商品条码")
    @Schema(description = "商品条码")
    private String productTsn;

    @ExcelProperty("搜索关键词")
    @Schema(description = "搜索关键词")
    private String keywords;

    @ExcelProperty("商品描述")
    @Schema(description = "商品描述")
    private String productBrief;

    @ExcelProperty("成本价")
    @Schema(description = "成本价")
    private String costPrice;

    @ExcelIgnore
    @Schema(description = "分类ID")
    private Integer categoryId;

    @ExcelIgnore
    @Schema(description = "店铺ID")
    private Integer shopId;

    /**
     * 导出：单规格（无 SKU 行）sku 传 null；多规格每行一个 SKU。
     */
    public static ProductImportExcelVO valueOf(Product p, String catPath, ProductSku sku) {
        ProductImportExcelVO.ProductImportExcelVOBuilder b = ProductImportExcelVO.builder()
                .productName(p.getProductName())
                .picUrl(StrUtil.blankToDefault(p.getPicUrl(), p.getPicOriginal()))
                .categoryName(catPath)
                .shippingType(buildShippingTypeText(p))
                .shippingTpl(buildShippingTplText(p))
                .productStatus(p.getProductStatus() != null ? p.getProductStatus().toString() : "1")
                .keywords(p.getKeywords())
                .productBrief(p.getProductBrief())
                .productSn(p.getProductSn())
                .descPicUrl(buildDescPicUrlText(p.getProductDesc()))
                .productTsn(StrUtil.blankToDefault(p.getProductTsn(), ""))
                .shopId(p.getShopId());
        if (sku != null) {
            b.specName(SpecImportUtils.toImportSpecText(sku.getSkuValue()))
                    .skuSn(StrUtil.blankToDefault(sku.getSkuSn(), ""))
                    .productPrice(sku.getSkuPrice() != null ? sku.getSkuPrice().toPlainString() : "0")
                    .productStock(sku.getSkuStock() != null ? sku.getSkuStock().toString() : "0")
                    .marketPrice(sku.getMarketPrice() != null ? sku.getMarketPrice().stripTrailingZeros().toPlainString() : "")
                    .skuTsn(StrUtil.blankToDefault(sku.getSkuTsn(), ""))
                    .costPrice(sku.getCostPrice() != null ? sku.getCostPrice().stripTrailingZeros().toPlainString() : "");
        } else {
            b.specName("")
                    .skuSn("")
                    .productPrice(p.getProductPrice() != null ? p.getProductPrice().toPlainString() : "0")
                    .productStock(p.getProductStock() != null ? p.getProductStock().toString() : "0")
                    .marketPrice(p.getMarketPrice() != null ? p.getMarketPrice().stripTrailingZeros().toPlainString() : "")
                    .skuTsn("")
                    .costPrice(p.getCostPrice() != null ? p.getCostPrice().stripTrailingZeros().toPlainString() : "");
        }
        return b.build();
    }

    /**
     * 导出（列表查询 DTO，与库存导出等逻辑衔接）。
     */
    public static ProductImportExcelVO valueOfExport(Product p, String catPath, ProductSkuDTO sku) {
        ProductImportExcelVO.ProductImportExcelVOBuilder b = ProductImportExcelVO.builder()
                .productName(p.getProductName())
                .picUrl(StrUtil.blankToDefault(p.getPicUrl(), p.getPicOriginal()))
                .categoryName(catPath)
                .shippingType(buildShippingTypeText(p))
                .shippingTpl(buildShippingTplText(p))
                .productStatus(p.getProductStatus() != null ? p.getProductStatus().toString() : "1")
                .keywords(p.getKeywords())
                .productBrief(p.getProductBrief())
                .productSn(p.getProductSn())
                .descPicUrl(buildDescPicUrlText(p.getProductDesc()))
                .productTsn(StrUtil.blankToDefault(p.getProductTsn(), ""))
                .shopId(p.getShopId());
        if (sku != null) {
            String spec = resolveSpecText(sku);
            b.specName(spec)
                    .skuSn(StrUtil.blankToDefault(sku.getSkuSn(), ""))
                    .productPrice(StrUtil.blankToDefault(sku.getSkuPrice(), "0"))
                    .productStock(sku.getSkuStock() != null ? sku.getSkuStock().toString() : "0")
                    .marketPrice(sku.getMarketPrice() != null ? sku.getMarketPrice().stripTrailingZeros().toPlainString() : "")
                    .skuTsn(StrUtil.blankToDefault(sku.getSkuTsn(), ""))
                    .costPrice(sku.getCostPrice() != null ? sku.getCostPrice().stripTrailingZeros().toPlainString() : "");
        } else {
            b.specName("")
                    .skuSn("")
                    .productPrice(p.getProductPrice() != null ? p.getProductPrice().toPlainString() : "0")
                    .productStock(p.getProductStock() != null ? p.getProductStock().toString() : "0")
                    .marketPrice(p.getMarketPrice() != null ? p.getMarketPrice().stripTrailingZeros().toPlainString() : "")
                    .skuTsn("")
                    .costPrice(p.getCostPrice() != null ? p.getCostPrice().stripTrailingZeros().toPlainString() : "");
        }
        return b.build();
    }

    /**
     * 把 {@code product_desc} HTML 反解析为图片 URL 列表，用 {@code ;} 拼回 Excel 可识别格式。
     * 仅取 type=pic 段，忽略文本段；用于「导出后再次导入」的闭环。
     */
    private static String buildDescPicUrlText(String productDesc) {
        if (StrUtil.isBlank(productDesc)) {
            return "";
        }
        java.util.List<ProductDescDTO> arr = ProductDescDTO.getProductDescArr(productDesc);
        if (CollUtil.isEmpty(arr)) {
            return "";
        }
        return arr.stream()
                .filter(v -> "pic".equals(v.getType()) && StrUtil.isNotBlank(v.getPic()))
                .map(ProductDescDTO::getPic)
                .collect(Collectors.joining(";"));
    }

    /**
     * 导出规格文本，多组之间用导入侧认的「；」而非落库串的「|」。
     * <p>
     * 导入的分组解析（{@code SpecImportUtils.parseSpecPairs}）只认 {@code ; ； , ，}，
     * 直接写落库串会让「导出→再导入」把多组规格解析成一组，规格值被污染。
     */
    private static String resolveSpecText(ProductSkuDTO sku) {
        if (StrUtil.isNotBlank(sku.getSkuValue())) {
            return SpecImportUtils.toImportSpecText(sku.getSkuValue());
        }
        if (CollUtil.isEmpty(sku.getSkuData())) {
            return "";
        }
        return sku.getSkuData().stream()
                .map(s -> s.getName() + ":" + s.getValue())
                .collect(Collectors.joining("；"));
    }

    private static String buildShippingTypeText(Product p) {
        StringBuilder sb = new StringBuilder();
        if (Integer.valueOf(1).equals(p.getIsLogistics())) {
            sb.append("快递发货");
        }
        if (Integer.valueOf(1).equals(p.getIsShopDelivery())) {
            if (!sb.isEmpty()) {
                sb.append("；");
            }
            sb.append("同城配送");
        }
        if (Integer.valueOf(1).equals(p.getIsShopPickup())) {
            if (!sb.isEmpty()) {
                sb.append("；");
            }
            sb.append("到店自提");
        }
        return sb.toString();
    }

    private static String buildShippingTplText(Product p) {
        if (Integer.valueOf(1).equals(p.getFixedShippingType())) {
            BigDecimal fee = p.getFixedShippingFee();
            return fee != null ? fee.stripTrailingZeros().toPlainString() : "0";
        }
        if (Integer.valueOf(2).equals(p.getFixedShippingType()) && p.getShippingTplId() != null) {
            return "【运费模板】" + p.getShippingTplId();
        }
        if (p.getFixedShippingFee() != null && p.getFixedShippingFee().compareTo(BigDecimal.ZERO) > 0
                && p.getShippingTplId() == null) {
            return p.getFixedShippingFee().stripTrailingZeros().toPlainString();
        }
        return "";
    }
}
