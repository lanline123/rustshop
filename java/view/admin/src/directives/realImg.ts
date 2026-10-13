// 判断图片的有效性
import type { Directive } from 'vue'
export type realImgDirective = Directive<HTMLImageElement, string>
import { imageFormat } from "@/utils/format";
import placeholderImage from '@/assets/404_images/404_cloud.png'

declare module 'vue' {
  export interface ComponentCustomProperties  {
    vRealImg: realImgDirective,
  }
}

let imageIsExist = function(url: string) {
    return new Promise((resolve) => {
        let img: any = new Image();
        img.onload = function () {
            if (this.complete == true){
                resolve(true);
                img = null;
            }
        }
        img.onerror = function () {
            resolve(false);
            img = null;
        }
        img.src = url;
    })
}


export default {
  async mounted(el, binding) {
    const imgURL  = imageFormat(binding.value)
    if(imgURL) {
      const exist =  await imageIsExist(imgURL)
        if (exist) {
          el.setAttribute('src', imgURL)
        } else {
            // 隐藏元素
            // el.style.display = 'none'
            // 使用占位图片
            el.setAttribute('src', placeholderImage)
        }
    }
  }
} satisfies realImgDirective
