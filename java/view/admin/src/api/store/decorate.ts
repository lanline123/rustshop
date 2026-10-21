import request from "@/utils/request";
import { DecorateStoreFormState } from "@/types/store/decorate.d";
export const getDecorateDetail = () => {
    return request<{ data: DecorateStoreFormState[] }>({
        url: "o2o/decorate/decorate/detail",
        method: "get",
    });
}

export const saveDecorate = (data: object) => {
    return request({
        url: "o2o/decorate/decorate/update",
        method: "post",
        data
    });
}