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

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 商品描述
 *
 * @author Tigshop团队
 * @create 2024年11月26日 17:20
 */
@Data
@Schema(description = "商品描述")
public class ProductDescDTO {
    @Schema(description = "类型")
    private String type;

    @Schema(description = "图片")
    @JsonTranslate(dataType = 7)
    private String pic;

    @Schema(description = "内容")
    @JsonTranslate(dataType = 9)
    private String html;

    @Schema(description = "视频封面")
    @JsonTranslate(dataType = 7)
    private String cover;

    @Schema(description = "视频地址")
    private String videoUrl;

    /**
     * 获取商品详情描述
     *
     * @param productDescList 商品详情描述列表
     * @return 商品详情描述字符串
     */
    public static String getProductDesc(List<ProductDescDTO> productDescList) {
        if (productDescList == null || productDescList.isEmpty()) {
            return "";
        }

        // 生成结果列表
        List<String> result = productDescList.stream()
                .map(item -> {
                    String type = item.getType();
                    if ("pic".equals(type)) {
                        String pic = item.getPic();
                        return "<div class=\"desc-pic-item\"><img src=\"" + pic + "\"></div>";
                    } else if ("video".equals(type)) {
                        String cover = item.getCover() != null ? item.getCover() : "";
                        String videoUrl = item.getVideoUrl() != null ? item.getVideoUrl() : "";
                        return "<div class=\"desc-video-item\" data-cover=\"" + cover + "\" data-video=\"" + videoUrl + "\"></div>";
                    } else if ("text".equals(type)) {
                        return item.getHtml();
                    }
                    return "";
                })
                // 过滤空字符串
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 使用分隔符拼接结果
        return String.join("<div data-division=1></div>", result);
    }

    /**
     * 获取商品详情中的图片地址
     *
     * @param html 商品详情HTML
     * @return 图片地址列表
     */
    public static List<ProductDescDTO> getProductDescArr(String html) {
        if (StrUtil.isEmpty(html)) {
            return List.of();
        }

        List<ProductDescDTO> result = new ArrayList<>();
        String[] arr = html.split("<div data-division=1></div>");

        for (String value : arr) {
            result.add(parseHtml(value));
        }

        return result;
    }

    /**
     * 解析HTML字符串，获取图片和文本内容
     *
     * @param value HTML字符串
     * @return 包含图片和文本内容的对象
     */
    private static ProductDescDTO parseHtml(String value) {
        ProductDescDTO dto = new ProductDescDTO();
        dto.setHtml(value);

        if (value.contains("desc-video-item")) {
            dto.setType("video");
            dto.setCover(extractAttribute(value, "data-cover"));
            dto.setVideoUrl(extractAttribute(value, "data-video"));
        } else if (value.contains("desc-pic-item")) {
            dto.setType("pic");
            dto.setPic(extractImageSrc(value));
        } else {
            dto.setType("text");
        }

        return dto;
    }

    /**
     * 提取图片地址
     *
     * @param html HTML字符串
     * @return 图片地址
     */
    private static String extractImageSrc(String html) {
        Matcher matcher = Pattern.compile("<img.*?src=[\"'](.*?)[\"'].*?>").matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String extractAttribute(String html, String attr) {
        Matcher matcher = Pattern.compile(attr + "=[\"'](.*?)[\"']").matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }
}
