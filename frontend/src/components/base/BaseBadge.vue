<script setup>
defineProps({
  text: {
    type: String,
    default:'Admin'
  },
  type:{
    type: String,
    default:'member'
  }
})
</script>

<template>
  <div :class="['badge',`badge-${type}`]">
    {{ text }}
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.badge{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  display: flex;
  align-items: center;     
  justify-content: center;
  border-radius: 1.25rem;
  border-width: 0;
  width: 100px;
  height: 40px;
}

.badge-owner{
  border: 1.5px solid #74c5e1;
  background-color: #f0f7ff;
  color:#2c3e50;
}

.badge-admin{
  border: 1.5px solid #bce3f5;
  background-color: #f0f7ff;
  color:#2c3e50;
}
.badge-member{
  border: 1.5px solid #d4ecf8;
  background-color: #f0f7ff;;
  color:#2c3e50;
}
.btn-delete{
  color:#7d1f3a;
  background-color:#d97998;
}
</style>
