import play from "../assets/videoFeed/play.png";
import cart from "../assets/videoFeed/cart.png";
import likeSvg from "../assets/videoFeed/like.svg?raw";

/**
 * 小程序的 mask-image 不认包内相对路径，只接受 data URI 或网络地址，
 * 故两张遮罩图以源码内联后再转 data URI。
 */
const toMaskUri = (svg: string) => `data:image/svg+xml,${encodeURIComponent(svg)}`;

/**
 * H5 / 小程序图标。
 * like 用 SVG 走 CSS mask 改色，以支持点赞激活态；
 * play / cart 为白色 PNG，直接展示。
 */
export const videoFeedIcons = {
    play,
    like: toMaskUri(likeSvg),
    cart
};
