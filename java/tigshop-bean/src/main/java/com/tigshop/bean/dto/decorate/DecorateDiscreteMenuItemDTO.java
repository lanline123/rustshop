package com.tigshop.bean.dto.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 装修组件列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "装修组件列表参数")
public class DecorateDiscreteMenuItemDTO {

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "图片标题")
    private String picTitle;

    @Schema(description = "图片缩略图")
    private String picThumb;

    @Schema(description = "图片URL")
    private String picUrl;

    @Schema(description = "图片链接信息")
    private PicLink picLink;

    // 构造函数
    public DecorateDiscreteMenuItemDTO(String type, String picTitle, String picThumb, String picUrl, PicLink picLink) {
        this.type = type;
        this.picTitle = picTitle;
        this.picThumb = picThumb;
        this.picUrl = picUrl;
        this.picLink = picLink;
    }

    // 内部类：定义链接数据结构
    @Setter
    @Getter
    public static class PicLink {
        @Schema(description = "路径")
        private String path;

        @Schema(description = "标签")
        private String label;

        @Schema(description = "名称")
        private String name;

        @Schema(description = "链接")
        private String link;

        // 构造函数
        public PicLink(String path, String label, String name, String link) {
            this.path = path;
            this.label = label;
            this.name = name;
            this.link = link;
        }
    }
}
