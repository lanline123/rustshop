import re,os
files=["api/public/src/routes/logistics/shipping_list.rs","api/public/src/routes/marketing/salesman.rs","api/public/src/routes/marketing/seckill.rs","api/public/src/routes/marketing/time_discount.rs","api/public/src/routes/merchant/shop.rs","api/public/src/routes/payment/balance_pay.rs"]
for f in files:
    t=open(f,encoding="utf-8",errors="replace").read()
    routes=t.count(".route(")
    fns=re.findall(r"pub fn (\w+)",t)
    print("%-58s .route=%d fns=%s bytes=%d"%(f,routes,",".join(fns),len(t)))
