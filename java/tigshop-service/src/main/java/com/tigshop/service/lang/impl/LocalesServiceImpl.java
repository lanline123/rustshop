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

package com.tigshop.service.lang.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.lang.LocalesDTO;
import com.tigshop.bean.dto.lang.LocalesListDTO;
import com.tigshop.bean.model.lang.Currency;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.vo.lang.LocalesCurrencyVO;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.lang.LocalesMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.lang.LocalesService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 语言地区服务接口实现类
 *
 * @author Tigshop团队
 * @create 2024年12月30日 16:39
 */
@Service
public class LocalesServiceImpl extends BaseServiceImpl<LocalesMapper, Locales> implements LocalesService {


    @Override
    public Page<LocalesVO> list(LocalesListDTO basePage) {
        // 分页查询
        Page<Locales> page = new Page<>(basePage.getPage(), basePage.getSize());
        LambdaQueryWrapper<Locales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(basePage.getIsEnabled()), Locales::getIsEnabled, basePage.getIsEnabled());
        // 排序字段
        buildSortOrder(page, basePage.getSortField(), basePage.getSortOrder());
        // 转vo
        List<LocalesVO> list = this.list(page, queryWrapper).stream().map(this::getLocalesVO).toList();

        return PageUtil.convertPage(page, list);
    }

    @Override
    public List<LocalesVO> getLocalesLimit3() {
        // 取三条不是中文的给翻译内容列表进行展示
        Page<Locales> page = new Page<>(0, 3);
        LambdaQueryWrapper<Locales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Locales::getIsEnabled, 1)
                .ne(Locales::getLocaleCode, "zh");

        return this.list(page, queryWrapper).stream().map(this::getLocalesVO).toList();
    }

    /**
     * 转vo
     *
     * @param item item
     * @return LocalesVO
     */
    @Override
    public LocalesVO getLocalesVO(Locales item) {
        LocalesVO vo = new LocalesVO();
        BeanUtils.copyProperties(item, vo);

        String lastUpdated;
        if (item.getLastUpdated() != null) {
            DateTime date = DateUtil.date(item.getLastUpdated() * 1000);
            lastUpdated = DateUtil.format(date, DATE_FORMAT);
        } else {
            lastUpdated = DateUtil.format(DateUtil.date(), DATE_FORMAT);
        }

        vo.setLastUpdated(lastUpdated);
        return vo;
    }

    @Override
    public LocalesVO detail(Integer id) {
        if (id != null) {
            Locales locales = this.getById(id);
            return getLocalesVO(locales);
        }
        return null;
    }

    @Override
    public boolean create(LocalesDTO dto) {
        if (dto != null) {
            Locales locales = new Locales();
            BeanUtils.copyProperties(dto, locales);
            locales.setLastUpdated(DateUtil.currentSeconds());
            return this.save(locales);
        }
        return false;
    }

    @Override
    public boolean update(LocalesDTO dto) {
        if (dto.getIsDefault() == 1) {
            // 更新列表中所有的默认为 0
            this.update(new LambdaUpdateWrapper<Locales>().eq(Locales::getIsDefault, 1).set(Locales::getIsDefault, 0));
        }

        if (dto != null) {
            Locales locales = new Locales();
            BeanUtils.copyProperties(dto, locales);
            locales.setLastUpdated(DateUtil.currentSeconds());
            return this.updateById(locales);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            return this.removeByIds(batchDTO.getIds());
        }
        if ("enabled".equals(batchDTO.getType())) {
            return this.updateBatchById(batchDTO.getIds().stream().map(id -> {
                Locales locales = new Locales();
                locales.setId(id);
                locales.setIsEnabled(batchDTO.getValue());
                return locales;
            }).toList());
        }
        return false;
    }

    @Override
    public List<LocalesCurrencyVO> getLocales() {
        // 先查查默认语言
        LambdaQueryWrapper<Locales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Locales::getIsEnabled, 1).orderByAsc(Locales::getSort);
        List<Locales> LocalesList = this.list(queryWrapper);
        // 把id给我找出来
        List<Integer> currencyIds = LocalesList.stream().map(Locales::getCurrencyId).toList();


        return LocalesList.stream().map(item -> {
            LocalesCurrencyVO vo = new LocalesCurrencyVO();
            BeanUtils.copyProperties(getLocalesVO(item), vo);
            return vo;
        }).toList();
    }

    @Override
    public Locales getByDomain(String domain) {
        if (StrUtil.isBlank(domain)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<Locales>()
                .eq(Locales::getDomain, domain.trim())
                .eq(Locales::getIsEnabled, 1)
                .last("limit 1"));
    }
}
