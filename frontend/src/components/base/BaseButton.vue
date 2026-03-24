<script setup>
defineProps({
  text: {
    type: String,
    default:'Xóa'
  },
  type:{
    type: String,
    default:'primary'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <button :class="['btn',`btn-${type}`]" :disabled="disabled">
    {{ text }}
  </button>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.btn {
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;     
  justify-content: center;
  border-radius: 1.25rem;
  border: none;
  width: fit-content;
  padding: 0 15px;
  height: 35px;
  transition: all 0.2s ease;
  cursor: pointer;
  outline: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn:active:not(:disabled) {
  transform: scale(0.96);
}

.btn-primary {
  color:#2f4562;
  background-color:#a0d8f1;
}
.btn-primary:hover:not(:disabled) {
  background-color:#7abfe5;
}

.btn-secondary {
  color:#3d5875;
  background-color:#bce3f5;
}
.btn-secondary:hover:not(:disabled) {
  background-color:#a1d2ec;
}

.btn-ghost {
  border: 1px solid #bce3f5;
  background-color: transparent;
  color:#2f4562;
}
.btn-ghost:hover:not(:disabled) {
  background-color: #f0f7ff;
}

.btn-transparent {
  background-color: transparent;
  color: #64748b;
}
.btn-transparent:hover:not(:disabled) {
  background-color: #f1f5f9;
  color: #1e293b;
}

.btn-dark {
  background-color: #1a5270;
  color: white;
  border: 1px solid #1a5270;
}
.btn-dark:hover:not(:disabled) {
  background-color: #0d3852;
}

.btn-delete {
  color:#7d1f3a;
  background-color:#d97998;
}
.btn-delete:hover:not(:disabled) {
  background-color:#e09ba5;
}
</style>
