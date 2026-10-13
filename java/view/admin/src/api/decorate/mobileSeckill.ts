import request from '@/utils/request';

// 获取秒杀商品列表
export const getMobileSeckillList = () => {
    return request<any>({
        url: '/promotion/seckill/listForDecorate',
        method: 'get',
    })
}
