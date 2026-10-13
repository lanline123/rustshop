import { requestUrl } from "@/utils/request";
import { updateVideoInfo } from "@/api/setting/gallery";
import { formatDuration } from "@/utils/time";
import { toApiGalleryId } from "@/utils/galleryTree";
import type { GalleryNodeId } from "@/types/setting/gallery.d";

export function getImageUploadUrl(galleryId: GalleryNodeId): string {
    return `${requestUrl.prefix}/setting/galleryPic/uploadImg?galleryId=${toApiGalleryId(galleryId)}`;
}

export function getVideoUploadUrl(): string {
    return `${requestUrl.prefix}/setting/galleryVideoInfo/uploadVideo`;
}

export function getUploadHeaders(): Record<string, string> {
    return requestUrl.headers();
}

export interface UploadXhrResult {
    code: number;
    message?: string;
    data?: Record<string, any>;
}

export function uploadFileWithXhr(
    url: string,
    file: File,
    headers: Record<string, string>,
    onProgress?: (percent: number) => void
): Promise<UploadXhrResult> {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append("file", file);

        xhr.upload.onprogress = (e) => {
            if (e.lengthComputable && onProgress) {
                onProgress(Math.round((e.loaded / e.total) * 100));
            }
        };

        xhr.onload = () => {
            try {
                resolve(JSON.parse(xhr.responseText) as UploadXhrResult);
            } catch {
                reject(new Error("upload failed"));
            }
        };

        xhr.onerror = () => reject(new Error("upload failed"));
        xhr.onabort = () => reject(new Error("upload cancelled"));

        xhr.open("POST", url);
        Object.entries(headers).forEach(([key, value]) => xhr.setRequestHeader(key, value));
        xhr.send(formData);
    });
}

export function readVideoDuration(file: File): Promise<string> {
    return new Promise((resolve) => {
        const videoUrl = URL.createObjectURL(file);
        const video = document.createElement("video");
        video.src = videoUrl;
        video.onloadedmetadata = () => {
            URL.revokeObjectURL(videoUrl);
            resolve(formatDuration(video.duration));
        };
        video.onerror = () => {
            URL.revokeObjectURL(videoUrl);
            resolve("");
        };
    });
}

export function getVideoNameFromFile(file: File): string {
    const base = file.name.replace(/\.[^.]+$/, "");
    return (base || file.name).slice(0, 20);
}

export async function uploadVideoFileWithXhr(
    file: File,
    headers: Record<string, string>,
    onProgress?: (percent: number) => void
): Promise<Record<string, any>> {
    const res = await uploadFileWithXhr(getVideoUploadUrl(), file, headers, onProgress);
    if (res.code === 0) {
        return res.data || {};
    }
    throw new Error(res.message || "upload failed");
}

export async function createVideoRecord(
    uploadData: Record<string, any>,
    file: File,
    galleryId: GalleryNodeId,
    duration: string
): Promise<void> {
    await updateVideoInfo("create", {
        ...uploadData,
        videoName: getVideoNameFromFile(file),
        duration,
        size: `${(file.size / 1024 / 1024).toFixed(2)}MB`,
        videoCover: "",
        galleryId: [toApiGalleryId(galleryId)],
        source: "upload"
    });
}
