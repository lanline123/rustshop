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

package com.tigshop.service.order.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.AftersalesLogCreateDTO;
import com.tigshop.bean.dto.order.AftersalesLogListDTO;
import com.tigshop.bean.dto.order.AftersalesLogUpdateDTO;
import com.tigshop.bean.dto.order.ReturnPicDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.order.AftersalesLog;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.AftersalesLogVO;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.order.AftersalesLogMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 售后记录服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class AftersalesLogServiceImpl extends BaseServiceImpl<AftersalesLogMapper, AftersalesLog> implements AftersalesLogService {

    private static final String AUTO_APPROVE_LOG_PREFIX = "系统自动审核通过";
    private static final String AUTO_RECEIVE_LOG_PREFIX = "系统自动确认收货";
    private static final String AUTO_FINANCE_LOG = "财务审核已关闭，系统已自动执行退款处理";

    private final TranslatePackageImpl translatePackage;
    private final AdminUserService adminUserService;

    @Override
    public ListResVO<AftersalesLogVO, AftersalesLogListDTO> list(AftersalesLogListDTO listDTO) {
        // 分页
        Page<AftersalesLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AftersalesLog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("aftersales_log", keyword);
        }*/
        // 执行查询
        Page<AftersalesLog> aftersalesLogPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<AftersalesLog> aftersalesLogPageRecords = aftersalesLogPage.getRecords();
        // 转换为VO
        List<AftersalesLogVO> aftersalesLogVOList = aftersalesLogPageRecords.stream()
                .map(aftersalesLog -> {
                    AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
                    BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
                    return aftersalesLogVO;
                }).toList();
        return new ListResVO<>(aftersalesLogVOList, listDTO, aftersalesLogPage.getTotal());
    }

    @Override
    public AftersalesLogVO detail(Integer id) {
        if (id != null) {
            AftersalesLog aftersalesLog = this.getById(id);
            AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
            BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
            return aftersalesLogVO;
        }
        return new AftersalesLogVO();
    }

    @Override
    public boolean create(AftersalesLogCreateDTO createDTO) {
        if (createDTO != null) {
            AftersalesLog aftersalesLog = new AftersalesLog();
            BeanUtils.copyProperties(createDTO, aftersalesLog);
            return this.save(aftersalesLog);
        }
        return false;
    }

    @Override
    public boolean update(AftersalesLogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            AftersalesLog aftersalesLog = new AftersalesLog();
            BeanUtils.copyProperties(updateDTO, aftersalesLog);
            return this.updateById(aftersalesLog);
        }
        return false;
    }

    @Override
    public boolean addAftersaleLog(Integer aftersalesId, String logInfo) {
        return addAftersaleLog(aftersalesId, logInfo, null, null);
    }

    /**
     * shopId / vendorId 入参仅为兼容既有调用方保留，不再参与归属判定。
     * 调用方传的是售后单所属店铺 / 供应商，会把平台的操作误记成店铺或供应商。
     */
    @Override
    public boolean addAftersaleLog(Integer aftersalesId, String logInfo, Integer shopId, Integer vendorId) {
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersalesId);
        aftersalesLog.setLogInfo(logInfo);
        if (SecurityUtils.isAdminUser()) {
            aftersalesLog.setAdminName(SecurityUtils.getCurrentUsername());
        } else {
            aftersalesLog.setUserName(SecurityUtils.getCurrentUsername());
        }
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        applyOperatorScope(aftersalesLog);
        return this.save(aftersalesLog);
    }

    /**
     * 系统留痕没有操作人，归属由调用方按「这条代做的是哪个环节」声明：
     * 代供应商环节传其 vendorId，代店铺环节传其 shopId，平台自身职能两者都传 0。
     */
    @Override
    public boolean addSystemAftersaleLog(Integer aftersalesId, String logInfo, Integer shopId, Integer vendorId) {
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersalesId);
        aftersalesLog.setLogInfo(logInfo);
        aftersalesLog.setAdminName(getSystemAdminName());
        aftersalesLog.setUserName("");
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        aftersalesLog.setShopId(normalizeScopeId(shopId));
        aftersalesLog.setVendorId(normalizeScopeId(vendorId));
        return this.save(aftersalesLog);
    }

    private Integer normalizeScopeId(Integer scopeId) {
        return scopeId == null || scopeId < 0 ? 0 : scopeId;
    }

    /**
     * 人工操作的协商记录归属取「操作人」而非售后单所属方：供应商管理员归其供应商，
     * 店铺 / 门店 / 自提点 / POS 管理员归其店铺，平台管理员归 0（前端显示为平台）。
     * 该字段只用于前端判定协商记录头像，不参与任何数据权限过滤。
     */
    private void applyOperatorScope(AftersalesLog aftersalesLog) {
        aftersalesLog.setShopId(0);
        aftersalesLog.setVendorId(0);
        AdminTypeEnum adminType = currentAdminType();
        if (adminType == null || adminType == AdminTypeEnum.ADMIN) {
            return;
        }
        if (adminType == AdminTypeEnum.VENDOR) {
            aftersalesLog.setVendorId(currentScopeId(true));
        } else {
            aftersalesLog.setShopId(currentScopeId(false));
        }
    }

    private AdminTypeEnum currentAdminType() {
        try {
            String adminType = getAdminType();
            return StrUtil.isBlank(adminType) ? null : AdminTypeEnum.fromCode(adminType);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer currentScopeId(boolean vendorScope) {
        try {
            Integer scopeId = vendorScope ? getVendorId() : getShopId();
            return scopeId == null || scopeId < 0 ? 0 : scopeId;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String getSystemAdminName() {
        AdminUser systemAdminUser = this.getSystemAdminUser();
        return systemAdminUser == null ? "" : systemAdminUser.getUsername();
    }

    private AdminUser getSystemAdminUser() {
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getAdminType, AdminTypeEnum.ADMIN.getCode())
                .orderByAsc(AdminUser::getAdminId)
                .last("limit 1");
        return adminUserService.getOne(queryWrapper, false);
    }

    private boolean isSystemAutoLog(AftersalesLog aftersalesLog) {
        String logInfo = aftersalesLog.getLogInfo();
        return StrUtil.contains(logInfo, AUTO_APPROVE_LOG_PREFIX)
                || StrUtil.contains(logInfo, AUTO_RECEIVE_LOG_PREFIX)
                || StrUtil.contains(logInfo, AUTO_FINANCE_LOG);
    }

    private void normalizeLog(AftersalesLog aftersalesLog, AftersalesLogVO aftersalesLogVO) {
        // 不再用售后单所属店铺 / 供应商回填归属：那会把平台和系统的操作重新算到供应商头上
        if (isSystemAutoLog(aftersalesLog) && StrUtil.isBlank(aftersalesLogVO.getAdminName())) {
            aftersalesLogVO.setAdminName(getSystemAdminName());
            aftersalesLogVO.setUserName("");
        }
    }

    @Override
    public List<AftersalesLogVO> getDetailLog(Integer id) {
        LambdaQueryWrapper<AftersalesLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AftersalesLog::getAftersaleId, id);
        queryWrapper.orderByAsc(AftersalesLog::getLogId);
        List<AftersalesLog> afterSalesLogs = this.list(queryWrapper);
        this.translateForLogInfo(afterSalesLogs);
        return getList(afterSalesLogs);
    }

    private List<AftersalesLog> translateForLogInfo(List<AftersalesLog> afterSalesLogs) {
        return afterSalesLogs.stream().peek(afterSalesLogVO -> {
            if (StrUtil.isNotBlank(afterSalesLogVO.getLogInfo()) && afterSalesLogVO.getLogInfo().contains(":")) {
                String[] splitLogInfo = afterSalesLogVO.getLogInfo().split(":");
                if (splitLogInfo.length > 0) {
                    afterSalesLogVO.setLogInfo(translatePackage.translate(splitLogInfo[0].trim()) + (splitLogInfo.length >= 2 && StrUtil.isNotBlank(splitLogInfo[1])? ":" + translatePackage.translate(splitLogInfo[1].trim()) : ":") );
                }
            }
            afterSalesLogVO.setLogInfo(translatePackage.translate(afterSalesLogVO.getLogInfo()));
        }).toList();
    }

    public List<AftersalesLogVO> getList(List<AftersalesLog> afterSalesLogs) {
        return afterSalesLogs.stream().map(aftersalesLog -> {
            AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
            BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
            if (aftersalesLog.getReturnPic() != null && !"[]".equals(aftersalesLog.getReturnPic())) {
                aftersalesLogVO.setReturnPic(JsonUtil.jsonToList(aftersalesLog.getReturnPic(), ReturnPicDTO.class));
            } else {
                aftersalesLogVO.setReturnPic(new ArrayList<>());
            }
            aftersalesLogVO.setAddTime(TigUtils.handelTime(Long.parseLong(aftersalesLog.getAddTime())));
            normalizeLog(aftersalesLog, aftersalesLogVO);
            return aftersalesLogVO;
        }).toList();
    }
}
