const API_BASE = '/adminapi';

export interface LoginResult {
  token: string;
  adminType?: string;
}

async function readJson(res: Response) {
  const text = await res.text();
  try {
    return text ? JSON.parse(text) : {};
  } catch {
    throw new Error(text || '接口返回格式异常');
  }
}

export async function getCsrfToken(): Promise<string> {
  const res = await fetch(`${API_BASE}/common/csrf/create`, {
    credentials: 'include',
  });
  const json = await readJson(res);
  if (!res.ok || json.code !== 0) {
    throw new Error(json.msg || '获取 CSRF Token 失败');
  }
  return json.data as string;
}

export async function login(username: string, password: string): Promise<LoginResult> {
  const csrf = await getCsrfToken();
  const res = await fetch(`${API_BASE}/login/signin`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-Token': csrf,
    },
    body: JSON.stringify({
      loginType: 'password',
      username,
      password,
      captchaUid: '',
    }),
  });
  const json = await readJson(res);
  if (!res.ok || json.code !== 0) {
    throw new Error(json.msg || '登录失败');
  }
  return json.data as LoginResult;
}

export function getToken(): string {
  return localStorage.getItem('admin_access_token') || '';
}

export function setToken(token: string) {
  localStorage.setItem('admin_access_token', token);
}

export function clearToken() {
  localStorage.removeItem('admin_access_token');
  localStorage.removeItem('admin_demo');
}

export function isDemo() {
  return localStorage.getItem('admin_demo') === '1';
}

export function enterDemo() {
  localStorage.setItem('admin_demo', '1');
}

export async function apiGet<T>(path: string, params?: Record<string, unknown>): Promise<T> {
  const query = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      if (Array.isArray(value)) {
        value.forEach((item) => {
          if (item !== undefined && item !== null && item !== '') query.append(key, String(item));
        });
      } else {
        query.set(key, String(value));
      }
    }
  });
  const suffix = query.toString() ? `?${query.toString()}` : '';
  const res = await fetch(`${API_BASE}${path}${suffix}`, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
  const json = await readJson(res);
  if (res.status === 401 || json.code === 401) {
    clearToken();
    window.location.reload();
    throw new Error('登录已过期，请重新登录');
  }
  if (!res.ok || json.code !== 0) {
    throw new Error(json.msg || '请求失败');
  }
  return json.data as T;
}

export async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getToken()}`,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const json = await readJson(res);
  if (res.status === 401 || json.code === 401) {
    clearToken();
    window.location.reload();
    throw new Error('登录已过期，请重新登录');
  }
  if (!res.ok || json.code !== 0) {
    throw new Error(json.msg || '请求失败');
  }
  return json.data as T;
}

export async function apiUpload<T>(
  path: string,
  file: File,
  fields?: Record<string, string>,
): Promise<T> {
  const form = new FormData();
  form.append('file', file);
  Object.entries(fields || {}).forEach(([key, value]) => form.append(key, value));
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
    body: form,
  });
  const json = await readJson(res);
  if (res.status === 401 || json.code === 401) {
    clearToken();
    window.location.reload();
    throw new Error('登录已过期，请重新登录');
  }
  if (!res.ok || json.code !== 0) {
    throw new Error(json.msg || '上传失败');
  }
  return json.data as T;
}
