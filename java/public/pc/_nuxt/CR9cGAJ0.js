function u(o,t,e=1){if(!Number.isFinite(t)||!Number.isFinite(o)||t<=0||o>=t)return 0;const n=(t-o)/t*100,s=Math.pow(10,e);return Math.floor(Number((n*s).toFixed(8)))/s}export{u as c};
