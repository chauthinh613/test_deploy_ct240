<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import api from '@/services/api'
import IconUser from '../icons/IconUser.vue';
import BaseComment from '../base/BaseComment.vue';
import IconDeleteTask from '../icons/IconDeleteTask.vue';
import { useRoute } from 'vue-router'
const emit = defineEmits(['close', 'update-task'])
const close_detail_task =()=>{
  emit('close');
}

const route = useRoute()

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

const boardId = computed(() => route.params?.idBoard)
const showMemberPicker = ref(false)
const memberSearch = ref('')
const boardMembers = ref([]) // [{ userResponse: UserResponse, isOwner }]
const taskAssignments = ref([]) // [UserResponse]
const hoveredMember = ref(null) // UserResponse | null

const getAvatarText = (name) => {
  if (!name) return 'U'
  const first = String(name).trim().charAt(0)
  return first.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase()
}

const getUserDisplayName = (u) => {
  const name = u?.name?.trim?.() ? u.name.trim() : ''
  return name || u?.username || 'Người dùng'
}

const fetchBoardMembers = async () => {
  if (!boardId.value) return
  try {
    const res = await api.get(`/boards/${boardId.value}/members`)
    boardMembers.value = res.data?.data ?? res.data ?? []
  } catch (err) {
    console.error('Load board members error:', err)
  }
}

const fetchTaskAssignments = async () => {
  if (!props.task?.id) return
  try {
    const res = await api.get(`/tasks/${props.task.id}/assignments`)
    taskAssignments.value = res.data?.data ?? res.data ?? []
  } catch (err) {
    console.error('Load task assignments error:', err)
  }
}

const assignedUserIdSet = computed(() => {
  return new Set(taskAssignments.value.map(u => u?.id).filter(Boolean))
})

const boardUsers = computed(() => {
  return boardMembers.value.map(bm => bm?.userResponse).filter(Boolean)
})

const filteredBoardUsers = computed(() => {
  const q = memberSearch.value.trim().toLowerCase()
  if (!q) return boardUsers.value
  return boardUsers.value.filter(u => {
    const name = String(u?.name ?? '').toLowerCase()
    const username = String(u?.username ?? '').toLowerCase()
    return name.includes(q) || username.includes(q)
  })
})

const openMemberPicker = () => {
  showMemberPicker.value = true
  memberSearch.value = ''
  hoveredMember.value = null
}

const closeMemberPicker = () => {
  showMemberPicker.value = false
}

const handleSelectMember = async (user) => {
  if (!props.task?.id || !user?.id) return
  try {
    await api.post(`/tasks/${props.task.id}/assign`, { userId: user.id })
    await fetchTaskAssignments()
    closeMemberPicker()
  } catch (e) {
    console.error('Assign member to task error:', e)
    alert('Không thể gán thành viên vào task!')
  }
}

const handleUnassignMember = async (user) => {
  if (!props.task?.id || !user?.id) return

  const ok = window.confirm(`Bạn có chắc chắn muốn xóa ${getUserDisplayName(user)} khỏi task không?`)
  if (!ok) return

  try {
    await api.delete(`/tasks/${props.task.id}/assign/${user.id}`)
    await fetchTaskAssignments()
  } catch (e) {
    console.error('Unassign member from task error:', e)
    alert('Không thể xóa thành viên khỏi task!')
  }
}

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
        await api.put(`/tasks/${props.task.id}`, {
            name: editTaskNameValue.value.trim() || props.task.name,
            deadline: localDeadline.value,
            completed: props.task.completed || false
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
            await api.delete(`/tasks/${props.task.id}`);
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
        const res = await api.get(`/tasks/${props.task.id}/comments`);
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
        await api.post(`/tasks/${props.task.id}/comments`, {
            content: newComment.value.trim()
        });
        newComment.value = '';
        fetchComments();
    } catch (error) {
        console.error("Lỗi khi đăng bình luận:", error);
    }
};

onMounted(() => {
    fetchComments();
    fetchTaskAssignments();
    fetchBoardMembers();
});
watch(() => props.task, (newVal) => {
    localDeadline.value = newVal?.deadline ? newVal.deadline.split('T')[0] : '';
    editTaskNameValue.value = newVal?.name || '';
    fetchComments();
    fetchTaskAssignments();
}, { deep: true });

watch(() => boardId.value, () => {
  fetchBoardMembers()
})

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
          role="button"
          tabindex="0"
          @click.stop="openMemberPicker"
          @keydown.enter.stop="openMemberPicker"
        >
          Thêm thành viên <span class="plus-sign">+</span>
        </div>

        <div class="wrapper-deadline">
          <div>Hạn chót</div>
          <input class="Deadline" type="date" v-model="localDeadline" @change="saveDeadline"/>
        </div>

        <div class="assigned-members">
          <div
            v-for="m in taskAssignments"
            :key="m?.id"
            class="assigned-member"
          >
            <div
              class="avatar"
              @mouseenter="hoveredMember = m"
              @mouseleave="hoveredMember = null"
            >
              {{ getAvatarText(getUserDisplayName(m)) }}
            </div>
            <div
              v-if="hoveredMember?.id === m?.id"
              class="member-tooltip"
            >
              {{ getUserDisplayName(m) }} ({{ m?.username }})
            </div>
          </div>

          <div class="assigned-member add" role="button" tabindex="0" @click.stop="openMemberPicker"
               @keydown.enter.stop="openMemberPicker">
            <div class="avatar add-avatar">+</div>
          </div>
        </div>
        </div>

        <div
          v-if="showMemberPicker"
          class="member-picker-overlay"
          @click.self="closeMemberPicker"
        >
          <div class="member-picker">
            <div class="member-picker-header">
              <div class="member-picker-title">Thành viên</div>
              <button class="member-picker-close" type="button" @click.stop="closeMemberPicker">
                ×
              </button>
            </div>
            <input
              class="member-picker-search"
              type="text"
              placeholder="Tìm kiếm các thành viên"
              v-model="memberSearch"
            />
            <div class="member-picker-list">
              <div v-if="filteredBoardUsers.length === 0" class="member-picker-empty">
                Không có thành viên phù hợp
              </div>
              <div
                v-for="user in filteredBoardUsers"
                :key="user.id"
                class="member-picker-item"
                :class="{ 'is-assigned': assignedUserIdSet.has(user.id) }"
              >
                <div class="member-picker-user" @click.stop="assignedUserIdSet.has(user.id) ? null : handleSelectMember(user)">
                  <div class="avatar">{{ getAvatarText(getUserDisplayName(user)) }}</div>
                  <div class="member-picker-user-info">
                    <div class="member-picker-user-name">{{ getUserDisplayName(user) }}</div>
                    <div class="member-picker-user-username">@{{ user.username }}</div>
                  </div>
                </div>

                <button
                  v-if="assignedUserIdSet.has(user.id)"
                  class="member-remove-btn"
                  type="button"
                  @click.stop="handleUnassignMember(user)"
                  title="Xóa khỏi task"
                >
                  ×
                </button>
                <button
                  v-else
                  class="member-assign-btn"
                  type="button"
                  @click.stop="handleSelectMember(user)"
                  title="Thêm vào task"
                >
                  +
                </button>
              </div>
            </div>
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
  position: relative;
  overflow: visible;
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
  position: relative;
  background: white; padding: 40px; border-radius: 1.25rem;
  width: 750px; max-width: 90%; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  font-family: "Quicksand", sans-serif;
  overflow: visible;
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

.plus-sign{
  margin-left: 6px;
  font-weight: 800;
  color: #1a5270;
}

.member-picker{
  width: 680px;
  max-width: 100%;
  background: #fff;
  border: 1px solid #d4ecf8;
  border-radius: 1.25rem;
  padding: 12px;
  margin-bottom: 0;
  max-height: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.member-picker-header{
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.member-picker-title{
  font-weight: 700;
  color: #2c3e50;
}
.member-picker-close{
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #d4ecf8;
  background: white;
  cursor: pointer;
}
.member-picker-close:hover{
  background: #f0f7ff;
}

.member-picker-overlay{
  position: absolute;
  inset: 0;
  background: rgba(232, 244, 250, 0.85);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 70px 40px 40px;
  z-index: 2001;
  border-radius: 1.25rem;
}
.member-picker-search{
  font-family: "Quicksand", sans-serif;
  width: 100%;
  border: 2px solid #74c5e1;
  border-radius: 1.25rem;
  padding: 10px 12px;
  outline: none;
}
.member-picker-search:focus{
  border-color: #38bdf8;
}
.member-picker-list{
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 4px;
  /* Vừa đủ hiển thị khoảng 2 dòng thành viên, còn lại sẽ cuộn */
  max-height: 145px;
}
.member-picker-empty{
  color: #6b8799;
  font-size: 13px;
  text-align: center;
  padding: 16px 0;
}
.member-picker-item{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 1rem;
  border: 1px solid #d4ecf8;
  background: #fff;
}
.member-picker-item:hover{
  background: #e8f4fa;
}

.member-picker-item.is-assigned{
  opacity: 0.92;
}
.member-picker-item.is-assigned .member-picker-user{
  cursor: default;
}

.member-picker-user{
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.member-picker-user-info{
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.member-picker-user-name{
  font-size: 14px;
  font-weight: 800;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 380px;
}

.member-picker-user-username{
  font-size: 12px;
  font-weight: 700;
  color: #88a7b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-remove-btn{
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid rgba(217, 121, 152, 0.9);
  background: #ffffff;
  color: #d97998;
  cursor: pointer;
  font-size: 18px;
  line-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
.member-remove-btn:hover{
  background: #feedf3;
}

.member-assign-btn{
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid #a0d8f1;
  background: #f0f7ff;
  color: #1a5270;
  cursor: pointer;
  font-size: 18px;
  line-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.assigned-members{
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  margin-left:10px;
}

.assigned-member{
  display: flex;
  align-items: center;
  padding: 0;
  border: none;
  background: transparent;
  position: relative;
}

.assigned-member.add{
  cursor: pointer;
  user-select: none;
}

.avatar{
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8f4fa;
  color: #1a5270;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  border: 1px solid #a0d8f1;
  flex: 0 0 auto;
}

.add-avatar{
  background: #f0f7ff;
}

.member-name{
  font-size: 14px;
  color: #2c3e50;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-tooltip{
  position: absolute;
  top: 38px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 8px;
  border-radius: 10px;
  border: 1px solid #d4ecf8;
  background: #fff;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  width: fit-content;
  box-shadow: 0 4px 14px rgba(0,0,0,0.08);
  z-index: 30000;
  pointer-events: none;
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