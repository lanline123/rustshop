import type {
    GalleryDetail,
    GalleryFormResult,
    GalleryTreeResult,
    GalleryPicDetail,
    GalleryPicListParams,
    GalleryVideoListParams
} from "@/types/setting/gallery.d";
import request from "@/utils/request";

export const getGalleryList = (params:Object) => {
    return request<GalleryFormResult>({
        url: "setting/gallery/list",
        method: "get",
        params,
    });
}

export const getGalleryPicList = (params:Object) => {
    return request<any>({
        url: "setting/galleryPic/list",
        method: "get",
        params,
    });
}
export const getGalleryTree = (params: { type: "image" | "video" }) => {
    return request<GalleryTreeResult>({
        url: "setting/gallery/tree",
        method: "get",
        params
    });
}
export const getGalleryPicDetail = (params: { id: number | string }) => {
    return request<GalleryPicDetail>({
        url: "setting/galleryPic/detail",
        method: "get",
        params
    });
}
export const getGalleryPicListForMaterial = (params: GalleryPicListParams) => {
    return request<any>({
        url: "setting/galleryPic/list",
        method: "get",
        params
    });
}

export const getGalleryInfo = (action: string, params:Object) => {
    return request<GalleryDetail>({
        url: "setting/gallery/" + action,
        method: "get",
        params,
    });
}

export const updateGalleryInfo = (operation: string, data: object) => {
    return request({
        url: "setting/gallery/" + operation,
        method: "post",
        data
    });
}
export const updateGalleryField = (data: object) => {
    return request({
        url: "setting/gallery/updateField",
        method: "post",
        data
    });
}
export const updateGalleryPicField = (data: object) => {
    return request({
        url: "setting/galleryPic/updateField",
        method: "post",
        data
    });
}
export const sortGallery = (data: { ids: Array<number | string>; type?: "image" | "video" }) => {
    return request({
        url: "setting/gallery/sort",
        method: "post",
        data
    });
}
export const moveGallery = (data: { sourceId: number | string; targetId: number | string; mode: string; type: "image" | "video" }) => {
    return request({
        url: "setting/gallery/move",
        method: "post",
        data
    });
}
export const sortGalleryPic = (data: { galleryId: number | string; ids: Array<number | string> }) => {
    return request({
        url: "setting/galleryPic/sort",
        method: "post",
        data
    });
}
export const batchDelGalleryPic = (data: { ids: Array<number | string>; type?: "image" | "video" }) => {
    return request({
        url: "setting/galleryPic/batchDel",
        method: "post",
        data
    });
}
export const batchDownloadGalleryPic = (data: { ids: Array<number | string>; type: "image" | "video" }) => {
    return request<Blob>({
        url: "setting/galleryPic/batchDownload",
        method: "post",
        data,
        responseType: "blob"
    });
}
export const transferGalleryPic = (data: { ids: Array<number | string>; targetGalleryId: number | string; type: "image" | "video" }) => {
    return request({
        url: "setting/galleryPic/transfer",
        method: "post",
        data
    });
}
export const delGalleryPicField = (data: object) => {
    return request({
        url: "setting/galleryPic/del",
        method: "post",
        data
    });
}
export const delGalleryField = (data: object) => {
    return request({
        url: "setting/gallery/del",
        method: "post",
        data
    });
}


// 编辑相册名称
export const batchVideoInfo = (data: object) => {
    return request({
        url: "setting/galleryVideo/batch",
        method: "post",
        data
    });
}
// 新增编辑视频
export const updateVideoInfo = (operation: string, data: object) => {
    return request({
        url: "setting/galleryVideoInfo/" + operation,
        method: "post",
        data
    });
}
// 删除视频
export const delVideoInfoField = (data: object) => {
    return request({
        url: "setting/galleryVideoInfo/del",
        method: "post",
        data
    });
}

// 新增编辑视频相册
export const updateGalleryVideoInfo = (operation: string, data: object) => {
    return request({
        url: "setting/galleryVideo/" + operation,
        method: "post",
        data
    });
}

// 删除视频相册
export const delGalleryVideoInfo = (data: object) => {
    return request({
        url: "setting/galleryVideo/del",
        method: "post",
        data
    });
}
// 获取视频列表
export const getGalleryVideoInfoList = (params:Object | GalleryVideoListParams) => {
    return request<any>({
        url: "setting/galleryVideoInfo/list",
        method: "get",
        params,
    });
}
// 获取视频详情
export const getGalleryVideoInfoDetail = (params:Object) => {
    return request<any>({
        url: "setting/galleryVideoInfo/detail",
        method: "get",
        params,
    });
}
// 获取视频相册列表
export const getGalleryVideoList = (params?:Object) => {
    return request<any>({
        url: "setting/galleryVideo/list",
        method: "get",
        params,
    });
}
// 获取视频相册详情
export const getGalleryVideoDetail = (params:Object) => {
    return request<any>({
        url: "setting/galleryVideo/detail",
        method: "get",
        params,
    });
}


// 获取视频相册下拉列表
export const getAllCategory = (params?:Object) => {
    return request<any>({
        url: "setting/galleryVideo/getAllCategory",
        method: "get",
        params,
    });
}

export const createExternalImage = (data: object) => {
    return request({
        url: "setting/galleryPic/createFromExternal",
        method: "post",
        data
    });
}