/**
 * 图片上传工具函数
 */
import { t } from "@/utils/i18n";
import { getSecret, baseUrl } from "@/utils/request";
import { imageFormat } from "@/utils/format";

/**
 * 上传图片
 * @param filePath 图片文件路径
 * @param onProgress 上传进度回调
 * @param apiUrl 上传接口地址
 * @returns 上传后的图片URL
 */
export function uploadImage(
    filePath: string,
    onProgress?: (progress: number) => void,
    apiUrl: string = "user/user/uploadImg",
    hidden: boolean = false
): Promise<string> {
    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: baseUrl + import.meta.env.VITE_API_PREFIX + apiUrl,
            filePath: filePath,
            name: "file",
            formData: {
                hidden: hidden.toString()
            },
            header: {
                Authorization: "Bearer " + (uni.getStorageSync("token") || ""),
                Secret: getSecret()
            },
            success: (uploadFileRes) => {
                try {
                    const response = JSON.parse(uploadFileRes.data);
                    if (response.code === 0) {
                        const imageUrl = imageFormat(response.data.picUrl || response.data.picThumb);
                        resolve(imageUrl);
                    } else {
                        reject(new Error(response.message || t("上传失败")));
                    }
                } catch (error) {
                    reject(new Error(t("解析响应失败")));
                }
            },
            fail: (error) => {
                reject(error);
            }
        });
    });
}

/**
 * 批量上传图片
 * @param filePaths 图片文件路径数组
 * @param onProgress 上传进度回调
 * @param apiUrl 上传接口地址
 * @returns 上传后的图片URL数组
 */
export async function uploadImages(filePaths: string[], onProgress?: (progress: number) => void, apiUrl: string = "user/user/uploadImg"): Promise<string[]> {
    const uploadedUrls: string[] = [];
    const total = filePaths.length;

    for (let i = 0; i < filePaths.length; i++) {
        const filePath = filePaths[i];
        try {
            const url = await uploadImage(filePath, undefined, apiUrl);
            uploadedUrls.push(url);
            if (onProgress) {
                onProgress(((i + 1) / total) * 100);
            }
        } catch (error) {
            console.error(`上传第 ${i + 1} 张图片失败:`, error);
            throw error;
        }
    }

    return uploadedUrls;
}
