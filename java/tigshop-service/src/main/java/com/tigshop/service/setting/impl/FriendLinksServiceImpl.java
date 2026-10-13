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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.FriendLinksCreateDTO;
import com.tigshop.bean.dto.setting.FriendLinksListDTO;
import com.tigshop.bean.dto.setting.FriendLinksUpdateDTO;
import com.tigshop.bean.model.setting.FriendLinks;
import com.tigshop.bean.vo.setting.FriendLinksVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.FriendLinksMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.FriendLinksService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 友情链接服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class FriendLinksServiceImpl extends BaseServiceImpl<FriendLinksMapper, FriendLinks> implements FriendLinksService {
    @Override
    public Page<FriendLinksVO> list(FriendLinksListDTO listDTO) {
        // 分页
        Page<FriendLinks> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<FriendLinks> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(FriendLinks::getLinkTitle, keyword);
        }
        // 执行查询
        Page<FriendLinks> friendLinksPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<FriendLinks> friendLinksPageRecords = friendLinksPage.getRecords();
        // 转换为VO
        List<FriendLinksVO> friendLinksVOList = friendLinksPageRecords.stream()
                .map(friendLinks -> {
                    FriendLinksVO friendLinksVO = new FriendLinksVO();
                    BeanUtils.copyProperties(friendLinks, friendLinksVO);
                    return friendLinksVO;
                }).toList();
        return PageUtil.convertPage(friendLinksPage, friendLinksVOList);
    }

    @Override
    public FriendLinksVO detail(Integer id) {
        if (id != null) {
            FriendLinks friendLinks = this.getById(id);
            FriendLinksVO friendLinksVO = new FriendLinksVO();
            BeanUtils.copyProperties(friendLinks, friendLinksVO);
            return friendLinksVO;
        }
        return null;
    }

    @Override
    public boolean create(FriendLinksCreateDTO createDTO) {
        if (createDTO != null) {
            FriendLinks friendLinks = new FriendLinks();
            BeanUtils.copyProperties(createDTO, friendLinks);
            return this.save(friendLinks);
        }
        return false;
    }

    @Override
    public boolean update(FriendLinksUpdateDTO updateDTO) {
        if (updateDTO != null) {
            FriendLinks friendLinks = new FriendLinks();
            BeanUtils.copyProperties(updateDTO, friendLinks);
            return this.updateById(friendLinks);
        }
        return false;
    }
}
