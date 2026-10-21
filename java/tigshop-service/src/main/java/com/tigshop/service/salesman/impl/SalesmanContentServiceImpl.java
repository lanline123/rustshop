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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.salesman.ContentStatus;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanContent;
import com.tigshop.bean.param.salesman.SalesmanContentEditParam;
import com.tigshop.bean.param.salesman.SalesmanContentSaveParam;
import com.tigshop.bean.query.salesman.SalesmanContentPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanContentVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.salesman.SalesmanContentMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.salesman.SalesmanContentService;
import com.tigshop.service.salesman.SalesmanService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 分销内容管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@AllArgsConstructor
public class SalesmanContentServiceImpl extends BaseServiceImpl<SalesmanContentMapper, SalesmanContent> implements SalesmanContentService {

    private final SalesmanService salesmanService;

    @Override
    public Page<SalesmanContentVO> list(SalesmanContentPageQuery query) {
        // 分页
        Page<SalesmanContent> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<SalesmanContent> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, query.getSortField(), query.getSortOrder());

        // 搜索关键字
        String title = query.getTitle();
        if (StringUtils.isNotEmpty(title)) {
            queryWrapper.like(SalesmanContent::getTitle, title);
        }
        // 筛选时间范围
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
            queryWrapper.between(SalesmanContent::getStartTime, TigUtils.toTimestampYmd(startTime), TigUtils.toTimestampYmd(endTime));
        } else if (StringUtils.isNotEmpty(startTime)) {
            queryWrapper.ge(SalesmanContent::getStartTime, TigUtils.toTimestampYmd(startTime));
        } else if (StringUtils.isNotEmpty(endTime)) {
            queryWrapper.le(SalesmanContent::getEndTime, TigUtils.toTimestampYmd(endTime));
        }

        Integer status = query.getStatus();
        Long currentTime = StringUtils.getCurrentTime();
        if (status != null) {
            if (ContentStatus.NOT_START.getCode() == status) {
                queryWrapper.gt(SalesmanContent::getStartTime, currentTime);
            } else if (ContentStatus.SHOWING.getCode() == status) {
                queryWrapper.le(SalesmanContent::getStartTime, currentTime)
                        .and(q -> q.ge(SalesmanContent::getEndTime, currentTime).or().eq(SalesmanContent::getEndTime, 0));
            } else {
                queryWrapper.lt(SalesmanContent::getEndTime, currentTime)
                        .gt(SalesmanContent::getEndTime, 0);
            }
        }
        Integer shopId = getShopId();
        if ("user".equals(query.getFrom())) {
//            $query->where('start_time', '<=', time())->where(function ($query) {
//                $query->where('end_time', '>=', time())->whereOr('end_time', 0);
//            })->where('is_available', 1);
//            $query->order('is_top', 'desc');
            queryWrapper
                    .le(SalesmanContent::getStartTime, StringUtils.getCurrentTime())
                    .and(i ->
                            i.ge(SalesmanContent::getEndTime, StringUtils.getCurrentTime())
                                    .or()
                                    .eq(SalesmanContent::getEndTime, 0))
                    .eq(SalesmanContent::getIsAvailable, 1);

            Salesman salesman = salesmanService.lambdaQuery()
                    .eq(Salesman::getUserId, getCurrentUserId())
                    .last("limit 1")
                    .one();

            if (salesman != null) {
                shopId = salesman.getShopId();
            } else {
                shopId = -1;
            }
        }

        queryWrapper.eq(SalesmanContent::getShopId, shopId);

        // 执行查询
        Page<SalesmanContent> salesmanContentPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<SalesmanContent> salesmanContentPageRecords = salesmanContentPage.getRecords();
        // 转换为VO
        List<SalesmanContentVO> salesmanContentVOList = salesmanContentPageRecords.stream()
                .map(salesmanContent -> {
                    SalesmanContentVO salesmanContentVO = new SalesmanContentVO();
                    BeanUtils.copyProperties(salesmanContent, salesmanContentVO);
                    if (salesmanContent.getStartTime() > 0) {
                        salesmanContentVO.setStartTime(TigUtils.handelTime(salesmanContent.getStartTime()));
                    }
                    if (salesmanContent.getEndTime() > 0) {
                        salesmanContentVO.setEndTime(TigUtils.handelTime(salesmanContent.getEndTime()));
                    }
                    salesmanContentVO.setStatusText(getStatusText(salesmanContent.getStartTime(), salesmanContent.getEndTime()));
                    return salesmanContentVO;
                }).toList();
        return PageUtil.convertPage(salesmanContentPage, salesmanContentVOList);
    }

    private String getStatusText(Long startTime, Long endTime) {
        Long now = StringUtils.getCurrentTime();

        if (endTime != null && startTime != null) {
            if (now < startTime) {
                return ContentStatus.getDescription(ContentStatus.NOT_START.getCode());
            } else if (now < endTime || endTime == 0) {
                return ContentStatus.getDescription(ContentStatus.SHOWING.getCode());
            } else {
                return ContentStatus.getDescription(ContentStatus.END.getCode());
            }
        }
        return "";
    }

    @Override
    public SalesmanContentVO detail(Integer id) {
        if (id != null) {
            SalesmanContent salesmanContent = this.getById(id);
            SalesmanContentVO salesmanContentVO = new SalesmanContentVO();
            BeanUtils.copyProperties(salesmanContent, salesmanContentVO);
            if (salesmanContent.getStartTime() > 0) {
                salesmanContentVO.setStartTime(TigUtils.handelTime(salesmanContent.getStartTime()));
            }
            if (salesmanContent.getEndTime() > 0) {
                salesmanContentVO.setEndTime(TigUtils.handelTime(salesmanContent.getEndTime()));
            }
            return salesmanContentVO;
        }
        return null;
    }

    @Override
    public void create(SalesmanContentSaveParam createDTO) {
        SalesmanContent salesmanContent = new SalesmanContent();
        BeanUtils.copyProperties(createDTO, salesmanContent);
        if (createDTO.getStartTime() != null) {
            salesmanContent.setStartTime(TigUtils.toTimestampYmdHms(createDTO.getStartTime()));
        }
        if (createDTO.getEndTime() != null) {
            salesmanContent.setEndTime(TigUtils.toTimestampYmdHms(createDTO.getEndTime()));
        }
        this.save(salesmanContent);
    }

    @Override
    public void update(SalesmanContentEditParam param) {
        SalesmanContent salesmanContent = new SalesmanContent();
        BeanUtils.copyProperties(param, salesmanContent);

        if (param.getStartTime() != null) {
            salesmanContent.setStartTime(TigUtils.toTimestampYmdHms(param.getStartTime()));
        }
        if (param.getEndTime() != null) {
            salesmanContent.setEndTime(TigUtils.toTimestampYmdHms(param.getEndTime()));
            //如果修改的结束时间在当前时间之后，则设置分销内容管理可用
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime endTime = LocalDateTime.parse(param.getEndTime(), formatter);
            salesmanContent.setIsAvailable(endTime.isAfter(LocalDateTime.now()) ? Constants.YES : Constants.NO);
        }
        this.updateById(salesmanContent);
    }
}
