package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.vendor.*;
import com.tigshop.bean.vo.shop.StaffShowVO;
import com.tigshop.bean.vo.vendor.VendorAccountFundVO;
import com.tigshop.bean.vo.vendor.VendorDetailConfigVO;
import com.tigshop.bean.vo.vendor.VendorDetailVO;
import com.tigshop.bean.vo.vendor.VendorVO;
import com.tigshop.service.common.BaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

/**
 * @author Admin
 * @description 针对表【vendor(供应商表)】的数据库操作Service
 * @createDate 2025-07-04 10:14:10
 */
public interface VendorService extends BaseService<Vendor> {

    /**
     * 列表
     *
     * @param pageQuery 查询参数
     * @return page列表
     */
    Page<VendorVO> list(VendorListPageParam pageQuery);

    /**
     * 创建供应商
     *
     * @param createParam
     */
    void create(VendorCreateParam createParam);

    @Transactional
    AdminUser getAdmin(VendorCreateParam createParam);

    @Transactional
    AdminUser getAdminUser(User user, VendorCreateParam.VendorData vendorData);

    /**
     * 更新供应商
     *
     * @param updateParam
     */
    void update(VendorUpdateParam updateParam);

    /**
     * 详情
     *
     * @param id
     * @return
     */
    VendorDetailVO detail(Integer id);

    /**
     * 当前供应商配置
     *
     * @return
     */
    VendorDetailConfigVO currentDetail();

    /**
     * 设置
     *
     * @param param
     */
    void setting(VendorConfigParam param);

    /**
     * 供应商员工展示信息
     *
     * @return
     */
    StaffShowVO staffShow();

    /**
     * 更新供应商信息
     *
     * @param param
     */
    void updateInfo(VendorUpdateInfoParam param);

    BigDecimal getSumVendorMoney();

    BigDecimal getSumFrozenMoney();

    Page<VendorAccountFundVO> listFund(VendorListPageParam param);

    /**
     * 删除供应商
     *
     * @param operateDTO
     * @return
     */
    boolean delVendor(OperateDTO operateDTO);
}
