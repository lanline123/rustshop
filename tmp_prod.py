import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
p="java/tigshop-service/src/main/java/com/tigshop/service/product/impl/ProductServiceImpl.java"
t=open(p,encoding="utf-8",errors="replace").read()
for name in ("getProductIds","getProductList"):
    i=t.find("public "+("String" if name=="getProductIds" else "List<ProductListResDTO>")+" "+name)
    if i<0:
        # fallback search
        i=t.find(name+"(")
    print("==",name,"at",t[:i].count("\n")+1 if i>=0 else -1)
    print(t[i:i+1200] if i>=0 else "NOT FOUND")
