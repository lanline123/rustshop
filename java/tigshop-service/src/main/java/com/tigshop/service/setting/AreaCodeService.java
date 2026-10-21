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

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.AreaCodeListDTO;
import com.tigshop.bean.model.setting.AreaCode;
import com.tigshop.bean.param.settings.areacode.AreaCodeEditParam;
import com.tigshop.bean.param.settings.areacode.AreaCodeSaveParam;
import com.tigshop.bean.vo.setting.AreaCodeVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 区号管理服务接口
 *
 * @author Tigshop团队
 * @create 2024年12月30日 16:40
 */
public interface AreaCodeService extends BaseService<AreaCode> {
    /**
     * 区号管理列表
     *
     * @param dto 查询参数
     * @return Page
     */
    Page<AreaCodeVO> list(AreaCodeListDTO dto);

    /**
     * 创建区号
     */
    void create(AreaCodeSaveParam param);

    /**
     * 更新区号
     */
    void update(AreaCodeEditParam param);

    /**
     * 更新字段
     */
    boolean updateField(UpdateFieldDTO updateField, String[] allowFields);

    /**
     * 区号详细信息
     *
     * @param id 查询id
     * @return AreaCodeVO
     */
    AreaCodeVO detail(Integer id);

    /**
     * 获取移动端区号
     *
     * @return List
     */
    List<AreaCodeVO> mobileAreaCode();
}
