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

package com.tigshop.service.lang;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.lang.LocalesDTO;
import com.tigshop.bean.dto.lang.LocalesListDTO;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.vo.lang.LocalesCurrencyVO;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 语言地区服务接口
 * @author Tigshop团队
 * @create 2024年12月30日 16:38
 */
public interface LocalesService extends BaseService<Locales> {
    /**
     * 语言地区列表
     * @param basePage 分页参数
     * @return 语言地区列表
     */
    Page<LocalesVO> list(LocalesListDTO basePage);

    /**
     * 语言地区详情
     * @param id 语言地区id
     * @return 语言地区详情
     */
    LocalesVO detail(Integer id);

    /**
     * 创建语言地区
     * @param dto 语言地区参数
     * @return 是否创建成功
     */
    boolean create(LocalesDTO dto);

    /**
     * 修改语言地区
     * @param dto 语言地区参数
     * @return 是否修改成功
     */
    boolean update(LocalesDTO dto);

    /**
     * 获取语言内容
     * @return List
     */
    List<LocalesVO> getLocalesLimit3();

    /**
     * 获取语言内容（VO）
     * @return item
     */
    LocalesVO getLocalesVO(Locales item);

    /**
     * 获取语言内容
     * @return List
     */
    List<LocalesCurrencyVO> getLocales();

    /**
     * 按域名获取已启用的语言
     * @param domain 访问域名，不含协议与端口
     * @return 语言地区，未配置该域名时返回 null
     */
    Locales getByDomain(String domain);
}
