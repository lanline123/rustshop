<template>
    <tig-layout>
        <view class="user-panel-block">
            <view class="user-info" @click="goPages('/pages/salesman/personalInfo')">
                <view class="user-info__avatar salesman-avatar">
                    <tig-image v-if="member.avatar" class="salesman-avatar__img salesman-avatar--round salesman-avatar__img--default" :src="member.avatar" />
                    <image v-else :src="'salesman/avatar.png'" class="salesman-avatar__img salesman-avatar--round salesman-avatar__img--default" />
                    <image :src="staticResource('salesman/viprank.png')" :alt="$t('等级1')" class="avatar-level-tag__img" />
                </view>
                <view class="user-info__content">
                    <view class="user-info__title">{{ $t(member.nickname) }}</view>
                    <view class="user-info__desc">{{ $t("手机号：") }}{{ member.mobile }}</view>
                </view>
                <up-icon name="arrow-right" color="#c8c9cc" size="16" />
            </view>
            <view v-if="salesmanData.level && salesmanData.salesmanConfig.level.length > 1" class="level-panel" @click="goPages('/pages/salesman/level')">
                <view class="upgrade-group">
                    <view class="van-image">
                        <image :src="staticResource('salesman/uplevel.png')" class="van-image__img" />
                    </view>
                </view>
                <view class="van-ellipsis">
                    <view class="level-panel__header">
                        <view class="level-name">{{ $t(salesmanData.levelName) }}</view>
                        <view class="level-icon van-image" style="width: 20px; height: 11px">
                            <image :src="levelIcon(salesmanData.level)" class="van-image__img" />
                        </view>
                        <view class="van-image" style="width: 8px; height: 8px">
                            <image :src="staticResource('salesman/toright.png')" class="van-image__img" />
                        </view>
                    </view>
                    <view class="level-panel__desc van-ellipsis">{{ $t(levelTxt) }}</view>
                </view>
            </view>
        </view>
        <view class="data-panel-block">
            <view class="main-data-panel van-grid">
                <image :src="staticResource('salesman/shouyi.png')" class="moeny-icon" />
                <view class="main-data-panel-item van-grid-item">
                    <view class="van-grid-item__content">
                        <view class="main-data-panel-item__desc">{{ $t("今日收益") }}</view>
                        <view class="main-data-panel-item__content"
                            ><format-price base :show-text="false" :currency-format="false" :price-data="salesmanData.order?.todaySum" />{{ incomeUnit }}</view
                        >
                    </view>
                </view>
                <view class="main-data-panel-item van-grid-item">
                    <view class="van-grid-item__content">
                        <view class="main-data-panel-item__desc">{{ $t("总收益") }}</view>
                        <view class="main-data-panel-item__content"
                            ><format-price base :show-text="false" :currency-format="false" :price-data="salesmanData.order?.allSum" />{{ incomeUnit }}</view
                        >
                        <view class="main-data-panel-item__desc main-data-panel-item__extra">
                            <view class="main-data-panel-item__text">
                                {{ $t("含待结算") }}
                            </view>
                            <format-price base :show-text="false" :currency-format="false" :price-data="salesmanData.order?.waitCpsSum" />
                            <view>
                                {{ incomeUnit }}
                            </view>
                            <!-- <up-icon name="arrow-right" color="#fff" size="12"></up-icon> -->
                        </view>
                    </view>
                </view>
            </view>
            <block v-if="showDataDetail">
                <view v-if="salesmanData.order" class="detail-data-panel van-grid">
                    <view class="van-grid-item" @click="goPages('/pages/salesman/orderPromote')">
                        <view class="van-grid-item__content">
                            <view class="detail-data-panel-item">
                                <view class="detail-data-panel-item__content">
                                    <view class="detail-data-panel-item__title">{{ $t("今日推广订单") }}</view>
                                    <view class="detail-data-panel-item__text">{{ salesmanData.order?.todayCount }}</view>
                                </view>
                            </view>
                        </view>
                    </view>
                    <view class="van-grid-item" @click="goPages('/pages/salesman/orderPromote')">
                        <view class="van-grid-item__content">
                            <view class="detail-data-panel-item">
                                <view class="detail-data-panel-item__content">
                                    <view class="detail-data-panel-item__title">{{ $t("累计推广订单") }}</view>
                                    <view class="detail-data-panel-item__text">{{ salesmanData.order?.allCount }}</view>
                                </view>
                            </view>
                        </view>
                    </view>
                    <view class="van-grid-item" @click="goCustomerList('/pages/salesman/relationList?customerType=today')">
                        <view class="van-grid-item__content">
                            <view class="detail-data-panel-item">
                                <view class="detail-data-panel-item__content">
                                    <view class="detail-data-panel-item__title">{{ $t("今日新增客户") }}</view>
                                    <view class="detail-data-panel-item__text">{{ salesmanData.order?.todayUserCount }}</view>
                                </view>
                            </view>
                        </view>
                    </view>
                    <view class="van-grid-item" @click="goCustomerList('/pages/salesman/relationList?customerType=all')">
                        <view class="van-grid-item__content">
                            <view class="detail-data-panel-item">
                                <view class="detail-data-panel-item__content">
                                    <view class="detail-data-panel-item__title">{{ $t("累计客户") }}</view>
                                    <view class="detail-data-panel-item__text">{{ salesmanData.order?.allUserCount }}</view>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
            </block>
            <view class="cash-block" @click="goPages('/pages/salesman/wallet')">
                <view role="button" tabindex="0" class="van-cell van-cell--clickable">
                    <view class="van-cell__title">
                        <text>{{ $t("可提现金额") }}({{ incomeUnit }})</text>
                    </view>
                    <view class="van-cell__value cash-block--withdraw">
                        <text>{{ member.commissionBalance || 0 }}</text>
                    </view>
                    <up-icon name="arrow-right" color="#c8c9cc" size="12" />
                </view>
                <view class="asset-icon" @click.stop="showTipClick">
                    <up-icon name="question-circle" color="#969799" size="14" />
                </view>
            </view>
            <view class="accordion__action-bar" @click="showClick">
                <view v-if="showDataDetail === false">{{ $t("展开更多") }}</view>
                <up-icon v-if="showDataDetail === false" name="arrow-down" color="#969799" size="14" />
                <up-icon v-else name="arrow-up" color="#969799" size="14" />
            </view>
        </view>
        <view class="promotion-block">
            <view class="van-cell van-cell--borderless">
                <view class="van-cell__title promotion-block__title">
                    <text>{{ $t("推广卖货") }}</text>
                </view>
            </view>
            <view class="promotion-content van-grid">
                <view class="promotion-item van-grid-item">
                    <view class="van-grid-item__content van-grid-item__content--horizontal van-hairline" @click="goPages('/pages/salesman/list')">
                        <view>
                            <view class="promotion-item__title">{{ $t("推广商品") }}</view>
                            <view class="promotion-item__desc">{{ $t("佣金赚不够") }}</view>
                        </view>
                        <view class="van-image">
                            <image :src="staticResource('salesman/goods.png!small.webp')" class="van-image__img" />
                        </view>
                    </view>
                </view>
                <view class="promotion-item van-grid-item" @click="goPages('/pages/salesman/portalPoster')">
                    <view class="van-grid-item__content van-grid-item__content--horizontal van-hairline">
                        <view>
                            <view class="promotion-item__title">{{ $t("推广海报") }}</view>
                            <view class="promotion-item__desc">{{ $t("发圈快速获客") }}</view>
                        </view>
                        <view class="van-image">
                            <image :src="staticResource('salesman/poster.png!small.webp')" class="van-image__img" />
                        </view>
                    </view>
                </view>
                <!-- <view class="promotion-item van-grid-item">
                        <view class="van-grid-item__content van-grid-item__content--horizontal van-hairline">
                            <view>
                                <view class="promotion-item__title">邀请好友</view>
                                <view class="promotion-item__desc">可获得邀请佣金</view>
                            </view>
                            <view class="van-image">
                                <image :src="staticResource('salesman/invitation.png!small.webp')" class="van-image__img" />
                            </view>
                        </view>
                    </view> -->
                <view class="promotion-item van-grid-item" @click="goPages('/pages/salesman/material')">
                    <view class="van-grid-item__content van-grid-item__content--horizontal van-hairline">
                        <view>
                            <view class="promotion-item__title">{{ $t("素材中心") }}</view>
                            <view class="promotion-item__desc">{{ $t("一键转发分享素材") }}</view>
                        </view>
                        <view class="van-image">
                            <image :src="staticResource('salesman/material.png!small.webp')" class="van-image__img" />
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <view class="operation-block">
            <view class="van-cell van-cell--borderless">
                <view class="van-cell__title operation-block__title">
                    <text>{{ $t("激励工具") }}</text>
                </view>
            </view>
            <view class="van-grid">
                <!-- <view class="van-grid-item" style="flex-basis: 25%">
                        <view class="van-grid-item__content van-grid-item__content--center">
                            <image
                                style="height: 100%; width: 100%"
                                src="data:image/svg+xml;base64,ICAgICAgICAgICAgICAgICAgPHN2ZyB3aWR0aD0iMzAiIGhlaWdodD0iMzAiIHZpZXdCb3g9IjAgMCAzMCAzMCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8ZyBtYXNrPSJ1cmwoI21hc2swXzEyMF83MjEyKSI+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxjaXJjbGUgb3BhY2l0eT0iMC41IiBjeD0iMTYuNSIgY3k9IjE1LjUiIHI9IjguNSIgZmlsbD0iI2Y0YzQ5ZiI+PC9jaXJjbGU+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxjaXJjbGUgY3g9IjEzIiBjeT0iMTMiIHI9IjMiIHN0cm9rZT0iIzMyMzIzMyIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2VMaW5lY2FwPSJyb3VuZCIgc3Ryb2tlTGluZWpvaW49InJvdW5kIj48L2NpcmNsZT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHBhdGgKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGQ9Ik0xNiAxM0MxOC42Njc2IDEyLjc5MyAyMC44Mjc0IDEzLjcyMTggMjAuOTgzMiAxNS4yOTYyQzIxLjE4ODIgMTcuMzY4OCAxOS41Mzk2IDE4LjgyNDEgMTUuMDM4IDIwLjU5NzdDMTMuMDMxOCAyMS4zODgxIDExLjg5NzMgMjIuNDIxNiAxMS4zNjk2IDIzLjM5NzMiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzdHJva2U9IiMzMjMyMzMiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzdHJva2Utd2lkdGg9IjIiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzdHJva2VMaW5lY2FwPSJyb3VuZCIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0cm9rZUxpbmVqb2luPSJyb3VuZCIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPjwvcGF0aD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGNpcmNsZSBjeD0iMTUiIGN5PSIxNCIgcj0iMTEiIHN0cm9rZT0iIzMyMzIzMyIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2VMaW5lY2FwPSJyb3VuZCIgc3Ryb2tlTGluZWpvaW49InJvdW5kIj48L2NpcmNsZT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHBhdGggZD0iTTIyIDIzTDI1LjUzNTUgMjYuNTM1NSIgc3Ryb2tlPSIjMzIzMjMzIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZUxpbmVjYXA9InJvdW5kIiBzdHJva2VMaW5lam9pbj0icm91bmQiPjwvcGF0aD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2c+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGRlZnM+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxsaW5lYXJHcmFkaWVudCBpZD0icGFpbnQwX2xpbmVhcl8xMjBfNzIxMiIgeDE9Ii0wLjUiIHkxPSIxNS41IiB4Mj0iMTYuNSIgeTI9IjMyLjUiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxzdG9wIGNsYXNzPSJncmFkaWVudF9fZmlsbCI+PC9zdG9wPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHN0b3Agb2Zmc2V0PSIxIiBjbGFzcz0iZ3JhZGllbnRfX2ZpbGxfZW5kIj48L3N0b3A+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9kZWZzPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9zdmc+Cg=="
                            ></image>
                            <view class="operation-item-text">销售商机</view>
                        </view>
                    </view> -->
                <view class="van-grid-item" style="flex-basis: 25%" @click="goPages('/pages/salesman/notifyCenter')">
                    <view class="van-grid-item__content van-grid-item__content--center">
                        <image
                            style="height: 100%; width: 100%"
                            src="data:image/svg+xml;base64,IDxzdmcgZGF0YS12LTc2YzAyMDIyPSIiIHdpZHRoPSIzMCIgaGVpZ2h0PSIzMCIgdmlld0JveD0iMCAwIDMwIDMwIiBmaWxsPSJub25lIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxnIG1hc2s9InVybCgjbWFzazBfMTIwXzcyMzgpIj4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHJlY3Qgb3BhY2l0eT0iMC41IiB4PSI4IiB5PSIxMi41IiB3aWR0aD0iMTciIGhlaWdodD0iMTQiIHJ4PSIxIiBmaWxsPSIjZjRjNDlmIj48L3JlY3Q+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxwYXRoIGQ9Ik05LjMzNzY1IDcuNUgyNFYyNi41SDVWNS41IiBzdHJva2U9IiMzMjMyMzMiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlTGluZWpvaW49InJvdW5kIj48L3BhdGg+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxwYXRoCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBkPSJNNSA1LjVDNSA0LjM5NTQzIDUuODk1NDMgMy41IDcgMy41SDIyVjcuNUg3QzUuODk1NDMgNy41IDUgNi42MDQ1NyA1IDUuNVY1LjVaIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3Ryb2tlPSIjMzIzMjMzIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3Ryb2tlLXdpZHRoPSIyIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3Ryb2tlTGluZWpvaW49InJvdW5kIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA+PC9wYXRoPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8cGF0aCBkPSJNOSAxOS41VjI1LjUiIHN0cm9rZT0iIzMyMzIzMyIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2VMaW5lY2FwPSJyb3VuZCI+PC9wYXRoPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvZz4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8ZGVmcz4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGxpbmVhckdyYWRpZW50IGlkPSJwYWludDBfbGluZWFyXzEyMF83MjM4IiB4MT0iLTAuNSIgeTE9IjE5LjUiIHgyPSIxMy4yNDAyIiB5Mj0iMzYuMTg0NSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHN0b3AgY2xhc3M9ImdyYWRpZW50X19maWxsIj48L3N0b3A+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8c3RvcCBvZmZzZXQ9IjEiIGNsYXNzPSJncmFkaWVudF9fZmlsbF9lbmQiPjwvc3RvcD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9saW5lYXJHcmFkaWVudD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2RlZnM+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3N2Zz4K"
                        />
                        <view class="operation-item-text">{{ $t("赚钱攻略") }}</view>
                    </view>
                </view>
                <view class="van-grid-item" style="flex-basis: 25%" @click="goPages('/pages/salesman/businessCard')">
                    <view class="van-grid-item__content van-grid-item__content--center">
                        <image
                            style="height: 30px; width: 100%"
                            src="data:image/svg+xml;base64,PHN2ZyBkYXRhLXYtNzZjMDIwMjI9IiIgd2lkdGg9IjMwIiBoZWlnaHQ9IjMwIiB2aWV3Qm94PSIwIDAgMzAgMzAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGcgbWFzaz0idXJsKCNtYXNrMF8xMjBfNzIwMCkiPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8cmVjdCBvcGFjaXR5PSIwLjUiIHg9IjciIHk9IjEyIiB3aWR0aD0iMjAiIGhlaWdodD0iMTIiIHJ4PSIxIiBmaWxsPSIjZjRjNDlmIj48L3JlY3Q+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxyZWN0IHg9IjMiIHk9IjciIHdpZHRoPSIyNCIgaGVpZ2h0PSIxNyIgc3Ryb2tlPSIjMzIzMjMzIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZUxpbmVqb2luPSJyb3VuZCI+PC9yZWN0PgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8cGF0aCBkPSJNOCAxOEgyMCIgc3Ryb2tlPSIjMzIzMjMzIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZUxpbmVjYXA9InJvdW5kIj48L3BhdGg+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxwYXRoIGQ9Ik04IDEzSDE0IiBzdHJva2U9IiMzMjMyMzMiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlTGluZWNhcD0icm91bmQiPjwvcGF0aD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2c+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGRlZnM+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxsaW5lYXJHcmFkaWVudCBpZD0icGFpbnQwX2xpbmVhcl8xMjBfNzIwMCIgeDE9Ii0zIiB5MT0iMTgiIHgyPSI3LjU4ODI0IiB5Mj0iMzUuNjQ3MSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHN0b3AgY2xhc3M9ImdyYWRpZW50X19maWxsIj48L3N0b3A+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8c3RvcCBvZmZzZXQ9IjEiIGNsYXNzPSJncmFkaWVudF9fZmlsbF9lbmQiPjwvc3RvcD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9saW5lYXJHcmFkaWVudD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2RlZnM+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3N2Zz4K"
                        />
                        <view class="operation-item-text">{{ $t("个人名片") }}</view>
                    </view>
                </view>
            </view>
        </view>
    </tig-layout>
    <view v-if="showTip" class="tip-bubble">
        {{ $t("推广订单佣金将在系统结算后发放到账，即可提现, 该金额为可在当前店铺提现的金额总额。") }}
    </view>
</template>

<script lang="ts" setup>
/**
 * 分销员中心首页
 * - 展示分销员等级、佣金统计、推广数据
 * - 提供推广商品、客户管理、提现等功能入口
 * - 显示分销员专属二维码
 */
import { ref, computed } from "vue";
import { getSalesman, getUser } from "@/api/salesman/salesmanCenter";
import type { UserItem, SalesmanItem } from "@/types/salesman/salesmanCenter";
import { onShow } from "@dcloudio/uni-app";
import { isOverseas, staticResource, redirect } from "@/utils";
import { useSafeAreaInsets } from "@/hooks";
import { useCurrency } from "@/hooks/useCurrency";
import { useI18n } from "vue-i18n";
import { isViewCustomerInfoEnabled } from "@/pages/salesman/src/salesmanPrivacy";
const { t } = useI18n();
const { baseCurrencyName } = useCurrency();
const incomeUnit = computed(() => (isOverseas() ? baseCurrencyName.value : t("元")));

const { saveTop } = useSafeAreaInsets();
const showDataDetail = ref(false);
const showClick = () => {
    showDataDetail.value = !showDataDetail.value;
};
const showTip = ref(false);
let tipTimer: any = null;
const showTipClick = () => {
    showTip.value = !showTip.value;
    clearTimeout(tipTimer);
    if (showTip.value) {
        tipTimer = setTimeout(() => {
            showTip.value = false;
        }, 2000);
    }
};
const member = ref<UserItem>({} as UserItem);
// 拉取最新用户信息（含分销佣金可用余额 commissionBalance，D1107），避免使用过期缓存
const _getUser = async () => {
    try {
        const result = await getUser();
        member.value = result;
    } catch (error: any) {
        member.value = uni.getStorageSync("userInfo");
    }
};

const salesmanData = ref<SalesmanItem>({} as SalesmanItem);
const _getSalesman = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getSalesman();
        salesmanData.value = result;
    } catch (error: any) {
        uni.showToast({
            title: t(error.message),
            icon: "none"
        });
        setTimeout(function () {
            redirect({
                url: "/pages/member/index"
            });
        }, 1500);
    } finally {
        uni.hideLoading();
    }
};

const levelIcon = (num: number) => {
    let levelIconUrl = "";
    switch (num) {
        case 1:
            levelIconUrl = staticResource("salesman/icon-level1.png");
            break;
        case 2:
            levelIconUrl = staticResource("salesman/icon-level2.png");
            break;
        case 3:
            levelIconUrl = staticResource("salesman/icon-level3.png");
            break;
        case 4:
            levelIconUrl = staticResource("salesman/icon-level4.png");
            break;
        case 5:
            levelIconUrl = staticResource("salesman/icon-level5.png");
            break;
        case 6:
            levelIconUrl = staticResource("salesman/icon-level6.png");
            break;
    }
    return levelIconUrl;
};

const levelTxt = computed(() => {
    let str = "";
    if (salesmanData.value.condition) {
        for (const key in salesmanData.value.condition) {
            if (salesmanData.value.condition[key]?.checked) {
                const { value = 0, title = "", unit = t("元") } = salesmanData.value.condition[key];
                const isAmount = key === "salesAmount" || key === "selfBuyAmount";
                const showUnit = isAmount && isOverseas() ? baseCurrencyName.value || unit : unit;
                str += `${title}${t("已达")}${value}${showUnit}、`;
            }
        }
    }
    return str;
});

const goPages = (url: string) => {
    uni.navigateTo({
        url
    });
};

const goCustomerList = (url: string) => {
    if (!isViewCustomerInfoEnabled(salesmanData.value.salesmanConfig?.viewCustomerInfo)) {
        return;
    }
    goPages(url);
};

const panelBlockBg = computed(() => {
    return `url(${staticResource("salesman/bg-header.png!middle.webp")})`;
});
const levelPanelBg = computed(() => {
    return `url(${staticResource("salesman/levelbg.png")})`;
});

onShow(() => {
    _getUser();
    _getSalesman();
});
</script>

<style lang="scss" scoped>
.user-panel-block {
    position: relative;
    box-sizing: border-box;
    width: 100vw;
    padding: 0 24rpx;
    overflow: hidden;
    background: v-bind(panelBlockBg) 0rpx 0rpx / 100% 100% no-repeat;
    padding-top: v-bind("saveTop + 12 + 'px'");
    .user-info {
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 40rpx 0;
        .user-info__avatar {
            margin-right: 14rpx;
            border: 6rpx solid hsla(0, 0%, 100%, 0.1);
            border-radius: 50rpx;
            .avatar-level-tag__img {
                position: absolute;
                right: 0;
                bottom: 0;
                width: 32rpx;
                height: 32rpx;
            }
        }
        .user-info__content {
            width: 100%;
            color: #fff;
            .user-info__title {
                margin-bottom: 12rpx;
                line-height: 48rpx;
                font-size: 36rpx;
                word-break: break-all;
            }
            .user-info__desc {
                line-height: 32rpx;
                font-size: 24rpx;
                opacity: 0.5;
            }
        }
    }
    .level-panel {
        display: flex;
        align-items: center;
        box-sizing: border-box;
        width: 100%;
        height: 122rpx;
        padding: 0 24rpx 0 0;
        color: #fff;
        background: v-bind(levelPanelBg) 0rpx 0rpx / 100% 100% no-repeat;
        .upgrade-group {
            width: 44rpx;
            margin-right: 14rpx;
            .van-image {
                display: block;
            }
        }
        .van-ellipsis {
            flex-grow: 1;
        }
        .level-panel__header {
            display: flex;
            align-items: center;
            color: #fff;
            column-gap: 4rpx;
            & > .level-name {
                line-height: 40rpx;
                font-size: 30rpx;
                font-weight: 700;
                margin-right: 4rpx;
            }
        }
        .level-panel__desc {
            line-height: 32rpx;
            font-size: 24rpx;
            margin-top: 8rpx;
        }
    }
}
.detail-data-panel {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    padding: 0 32rpx;
    .van-grid-item {
        &:after {
            position: absolute;
            box-sizing: border-box;
            content: " ";
            pointer-events: none;
            top: -50%;
            right: -50%;
            bottom: -50%;
            left: -50%;
            border: 0 solid #ebedf0;
            transform: scale(0.5);
            border-bottom-width: 2rpx;
        }
        &:nth-child(odd) .detail-data-panel-item__content {
            padding-bottom: 24rpx;
        }
        &:nth-child(2n) .detail-data-panel-item__content {
            padding-bottom: 24rpx;
            padding-left: 32rpx;
        }
    }
    .van-grid-item__content {
        padding: 24rpx 0 0;
        .detail-data-panel-item {
            .detail-data-panel-item__title {
                margin-bottom: 12rpx;
                color: #969799;
                font-size: 24rpx;
                line-height: 32rpx;
            }
            .detail-data-panel-item__text {
                color: #323233;
                font-size: 36rpx;
                line-height: 48rpx;
            }
        }
    }
}
.data-panel-block {
    position: relative;
    margin: 24rpx;
    background-color: #fff;
    overflow: hidden;
    .main-data-panel {
        width: 100%;
        min-height: 200rpx;
        color: #fff;
        border-radius: 8rpx;
        position: relative;
        background: linear-gradient(135deg, rgb(233, 181, 67) 0%, rgb(238, 118, 50) 100%);
        .moeny-icon {
            position: absolute;
            bottom: 0;
            right: 0;
            width: 220rpx;
        }
        .main-data-panel-item {
            flex-basis: 50%;
            .van-grid-item__content {
                padding: 32rpx;
                background: transparent;
            }
            .main-data-panel-item__desc {
                margin-bottom: 16rpx;
                line-height: 32rpx;
                font-size: 24rpx;
                opacity: 0.8;
            }
            .main-data-panel-item__content {
                line-height: 48rpx;
                font-size: 40rpx;
                font-weight: 700;
            }
            .main-data-panel-item__extra {
                margin-top: 8rpx;
                display: flex;
                align-items: center;
                .main-data-panel-item__text {
                    flex: 1;
                }
            }
        }
    }
    .cash-block {
        background-color: #fff;
        position: relative;
        .van-cell {
            position: relative;
            border-radius: 0 0 8rpx 8rpx;
            padding: 20rpx 24rpx;
            .icon-youjiantou {
                color: #c8c9cc;
            }
        }
        .cash-block--withdraw {
            font-size: 32rpx;
            color: #ff720d;
            font-weight: 700;
        }
        .asset-icon {
            position: absolute;
            top: 32rpx;
            left: 220rpx;
        }
    }
    .accordion__action-bar {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 48rpx;
        line-height: 48rpx;
        text-align: center;
        & > view {
            margin-right: 8rpx;
            font-size: 24rpx;
            color: #969799;
        }
    }
}
.promotion-block {
    margin: 0 24rpx 24rpx;
    background-color: #fff;
    border-radius: 8rpx;
    .van-cell {
        border-radius: 8rpx;
    }
    .promotion-block__title {
        font-weight: 700;
    }
    .promotion-content {
        border-radius: 8rpx;
        .promotion-item {
            flex-basis: 50%;
            .van-grid-item__content {
                justify-content: space-between;
                padding: 24rpx 24rpx;
            }
        }
        .promotion-item__title {
            margin-bottom: 8rpx;
            line-height: 40rpx;
            font-size: 28rpx;
            font-weight: 700;
        }
        .promotion-item__desc {
            line-height: 36rpx;
            font-size: 24rpx;
            color: #969799;
        }
        .van-image {
            width: 88rpx;
            height: 88rpx;
        }
    }
}
.operation-block {
    margin: 24rpx 24rpx 0;
    background-color: #fff;
    overflow: hidden;
    border-radius: 8rpx;
    &.block--disabled {
        filter: grayscale(1);
    }
    .operation-block__title {
        font-weight: 700;
    }
    .operation-item-text {
        color: #646566;
        font-size: 25rpx;
        line-height: 1.5;
        word-break: break-all;
        margin-top: 16rpx;
    }
}

// flex布局
.van-grid {
    display: flex;
    flex-wrap: wrap;
    .van-grid-item {
        position: relative;
        box-sizing: border-box;
    }
    .van-grid-item__content {
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
        height: 100%;
        padding: 32rpx 16rpx;
        background-color: #fff;
        &:after {
            z-index: 1;
            border-width: 0 2rpx 2rpx 0;
        }
    }
    .van-grid-item__content--center {
        align-items: center;
        justify-content: center;
    }
    .van-grid-item__content--horizontal {
        flex-direction: row;
    }
    .van-grid-item__text {
        color: #646566;
        font-size: 24rpx;
        word-break: break-all;
    }
}
.van-cell {
    position: relative;
    display: flex;
    box-sizing: border-box;
    width: 100%;
    padding: 20rpx 32rpx;
    overflow: hidden;
    color: #323233;
    font-size: 28rpx;
    line-height: 48rpx;
    background-color: #fff;
    .van-cell--clickable {
        cursor: pointer;
    }
    .van-cell__title,
    .van-cell__value {
        flex: 1;
    }
    .van-cell__value {
        position: relative;
        overflow: hidden;
        color: #969799;
        text-align: right;
        vertical-align: middle;
        word-wrap: break-word;
    }
}
.van-ellipsis {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}
// 头像
.salesman-avatar {
    position: relative;
    display: inline-block;
    .salesman-avatar__img {
        display: block;
        margin: 0 auto;
    }
    .salesman-avatar--round {
        border-radius: 100%;
        overflow: hidden;
    }
    .salesman-avatar__img--default {
        height: 100rpx;
        width: 100rpx;
    }
    .salesman-avatar__img--big {
        height: 120rpx;
        width: 120rpx;
    }
}
// 图片icon
.van-icon {
    position: relative;
    display: flex;
    align-items: center;
    text-rendering: auto;
    .van-icon__image {
        width: 32rpx;
        height: 32rpx;
        object-fit: contain;
    }
}
.van-image {
    position: relative;
    display: inline-block;
}
.van-image__error,
.van-image__img,
.van-image__loading {
    display: block;
    width: 100%;
    height: 100%;
}
[class*="van-hairline"]:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    top: -50%;
    right: -50%;
    bottom: -50%;
    left: -50%;
    border: 0 solid #ebedf0;
    transform: scale(0.5);
}
.tip-bubble {
    position: fixed;
    top: 20vh;
    left: 50%;
    transform: translateX(-50%);
    width: 70%;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    font-size: 28rpx;
    line-height: 1.5;
    padding: 20rpx 30rpx;
    border-radius: 16rpx;
    z-index: 10075;
}
</style>
