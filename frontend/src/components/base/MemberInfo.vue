<script setup>
import { computed } from 'vue'
const props = defineProps({
  type:{
    type: String,
    default:'text'
  },
  placeholder:{
    type:String,
    default:'Nhập tên task'
  }
})
const displayPlaceholder = computed(() => {
  return props.type === 'text' ? props.placeholder : ''
})
</script>

<template>
<input :class="['input',`input-${type}`]" 
       :type="[`${type}`]"
       :placeholder="displayPlaceholder">
</input>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.input{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  display: flex;
  align-items: center;     
  justify-content: center;
  text-align: center;
  border-radius: 1.25rem;
  width: 50%;
  height: 40px;
  outline: none;
  padding: 4px;
  
}

.input-text{
  border: 1px solid #d4ecf8;
  color:#2f4562;
  background-color:#ffffff;
}
.input-text:focus{
  background-color:white;
  border: 2px solid #d4ecf8;
}
.input-date{
    background-color: white;
    border: 2px solid #74c5e1;
}

</style>
