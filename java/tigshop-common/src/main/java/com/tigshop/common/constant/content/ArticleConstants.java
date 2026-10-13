package com.tigshop.common.constant.content;

import java.util.Map;

/**
 * 文章常量
 *
 * @author kidd
 * @create 2025/7/4
 */
public class ArticleConstants {
    public static final String ARTICLE_TITLE_IS_NULL = "文章名称不能为空";
    public static final String ARTICLE_LENGTH_ERROR = "文章名称长度为1-30位";

    public static final String ARTICLE_NOT_FOUND = "文章不存在";
    public static final String ARTICLE_TITLE_EXISTS = "文章名称已存在";

    public static final String ARTICLE_TARGET_CAT_IS_NULL = "目标不能为空";

    //ARTICLE_TYPE_MAP 1为普通文章，2为帮助文章
    public static final Map<Integer, String> ARTICLE_TYPE_MAP = Map.of(
            1, "普通文章",
            2, "帮助文章"
    );

}
