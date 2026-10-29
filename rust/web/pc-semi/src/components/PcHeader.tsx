'use client';

import { useState } from 'react';
import Link from 'next/link';
import type { PcHeaderConfig } from '@/decorate/types';
import { imageUrl } from '@/decorate/components/common';

const FALLBACK_NAV = [
  { title: '首页', link: '/' },
  { title: '全部商品', link: '/category' },
  { title: '回收业务', link: '/list' },
  { title: '工程服务', link: '/list' },
  { title: '资讯中心', link: '/article' },
];

export default function PcHeader({ config }: { config?: PcHeaderConfig | null }) {
  const [keyword, setKeyword] = useState('');
  const header = config || {};
  const logo = imageUrl(header.logoPic);
  const showTopBar = Number(header.showTopMenuBar ?? 1) === 1;
  const showNav = Number(header.showMainNav ?? 1) === 1;
  const navItems: Array<{ title?: string; link?: string; name?: string; label?: string }> = Array.isArray(header.mainNav) && header.mainNav.length ? header.mainNav : FALLBACK_NAV;

  const goSearch = () => {
    if (keyword.trim()) window.location.href = `/search?keyword=${encodeURIComponent(keyword.trim())}`;
  };

  return (
    <header className={`pc-header ${header.isWhite === 0 ? 'pc-header--dark' : ''}`}>
      {showTopBar ? (
        <div className="pc-header__top">
          <div className="pc-container pc-header__top-inner">
            <div>
              <span>{String(header.topRegionText || '中国江西')}</span>
              <span className="pc-header__top-sep">|</span>
              <span>欢迎来到万物新生</span>
            </div>
            <div className="pc-header__top-links">
              <Link href="/login">登录</Link>
              <Link href="/register">注册</Link>
              <Link href="/member">我的账户</Link>
              <Link href="/member/order/list">我的订单</Link>
              <Link href="/help">帮助中心</Link>
            </div>
          </div>
        </div>
      ) : null}

      <div className="pc-container pc-header__main">
        <Link href="/" className="pc-header__brand">
          {logo ? <img src={logo} alt="logo" style={{ width: header.logoWidth || 120 }} /> : <b>万物新生</b>}
        </Link>

        <div className="pc-header__search">
          <span className="pc-header__search-icon">⌕</span>
          <input
            className="pc-header__search-input"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') goSearch();
            }}
            placeholder="搜索商品 / 分类 / 品牌"
          />
          <button type="button" className="pc-header__search-button" onClick={goSearch}>
            搜索
          </button>
        </div>

        <div className="pc-header__actions">
          <Link href="/member" className="pc-header__action">
            <span>我的账户</span>
          </Link>
          <Link href="/cart" className="pc-header__action">
            <span>购物车</span>
          </Link>
        </div>
      </div>

      {showNav ? (
        <nav className="pc-header__nav">
          <div className="pc-container pc-header__nav-inner">
            {navItems.map((item, index) => (
              <Link key={index} href={item.link || '/'}>
                {item.title || item.name || item.label}
              </Link>
            ))}
          </div>
        </nav>
      ) : null}
    </header>
  );
}
