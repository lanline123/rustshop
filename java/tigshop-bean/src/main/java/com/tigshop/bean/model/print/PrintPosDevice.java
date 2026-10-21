package com.tigshop.bean.model.print;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 打印机与收银台设备码的多对多关系。 */
@Data
@TableName("print_pos_device")
public class PrintPosDevice {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer shopId;

    private Integer printId;

    private String deviceCode;

    private Long addTime;
}
