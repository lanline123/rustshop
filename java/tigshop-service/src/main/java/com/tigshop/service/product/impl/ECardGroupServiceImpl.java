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

package com.tigshop.service.product.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.ECardGroupCreateDTO;
import com.tigshop.bean.dto.product.ECardGroupListDTO;
import com.tigshop.bean.dto.product.ECardGroupUpdateDTO;
import com.tigshop.bean.enums.product.DeleteType;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.ECardGroup;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.product.ECardExportVO;
import com.tigshop.bean.vo.product.ECardGroupVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ECardGroupMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ECardGroupService;
import com.tigshop.service.product.ECardService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.product.ECardGroupConstants.*;

/**
 * 电子卡券组服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class ECardGroupServiceImpl extends BaseServiceImpl<ECardGroupMapper, ECardGroup> implements ECardGroupService {
    @Resource
    private final ProductMapper productMapper;
    @Resource
    private AdminLogService adminLogService;
    @Resource
    private ECardService eCardService;

    public ECardGroupServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Page<ECardGroupVO> list(ECardGroupListDTO listDTO, HttpServletResponse response) {
        // 分页
        Page<ECardGroup> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ECardGroup> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ECardGroup::getGroupName, keyword);
        }

        queryWrapper.eq(ECardGroup::getShopId, getShopId());

        if(listDTO.getIsDownload() != null && listDTO.getIsDownload() > 0) {
            //导出模板
            List<ECardExportVO> eCardExportVO = new ArrayList<>();
            eCardExportVO.add(new ECardExportVO());
            ExcelUtils<ECardExportVO> excelUtils = new ExcelUtils<>(eCardExportVO, ECardExportVO.class);
            excelUtils.exportExcelWeb(response);
        }

        // 执行查询
        Page<ECardGroup> eCardGroupPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ECardGroup> eCardGroupPageRecords = eCardGroupPage.getRecords();
        // 转换为VO
        List<ECardGroupVO> eCardGroupVOList = eCardGroupPageRecords.stream()
                .map(eCardGroup -> {
                    ECardGroupVO eCardGroupVO = new ECardGroupVO();
                    BeanUtils.copyProperties(eCardGroup, eCardGroupVO);
                    convert(eCardGroupVO, eCardGroup);
                    return eCardGroupVO;
                }).toList();
        return PageUtil.convertPage(eCardGroupPage, eCardGroupVOList);
    }

    @Override
    public ECardGroupVO detail(Integer id) {
        if (id != null) {
            ECardGroup eCardGroup = this.getById(id);
            ECardGroupVO eCardGroupVO = new ECardGroupVO();
            BeanUtils.copyProperties(eCardGroup, eCardGroupVO);
            convert(eCardGroupVO, eCardGroup);
            return eCardGroupVO;
        }
        return null;
    }

    /**
     * 格式转换
     * @param eCardGroupVO eCardGroupVO
     * @param eCardGroup eCardGroup
     */
    public void convert(ECardGroupVO eCardGroupVO,ECardGroup eCardGroup) {
        // 添加时间转换
        DateTime date = DateUtil.date(eCardGroup.getAddTime() * 1000);
        eCardGroupVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

        // 更新时间转换
        DateTime modifyDate = DateUtil.date(eCardGroup.getUpTime() * 1000);
        eCardGroupVO.setUpTime(DateUtil.format(modifyDate, "yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public boolean create(ECardGroupCreateDTO createDTO) {
        if (createDTO != null) {
            ECardGroup eCardGroup = new ECardGroup();
            BeanUtils.copyProperties(createDTO, eCardGroup);
            // 查询数据库，检查卡券组名称是否已存在
            checkCardGroupUnique(createDTO);
            // 添加时间
            eCardGroup.setAddTime(DateUtil.currentSeconds());
            // 更新时间
            eCardGroup.setUpTime(DateUtil.currentSeconds());
            eCardGroup.setShopId(getShopId());
            return this.save(eCardGroup);
        }
        return false;
    }

    /**
     * 验证唯一
     */
    public void checkCardGroupUnique(ECardGroupCreateDTO createDTO) {
        String groupName = createDTO.getGroupName();
        if (this.count(new LambdaQueryWrapper<ECardGroup>()
                .eq(ECardGroup::getGroupName, groupName)
                .select(ECardGroup::getGroupName)) > 0) {
            throw new GlobalException(GROUP_NAME_EXISTS);
        }
    }

    public Long getCount(Integer id) {
        return this.count(new LambdaQueryWrapper<ECardGroup>()
                .eq(ECardGroup::getGroupId, id)
                .select(ECardGroup::getGroupId));
    }

    @Override
    public boolean update(ECardGroupUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ECardGroup eCardGroup = new ECardGroup();

            Long count = getCount(updateDTO.getGroupId());
            if (count == 0) {
                throw new GlobalException(E_CARD_GROUP_NOT_EXISTS);
            }
            BeanUtils.copyProperties(updateDTO, eCardGroup);
            // 更新时间
            eCardGroup.setUpTime(DateUtil.currentSeconds());
            return this.updateById(eCardGroup);
        }
        return false;
    }

    @Override
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        // 商品中有关联了分组的上架商品不能删除
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCardGroupId, id)
                .eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode())
                .eq(Product::getProductStatus,1);
        List<Product> productList = productMapper.selectList(queryWrapper);
        if (!productList.isEmpty()){
            throw new GlobalException(GROUP_ID_BY_PRODUCT_EXISTS);
        }

        //新增日志
        adminLogService.createByLogInfo(StrUtil.format("删除电子卡券分组信息：id:{}", id));
        return this.removeById(id);
    }

    @Override
    public boolean batchImport(Integer groupId, MultipartFile file) {
       if( file == null) {
           throw new GlobalException(E_CARD_GROUP_FILE_NOT_NULL);
       }
       ExcelUtils<ECardExportVO> excelUtils = new ExcelUtils<>(file, ECardExportVO.class);
       List<ECardExportVO> list = excelUtils.importExcel();
       List<ECard> eCards = new ArrayList<>();
       for (ECardExportVO ecardExportVO : list) {
           ECard eCard = new ECard();
           eCard.setGroupId(groupId);
           eCard.setCardNumber(ecardExportVO.getCardNumber());
           eCard.setCardPwd(ecardExportVO.getCardPwd());
           eCard.setAddTime(StringUtils.getCurrentTime());
           eCards.add(eCard);
       }
       if (!eCards.isEmpty()) {
            eCardService.saveBatch(eCards);
       }
        return true;
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        if (ObjectUtil.equals(updateField.getField(), "isUse")) {
            log.info(updateField.getField());
            // 查看是否有已经上架的商品绑定过该卡组
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getCardGroupId, updateField.getId())
                    .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                    .eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode());
            if (!productMapper.selectList(queryWrapper).isEmpty()) {
                throw new GlobalException(GROUP_ID_BY_PRODUCT_EXISTS);
            }
        }
        return super.updateField(updateField, allowFields);
    }
}
