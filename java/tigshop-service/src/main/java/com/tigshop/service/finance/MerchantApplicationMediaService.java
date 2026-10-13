package com.tigshop.service.finance;

import com.tigshop.common.exception.GlobalException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商户进件媒体 Service
 *
 * @author Tigshop团队
 */
public interface MerchantApplicationMediaService {

    String uploadMedia(MultipartFile file, Integer uploaderAdminId);

    List<String> normalizeSubmissionMedia(
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId,
            String licenseValue,
            String portraitValue,
            String nationalValue);

    boolean bindTempMedia(
            Long applicationId,
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId,
            String licenseMediaId,
            String portraitMediaId,
            String nationalMediaId);

    List<String> reuploadBoundMedia(Long applicationId);

    /**
     * 读取进件证件原图，供后台审核页预览。
     *
     * <p>证件是营业执照与身份证正反面，存放在私有存储、对象键随机，且提交快照里只保留渠道 MediaID。
     * 因此不返回可直接访问的地址，只以字节流交给受权限保护的接口输出，避免链接被转发后脱离鉴权。
     *
     * <p>调用方须先按当前登录主体确认对该申请单有读取权限（见 {@code MerchantApplicationService#loadMediaContent}），
     * 本方法再以申请单登记的主体作为查询条件复核媒体归属，防止只凭 applicationId 拉取他人证件。
     *
     * @param applicationId 进件申请 id
     * @param applicantType 申请主体类型（申请单上登记的 SHOP / VENDOR）
     * @param applicantId   申请主体 id（申请单上登记的店铺 / 供应商 id）
     * @param roleCode      媒体角色，见 {@code MerchantApplicationMediaRole}：1 营业执照 2 身份证人像面 3 身份证国徽面
     * @return 原图内容；媒体不存在、归属不符、已清理或角色非法时返回 {@code null}
     */
    MediaContent loadBoundMediaContent(
            Long applicationId, String applicantType, Long applicantId, Integer roleCode);

    /**
     * 进件证件原图及其 MIME 类型。
     */
    record MediaContent(byte[] content, String contentType) {
    }

    void extendBoundMediaExpiration(Long applicationId);

    void extendBoundMediaExpirationInCurrentTransaction(Long applicationId);

    void cleanupBoundMediaInNewTransaction(Long applicationId);

    class ReuploadFailedException extends GlobalException {

        public ReuploadFailedException() {
            super("进件媒体重传失败，请稍后重试");
        }
    }
}
