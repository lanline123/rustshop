package com.tigshop.bean.param.salesman;

import com.tigshop.bean.dto.salesman.SalesmanMaterialUpdateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.salesman.SalesmanMaterialConstants.SALESMAN_MATERIAL_ID_NOT_NULL;

/**
 * 分销员分组更新参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "分销员分组参数")
public class SalesmanMaterialEditParam {
    @Schema(description = "分销员分组ID")
    @NotNull(message = SALESMAN_MATERIAL_ID_NOT_NULL)
    private Integer id;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "是否可用")
    private Integer isAvailable;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "图片列表")
    private List<SalesmanMaterialUpdateDTO.Pic> pics;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分享次数")
    private Integer shareNum;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "分销员分组图片")
    private String salesmanMaterialPic;

    @Schema(description = "分销员分组排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "分销员分组描述")
    private String salesmanMaterialDesc;

    @Data
    @Schema(description = "图片信息")
    public static class Pic {
        @Schema(description = "图片ID")
        private Integer picId;

        @Schema(description = "图片缩略图")
        private String picThumb;

        @Schema(description = "图片URL")
        private String picUrl;

        @Schema(description = "图片名称")
        private String picName;
    }
}
