import { formatAnimation } from "@/components/modules";
import { computed, ref, onMounted, onUnmounted, getCurrentInstance } from "vue";
import { getElementRect, getImageInfo } from "@/utils";
import { imageFormat } from "@/utils/format";

export const useAnimation = (selector: string, animation: any) => {
    const instance = getCurrentInstance();
    const show = ref(false);
    let observer: any;
    onMounted(() => {
        observer = uni.createIntersectionObserver(instance);
        observer.relativeToViewport({ top: 0, bottom: 10 }).observe(selector, (res: any) => {
            show.value = res.intersectionRatio > 0 ? true : false;
        });
    });
    onUnmounted(() => {
        observer?.disconnect();
    });

    const AnimationFormat = computed(() => {
        return show.value ? `transform: translate(0px, 0px) scale(1) rotateY(0deg);opacity:1;` : formatAnimation(animation.type || "", animation.range || "");
    });

    const transitionDuration = (index: number) => {
        return show.value ? `transition-duration:${index * Number(animation.delay) + Number(animation.duration)}s;` : `transition-duration:0s;`;
    };

    return {
        AnimationFormat,
        show,
        transitionDuration
    };
};

export const useImgInfo = (imgUrl: string, selector: string, immediate = true) => {
    const imgratio = ref(0);
    const getImgratio = async (url: string) => {
        try {
            const res = await getImageInfo(url);
            imgratio.value = res.height / res.width;
        } catch (error) {
            console.error(error);
        }
    };
    const instance = getCurrentInstance();
    const height = ref(0);
    const getHeght = async (selector: string, column?: number) => {
        try {
            const res = await getElementRect(selector, instance);
            if (res) {
                if (column) {
                    height.value = (res.width / column) * imgratio.value;
                } else {
                    height.value = res.width * imgratio.value;
                }
            }
        } catch (error) {
            console.error(error);
        }
    };

    const updateImgInfo = async () => {
        await getImgratio(imageFormat(imgUrl));
        getHeght(selector);
    };

    if (immediate) {
        onMounted(async () => {
            updateImgInfo();
        });
    }

    return {
        height,
        imgratio,
        updateImgInfo,
        getHeght,
        getImgratio
    };
};
