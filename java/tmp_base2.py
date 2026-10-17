import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
p="tigshop-service/src/main/java/com/tigshop/service/common/impl/BaseServiceImpl.java"
t=open(p,encoding="utf-8",errors="replace").read()
for m in re.finditer(r"buildSortOrder\([^)]*String[^)]*\)\s*\{",t):
    i=m.start(); print(t[i-120:i+1100]); print("-----")
