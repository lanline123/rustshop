// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import java.util.List;

/**
 * 构建分页的工具类
 *
 * @author Tigshop团队
 * @create 2025年4月01日 10:00
 */
public class PageUtil {

    /**
     * 将 Page<Entity> 转换为 Page<VO>
     *
     * @param sourcePage 源 Page（如 Page<Entity>）
     * @param voList     目标 VO List（如 List<VO>）
     * @param <T>        VO 类型
     * @param <E>        Entity 类型
     * @return Page<T> 返回转换后的 Page<VO>
     */
    public static <T, E> Page<T> convertPage(Page<E> sourcePage, List<T> voList) {
        Page<T> targetPage = new Page<>();
        // 复制分页信息
        BeanUtils.copyProperties(sourcePage, targetPage);
        // 设置 VO 列表
        targetPage.setRecords(voList);
        return targetPage;
    }
}
