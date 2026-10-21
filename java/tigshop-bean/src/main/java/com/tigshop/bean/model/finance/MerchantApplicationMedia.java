package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 商户进件媒体元数据 model
 *
 * @author Tigshop团队
 */
@Data
@TableName("merchant_application_media")
@Schema(description = "商户进件媒体元数据")
public class MerchantApplicationMedia {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "进件媒体ID")
    private Long id;

    @Schema(description = "进件申请ID，0表示未绑定")
    private Long applicationId;

    @Schema(description = "上传后台账号ID")
    private Integer uploaderAdminId;

    @Schema(description = "申请主体类型:SHOP/VENDOR")
    private String applicantType;

    @Schema(description = "申请主体ID")
    private Long applicantId;

    @Schema(description = "媒体角色:0未绑定 1营业执照 2身份证人像面 3身份证国徽面")
    private Integer mediaRole;

    @Schema(description = "初始MediaID的SHA-256摘要")
    private String initialMediaIdHash;

    @Schema(description = "私有OSS随机对象键")
    private String objectName;

    @Schema(description = "文件扩展名")
    private String fileExt;

    @Schema(description = "文件MIME类型")
    private String contentType;

    @Schema(description = "文件大小，单位字节")
    private Long fileSize;

    @Schema(description = "状态:0上传中 1临时 2已绑定 3待删除 4已删除")
    private Integer status;

    @Schema(description = "删除重试次数")
    private Integer retryCount;

    @Schema(description = "下次补偿时间")
    private Date nextRetryTime;

    @Schema(description = "当前生命周期到期时间")
    private Date expireTime;

    @Schema(description = "脱敏错误分类")
    private String lastErrorCode;

    @TableLogic
    @Schema(description = "逻辑删除:0未删 非0已删")
    private Integer isDel;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date modifyTime;
}
