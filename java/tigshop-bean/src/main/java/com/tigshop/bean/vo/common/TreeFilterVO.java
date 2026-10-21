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

import cn.hutool.core.lang.tree.Tree;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 树形结构返回类
 *
 * @author Jayce
 * @create 2024年10月17日 16:57
 */
@Getter
@Setter
@Schema(description = "树形结构返回类")
public class TreeFilterVO {
    @Schema(description = "树")
    private List<Tree<Integer>> filterResult;

    public TreeFilterVO(List<Tree<Integer>> filterResult){
        this.filterResult = filterResult;
    }
}
