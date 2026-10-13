<template>
    <div class="table-container">
        <a-spin :spinning="loading">
            <el-table :data="filterState" row-key="buyerShowCommentId" tooltip-effect="dark myTooltips" :total="total" :loading="loading" @selection-change="onSelectChange">
                <el-table-column v-if="showSelection" type="selection" width="32" :selectable="isSelectable" />
                <el-table-column :label="$t('评论内容')" prop="content" show-overflow-tooltip>
                    <template #default="{ row }">
                        <div style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{row.content}}</div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('发布时间')" prop="addTime"></el-table-column>
                <el-table-column :label="$t('发布人')" prop="userName"></el-table-column>
                <el-table-column :label="$t('状态')" prop="statusText"></el-table-column>
                <el-table-column :label="$t('操作')" fixed="right" :width="240">
                    <template #default="{ row }">
                        <div class="flex">
                            <DialogForm
                                v-if="showRelatedComments"
                                :params="{ act: 'add', buyerShowId: row.buyerShowId }"
                                isDrawer
                                :delayed="20000"
                                path="promotion/buyerShow/buyerShowComments/commentManagementList"
                                :title="$t('相关评论')"
                                width="900px"
                                @okCallback="loadFilter"
                                @closeCallback="loadFilter"
                                :showClose="false"
                                :showOnOk="false"
                            >
                                <a class="btn-link mr10">{{ $t('相关评论') }}</a>
                            </DialogForm>
                            <div v-if="row.statusText === '待审核'">
                                <a class="btn-link mr10" @click="changeBuyerShowPassOrReject({ buyerShowCommentId: row.buyerShowCommentId, status: 1})"> {{ $t('通过') }} </a>
                                <a class="btn-link mr10" v-if="row.statusText === '待审核'" @click="changeBuyerShowPassOrReject({ buyerShowCommentId: row.buyerShowCommentId, status: 2})"> {{ $t('拒绝') }} </a>
                            </div>
                            <a class="btn-link mr10" v-if="['隐藏', '公开'].includes(row.statusText)" @click="changeBuyerShowShowOrHide({ buyerShowCommentId: row.buyerShowCommentId, isShow: row.statusText === '隐藏' ? 1 : 0})">
                                {{ row.statusText === $t('隐藏') ? $t('公开') : $t('隐藏') }}
                            </a>
                            <a class="btn-link" @click="changeBuyerShowTop({ buyerShowCommentId: row.buyerShowCommentId, isTop: row.isTop === 1 ? 0 : 1})">{{ row.isTop === 1 ? $t('取消置顶') : $t('置顶') }}</a>
                        </div>
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
            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { useConfigStore } from "@/store/config";
import { ElMessageBox } from "element-plus";
import { WarningFilled } from '@element-plus/icons-vue'
import { message } from "ant-design-vue";
import { defineModel, markRaw, defineProps } from 'vue'
import { commentInfoAudit, commentInfoShowOrHide, commentInfoTop } from "@/api/promotion/buyShow";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const filterParams = defineModel('filterParams', {});
const filterState = defineModel('filterState', {});
const loading = defineModel('loading', false);
const total = defineModel('total', 0);
const props = defineProps({
    // 是否显示复选框
    showSelection: {
        type: Boolean,
        default: true
    },
    // 是否展示相关评论按钮
    showRelatedComments: {
        type: Boolean,
        default: true
    }
});

const config: any = useConfigStore();

// 评论显示隐藏
const changeBuyerShowShowOrHide = async (data: any) => {
    ElMessageBox.confirm(
        data.isShow == 0 ? t('隐藏后，该评论仅创建人自己可见') : t('公开后，该评论将变为公开状态，所有客户可见'),
        {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            icon: markRaw(WarningFilled),
            customClass: 'buyShowHide',
        }
    ).then(async () => {
        await commentInfoShowOrHide(data);
        message.success(t('操作成功'));
        loadFilter();
    })
        .catch((error: any) => {
             // message.error(error.message);
            console.log(error.message); 
        })
}

// 评价通过或拒绝
const changeBuyerShowPassOrReject = async (data: any) => {
    ElMessageBox.confirm(
        data.status == 1 ? t('通过后，该评论将变为公开状态，所有客户可见') : t('拒绝后，该评论将变为隐藏状态，所有客户不可见'),
        {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            icon: markRaw(WarningFilled),
            customClass: 'buyShowHide',
        }
    ).then(async () => {
        await commentInfoAudit(data);
        message.success(t('操作成功'));
        loadFilter();
    })
        .catch((error: any) => {
            // message.error(error.message);
            console.log(error.message); 
        })
}

// 评论置顶
const changeBuyerShowTop = async (data: object) => {
    commentInfoTop(data).then(() => {
        message.success(t('操作成功'));
        loadFilter();
    }).catch((error: any) => {
        // message.error(error.message);
        console.log(error.message); 
    })
}
const emit = defineEmits(['okCallback', 'onSelectChange']);
const loadFilter = () => {
    emit('okCallback')
}
const onSelectChange = (val: any) => {
    emit('onSelectChange', val)
};

// 判断是否可选 (待审核才能勾选)
const isSelectable = (row: any, index: number) => {
    return row.status === 0
};

</script>


<style lang="scss" scoped>
.el-table{
    :deep(.el-popper.is-dark) {
        max-width: 50%;
    }
}
</style>
