import { useState, type ReactNode } from 'react';
import { Avatar, Badge, Button, Input, Tooltip, Typography } from '@douyinfe/semi-ui';
import { IconBell, IconExit, IconHelpCircle, IconMenu, IconSearch } from '@douyinfe/semi-icons';
import { navGroups, pageTitles } from '../config/adminMenu';

const { Text } = Typography;

export type PageKey = string;

interface AdminLayoutProps {
  page: PageKey;
  onPageChange: (page: PageKey) => void;
  onLogout: () => void;
  children: ReactNode;
}

export default function AdminLayout({ page, onPageChange, onLogout, children }: AdminLayoutProps) {
  const [collapsed, setCollapsed] = useState(false);
  const activeGroup =
    navGroups.find((group) =>
      group.items.some(
        (item) => item.key === page || item.children?.some((child) => child.key === page),
      ),
    ) || navGroups[2];

  const activeParent = activeGroup.items.find(
    (item) => item.key === page || item.children?.some((child) => child.key === page),
  );
  const hasThirdLevel = Boolean(activeParent?.children?.length);

  return (
    <div className={`app-shell ${collapsed ? 'is-collapsed' : ''}`}>
      <aside className="sidebar">
        <div className={`brand ${collapsed ? 'brand-collapsed' : ''}`}>
          <span className="brand-mark">W</span>
          {!collapsed && (
            <div className="brand-text">
              <div className="brand-name">万物新生</div>
              <div className="brand-sub">运营管理后台</div>
            </div>
          )}
        </div>

        <div className="sidebar-body">
          <div className="rail">
            {navGroups.map((group) => {
              const active = activeGroup.key === group.key;
              return (
                <Tooltip content={group.label} position="right" disabled={!collapsed} key={group.key}>
                  <button
                    type="button"
                    className={`rail-item ${active ? 'is-active' : ''}`}
                    onClick={() => {
                      const first = group.items[0];
                      onPageChange(first.children?.[0]?.key || first.key);
                    }}
                  >
                    <span className="rail-icon">{group.icon}</span>
                    {!collapsed && <span className="rail-label">{group.label}</span>}
                  </button>
                </Tooltip>
              );
            })}
          </div>

          {!collapsed && (
            <div className="submenu">
              <div className="submenu-title">{activeGroup.label}</div>
              <div className="submenu-list">
                {activeGroup.items.map((item) =>
                  item.children?.length ? (
                    <div className="submenu-section" key={item.key}>
                      <div className="submenu-section-title">{item.label}</div>
                      {item.children.map((child) => (
                        <button
                          type="button"
                          className={`submenu-item submenu-item-child ${
                            page === child.key ? 'is-active' : ''
                          }`}
                          onClick={() => onPageChange(child.key)}
                          key={child.key}
                        >
                          <span className="submenu-dot" />
                          <span>{child.label}</span>
                        </button>
                      ))}
                    </div>
                  ) : (
                    <button
                      type="button"
                      className={`submenu-item ${page === item.key ? 'is-active' : ''}`}
                      onClick={() => onPageChange(item.key)}
                      key={item.key}
                    >
                      <span className="submenu-dot" />
                      <span>{item.label}</span>
                    </button>
                  ),
                )}
              </div>
            </div>
          )}
        </div>

        <div className="side-footer">
          {!collapsed ? (
            <>
              <div className="side-help">
                <IconHelpCircle />
                <span>帮助与文档</span>
              </div>
              <div className="side-version">v0.1.0 · Semi UI</div>
            </>
          ) : (
            <Tooltip content="帮助与文档" position="right">
              <button type="button" className="rail-help">
                <IconHelpCircle />
              </button>
            </Tooltip>
          )}
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
              <span className="breadcrumb-muted">{activeGroup.label}</span>
              {hasThirdLevel && (
                <>
                  <span className="breadcrumb-sep">/</span>
                  <span className="breadcrumb-muted">{activeParent?.label}</span>
                </>
              )}
              <span className="breadcrumb-sep">/</span>
              <span className="breadcrumb-current">{pageTitles[page] || page}</span>
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
                <h1 className="page-title">{pageTitles[page] || page}</h1>
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
