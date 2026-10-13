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

package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.ECardGroupCreateDTO;
import com.tigshop.bean.dto.product.ECardGroupListDTO;
import com.tigshop.bean.dto.product.ECardGroupUpdateDTO;
import com.tigshop.bean.model.product.ECardGroup;
import com.tigshop.bean.vo.product.ECardGroupVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 电子卡券组服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ECardGroupService extends BaseService<ECardGroup> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ECardGroupVO> list(ECardGroupListDTO listDTO, HttpServletResponse response);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ECardGroupVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ECardGroupCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ECardGroupUpdateDTO updateDTO);

    /**
     * 批量导入
     * @param groupId 组id
     * @param file 文件
     * @return boolean
     */
    boolean batchImport(Integer groupId, MultipartFile file);

    @Override
    boolean updateField(UpdateFieldDTO updateFieldDTO, String[] allowFields);
}
