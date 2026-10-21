package com.tigshop.bean.param.promotion.buyershow;

import com.tigshop.bean.model.promotion.BuyerShowVideo;
import com.tigshop.bean.model.setting.GalleryPic;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "更新买家秀参数")
public class BuyerShowUpdateParam {

    @NotNull(message = "买家秀id不能为空")
    @Schema(description = "自增ID，买家秀ID")
    private Integer buyerShowId;

    @NotEmpty(message = "关联商品不能为空")
    @Schema(description = "[json]活动范围相关信息")
    private List<Integer> rangeData;

    @NotBlank(message = "买家头像不能为空")
    @Schema(description = "买家头像")
    private String buyerPhoto;

    @NotBlank(message = "买家名称不能为空")
    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "视频信息")
    private List<BuyerShowVideo> buyerShowVideoList;

    @Schema(description = "图片信息")
    private List<GalleryPic> imgList;

    @Schema(description = "所属分组")
    private List<Integer> groupData;

    @Schema(description = "排序")
    private Integer sortOrder;
}
