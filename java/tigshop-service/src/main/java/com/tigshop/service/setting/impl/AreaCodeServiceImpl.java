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

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.AreaCodeListDTO;
import com.tigshop.bean.model.setting.AreaCode;
import com.tigshop.bean.param.settings.areacode.AreaCodeEditParam;
import com.tigshop.bean.param.settings.areacode.AreaCodeSaveParam;
import com.tigshop.bean.vo.setting.AreaCodeVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.AreaCodeMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.AreaCodeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.tigshop.common.constant.ResultTextConstants.INVALID_FIELD_VALUE;
import static com.tigshop.common.constant.setting.AreaCodeConstants.AREA_CODE_IS_EXISTS;
import static com.tigshop.common.constant.setting.AreaCodeConstants.AREA_CODE_NAME_IS_EXISTS;

/**
 * 区号管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月30日 16:41
 */
@Service
public class AreaCodeServiceImpl extends BaseServiceImpl<AreaCodeMapper, AreaCode> implements AreaCodeService {
    @Override
    public Page<AreaCodeVO> list(AreaCodeListDTO dto) {
        Page<AreaCode> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件
        String name = dto.getName();
        if (StrUtil.isNotEmpty(name)) {
            queryWrapper.like(AreaCode::getName, name);
        }
        // 构建排序条件
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        // 转换为VO返回
        List<AreaCodeVO> list = this.list(page, queryWrapper).stream().map(item -> {
            AreaCodeVO vo = new AreaCodeVO();
            BeanUtils.copyProperties(item, vo);
            vo.setLabel(StrUtil.format("+{}", item.getCode()));
            return vo;
        }).toList();
        return PageUtil.convertPage(page, list);
    }

    @Override
    public void create(AreaCodeSaveParam param) {
        // 校验
        checkAreaCode(param, null);

        // 如果当前设置为默认，则修改其他默认
        if (Constants.YES.equals(param.getIsDefault())) {
            this.lambdaUpdate()
                    .set(AreaCode::getIsDefault, Constants.NO)
                    .eq(AreaCode::getIsDefault, Constants.YES)
                    .update();
        }

        AreaCode areaCode = param.createAreaCode();
        this.save(areaCode);
    }

    @Override
    public void update(AreaCodeEditParam param) {
        // 校验
        checkAreaCode(param, param.getId());

        // 如果当前设置为默认，则修改其他默认
        if (Constants.YES.equals(param.getIsDefault())) {
            this.lambdaUpdate()
                    .set(AreaCode::getIsDefault, Constants.NO)
                    .eq(AreaCode::getIsDefault, Constants.YES)
                    .update();
        }

        AreaCode areaCode = param.createAreaCode();
        this.updateById(areaCode);
    }

    /**
     * 校验区号
     */
    public void checkAreaCode(AreaCodeSaveParam param, Integer areaCodeId){
        long codeCount = this.count(new LambdaQueryWrapper<AreaCode>()
                .ne(null != areaCodeId, AreaCode::getId, areaCodeId)
                .eq(AreaCode::getCode, param.getCode()));
        if (codeCount > 0){
            throw new GlobalException(AREA_CODE_IS_EXISTS);
        }
        long nameCount = this.count(new LambdaQueryWrapper<AreaCode>()
                .ne(null != areaCodeId, AreaCode::getId, areaCodeId)
                .eq(AreaCode::getName, param.getCode()));
        if (nameCount > 0){
            throw new GlobalException(AREA_CODE_NAME_IS_EXISTS);
        }
    }

    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        // 校验字段名
        String field = updateField.getField();
        // 获取数据库的字段名
        field = getColumnByPropertyName(field);
        if (!Arrays.asList(allowFields).contains(field)){
            throw new GlobalException(INVALID_FIELD_VALUE);
        }

        // 如果当前设置为默认，则修改其他默认
        if (field.equals("is_default") && "1".equals(updateField.getVal())) {
            this.lambdaUpdate()
                    .set(AreaCode::getIsDefault, Constants.NO)
                    .eq(AreaCode::getIsDefault, Constants.YES)
                    .update();
        }

        // 构造更新条件
        UpdateWrapper<AreaCode> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(getKeyColumn(), updateField.getId()).set(field, updateField.getVal());

        return this.update(updateWrapper);
    }

    @Override
    public AreaCodeVO detail(Integer id) {
        if (id != null){
            AreaCode areaCode = this.getById(id);
            AreaCodeVO areaCodeVO = new AreaCodeVO();
            BeanUtils.copyProperties(areaCode, areaCodeVO);
            areaCodeVO.setLabel(StrUtil.format("+{}", areaCode.getCode()));
            return areaCodeVO;
        }
        return null;
    }

    @Override
    public List<AreaCodeVO> mobileAreaCode(){
        LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaCode::getIsAvailable, 1)
                .orderByDesc(AreaCode::getIsDefault);
        List<AreaCode> list = this.list(queryWrapper);
        return list.stream().map(item -> {
            AreaCodeVO vo = new AreaCodeVO();
            BeanUtils.copyProperties(item, vo);
            vo.setLabel(StrUtil.format("+{}", item.getCode()));
            return vo;
        }).toList();
    }
}
