<template>
    <div class="staff-table-wrapper">
        <a-spin :spinning="loading">
            <el-table :data="data" @selection-change="handleSelectionChange">
                <!-- 多选列 -->
                <el-table-column v-if="showSelection" type="selection" width="32" />
                <!-- 会员账号 -->
                <el-table-column :label="$t('会员账号')">
                    <template #default="{ row }">
                        {{ row.memberInfo?.username || row.nickname || "-" }}
                    </template>
                </el-table-column>
                <!-- 头像/姓名 -->
                <el-table-column :label="$t('头像/姓名')" min-width="100">
                    <template #default="{ row }">
                        <div class="avatar-username">
                            <el-avatar class="avatar-fixed" :src="row.avatar" :size="30">
                                <i class="el-icon-user"></i>
                            </el-avatar>
                            <div>{{ row.name }}</div>
                        </div>
                    </template>
                </el-table-column>
                <!-- 联系方式 -->
                <el-table-column prop="phone" :label="$t('联系方式')" min-width="160">
                    <template #default="{ row }">
                        <StaffMobileCard :key="`${row.id}-${row.phone ?? ''}`" :id="row.id" :mobile="row.phone" />
                    </template>
                </el-table-column>
                <!-- 账号状态 -->
                <el-table-column :label="$t('账号状态')" width="120" align="center">
                    <template #default="{ row }">
                        <Switch
                            :checked="row.status"
                            :switchingIndex="1"
                            :params="{ id: row.id }"
                            :requestApi="toggleStatusApi"
                            :beforeChange="() => handleBeforeStatusChange(row)"
                            @callback="handleStatusChangeCallback"
                        />
                    </template>
                </el-table-column>
                <!-- 所属门店 -->
                <!-- <el-table-column prop="storeName" label="所属门店" /> -->
                <!-- 服务状态 -->
                <el-table-column :label="staffType === 'deliveryman' ? $t('配送员状态') : $t('服务员状态')" width="120" align="center">
                    <template #default="{ row }">
                        <span :class="{ 'off-duty-text': row.workStatus === 0 }">
                            {{ row.workStatusName }}
                        </span>
                    </template>
                </el-table-column>
                <!-- 添加时间 -->
                <el-table-column prop="createTime" :label="$t('添加时间')" :min-width="150" />
                <!-- 操作列 -->
                <el-table-column :label="$t('操作')" width="180" fixed="right">
                    <template #default="{ row }">
                        <DialogForm
                            :params="{ id: row.id, act: 'edit' }"
                            isDrawer
                            :path="staffType === 'deliveryman' ? 'organize/deliveryman/Info' : 'organize/serviceman/Info'"
                            :title="staffType === 'deliveryman' ? $t('编辑配送员') : $t('编辑服务员')"
                            width="650px"
                            @okCallback="handleEditSuccess"
                        >
                            <el-button type="primary" link>{{ $t('编辑') }}</el-button>
                        </DialogForm>
                        <el-divider direction="vertical" />
                        <DialogForm
                            :params="{ id: row.id }"
                            isDrawer
                            :show-footer="false"
                            :path="staffType === 'deliveryman' ? 'organize/deliveryman/Detail' : 'organize/serviceman/Detail'"
                            :title="staffType === 'deliveryman' ? $t('配送员详情') : $t('服务员详情')"
                            :width="staffType === 'deliveryman' ? '1200px' : '1200px'"
                        >
                            <el-button type="primary" link>{{ $t('详情') }}</el-button>
                        </DialogForm>
                        <el-divider direction="vertical" />
                        <DeleteRecord
                            :params="{ id: row.id }"
                            :requestApi="deleteApi"
                            :title="props.staffType === 'deliveryman' ? $t('确认删除该配送员吗？删除后不可恢复！') : $t('确认删除该服务员吗？删除后不可恢复！')"
                            @afterDelete="handleDeleteSuccess"
                        > {{ $t('删除') }} </DeleteRecord>
                    </template>
                </el-table-column>

                <!-- 空状态 -->
                <template #empty>
                    <div class="empty-warp">
                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                    </div>
                </template>
            </el-table>
        </a-spin>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { Modal } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { Switch, DeleteRecord } from "@/components/list";
import StaffMobileCard from "@/components/organize/StaffMobileCard.vue";
import { toggleStaffStatus, deleteServiceStaff, type ServiceStaffVO } from "@/api/organize/serviceStaff";

/**
 * 人员表格组件
 * 功能：展示配送员/服务员列表数据
 */

// ===== Props =====
import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    data: ServiceStaffVO[];
    loading?: boolean;
    staffType: "deliveryman" | "serviceman";
    showSelection?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    loading: false,
    showSelection: true
});

// ===== 计算属性 =====
/**
 * 状态切换 API（统一使用 serviceStaff 接口）
 */
const toggleStatusApi = computed(() => toggleStaffStatus);

/**
 * 删除 API（统一使用 serviceStaff 接口）
 */
const deleteApi = computed(() => deleteServiceStaff);

// ===== Emits =====
const emit = defineEmits<{
    "selection-change": [selectedItems: ServiceStaffVO[]];
    "edit-success": [];
}>();

// ===== 方法 =====

/**
 * 删除成功回调
 */
const handleDeleteSuccess = () => {
    emit("edit-success");
};

/**
 * 选择变化
 */
const handleSelectionChange = (selection: ServiceStaffVO[]) => {
    emit("selection-change", selection);
};

/**
 * 编辑成功回调
 */
const handleEditSuccess = () => {
    emit("edit-success");
};

/**
 * 状态切换成功回调
 */
const handleStatusChangeCallback = () => {
    emit("edit-success");
};

/**
 * 状态切换前确认
 */
const handleBeforeStatusChange = (row: ServiceStaffVO): Promise<boolean> => {
    return new Promise((resolve) => {
        const staffName = props.staffType === "deliveryman" ? t('配送员') : t('服务员');
        const actionText = row.status ? t('禁用') : t('启用');
        Modal.confirm({
            zIndex: 2009,
            title: t('确认{actionText}该{staffName}吗？', { actionText, staffName }),
            okType: "danger",
            maskClosable: true,
            onOk() {
                resolve(true);
            },
            onCancel() {
                resolve(false);
            }
        });
    });
};
</script>

<style lang="less" scoped>
.off-duty-text {
    color: #999;
}

.avatar-username {
    display: flex;
    align-items: center;
    gap: 8px;

    .avatar-fixed {
        flex-shrink: 0;
    }
}
</style>
