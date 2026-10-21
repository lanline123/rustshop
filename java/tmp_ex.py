import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
# find exchange-related service impl
for dp,_,fs in os.walk("tigshop-service"):
    for f in fs:
        if f in ("PointsExchangeServiceImpl.java","ExchangeServiceImpl.java"):
            p=os.path.join(dp,f); t=open(p,encoding="utf-8",errors="replace").read()
            print("==",p)
            for i,l in enumerate(t.splitlines(),1):
                if re.search(r"orderBy|order by|ORDER BY|points_exchange_id|buildSortOrder|getList",l):
                    print(i,l.strip()[:170])
