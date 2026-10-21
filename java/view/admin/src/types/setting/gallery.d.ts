
export interface GalleryDetail {
    id?: number | string;
    galleryId?: number | string;
    parentId?: number | string;
    galleryAdminId?: number | string;
    galleryName?: string;
    gallerySort?: number;
    galleryThumb?:string;
    shopId?: number;
    name?: string;
    sort?: number;
}
export interface GalleryFormResult {
    errcode:number;
    message:string;
    records: GalleryFormState[];
}

export interface GalleryFormState {
    galleryId: number | string;
    id: number | string;
    parentId: number | string;
    galleryAdminId: number;
    galleryName: string;
    name: string;
    gallerySort: number;
    galleryThumb: string;
    shopId: number;
    galleryPics: any[]
    galleryVideoInfoList?: any[];
    children?: GalleryTreeNode[];
    picCount?: number;
}


export interface VideoDetail {
    id?: number | string;
    videoUrl: string;
    videoName: string;
    galleryId?: any;
    videoCover: string;
    format: string;
    duration: string;
    size: string;
    description?: string;
    source?: "upload" | "external";
}

export type GalleryMaterialType = "image" | "video";
export type GalleryNodeId = number | string;

export interface GalleryTreeNode {
    id: GalleryNodeId;
    galleryId?: GalleryNodeId;
    parentId?: GalleryNodeId;
    galleryName?: string;
    name?: string;
    gallerySort?: number;
    sort?: number;
    picCount?: number;
    children?: GalleryTreeNode[];
    galleryPics?: GalleryPicItem[];
    galleryVideoInfoList?: VideoItem[];
}

export interface GalleryTreeResult {
    records: GalleryTreeNode[];
}

export interface GalleryPicItem {
    picId: GalleryNodeId;
    id?: GalleryNodeId;
    picName?: string;
    picUrl?: string;
    picThumb?: string;
    picWidth?: number;
    picHeight?: number;
    picSize?: number | string;
    galleryId?: GalleryNodeId;
    updateTime?: string;
    createTime?: string;
    addTime?: number | string;
    fileType?: string;
    status?: string;
    percent?: number;
    format?: string;
    source?: "upload" | "external";
}

export interface GalleryPicDetail extends GalleryPicItem {
    [key: string]: unknown;
}

export interface VideoItem {
    id: GalleryNodeId;
    videoId?: GalleryNodeId;
    videoName?: string;
    videoUrl?: string;
    videoCover?: string;
    description?: string;
    format?: string;
    duration?: string;
    size?: string | number;
    galleryId?: GalleryNodeId;
    updateTime?: string;
    createTime?: string;
    status?: string;
    percent?: number;
    source?: "upload" | "external";
}

export interface GalleryPicListParams {
    galleryId?: GalleryNodeId;
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: "desc" | "asc" | "manual" | string;
    keyword?: string;
    format?: string;
    source?: string;
}

export interface GalleryVideoListParams {
    id?: GalleryNodeId;
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: "desc" | "asc" | string;
    keyword?: string;
    format?: string;
    source?: string;
}

export interface GalleryPicListResult {
    galleryPicPage?: {
        records: GalleryPicItem[];
        total: number;
    };
    childGalleryList?: GalleryTreeNode[];
    galleryInfo?: GalleryTreeNode;
}

export interface GalleryVideoInfoListResult {
    galleryVideoInfoPage?: {
        records: VideoItem[];
        total: number;
    };
    childGalleryList?: GalleryTreeNode[];
    galleryVideo?: GalleryTreeNode;
}
