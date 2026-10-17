import re,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
t=open("crates/bin/src/main.rs",encoding="utf-8",errors="replace").read()
lines=re.findall(r'let\s+(\w+)\s*=\s*Arc::new\((\w+)::new\(([^;]*)\)\);',t)
print("constructed services:",len(lines))
for var,ty,args in lines:
    print("  %-12s %-22s args=%s"%(var,ty,args.strip()[:60]))
