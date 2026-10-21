// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.config;

import com.tigshop.bean.model.setting.Region;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 地区返回参数
 *
 * @author Jayce
 * @create 2024年11月11日 15:11
 */
@Getter
@Setter
public class ConfigBasicVO {
    private List<Region> countrys;

    private Map<String, Map<String, Object>> item;

    public ConfigBasicVO(List<Region> countrys, Map<String, Map<String, Object>> item) {
        this.countrys = countrys;
        this.item = item;
    }
}
