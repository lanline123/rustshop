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
import com.tigshop.bean.dto.promotion.SignInSettingCreateDTO;
import com.tigshop.bean.dto.promotion.SignInSettingListDTO;
import com.tigshop.bean.dto.promotion.SignInSettingUpdateDTO;
import com.tigshop.bean.model.promotion.SignInSetting;
import com.tigshop.bean.vo.promotion.SignInSettingVO;
import com.tigshop.service.common.BaseService;

/**
 * 积分签到服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SignInSettingService extends BaseService<SignInSetting> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<SignInSettingVO> list(SignInSettingListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    SignInSettingVO detail(Integer id);

    /**
     * 新增签到配置，天数按现有最大天数顺延。
     */
    boolean create(SignInSettingCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(SignInSettingUpdateDTO updateDTO);

}
