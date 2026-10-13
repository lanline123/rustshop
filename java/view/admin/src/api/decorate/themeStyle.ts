import request from "@/utils/request";
import type { ThemeStyleFormResult} from "@/types/decorate/themeStyle.d"



export const getThemeStyle  = ( params: object) => {
    return request<ThemeStyleFormResult>({
        url: "decorate/themeStyle/list",
        method: "get",
        params
    });
}


export const saveThemeStyle  = ( params: object) => {
    return request<ThemeStyleFormResult>({
        url: "decorate/themeStyle/saveThemeStyle",
        method: "post",
        data:params
    });
}


