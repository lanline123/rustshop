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

package com.tigshop.service.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.merchant.MerchantCreateDTO;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.dto.merchant.MerchantUpdateDTO;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.service.common.BaseService;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商家表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface MerchantService extends BaseService<Merchant> {
    /** 入驻营业执照专用上传：添加公示水印，不进入相册。 */
    String uploadLicensePic(MultipartFile file);

    /** 获取已通过入驻或平台确认的商户营业执照，没有有效资料返回空字符串。 */
    String getApprovedLicensePic(Integer merchantId);

    /**
     * 列表
     */
    Page<MerchantVO> list(MerchantListPageQuery pageQuery);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    MerchantVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(MerchantCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(MerchantUpdateDTO updateDTO);

    /**
     * 根据id获取MerchantVO
     * @param id 商家id
     * @return MerchantVO
     */
    MerchantVO getMerchantVO(Integer id);

    /**
     * 申请入驻协议
     * @return String
     */
    String applyShopAgreement();

    /**
     * 删除
     *
     * @param id 主键
     */
    void delMerchant(Integer id);
}
