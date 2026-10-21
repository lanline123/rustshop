package com.tigshop.mapper.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.content.Article;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章
 *
 * @author kidd
 * @create 2025/7/4
 */
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 分页查询
     */
    Page<ArticleVO> page(@Param("page") Page<ArticleVO> page, @Param("pageQuery") ArticlePageQuery pageQuery);
}

