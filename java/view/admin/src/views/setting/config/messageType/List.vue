<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="notice-warp">
                    <strong> {{ $t('模板行业选择') }}</strong>
                    <p>{{ $t('主营行业：根据您的主营行业选择适合的服务类目') }}</p>
                    <p>{{ $t('副营行业：商业服务 -> 软件/建站/技术开发') }}</p>
                    <strong> {{ $t('生成小程序消息模板') }}</strong>
                    <p>{{ $t('请先添加服务类目《商业服务->软件/建站/技术开发》，否则模板导入不成功，点击会自动删除原有的小程序模板并生成新的模板！') }}</p>
                    <!--                    <strong> 同步小程序消息模板</strong>-->
                    <!--                    <p> 请先点击生成小程序模板，小程序后台已自动添加模板。点击同步则会将生成的模板导入到数据库。检查消息模板ID与小程序后台模板ID是否一致即可！</p>-->
                    <strong> {{ $t('生成公众号模板消息') }}</strong>
                    <p>{{ $t('请先添加服务类目《商业服务->软件/建站/技术开发》，否则模板导入不成功，点击会自动删除原有的公众号模板并生成新的模板！') }}</p>
                    <!--                    <strong> 同步公众号模板消息</strong>-->
                    <!--                    <p> 请先点击生成公众号模板消息，点击同步则会将生成的模板导入到数据库。检查消息模板ID与公众号后台模板ID是否一致即可！</p>-->
                </div>
                <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                    <el-tab-pane :label="$t('发送给用户')" :name="1"></el-tab-pane>
                    <el-tab-pane :label="$t('发送给商家')" :name="2"></el-tab-pane>
                    <div class="table-action">
                        <TigInput v-model="filterParams.keyword" :placeholder="$t('输入关键字')" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit">
                            <template #append>
                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                            </template>
                        </TigInput>
                        <el-button plain @click="generate()">{{ $t('生成小程序消息模板') }}</el-button>
                        <el-button plain @click="generateWechat()">{{ $t('生成公众号模板消息') }}</el-button>
                    </div>
                </el-tabs>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="messageId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column :label="$t('消息类型')" prop="name">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.name"
                                        :params="{ id: row.messageId, field: 'name' }"
                                        :requestApi="updateMessageTypeFiled"
                                        :label="$t('消息类型')"
                                        type="input"
                                    >
                                        <div>{{ row.name }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('消息描述')" prop="describe"></el-table-column>
                            <template v-if="activeKey == 1">
                                <el-table-column :label="$t('邮件')" width="100">
                                    <template #default="{ row }">
                                        <Switch
                                            v-if="row.isMail > -1"
                                            v-model:checked="row.isMail"
                                            :params="{ id: row.messageId, field: 'isMail' }"
                                            :requestApi="updateMessageTypeFiled"
                                        />
                                    </template>
                                </el-table-column>
                            </template>
                            <template v-if="activeKey == 2">
                                <el-table-column :label="$t('钉钉')" width="100">
                                    <template #default="{ row }">
                                        <Switch
                                            v-if="row.isDing > -1"
                                            v-model:checked="row.isDing"
                                            :params="{ id: row.messageId, field: 'isDing' }"
                                            :requestApi="updateMessageTypeFiled"
                                        />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('企业微信')" width="100">
                                    <template #default="{ row }">
                                        <Switch
                                            v-if="row.isWxWork > -1"
                                            v-model:checked="row.isWxWork"
                                            :params="{ id: row.messageId, field: 'isWxWork' }"
                                            :requestApi="updateMessageTypeFiled"
                                        />
                                    </template>
                                </el-table-column>
                            </template>
                            <el-table-column :label="$t('消息')" width="100" v-if="activeKey == 1">
                                <template #default="{ row }">
                                    <Switch
                                        v-if="row.isMessage > -1"
                                        v-model:checked="row.isMessage"
                                        :params="{ id: row.messageId, field: 'isMessage' }"
                                        :requestApi="updateMessageTypeFiled"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('短信')" width="100">
                                <template #default="{ row }">
                                    <Switch
                                        v-if="row.isMsg > -1"
                                        v-model:checked="row.isMsg"
                                        :params="{ id: row.messageId, field: 'isMsg' }"
                                        :requestApi="updateMessageTypeFiled"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('公众号')" width="100" v-if="activeKey == 1">
                                <template #default="{ row }">
                                    <Switch
                                        v-if="row.isWechat > -1"
                                        v-model:checked="row.isWechat"
                                        :params="{ id: row.messageId, field: 'isWechat' }"
                                        :requestApi="updateMessageTypeFiled"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('小程序')" width="100" v-if="activeKey == 1">
                                <template #default="{ row }">
                                    <Switch
                                        v-if="row.isMiniProgram > -1"
                                        v-model:checked="row.isMiniProgram"
                                        :params="{ id: row.messageId, field: 'isMiniProgram' }"
                                        :requestApi="updateMessageTypeFiled"
                                    />
                                </template>
                            </el-table-column>
                            <!-- <el-table-column label="APP" width="100">
                                <template #default="{ row }">
                                    <Switch v-if="row.isApp>-1" v-model:checked="row.isApp" :params="{ id: row.messageId, field: 'isApp' }" :requestApi="updateMessageTypeFiled"/>
                                </template>
                            </el-table-column>
                            <el-table-column label="钉钉" width="100">
                                <template #default="{ row }">
                                    <Switch v-if="row.isDing>-1" v-model:checked="row.isDing" :params="{ id: row.messageId, field: 'isDing' }" :requestApi="updateMessageTypeFiled"/>
                                </template>
                            </el-table-column> -->
                            <el-table-column :width="100" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.messageId }"
                                        isDrawer
                                        path="setting/config/messageType/Info"
                                        :title="row.name + $t('消息编辑')"
                                        width="700px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { Pagination, Switch } from "@/components/list";
import { message, Modal } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import type { MessageTypeFilterParams, MessageTypeFilterState } from "@/types/setting/messageType.d";
import {
    batchSubmit,
    getMessageTypeList,
    miniProgramMessageTemplate,
    miniProgramMessageTemplateSync,
    updateMessageTypeFiled,
    wechatMessageTemplate,
    wechatMessageTemplateSync
} from "@/api/setting/messageType";
import { PopForm } from "@/components/pop-form";
import { useListRequest } from "@/hooks/useListRequest";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<MessageTypeFilterState, MessageTypeFilterParams>({
    apiFunction: getMessageTypeList,
    idKey: "messageId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        sendType: 1,
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

const activeKey = ref<number>(1);
const onTabChange = (val: number) => {
    filterParams.sendType = val;
    loadFilter();
};
const generate = async () => {
    Modal.confirm({
        title: t('您确认要生成小程序消息模板吗？'),
        onOk: async () => {
            try {
                const result = await miniProgramMessageTemplate();
                message.success(t('操作成功'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};

const synchronization = async () => {
    Modal.confirm({
        title: t('您确认要同步小程序消息模板吗？'),
        onOk: async () => {
            try {
                const result = await miniProgramMessageTemplateSync();
                message.success(t('操作成功'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};

const generateWechat = async () => {
    Modal.confirm({
        title: t('您确认要生成公众号模板消息吗？'),
        onOk: async () => {
            try {
                const result = await wechatMessageTemplate();
                message.success(t('操作成功'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};

const synchronizationWechat = async () => {
    Modal.confirm({
        title: t('您确认要同步公众号模板消息吗？'),
        onOk: async () => {
            try {
                const result = await wechatMessageTemplateSync();
                message.success(t('操作成功'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
.table-action {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    .el-button + .el-button {
        margin-left: 0;
    }
}
</style>
