/** 文本省略组件 - 多行文本超出指定行数后显示省略号 */
<template>
  <div v-if="type==='div'" class="ellipsis-text" :style="ellipsisStyle">{{ text }}</div>
  <a v-else-if="type==='a'" class="ellipsis-text" :style="ellipsisStyle">{{ text }}</a>
</template>

<script>
export default {
  props: {
    /** 显示的文本内容 */
    text: String,
    /** 元素类型：div 或 a */
    type: { type: String, default: 'div' },
    /** 最大显示行数 */
    maxLines: Number,
  },
  computed: {
    /**
     * 计算省略样式
     * @returns {Object} CSS 样式对象
     */
    ellipsisStyle() {
      if (this.maxLines) {
        return {
          display: '-webkit-box',
          '-webkit-box-orient': 'vertical',
          '-webkit-line-clamp': `${this.maxLines}`,
          overflow: 'hidden',
        };
      }
      return {};
    },
  },
};
</script>

<style lang="less" scoped>
.ellipsis-text {
  /* 可以自定义其他样式 */
}
</style>
