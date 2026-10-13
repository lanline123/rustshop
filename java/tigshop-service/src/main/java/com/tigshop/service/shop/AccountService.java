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
import com.tigshop.bean.dto.shop.AccountCreateDTO;
import com.tigshop.bean.dto.shop.AccountListDTO;
import com.tigshop.bean.dto.shop.AccountUpdateDTO;
import com.tigshop.bean.model.shop.Account;
import com.tigshop.bean.vo.shop.AccountVO;
import com.tigshop.bean.vo.shop.ShopAccountLogVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
 * 店铺资金变化服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface AccountService extends BaseService<Account> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    AccountVO index(AccountListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    AccountVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(AccountCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(AccountUpdateDTO updateDTO);

    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ShopAccountLogVO> list(AccountListDTO listDTO);

    void refundWithDrawLog(Integer shopId, BigDecimal amount);

    void addWithDrawLog(Integer shopId, BigDecimal amount);

    void completeWithDrawLog(Integer shopId, BigDecimal amount);

    /** 记录已确认的线上资金事件；只写日志，不修改店铺线下余额，重复事件不重复入账。 */
    void recordOnlineChange(Integer shopId, String payChannel, String eventKey,
                            BigDecimal changeAmount, String changeReasons, Long addTime);
}
