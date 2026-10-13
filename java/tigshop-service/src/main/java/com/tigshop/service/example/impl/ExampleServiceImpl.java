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

package com.tigshop.service.example.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.example.ExampleCreateDTO;
import com.tigshop.bean.dto.example.ExampleListDTO;
import com.tigshop.bean.dto.example.ExampleUpdateDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.model.example.Example;
import com.tigshop.bean.vo.example.ExampleVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.example.ExampleMapper;
import com.tigshop.service.example.ExampleService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 示例服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ExampleServiceImpl extends BaseServiceImpl<ExampleMapper, Example> implements ExampleService {
    @Override
    public Page<ExampleVO> list(ExampleListDTO listDTO) {
        // 分页
        Page<Example> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Example> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer isShow = listDTO.getIsShow();
        if (IsShowType.fromTypeCode(isShow) != null) {
            queryWrapper.eq(Example::getIsShow, isShow);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Example::getExampleName, keyword);
        }
        // 执行查询
        Page<Example> examplePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Example> examplePageRecords = examplePage.getRecords();
        // 转换为VO
        List<ExampleVO> exampleVOList = examplePageRecords.stream()
                .map(example -> {
                    ExampleVO exampleVO = new ExampleVO();
                    BeanUtils.copyProperties(example, exampleVO);
                    return exampleVO;
                }).toList();

        return PageUtil.convertPage(examplePage, exampleVOList);
    }

    @Override
    public ExampleVO detail(Integer id) {
        if (id != null) {
            Example example = this.getById(id);
            ExampleVO exampleVO = new ExampleVO();
            BeanUtils.copyProperties(example, exampleVO);
            return exampleVO;
        }
        return null;
    }

    @Override
    public boolean create(ExampleCreateDTO createDTO) {
        if (createDTO != null) {
            Example example = new Example();
            BeanUtils.copyProperties(createDTO, example);
            return this.save(example);
        }
        return false;
    }

    @Override
    public boolean update(ExampleUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Example example = new Example();
            BeanUtils.copyProperties(updateDTO, example);
            return this.updateById(example);
        }
        return false;
    }
}
