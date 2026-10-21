// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.login.RegisterEmailDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.user.*;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.param.user.CheckModifyEmailDTO;
import com.tigshop.bean.param.user.ModifyEmailParam;
import com.tigshop.bean.param.user.ModifyMobileParam;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.bean.vo.finance.UserCreditDetailVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.bean.vo.user.*;
import com.tigshop.service.finance.CreditUserService;
import com.tigshop.service.user.UserRankConfigService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.video.GalleryVideoInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户中心
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/user"))
@Tag(name = "前台用户中心")
@Validated
public class UserController {
    @Resource
    UserService userService;

    @Resource
    UserRankService userRankService;

    @Resource
    UserRankConfigService userRankConfigService;

    @Resource
    GalleryVideoInfoService galleryVideoInfoService;

    @Resource
    private CreditUserService creditUserService;

    @GetMapping("/detail")
    @Operation(summary = "会员详情")
    public UserClientDetailVO detail() {
        return userService.clientDetail(getCurrentUserId());
    }

    @GetMapping("/creditDetail")
    @Operation(summary = "用户账期支付信息详情")
    public UserCreditDetailVO creditDetail() {
        return creditUserService.getUserCreditDetail(getCurrentUserId());
    }

    @PostMapping("/updateInformation")
    @Operation(summary = "更新会员信息")
    public void updateInformation(@RequestBody UpdateInformationDTO updateInformationDTO) {
        userService.updateInformation(updateInformationDTO);
    }

    @Operation(summary = "会员中心")
    @GetMapping("/memberCenter")
    public MemberCenterVO memberCenter() {
        return userService.memberCenter();
    }

    //TODO 小程序回调

    @PostMapping("/sendMobileCodeByModifyPassword")
    @Operation(summary = "发送手机验证码")
    public void sendMobileCodeByModifyPassword(@RequestBody RegisterSmsDTO dto) {
        dto.setEvent("modify_password");
        userService.sendMobileCodeByModifyPassword(dto);
    }

    @PostMapping("/checkModifyPasswordMobileCode")
    @Operation(summary = "校验手机验证码")
    public void checkModifyPasswordMobileCode(@RequestBody CheckModifyPasswordDTO checkModifyPasswordDTO) {
        userService.checkModifyPasswordMobileCode(checkModifyPasswordDTO);
    }

    @PostMapping("/sendMobileCodeByMobileValidate")
    @Operation(summary = "手机修改获取验证码")
    public void sendMobileCodeByMobileValidate(@RequestBody RegisterSmsDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setEvent("mobile_validate");
        userService.sendMobileCodeByMobileValidate(dto, userId);
    }

    @PostMapping("/sendEmailCodeByEmailValidate")
    @Operation(summary = "邮箱修改获取验证码")
    public void sendEmailCodeByEmailValidate(@RequestBody RegisterEmailDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setEvent("email_validate");
        userService.sendEmailCodeByEmailValidate(dto, userId);
    }

    @PostMapping("sendMobileCodeByModifyMobile")
    @Operation(summary = "手机修改新手机获取验证码")
    public void sendMobileCodeByModifyMobile(@RequestBody RegisterSmsDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setEvent("modify_mobile");
        userService.sendMobileCodeByModifyMobile(dto, userId);
    }

    @PostMapping("sendEmailCodeByModifyEmail")
    @Operation(summary = "邮箱修改新邮箱获取验证码")
    public void sendEmailCodeByModifyEmail(@RequestBody RegisterEmailDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setEvent("modify_email");
        userService.sendEmailCodeByModifyEmail(dto, userId);
    }

    @PostMapping("/modifyMobile")
    @Operation(summary = "修改手机号")
    public void modifyMobile(@RequestBody @Validated ModifyMobileParam param) {
        userService.modifyMobile(param);
    }

    @PostMapping("/modifyEmail")
    @Operation(summary = "修改邮箱")
    public void modifyEmail(@RequestBody @Validated ModifyEmailParam param) {
        userService.modifyEmail(param);
    }

    @PostMapping("/mobileValidate")
    @Operation(summary = "手机验证")
    public void mobileValidate(@RequestBody CheckModifyPasswordDTO checkModifyPasswordDTO) {
        userService.mobileValidate(checkModifyPasswordDTO);
    }

    @PostMapping("emailValidateNew")
    @Operation(summary = "邮箱验证")
    public void emailValidateNew(@RequestBody CheckModifyEmailDTO checkModifyEmailDTO) {
        userService.emailValidateNew(checkModifyEmailDTO);
    }

    @PostMapping("emailValidate")
    @Operation(summary = "邮箱验证")
    public void emailValidate(@RequestBody CheckEmailModifyDTO checkEmailModifyDTO) {
        userService.emailValidate(checkEmailModifyDTO);
    }

    @GetMapping("/historyProduct")
    @Operation(summary = "最近浏览")
    public List<ProductListResDTO> historyProduct() {
        return userService.historyProduct();
    }

    @PostMapping("/delHistoryProduct")
    @Operation(summary = "删除最近浏览")
    public void delHistoryProduct(@RequestBody DelHistoryProductDTO delHistoryProductDTO) {
        userService.delHistoryProduct(delHistoryProductDTO);
    }

    @PostMapping("/uploadImg")
    @Operation(summary = "上传图片")
    public GalleryPicUploadVO uploadImg(@RequestParam("file") MultipartFile file,@RequestParam(value = "hidden", required = false, defaultValue = "false") boolean isHidden) {
        return userService.uploadImg(file,isHidden);
    }

    @PostMapping("/uploadVideo")
    @Operation(summary = "上传视频")
    public UploadVideoInfoVO uploadVideo(@RequestParam("file") MultipartFile file) {
        return galleryVideoInfoService.uploadVideo(file);
    }


    @PostMapping("modifyAvatar")
    @Operation(summary = "修改头像")
    public void modifyAvatar(@RequestParam("file") MultipartFile file) {
        userService.modifyAvatar(file);
    }

    @GetMapping("/collectionShop")
    @Operation(summary = "收藏店铺")
    public Page<CollectShopVO> collectionShop(CollectShopListDTO collectShopListDTO) {
        return userService.myCollectShop(collectShopListDTO);
    }

    @GetMapping("/levelList")
    @Operation(summary = "会员等级列表")
    public UserLevelListVO levelList() {
        List<UserRank> userRanks = userRankService.lambdaQuery().orderByAsc(UserRank::getMinGrowthPoints).list();
        List<UserRankListVO> item = userRanks.stream().map(rank -> {
            UserRankListVO result = new UserRankListVO();
            BeanUtil.copyProperties(rank, result, CopyOptions.create().setIgnoreProperties(UserRank::getRights));
            List<UserRankListVO.Right> rights = JSON.parseArray(rank.getRights(), UserRankListVO.Right.class);
            result.setRights(rights);
            return result;
        }).toList();
        return UserLevelListVO.builder()
                .item(item)
                .rankConfig(userRankConfigService.getUserRankConfigVO("rank_config"))
                .growConfig(userRankConfigService.getGrowConfig("grow_config"))
                .build();
    }

    @GetMapping("/levelInfo")
    @Operation(summary = "获取用户权益信息")
    public UserLevelInfoVO levelInfo(@RequestParam("rankId") Integer rankId) {
        return userRankService.getRankInfo(rankId);
    }

    @Operation(summary = "前台用户退出登录")
    @PostMapping("logout")
    public void logout() {
        userService.clientLogout();
    }

    @PostMapping("/close")
    @Operation(summary = "禁用用户")
    public void close() {
        Integer userId = getCurrentUserId();
        userService.closeUser(userId);
    }
}
