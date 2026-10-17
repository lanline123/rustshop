import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for p in ["java/tigshop-security/tigshop-security-api/src/main/java/com/tigshop/security/api/config/WhiteListConfig.java",
          "java/tigshop-security/tigshop-security-admin/src/main/java/com/tigshop/security/admin/config/SecurityConfig.java"]:
    t=open(p,encoding="utf-8",errors="replace").read()
    print("==",p.split("/")[-1])
    for m in re.finditer(r'"(/api[^"]*|/adminapi[^"]*)"',t):
        print("  ",m.group(1))
