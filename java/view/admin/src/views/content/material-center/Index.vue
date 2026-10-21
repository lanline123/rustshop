<template>
    <div class="container material-center-page">
        <div class="content-wrapper">
            <div class="material-center-card">
                <div class="material-tabs">
                    <span class="tab-item" :class="{ active: activeTab === 'image' }" @click="switchTab('image')">{{ $t('图片') }}</span>
                    <span class="tab-item" :class="{ active: activeTab === 'video' }" @click="switchTab('video')">{{ $t('视频') }}</span>
                </div>

                <div class="global-actions">
                    <template v-if="activeTab === 'image'">
                        <el-button type="primary" @click="uploadImageVisible = true">{{ $t('上传图片') }}</el-button>
                        <el-button @click="externalImageVisible = true">{{ $t('添加外部链接') }}</el-button>
                    </template>
                    <template v-else>
                        <el-button type="primary" @click="uploadVideoVisible = true">{{ $t('上传视频') }}</el-button>
                        <el-button @click="externalVideoVisible = true">{{ $t('添加外部链接') }}</el-button>
                    </template>
                </div>

                <div class="material-body">
                    <aside class="material-sidebar">
                        <div class="sidebar-header">
                            <span class="sidebar-title">{{ activeTab === "image" ? $t('图片分组') : $t('视频分组') }}</span>
                            <a class="sidebar-manage-link" @click="groupManageVisible = true">{{ $t('分组管理') }}</a>
                        </div>
                        <div class="sidebar-search">
                            <el-input v-model="albumSearchKey" clearable :placeholder="$t('搜索分组')" />
                        </div>
                        <div class="sidebar-tree">
                            <AlbumTree
                                :tree-data="albumTreeData"
                                :active-id="currentAlbumId"
                                :search-key="albumSearchKey"
                                :active-tab="activeTab"
                                @select="handleAlbumSelect"
                                @create="openAlbumCreate"
                                @rename="openAlbumRename"
                                @delete="openAlbumDelete"
                                @sort="handleAlbumSort"
                            />
                        </div>
                    </aside>
                    <div class="material-content-wrapper">
                        <main class="material-content">
                            <div class="content-toolbar">
                                <div class="filter-grid">
                                    <TigInput
                                        v-model="searchKeyword"
                                        name="searchKeyword"
                                        :placeholder="activeTab === 'image' ? $t('图片名称') : $t('视频名称')"
                                        @keyup.enter="handleSearch"
                                        clearable
                                        @clear="handleSearch"
                                    >
                                        <template #append>
                                            <el-button @click="handleSearch"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                    <el-select v-model="sortMode" @change="handleSearch">
                                        <el-option :label="$t('按更新时间排序')" value="desc" />
                                        <el-option :label="$t('时间从先到后')" value="asc" />
                                        <el-option :label="$t('按名称排序')" value="name" />
                                    </el-select>
                                    <el-select v-if="activeTab === 'image'" v-model="filterFormat" clearable :placeholder="$t('全部格式')" @change="handleSearch">
                                        <el-option v-for="item in formatOptions" :key="item" :label="item === 'webp' ? 'WebP' : item.toUpperCase()" :value="item" />
                                    </el-select>
                                    <el-select v-model="filterSource" clearable :placeholder="$t('全部来源')" @change="handleSearch">
                                        <el-option :label="$t('全部来源')" value="" />
                                        <el-option :label="$t('本地上传')" value="local" />
                                        <el-option :label="$t('外部链接')" value="external" />
                                    </el-select>
                                </div>
                            </div>
                            <div class="batch-toolbar">
                                <el-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="handleSelectAll">{{ $t('全选') }}</el-checkbox>
                                <span class="batch-info">{{ $t('共{total}条，已选{length}条', { total, length: selectedIds.length }) }}</span>
                                <el-popconfirm
                                    :title="$t('确认下载已选中的 {length} 个素材吗？', { length: selectedIds.length })"
                                    :confirm-button-text="$t('确定')"
                                    :cancel-button-text="$t('取消')"
                                    @confirm="handleBatchDownload"
                                >
                                    <template #reference>
                                        <el-button :disabled="!selectedIds.length">{{ $t('批量下载') }}</el-button>
                                    </template>
                                </el-popconfirm>
                                <el-popconfirm
                                    :title="$t('确认批量删除已选中的 {length} 个素材吗？', { length: selectedIds.length })"
                                    :confirm-button-text="$t('确定')"
                                    :cancel-button-text="$t('取消')"
                                    @confirm="handleBatchDelete"
                                >
                                    <template #reference>
                                        <el-button :disabled="!selectedIds.length">{{ $t('批量删除') }}</el-button>
                                    </template>
                                </el-popconfirm>
                                <el-button :disabled="!selectedIds.length" @click="transferVisible = true">{{ $t('移动到') }}</el-button>
                                <el-popconfirm
                                    :title="$t('确认导出已选中的 {length} 个素材链接吗？', { length: selectedIds.length })"
                                    :confirm-button-text="$t('确定')"
                                    :cancel-button-text="$t('取消')"
                                    @confirm="handleExportLinks"
                                >
                                    <template #reference>
                                        <el-button :disabled="!selectedIds.length">{{ $t('导出链接') }}</el-button>
                                    </template>
                                </el-popconfirm>
                            </div>
                            <DragUploadZone
                                :active-tab="activeTab"
                                :uploading="dropUploading"
                                :uploading-text="dropUploadingText"
                                @files-dropped="handleFilesDropped"
                            >
                                <a-spin :spinning="loading">
                                    <MaterialGrid
                                        :active-tab="activeTab"
                                        :items="currentItems"
                                        :selected-ids="selectedIds"
                                        :loading="loading"
                                        @select="toggleSelect"
                                        @preview="openPreview"
                                        @edit="openEdit"
                                        @delete="handleItemDelete"
                                        @download="downloadOne"
                                        @copy="copyOne"
                                    />
                                </a-spin>
                            </DragUploadZone>
                            <div class="content-pagination">
                                <el-pagination
                                    v-model:current-page="currentPage"
                                    v-model:page-size="pageSize"
                                    :total="total"
                                    layout="total, sizes, prev, pager, next"
                                    @change="loadMaterials"
                                />
                            </div>
                        </main>
                    </div>
                </div>
            </div>
        </div>

        <DialogForm
            v-model:visible="albumFormVisible"
            path="content/material-center/components/AlbumFormDialog"
            :title="albumFormMode === 'add' ? $t('新建分组') : $t('重命名分组')"
            width="420px"
            :okText="$t('确定')"
            :params="{ mode: albumFormMode, albumType: activeTab, albumId: editingAlbumId, parentId: editingParentId, initialName: editingAlbumName }"
            @okCallback="reloadAll"
        />
        <DialogForm
            v-model:visible="albumDeleteVisible"
            path="content/material-center/components/AlbumDeleteDialog"
            :title="$t('删除分组')"
            width="520px"
            :okText="$t('确定')"
            dialogClass="album-delete-dialog"
            :params="{ albumType: activeTab, albumId: deletingAlbumId, albumName: deletingAlbumName }"
            @okCallback="handleAlbumDeleteOk"
        >
            <template #footer="{ close, onOk, confirmLoading }">
                <el-button @click="close">{{ $t('取消') }}</el-button>
                <el-button type="primary" :loading="confirmLoading" @click="onOk">{{ $t('确定') }}</el-button>
            </template>
        </DialogForm>
        <DialogForm
            v-model:visible="transferVisible"
            path="content/material-center/components/TransferDialog"
            :title="$t('移动到')"
            width="460px"
            :okText="$t('确认移动')"
            :params="{ treeData: albumTreeData, selectedIds, selectedCount: selectedIds.length, albumType: activeTab, currentAlbumId }"
            @okCallback="handleTransferOk"
        />
        <DialogForm
            v-model:visible="groupManageVisible"
            path="content/material-center/components/GroupManageDialog"
            :title="$t('分组管理')"
            width="760px"
            :okText="$t('确定')"
            :params="{ treeData: albumTreeData, albumType: activeTab }"
            @okCallback="loadTree"
        />
        <DialogForm
            v-model:visible="uploadImageVisible"
            path="content/material-center/components/UploadImageDialog"
            :title="$t('上传图片')"
            width="600px"
            :okText="$t('确定')"
            :params="{ treeData: albumTreeData, currentAlbumId }"
            @okCallback="reloadAll"
        />
        <DialogForm
            v-model:visible="uploadVideoVisible"
            path="content/material-center/components/UploadVideoDialog"
            :title="$t('上传视频')"
            width="760px"
            :okText="$t('确定')"
            :params="{ treeData: albumTreeData, currentAlbumId }"
            @okCallback="reloadAll"
        />
        <DialogForm
            v-model:visible="externalImageVisible"
            path="content/material-center/components/ExternalImageDialog"
            :title="$t('添加外部链接')"
            width="540px"
            :okText="$t('确定')"
            :params="{ treeData: albumTreeData, currentAlbumId }"
            @okCallback="reloadAll"
        />
        <DialogForm
            v-model:visible="externalVideoVisible"
            path="content/material-center/components/ExternalVideoDialog"
            :title="$t('添加外部视频链接')"
            width="540px"
            :okText="$t('确定')"
            :params="{ treeData: albumTreeData, currentAlbumId }"
            @okCallback="reloadAll"
        />
        <DialogForm
            v-model:visible="previewVisible"
            path="content/material-center/components/MaterialPreview"
            width="900px"
            :showFooter="false"
            :showHeader="false"
            :params="{ activeTab, item: previewItem, hasPrev: previewIndex > 0, hasNext: previewIndex < currentItems.length - 1, albumName: currentAlbumName }"
            @callback="handlePreviewCallback"
        />
        <DialogForm
            v-model:visible="editVisible"
            path="content/material-center/components/MaterialEditDialog"
            :title="activeTab === 'image' ? $t('编辑图片') : $t('编辑视频')"
            width="520px"
            :okText="$t('保存')"
            :params="{ activeTab, itemId: editingItemId, treeData: albumTreeData }"
            @okCallback="reloadAll"
        />
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import * as XLSX from "xlsx";
import { DialogForm } from "@/components/dialog";
import {
    batchDelGalleryPic,
    batchDownloadGalleryPic,
    delGalleryPicField,
    delVideoInfoField,
    getGalleryList,
    getGalleryPicListForMaterial,
    getGalleryTree,
    getGalleryVideoInfoList,
    getGalleryVideoList,
    sortGallery
} from "@/api/setting/gallery";
import { imageFormat } from "@/utils/format";
import { downloadUrlAsFile, saveBlob } from "@/utils/download";
import type { GalleryNodeId, GalleryPicItem, GalleryTreeNode, VideoItem } from "@/types/setting/gallery.d";
import { buildExportRows, buildTreeFromFlat, copyText, getMaterialUrl, toListApiGalleryId } from "@/utils/galleryTree";
import AlbumTree from "./components/AlbumTree.vue";
import MaterialGrid from "./components/MaterialGrid.vue";
import { DragUploadZone } from "@/components/gallery";
import { createVideoRecord, getImageUploadUrl, getUploadHeaders, readVideoDuration, uploadFileWithXhr, uploadVideoFileWithXhr } from "@/utils/galleryUpload";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineOptions({ name: "MaterialCenterIndex" });

const activeTab = ref<"image" | "video">("image");
const albumTreeData = ref<GalleryTreeNode[]>([]);
const currentAlbumId = ref<GalleryNodeId>(0);
const albumSearchKey = ref("");
const searchKeyword = ref("");
const sortMode = ref("desc");
const filterFormat = ref("");
const filterSource = ref("");
const currentPage = ref(1);
const pageSize = ref(30);
const total = ref(0);
const loading = ref(false);
const picList = ref<GalleryPicItem[]>([]);
const videoList = ref<VideoItem[]>([]);
const selectedIds = ref<GalleryNodeId[]>([]);

const albumFormVisible = ref(false);
const albumFormMode = ref<"add" | "rename">("add");
const editingAlbumId = ref<GalleryNodeId>();
const editingParentId = ref<GalleryNodeId>(0);
const editingAlbumName = ref("");
const albumDeleteVisible = ref(false);
const deletingAlbumId = ref<GalleryNodeId>();
const deletingAlbumName = ref("");
const transferVisible = ref(false);
const groupManageVisible = ref(false);
const uploadImageVisible = ref(false);
const uploadVideoVisible = ref(false);
const externalImageVisible = ref(false);
const externalVideoVisible = ref(false);
const previewVisible = ref(false);
const previewItem = ref<GalleryPicItem | VideoItem | null>(null);
const editVisible = ref(false);
const editingItemId = ref<GalleryNodeId>();
const currentItems = computed(() => (activeTab.value === "image" ? picList.value : videoList.value));
const formatOptions = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
const isAllSelected = computed(() => currentItems.value.length > 0 && currentItems.value.every((item) => selectedIds.value.includes(getItemId(item))));
const isIndeterminate = computed(() => selectedIds.value.length > 0 && !isAllSelected.value);
const previewIndex = computed(() => (previewItem.value ? currentItems.value.findIndex((item) => getItemId(item) === getItemId(previewItem.value as any)) : -1));
const currentAlbumName = computed(() => findAlbumName(albumTreeData.value, currentAlbumId.value));

const getItemId = (item: GalleryPicItem | VideoItem): GalleryNodeId => (item as GalleryPicItem).picId || (item as VideoItem).id;
const findAlbumName = (nodes: GalleryTreeNode[], id: GalleryNodeId): string => {
    for (const node of nodes) {
        if (String(node.galleryId || node.id) === String(id)) return node.galleryName || node.name || "";
        const child = node.children?.length ? findAlbumName(node.children, id) : "";
        if (child) return child;
    }
    return id === 0 ? (activeTab.value === "image" ? t('全部图片') : t('全部视频')) : "";
};

const switchTab = (tab: "image" | "video") => {
    activeTab.value = tab;
    currentAlbumId.value = 0;
    currentPage.value = 1;
    selectedIds.value = [];
    searchKeyword.value = "";
    filterFormat.value = "";
    filterSource.value = "";
    sortMode.value = "desc";
    reloadAll();
};

const loadTree = async () => {
    try {
        const result = await getGalleryTree({ type: activeTab.value });
        albumTreeData.value = buildTreeFromFlat(result.records || [], (node) => node.galleryId || node.id);
    } catch {
        const result = activeTab.value === "image" ? await getGalleryList({ galleryId: 0 }) : await getGalleryVideoList({ galleryId: 0 });
        albumTreeData.value = buildTreeFromFlat((result.records || []) as GalleryTreeNode[], (node) => node.galleryId || node.id);
    }
};

const loadMaterials = async () => {
    loading.value = true;
    try {
        const listGalleryId = toListApiGalleryId(currentAlbumId.value);
        if (activeTab.value === "image") {
            const result = await getGalleryPicListForMaterial({
                ...(listGalleryId !== undefined ? { galleryId: listGalleryId } : {}),
                page: currentPage.value,
                size: pageSize.value,
                ...(sortMode.value === "name" ? { sortField: "picName", sortOrder: "asc" } : { sortOrder: sortMode.value }),
                keyword: searchKeyword.value,
                format: filterFormat.value,
                source: filterSource.value
            });
            picList.value = (result.galleryPicPage?.records || []).filter((item: GalleryPicItem) => item.picThumb || item.status);
            total.value = result.galleryPicPage?.total || 0;
        } else {
            const result = await getGalleryVideoInfoList({
                ...(listGalleryId !== undefined ? { id: listGalleryId } : {}),
                page: currentPage.value,
                size: pageSize.value,
                ...(sortMode.value === "name" ? { sortField: "videoName", sortOrder: "asc" } : { sortOrder: sortMode.value }),
                keyword: searchKeyword.value,
                source: filterSource.value
            });
            videoList.value = result.galleryVideoInfoPage?.records || [];
            total.value = result.galleryVideoInfoPage?.total || 0;
        }
    } catch (error: any) {
        message.error(error.message || t('加载素材失败'));
    } finally {
        loading.value = false;
    }
};

const reloadAll = async () => {
    await loadTree();
    await loadMaterials();
};

const dropUploading = ref(false);
const dropUploadingText = ref("");

const handleFilesDropped = async (files: File[]) => {
    const total = files.length;
    let success = 0;
    let fail = 0;
    dropUploading.value = true;
    try {
        if (activeTab.value === "image") {
            const url = getImageUploadUrl(currentAlbumId.value);
            const headers = getUploadHeaders();
            for (let i = 0; i < files.length; i++) {
                const prefix = total > 1 ? t('正在上传 {i1}/{total}', { i1: i + 1, total }) : t('正在上传');
                dropUploadingText.value = prefix;
                try {
                    const result = await uploadFileWithXhr(url, files[i], headers, (percent) => {
                        dropUploadingText.value = percent >= 100 ? t('{prefix} 处理中...', { prefix }) : `${prefix} ${percent}%`;
                    });
                    if (result.code === 0) success++;
                    else fail++;
                } catch {
                    fail++;
                }
            }
        } else {
            const headers = getUploadHeaders();
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                const prefix = total > 1 ? t('正在上传 {i1}/{total}', { i1: i + 1, total }) : t('正在上传');
                dropUploadingText.value = prefix;
                try {
                    const uploadData = await uploadVideoFileWithXhr(file, headers, (percent) => {
                        dropUploadingText.value = percent >= 100 ? t('{prefix} 处理中...', { prefix }) : `${prefix} ${percent}%`;
                    });
                    dropUploadingText.value = total > 1 ? t('正在保存 {i1}/{total}', { i1: i + 1, total }) : t('正在保存');
                    const duration = await readVideoDuration(file);
                    await createVideoRecord(uploadData, file, currentAlbumId.value, duration);
                    success++;
                } catch {
                    fail++;
                }
            }
        }
    } finally {
        dropUploading.value = false;
        dropUploadingText.value = "";
    }

    if (success > 0) await reloadAll();
    if (fail === 0 && success > 0) {
        message.success(t('全部上传成功'));
    } else if (success > 0) {
        message.warning(t('上传完成，成功 {success} 个，失败 {fail} 个', { success, fail }));
    } else {
        message.error(t('上传失败'));
    }
};

const handleAlbumSelect = (id: GalleryNodeId) => {
    currentAlbumId.value = id;
    currentPage.value = 1;
    selectedIds.value = [];
    loadMaterials();
};

const handleSearch = () => {
    currentPage.value = 1;
    loadMaterials();
};

const openAlbumCreate = (node?: GalleryTreeNode) => {
    albumFormMode.value = "add";
    editingAlbumId.value = undefined;
    editingParentId.value = node ? node.galleryId || node.id : 0;
    editingAlbumName.value = "";
    albumFormVisible.value = true;
};

const openAlbumRename = (node: GalleryTreeNode) => {
    albumFormMode.value = "rename";
    editingAlbumId.value = node.galleryId || node.id;
    editingParentId.value = node.parentId || 0;
    editingAlbumName.value = node.galleryName || node.name || "";
    albumFormVisible.value = true;
};

const openAlbumDelete = (node: GalleryTreeNode) => {
    deletingAlbumId.value = node.galleryId || node.id;
    deletingAlbumName.value = node.galleryName || node.name || "";
    albumDeleteVisible.value = true;
};

const albumExists = (nodes: GalleryTreeNode[], id: GalleryNodeId): boolean =>
    nodes.some((node) => String(node.galleryId || node.id) === String(id) || (node.children?.length ? albumExists(node.children, id) : false));

const handleAlbumDeleteOk = async () => {
    await loadTree();
    if (currentAlbumId.value !== 0 && !albumExists(albumTreeData.value, currentAlbumId.value)) {
        currentAlbumId.value = 0;
        currentPage.value = 1;
        selectedIds.value = [];
    }
    await loadMaterials();
};

const toggleSelect = (item: GalleryPicItem | VideoItem) => {
    const id = getItemId(item);
    selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((itemId) => itemId !== id) : selectedIds.value.concat(id);
};

const handleSelectAll = (checked: any) => {
    selectedIds.value = checked ? currentItems.value.map(getItemId) : [];
};

const openPreview = (item: GalleryPicItem | VideoItem) => {
    previewItem.value = item;
    previewVisible.value = true;
};

const previewByOffset = (offset: number) => {
    const next = currentItems.value[previewIndex.value + offset];
    if (next) previewItem.value = next;
};

const handlePreviewCallback = (payload: { type?: string }) => {
    if (payload?.type === "prev") previewByOffset(-1);
    if (payload?.type === "next") previewByOffset(1);
};

const openEdit = (item: GalleryPicItem | VideoItem) => {
    editingItemId.value = getItemId(item);
    editVisible.value = true;
};

const handleItemDelete = async (item: GalleryPicItem | VideoItem) => {
    await (activeTab.value === "image" ? delGalleryPicField({ id: getItemId(item) }) : delVideoInfoField({ id: getItemId(item) }));
    message.success(t('删除成功'));
    reloadAll();
};

const handleBatchDelete = async () => {
    if (!selectedIds.value.length) return;
    try {
        await batchDelGalleryPic({ ids: selectedIds.value, type: activeTab.value });
        message.success(t('删除成功'));
        selectedIds.value = [];
        reloadAll();
    } catch (error: any) {
        message.error(error.message || t('批量删除失败'));
    }
};

const handleBatchDownload = async () => {
    const blob = await batchDownloadGalleryPic({ ids: selectedIds.value, type: activeTab.value });
    saveBlob(blob, t('{valueimage}素材.zip', { valueimage: activeTab.value === "image" ? t('图片') : t('视频') }));
};

const downloadOne = async (item: GalleryPicItem | VideoItem) => {
    const url =
        activeTab.value === "image"
            ? imageFormat((item as GalleryPicItem).picUrl || (item as GalleryPicItem).picThumb)
            : imageFormat((item as VideoItem).videoUrl);
    if (!url) return;
    const filename = activeTab.value === "image" ? (item as GalleryPicItem).picName || "image" : (item as VideoItem).videoName || "video";
    try {
        await downloadUrlAsFile(url, filename);
    } catch (error: any) {
        message.error(error.message || t('下载失败，请检查资源跨域配置'));
    }
};

const copyOne = async (item: GalleryPicItem | VideoItem) => {
    await copyText(activeTab.value === "image" ? imageFormat(getMaterialUrl(item, "image")) : imageFormat(getMaterialUrl(item, "video")));
    message.success(t('链接已复制'));
};

const handleExportLinks = () => {
    const selectedItems = currentItems.value.filter((item) => selectedIds.value.includes(getItemId(item)));
    const rows = buildExportRows(
        selectedItems.map((item) => ({
            ...item,
            picUrl: activeTab.value === "image" ? imageFormat((item as GalleryPicItem).picUrl || (item as GalleryPicItem).picThumb) : undefined,
            videoUrl: activeTab.value === "video" ? imageFormat((item as VideoItem).videoUrl) : undefined
        })),
        activeTab.value
    );
    const sheet = XLSX.utils.json_to_sheet(rows);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, sheet, t('素材链接'));
    XLSX.writeFile(workbook, t('{valueimage}素材链接.xlsx', { valueimage: activeTab.value === "image" ? t('图片') : t('视频') }));
};

const handleTransferOk = () => {
    transferVisible.value = false;
    selectedIds.value = [];
    reloadAll();
};

const handleAlbumSort = async (ids: GalleryNodeId[]) => {
    try {
        await sortGallery({ ids, type: activeTab.value });
        message.success(t('分组排序已保存'));
        loadTree();
    } catch (error: any) {
        message.error(error.message || t('保存分组排序失败'));
        loadTree();
    }
};

onMounted(reloadAll);
</script>

<style scoped lang="less">
.material-center-page {
    background: #f5f7fa;
}
.material-center-card {
    height: calc(100vh - 210px);
    background: #fff;
    padding: 10px 24px 24px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
.material-tabs {
    display: flex;
    gap: 32px;
    padding: 0;
    margin-bottom: 16px;
    border-bottom: 1px solid #eee;
    flex-shrink: 0;
    .tab-item {
        padding: 12px 0;
        cursor: pointer;
        font-size: 14px;
        color: #333;
        border-bottom: 2px solid transparent;
        transition: all 0.2s;
        position: relative;
        bottom: -1px;
        &.active {
            color: var(--tig-primary);
            border-bottom-color: var(--tig-primary);
            font-weight: 500;
        }
        &:hover {
            color: var(--tig-primary);
        }
    }
}
.global-actions {
    padding-bottom: 16px;
    flex-shrink: 0;
}
.material-body {
    display: flex;
    gap: 10px;
    flex: 1;
    min-height: 0;
    overflow: hidden;
}
.material-sidebar {
    width: 240px;
    flex-shrink: 0;
    border: 1px solid #f0f0f0;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
.sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 25px 12px;
    flex-shrink: 0;
    .sidebar-title {
        font-size: 14px;
        font-weight: 500;
        color: #333;
    }
    .sidebar-manage-link {
        color: var(--tig-primary);
        font-size: 13px;
        cursor: pointer;
    }
}
.sidebar-search {
    padding: 0 12px 20px;
    flex-shrink: 0;
}
.sidebar-tree {
    flex: 1;
    padding: 0 12px 12px;
    overflow-y: auto;
    line-height: 1;
}
.material-content-wrapper {
    flex: 1;
    min-width: 0;
    display: flex;
    border: 1px solid #f0f0f0;
    overflow: hidden;
}
.material-content {
    flex: 1;
    min-width: 0;
    padding: 24px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
.content-toolbar {
    margin-bottom: 16px;
    flex-shrink: 0;
    .filter-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 12px;
        max-width: 860px;
        .filter-search-btn {
            width: 120px;
            justify-self: start;
        }
    }
}
.batch-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    margin-bottom: 16px;
    flex-shrink: 0;
    .batch-info {
        font-size: 13px;
        color: #999;
        margin-right: 12px;
    }
}
.content-pagination {
    display: flex;
    justify-content: flex-end;
    padding: 16px 0 8px;
    margin-top: 12px;
    flex-shrink: 0;
}
:deep(.drag-upload-zone) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
:deep(.ant-spin-nested-loading) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100%;
}
:deep(.ant-spin-container) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100%;
}
:deep(.album-tree-item) {
    line-height: 1;
}
:deep(.album-node) {
    height: 30px;
    display: flex;
    align-items: center;
    border-radius: 4px;
    cursor: pointer;
    padding-right: 8px;
    padding-left: 12px;
    margin: 0;
    position: relative;
    &:hover {
        background: #f5f5f5;
        .album-more {
            display: inline-flex;
            opacity: 1;
        }
        .drag-handle {
            opacity: 1;
        }
    }
    &.active {
        background: var(--tig-item-active-bg);
        color: var(--tig-primary);
    }
    .album-name {
        flex: 1;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .album-more {
        display: inline-flex;
        opacity: 0;
        transition: opacity 0.15s;
        color: #999;
        font-size: 16px;
        width: 24px;
        height: 24px;
        line-height: 24px;
        align-self: center;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
        vertical-align: middle;
        :deep(.el-tooltip__trigger) {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            line-height: 24px;
        }
        &:hover {
            background: rgba(0, 0, 0, 0.06);
            color: #333;
        }
        &.always-visible {
            opacity: 1;
        }
    }
    .drag-handle {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 20px;
        height: 20px;
        flex: 0 0 20px;
        margin-left: -24px;
        margin-right: 4px;
        color: #bbb;
        cursor: grab;
        opacity: 0;
        transition:
            opacity 0.15s,
            color 0.15s;
        &:hover {
            color: #666;
        }
    }
}
:deep(.album-node.child-node) {
    padding-left: 32px;
}
:deep(.album-arrow),
:deep(.album-arrow-placeholder) {
    width: 18px;
    height: 18px;
    flex: 0 0 18px;
    margin-right: 6px;
    border-radius: 2px;
}
:deep(.album-arrow::after) {
    content: "";
    display: block;
    margin: 4px 0 0 6px;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 5px 0 5px 6px;
    border-color: transparent transparent transparent #999;
}
:deep(.album-arrow.expanded::after) {
    transform: rotate(90deg);
}
:deep(.video-play-button) {
    position: absolute;
    z-index: 3;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: 0;
    background: rgba(0, 0, 0, 0.45);
    cursor: pointer;
    span {
        display: block;
        margin-left: 12px;
        width: 0;
        height: 0;
        border-top: 8px solid transparent;
        border-bottom: 8px solid transparent;
        border-left: 12px solid #fff;
    }
}

/* 移动端响应式适配 */
@media screen and (max-width: 768px) {
    .material-center-card {
        padding: 10px 12px 12px;
        height: auto !important;
        overflow: visible !important;
    }
    .material-body {
        flex-direction: column;
        height: auto !important;
        min-height: 0 !important;
        overflow: visible !important;
    }
    .material-sidebar {
        width: 100%;
        max-height: 300px;
        height: auto !important;
        overflow: visible !important;
    }
    .sidebar-tree {
        max-height: 250px;
        overflow-y: auto;
    }
    .material-content-wrapper {
        height: auto !important;
        overflow: visible !important;
    }
    .material-content {
        padding: 12px;
        height: auto !important;
        overflow: visible !important;
    }
    .content-toolbar .filter-grid {
        grid-template-columns: 1fr;
    }
    .batch-toolbar {
        flex-wrap: wrap;
        gap: 8px;
    }
    .material-tabs {
        gap: 20px;
    }
    :deep(.drag-upload-zone) {
        height: auto !important;
        overflow: visible !important;
        display: block !important;
    }
    :deep(.ant-spin-nested-loading) {
        height: auto !important;
        overflow: visible !important;
        display: block !important;
    }
    :deep(.ant-spin-container) {
        height: auto !important;
        overflow: visible !important;
        display: block !important;
    }
}
</style>
