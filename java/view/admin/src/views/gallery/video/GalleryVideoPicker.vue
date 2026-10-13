<template>
    <div class="gallery_warp gallery-picker gallery-video-picker">
        <div class="picker-header">
            <div class="header-left">
                <DialogForm
                    width="700px"
                    :title="$t('上传视频')"
                    path="gallery/video/VideoEdit"
                    :params="{ act: 'add', galleryId: apiGalleryId }"
                    @okCallback="reloadList"
                >
                    <el-button type="primary">{{ $t('上传视频') }}</el-button>
                </DialogForm>
                <a class="go-center-link" @click="goMaterialCenter">{{ $t('前往素材中心') }}</a>
            </div>
            <div class="header-right">
                <el-input v-model="keyword" clearable :placeholder="$t('视频名称')" class="picker-input" @keyup.enter="reloadList" @clear="reloadList" />
                <el-select v-model="sortMode" class="picker-select" @change="reloadList">
                    <el-option :label="$t('按更新时间排序')" value="desc" />
                    <el-option :label="$t('时间从先到后')" value="asc" />
                    <el-option :label="$t('按名称排序')" value="name" />
                </el-select>
                <el-select v-model="format" clearable :placeholder="$t('视频格式')" class="picker-select" @change="reloadList">
                    <el-option label="MP4" value="mp4" />
                    <el-option label="AVI" value="avi" />
                    <el-option label="MOV" value="mov" />
                    <el-option label="WebM" value="webm" />
                    <el-option label="FLV" value="flv" />
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
                    <div class="gallery-picker-tree-node" :class="{ active: galleryStore.topVideoGalleryId == 0 }" @click="folderChange(0)">
                        <span class="tree-arrow" :class="{ expanded: rootExpanded }" @click.stop="rootExpanded = !rootExpanded"></span>
                        <span class="tree-node-label">{{ $t('全部视频') }}</span>
                    </div>
                    <template v-if="rootExpanded">
                        <PickerTreeNode
                            v-for="item in filteredTree"
                            :key="String(item.galleryId || item.id)"
                            :node="item"
                            :active-id="galleryStore.topVideoGalleryId"
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
                    <DragUploadZone active-tab="video" :uploading="dropUploading" :uploading-text="dropUploadingText" @files-dropped="handleFilesDropped">
                        <div class="gallery-list picker-grid-scroll">
                            <ul v-if="videoList.length > 0" class="gallery-list-ul video-grid-ul">
                                <li
                                    v-for="(video, index) in videoList"
                                    :key="String(video.id || index)"
                                    :class="'gallery-video-card ' + (isSelected(video) ? 'selected' : '')"
                                >
                                    <div class="video-thumb" @click="selectVideo(video)">
                                        <button class="video-play-button" type="button" @click.stop="openPreview(video)">
                                            <span></span>
                                        </button>
                                        <el-image v-if="video.videoCover" fit="cover" :src="imageFormat(video.videoCover)" class="video-cover" />
                                        <video v-else :src="imageFormat(video.videoUrl)" class="video-cover"></video>
                                        <div class="video-meta-bar">{{ video.duration }}</div>
                                    </div>
                                    <div class="desc">
                                        <span class="pic-name" :title="video.videoName">{{ video.videoName }}</span>
                                    </div>
                                    <div class="img-selected-box">
                                        <div class="img-selected-box-angle"></div>
                                        <div class="img-selected-box-sort ico-font">&#xe63b;</div>
                                    </div>
                                </li>
                            </ul>
                            <div v-else-if="!loading" class="empty-warp">
                                <div class="empty-bg">{{ $t('暂无素材') }}</div>
                            </div>
                        </div>
                    </DragUploadZone>
                    <div class="picker-footer">
                        <span>{{ $t('共{total}个', { total }) }}</span>
                        <Pagination
                            v-model:page="galleryStore.page"
                            v-model:size="pageSize"
                            :total="total"
                            layout="slot ,prev, pager, next"
                            :background="false"
                            @callback="pageChange"
                        />
                    </div>
                </a-spin>
            </div>
        </div>
        <el-dialog v-model="previewVisible" :title="previewVideo?.videoName || $t('视频预览')" width="760px" destroy-on-close>
            <video v-if="previewVideo" :src="imageFormat(previewVideo.videoUrl)" controls autoplay class="video-preview-player"></video>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { useGalleryStore } from "@/store/gallery";
import { getGalleryTree, getGalleryVideoInfoList, getGalleryVideoList } from "@/api/setting/gallery";
import type { GalleryNodeId, GalleryTreeNode, VideoItem } from "@/types/setting/gallery.d";
import { buildTreeFromFlat, filterTreeByName, toApiGalleryId, toListApiGalleryId } from "@/utils/galleryTree";
import { DragUploadZone } from "@/components/gallery";
import { createVideoRecord, getUploadHeaders, readVideoDuration, uploadVideoFileWithXhr } from "@/utils/galleryUpload";
import PickerTreeNode from "../PickerTreeNode.vue";
import { getAdminType } from "@/utils/storage";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({ isMultiple: { type: Boolean, default: false } });
const emit = defineEmits(["submitCallback", "close", "okType"]);
emit("okType", false);

const router = useRouter();
const galleryStore = useGalleryStore();
const loading = ref(false);
const treeData = ref<GalleryTreeNode[]>([]);
const videoList = ref<VideoItem[]>([]);
const total = ref(0);
const pageSize = ref(10);
const sortMode = ref("desc");
const selectedVideos = ref<VideoItem[]>([]);
const keyword = ref("");
const format = ref("");
const filterSource = ref("");
const filterType = ref("video");
const folderKeyword = ref("");
const rootExpanded = ref(true);
const expandedIds = reactive(new Set<string>());
const previewVisible = ref(false);
const previewVideo = ref<VideoItem | null>(null);

const apiGalleryId = computed(() => toApiGalleryId(galleryStore.videoGalleryId));
const filteredTree = computed(() => filterTreeByName(treeData.value, folderKeyword.value));

const toggleExpand = (id: string) => {
    expandedIds.has(id) ? expandedIds.delete(id) : expandedIds.add(id);
};

const loadTree = async () => {
    try {
        const result = await getGalleryTree({ type: "video" });
        treeData.value = buildTreeFromFlat(result.records || [], (node) => node.galleryId || node.id);
    } catch {
        const result = await getGalleryVideoList({ galleryId: 0 });
        treeData.value = buildTreeFromFlat((result.records || []) as GalleryTreeNode[], (node) => node.id);
    }
};

const loadList = async () => {
    loading.value = true;
    try {
        const listGalleryId = toListApiGalleryId(galleryStore.topVideoGalleryId);
        const result = await getGalleryVideoInfoList({
            ...(listGalleryId !== undefined ? { id: listGalleryId } : {}),
            page: galleryStore.page,
            size: pageSize.value,
            ...(sortMode.value === "name" ? { sortField: "videoName", sortOrder: "asc" } : { sortOrder: sortMode.value }),
            keyword: keyword.value,
            format: format.value,
            source: filterSource.value
        });
        videoList.value = result.galleryVideoInfoPage?.records || [];
        total.value = result.galleryVideoInfoPage?.total || 0;
    } catch (error: any) {
        message.error(error.message || t('加载视频失败'));
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
    const headers = getUploadHeaders();
    const total = files.length;
    let success = 0;
    let fail = 0;
    dropUploading.value = true;
    try {
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
                await createVideoRecord(uploadData, file, galleryStore.topVideoGalleryId, duration);
                success++;
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
        message.warning(t('上传完成，成功 {success} 个，失败 {fail} 个', { success, fail }));
    } else {
        message.error(t('上传失败'));
    }
};

const folderChange = (id: GalleryNodeId) => {
    galleryStore.topVideoGalleryId = id;
    galleryStore.videoGalleryId = toApiGalleryId(id);
    reloadList();
};

const pageChange = () => loadList();

const isSelected = (video: VideoItem) => selectedVideos.value.some((item) => item.id === video.id);
const selectVideo = (video: VideoItem) => {
    if (props.isMultiple) {
        selectedVideos.value = isSelected(video) ? selectedVideos.value.filter((item) => item.id !== video.id) : selectedVideos.value.concat(video);
    } else {
        selectedVideos.value = [video];
    }
    emit("okType", selectedVideos.value.length > 0);
};

const openPreview = (video: VideoItem) => {
    previewVideo.value = video;
    previewVisible.value = true;
};

const goMaterialCenter = () => {
    // 超管走 /content/material-center，店铺/门店/供应商统一走 /setting/material-center
    const target = getAdminType() === "admin" ? "/content/material-center" : "/setting/material-center";
    window.open(router.resolve(target).href, "_blank", "noopener,noreferrer");
};

const onFormSubmit = () => {
    emit("submitCallback", props.isMultiple ? selectedVideos.value : selectedVideos.value);
};

onMounted(() => {
    galleryStore.videoGalleryId = 0;
    galleryStore.topVideoGalleryId = 0;
    galleryStore.page = 1;
    loadTree();
    loadList();
});

defineExpose({ onFormSubmit });
</script>
