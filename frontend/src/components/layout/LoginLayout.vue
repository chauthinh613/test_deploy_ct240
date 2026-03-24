<script setup>
</script>

<template>
  <div class="wrapper">
    <slot></slot>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.wrapper{
  display: flex;
  justify-content: center;
  align-self: center;
  margin-top: 50px;
}
</style>
