<template>
    <div class="container">
        <div class="">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="list-table-tool-col">
                            <el-space>
                                <DialogForm
                                    isDrawer
                                    :title="$t('生成 API Token')"
                                    path="authority/accountEditing/GenerateToken"
                                    width="500px"
                                    @okCallback="loadTokenList"
                                >
                                    <el-button type="primary">{{ $t('生成 Token') }}</el-button>
                                </DialogForm>
                            </el-space>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="tokenList" row-key="id">
                            <el-table-column label="Token" min-width="160">
                                <template #default="{ row }">
                                    <code>{{ row.tokenPrefix }}************************</code>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('名称')" prop="name" min-width="120" />
                            <el-table-column :label="$t('创建时间')" min-width="160">
                                <template #default="{ row }">
                                    {{ formatTime(row.createdAt) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('最后使用')" min-width="160">
                                <template #default="{ row }">
                                    {{ row.lastUsedAt ? formatTime(row.lastUsedAt) : $t('从未使用') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('过期时间')" min-width="160">
                                <template #default="{ row }">
                                    <span v-if="!row.expiresAt">{{ $t('永不过期') }}</span>
                                    <span v-else :class="{ 'text-danger': isExpired(row.expiresAt) }">{{ formatTime(row.expiresAt) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" width="100">
                                <template #default="{ row }">
                                    <el-tag v-if="row.isActive === 1 && !isExpired(row.expiresAt)" type="success" size="small">{{ $t('有效') }}</el-tag>
                                    <el-tag v-else-if="row.isActive === 0" type="info" size="small">{{ $t('已吊销') }}</el-tag>
                                    <el-tag v-else type="warning" size="small">{{ $t('已过期') }}</el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" width="100" fixed="right">
                                <template #default="{ row }">
                                    <el-popconfirm
                                        v-if="row.isActive === 1"
                                        :title="$t('确认要吊销此 Token 吗？吊销后将无法恢复。')"
                                        :confirm-button-text="$t('确认吊销')"
                                        :cancel-button-text="$t('取消')"
                                        @confirm="handleRevoke(row.id)"
                                    >
                                        <template #reference>
                                            <el-button type="danger" link size="small">{{ $t('吊销') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无 Token') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { ref, onMounted } from "vue";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { DialogForm } from "@/components/dialog";
import { generateApiToken, getApiTokenList, revokeApiToken } from "@/api/authority/accountEditing";
import type { ApiTokenItem } from "@/types/authority/accountEditing.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref(false);
const tokenList = ref<ApiTokenItem[]>([]);

const formatTime = (timestamp: number) => {
    return dayjs.unix(timestamp).format("YYYY-MM-DD HH:mm:ss");
};

const isExpired = (expiresAt: number | null) => {
    if (!expiresAt) return false;
    return dayjs.unix(expiresAt).isBefore(dayjs());
};

const loadTokenList = async () => {
    loading.value = true;
    try {
        const result = await getApiTokenList();
        tokenList.value = Array.isArray(result) ? result : [];
    } catch (error: any) {
        message.error(error.message || t('获取 Token 列表失败'));
    } finally {
        loading.value = false;
    }
};

const handleRevoke = async (id: number) => {
    try {
        await revokeApiToken({ id });
        message.success(t('Token 已吊销'));
        await loadTokenList();
    } catch (error: any) {
        message.error(error.message || t('吊销失败'));
    }
};

onMounted(() => {
    loadTokenList();
});
</script>

<style lang="less" scoped>
.text-danger {
    color: #f56c6c;
}
</style>
