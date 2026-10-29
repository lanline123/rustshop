'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card } from '@douyinfe/semi-ui';
import { apiGet } from '@/lib/client-api';

interface PointLog {
  logId?: number;
  points?: string;
  changeTypeName?: string;
  changeTime?: string;
  changeDesc?: string;
  changeType?: number;
}

export default function PointsClient() {
  const [rows, setRows] = useState<PointLog[]>([]);
  const [totalPoints, setTotalPoints] = useState(0);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  useEffect(() => {
    apiGet<{ records?: PointLog[]; userPoints?: number }>('user/pointsLog/list', { page: 1, size: 30 })
      .then((result) => {
        setRows(result.records || []);
        setTotalPoints(Number(result.userPoints || 0));
      })
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
      })
      .finally(() => setLoading(false));
  }, []);

  if (needLogin) {
    return <Card className="cart-state"><h2>登录后查看积分</h2><Link href="/login?redirect=/member/points"><Button theme="solid" type="primary" size="large">去登录</Button></Link></Card>;
  }

  return (
    <div className="member-points">
      <Card className="member-points__summary">
        <span>当前积分</span>
        <b>{totalPoints}</b>
      </Card>
      <Card className="member-card" title="积分明细">
        {loading ? (
          <p>积分记录加载中...</p>
        ) : rows.length ? (
          <div className="member-points__list">
            {rows.map((row, index) => (
              <div key={row.logId || index} className="member-points__row">
                <div>
                  <b>{row.changeTypeName || row.changeDesc || '积分变动'}</b>
                  <span>{row.changeTime}</span>
                </div>
                <b className={Number(row.points) >= 0 ? 'is-plus' : 'is-minus'}>
                  {Number(row.points) >= 0 ? '+' : ''}{row.points}
                </b>
              </div>
            ))}
          </div>
        ) : (
          <p>暂无积分记录</p>
        )}
      </Card>
    </div>
  );
}
