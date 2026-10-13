package com.tigshop.bean.vo.common;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 状态list
 *
 * @author Jayce
 * @create 2024年10月22日 11:17
 */
@Getter
@Setter
@Schema(description = "状态封装返回")
public class StatusListVO<T> {
    @Schema(description = "状态list")
    private T statusList;

    public StatusListVO(T statusList) {
        this.statusList = statusList;
    }
}

