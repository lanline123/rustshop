<template>
    <div class="record-table-wrapper">
        <a-spin :spinning="loading">
            <el-table :data="data">
                <!-- 配送员展示字段 -->
                <template v-if="recordType === 'delivery'">
                    <el-table-column prop="staffName" :label="$t('配送员名称')" min-width="120" />
                    <el-table-column :label="$t('配送员电话')" min-width="130">
                        <template #default="{ row }">
                            <StaffMobileCard :id="getStaffId(row)" :mobile="getStaffPhone(row)" />
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('订单编号')" min-width="200" class-name="order-sn-column">
                        <template #default="{ row }">
                            <div class="order-sn-text">{{ row?.orderSn ?? "-" }}</div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('配送费')" min-width="110">
                        <template #default="{ row }">
                            <span v-if="row?.estimatedServiceFee != null && Number(row.estimatedServiceFee) > 0">{{ priceFormat(row.estimatedServiceFee) }}</span>
                            <span v-else>-</span>
                        </template>
                    </el-table-column>

                    <el-table-column prop="consignee" :label="$t('收货人')" min-width="110" />
                    <el-table-column :label="$t('收货人电话')" min-width="130">
                        <template #default="{ row }">
                            <!-- 收货人电话通常无可用 staffId：仅脱敏展示，不触发“查看明文”接口 -->
                            <StaffMobileCard :mobile="row?.mobile" />
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('商品数量')" align="center" min-width="90">
                        <template #default="{ row }">
                            {{ getProductQuantity(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('商品总价')" align="right" min-width="110">
                        <template #default="{ row }">
                            {{ getTotalPrice(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('配送起点')" min-width="220" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ formatPickupAddress(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('配送终点')" min-width="240" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ formatDeliveryAddress(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column prop="statusName" :label="$t('配送状态')" align="center" min-width="90">
                        <template #default="{ row }">
                            {{ row?.statusName || "-" }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('配送距离')" align="center" min-width="110">
                        <template #default="{ row }">
                            {{ formatDistance(row?.distance) }}
                        </template>
                    </el-table-column>

                    <el-table-column prop="createTime" :label="$t('下单时间')" min-width="160">
                        <template #default="{ row }">{{ row?.createTime ?? "-" }}</template>
                    </el-table-column>

                    <el-table-column prop="completeTime" :label="$t('完成时间')" min-width="160">
                        <template #default="{ row }">{{ row?.completeTime ?? "-" }}</template>
                    </el-table-column>

                    <el-table-column :label="$t('客户备注')" min-width="180" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ formatBuyerNote(row?.orderInfo?.buyerNote ?? row?.customerRemark) }}
                        </template>
                    </el-table-column>
                </template>

                <!-- 服务员展示字段 -->
                <template v-else>
                    <el-table-column prop="staffName" :label="$t('服务员名称')" min-width="120" />
                    <el-table-column :label="$t('服务员电话')" min-width="150">
                        <template #default="{ row }">
                            <StaffMobileCard :id="getStaffId(row)" :mobile="getStaffPhone(row)" />
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('订单编号')" min-width="200" class-name="order-sn-column">
                        <template #default="{ row }">
                            <div class="order-sn-text">{{ row?.orderSn ?? "-" }}</div>
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('服务费')" min-width="110">
                        <template #default="{ row }">
                            <span v-if="row?.estimatedServiceFee != null && Number(row.estimatedServiceFee) > 0">{{ priceFormat(row.estimatedServiceFee) }}</span>
                            <span v-else>-</span>
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('会员名称')" min-width="120">
                        <template #default="{ row }">
                            {{ getMemberName(row) }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('会员电话')" min-width="130">
                        <template #default="{ row }">
                            <!-- 会员/联系人电话通常无可用 staffId：仅脱敏展示，不触发“查看明文”接口 -->
                            <StaffMobileCard :mobile="getMemberPhoneRaw(row) ?? getMemberPhoneText(row)" />
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('服务地址')" min-width="240" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ formatServiceAddress(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('商品数量')" align="center" min-width="90">
                        <template #default="{ row }">
                            {{ getProductQuantity(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('订单金额')" align="right" min-width="110">
                        <template #default="{ row }">
                            {{ getOrderAmount(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column prop="statusName" :label="$t('服务状态')" align="center" min-width="90">
                        <template #default="{ row }">
                            {{ row?.statusName || "-" }}
                        </template>
                    </el-table-column>

                    <el-table-column prop="createTime" :label="$t('下单时间')" min-width="160">
                        <template #default="{ row }">{{ row?.createTime ?? "-" }}</template>
                    </el-table-column>

                    <el-table-column :label="$t('服务开始时间')" min-width="160">
                        <template #default="{ row }">
                            {{ getServiceStartTime(row) }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('服务结束时间')" min-width="160">
                        <template #default="{ row }">
                            {{ getServiceEndTime(row) }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('服务总时长')" align="center" min-width="120">
                        <template #default="{ row }">
                            {{ getServiceDuration(row) }}
                        </template>
                    </el-table-column>

                    <el-table-column :label="$t('客户备注')" min-width="180" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ formatBuyerNote(row?.orderInfo?.buyerNote) }}
                        </template>
                    </el-table-column>
                </template>

                <!-- 操作列 -->
                <el-table-column :label="$t('操作')" fixed="right" align="center">
                    <template #default="{ row }">
                        <DialogForm
                            :params="{ act: 'detail', id: row.orderId }"
                            :showClose="false"
                            :showOnOk="false"
                            :title="$t('订单详情 ') + row.orderSn"
                            isDrawer
                            path="order/order/Info"
                            width="880px"
                        >
                            <el-button type="primary" link>{{ $t('详情') }}</el-button>
                        </DialogForm>
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
import { DialogForm } from "@/components/dialog";
import type { DeliveryOrderVO } from "@/api/organize/serviceStaff";
import type { ServiceRecordFilterState } from "@/types/organize/serviceRecord";
import { priceFormat } from "@/utils/format";
import dayjs from "dayjs";
import { formatDuration } from "@/utils/time";
import StaffMobileCard from "@/components/organize/StaffMobileCard.vue";

/**
 * 记录表格组件
 * 功能：展示配送记录/服务记录列表数据
 */

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    data: DeliveryOrderVO[] | ServiceRecordFilterState[];
    loading?: boolean;
    recordType: "delivery" | "service";
}

const props = withDefaults(defineProps<Props>(), {
    loading: false
});

/**
 * 兼容不同列表数据结构：抽取员工ID/员工电话
 */
const getStaffId = (row: any): number | undefined => {
    const id = row?.staffId ?? row?.servicemanId ?? row?.deliverymanId;
    const n = Number(id);
    return Number.isFinite(n) && n > 0 ? n : undefined;
};

const getStaffPhone = (row: any): string | undefined => {
    return row?.staffPhone ?? row?.servicemanPhone ?? row?.phone;
};

/**
 * 配送距离格式化：接口通常返回 meters（m）
 */
const formatDistance = (distance?: number | string): string => {
    if (!(distance === 0 || distance)) return "-";
    const val = Number(distance);
    if (Number.isNaN(val)) return "-";
    return val >= 1000 ? `${(val / 1000).toFixed(2)} km` : `${val} m`;
};

/**
 * 订单买家备注：空字符串/空白字符串时统一展示 "-"
 */
const formatBuyerNote = (buyerNote?: string): string => {
    const val = typeof buyerNote === "string" ? buyerNote.trim() : "";
    return val ? val : "-";
};

/**
 * 配送起点：优先 pickupRegionNames + pickupAddress；兜底 shopName
 */
const formatPickupAddress = (row: any): string => {
    const parts = [row?.pickupRegionNames, row?.pickupAddress].filter(Boolean);
    if (parts.length > 0) return parts.join(" ");
    return row?.shopName || "-";
};

/**
 * 配送终点：优先 regionNames + address
 */
const formatDeliveryAddress = (row: any): string => {
    const parts = [row?.regionNames, row?.address].filter(Boolean);
    return parts.length > 0 ? parts.join(" ") : "-";
};

/**
 * 服务地址：优先 regionNames + address；兜底 serviceAddress
 */
const formatServiceAddress = (row: any): string => {
    const parts = [row?.regionNames, row?.address].filter(Boolean);
    if (parts.length > 0) return parts.join(" ");
    return row?.serviceAddress || "-";
};

/**
 * 商品数量：优先 orderInfo.productQuantity，其次 productCount / productQuantity
 */
const getProductQuantity = (row: any): string => {
    // 优先：遍历 orderItems 累加 quantity
    const items = row?.orderItems;
    if (Array.isArray(items)) {
        const sum = items.reduce((acc: number, item: any) => {
            const q = item?.quantity ?? item?.productQuantity ?? item?.qty;
            const n = Number(q);
            return acc + (Number.isFinite(n) ? n : 0);
        }, 0);
        return String(sum);
    }

    // 兜底：使用汇总字段
    const val = row?.orderInfo?.productQuantity ?? row?.productCount ?? row?.productQuantity;
    if (val === 0) return "0";
    return val ? String(val) : "-";
};

/**
 * 商品总价：优先 orderInfo.totalAmount，其次 totalAmount / orderAmount / payAmount
 */
const getTotalPrice = (row: any): string => {
    const val = row?.orderInfo?.productAmount;
    if (!(val === 0 || val)) return "-";
    const num = Number(val);
    if (Number.isNaN(num)) return "-";
    return priceFormat(num, true);
};

const getOrderAmount = (row: any): string => {
    const val = row?.orderAmount;
    if (!(val === 0 || val)) return "-";
    const num = Number(val);
    if (Number.isNaN(num)) return "-";
    return priceFormat(num, true);
};

/**
 * 服务员：会员名称/电话
 * 说明：当前服务记录页使用 deliveryOrder(type=2) 列表，接口字段可能是 consignee/mobile 或 contactName/contactPhone。
 */
const getMemberName = (row: any): string => {
    return row?.consignee || row?.memberName || row?.contactName || "-";
};
const getMemberPhoneRaw = (row: any): string | undefined => {
    return row?.mobile || row?.memberPhone || row?.contactPhone;
};
const getMemberPhoneText = (row: any): string => {
    return getMemberPhoneRaw(row) || "-";
};

/**
 * 服务时间字段（尽量兼容不同后端字段命名）
 */
const getServiceStartTime = (row: any): string => {
    return row?.pickupTime ?? "-";
};
const getServiceEndTime = (row: any): string => {
    return row?.completeTime ?? "-";
};

/**
 * 服务总时长：由开始/结束时间计算（HH:MM:SS）
 */
const getServiceDuration = (row: any): string => {
    const start = row?.pickupTime ?? row?.startTime;
    const end = row?.completeTime ?? row?.endTime;
    if (!start || !end) return "-";

    const s = dayjs(start);
    const e = dayjs(end);
    if (!s.isValid() || !e.isValid()) return "-";

    const seconds = e.diff(s, "second");
    if (!(seconds === 0 || seconds) || seconds < 0) return "-";
    return formatDurationMs(seconds * 1000);
};
const formatDurationMs = (ms: number): string => {
    if (!Number.isFinite(ms) || ms < 0) return "-";
    const totalMinutes = Math.floor(ms / 60000);
    const days = Math.floor(totalMinutes / (60 * 24));
    const hours = Math.floor((totalMinutes % (60 * 24)) / 60);
    const minutes = totalMinutes % 60;

    if (days > 0) return t('{days}天{hours}小时{minutes}分钟', { days, hours, minutes });
    if (hours > 0) return t('{hours}小时{minutes}分钟', { hours, minutes });
    // 如果只有分钟，且分钟不够1分钟，则显示1分钟
    if (minutes < 1) return t('1分钟');
    return t('{minutes}分钟', { minutes });
};
</script>

<style lang="less" scoped>
.record-table-wrapper {
    background: #fff;
    border-radius: 4px;
    overflow: hidden;

    :deep(.order-sn-column .cell) {
        white-space: normal;
        word-break: break-all;
    }
}

// 响应式适配
@media (max-width: 992px) {
    .record-table-wrapper {
        overflow-x: auto;

        :deep(.el-table) {
            min-width: 1200px;
        }
    }
}
</style>
