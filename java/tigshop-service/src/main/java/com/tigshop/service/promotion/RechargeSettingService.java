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

package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.RechargeSettingCreateDTO;
import com.tigshop.bean.dto.promotion.RechargeSettingListDTO;
import com.tigshop.bean.dto.promotion.RechargeSettingUpdateDTO;
import com.tigshop.bean.model.promotion.RechargeSetting;
import com.tigshop.bean.query.promotion.recharge.RechargeSettingPageQuery;
import com.tigshop.bean.vo.promotion.RechargeSettingVO;
import com.tigshop.service.common.BaseService;

/**
 * 充值余额服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface RechargeSettingService extends BaseService<RechargeSetting> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<RechargeSettingVO> list(RechargeSettingListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    RechargeSettingVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(RechargeSettingCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(RechargeSettingUpdateDTO updateDTO);

    /**
     * 充值余额列表
     */
    Page<RechargeSettingVO> settingPage(RechargeSettingPageQuery pageQuery);
}
