package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.finance.MerchantApplication;
import com.tigshop.bean.param.finance.MerchantApplicationSubmitParam;
import com.tigshop.bean.query.finance.MerchantApplicationPageQuery;
import com.tigshop.bean.vo.finance.MerchantApplicationVO;
import com.tigshop.service.common.BaseService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商户进件 Service
 *
 * @author Tigshop团队
 */
public interface MerchantApplicationService extends BaseService<MerchantApplication> {

    /**
     * 进件申请分页列表（平台端审核列表）。
     *
     * <p>平台账号可查看全部；店铺/供应商账号即便持有权限，也只能看到本主体的申请。
     */
    Page<MerchantApplicationVO> listPage(MerchantApplicationPageQuery query);

    /**
     * 平台端直接提交进件（进件管理：选择店铺直接提交微信进件）
     */
    MerchantApplicationVO submit(MerchantApplicationSubmitParam param);

    /**
     * 店铺/供应商端提交进件申请（保存为待审核，不直接提交微信）
     */
    MerchantApplicationVO apply(MerchantApplicationSubmitParam param);

    /**
     * 店铺/供应商端获取当前登录主体最新一条进件申请（驱动步骤页），无则返回 null
     */
    MerchantApplicationVO current();

    /** 当前主体指定渠道的进件账户；优先返回最新成功记录。 */
    MerchantApplicationVO current(String payChannel);

    /**
     * 进件申请详情。
     *
     * <p>平台账号可查看任意申请；店铺/供应商账号只能查看本主体的申请，越权与不存在统一按「不存在」处理。
     */
    MerchantApplicationVO detail(Long id);

    /**
     * 读取进件证件原图（营业执照 / 身份证正反面）。
     *
     * <p>先按当前登录主体校验对该申请单的读取权限，再以申请单登记的主体去媒体表取图，
     * 任一环节不满足都返回 {@code null}，不区分「不存在」与「无权」。
     *
     * @param id       进件申请 id
     * @param roleCode 证件角色：1 营业执照 2 身份证人像面 3 身份证国徽面
     */
    MerchantApplicationMediaService.MediaContent loadMediaContent(Long id, Integer roleCode);

    /**
     * 平台端审核通过：提交微信进件
     */
    void approve(Long id);

    /**
     * 平台端审核拒绝
     */
    void reject(Long id, String rejectReason);

    /**
     * 刷新进件状态（轮询微信侧）
     */
    void refresh(Long id);

    /**
     * 上传进件媒体文件，返回 media_id / file_id
     *
     * @param payChannel 渠道 WECHAT/HUIFU，空则默认 WECHAT
     */
    String uploadMedia(MultipartFile file, String payChannel);
}
