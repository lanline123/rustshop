// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.wholesale;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 批发商身份表
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@TableName(value = "wholesaler")
@Data
public class Wholesaler {

    /** 批发商ID */
    @TableId(type = IdType.AUTO)
    private Integer wholesalerId;

    /** 关联 user.user_id */
    private Integer userId;

    /** 关联 wholesaler_grade.grade_id */
    private Integer gradeId;

    /** 0=禁用 1=启用 */
    private Integer status;

    /** 1=注册自动 2=后台导入 3=手动添加 */
    private Integer source;

    /** 多店铺隔离（一期固定0） */
    private Integer shopId;

    /** 备注 */
    private String remark;

    /** 0=有效 1=删除（逻辑删除） */
    @TableLogic
    private Integer isDelete;

    /** 创建时间（Unix秒） */
    private Long createTime;

    /** 更新时间（Unix秒） */
    private Long updateTime;
}
