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

package com.tigshop.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.shop.ShopWithdrawSummaryVO;
import com.tigshop.bean.vo.shop.ShopWithdrawVO;
import com.tigshop.service.common.BaseService;

import java.util.Map;

/**
 * 商家账户表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShopWithdrawService extends BaseService<ShopWithdraw> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ShopWithdrawVO> list(ShopWithdrawListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ShopWithdrawVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ShopWithdrawCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ShopWithdrawUpdateDTO updateDTO);

    /**
     * 获取状态映射
     *
     * @return code -> 描述
     */
    StatusListVO<Map<Integer, String>> getStatusList();


    /**
     * 审核提现
     *
     * @param dto 审核参数
     */
    void audit(ShopWithdrawAuditDTO dto);

    /**
     * 上传打款凭证
     *
     * @param dto 上传参数
     */
    void uploadPayVoucher(ShopWithdrawPayVoucherDTO dto);

    /**
     * 店铺提现汇总列表
     *
     * @param listDTO 查询参数
     * @return Page<ShopWithdrawSummaryVO>
     */
    Page<ShopWithdrawSummaryVO> summaryList(ShopWithdrawSummaryListDTO listDTO);

    /**
     * 双余额查询（线下 shopMoney + 微信子商户可用余额 wechatBalance）
     *
     * @return {shopMoney, wechatBalance}
     */
    Map<String, Object> getDualBalance();

    /** 渠道确定结果落账；CAS 与线上流水同事务，重复通知不重复记账。 */
    void applyOnlineWithdrawResult(Integer id, com.tigshop.bean.dto.finance.profitsharing.WithdrawResult result);
}
