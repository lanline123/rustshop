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

package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserCompanyAuditDTO;
import com.tigshop.bean.dto.user.UserCompanyCreateDTO;
import com.tigshop.bean.dto.user.UserCompanyListDTO;
import com.tigshop.bean.model.user.UserCompany;
import com.tigshop.bean.vo.user.UserCompanyApplyVO;
import com.tigshop.bean.vo.user.UserCompanyItemVO;
import com.tigshop.bean.vo.user.UserCompanyVO;
import com.tigshop.service.common.BaseService;

/**
 * 会员企业认证服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserCompanyService extends BaseService<UserCompany> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<UserCompanyVO> list(UserCompanyListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    UserCompanyItemVO detail(Integer id);

    /**
     * 前台查询当前会员自己的实名认证详情
     *
     * @param id 认证记录主键
     * @param userId 当前会员 ID
     * @return ItemVO
     */
    UserCompanyItemVO clientDetail(Integer id, Integer userId);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(UserCompanyCreateDTO createDTO);

    /**
     * 审核
     * @param auditDTO 审核参数
     * @return boolean
     */
    boolean audit(UserCompanyAuditDTO auditDTO);

    /**
     * 前台提交企业认证
     *
     * @param createDTO 企业认证参数
     * @return ItemVO
     */
    UserCompanyVO companyAudit(UserCompanyCreateDTO createDTO);

    /**
     * 我的申请详情
     *
     * @return ItemVO
     */
    UserCompanyApplyVO myApply();
}
