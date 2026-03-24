<script setup>
import {ref, watch} from 'vue'
import IconAdd from '../icons/IconAdd.vue';
import IconDelete from '../icons/IconDelete.vue';
const props = defineProps({
  name_card: {
    type: String,
    default: 'Tên Card'
  },
  cardId: {
    type: [String, Number],
    required: true 
  }
});

const emit = defineEmits(['add-new-task','delete-card', 'update-card-name']);

const handleDeleteCard=()=>{
  if(confirm(`Bạn có muốn xóa danh sách "${props.name_card}" không. Toàn bộ dữ liệu thẻ trong danh sách sẽ bị xóa cùng với danh sách`))
  emit('delete-card',props.cardId);
}

const isEditingName = ref(false);
const editNameValue = ref(props.name_card);

watch(() => props.name_card, (newVal) => {
  editNameValue.value = newVal;
});

const startEditingName = () => {
  isEditingName.value = true;
};

const saveCardName = () => {
  if (!editNameValue.value.trim() || editNameValue.value.trim() === props.name_card) {
    isEditingName.value = false;
    editNameValue.value = props.name_card;
    return;
  }
  emit('update-card-name', props.cardId, editNameValue.value.trim());
  isEditingName.value = false;
};

const isAdding = ref(false);
const taskTitle = ref('');

const saveTask = () => {
  if (!taskTitle.value.trim()) return;

  emit('add-new-task', props.cardId, taskTitle.value.trim());

  taskTitle.value = ''; 
};

const openForm=()=>{
  isAdding.value=true;
};

const closeForm=()=>{
  isAdding.value=false;
  taskTitle.value='';
};

</script>
<template>
  <div class="card-wrapper">
    <div class="header_card">
      <input 
        v-if="isEditingName"
        v-model="editNameValue"
        class="card_name_input"
        autofocus
        @blur="saveCardName"
        @keydown.enter="saveCardName"
      />
      <div v-else class="card_name" @click="startEditingName">{{ name_card }}</div>
    <div class="delete" @click="handleDeleteCard"><IconDelete></IconDelete></div>
    </div>
    <slot></slot>
    <div v-if="isAdding" class="add_task_form">
      <div class="separator"></div>
      <input
      v-model="taskTitle" 
        placeholder="Nhập tên task" 
        class="task-input"
        autofocus
        @keydown.enter="saveTask"></input>
      <div class="form-actions">
        <button class="btn-save" @click="saveTask">Thêm task</button> 
        <button class="btn-cancel" @click="closeForm">✕</button>
      </div>
    </div>
    <div v-else class="add_task" @click="openForm" >
      <IconAdd></IconAdd>
        <div>Thêm task mới</div>
  </div>
  </div>
</template>


<style scoped>
.delete{
  color: #1a5270;
  display: flex;
  padding-top: 10px;
  align-items: center;
  justify-content: center;
}
.header_card{
  width: 100%;
  padding: 0 20px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}
.add_task_form {
  width: 100%;
  height: fit-content;
  padding: 0 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-top: 2px;
  margin-bottom: 10px;
}

.task-input {
  width: 100%;
  height: fit-content;
  padding: 10px;
  border-radius: 0.5rem;
  border: none;
  resize: none;
  font-family: inherit;
  font-size: 14px;
}

.task-input:focus {
  outline: none;
  border: 1px solid #329ad2;;
}

.form-actions {
  margin-top: 5px;
  display: flex;
  margin-right:auto;
  margin-left: 2px;
  gap: 12px;
}

.btn-save {
  background-color: #1a5270; 
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 0.5rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  transition: all 0.2s ease;
}

.btn-save:active{
  transform: scale(0.95);
}

.btn-cancel {
  background: none;
  border: none;
  font-size: 20px;
  color: #555;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.btn-cancel:active{
  transform: scale(0.95);
}
.btn-cancel:hover {
  color: #000;
}

.add_task{
  color:#1a5270;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-right: auto;
  margin-top: 5px;
  margin-left:10px;
  padding:0 4px;
  cursor: pointer;
  padding: 3px 16px 3px 10px;
  transition: all 0.2s ease;
  border-radius: 1.25rem;
}
.add_task:hover{
  background-color: #1a5270;
  color:#ffffff;
}
.add_task:active{
  transform: scale(0.95);
}
.card_name_input {
  font-size: 17px;
  color: #1a5270;
  font-weight: 500;
  padding-top: 10px;
  border: none;
  background: transparent;
  outline: none;
  width: 100%;
  border-bottom: 2px solid #1a5270;
  margin-right: 10px;
  font-family: inherit;
}

.card_name{
    font-size: 17px;
    color:#1a5270;
    font-weight: 500;
    padding-top: 10px;
    cursor: pointer;
}
.card-wrapper{
    display: flex;
    flex-shrink: 0;
    gap:5px;
    flex-direction: column;
    align-items: center;
    justify-items: center;
    border-radius: 1.25rem;
    width: 260px;
    height: fit-content;
    padding-bottom: 8px;
    background-color: #d4ecf8;
}


</style>