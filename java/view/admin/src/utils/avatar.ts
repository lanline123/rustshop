import img1 from "@/assets/avatar/1.jpeg";
import img2 from "@/assets/avatar/2.jpeg";
import img3 from "@/assets/avatar/3.jpeg";
import img4 from "@/assets/avatar/4.jpeg";
import img5 from "@/assets/avatar/5.jpeg";
import img6 from "@/assets/avatar/6.jpeg";
import img7 from "@/assets/avatar/7.jpeg";
import img8 from "@/assets/avatar/8.jpeg";
import img9 from "@/assets/avatar/9.jpeg";
import img10 from "@/assets/avatar/10.jpeg";
import img11 from "@/assets/avatar/11.jpeg";
import img12 from "@/assets/avatar/12.jpeg";
import img13 from "@/assets/avatar/13.jpeg";
import img14 from "@/assets/avatar/14.jpeg";
import img15 from "@/assets/avatar/15.jpeg";
import img16 from "@/assets/avatar/16.jpeg";
import img17 from "@/assets/avatar/17.jpeg";
import img18 from "@/assets/avatar/18.jpeg";
import img19 from "@/assets/avatar/19.jpeg";
import img20 from "@/assets/avatar/20.jpeg";
import img21 from "@/assets/avatar/21.jpeg";
import img22 from "@/assets/avatar/22.jpeg";
import img23 from "@/assets/avatar/23.jpeg";
import img24 from "@/assets/avatar/24.jpeg";
import img25 from "@/assets/avatar/25.jpeg";
import img26 from "@/assets/avatar/26.jpeg";
import img27 from "@/assets/avatar/27.jpeg";
import img28 from "@/assets/avatar/28.jpeg";
import img29 from "@/assets/avatar/29.jpeg";
import img30 from "@/assets/avatar/30.jpeg";
import img31 from "@/assets/avatar/31.jpeg";
import img32 from "@/assets/avatar/32.jpeg";
import img33 from "@/assets/avatar/33.jpeg";
import img34 from "@/assets/avatar/34.jpeg";

export const avatarList = [
    "../assets/avatar/1.jpeg",
    "../assets/avatar/2.jpeg",
    "../assets/avatar/3.jpeg",
    "../assets/avatar/4.jpeg",
    "../assets/avatar/5.jpeg",
    "../assets/avatar/6.jpeg",
    "../assets/avatar/7.jpeg",
    "../assets/avatar/8.jpeg",
    "../assets/avatar/9.jpeg",
    "../assets/avatar/10.jpeg",
    "../assets/avatar/11.jpeg",
    "../assets/avatar/12.jpeg",
    "../assets/avatar/13.jpeg",
    "../assets/avatar/14.jpeg",
    "../assets/avatar/15.jpeg",
    "../assets/avatar/16.jpeg",
    "../assets/avatar/17.jpeg",
    "../assets/avatar/18.jpeg",
    "../assets/avatar/19.jpeg",
    "../assets/avatar/20.jpeg",
    "../assets/avatar/21.jpeg",
    "../assets/avatar/22.jpeg",
    "../assets/avatar/23.jpeg",
    "../assets/avatar/24.jpeg",
    "../assets/avatar/25.jpeg",
    "../assets/avatar/26.jpeg",
    "../assets/avatar/27.jpeg",
    "../assets/avatar/28.jpeg",
    "../assets/avatar/29.jpeg",
    "../assets/avatar/30.jpeg",
    "../assets/avatar/31.jpeg",
    "../assets/avatar/32.jpeg",
    "../assets/avatar/33.jpeg",
    "../assets/avatar/34.jpeg"
];

export const avatarFileList = [img1, img2, img3, img4, img5, img6, img7, img8, img9, img10, img11, img12, img13, img14, img15, img16, img17, img18, img19, img20, img21, img22, img23, img24, img25, img26, img27, img28, img29, img30, img31, img32, img33, img34];

export const returnAvatar = (url: string | number) => {
    if (typeof url === "number") {
        return avatarFileList[url];
    } else {
        let num = avatarList.indexOf(url);
        if (num > -1) {
            return avatarFileList[num];
        } else {
            return '';
        }
    }
};
export const returnAvatarString = (url: string | number) => {
    if (typeof url === "number") {
        return avatarList[url];
    }
    return ''
};
