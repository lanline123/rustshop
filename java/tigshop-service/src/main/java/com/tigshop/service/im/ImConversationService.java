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

package com.tigshop.service.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.*;
import com.tigshop.bean.model.im.ImConversation;
import com.tigshop.bean.query.im.ImConversationPageQuery;
import com.tigshop.bean.vo.im.*;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 客服会话服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ImConversationService extends BaseService<ImConversation> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ImConversationVO> list(ImConversationPageQuery listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ImConversationVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    ImConversation create(ImConversationCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ImConversationUpdateDTO updateDTO);

    /**
     * 搜索用户
     * @param keyword 关键字
     * @param servantId id
     * @return List
     */
    List<ImSearchUserVO> getUserSearch(String keyword, Integer servantId);

    /**
     * 搜索会话
     * @param keyword 关键字
     * @param servantId id
     * @return List
     */
    List<ImSearchConversationVO> getConversationSearch(String keyword, Integer servantId);

    /**
     * 转接
     * @param dto 转接参数
     * @return boolean
     */
    boolean transfer(TransferDTO dto);

    /**
     * 待接入的客户列表
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<WaitServantListVO> waitServantList(WaitServantListDTO listDTO);

    /**
     *  删除
     * @param dto 参数
     * @return boolean
     */
    boolean delete(DelDTO dto);

    /**
     * 获取历史会话
     */
    Page<ConsultHistoryListVO> consultHistory(ImConversationPageQuery pageQuery);

    /**
     * 保存会话备注
     * @param dto 参数
     * @return boolean
     */
    boolean saveRemark(SaveRemarkDTO dto);

    /**
     * 前台搜索店铺和会话
     * @param keyword 关键字
     * @param userId 用户ID
     * @return Map
     */
    Map<String, Object> frontSearch(String keyword, Integer userId);
}
