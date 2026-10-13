// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.common.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.common.BaseMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.tigshop.common.constant.ResultTextConstants.INVALID_FIELD_VALUE;

/**
 * 基础服务类
 *
 * @author Tigshop团队
 * @create 2024年11月04日 17:16
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends MPJBaseServiceImpl<M, T> {

    /**
     * 通用的更新字段方法
     *
     * @param updateField UpdateFieldDTO
     * @param allowFields 允许更新的字段
     * @return boolean 是否更新成功
     */
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        // 校验字段名
        String field = updateField.getField();
        // 获取数据库的字段名
        field = getColumnByPropertyName(field);
        if (!Arrays.asList(allowFields).contains(field)){
            throw new GlobalException(INVALID_FIELD_VALUE);
        }
        // 构造更新条件
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(getKeyColumn(), updateField.getId())
                     .set(field, updateField.getVal());

        return this.update(updateWrapper);
    }

    /**
     * 通用的批量操作方法
     *
     * @param batchDTO BatchDTO
     * @return 操作影响的行数
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            return this.removeByIds(batchDTO.getIds());
        }
        return false;
    }

    /**
     * 通用的删除方法
     *
     * @param id id
     * @return boolean
     */
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        return this.removeById(id);
    }

    /**
     * 通用的排序方法
     *
     * @param page 分页对象
     */
    public <E> Page<E> buildSortOrder(BasePage page) {
        Page<E> resutlPage = new Page<>(page.getPage(), page.getSize());
        this.buildSortOrder(resutlPage, page);
        return resutlPage;
    }

    public <E> void buildSortOrder(Page<E> page, BasePage pageQuery) {
        if (pageQuery.getSortItems() == null || pageQuery.getSortItems().isEmpty()) {
            buildSortOrder(page, pageQuery.getSortField(), pageQuery.getSortOrder());
            return;
        }
        for (com.tigshop.common.core.entity.SortItem item : pageQuery.getSortItems()) {
            if (item == null || StrUtil.isEmpty(item.getField())) {
                continue;
            }
            String column = getColumnByPropertyName(item.getField());
            if ("asc".equalsIgnoreCase(item.getOrder())) {
                page.addOrder(OrderItem.asc(column));
            } else {
                page.addOrder(OrderItem.desc(column));
            }
        }
    }

    /**
     * 通用的排序方法
     *
     * @param page 分页对象
     * @param sortField 排序字段
     * @param sortOrder 排序方式
     */
    public <E> void buildSortOrder(Page<E> page, String sortField, String sortOrder) {
        // 获取主键字段（如果 sortField 为空，则使用主键）
        if (StrUtil.isEmpty(sortField) || ObjectUtil.equals(sortField, getKeyProperty())) {
            sortField = getKeyColumn();
        }
        // 设置排序
        if ("asc".equalsIgnoreCase(sortOrder)) {
            // 升序
            OrderItem build = OrderItem.asc(getColumnByPropertyName(sortField));
            page.setOrders(List.of(build));
        } else if ("desc".equalsIgnoreCase(sortOrder) || StrUtil.isEmpty(sortOrder)) {
            // 降序
            OrderItem build = OrderItem.desc(getColumnByPropertyName(sortField));
            page.setOrders(List.of(build));
        }
    }

    /**
     * 获取数据库的字段名
     * @param propertyName 属性名
     * @return 数据库字段名，如果找不到则返回主键字段（防止 SQL 注入）
     */
    public String getColumnByPropertyName(String propertyName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
        if (tableInfo.getKeyColumn().equals(propertyName)) {
            return tableInfo.getKeyColumn();
        }

        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        for (TableFieldInfo fieldInfo : fieldList) {
            //兼容传入数据库字段
            if (fieldInfo.getProperty().equals(propertyName) || fieldInfo.getColumn().equals(propertyName)) {
                // 获取数据库的主键字段
                return fieldInfo.getColumn();
            }
        }
        // 安全修复：找不到属性时返回主键字段，而非用户输入的原始值，防止 SQL 注入
        return tableInfo.getKeyColumn();
    }

    /**
     * 根据指定实体类获取数据库字段名（用于跨表排序场景）
     * @param entityClass 实体类
     * @param propertyName 属性名
     * @return 数据库字段名
     */
    public String getColumnByEntity(Class<?> entityClass, String propertyName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo == null) {
            return propertyName;
        }
        if (tableInfo.getKeyColumn().equals(propertyName) || tableInfo.getKeyProperty().equals(propertyName)) {
            return tableInfo.getKeyColumn();
        }
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        for (TableFieldInfo fieldInfo : fieldList) {
            if (fieldInfo.getProperty().equals(propertyName) || fieldInfo.getColumn().equals(propertyName)) {
                return fieldInfo.getColumn();
            }
        }
        return tableInfo.getKeyColumn();
    }

    /**
     * 获取主键字段
     * @return String
     */
    public String getKeyColumn(){
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
        if (tableInfo != null) {
            // 获取数据库的主键字段
            return tableInfo.getKeyColumn();
        }
        return null;
    }

    /**
     * 获取主键属性名
     * @return String
     */
    public String getKeyProperty(){
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
        if (tableInfo != null) {
            // 获取数据库的主键字段
            return tableInfo.getKeyProperty();
        }
        return null;
    }

    /**
     * 通用的排序方法
     *
     * @param queryWrapper 查询条件
     * @param sortField    排序字段
     * @param sortOrder    排序方式
     */
    public void buildSortOrder(MPJLambdaWrapper<T> queryWrapper, String sortField, String sortOrder) {
        // 设置排序
        if ("asc".equalsIgnoreCase(sortOrder)) {
            // 升序
            queryWrapper.orderByAsc(getColumnByPropertyName(sortField));
        } else if ("desc".equalsIgnoreCase(sortOrder) || StrUtil.isEmpty(sortOrder)) {
            // 降序
            queryWrapper.orderByDesc(getColumnByPropertyName(sortField));
        }
    }

    /**
     * 获取当前登录账号类型
     * @return Integer
     */
    public String getAdminType(){
        return HeaderUtils.getAdminType();
    }

    /**
     * 获取当前店铺ID
     * @return Integer
     */
    public Integer getShopId(){
        return HeaderUtils.getShopId();
    }

    /**
     * 获取当前供应商ID
     * @return Integer
     */
    public Integer getVendorId(){
        return HeaderUtils.getVendorId();
    }
}
