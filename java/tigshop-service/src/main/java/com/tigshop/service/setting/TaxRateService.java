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
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.dto.setting.TaxRateDTO;
import com.tigshop.bean.model.setting.TaxRate;
import com.tigshop.bean.query.setting.TaxRatePageQuery;
import com.tigshop.bean.vo.setting.TaxRateVO;

/**
 * 税率管理服务接口
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
public interface TaxRateService extends IService<TaxRate> {

    /**
     * 分页查询税率列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<TaxRateVO> list(TaxRatePageQuery query);

    /**
     * 根据ID获取税率详情
     *
     * @param taxRateId 税率ID
     * @return 税率详情
     */
    TaxRateVO detail(Integer taxRateId);

    /**
     * 创建税率
     *
     * @param dto 创建参数
     * @return 是否成功
     */
    boolean create(TaxRateDTO dto);

    /**
     * 更新税率
     *
     * @param dto 更新参数
     * @return 是否成功
     */
    boolean update(TaxRateDTO dto);

    /**
     * 删除税率
     *
     * @param taxRateId 税率ID
     * @return 是否成功
     */
    boolean del(Integer taxRateId);

    /**
     * 根据地区ID获取生效的税率
     *
     * @param regionId 地区ID
     * @return 税率
     */
    TaxRate getActiveByRegionId(Integer regionId);
}
