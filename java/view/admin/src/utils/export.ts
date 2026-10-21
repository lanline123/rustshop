
import dayjs from "dayjs"
export function requestExport(result: any, title: string = '导出表格', type:string = '.xlsx') {
    let blob = new Blob([result as any], { type: 'application/octet-stream' });
    // 创建一个链接并设置下载属性
    const downloadLink = document.createElement('a')
    let url:any = window.URL || window.webkitURL // 兼容不同浏览器的 URL 对象
    url = url.createObjectURL(blob)
    downloadLink.href = url
    let time = dayjs(new Date()).format('YYYY-MM-DD')
    downloadLink.download = `${title + time + type}`; // 设置下载的文件名
    // 将链接添加到 DOM 中，模拟点击
    document.body.appendChild(downloadLink)
    downloadLink.click()
    // 移除创建的链接和释放 URL 对象
    document.body.removeChild(downloadLink)
    window.URL.revokeObjectURL(url)
}
export default requestExport;
