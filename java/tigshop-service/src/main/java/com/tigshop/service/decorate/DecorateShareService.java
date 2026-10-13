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

package com.tigshop.service.decorate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.decorate.DecorateShareCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateShareListDTO;
import com.tigshop.bean.dto.decorate.DecorateShareUpdateDTO;
import com.tigshop.bean.model.decorate.DecorateShare;
import com.tigshop.bean.vo.decorate.DecorateShareVO;
import com.tigshop.service.common.BaseService;

/**
 * 售后申请表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface DecorateShareService extends BaseService<DecorateShare> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<DecorateShareVO> list(DecorateShareListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    DecorateShareVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(DecorateShareCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(DecorateShareUpdateDTO updateDTO);

    /**
     * 分享模板
     * @param decorateId
     * @return ItemVO
     */
    DecorateShareVO share(int decorateId);

    /**
     * 导入分享模板
     * @param url 分享链接
     * @return boolean
     */
    boolean importUrl(String url);

    /**
     * 获取分享信息
     * @param sn
     * @param token
     * @return
     */
    Object getInfoBySn(String sn, String token);

}
