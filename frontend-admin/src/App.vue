<!--
  管理后台根组件
  提供应用的基础布局和全局样式
-->
<template>
  <div id="app">
    <!-- 路由视图容器 -->
    <router-view />
  </div>
</template>

<script setup>
// 根组件逻辑（当前为空）
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 应用容器样式 */
#app {
  min-height: 100vh;
}
</style>
