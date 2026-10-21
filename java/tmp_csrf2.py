import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for p in ["tigshop-security/tigshop-security-admin/src/main/java/com/tigshop/security/admin/filter/LoginFilter.java",
          "tigshop-security/tigshop-security-admin/src/main/java/com/tigshop/security/admin/config/SecurityConfig.java",
          "tigshop-security/tigshop-security-api/src/main/java/com/tigshop/security/api/filter/ClientLoginFilter.java",
          "tigshop-security/tigshop-security-api/src/main/java/com/tigshop/security/api/config/WhiteListConfig.java",
          "tigshop-common/src/main/java/com/tigshop/common/constant/Constants.java"]:
    t=open(p,encoding="utf-8",errors="replace").read()
    lines=t.splitlines()
    for i,l in enumerate(lines):
        if re.search(r"csrf|CSRF",l,re.I):
            print("==",p.split("/")[-1],i+1)
            print("\n".join(lines[max(0,i-4):i+10]))
            print()
