package com.tigshop.bean.dto.promotion;

import com.tigshop.bean.model.promotion.BuyerShowGallery;
import com.tigshop.bean.model.promotion.BuyerShowLike;
import com.tigshop.bean.model.promotion.BuyerShowVideo;
import com.tigshop.bean.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Tigshop团队
 */
@Data
@Schema(description = "查询参数")
@Builder
public class BuyerShowDTO {

    private Map<Integer, List<BuyerShowGallery>> buyerShowGalleryMap;

    private Map<Integer, List<BuyerShowVideo>> buyerShowVideoMap;

    private List<BuyerShowLike> buyerShowLikeList;

    private Map<Integer, User> userMap;
}
