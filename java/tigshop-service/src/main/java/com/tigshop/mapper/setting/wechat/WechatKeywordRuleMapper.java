// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.setting.wechat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.setting.wechat.WechatKeywordRule;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 公众号关键字回复-规则主表映射（对应需求 §8.2 wechat_keyword_rule）
 *
 * <p>提供两个自定义查询：
 * <ul>
 *   <li>{@link #pageWithCount(Page, String, Integer)}：列表分页，
 *       通过子查询聚合每条规则的关键字数 / 回复数，供管理端列表直接渲染。</li>
 *   <li>{@link #countRules()}：规则总数，供新增前按需求 §2.4 做 200 条上限校验。</li>
 * </ul>
 * 其余 CRUD 复用 {@link BaseMapper}。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Mapper
public interface WechatKeywordRuleMapper extends BaseMapper<WechatKeywordRule> {

    /**
     * 分页查询关键字规则列表，行内同时聚合关键字数与回复数。
     *
     * <p>分页 LIMIT 由 MyBatis-Plus 分页插件自动拼接，XML 中不要手写。
     *
     * @param page     分页参数（records 将被回填）
     * @param ruleName 规则名称模糊查询，空值忽略
     * @param enabled  启用状态过滤：0=停用 1=启用，null 表示不过滤
     * @return 分页结果，每行字段见 {@link KeywordRuleVO}
     */
    Page<KeywordRuleVO> pageWithCount(Page<KeywordRuleVO> page,
                                      @Param("ruleName") String ruleName,
                                      @Param("enabled") Integer enabled);

    /**
     * 规则总数（全量，不区分启用状态），用于需求 §2.4 新增前做 200 条上限校验。
     *
     * @return 规则总数
     */
    @Select("SELECT COUNT(1) FROM wechat_keyword_rule")
    int countRules();
}
