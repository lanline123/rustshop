import { useState, type ReactNode } from 'react';
import { Avatar, Badge, Button, Input, Tooltip, Typography } from '@douyinfe/semi-ui';
import {
  IconBell,
  IconComment,
  IconCreditCard,
  IconExit,
  IconFile,
  IconGift,
  IconHelpCircle,
  IconHome,
  IconMenu,
  IconOrderedList,
  IconSearch,
  IconSetting,
  IconShoppingBag,
  IconUser,
} from '@douyinfe/semi-icons';

const { Text } = Typography;

export type PageKey =
  | 'dashboard'
  | 'products'
  | 'orders'
  | 'members'
  | 'aftersales'
  | 'marketing'
  | 'content'
  | 'finance'
  | 'settings';

export const pageTitles: Record<PageKey, string> = {
  dashboard: '数据看板',
  products: '商品管理',
  orders: '订单管理',
  members: '会员管理',
  aftersales: '售后管理',
  marketing: '营销中心',
  content: '内容管理',
  finance: '财务中心',
  settings: '系统设置',
};

const menuGroups: Array<{
  title: string;
  items: Array<{ key: PageKey; label: string; icon: ReactNode }>;
}> = [
  {
    title: '概览',
    items: [{ key: 'dashboard', label: '数据看板', icon: <IconHome /> }],
  },
  {
    title: '交易',
    items: [
      { key: 'products', label: '商品管理', icon: <IconShoppingBag /> },
      { key: 'orders', label: '订单管理', icon: <IconOrderedList /> },
      { key: 'aftersales', label: '售后管理', icon: <IconComment /> },
    ],
  },
  {
    title: '用户',
    items: [{ key: 'members', label: '会员管理', icon: <IconUser /> }],
  },
  {
    title: '内容',
    items: [
      { key: 'content', label: '内容管理', icon: <IconFile /> },
      { key: 'marketing', label: '营销中心', icon: <IconGift /> },
    ],
  },
  {
    title: '系统',
    items: [
      { key: 'finance', label: '财务中心', icon: <IconCreditCard /> },
      { key: 'settings', label: '系统设置', icon: <IconSetting /> },
    ],
  },
];

interface AdminLayoutProps {
  page: PageKey;
  onPageChange: (page: PageKey) => void;
  onLogout: () => void;
  children: ReactNode;
}

export default function AdminLayout({ page, onPageChange, onLogout, children }: AdminLayoutProps) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className={`app-shell ${collapsed ? 'is-collapsed' : ''}`}>
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">W</span>
          {!collapsed && (
            <div className="brand-text">
              <div className="brand-name">万物新生</div>
              <div className="brand-sub">运营管理后台</div>
            </div>
          )}
        </div>

        <nav className="side-nav">
          {menuGroups.map((group) => (
            <div className="nav-group" key={group.title}>
              {!collapsed && <div className="nav-group-title">{group.title}</div>}
              {group.items.map((item) => (
                <Tooltip content={item.label} position="right" key={item.key} disabled={!collapsed}>
                  <button
                    type="button"
                    className={`nav-item ${page === item.key ? 'is-active' : ''}`}
                    onClick={() => onPageChange(item.key)}
                  >
                    <span className="nav-icon">{item.icon}</span>
                    {!collapsed && <span className="nav-label">{item.label}</span>}
                  </button>
                </Tooltip>
              ))}
            </div>
          ))}
        </nav>

        <div className="side-footer">
          {!collapsed ? (
            <div className="side-help">
              <IconHelpCircle />
              <span>帮助与文档</span>
            </div>
          ) : (
            <Tooltip content="帮助与文档" position="right">
              <button type="button" className="nav-item">
                <span className="nav-icon">
                  <IconHelpCircle />
                </span>
              </button>
            </Tooltip>
          )}
          {!collapsed && <div className="side-version">v0.1.0 · Semi UI</div>}
        </div>
      </aside>

      <div className="main-shell">
        <header className="topbar">
          <div className="topbar-left">
            <Button
              icon={<IconMenu />}
              theme="borderless"
              className="collapse-btn"
              onClick={() => setCollapsed((value) => !value)}
            />
            <div className="breadcrumb">
              <span className="breadcrumb-muted">首页</span>
              <span className="breadcrumb-sep">/</span>
              <span className="breadcrumb-current">{pageTitles[page]}</span>
            </div>
          </div>

          <div className="topbar-right">
            <Input
              className="topbar-search"
              prefix={<IconSearch />}
              placeholder="搜索商品、订单、会员"
              onEnterPress={(value) => {
                if (String(value || '').trim()) onPageChange('products');
              }}
            />
            <Badge count={3} dot>
              <Button icon={<IconBell />} theme="borderless" className="icon-btn" />
            </Badge>
            <div className="account">
              <Avatar color="blue" size="small">
                管
              </Avatar>
              <div className="account-text">
                <span className="account-name">admin</span>
                <span className="account-role">平台管理员</span>
              </div>
            </div>
            <Button icon={<IconExit />} theme="borderless" className="icon-btn" onClick={onLogout} />
          </div>
        </header>

        <main className="page-content">
          <div className="page-inner">
            <div className="page-heading">
              <div>
                <h1 className="page-title">{pageTitles[page]}</h1>
                <Text type="tertiary" size="small">
                  万物新生 · 运营管理后台
                </Text>
              </div>
            </div>
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
