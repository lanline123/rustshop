'use client';

import PcHeader from '@/components/PcHeader';
import { DecorateRenderer } from '@/decorate/components/modules';
import type { DecorateModule } from '@/decorate/types';

const IMG = 'https://nlatstore.cn-qingdao.oss.aliyuncs.com/img/gallery/202511/1763604611KwC30o8HhzqJ3jOLfI.jpeg';

const MODULES: DecorateModule[] = [
  {
    type: 'pc-notice',
    isShow: true,
    moduleIndex: 1,
    module: { noticeTitle: '平台公告', noticeList: [{ picTitle: '新版 PC 商城装修模块视觉回归' }, { picTitle: '退役动力电池循环利用产业服务平台' }] },
  },
  {
    type: 'pc-image-nav',
    isShow: true,
    moduleIndex: 2,
    module: { items: Array.from({ length: 8 }).map((_, index) => ({ picUrl: IMG, picTitle: `导航 ${index + 1}`, picLink: '/search' })) },
  },
  {
    type: 'pc-home-quick-nav',
    isShow: true,
    moduleIndex: 3,
    module: { items: Array.from({ length: 6 }).map((_, index) => ({ picThumb: IMG, picTitle: `快捷入口 ${index + 1}`, picLink: '/search' })) },
  },
  {
    type: 'pc-image-ad',
    isShow: true,
    moduleIndex: 4,
    module: { picType: 1, picList: [{ picUrl: IMG, picLink: '/search', picTitle: '广告位' }] },
  },
  {
    type: 'pc-group-banner',
    isShow: true,
    moduleIndex: 5,
    module: { banners: [{ picUrl: IMG, picLink: '/search' }, { picUrl: IMG, picLink: '/search' }] },
  },
  {
    type: 'pc-product-group',
    isShow: true,
    moduleIndex: 6,
    module: {
      groupTitle: { showTitle: 1 },
      productGroups: [
        { title: '破碎分选', sourceType: 'productGroup', productGroupId: 23 },
        { title: '湿法冶金', sourceType: 'productGroup', productGroupId: 25 },
      ],
    },
  },
  {
    type: 'pc-home-coupon',
    isShow: true,
    moduleIndex: 7,
    module: { title: '领券中心', desc: '先领券再下单', showTitle: 1 },
  },
  {
    type: 'pc-home-seckill',
    isShow: true,
    moduleIndex: 8,
    module: { title: { titleText: '限时秒杀', descText: '每日限时低价' } },
  },
  {
    type: 'pc-home-groupon',
    isShow: true,
    moduleIndex: 9,
    module: { title: { titleText: '拼团活动', descText: '邀请好友一起拼' } },
  },
];

export default function DecoratePreview() {
  return (
    <>
      <PcHeader />
      <main className="pc-container" style={{ padding: '16px 0 60px' }}>
        <h1 className="pc-cart-title">装修模块视觉回归</h1>
        <DecorateRenderer modules={MODULES} />
      </main>
    </>
  );
}
