const appendDownloadLink = (href: string, filename: string) => {
    const link = document.createElement("a");
    link.href = href;
    link.download = filename;
    link.style.display = "none";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

export const saveBlob = (blob: Blob, filename: string) => {
    const url = URL.createObjectURL(blob);
    appendDownloadLink(url, filename);
    URL.revokeObjectURL(url);
};

export const downloadUrlAsFile = async (url: string, filename: string) => {
    const requestUrl = new URL(url, window.location.href);
    const credentials: RequestCredentials = requestUrl.origin === window.location.origin ? "same-origin" : "omit";
    const response = await fetch(requestUrl.href, { credentials });
    if (!response.ok) throw new Error("资源下载失败");
    const blob = await response.blob();
    saveBlob(blob, filename);
};
