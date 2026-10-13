import type {ReservationTplFilterParams, ReservationTplFilterResult, ReservationTplFilterState} from "@/types/setting/appointmentTpl.d";
import request from "@/utils/request";

export const getReservationTplList = (params: ReservationTplFilterParams) => {
    return request<ReservationTplFilterResult>({
        url: "o2o/reservationTpl/pageList",
        method: "get",
        params,
    });
}
export const getReservationTplAll = (params?: object) => {
    return request<ReservationTplFilterResult>({
        url: "o2o/reservationTpl/list",
        method: "get",
        params
    });
}

export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "o2o/reservationTpl/batch",
        method: "post",
        data: {type, ...data},
    });
}


export const delReservationTpl = (data: object) => {
    return request({
        url: "o2o/reservationTpl/del",
        method: "post",
        data,
    });
}

export const getReservationTpl = (action: string, params: object) => {
    return request<ReservationTplFilterState>({
        url: "o2o/reservationTpl/" + action,
        method: "get",
        params
    });
}

export const updateReservationTpl = (operation: string, data: object) => {
    return request({
        url: "o2o/reservationTpl/" + operation,
        method: "post",
        data
    });
}