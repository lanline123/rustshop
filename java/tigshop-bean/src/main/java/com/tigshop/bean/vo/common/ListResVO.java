// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.common;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应对象
 *
 * @param <T> 数据列表
 * @param <F> 筛选条件
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
@Schema(description = "通用分页响应对象")
@Getter
@Setter
public class ListResVO<T, F> {
    @Schema(description = "数据列表")
    private List<T> filterResult;

    @Schema(description = "筛选条件")
    private F filter;

    @Schema(description = "数据总数")
    private Long total;

    @Schema(description = "父级名称")
    private String parentName;

    public ListResVO(F filter) {
        this.filterResult = Collections.emptyList();
        this.filter = filter;
        this.total = 0L;
    }
    public ListResVO(List<T> filterResult) {
        this.filterResult = Collections.emptyList();
        if (filterResult != null){
            this.filterResult = filterResult;
        }
        this.filter = null;
        this.total = 0L;
    }
    public ListResVO(List<T> filterResult, F filter, Long total) {
        this.filterResult = filterResult;
        this.filter = filter;
        this.total = total;
    }

    public ListResVO(List<T> filterResult, F filter, Long total, String parentName) {
        this.filterResult = filterResult;
        this.filter = filter;
        this.total = total;
        this.parentName = parentName;
    }

}
