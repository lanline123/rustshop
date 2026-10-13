import request from "@/utils/request";

//自动生成SEO链接
export const getTranslateToSeoIdentification = (text: string) => {
    return request<string>({
        url: `setting/seo/translateToSeoIdentification?chineseText=${encodeURIComponent(text ?? "")}`,
        method: "get",
    });
}

//批量更新商品类目SEO链接
export const batchUpdateCategoryLink = (data: object) => {
    return request({
        url: "setting/seo/batchUpdateCategoryLink",
        method: "post",
        data,
    });
}

//批量更新商品SEO链接
export const batchUpdateProductLink = (data: object) => {
    return request({
        url: "setting/seo/batchUpdateProductLink",
        method: "post",
        data,
    });
}