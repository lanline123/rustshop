package com.tigshop.service.video.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryVideoCreateDTO;
import com.tigshop.bean.dto.setting.GalleryVideoUpdateDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import com.tigshop.bean.vo.setting.GalleryTreeNodeVO;
import com.tigshop.bean.vo.setting.GalleryVideoListVO;
import com.tigshop.bean.vo.setting.GalleryVideoVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.video.GalleryVideoInfoMapper;
import com.tigshop.mapper.video.GalleryVideoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.video.GalleryVideoService;
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
 * @author wzh
 */
@Service
public class GalleryVideoServiceImpl extends BaseServiceImpl<GalleryVideoMapper, GalleryVideo>
        implements GalleryVideoService {

    @Resource
    private GalleryVideoInfoMapper galleryVideoInfoMapper;

    @Override
    public Page<GalleryVideoVO> list(GalleryListDTO dto) {
        // 创建分页对象
        Page<GalleryVideo> page = new Page<>(dto.getPage(), 99);
        // 构建查询条件
        LambdaQueryWrapper<GalleryVideo> queryWrapper = buildGalleryQueryWrapper(dto);
        // 执行分页查询
        Page<GalleryVideo> galleryPage = this.page(page, queryWrapper);
        // 将查询结果转为 DTO
        List<GalleryVideoVO> recordsDTO = galleryPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        // 返回分页结果
        return PageUtil.convertPage(galleryPage, recordsDTO);
    }

    @Override
    public List<GalleryVideoListVO> treeList() {
        LambdaQueryWrapper<GalleryVideo> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, 0);
            queryWrapper.eq(GalleryVideo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType()) ||
                AdminTypeEnum.STORE == AdminTypeEnum.fromCode(getAdminType()) ||
                AdminTypeEnum.PICKUP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getVendorId, getVendorId());
        }
        queryWrapper.orderByAsc(GalleryVideo::getSort).orderByAsc(GalleryVideo::getId);
        List<GalleryVideo> galleryVideoList = list(queryWrapper);

        if (galleryVideoList.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用 Map 缓存所有节点，避免多次遍历
        Map<Integer, GalleryVideoListVO> nodeMap = new HashMap<>();

        // 初始化所有节点到 Map
        galleryVideoList.forEach(gallery -> {
            GalleryVideoListVO vo = BeanUtil.copyProperties(gallery, GalleryVideoListVO.class);
            nodeMap.put(vo.getId(), vo);
        });

        // 按列表挂树后，再按同级 sort + id 重排，避免父子共用全局 sort 时父级被打乱
        List<GalleryVideoListVO> rootList = new ArrayList<>();
        for (GalleryVideo gallery : galleryVideoList) {
            GalleryVideoListVO node = nodeMap.get(gallery.getId());
            GalleryVideoListVO parent = nodeMap.get(node.getParentId());
            if (node.getParentId() == 0) {
                rootList.add(node);
            } else if (parent != null) {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        sortVideoTreeNodes(rootList);

        return rootList;
    }

    /**
     * 递归按同级 sort 升序、id 升序排列
     */
    private void sortVideoTreeNodes(List<GalleryVideoListVO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator
                .comparing((GalleryVideoListVO n) -> n.getSort() == null ? Integer.MAX_VALUE : n.getSort())
                .thenComparing(n -> n.getId() == null ? 0 : n.getId()));
        for (GalleryVideoListVO node : nodes) {
            sortVideoTreeNodes(node.getChildren());
        }
    }


    @Override
    public GalleryVideoVO detail(Integer id) {
        // 如果 ID 小于 1，返回空
        if (id < 1) {
            return null;
        }
        // 获取相册实体并转换为 DTO
        GalleryVideo gallery = this.getById(id);
        // 返回相册详情
        return convertToDTO(gallery);
    }

    @Override
    public Integer create(GalleryVideoCreateDTO dto) {
        GalleryVideo gallery = convertToBean(dto);
        Integer parentId = gallery.getParentId() == null ? 0 : gallery.getParentId();
        gallery.setParentId(parentId);
        gallery.setSort(nextVideoSort(parentId));
        this.save(gallery);
        return gallery.getId();
    }

    /**
     * 计算同父级下下一个排序值（max(sort) + 1，无同级时为 1）
     */
    private int nextVideoSort(Integer parentId) {
        LambdaQueryWrapper<GalleryVideo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GalleryVideo::getParentId, parentId);
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, 0);
            queryWrapper.eq(GalleryVideo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType()) ||
                AdminTypeEnum.STORE == AdminTypeEnum.fromCode(getAdminType()) ||
                AdminTypeEnum.PICKUP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getVendorId, getVendorId());
        }
        queryWrapper.orderByDesc(GalleryVideo::getSort).last("LIMIT 1");
        GalleryVideo max = this.getOne(queryWrapper, false);
        if (max == null || max.getSort() == null) {
            return 1;
        }
        return max.getSort() + 1;
    }

    @Override
    public Integer update(GalleryVideoUpdateDTO dto) {
        GalleryVideo galleryVideo = this.getById(dto.getId());
        Assert.notNull(galleryVideo, "视频相册不存在");

        Long count = this.lambdaQuery()
                .ne(GalleryVideo::getId, dto.getId())
                .eq(GalleryVideo::getParentId, galleryVideo.getParentId())
                .eq(GalleryVideo::getName, dto.getName())
                .count();

        Assert.isTrue(count == 0, "视频相册名称已存在");

        this.lambdaUpdate()
                .eq(GalleryVideo::getId, dto.getId())
                .set(GalleryVideo::getName, dto.getName())
                .set(dto.getSort() != null, GalleryVideo::getSort, dto.getSort())
                .update();

        return dto.getId();
    }

    private LambdaQueryWrapper<GalleryVideo> buildGalleryQueryWrapper(GalleryListDTO dto) {
        Page<GalleryVideo> page = new Page<>(dto.getPage(), 10000);
        LambdaQueryWrapper<GalleryVideo> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, 0);
            queryWrapper.eq(GalleryVideo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getVendorId, getVendorId());
        }
        Integer galleryId = dto.getGalleryId();
        // 如果指定了 galleryId，按 parent_id 过滤
        if (galleryId != null && galleryId >= 0) {
            queryWrapper.eq(GalleryVideo::getParentId, galleryId);
        }

        // 排序
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        return queryWrapper;
    }


    private GalleryVideoVO convertToDTO(GalleryVideo gallery) {
        GalleryVideoVO dto = new GalleryVideoVO();
        BeanUtils.copyProperties(gallery, dto);
        return dto;
    }


    private GalleryVideo convertToBean(GalleryVideoCreateDTO dto) {
        GalleryVideo gallery = new GalleryVideo();
        BeanUtils.copyProperties(dto, gallery);
        gallery.setShopId(getShopId());
        gallery.setAddUserId(SecurityUtils.getCurrentAdminId());
        gallery.setAddTime(StringUtils.getCurrentTime());
        gallery.setVendorId(getVendorId());
        return gallery;
    }

    @Override
    public List<GalleryTreeNodeVO> tree() {
        List<GalleryVideoListVO> voList = treeList();
        List<GalleryTreeNodeVO> result = convertToTreeNodeVO(voList);
        // 将每个节点的数量累加为「自身 + 所有子孙」的累计值
        for (GalleryTreeNodeVO root : result) {
            accumulatePicCount(root);
        }
        return result;
    }

    /**
     * 递归累加视频相册节点数量（自身 + 所有子孙），并回写到节点的 picCount
     *
     * @param node 相册树节点
     * @return 该节点含子孙的累计数量
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

    private List<GalleryTreeNodeVO> convertToTreeNodeVO(List<GalleryVideoListVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return new ArrayList<>();
        }
        List<GalleryTreeNodeVO> result = new ArrayList<>();
        for (GalleryVideoListVO vo : voList) {
            GalleryTreeNodeVO node = new GalleryTreeNodeVO();
            node.setId(vo.getId());
            node.setGalleryId(vo.getId());
            node.setParentId(vo.getParentId());
            node.setName(vo.getName());
            node.setGalleryName(vo.getName());
            node.setGallerySort(vo.getSort());
            node.setSort(vo.getSort());

            long count = galleryVideoInfoMapper.selectCount(
                    new LambdaQueryWrapper<GalleryVideoInfo>()
                            .eq(GalleryVideoInfo::getGalleryId, vo.getId()));
            node.setPicCount(count);

            node.setChildren(convertToTreeNodeVO(vo.getChildren()));
            result.add(node);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(GallerySortDTO dto) {
        List<Integer> ids = dto.getIds();
        for (int i = 0; i < ids.size(); i++) {
            this.lambdaUpdate()
                    .eq(GalleryVideo::getId, ids.get(i))
                    .set(GalleryVideo::getSort, i + 1)
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
            galleryVideoInfoMapper.update(null,
                    new LambdaUpdateWrapper<GalleryVideoInfo>()
                            .set(GalleryVideoInfo::getGalleryId, targetId)
                            .eq(GalleryVideoInfo::getGalleryId, groupId));
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
        List<GalleryVideo> children = this.lambdaQuery()
                .eq(GalleryVideo::getParentId, parentId)
                .list();
        for (GalleryVideo child : children) {
            result.add(child.getId());
            collectChildGroupIds(child.getId(), result);
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
                galleryVideoInfoMapper.delete(
                        new LambdaQueryWrapper<GalleryVideoInfo>()
                                .eq(GalleryVideoInfo::getGalleryId, groupId));
            }
        } else {
            for (Integer groupId : allGroupIds) {
                galleryVideoInfoMapper.update(null,
                        new LambdaUpdateWrapper<GalleryVideoInfo>()
                                .set(GalleryVideoInfo::getGalleryId, 0)
                                .eq(GalleryVideoInfo::getGalleryId, groupId));
            }
        }

        this.removeByIds(allGroupIds);
    }
}




