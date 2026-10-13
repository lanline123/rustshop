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

package com.tigshop.service.im.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.ConfigCreateDTO;
import com.tigshop.bean.dto.im.ConfigDataDTO;
import com.tigshop.bean.dto.im.ConfigListDTO;
import com.tigshop.bean.dto.im.ConfigUpdateDTO;
import com.tigshop.bean.model.im.Config;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.im.ConfigVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.im.ImConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * im配置服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ImConfigServiceImpl extends BaseServiceImpl<ImConfigMapper, Config> implements ImConfigService {
    @Override
    public ListResVO<ConfigVO, ConfigListDTO> list(ConfigListDTO listDTO) {
        // 分页
        Page<Config> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (IsShowType.fromTypeCode(isShow) != null) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }*/
        // 执行查询
        Page<Config> configPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Config> configPageRecords = configPage.getRecords();
        // 转换为VO
        List<ConfigVO> configVOList = configPageRecords.stream()
                .map(config -> {
                    ConfigVO configVO = new ConfigVO();
                    BeanUtils.copyProperties(config, configVO);
                    return configVO;
                }).toList();
        return new ListResVO<>(configVOList, listDTO, configPage.getTotal());
    }

    @Override
    public ConfigDataDTO detail(String code, Integer shopId) {
        if (code != null) {
            LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Config::getCode, code);
            queryWrapper.eq(ObjectUtil.isNotEmpty(shopId), Config::getShopId, shopId);
            Config config = this.getOne(queryWrapper);
            if (config != null) {
                ConfigDataDTO configData = JsonUtil.fromJson(config.getData(), ConfigDataDTO.class);
                return configData;
            }
        }
        return null;
    }

    @Override
    public boolean create(ConfigCreateDTO createDTO) {
        if (createDTO != null) {
            LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Config::getCode, createDTO.getCode());
            queryWrapper.eq(Config::getShopId, createDTO.getShopId());
            Config config = this.getOne(queryWrapper);
            if (config == null) {
                Config createConfig = new Config();
                BeanUtils.copyProperties(createDTO, createConfig);
                createConfig.setData(JsonUtil.toJson(createDTO.getData()));
                return this.save(createConfig);
            } else {
                config.setData(JsonUtil.toJson(createDTO.getData()));
                return this.updateById(config);
            }
        }
        return false;
    }

    @Override
    public boolean update(ConfigUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Config config = new Config();
            BeanUtils.copyProperties(updateDTO, config);
            return this.updateById(config);
        }
        return false;
    }
}
