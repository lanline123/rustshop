// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.vo.setting.GalleryTreeNodeVO;
import com.tigshop.bean.model.setting.Gallery;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 相册服务
 *
 * @author Jayce
 * @create 2024年11月12日 13:46
 */
public interface GalleryService extends BaseService<Gallery> {
    /**
     * 列表
     *
     * @param dto 列表参数
     * @return 相册列表
     */
    Page<GalleryDTO> list(GalleryListDTO dto);

    /**
     * 详情
     *
     * @param id id
     * @return 相册详情
     */
    GalleryDTO detail(Integer id);

    /**
     * 创建
     *
     * @param dto 创建参数
     * @return 是否
     */
    boolean create(GalleryDTO dto);

    /**
     * 更新
     *
     * @param dto 更新参数
     * @return 是否
     */
    boolean update(GalleryUpdateDTO dto);

    /**
     * 根据pid获取图片列表
     *
     * @param pid 父id
     * @return 相册列表
     */
    List<GalleryDTO> getGalleryByPid(Integer pid);

    /**
     * 获取相册自身及其所有子孙相册的 ID 集合
     *
     * @param galleryId 相册 ID
     * @return 包含自身及全部子孙相册的 ID 列表
     */
    List<Integer> getSelfAndChildIds(Integer galleryId);

    List<GalleryTreeNodeVO> tree();
    void sort(GallerySortDTO dto);
    void move(GalleryMoveDTO dto);
    void delWithMode(GalleryDelDTO dto);
}
