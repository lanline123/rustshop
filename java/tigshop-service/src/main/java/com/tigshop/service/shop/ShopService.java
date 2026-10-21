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
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.login.LoginChooseParam;
import com.tigshop.bean.param.vendor.ShopVendorSettingUpdateParam;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.query.shop.StoreDetailQuery;
import com.tigshop.bean.query.shop.StorePageQuery;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.o2o.StoreDetailVO;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.bean.vo.shop.ClientShopDetailVO;
import com.tigshop.bean.vo.shop.ClientShopVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.shop.StaffShowVO;
import com.tigshop.bean.vo.vendor.ShopVendorSettingVO;
import com.tigshop.service.common.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShopService extends BaseService<Shop> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ShopVO> list(ShopListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ShopVO detail(Integer id);

    /**
     * 详情，并可限定返回当前登录管理员在该店铺的绑定记录
     *
     * @param id 店铺主键
     * @param onlyCurrentAdmin 是否只查询当前登录管理员的店铺绑定
     * @return ItemVO
     */
    ShopVO detail(Integer id, boolean onlyCurrentAdmin);

    /**
     * 详情（含评分：商品评分 productScore、计算后的评星 shopStar），与前台 clientDetail 保持一致
     *
     * @param id 主键
     * @return ShopVO
     */
    ShopVO detailWithScore(Integer id);

    /**
     * 创建
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ShopCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ShopUpdateDTO updateDTO);

    /**
     * 汇总店铺资金
     * @param shopId 店铺id
     * @return BigDecimal
     */
    BigDecimal getSumShopMoney(Integer shopId);

    /**
     * 汇总店铺冻结资金
     * @param shopId 店铺id
     * @return BigDecimal
     */
    BigDecimal getSumFrozenMoney(Integer shopId);

    /**
     * 获取店铺列表
     * @param keyword 查询条件
     * @return List
     */
    List<Shop> getShopByShopTitle(String keyword);

    /**
     * 前端店铺列表
     * @param listDTO 查询参数
     * @return ListResVO
     */

    Page<ClientShopVO> clientList(ShopListDTO listDTO);

    /**
     * 前端店铺详情
     * @param id 店铺id
     * @return item
     */
    ClientShopDetailVO clientDetail(Integer id);

    /**
     * 根据id集合获取店铺列表
     * @param ids id集合
     * @return ShopList
     */
    List<Shop> getListByIds(List<Integer> ids);

    @Transactional
    Shop updateShop(Integer id, Shop shopData, boolean isAdd);

    /**
     * 检测店铺状态
     * @param shopId 店铺id
     * @param productName 商品名称
     * @return bool
     */
    Boolean checkShopStatus(Integer shopId,String productName);

    /**
     * 获取店铺列表
     * @param dto 参数
     * @return List
     */
    List<Shop> getShopList(ShopListDTO dto);

    /**
     * 获取员工展示信息
     * @param shopId 店铺id
     * @return StaffShowVO
     */
    StaffShowVO staffShow(Integer shopId);

    /**
     * 更新店铺信息
     * @param dto 参数
     */
    Shop updateInfo(ShopUpdateInfoDTO dto);

    /**
     * 店铺设置
     * @param shopSettingDTO
     */
    void setting(ShopSettingDTO shopSettingDTO);

    /**
     * 选择登录平台
     * @param loginChooseParam
     */
    void choose(LoginChooseParam loginChooseParam);

    /**
     * 检测权限并返回权限列表
     * @param adminId 管理员id
     * @param id 店铺id
     * @param adminType 管理员类型
     * @return
     */
    List<String> checkAndReturnAuthList(Integer adminId, String id, String adminType);

    boolean checkIsUsing(Integer adminId, String id, String adminType);

    /**
     * 获取供应商设置
     * @return
     */
    ShopVendorSettingVO getVendorSetting();

    /**
     * 更新供应商设置
     * @param param
     */
    void updateVendorSetting(ShopVendorSettingUpdateParam param);

    /**
     * 获取门店关联自提点
     * @param query
     * @return
     */
    Page<PickupListVO> pickupList(PickupPageQuery query);


    /**
     * 获取门店列表
     * @return
     */
    Page<StoreListVO> storeList(StorePageQuery storePageQuery);

    /**
     * 获取门店详情
     * @param query
     * @return
     */
    StoreDetailVO storeDetail(StoreDetailQuery query);

    /**
     * 根据商品id获取门店列表
     * @param productId 商品id
     * @return  List
     */
    List<StoreListVO> getStoreListByProductId(Integer productId);

    /**
     * 获取自提点详情
     * @param shopId 店铺id
     * @return PickupListVO
     */
    PickupListVO pickupDetail(Integer shopId);

    /**
     * 获取所有门店列表
     * @return List<Shop>
     */
    List<Shop> getAllShops();

    /**
     * 获取所有门店ID列表（仅门店类型）
     * @return List<Integer>
     */
    List<Integer> getAllStoreIds();

    /**
     * 删除店铺
     * @param operateDTO
     * @return
     */
    boolean delShop(OperateDTO operateDTO);
}
