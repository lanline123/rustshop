package com.tigshop.bean.vo.promotion.buyershow;

import com.tigshop.bean.model.promotion.BuyerShowGallery;
import com.tigshop.bean.model.promotion.BuyerShowVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀详情信息")
public class BuyerShowVO {

    @Schema(description = "自增ID，买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "关联商品数据Id")
    private List<Integer> rangeData;

    @Schema(description = "买家头像")
    private String buyerPhoto;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "图片信息")
    private List<BuyerShowGallery> imgList;

    @Schema(description = "视频信息")
    private List<BuyerShowVideo> videoList;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "分组Id信息")
    private List<Integer> groupIdList;

    @Schema(description = "分组名称信息")
    private List<String> groupNameList;

    @Schema(description = "排序")
    private Integer sortOrder;
}
