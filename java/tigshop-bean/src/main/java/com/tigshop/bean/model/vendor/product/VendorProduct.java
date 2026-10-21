package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.bean.enums.vendor.VendorProductAuditStateEnum;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "供应商商品")
@EqualsAndHashCode(callSuper = true)
@TableName(value ="vendor_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProduct extends BasePO implements Serializable {

    @Schema(description = "自建供应商关联的来源商品ID")
    private Integer sourceProductId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品品牌ID")
    private Integer productBrandId;

    @Schema(description = "商品类目ID")
    private Integer productCategoryId;

    @Schema(description = "商品编码生成类型；1-系统生成，2-手动输入")
    private Integer productSnGenerateType;

    @Schema(description = "商品描述")
    private String productBrief;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @Schema(description = "规格类型，1-单规格，2-多规格")
    private Integer skuType;

    @Schema(description = "商品详情")
    private String productDesc;

    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

     @Schema(description = "商品实际图片")
     private String picUrl;

    @Schema(description = "商品缩略图片")
    private String picThumb;

    @Schema(description = "商品原始图片")
    private String picOriginal;

    @Schema(description = "高清图")
    private String picLarge;

    @Schema(description = "商品是否回收；0-否，1-是")
    private Integer isRecycle;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public boolean isSales() {
        return Constants.YES.equals(this.productState);
    }

    public boolean isDel() {
        return Constants.YES.equals(super.getIsDel());
    }

    public boolean isNotSales() {
        return Constants.NO.equals(this.productState);
    }
    public boolean isPassAudit() {
        return VendorProductAuditStateEnum.PASS_AUDIT.getCode() == this.auditState;
    }

}
