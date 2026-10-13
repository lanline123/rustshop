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
import com.tigshop.bean.dto.setting.LogisticsCompanyCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyUpdateDTO;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.setting.LogisticsCompanyVO;
import com.tigshop.bean.vo.setting.SyncLogisticsCompanyResultVO;
import com.tigshop.service.common.BaseService;

/**
 * 配送方式配置信息服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface LogisticsCompanyService extends BaseService<LogisticsCompany> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<LogisticsCompanyVO> list(LogisticsCompanyListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    LogisticsCompanyVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(LogisticsCompanyCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(LogisticsCompanyUpdateDTO updateDTO);

    /**
     * 根据id获取物流公司信息
     * @param id 主键
     * @return LogisticsCompany
     */
    LogisticsCompany getLogisticsCompanyById(Integer id);

    /**
     * 同步快递鸟物流公司列表到本系统
     *
     * @param overwriteName 若系统已存在相同 logisticsCode，是否覆盖 logisticsName
     * @param defaultIsEnabled 新增记录默认是否启用：1启用，0禁用
     * @return 同步结果统计
     */
    SyncLogisticsCompanyResultVO syncKdnLogisticsCompanies(boolean overwriteName, int defaultIsEnabled);

    /**
     * 同步快递100常见物流公司到本系统（按 kuaidi100_code 幂等 upsert）
     *
     * @param overwriteName 若已存在相同 kuaidi100_code，是否覆盖 logisticsName
     * @param defaultIsEnabled 新增记录默认是否启用：1启用，0禁用
     * @return 同步结果统计
     */
    SyncLogisticsCompanyResultVO syncKuaidi100LogisticsCompanies(boolean overwriteName, int defaultIsEnabled);
}
