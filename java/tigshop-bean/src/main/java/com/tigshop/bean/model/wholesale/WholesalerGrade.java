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
 * 批发商等级表
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@TableName(value = "wholesaler_grade")
@Data
public class WholesalerGrade {

    /** 等级ID */
    @TableId(type = IdType.AUTO)
    private Integer gradeId;

    /** 等级名称 */
    private String gradeName;

    /** 0=禁用 1=启用 */
    private Integer status;

    /** 排序值，升序 */
    private Integer sortOrder;

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
