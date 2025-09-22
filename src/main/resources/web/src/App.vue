<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="app">
    <RouterView />
  </div>
</template>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: var(--font-family-base);
}

#app {
  height: 100%;
  background-color: var(--color-background);
  color: var(--color-text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}
</style>