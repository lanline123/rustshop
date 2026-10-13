import { defineStore } from "pinia";
interface State {
    topGalleryId: any;
    topVideoGalleryId: any;
    videoGalleryId: any;
    galleryId: number;
    page: number;
    sortOrder: string;
    isRefresh: boolean;
}
export const useGalleryStore = defineStore("gallery", {
    state: (): State => {
        return {
            videoGalleryId: 0,
            topVideoGalleryId: 0,
            topGalleryId: 0,
            galleryId: 0,
            page: 1,
            sortOrder: "desc",
            isRefresh: false
        };
    },
    getters: {}
});
