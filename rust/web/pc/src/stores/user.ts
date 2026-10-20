/**
 * 用户状态（Pinia）。
 *
 * 负责：令牌持久化（`tig_user_token` / `tig_user_name`）、登录、资料与用户中心概览，
 * 以及登录失效时的本地清理。所有数据均来自真实接口：
 * - 登录：`POST /api/user/login/signin`
 * - 资料：`GET /api/user/user/detail`
 * - 中心概览：`GET /api/user/user/memberCenter`
 */
import { defineStore } from 'pinia';
import { userApi } from '@/api';
import type { MemberCenter } from '@/api/types';

const TOKEN_KEY = 'tig_user_token';
const NAME_KEY = 'tig_user_name';

interface UserState {
  token: string;
  username: string;
  profile: Record<string, any> | null;
  memberCenter: MemberCenter | null;
  loading: boolean;
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    username: localStorage.getItem(NAME_KEY) || '',
    profile: null,
    memberCenter: null,
    loading: false,
  }),

  getters: {
    isLoggedIn: (state): boolean => Boolean(state.token),
    displayName: (state): string =>
      state.memberCenter?.nickname ||
      state.profile?.nickname ||
      state.memberCenter?.username ||
      state.profile?.username ||
      state.username,
    avatar: (state): string => state.memberCenter?.avatar || state.profile?.avatar || '',
  },

  actions: {
    setSession(token: string, username: string) {
      this.token = token;
      this.username = username;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(NAME_KEY, username);
    },

    clearSession() {
      this.token = '';
      this.username = '';
      this.profile = null;
      this.memberCenter = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(NAME_KEY);
    },

    /** 会员登录（账号 + 密码；短信/微信登录不在本次重构范围）。 */
    async login(username: string, password: string, captchaVerification?: string) {
      const vo = await userApi.login({ username, password, captchaVerification });
      const token = vo.accessToken || vo.token;
      if (!token) {
        throw new Error('登录响应缺少访问令牌');
      }
      this.setSession(token, vo.username || username);
      return vo;
    },

    /** 拉取个人资料（失败时保留旧值，不阻塞页面）。 */
    async fetchProfile() {
      if (!this.token) return null;
      try {
        this.profile = await userApi.profile();
      } catch {
        this.profile = null;
      }
      return this.profile;
    },

    /** 拉取用户中心概览（个人中心/订单角标）。 */
    async fetchMemberCenter() {
      if (!this.token) {
        this.memberCenter = null;
        return null;
      }
      try {
        this.memberCenter = await userApi.memberCenter();
      } catch {
        this.memberCenter = null;
      }
      return this.memberCenter;
    },

    /** 个人中心页面统一加载。 */
    async loadCenter() {
      if (!this.token) return;
      this.loading = true;
      try {
        await Promise.all([this.fetchProfile(), this.fetchMemberCenter()]);
      } finally {
        this.loading = false;
      }
    },

    logout() {
      this.clearSession();
    },
  },
});
