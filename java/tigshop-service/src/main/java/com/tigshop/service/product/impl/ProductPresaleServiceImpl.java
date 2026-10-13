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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.dto.product.ProductPresaleDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductPresale;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductPresaleMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductPresaleService;
import com.tigshop.service.product.ProductSkuService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 商品预售服务实现
 *
 * @author Tigshop团队
 * @create 2025年11月21日
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ProductPresaleServiceImpl extends BaseServiceImpl<ProductPresaleMapper, ProductPresale> implements ProductPresaleService {

    private final ProductPresaleMapper productPresaleMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductMapper productMapper;
    private final TigshopProperties tigshopProperties;
    private final ProductSkuService productSkuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(Integer productId, ProductPresaleDTO presaleDTO, BigDecimal productPrice) {
        if (presaleDTO == null) {
            return true;
        }

        // 查询是否已存在预售记录
        ProductPresale existPresale = productPresaleMapper.selectByProductId(productId);

        // 构建预售实体
        ProductPresale presale = new ProductPresale();
        BeanUtils.copyProperties(presaleDTO, presale);

        // 用于更新时显式置空的字段
        LambdaUpdateWrapper<ProductPresale> updateWrapper = null;
        if (existPresale != null) {
            updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ProductPresale::getProductId, productId);
        }

        // 尾款支付时间：根据类型清理另一种类型的字段
        if (presaleDTO.getFinalPaymentType() != null) {
            if (presaleDTO.getFinalPaymentType() == 1) {
                // 相对时间：清空指定日期字段
                presale.setFinalPaymentStartTime(null);
                presale.setFinalPaymentEndTime(null);
                if (updateWrapper != null) {
                    updateWrapper.set(ProductPresale::getFinalPaymentStartTime, null);
                    updateWrapper.set(ProductPresale::getFinalPaymentEndTime, null);
                }
            } else if (presaleDTO.getFinalPaymentType() == 2) {
                // 指定日期：清空相对时间字段
                presale.setFinalPaymentRelativeDays(null);
                presale.setFinalPaymentDurationDays(null);
                presale.setFinalPaymentDurationHours(null);
                if (updateWrapper != null) {
                    updateWrapper.set(ProductPresale::getFinalPaymentRelativeDays, null);
                    updateWrapper.set(ProductPresale::getFinalPaymentDurationDays, null);
                    updateWrapper.set(ProductPresale::getFinalPaymentDurationHours, null);
                }
                if (!StringUtils.isEmpty(presaleDTO.getFinalPaymentStartTime())) {
                    presale.setFinalPaymentStartTime(TigUtils.toTimestampYmdHm(presaleDTO.getFinalPaymentStartTime()));
                }
                if (!StringUtils.isEmpty(presaleDTO.getFinalPaymentEndTime())) {
                    presale.setFinalPaymentEndTime(TigUtils.toTimestampYmdHm(presaleDTO.getFinalPaymentEndTime()));
                }
            }
        }

        // 发货时间：根据类型清理另一种类型的字段
        if (presaleDTO.getDeliveryType() != null) {
            if (presaleDTO.getDeliveryType() == 1) {
                // 相对时间：清空指定日期字段
                presale.setDeliveryDate(null);
                if (updateWrapper != null) {
                    updateWrapper.set(ProductPresale::getDeliveryDate, null);
                }
            } else if (presaleDTO.getDeliveryType() == 2) {
                // 指定日期：清空相对时间字段
                presale.setDeliveryRelativeDays(null);
                presale.setDeliveryRelativeHours(null);
                if (updateWrapper != null) {
                    updateWrapper.set(ProductPresale::getDeliveryRelativeDays, null);
                    updateWrapper.set(ProductPresale::getDeliveryRelativeHours, null);
                }
                if (!StringUtils.isEmpty(presaleDTO.getDeliveryDate())) {
                    presale.setDeliveryDate(TigUtils.toTimestampYmd(presaleDTO.getDeliveryDate()));
                }
            }
        }

        presale.setProductId(productId);

        // 设置时间戳
        long currentTime = StringUtils.getCurrentTime();
        if (existPresale != null) {
            presale.setPresaleId(existPresale.getPresaleId());
            presale.setCreateTime(existPresale.getCreateTime());
            presale.setUpdateTime(currentTime);
            return this.update(presale, updateWrapper);
        } else {
            presale.setCreateTime(currentTime);
            presale.setUpdateTime(currentTime);
            return this.save(presale);
        }
    }

    @Override
    public ProductPresale getByProductId(Integer productId) {
        return productPresaleMapper.selectByProductId(productId);
    }

    @Override
    public ProductPresaleVO getPresaleVOByProductId(Integer productId) {
        ProductPresale presale = productPresaleMapper.selectByProductId(productId);
        if (presale == null) {
            return null;
        }

        // 转换为VO
        ProductPresaleVO vo = new ProductPresaleVO();
        BeanUtils.copyProperties(presale, vo);


        String startTimeStr = TigUtils.handelTime(presale.getFinalPaymentStartTime(),"yyyy-MM-dd HH:mm");
        String endTimeStr = TigUtils.handelTime(presale.getFinalPaymentEndTime(),"yyyy-MM-dd HH:mm");
        vo.setFinalPaymentStartTime(startTimeStr);
        vo.setFinalPaymentEndTime(endTimeStr);
        vo.setDeliveryDate(TigUtils.handelTime(presale.getDeliveryDate(),"yyyy-MM-dd"));

        vo.setPresale(presale);

        // 设置销售模式文本
        if (presale.getSaleMode() != null) {
            if (presale.getSaleMode() == 1) {
                vo.setSaleModeText("全款预售");
            } else if (presale.getSaleMode() == 2) {
                vo.setSaleModeText("定金预售");
            }
        }

        // 如果是定金预售，实时计算定金金额
        if (presale.getSaleMode() == 2) {
            BigDecimal depositAmount = calculateDepositAmount(productId,null, presale);
            vo.setDepositAmount(depositAmount);
            vo.setFinalPaymentAmount(getBasePrice(productId).subtract(depositAmount).max(BigDecimal.ZERO));
        }

        return vo;
    }

    @Override
    public boolean deleteByProductId(Integer productId) {
        int deleted = productPresaleMapper.deleteByProductId(productId);
        return deleted > 0;
    }


    @Override
    public BigDecimal calculateDepositAmount(Integer productId, Integer skuId, ProductPresale presaleConfig) {
        if (presaleConfig == null || presaleConfig.getSaleMode() != 2) {
            return BigDecimal.ZERO;
        }


        // 1. 根据定金类型计算
        BigDecimal depositAmount;
        if (presaleConfig.getDepositType() == 1) {
            BigDecimal basePrice;
            if(skuId==null || skuId<=0){
                basePrice =   getBasePrice(productId);
            }else{
                ProductSku one = productSkuService.lambdaQuery().
                        eq(ProductSku::getSkuId, skuId).one();
                if(Objects.isNull(one)){
                    throw new GlobalException("商品SKU不存在");
                }
                if (Constants.NO.equals(one.getIsEnabled())) {
                    throw new GlobalException("商品规格已禁用，请重新选择");
                }
                basePrice = one.getSkuPrice();
            }

            // 按百分比收取
            depositAmount = basePrice
                    .multiply(new BigDecimal(presaleConfig.getDepositRatio()))
                    .divide(new BigDecimal(100), 2, RoundingMode.DOWN);

            // 3. 是否向下取整
            if (presaleConfig.getIsRoundDown() != null && presaleConfig.getIsRoundDown() == 1) {
                depositAmount = depositAmount.setScale(0, RoundingMode.DOWN);
            }
        } else {
            // 按固定金额收取
            depositAmount = presaleConfig.getDepositFixedAmount();
        }

        return depositAmount;
    }


    /**
     * 获取基础价格（用于计算定金）
     * 如果有SKU，取最低价；如果没有SKU，使用商品价格
     *
     * @param productId 商品ID
     * @return 基础价格
     */
    public BigDecimal getBasePrice(Integer productId) {
        // 查询商品所有SKU的最低价格
        BigDecimal minSkuPrice = productSkuMapper.selectMinPriceByProductId(productId);

        if (minSkuPrice != null && minSkuPrice.compareTo(BigDecimal.ZERO) > 0) {
            return minSkuPrice;
        }

        // 没有SKU或SKU价格为空，使用商品表的价格
        Product product = productMapper.selectById(productId);
        if (product != null && product.getProductPrice() != null) {
            return product.getProductPrice();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public void validatePresaleConfig(Integer saleMode, ProductPresaleDTO presaleDTO, BigDecimal productPrice) {
        if (saleMode == null || saleMode == 0) {
            // 现货销售，无需校验
            return;
        }

        if (presaleDTO == null) {
            throw new GlobalException("预售商品必须配置预售信息");
        }

        presaleDTO.initTimestamp();

        // 校验全款预售
        if (saleMode == 1) {
            validateFullPaymentPresale(presaleDTO);
        }

        // 校验定金预售
        if (saleMode == 2) {
            validateDepositPresale(presaleDTO, productPrice);
        }
    }

    /**
     * 校验全款预售配置
     */
    private void validateFullPaymentPresale(ProductPresaleDTO presaleDTO) {
        // 校验发货时间配置
        if (presaleDTO.getDeliveryType() == null) {
            throw new GlobalException("请设置发货时间");
        }

        if (presaleDTO.getDeliveryType() == 1) {
            // 相对时间
            if (presaleDTO.getDeliveryRelativeDays() == null || presaleDTO.getDeliveryRelativeDays() < 0) {
                throw new GlobalException("请设置正确的发货天数");
            }
        } else if (presaleDTO.getDeliveryType() == 2) {
            // 指定日期
            if (presaleDTO.getDeliveryDate() == null || presaleDTO.getDeliveryDateTimestamp() == null || presaleDTO.getDeliveryDateTimestamp() <= System.currentTimeMillis() / 1000) {
                throw new GlobalException("发货日期必须大于当前时间");
            }
        }
    }

    /**
     * 校验定金预售配置
     */
    private void validateDepositPresale(ProductPresaleDTO presaleDTO, BigDecimal productPrice) {
        // 校验定金类型
        if (presaleDTO.getDepositType() == null) {
            throw new GlobalException("请选择定金收取方式");
        }

        // 按百分比收取
        if (presaleDTO.getDepositType() == 1) {
            if (presaleDTO.getDepositRatio() == null || presaleDTO.getDepositRatio() < 1 || presaleDTO.getDepositRatio() > 99) {
                throw new GlobalException("定金比例必须在1-99之间");
            }
        }

        // 按固定金额收取
        if (presaleDTO.getDepositType() == 2) {
            if (presaleDTO.getDepositFixedAmount() == null ||
                    presaleDTO.getDepositFixedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new GlobalException("固定定金金额必须大于0");
            }

            // 校验定金金额是否小于商品价格
            if (productPrice != null && presaleDTO.getDepositFixedAmount().compareTo(productPrice) >= 0) {
                throw new GlobalException("定金金额不能大于等于商品售价");
            }
        }

        // 校验尾款支付时间配置（仅用于展示）
        if (presaleDTO.getFinalPaymentType() == null) {
            throw new GlobalException("请设置尾款支付时间");
        }

        presaleDTO.initTimestamp();

        if (presaleDTO.getFinalPaymentType() == 1) {
            // 相对时间
            if (presaleDTO.getFinalPaymentRelativeDays() == null || presaleDTO.getFinalPaymentRelativeDays() < 0) {
                throw new GlobalException("请设置正确的尾款支付相对天数");
            }
            if ((presaleDTO.getFinalPaymentDurationDays() == null || presaleDTO.getFinalPaymentDurationDays() <= 0)
                    && (presaleDTO.getFinalPaymentDurationHours() == null || presaleDTO.getFinalPaymentDurationHours() <= 0)) {
                throw new GlobalException("请设置正确的尾款支付期限");
            }
        } else if (presaleDTO.getFinalPaymentType() == 2) {
            // 固定时间
            if (presaleDTO.getFinalPaymentStartTime() == null ||
                    presaleDTO.getFinalPaymentEndTime() == null) {
                throw new GlobalException("请设置尾款支付时间范围");
            }
            if (presaleDTO.getFinalPaymentStartTimeStamp() == null || presaleDTO.getFinalPaymentEndTimeStamp() == null ||
                    presaleDTO.getFinalPaymentStartTimeStamp() >= presaleDTO.getFinalPaymentEndTimeStamp()) {
                throw new GlobalException("尾款支付开始时间必须小于结束时间");
            }
        }

        // 校验发货时间配置
        if (presaleDTO.getDeliveryType() == null) {
            throw new GlobalException("请设置发货时间");
        }
    }
}
