import re
t=open("api/admin/src/routes/content/decorate.rs",encoding="utf-8",errors="replace").read()
print("decorate .route count:",t.count(".route("))
for m in re.finditer(r'\.route\(\s*"(/decorate/pcCatFloor[^"]*)"\s*,\s*(\w+)\(',t): print(" ",m.group(1),m.group(2))
print("--- seckill public file ---")
t=open("api/public/src/routes/marketing/seckill.rs",encoding="utf-8",errors="replace").read()
i=t.find("pub fn public_routes")
print(t[i:i+1400])
