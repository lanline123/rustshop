// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product;

//**---------------------------------------------------------------------+
//** 控制器文件 -- 品牌
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.BrandAuditDTO;
import com.tigshop.bean.dto.product.BrandDTO;
import com.tigshop.bean.dto.product.BrandListDTO;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.vo.product.BrandSearchVO;
import com.tigshop.bean.vo.product.BrandVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 商品品牌
 *
 * @author Jayce
 * @create 2024-09-30 15:16:06
 */
public interface BrandService extends BaseService<Brand> {
    /**
     * 商品品牌列表
     *
     * @param brand 查询参数
     * @return ListResVO
     */
    Page<BrandVO> list(BrandListDTO brand);

    /**
     * 商品品牌详情
     *
     * @param id 商品品牌ID
     * @return ItemVO
     */
    BrandDTO detail(Integer id);

    /**
     * 创建商品品牌
     *
     * @param brand 商品品牌
     * @return boolean
     */
    String create(BrandDTO brand);


    /**
     * 更新商品品牌
     *
     * @param brand 商品品牌
     * @return boolean
     */
    String update(BrandDTO brand);

    /**
     * 查询商品品牌
     *
     * @return BrandSearchVO
     */
    BrandSearchVO search();

    List<String> getBrandNameByIds(List<Integer> brandIds);


    void updateFirstWord();

    /**
     * 获取品牌主键，根据品牌名称
     * @param brandName 品牌名称
     * @param isAutoBrand 是否自动创建
     */
    Integer getBrandId(String brandName, Integer isAutoBrand);


    /**
     * 审核品牌
     * @param dto 审核参数
     * @return String
     */
    String audit(BrandAuditDTO dto);


    /**
     * 待审核品牌数量
     * @return Long
     */
    Long auditWaitNum();
}
