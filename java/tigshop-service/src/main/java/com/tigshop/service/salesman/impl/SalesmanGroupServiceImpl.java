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

package com.tigshop.service.salesman.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.param.salesman.SalesmanGroupEditParam;
import com.tigshop.bean.param.salesman.SalesmanGroupSaveParam;
import com.tigshop.bean.query.salesman.SalesmanGroupPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanGroupVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.salesman.SalesmanGroupMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.salesman.SalesmanGroupService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分销员分组接口
 *
 * @author kidd
 * @since 2025/06/21
 */
@Service
@AllArgsConstructor
public class SalesmanGroupServiceImpl extends BaseServiceImpl<SalesmanGroupMapper, SalesmanGroup> implements SalesmanGroupService {

    private final SalesmanMapper salesmanMapper;

    @Override
    public Page<SalesmanGroupVO> list(SalesmanGroupPageQuery pageQuery) {

        Page<SalesmanGroup> page = buildSortOrder(pageQuery);
        Page<SalesmanGroup> salesmanGroupPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(pageQuery.getGroupName()), SalesmanGroup::getGroupName, pageQuery.getGroupName())
                .orderByAsc(SalesmanGroup::getAddTime)
                .page(page);

        List<SalesmanGroup> salesmanGroups = salesmanGroupPage.getRecords();

        if (CollUtil.isEmpty(salesmanGroups)) {
            return new Page<>();
        }

        List<SalesmanGroupVO> resultRecords = salesmanGroups.stream().map(SalesmanGroupVO::new).toList();
        return PageUtil.convertPage(salesmanGroupPage, resultRecords);
    }

    @Override
    public SalesmanGroupVO detail(Integer id) {
        SalesmanGroup salesmanGroup = this.getById(id);
        Assert.notNull(salesmanGroup, () -> new GlobalException("未知的分销员分组"));

        return new SalesmanGroupVO(salesmanGroup);
    }

    @Override
    public void create(SalesmanGroupSaveParam param) {
        SalesmanGroup salesmanGroup = param.createSalesmanGroup();
        this.save(salesmanGroup);
    }

    @Override
    public void update(SalesmanGroupEditParam param) {
        SalesmanGroup salesmanGroup = this.getById(param.getGroupId());
        Assert.notNull(salesmanGroup, () -> new GlobalException("未知的分销员分组"));

        SalesmanGroup updateSalesmanGroup = param.updateSalesmanGroup(salesmanGroup);
        this.updateById(updateSalesmanGroup);
    }

    @Override
    public boolean del(Integer id) {
        List<Salesman> salesmanList = salesmanMapper.selectList(
                Wrappers.lambdaQuery(Salesman.class)
                        .eq(Salesman::getGroupId, id)
        );

        if (CollUtil.isNotEmpty(salesmanList)) {
            throw new GlobalException("该分组下有分销员，无法删除");
        }

        return this.removeById(id);
    }
}
