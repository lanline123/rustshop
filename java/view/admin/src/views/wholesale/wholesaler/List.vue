/** * @file 批发商列表 * @description 筛选/列表/设置等级/启停(状态列 Switch)/手动添加/导入/移除(单个+多选，软删批发商身份) */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('客户信息：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入客户名称/手机号')"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('注册渠道：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.fromTag" clearable :placeholder="$t('请选择注册渠道')" @change="onSearchSubmit">
                                                <el-option :value="1" :label="$t('公众号')" />
                                                <el-option :value="2" :label="$t('小程序')" />
                                                <el-option :value="3" label="H5" />
                                                <el-option :value="4" label="PC" />
                                                <el-option :value="5" label="APP" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('批发商等级：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.gradeId" clearable :placeholder="$t('请选择批发商等级')" @change="onSearchSubmit">
                                                <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('批发商状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable :placeholder="$t('请选择批发商状态')" @change="onSearchSubmit">
                                                <el-option :value="1" :label="$t('启用')" />
                                                <el-option :value="0" :label="$t('禁用')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('添加时间：') }}</span></label>
                                        <div class="flex flex-align-center">
                                            <SelectTimeInterval
                                                type="datetime"
                                                valueFormat="YYYY-MM-DD HH:mm:ss"
                                                v-model:start-date="filterParams.addTimeStart"
                                                v-model:end-date="filterParams.addTimeEnd"
                                                @callback="onTimeChange"
                                            ></SelectTimeInterval>
                                            <div style="margin-left: 10px">
                                                <el-radio-group v-model="quickDate" @change="onQuickDate">
                                                    <el-radio-button value="today">{{ $t('今天') }}</el-radio-button>
                                                    <el-radio-button value="yesterday">{{ $t('昨天') }}</el-radio-button>
                                                    <el-radio-button value="7">{{ $t('近7天') }}</el-radio-button>
                                                    <el-radio-button value="30">{{ $t('近30天') }}</el-radio-button>
                                                    <el-radio-button value="180">{{ $t('近半年') }}</el-radio-button>
                                                </el-radio-group>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="onReset">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadData"
                                        :title="$t('添加批发商')"
                                        width="600px"
                                        path="wholesale/wholesaler/src/AddWholesaler"
                                        :params="{}"
                                    >
                                        <el-button type="primary">{{ $t('添加批发商') }}</el-button>
                                    </DialogForm>
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadData"
                                        :title="$t('批量添加批发商')"
                                        width="1000px"
                                        path="wholesale/wholesaler/src/ImportDrawer"
                                        :params="{}"
                                    >
                                        <el-button>{{ $t('批量添加批发商') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('确认删除所选批发商吗？删除后该用户不再是批发商。')" @confirm="onBatchRemove">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"
                                        >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                                    >
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="listData" row-key="wholesalerId" @selection-change="onSelectChange">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('客户信息')" min-width="300">
                                <template #default="{ row }">
                                    <div class="user-info">
                                        <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                                        <div class="user-info__main">
                                            <div class="user-name">{{ row.username || row.nickname || "--" }}</div>
                                            <div class="user-sub">
                                                <MobileCard :mobile="row.mobile || '--'" />
                                            </div>
                                            <el-tag v-if="row.fromTagName" size="small" disable-transitions>{{ row.fromTagName }}</el-tag>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员等级')" min-width="140">
                                <template #default="{ row }">{{ row.rankName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('是否认证')" min-width="120">
                                <template #default="{ row }">{{ row.authStatusName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('批发商等级')" prop="gradeName" min-width="140">
                                <template #default="{ row }">{{ row.gradeName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" min-width="110" align="center">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.status"
                                        :requestApi="statusApi"
                                        :params="{ wholesalerId: row.wholesalerId }"
                                        :beforeChange="() => confirmToggleStatus(row)"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('添加时间')" prop="addTime" min-width="180" />
                            <el-table-column :label="$t('操作')" fixed="right" width="320">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            isDrawer
                                            :title="$t('会员管理')"
                                            width="800px"
                                            :showClose="false"
                                            :showOnOk="false"
                                            path="user/user/Detail"
                                            :params="{ act: 'detail', id: row.userId }"
                                        >
                                            <a class="btn-link">{{ $t('查看') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadData"
                                            :title="$t('设置批发商等级')"
                                            width="500px"
                                            path="wholesale/wholesaler/src/SetGradeDrawer"
                                            :params="{ wholesalerId: row.wholesalerId, gradeId: row.gradeId }"
                                        >
                                            <a class="btn-link">{{ $t('设置批发商等级') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord
                                            :params="{ wholesalerIds: [row.wholesalerId] }"
                                            :requestApi="removeWholesaler"
                                            :title="$t('确认删除该批发商？')"
                                            :content="$t('删除后该用户不再是批发商。')"
                                            @afterDelete="loadData"
                                        >
                                            <a class="btn-link">{{ $t('删除') }}</a>
                                        </DeleteRecord>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadData" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Modal, message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { SelectTimeInterval } from "@/components/select";
import { useListRequest } from "@/hooks/useListRequest";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";
import { formattedDate } from "@/utils/time";
import { getWholesalerPage, setWholesalerStatus, removeWholesaler } from "@/api/wholesale/wholesaler";
import { getGradeList } from "@/api/wholesale/grade";
import type { WholesalerVO, WholesalerPageQuery } from "@/types/wholesale/wholesaler";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const router = useRouter();
const gradeOptions = ref<WholesalerGradeVO[]>([]);
const quickDate = ref<string>("");

const { listData, loading, total, selectedIds, filterParams, loadData, onSearchSubmit, onSelectChange, resetParams } = useListRequest<
    WholesalerVO,
    WholesalerPageQuery
>({
    apiFunction: getWholesalerPage,
    idKey: "wholesalerId",
    defaultParams: {
        keyword: "",
        gradeId: undefined,
        status: undefined,
        fromTag: undefined,
        addTimeStart: "",
        addTimeEnd: "",
        page: 1,
        size: config.get("pageSize")
    }
});

// 手动调整时间区间时，取消快捷选项的选中态
const onTimeChange = () => {
    quickDate.value = "";
    onSearchSubmit();
};

const onQuickDate = (val: any) => {
    const now = new Date();
    const fmt = "YYYY-MM-DD HH:mm:ss";
    let start = new Date();
    let end = new Date();
    if (val === "today") {
        start.setHours(0, 0, 0, 0);
    } else if (val === "yesterday") {
        start.setDate(now.getDate() - 1);
        start.setHours(0, 0, 0, 0);
        end.setDate(now.getDate() - 1);
        end.setHours(23, 59, 59, 0);
    } else {
        start.setDate(now.getDate() - Number(val));
        start.setHours(0, 0, 0, 0);
    }
    filterParams.addTimeStart = formattedDate(start, fmt);
    filterParams.addTimeEnd = formattedDate(end, fmt);
    onSearchSubmit();
};

const onReset = () => {
    quickDate.value = "";
    resetParams();
};

// 启停二次确认（与 DeleteRecord 同款：无图标、标题+正文）
const confirmToggleStatus = (row: WholesalerVO) => {
    const next = row.status === 1 ? 0 : 1;
    const isEnable = next === 1;
    return new Promise<boolean>((resolve) => {
        Modal.confirm({
            title: isEnable ? t('确认启用该批发商？') : t('确认禁用该批发商？'),
            content: isEnable ? t('启用后该批发商可重新享受批发价。') : t('禁用后该批发商不再享受批发价（历史订单不受影响）。'),
            okText: t('确定'),
            okType: isEnable ? "primary" : "danger",
            maskClosable: true,
            onOk: () => resolve(true),
            onCancel: () => resolve(false)
        });
    });
};

// 启停：Switch 传入 val=目标状态
const statusApi = (p: any) => setWholesalerStatus({ wholesalerId: p.wholesalerId, status: p.val });

// 多选移除：软删所选批发商身份（不传等级）
const onBatchRemove = async () => {
    if (selectedIds.value.length === 0) {
        message.warning(t('请至少选择一项'));
        return;
    }
    try {
        await removeWholesaler({ wholesalerIds: selectedIds.value as number[] });
        message.success(t('移除成功'));
        loadData();
    } catch (error: any) {
        message.error(error.message || t('移除失败'));
    }
};

onMounted(async () => {
    const query = router.currentRoute.value.query;
    if (query.gradeId) {
        filterParams.gradeId = Number(query.gradeId);
    }
    try {
        gradeOptions.value = (await getGradeList({ status: 1, withUserCount: false })) || [];
    } catch (error: any) {
        message.error(error.message || t('等级加载失败'));
    }
    loadData();
});
</script>
<style lang="less" scoped>
.simple-form-field .form-group :deep(.control-label) {
    width: 90px;
    white-space: nowrap;
}
.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    .user-info__main {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 2px;
        .user-name {
            color: #333;
        }
        .user-sub {
            color: #999;
            font-size: 12px;
        }
    }
}
</style>
