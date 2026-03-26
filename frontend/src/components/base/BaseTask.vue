<script setup>
import { computed } from 'vue';
const props=defineProps({
    taskId:{
        type:[String,Number],
        required:true
    },
    task_name:{
        type:String,
        default:'Task Name'
    },
    deadline:{
        type:String,
        default:''
    },
    completed:{
        type:Boolean,
        default:false
    }
})
const emit=defineEmits(['update-status']);
//format lại định dạng deadline

const formattedDeadline = computed(() => {
    if (!props.deadline) return '';
    try {
        const date = new Date(props.deadline);
        if (isNaN(date.getTime())) return props.deadline;
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        const yyyy = date.getFullYear();
        return `${mm}/${dd}/${yyyy}`;
    } catch {
        return props.deadline;
    }
});

const handleCheckboxChange = (event) => {
  const isChecked = event.target.checked;
  
  emit('update-status', props.taskId, isChecked);
};

</script>
<template>
    <div class="task">
        <input type="checkbox" 
        :id="'checkbox-' + taskId"
        :checked="completed"
        @change.stop="handleCheckboxChange"
        @click.stop>
        <label :for="'checkbox-' + taskId"
        :class="{ 'is-done': completed }">{{ task_name }}  
        </label>
        <div class="deadline" v-if="formattedDeadline">{{ formattedDeadline }}</div>
    </div>
</template>
<style scoped>
.is-done {
  text-decoration: line-through;
  color: #888;
}
label{
    font-size: 14px;
    color: #1a5270;
    line-height: 1; 
    display: inline-block;
}
.deadline{
    display: flex;
    font-size: 10px;
    align-items: center;
    justify-content: center;
    margin-left: auto;
    line-height: 1;
}
.task{
    color: #1a5270;
    display: flex;
    align-items: center;
    justify-items: center;
    align-content: center;
    gap:10px;
    width: 93%;
    background-color: #ffffff;
    border-radius: 0.5rem;
    min-height: 30px;
    height: fit-content;
    padding: 5px;
    margin-bottom: 5px;
    cursor: pointer;
}
#checkbox{
    align-self: center;
    border-radius: 1.25rem;
}
</style>