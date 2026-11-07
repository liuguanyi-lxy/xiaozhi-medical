<template>
  <ChatWindow/>
</template>

<script setup>
// 当前使用重做版界面（ChatWindow.v2.vue）。
// 想切回原界面，把下面这行的 v2 去掉即可：'@/components/ChatWindow.vue'
import ChatWindow from '@/components/ChatWindow.v2.vue'
</script>

<style>
html,
body {
  overflow: hidden;
}
</style>
