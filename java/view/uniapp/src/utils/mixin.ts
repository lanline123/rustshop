import { currRoute } from "@/utils";
import { useTabbarStore } from "@/store/tabbar";

export default {
    install(vue: any) {
        vue.mixin({
            onLoad: () => {
                const route = currRoute() || "";
                useTabbarStore().$patch((state) => {
                    state.currRoute = route;
                });
            },
            onShow: () => {
                const route = currRoute() || "";
                useTabbarStore().$patch((state) => {
                    state.currRoute = route;
                });
            },
            onPageScroll() {}
        });
    }
};
