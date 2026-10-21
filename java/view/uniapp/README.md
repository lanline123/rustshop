### 介绍

TigShop 开源商城系统是一款全开源可商用的系统，前后端分离开发，拥抱最新的 PHP8+ VUE3 Ts uniapp 全新技术架构。

文档祥见：https://github.com/tigshop/tigshop-api

1.配置线上（生产环境）api 接口地址: 在 **.env.production** 文件中配置 **VITE_API_URL**,填上你自己的 api 接口地址，例如：   
<code>VITE_API_URL=https://your.api.url</code>

2.在项目根目录下打开终端，运行 npm run build:h5

3.打包成功之后，会在根目录下新建一个 dist 文件夹，打开 dist 文件夹，有一个 build 文件夹,打开 build 文件夹，有一个 h5 文件夹。这个就是打包之后的静态文件
