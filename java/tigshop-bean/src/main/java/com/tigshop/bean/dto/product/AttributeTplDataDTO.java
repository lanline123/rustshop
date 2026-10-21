// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import lombok.Data;

import java.util.List;

/**
 * 模板数据
 *
 * @author Jayce
 * @create 2024年11月05日 11:15
 */
@Data
public class AttributeTplDataDTO {
    private List<Attribute> normal;
    private List<Attribute> spe;
    private List<Attribute> extra;

    @Data
    public static class Attribute {
        private String attrName;
        private List<String> attrValues;
    }
}
