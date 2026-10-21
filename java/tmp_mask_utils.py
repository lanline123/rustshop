import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
targets={"AnonymousMaskUtils.java":None,"StringUtils.java":None}
for dp,_,fs in os.walk("."):
    for f in fs:
        if f in targets: targets[f]=os.path.join(dp,f)
print(targets)
if targets["AnonymousMaskUtils.java"]:
    t=open(targets["AnonymousMaskUtils.java"],encoding="utf-8",errors="replace").read()
    print(t)
