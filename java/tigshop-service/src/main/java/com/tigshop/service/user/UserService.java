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
import com.tigshop.bean.dto.login.*;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.user.*;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.login.BindWechatParam;
import com.tigshop.bean.param.login.CheckEmailParam;
import com.tigshop.bean.param.login.RegisterParam;
import com.tigshop.bean.param.user.*;
import com.tigshop.bean.query.user.UserFundDetailPageQuery;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.bean.vo.login.LoginWechatEventVO;
import com.tigshop.bean.vo.user.*;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 */
public interface UserService extends BaseService<User> {

    /**
     * 获取用户列表
     */
    Page<UserDTO> list(UserListDTO dto);

    /**
     * 获取用户详细信息
     *
     * @param id 用户ID，用于查询特定用户的详细信息
     * @return 用户数据传输对象，包含用户详细信息
     */
    UserDTO detail(Integer id);

    /**
     * 获取用户详细信息和用户等级信息
     *
     * @param id 用户ID，用于查询特定用户的详细信息和等级信息
     * @return 包含用户数据传输对象和用户等级数据传输对象的响应
     */
    UserItemVO getUserAndUserRank(Integer id);

    /**
     * 创建新用户
     *
     * @param user 用户数据传输对象，包含创建新用户所需的信息
     * @return 创建操作是否成功的布尔值
     */
    User create(UserDTO user);

    /**
     * 更新用户信息
     *
     * @param user 用户数据传输对象，包含更新用户信息所需的信息
     * @return 更新操作是否成功的布尔值
     */
    boolean update(UserParam user);

    /**
     * 获取所有用户的余额总和
     *
     * @return 所有用户的余额总和
     */
    BigDecimal getUserBalanceTotal();

    /**
     * 通过会员名称模糊查询用户
     * @param keyword 查询字段
     * @return List<User>
     */
    List<User> getUserByUsernameLike(String keyword);

    /**
     * 通过email 手机号 会员名称模糊查询
     * @param keyword 查询字段
     * @return List<User>
     */
    List<User> getUserByUserInfoLike(String keyword);


    /**
     * 服务器校验
     */
    void wechatServer(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 生成唯一的随机用户名（8位字母数字组合），若已存在则重新生成
     */
    String generateUniqueUsername();

    /**
     * 通过用户名查询用户
     * @param username 用户名
     * @return User
     */
    User getUserByUsername(String username);

    /**
     * 通过手机号查询用户
     * @param mobile 手机号
     * @return User
     */
    User getUserByMobile(String mobile);

    /**
     * 通过手机号批量查询用户（去重，避免逐个 getUserByMobile 的 N+1 查询）。
     *
     * @param mobiles 手机号集合
     * @return 手机号 -> User 映射（不存在的手机号不会出现在结果中）
     */
    java.util.Map<String, User> getUsersByMobiles(java.util.Collection<String> mobiles);

    /**
     * 检查用户公司权限
     */
    void checkUserCompanyAuth();

    /**
     * 获取用户推荐人id
     * @param currentUserId 当前用户id
     * @return Integer
     */
    Integer getUserReferrerId(Integer currentUserId);

    /**
     * 减少积分
     * @param usePoint 积分
     * @param currentUserId 当前用户id
     * @param desc 备注
     */
    void decPoints(Integer usePoint, Integer currentUserId, String desc);


    /**
     * 增加积分
     * @param point 积分
     * @param currentUserId 当前用户id
     * @param desc 备注
     */
    void incPoints(Integer point, Integer currentUserId, String desc);

    /**
     * 减少余额
     * @param useBalance 余额
     * @param currentUserId 当前用户id
     * @param desc 备注
     */
    void decBalance(BigDecimal useBalance, Integer currentUserId, String desc);

    /**
     * 获取用户详情
     *
     * @return UserClientDetailVO
     */
    UserClientDetailVO clientDetail(Integer userId);

    /**
     * 更新会员信息
     * @param updateInformationDTO 更新会员信息
     * @return boolean
     */
    boolean updateInformation(UpdateInformationDTO updateInformationDTO);

    /**
     * 会员中心
     */
    MemberCenterVO memberCenter();

    /**
     * 发送修改密码验证码
     * @return boolean
     */
    void sendMobileCodeByModifyPassword(RegisterSmsDTO dto);

    /**
     * 校验修改密码验证码
     * @param checkModifyPasswordDTO 校验修改密码验证码
     * @return boolean
     */
    boolean checkModifyPasswordMobileCode(CheckModifyPasswordDTO checkModifyPasswordDTO);

    /**
     * 修改手机号
     * @param modifyMobileParam 修改手机号
     */
    void modifyMobile(ModifyMobileParam modifyMobileParam);

    /**
     * 修改邮箱
     * @param param
     */
    void modifyEmail(ModifyEmailParam param);

    /**
     * 获取浏览记录
     * @return List<ProductListResDTO>
     */
    List<ProductListResDTO> historyProduct();

    /**
     * 手机验证
     * @param checkModifyPasswordDTO 密码修改DTO
     * @return boolean
     */
    boolean mobileValidate( CheckModifyPasswordDTO checkModifyPasswordDTO);

    /**
     * 邮箱验证
     * @param checkModifyEmailDTO
     * @return
     */
    boolean emailValidateNew(CheckModifyEmailDTO checkModifyEmailDTO);

    /**
     * 邮箱验证
     * @param checkEmailModifyDTO 邮箱验证DTO
     * @return boolean
     */
    boolean emailValidate(CheckEmailModifyDTO checkEmailModifyDTO);

    /**
     * 删除浏览记录
     * @param delHistoryProductDTO 删除浏览记录
     * @return boolean
     */
    boolean delHistoryProduct(DelHistoryProductDTO delHistoryProductDTO);

    /**
     * 上传图片
     * @param file 图片
     * @return GalleryPicUploadVO
     */
    GalleryPicUploadVO uploadImg( MultipartFile file);

    /**
     * 上传图片
     * @param file 图片
     * @param isHidden 是否隐藏
     * @return GalleryPicUploadVO
     */
    GalleryPicUploadVO uploadImg(MultipartFile file, boolean isHidden);

    /**
     * 修改头像
     * @param file 图片
     * @return boolean
     */
    boolean modifyAvatar(MultipartFile file);

    /**
     * 收藏店铺
     *
     * @param collectShopListDTO collectShopListDTO
     * @return CollectShopVO
     */
    Page<CollectShopVO> myCollectShop(CollectShopListDTO collectShopListDTO);

    /**
     * 注册
     */
    String register(RegisterParam param);

    /**
     * 手机号验证码登录时没有账号则自动创建并登录
     * @param mobile
     */
    void loginByPhoneAndRegister(String mobile);

    /**
     * 校验手机号
     * @param dto 校验手机号
     * @return String
     */
    String checkMobile(CheckMobileDTO dto);

    /**
     * 检验邮箱
     * @param param
     * @return
     */
    String checkEmail(@Valid CheckEmailParam param);

    /**
     * 修改密码
     * @param dto 修改密码
     * @return boolean
     */
    boolean modifyPassword(ForgetPasswordDTO dto);

    /**
     * 用户资金管理
     * @param dto 用户资金管理
     */
    void fundManagement(UserFundManagementDTO dto);

    /**
     * 手机修改获取验证码
     * @param dto 参数
     * @param userId 用户ID
     * @return boolean
     */
    void sendMobileCodeByMobileValidate( RegisterSmsDTO dto, Integer userId);

    /**
     * 邮箱修改获取验证码
     * @param dto 邮箱修改获取验证码
     * @param userId 用户ID
     * @return boolean
     */
    void sendEmailCodeByEmailValidate(RegisterEmailDTO dto, Integer userId);

    /**
     * 手机修改新手机获取验证码
     * @param dto 参数
     * @param userId 用户ID
     * @return boolean
     */
    void sendMobileCodeByModifyMobile( RegisterSmsDTO dto, Integer userId);

    /**
     * 邮箱修改获取验证码
     * @param dto
     * @param userId
     */
    void sendEmailCodeByModifyEmail(RegisterEmailDTO dto, Integer userId);

    /**
     * 绑定手机
     * @param dto 绑定手机号信息
     * @return String
     */
    String bindMobile(@Valid BindMobileDTO dto);

    /**
     * 微信事件
     */
    LoginWechatEventVO wechatEvent(@Valid WechatEventDTO dto);

    /**
     * 用户资金明细
     */
    Page<?> userFundDetail(UserFundDetailPageQuery pageQuery);

    /**
     * 获取用户手机号
     */
    String getMobile(LoginGetMobileParam param);

    /**
     * 获取用户openid
     */
    void updateUserOpenId(LoginUpdateUserOpenIdParam param);

    /**
     * 通过微信code获得微信用户信息
     */
    WxLoginInfoByCodeVO getWxLoginInfoByCode(String code) throws WxErrorException;

    /**
     * 前台用户退出登录
     */
    void clientLogout();

    /**
     * 管理员退出登录
     */
    void adminLogout();

    /**
     * app注销用户
     */
    void closeUser(int userId);

    /**
     * 绑定微信公众号
     */
    void bindWechat(BindWechatParam param);

    /**
     * 解绑微信公众号
     */
    void unbindWechat();

    /**
     * 下载会员导入模板
     * @param response HttpServletResponse
     */
    void downloadUserImportTemplate(HttpServletResponse response);

    /**
     * 批量导入会员
     * @param file 上传的Excel文件
     * @return 导入成功的会员列表
     */
    List<UserImportSuccessListVO> batchImportUser(MultipartFile file);


    /**
     * 获取会员导入失败的数据
     * @param file 上传的Excel文件
     * @param response HttpServletResponse
     */
    void getUserImportErrorData(MultipartFile file, HttpServletResponse response);

    /**
     * 设置管理员备注和标记
     * @param param 设置管理员备注参数
     */
    void setAdminNote(SetAdminNoteParam param);

    String getUserAvatar(String avatar);
}
