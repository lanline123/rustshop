import type { ReactNode } from 'react';
import { Avatar, Button, Input, Layout, Nav, Space, Typography } from '@douyinfe/semi-ui';
import {
  IconBell,
  IconComment,
  IconCreditCard,
  IconExit,
  IconFile,
  IconGift,
  IconHome,
  IconOrderedList,
  IconSearch,
  IconSetting,
  IconShoppingBag,
  IconUser,
} from '@douyinfe/semi-icons';

const { Title, Text } = Typography;

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

const navItems = [
  { itemKey: 'dashboard', text: '数据看板', icon: <IconHome /> },
  { itemKey: 'products', text: '商品管理', icon: <IconShoppingBag /> },
  { itemKey: 'orders', text: '订单管理', icon: <IconOrderedList /> },
  { itemKey: 'members', text: '会员管理', icon: <IconUser /> },
  { itemKey: 'aftersales', text: '售后管理', icon: <IconComment /> },
  { itemKey: 'marketing', text: '营销中心', icon: <IconGift /> },
  { itemKey: 'content', text: '内容管理', icon: <IconFile /> },
  { itemKey: 'finance', text: '财务中心', icon: <IconCreditCard /> },
  { itemKey: 'settings', text: '系统设置', icon: <IconSetting /> },
];

interface AdminLayoutProps {
  page: PageKey;
  onPageChange: (page: PageKey) => void;
  onLogout: () => void;
  children: ReactNode;
}

export default function AdminLayout({ page, onPageChange, onLogout, children }: AdminLayoutProps) {
  return (
    <Layout className="admin-layout">
      <Layout.Sider className="admin-sider" style={{ width: 232 }}>
        <div className="sider-logo">
          <span className="sider-logo-mark">W</span>
          <div>
            <div className="sider-logo-name">万物新生</div>
            <div className="sider-logo-sub">运营管理后台</div>
          </div>
        </div>

        <Nav
          className="admin-nav"
          items={navItems}
          selectedKeys={[page]}
          onSelect={({ itemKey }) => onPageChange(itemKey as PageKey)}
        />
      </Layout.Sider>

      <Layout>
        <Layout.Header className="admin-header">
          <div className="header-left">
            <Title heading={5} style={{ margin: 0 }}>
              {pageTitles[page]}
            </Title>
            <Text type="tertiary" size="small">
              万物新生 · 运营管理后台
            </Text>
          </div>

          <Space spacing={10} align="center">
            <Input
              className="header-search"
              prefix={<IconSearch />}
              placeholder="搜索商品、订单、会员"
              onEnterPress={(value) => {
                const keyword = String(value || '').trim();
                if (keyword) {
                  onPageChange('products');
                }
              }}
            />
            <Button icon={<IconBell />} theme="borderless" />
            <div className="header-user">
              <Avatar color="blue" size="small">
                管
              </Avatar>
              <span>admin</span>
            </div>
            <Button icon={<IconExit />} theme="borderless" onClick={onLogout}>
              退出
            </Button>
          </Space>
        </Layout.Header>

        <Layout.Content className="admin-content">
          <div className="content-inner">{children}</div>
        </Layout.Content>
      </Layout>
    </Layout>
  );
}
