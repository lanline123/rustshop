package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.user.ShopMember;
import com.tigshop.bean.param.user.ShopMemberAddParam;
import com.tigshop.bean.param.user.ShopMemberQueryPage;
import com.tigshop.bean.param.user.ShopMemberShopQueryPage;
import com.tigshop.bean.vo.user.ShopMemberListVO;
import com.tigshop.bean.vo.user.ShopMemberShopListVO;
import com.tigshop.bean.vo.user.UserImportSuccessListVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 店铺会员绑定服务。
 *
 * <p>开关开启后，用户在某店铺/门店下单支付即成为该店会员，按 (shopId, userId) 记账
 * （累计消费金额/订单数/入会时间/最近消费时间）。
 */
public interface ShopMemberService extends BaseService<ShopMember> {

    /**
     * 订单支付成功后按订单 shopId 记账（幂等 upsert）。
     *
     * <p>前置：总开关开启 且 order.shopId>0 且 order.userId>0；否则不记账。
     * 金额取 paidAmount（为空兜底 totalAmount）。首次入会插入，已存在则原子累加。
     * 调用方需在事务提交后调用并自行 try/catch，本方法异常不得阻断支付主流程。
     */
    void recordOnPaid(Order order);

    /**
     * 会员列表。
     * <p>shopId 解析：query.shopId 非空（平台端指定店铺）优先，否则取 {@code HeaderUtils.getShopId()}（店铺端）。
     * 脱敏：店铺端（getShopId()>0）mobile 恒脱敏；冻结态额外脱敏 nickname。
     */
    Page<ShopMemberListVO> memberList(ShopMemberQueryPage query);

    /**
     * 会员详情（只读）。
     * @param shopId 平台端指定；店铺端传 null 取当前登录店铺
     */
    ShopMemberListVO memberDetail(Integer shopId, Integer userId);

    /**
     * 平台端店铺列表（含店铺会员数），只读。
     */
    Page<ShopMemberShopListVO> shopList(ShopMemberShopQueryPage query);

    /**
     * 店铺端新增单个客户（CR-20260708-1530）。
     *
     * <p>守卫：总开关冻结 + allowAdd + 仅店铺端（getShopId()&gt;0）。
     * 逻辑：按手机号查 user，存在复用/不存在新建，再 upsert shop_member 绑定；已是本店会员则抛异常。
     *
     * @return 新增/绑定后的 userId
     */
    Integer addMember(ShopMemberAddParam param);

    /**
     * 店铺端批量导入客户（Excel，CR-20260708-1530）。
     *
     * <p>守卫：总开关冻结 + allowImport + 仅店铺端。范式：校验全部→有错整体拒绝→无错逐行复用/建 user + 绑定。
     *
     * @return 导入成功列表
     */
    List<UserImportSuccessListVO> importMembers(MultipartFile file);

    /**
     * 导出导入失败行 Excel（CR-20260708-1530）。
     */
    void getImportErrorData(MultipartFile file, HttpServletResponse response);

    /**
     * 下载店铺会员导入模板（字段仅到生日，不含会员等级/余额/积分/成长积分/备注）。
     */
    void downloadImportTemplate(HttpServletResponse response);
}
