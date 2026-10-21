// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.im.ImConversationPageQuery;
import com.tigshop.bean.vo.im.ConsultHistoryListVO;
import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.im.ImConversation;
import org.apache.ibatis.annotations.Param;

/**
 * 客服会话映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface ImConversationMapper extends BaseMapper<ImConversation> {

    /**
     * 获取历史会话
     */
    Page<ConsultHistoryListVO> consultHistory(@Param("page") Page<ConsultHistoryListVO> page, @Param("pageQuery") ImConversationPageQuery pageQuery);

}
