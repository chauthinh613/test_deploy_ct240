<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import IconUser from '../icons/IconUser.vue';
import BaseComment from '../base/BaseComment.vue';
import IconDeleteTask from '../icons/IconDeleteTask.vue';
const emit = defineEmits(['close', 'update-task'])
const close_detail_task =()=>{
  emit('close');
}

const props = defineProps({
  task: {
    type: Object,
    default: () => ({ name: 'Task name', id: null, deadline: '' })
  }
})

const localDeadline = ref(props.task?.deadline ? props.task.deadline.split('T')[0] : '');
const comments = ref([]);
const newComment = ref('');
const isSaving = ref(false);
const isEditingTaskName = ref(false);
const editTaskNameValue = ref(props.task?.name || '');

const startEditingTaskName = () => {
    isEditingTaskName.value = true;
};

const saveTaskInfo = async () => {
    if (!editTaskNameValue.value.trim() && isEditingTaskName.value) {
        isEditingTaskName.value = false;
        editTaskNameValue.value = props.task?.name || '';
        return;
    }

    if(!props.task?.id) return;
    try {
        const token = localStorage.getItem('token');
        await axios.put(`http://localhost:8080/api/tasks/${props.task.id}`, {
            name: editTaskNameValue.value.trim() || props.task.name,
            deadline: localDeadline.value,
            completed: props.task.completed || false
        }, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        isEditingTaskName.value = false;
        emit('update-task');
    } catch (error) {
        console.error("Lỗi khi cập nhật thông tin task:", error);
    }
};

const handleDeleteTask = async () => {
    if(!props.task?.id) return;
    if (confirm(`Bạn có chắc chắn muốn xóa tác vụ "${props.task?.name}" không? Toàn bộ bình luận và dữ liệu liên quan sẽ bị xóa vĩnh viễn.`)) {
        try {
            const token = localStorage.getItem('token');
            await axios.delete(`http://localhost:8080/api/tasks/${props.task.id}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            emit('update-task');
            close_detail_task();
        } catch (error) {
            console.error("Lỗi khi xóa task:", error);
            alert("Không thể xóa tác vụ này! Vui lòng thử lại.");
        }
    }
};

const fetchComments = async () => {
    if(!props.task?.id) return;
    try {
        const token = localStorage.getItem('token');
        const res = await axios.get(`http://localhost:8080/api/tasks/${props.task.id}/comments`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        comments.value = res.data.data || res.data || [];
    } catch (err) {
        console.log("Load comments error or not found", err);
    }
};

const saveDeadline = async () => {
    await saveTaskInfo();
};

const handleSaveTask = async () => {
    isSaving.value = true;
    await saveDeadline();
    isSaving.value = false;
    close_detail_task();
};

const handlePostComment = async () => {
    if(!newComment.value.trim() || !props.task?.id) return;
    try {
        const token = localStorage.getItem('token');
        await axios.post(`http://localhost:8080/api/tasks/${props.task.id}/comments`, {
            content: newComment.value.trim()
        }, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        newComment.value = '';
        fetchComments();
    } catch (error) {
        console.error("Lỗi khi đăng bình luận:", error);
    }
};

onMounted(() => {
    fetchComments();
});
watch(() => props.task, (newVal) => {
    localDeadline.value = newVal?.deadline ? newVal.deadline.split('T')[0] : '';
    editTaskNameValue.value = newVal?.name || '';
    fetchComments();
}, { deep: true });

</script>

<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="close_detail_task">
      <div class="modal">
        <div class="header-task">
          <input 
            v-if="isEditingTaskName"
            v-model="editTaskNameValue"
            class="task_name_input"
            autofocus
            @blur="saveTaskInfo"
            @keydown.enter.prevent="saveTaskInfo"
          />
          <div v-else class="label_task" @click="startEditingTaskName" title="Nhấn để đổi tên">{{ editTaskNameValue }}</div>
          <div class="delete-task-icon" @click="handleDeleteTask" title="Xóa tác vụ">
            <IconDeleteTask />
          </div>
        </div>
        <div class="modal-content">
        
        <div class="left-modal">
        <div
          class="modal-assign"
          @keyup.enter="handleCreateCard" 
          autofocus>
          Thêm thành viên  +
        </div>
        <div class="wrapper-deadline">
          <div>Hạn chót</div>
          <input class="Deadline" type="date" v-model="localDeadline" @change="saveDeadline"/>
        </div>
        </div>
        <div class="right-modal">
        <div class="comment_nav">
          <input type="text" placeholder="Viết bình luận..." v-model="newComment" @keydown.enter="handlePostComment">
        </div>
        <div class="comments-list">
          <BaseComment v-for="comment in comments" :key="comment.id || Math.random()" :userName="comment.userName || comment.user?.username || 'Người dùng'" :content="comment.content"></BaseComment>
        </div>
        </div>
        </div>
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="close_detail_task">Hủy</button>
          <button class="btn-submit" @click="handleSaveTask" :disabled="isSaving">
            {{ isSaving ? 'Đang lưu...' : 'Lưu thẻ' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.header-task {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}
.delete-task-icon {
  cursor: pointer;
  color: #1a5270;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  border-radius: 5px;
  transition: all 0.2s;
  margin-top: -2px;
}
.delete-task-icon:hover {
  background-color: #fce4e4;
  border-radius: 50%;
  color: #d9534f;
}
.task_name_input {
  font-size: 25px;
  font-weight: 500;
  margin-left: 5px;
  margin-bottom: 20px;
  border: none;
  background: transparent;
  outline: none;
  width: 90%;
  border-bottom: 2px solid #1a5270;
  color: inherit;
  font-family: inherit;
}
.label_task{
  font-size: 25px;
  font-weight: 500;
  margin-left: 5px;
  cursor: pointer;
  margin-bottom: 20px;
}
.label_task:hover {
  opacity: 0.8;
}
.left-modal{
  padding: 16px 0px;
  width: 25%;
  display: flex;
  flex-direction: column;
  justify-items: center;
}
.comment_nav{
  padding: 5px 4%;
  width: 100%;
  display: flex;
  gap: 10px;
  align-content: center;
  justify-content: center;
  margin-top: 10px;
}

.comment_nav input{
  font-family: "Quicksand", sans-serif;;
  padding: 15px;
  border-radius: 1.25rem;
  border:0px;
  width: 100%;
  height: 40px;
  display: flex;
}
.comment_nav input:focus{
  border: 1px solid #a0d8f1;
  outline: none;
}
.modal-content{
  display: flex;
  gap:25px;
  flex-direction: row;
}
.right-modal{
  display: flex;
  flex-direction: column;
  width: 70%;
  height: fit-content;
  min-height: 130px;
  border-radius: 1.25rem;
  background-color: #e8f4fa;
  margin-bottom: 30px;
}
.wrapper-deadline{
  display: flex;
  flex-direction: column;
  gap:2px;
  
}
.wrapper-deadline div{
  font-weight:500;
  margin-left: 10px;
}
.Deadline{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 14px;
  display: flex;   
  justify-content: center;
  text-align: center;
  border-radius: 1.25rem;
  width: 100%;
  height: 35px;
  outline: none;
  padding: 4px;
  border: 1px solid #ccc;
  color:inherit;
}
.Deadline:hover{
  cursor: pointer;
  border-color: #74c5e1;
}
/* Di chuyển toàn bộ CSS của Modal từ file cũ sang đây */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.4); display: flex;
  justify-content: center; align-items: center; z-index: 1000;
}
.modal {
  background: white; padding: 24px; border-radius: 1.25rem;
  width: 750px; max-width: 90%; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  font-family: "Quicksand", sans-serif;
}
.modal h2 { margin-top: 0; margin-bottom: 16px; font-size: 20px; }
.modal-assign {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%; 
  padding: 10px 12px; border: 1px solid #ccc;
  border-radius: 1.25rem; font-size: 15px; margin-bottom: 5px;
  box-sizing: border-box; font-family: inherit;
  height:35px;
}
.modal-assign:hover {
  cursor: pointer;
  outline: none; border-color: #74c5e1;
}
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; }
.btn-cancel, .btn-submit {
  padding: 8px 16px; border-radius: 1.25rem; cursor: pointer;
  font-weight: 600; font-family: inherit; border: none;
}
.btn-cancel { background: #f1f1f1; color: #333; }
.btn-cancel:hover { background: #e1e1e1; }
.btn-submit { 
  margin-right: 10px;
  background: #0079bf;; color: white; }
.btn-submit:hover { background: #00659f;; }
.btn-submit:disabled, .btn-cancel:disabled { opacity: 0.6; cursor: not-allowed; }

.comments-list {
  max-height: 200px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding-bottom: 10px;
}

/* Custom scrollbar cho comments-list */
.comments-list::-webkit-scrollbar {
  width: 6px;
}
.comments-list::-webkit-scrollbar-track {
  background: transparent;
}
.comments-list::-webkit-scrollbar-thumb {
  background-color: #bce3f5;
  border-radius: 10px;
}
.comments-list::-webkit-scrollbar-thumb:hover {
  background-color: #8bbcd6;
}
</style>