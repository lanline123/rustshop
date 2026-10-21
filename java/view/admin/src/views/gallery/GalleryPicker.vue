<template>
    <div class="gallery_warp gallery-picker">
        <div class="picker-header">
            <div class="header-left">
                <el-button type="primary" @click="openUploadDialog">{{ $t('上传图片') }}</el-button>
                <a class="go-center-link" @click="goMaterialCenter">{{ $t('前往素材中心') }}</a>
            </div>
            <div class="header-right">
                <el-input v-model="keyword" clearable :placeholder="$t('图片名称')" class="picker-input" @keyup.enter="reloadList" @clear="reloadList" />
                <el-select v-model="sortMode" class="picker-select" @change="reloadList">
                    <el-option :label="$t('按更新时间排序')" value="desc" />
                    <el-option :label="$t('时间从先到后')" value="asc" />
                    <el-option :label="$t('按名称排序')" value="name" />
                </el-select>
                <el-select v-model="format" clearable :placeholder="$t('图片格式')" class="picker-select" @change="reloadList">
                    <el-option label="JPG" value="jpg" />
                    <el-option label="JPEG" value="jpeg" />
                    <el-option label="PNG" value="png" />
                    <el-option label="GIF" value="gif" />
                    <el-option label="WebP" value="webp" />
                    <el-option label="BMP" value="bmp" />
                </el-select>
                <el-select v-model="filterSource" clearable :placeholder="$t('全部来源')" class="picker-select" @change="reloadList">
                    <el-option :label="$t('全部来源')" value="" />
                    <el-option :label="$t('本地上传')" value="local" />
                    <el-option :label="$t('外部链接')" value="external" />
                </el-select>
                <el-button @click="reloadList">{{ $t('搜索') }}</el-button>
            </div>
        </div>
        <div class="picker-body">
            <div class="picker-sidebar">
                <el-input v-model="folderKeyword" clearable :placeholder="$t('搜索分组名称')" class="sidebar-search" />
                <div class="sidebar-tree-scroll">
                    <div class="gallery-picker-tree-node" :class="{ active: galleryStore.topGalleryId == 0 }" @click="folderChange(0)">
                        <span class="tree-arrow" :class="{ expanded: rootExpanded }" @click.stop="rootExpanded = !rootExpanded"></span>
                        <span class="tree-node-label">{{ $t('全部图片') }}</span>
                    </div>
                    <template v-if="rootExpanded">
                        <PickerTreeNode
                            v-for="item in filteredTree"
                            :key="String(item.galleryId || item.id)"
                            :node="item"
                            :active-id="galleryStore.topGalleryId"
                            :expanded-ids="expandedIds"
                            :depth="1"
                            @select="folderChange"
                            @toggle="toggleExpand"
                        />
                    </template>
                </div>
            </div>
            <div class="picker-content">
                <a-spin :spinning="loading">
                    <DragUploadZone active-tab="image" :uploading="dropUploading" :uploading-text="dropUploadingText" @files-dropped="handleFilesDropped">
                        <div class="gallery-list picker-grid-scroll">
                            <ul v-if="picList.length > 0" class="gallery-list-ul">
                                <li v-for="(pic, index) in picList" :key="String(pic.picId || index)" :class="'gallery-pic-item ' + (isSelected(pic) ? 'selected' : '')">
                                    <template v-if="pic.picThumb">
                                        <div class="img" @click="selectPic(pic)">
                                            <el-image fit="cover" :src="imageFormat(pic.picThumb)" style="width: 100%; height: 100%" />
                                        </div>
                                        <div class="desc">
                                            <span class="pic-name" :title="pic.picName">{{ pic.picName }}</span>
                                        </div>
                                        <div class="img-selected-box">
                                            <div class="img-selected-box-angle"></div>
                                            <div class="img-selected-box-sort ico-font">&#xe63b;</div>
                                        </div>
                                    </template>
                                </li>
                            </ul>
                            <div v-else-if="!loading" class="empty-warp">
                                <div class="empty-bg">{{ $t('暂无素材') }}</div>
                            </div>
                        </div>
                    </DragUploadZone>
                    <div class="picker-footer">
                        <span>{{ $t('共{total}张', { total }) }}</span>
                        <Pagination v-model:page="galleryStore.page" v-model:size="pageSize" :total="total" layout="slot ,prev, pager, next" :background="false" @callback="pageChange" />
                    </div>
                </a-spin>
            </div>
        </div>

        <el-dialog v-model="uploadDialogVisible" :title="$t('上传图片')" width="520px" :append-to-body="true" destroy-on-close>
            <el-form label-width="90px" class="picker-upload-dialog">
                <el-form-item :label="$t('上传方式')">
                    <el-radio-group v-model="pickerUploadMode">
                        <el-radio value="local">{{ $t('本地上传') }}</el-radio>
                        <el-radio value="external">{{ $t('外部链接') }}</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item :label="pickerUploadMode === 'local' ? $t('上传至分组') : $t('所在分组')">
                    <el-tree-select v-model="pickerUploadGalleryId" :data="pickerTreeOptions" check-strictly :render-after-expand="false" style="width: 320px" />
                </el-form-item>
                <template v-if="pickerUploadMode === 'local'">
                    <el-form-item :label="$t('上传图片')">
                        <div class="picker-upload-field">
                            <Upload
                                name="file"
                                action="#"
                                :file-list="pickerPendingFiles"
                                :showUploadList="false"
                                :before-upload="beforePickerUpload"
                                :multiple="true"
                                accept="image/*"
                                @change="handlePickerUploadChange"
                            >
                                <el-button type="primary">{{ $t('选择图片') }}</el-button>
                            </Upload>
                            <div v-if="pickerPendingFiles.length" class="picker-upload-preview">
                                <div v-for="file in pickerPendingFiles" :key="file.uid" class="picker-upload-preview-item">
                                    <el-icon class="picker-upload-file-icon"><Paperclip /></el-icon>
                                    <span class="picker-upload-file-name">{{ file.name }}</span>
                                    <span class="picker-upload-file-actions">
                                        <button type="button" class="picker-upload-file-remove" @click.stop="removePickerFile(file.uid)">
                                            <el-icon><Delete /></el-icon>
                                        </button>
                                    </span>
                                </div>
                            </div>
                            <div class="picker-upload-tip">{{ $t('支持 JPG/PNG/GIF/BMP/WebP，单文件不超过 {imageUploadMaxSize}MB', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}。{{ $t('选择文件后点击保存上传') }}</div>
                        </div>
                    </el-form-item>
                </template>
                <template v-else>
                    <el-form-item :label="$t('图片直链')">
                        <TigInput v-model="pickerExternalUrl" :placeholder="$t('请输入图片的网络地址')" clearable style="width: 320px" />
                        <div style="margin-top: 8px; color: #999; font-size: 13px; line-height: 20px;">{{ $t('需要填写单张图片的网络地址，格式支持 bmp/jpg/png/gif') }}</div>
                    </el-form-item>
                </template>
            </el-form>
            <template #footer>
                <el-button @click="uploadDialogVisible = false">{{ $t('取消') }}</el-button>
                <el-button type="primary" :loading="pickerSubmitting" @click="submitPickerConfirm">{{ $t('保存') }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { message, Upload } from "ant-design-vue";
import type { UploadChangeParam, UploadFile } from "ant-design-vue";
import { Delete, Paperclip } from "@element-plus/icons-vue";
import { requestUrl } from "@/utils/request";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { useGalleryStore } from "@/store/gallery";
import { useConfigStore } from "@/store/config";
import { createExternalImage, getGalleryList, getGalleryPicListForMaterial, getGalleryTree } from "@/api/setting/gallery";
import type { GalleryPicItem, GalleryTreeNode, GalleryNodeId } from "@/types/setting/gallery.d";
import { buildTreeFromFlat, filterTreeByName, toApiGalleryId, toListApiGalleryId } from "@/utils/galleryTree";
import { DragUploadZone } from "@/components/gallery";
import { getImageUploadUrl, getUploadHeaders, uploadFileWithXhr } from "@/utils/galleryUpload";
import { getAdminType } from "@/utils/storage";
import { isWithinImageUploadLimit } from "@/utils/imageUploadLimit";
import PickerTreeNode from "./PickerTreeNode.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({ isMultiple: { type: Boolean, default: false } });
const emit = defineEmits(["submitCallback", "close", "okType"]);
emit("okType", false);
const router = useRouter();
const galleryStore = useGalleryStore();
const configStore = useConfigStore();

const loading = ref(false);
const treeData = ref<GalleryTreeNode[]>([]);
const list = ref<GalleryPicItem[]>([]);
const uploadList = ref<GalleryPicItem[]>([]);
const total = ref(0);
const pageSize = ref(15);
const sortMode = ref("desc");
const selectedPics = ref<GalleryPicItem[]>([]);
const keyword = ref("");
const format = ref("");
const filterSource = ref("");
const filterType = ref("image");
const folderKeyword = ref("");
const rootExpanded = ref(true);
const expandedIds = reactive(new Set<string>());

const apiGalleryId = computed(() => toApiGalleryId(galleryStore.galleryId));
const picList = computed(() => uploadList.value.concat(list.value).filter((item) => item.picThumb || item.status));
const filteredTree = computed(() => filterTreeByName(treeData.value, folderKeyword.value));

const uploadDialogVisible = ref(false);
const pickerUploadMode = ref<"local" | "external">("local");
const pickerUploadGalleryId = ref<GalleryNodeId>(0);
const pickerExternalUrl = ref("");
const pickerSubmitting = ref(false);
const pickerPendingFiles = ref<UploadFile[]>([]);
const mapPickerTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => ({ label: node.galleryName || node.name, value: node.galleryId || node.id, children: node.children?.length ? mapPickerTree(node.children) : undefined }));
const pickerTreeOptions = computed(() => [{ label: t('全部图片'), value: 0 }, ...mapPickerTree(treeData.value)]);
const pickerUploadApiGalleryId = computed(() => toApiGalleryId(pickerUploadGalleryId.value));

const openUploadDialog = () => {
    pickerUploadGalleryId.value = toApiGalleryId(galleryStore.topGalleryId);
    pickerPendingFiles.value = [];
    uploadDialogVisible.value = true;
};

const handlePickerUploadChange = (info: UploadChangeParam) => {
    pickerPendingFiles.value = (info.fileList || []).filter((file) => Boolean(file.originFileObj));
};

const removePickerFile = (uid: string | number) => {
    pickerPendingFiles.value = pickerPendingFiles.value.filter((file) => file.uid !== uid);
};


const beforePickerUpload = (file: File) => {
    const ext = file.name.split(".").pop()?.toLowerCase() || "";
    if (!["bmp", "png", "jpeg", "jpg", "gif", "webp"].includes(ext)) {
        message.error(t('不支持 {ext} 格式的图片', { ext }));
        return Upload.LIST_IGNORE;
    }
    if (!isWithinImageUploadLimit(file, configStore.imageUploadMaxSize)) {
        message.warning(t('{name} 超过 {imageUploadMaxSize}MB 限制', { name: file.name, imageUploadMaxSize: configStore.imageUploadMaxSize }));
        return Upload.LIST_IGNORE;
    }
    return false;
};

const submitPickerExternal = async () => {
    const url = pickerExternalUrl.value.trim();
    if (!url) {
        message.warning(t('请输入图片的网络地址'));
        return;
    }
    pickerSubmitting.value = true;
    try {
        const fileName = (() => { try { return new URL(url).pathname.split("/").pop() || t('外部图片'); } catch { return t('外部图片'); } })();
        await createExternalImage({
            picUrl: url,
            picThumb: url,
            picName: fileName,
            galleryId: toApiGalleryId(pickerUploadGalleryId.value),
            source: "external"
        });
        message.success(t('添加外部图片成功'));
        pickerExternalUrl.value = "";
        uploadDialogVisible.value = false;
        loadList();
    } catch (error: any) {
        message.error(error.message || t('添加外部图片失败'));
    } finally {
        pickerSubmitting.value = false;
    }
};

const submitPickerConfirm = async () => {
    if (pickerUploadMode.value === "external") {
        await submitPickerExternal();
        return;
    }
    if (!pickerPendingFiles.value.length) {
        message.warning(t('请选择图片'));
        return;
    }
    pickerSubmitting.value = true;
    let success = 0;
    let fail = 0;
    try {
        const url = getImageUploadUrl(pickerUploadGalleryId.value);
        const headers = getUploadHeaders();
        for (const file of pickerPendingFiles.value) {
            try {
                const result = await uploadFileWithXhr(url, file.originFileObj as File, headers);
                if (result?.code === 0) success++;
                else fail++;
            } catch {
                fail++;
            }
        }
        if (success > 0) {
            message.success(fail ? t('上传完成，成功 {success} 张，失败 {fail} 张', { success, fail }) : t('上传成功'));
            pickerPendingFiles.value = [];
            uploadDialogVisible.value = false;
            loadList();
        } else {
            message.error(t('上传失败'));
        }
    } finally {
        pickerSubmitting.value = false;
    }
};

const toggleExpand = (id: string) => {
    expandedIds.has(id) ? expandedIds.delete(id) : expandedIds.add(id);
};

const loadTree = async () => {
    try {
        const result = await getGalleryTree({ type: "image" });
        treeData.value = buildTreeFromFlat(result.records || [], (node) => node.galleryId || node.id);
    } catch {
        const result = await getGalleryList({ galleryId: 0 });
        treeData.value = buildTreeFromFlat((result.records || []) as GalleryTreeNode[], (node) => node.galleryId || node.id);
    }
};

const loadList = async () => {
    loading.value = true;
    try {
        const listGalleryId = toListApiGalleryId(galleryStore.topGalleryId);
        const result = await getGalleryPicListForMaterial({
            ...(listGalleryId !== undefined ? { galleryId: listGalleryId } : {}),
            page: galleryStore.page,
            size: pageSize.value,
            ...(sortMode.value === "name" ? { sortField: "picName", sortOrder: "asc" } : { sortOrder: sortMode.value }),
            keyword: keyword.value,
            format: format.value,
            source: filterSource.value
        });
        list.value = result.galleryPicPage?.records || [];
        total.value = result.galleryPicPage?.total || 0;
    } catch (error: any) {
        message.error(error.message || t('加载图片失败'));
    } finally {
        loading.value = false;
    }
};

const reloadList = () => {
    galleryStore.page = 1;
    loadList();
};

const dropUploading = ref(false);
const dropUploadingText = ref("");

const handleFilesDropped = async (files: File[]) => {
    const url = getImageUploadUrl(galleryStore.topGalleryId);
    const headers = getUploadHeaders();
    const total = files.length;
    let success = 0;
    let fail = 0;
    dropUploading.value = true;
    try {
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
    } finally {
        dropUploading.value = false;
        dropUploadingText.value = "";
    }

    if (success > 0) loadList();
    if (fail === 0 && success > 0) {
        message.success(t('全部上传成功'));
    } else if (success > 0) {
        message.warning(t('上传完成，成功 {success} 张，失败 {fail} 张', { success, fail }));
    } else {
        message.error(t('上传失败'));
    }
};

const folderChange = (id: GalleryNodeId) => {
    galleryStore.topGalleryId = id;
    galleryStore.galleryId = (toApiGalleryId(id) as number) || 0;
    reloadList();
};

const pageChange = () => loadList();

const isSelected = (pic: GalleryPicItem) => selectedPics.value.some((item) => item.picId === pic.picId);
const selectPic = (pic: GalleryPicItem) => {
    if (props.isMultiple) {
        selectedPics.value = isSelected(pic) ? selectedPics.value.filter((item) => item.picId !== pic.picId) : selectedPics.value.concat(pic);
    } else {
        selectedPics.value = [pic];
    }
    emit("okType", selectedPics.value.length > 0);
};

const goMaterialCenter = () => {
    // 超管走 /content/material-center，店铺/门店/供应商统一走 /setting/material-center
    const target = getAdminType() === "admin" ? "/content/material-center" : "/setting/material-center";
    window.open(router.resolve(target).href, "_blank", "noopener,noreferrer");
};

const onFormSubmit = () => {
    emit("submitCallback", props.isMultiple ? selectedPics.value : selectedPics.value);
};

onMounted(() => {
    galleryStore.galleryId = 0;
    galleryStore.topGalleryId = 0;
    galleryStore.page = 1;
    loadTree();
    loadList();
});

defineExpose({ onFormSubmit });
</script>
