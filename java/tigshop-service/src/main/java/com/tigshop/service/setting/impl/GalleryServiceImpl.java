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

package com.tigshop.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.dto.setting.GalleryDTO;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryUpdateDTO;
import com.tigshop.bean.model.setting.Gallery;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.vo.setting.GalleryTreeNodeVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.GalleryMapper;
import com.tigshop.mapper.setting.GalleryPicMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.GalleryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 相册服务实现
 *
 * @author Jayce
 * @create 2024年11月12日 13:48
 */
@Service
public class GalleryServiceImpl extends BaseServiceImpl<GalleryMapper, Gallery> implements GalleryService {
    @Resource
    private GalleryPicMapper galleryPicMapper;

    /**
     * 获取相册列表（分页）
     *
     * @param dto 查询条件和分页信息
     * @return 分页后的相册 DTO 列表
     */
    @Override
    public Page<GalleryDTO> list(GalleryListDTO dto) {
        Page<Gallery> page = new Page<>(dto.getPage(), 99);
        String sortField = dto.getSortField();
        if (sortField == null || sortField.isEmpty()) {
            sortField = "gallerySort";
        }
        String sortOrder = dto.getSortOrder();
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "asc";
        }
        buildSortOrder(page, sortField, sortOrder);
        // 构建查询条件
        LambdaQueryWrapper<Gallery> queryWrapper = buildGalleryQueryWrapper(dto);
        // 执行分页查询
        Page<Gallery> galleryPage = this.page(page, queryWrapper);
        // 将查询结果转为 DTO
        List<GalleryDTO> recordsDTO = galleryPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        // 返回分页结果
        return PageUtil.convertPage(galleryPage, recordsDTO);
    }

    /**
     * 构建 Gallery 查询条件
     * @param dto 查询条件和排序信息
     * @return QueryWrapper
     */
    private LambdaQueryWrapper<Gallery> buildGalleryQueryWrapper(GalleryListDTO dto) {
        LambdaQueryWrapper<Gallery> queryWrapper = new LambdaQueryWrapper<>();
        Integer shopId = getShopId();
        queryWrapper.eq(shopId != null, Gallery::getShopId, shopId);

        Integer vendorId = HeaderUtils.getVendorId();
        queryWrapper.eq(vendorId != null, Gallery::getVendorId, vendorId);

        Integer galleryId = dto.getGalleryId();
        // 如果指定了 galleryId，按 parent_id 过滤
        if (galleryId != null && galleryId >= 0) {
            queryWrapper.eq(Gallery::getParentId, galleryId);
        }

        return queryWrapper;
    }

    /**
     * 获取相册详情
     *
     * @param id 相册 ID
     * @return 相册详情 DTO
     */
    @Override
    public GalleryDTO detail(Integer id) {
        // 如果 ID 小于 1，返回空
        if (id < 1) {
            return null;
        }
        // 获取相册实体并转换为 DTO
        Gallery gallery = this.getById(id);
        // 返回相册详情
        return convertToDTO(gallery);
    }

    /**
     * 创建相册
     * @param dto 相册 DTO
     * @return 是否创建成功
     */
    @Override
    public boolean create(GalleryDTO dto) {
        // 将 DTO 转换为实体并保存
        Gallery gallery = convertToBean(dto);
        Integer vendorId = HeaderUtils.getVendorId();
        gallery.setVendorId(vendorId);
        // 归一化根节点 parentId，并按同级 max(sort)+1 追加，避免落到库默认值 50 打乱父子排序
        Integer parentId = gallery.getParentId() == null ? 0 : gallery.getParentId();
        gallery.setParentId(parentId);
        gallery.setGallerySort(nextGallerySort(parentId));
        return this.save(gallery);
    }

    /**
     * 计算同父级下下一个排序值（max(gallery_sort) + 1，无同级时为 1）
     */
    private int nextGallerySort(Integer parentId) {
        LambdaQueryWrapper<Gallery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Gallery::getParentId, parentId);
        Integer shopId = getShopId();
        queryWrapper.eq(shopId != null, Gallery::getShopId, shopId);
        Integer vendorId = HeaderUtils.getVendorId();
        queryWrapper.eq(vendorId != null, Gallery::getVendorId, vendorId);
        queryWrapper.orderByDesc(Gallery::getGallerySort).last("LIMIT 1");
        Gallery max = this.getOne(queryWrapper, false);
        if (max == null || max.getGallerySort() == null) {
            return 1;
        }
        return max.getGallerySort() + 1;
    }

    /**
     * 更新相册
     * @param dto 相册 DTO
     * @return 是否更新成功
     */
    @Override
    public boolean update(GalleryUpdateDTO dto) {
        // 构建更新条件
        LambdaUpdateWrapper<Gallery> updateWrapper = buildUpdateWrapper(dto);
        // 将 DTO 转换为实体并执行更新
        Gallery gallery = convertToBean(dto);
        return this.update(gallery, updateWrapper);
    }

    /**
     * 构建更新条件（根据 ID）
     * @param dto 相册 DTO
     * @return LambdaUpdateWrapper
     */
    private LambdaUpdateWrapper<Gallery> buildUpdateWrapper(GalleryUpdateDTO dto) {
        LambdaUpdateWrapper<Gallery> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Gallery::getGalleryId, dto.getId());
        return updateWrapper;
    }

    /**
     * 获取指定父级 ID 的所有子相册
     * @param pid 父级相册 ID
     * @return 子相册 DTO 列表
     */
    @Override
    public List<GalleryDTO> getGalleryByPid(Integer pid) {
        LambdaQueryWrapper<Gallery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Gallery::getParentId, pid);
        List<Gallery> galleryList = this.list(queryWrapper);
        // 将实体列表转换为 DTO 列表
        return galleryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 将 Gallery 实体转换为 DTO
     * @param gallery Gallery 实体
     * @return GalleryDTO
     */
    private GalleryDTO convertToDTO(Gallery gallery) {
        GalleryDTO dto = new GalleryDTO();
        BeanUtils.copyProperties(gallery, dto);
        return dto;
    }

    /**
     * 将 GalleryDTO 转换为实体
     * @param dto GalleryDTO
     * @return Gallery 实体
     */
    private Gallery convertToBean(GalleryDTO dto) {
        Gallery gallery = new Gallery();
        BeanUtils.copyProperties(dto, gallery);
        gallery.setShopId(getShopId());
        return gallery;
    }

    @Override
    public List<GalleryTreeNodeVO> tree() {
        LambdaQueryWrapper<Gallery> queryWrapper = new LambdaQueryWrapper<>();
        Integer shopId = getShopId();
        queryWrapper.eq(shopId != null, Gallery::getShopId, shopId);
        Integer vendorId = HeaderUtils.getVendorId();
        queryWrapper.eq(vendorId != null, Gallery::getVendorId, vendorId);
        // gallery_sort 为全局字段，同值时用 id 兜底；最终同级顺序在组装后按父级隔离重排
        queryWrapper.orderByAsc(Gallery::getGallerySort).orderByAsc(Gallery::getGalleryId);

        List<Gallery> galleryList = this.list(queryWrapper);
        if (galleryList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, Long> picCountMap = new HashMap<>();
        for (Gallery g : galleryList) {
            long count = galleryPicMapper.selectCount(
                    new LambdaQueryWrapper<GalleryPic>()
                            .eq(GalleryPic::getGalleryId, g.getGalleryId())
            );
            picCountMap.put(g.getGalleryId(), count);
        }

        Map<Integer, GalleryTreeNodeVO> nodeMap = new HashMap<>();
        for (Gallery g : galleryList) {
            GalleryTreeNodeVO node = new GalleryTreeNodeVO();
            node.setId(g.getGalleryId());
            node.setGalleryId(g.getGalleryId());
            node.setParentId(g.getParentId());
            node.setGalleryName(g.getGalleryName());
            node.setName(g.getGalleryName());
            node.setGallerySort(g.getGallerySort());
            node.setSort(g.getGallerySort());
            node.setPicCount(picCountMap.getOrDefault(g.getGalleryId(), 0L));
            node.setChildren(new ArrayList<>());
            nodeMap.put(g.getGalleryId(), node);
        }

        // 先挂树，再按「同级」gallerySort + id 排序，避免父子共用全局 sort 时父级被打乱
        List<GalleryTreeNodeVO> rootList = new ArrayList<>();
        for (Gallery g : galleryList) {
            GalleryTreeNodeVO node = nodeMap.get(g.getGalleryId());
            if (node.getParentId() == null || node.getParentId() == 0) {
                rootList.add(node);
            } else {
                GalleryTreeNodeVO parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    rootList.add(node);
                }
            }
        }
        sortTreeNodes(rootList);

        // 将每个节点的图片数量累加为「自身 + 所有子孙」的累计值
        for (GalleryTreeNodeVO root : rootList) {
            accumulatePicCount(root);
        }

        return rootList;
    }

    /**
     * 递归按同级 gallerySort 升序、id 升序排列，使父子层级排序互不干扰
     */
    private void sortTreeNodes(List<GalleryTreeNodeVO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator
                .comparing((GalleryTreeNodeVO n) -> n.getGallerySort() == null ? Integer.MAX_VALUE : n.getGallerySort())
                .thenComparing(n -> n.getId() == null ? 0 : n.getId()));
        for (GalleryTreeNodeVO node : nodes) {
            sortTreeNodes(node.getChildren());
        }
    }

    /**
     * 递归累加相册节点图片数量（自身 + 所有子孙），并回写到节点的 picCount
     *
     * @param node 相册树节点
     * @return 该节点含子孙的累计图片数量
     */
    private long accumulatePicCount(GalleryTreeNodeVO node) {
        long total = node.getPicCount() == null ? 0L : node.getPicCount();
        if (node.getChildren() != null) {
            for (GalleryTreeNodeVO child : node.getChildren()) {
                total += accumulatePicCount(child);
            }
        }
        node.setPicCount(total);
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(GallerySortDTO dto) {
        List<Integer> ids = dto.getIds();
        for (int i = 0; i < ids.size(); i++) {
            this.lambdaUpdate()
                    .eq(Gallery::getGalleryId, ids.get(i))
                    .set(Gallery::getGallerySort, i + 1)
                    .update();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(GalleryMoveDTO dto) {
        Integer sourceId = dto.getSourceId();
        Integer targetId = dto.getTargetId();
        String mode = dto.getMode();

        if (sourceId.equals(targetId)) {
            throw new GlobalException("来源分组和目标分组不能相同");
        }

        List<Integer> sourceGroupIds = new ArrayList<>();
        collectChildGroupIds(sourceId, sourceGroupIds);
        sourceGroupIds.add(0, sourceId);

        if (sourceGroupIds.contains(targetId)) {
            throw new GlobalException("目标分组不能是来源分组的子孙节点");
        }

        for (Integer groupId : sourceGroupIds) {
            galleryPicMapper.update(null,
                    new LambdaUpdateWrapper<GalleryPic>()
                            .set(GalleryPic::getGalleryId, targetId)
                            .eq(GalleryPic::getGalleryId, groupId));
        }

        if ("move_and_delete".equals(mode)) {
            this.removeByIds(sourceGroupIds);
        }
    }

    @Override
    public List<Integer> getSelfAndChildIds(Integer galleryId) {
        List<Integer> ids = new ArrayList<>();
        if (galleryId == null) {
            return ids;
        }
        ids.add(galleryId);
        collectChildGroupIds(galleryId, ids);
        return ids;
    }

    private void collectChildGroupIds(Integer parentId, List<Integer> result) {
        List<Gallery> children = this.lambdaQuery()
                .eq(Gallery::getParentId, parentId)
                .list();
        for (Gallery child : children) {
            result.add(child.getGalleryId());
            collectChildGroupIds(child.getGalleryId(), result);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delWithMode(GalleryDelDTO dto) {
        Integer id = dto.getId();
        String deleteMode = dto.getDeleteMode();

        List<Integer> allGroupIds = new ArrayList<>();
        allGroupIds.add(id);
        collectChildGroupIds(id, allGroupIds);

        if ("group_and_files".equals(deleteMode)) {
            for (Integer groupId : allGroupIds) {
                galleryPicMapper.delete(
                        new LambdaQueryWrapper<GalleryPic>()
                                .eq(GalleryPic::getGalleryId, groupId));
            }
        } else {
            for (Integer groupId : allGroupIds) {
                galleryPicMapper.update(null,
                        new LambdaUpdateWrapper<GalleryPic>()
                                .set(GalleryPic::getGalleryId, 0)
                                .eq(GalleryPic::getGalleryId, groupId));
            }
        }

        this.removeByIds(allGroupIds);
    }
}
