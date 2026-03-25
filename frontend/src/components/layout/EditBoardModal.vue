<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'

const router = useRouter()

const props = defineProps({
  boardId: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close-modal', 'board-updated'])

const isFetching = ref(false)
const isSaving = ref(false)
const isDeleting = ref(false)

const boardName = ref('')
const boardDesc = ref('')
const isPrivate = ref(false)
const spaceId = ref('')
const currentUserId = ref('')

onMounted(async () => {
  try {
    const res = await api.get("/users/profile")
    currentUserId.value = res.data.data?.id ?? res.data?.id
  } catch(e) {}
})

const showMembers = ref(false)
const boardMembers = ref([])
const spaceMembers = ref([])

const closeModal = () => {
  emit('close-modal')
}

// 1. Fetch Board Info
const fetchBoard = async () => {
  if (!props.boardId) return
  isFetching.value = true
  try {
    const res = await api.get(`/boards/${props.boardId}`)
    const data = res.data?.data ?? res.data ?? {}

    boardName.value = data.name ?? ''
    boardDesc.value = data.description ?? ''
    
    // In case the backend returns "private" instead of "isPrivate" due to Spring Boot serialization defaults
    isPrivate.value = data.isPrivate === true || data.private === true || data.isPrivate === 'true' || data.private === 'true'
    spaceId.value = data.spaceId ?? ''

    if (spaceId.value) {
      await fetchMembers()
    }
  } catch (e) {
    alert('Không thể tải thông tin Board!')
  } finally {
    isFetching.value = false
  }
}

// 2. Fetch Members (Board & Space)
const fetchMembers = async () => {
  if (!props.boardId || !spaceId.value) return
  try {
    const [boardRes, spaceRes] = await Promise.all([
      api.get(`/boards/${props.boardId}/members`),
      api.get(`/spaces/${spaceId.value}/members`)
    ])

    // Update board members list
    boardMembers.value = boardRes.data?.data ?? boardRes.data ?? []
    
    // Update space members list
    spaceMembers.value = spaceRes.data?.data ?? spaceRes.data ?? []

  } catch (e) {
    console.error('Lỗi khi tải danh sách thành viên:', e)
  }
}

watch(
  () => props.boardId,
  () => {
    showMembers.value = false
    fetchBoard()
  },
  { immediate: true }
)

// lọc lại những người là space hông là thành viên của board
const availableSpaceMembers = computed(() => {
  return spaceMembers.value.filter(sm => {
    return !boardMembers.value.some(bm => bm.userResponse?.id === sm.userResponse?.id)
  })
})

const getAvatarText = (name) => {
  if (!name) return 'U'
  const first = name.trim().charAt(0)
  return first.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toUpperCase()
}

// 3. Toggle Privacy
const togglePrivate = () => {
  const currentStatus = isPrivate.value ? "Công khai" : "Riêng tư";
  const ok = window.confirm(`Bạn có chắc chắn muốn đổi sang ${currentStatus} không?`)
  if (ok) {
    isPrivate.value = !isPrivate.value
  }
}

const toggleMembersSection = () => {
  showMembers.value = !showMembers.value
}

// 4. Handle Save Board
const handleSave = async () => {
  if (!props.boardId) return

  isSaving.value = true
  try {
    const reqBody = {
      isPrivate: isPrivate.value
    }
    const nextName = boardName.value.trim()
    const nextDesc = boardDesc.value.trim()

    if (nextName) reqBody.name = nextName
    if (nextDesc) reqBody.description = nextDesc

    await api.put(
      `/boards/${props.boardId}`,
      reqBody
    )

    emit('board-updated')
    closeModal()
    alert('Lưu thành công!')
  } catch (e) {
    alert('Có lỗi xảy ra khi lưu hoặc bạn không có quyền')
  } finally {
    isSaving.value = false
  }
}

// 6. Handle Delete Board
const handleDeleteBoard = async () => {
  if (!props.boardId) return;

  const ok = window.confirm('Bạn có chắc chắn muốn xoá Board này không? (Toàn bộ dữ liệu sẽ bị mất)')
  if (!ok) return;

  isDeleting.value = true;
  try {
    await api.delete(`/boards/${props.boardId}`)
    
    closeModal()
    router.push('/home')
  } catch (e) {
    alert('Bạn không có quyền hoặc có lỗi xảy ra khi xoá Board')
  } finally {
    isDeleting.value = false;
  }
}

// 5. Handle Add Member
const handleAddMember = async (userId) => {
  try {
    await api.post(
      `/boards/${props.boardId}/members`,
      { userId: userId, isOwner: false }
    )
    
    // Re-fetch members to update UI
    await fetchMembers()
  } catch (e) {
    alert('Lỗi khi thêm thành viên')
  }
}

// 6. Handle Remove Member
const handleRemoveMember = async (userId) => {
  const isLeaving = userId === currentUserId.value;
  const msg = isLeaving 
    ? "Bạn có chắc chắn muốn rời khỏi Board này không?" 
    : "Bạn có chắc chắn muốn xoá thành viên này khỏi Board không?";

  const ok = window.confirm(msg)
  if (!ok) return;

  try {
    if (isLeaving) {
      await api.delete(`/boards/${props.boardId}/members`)
      closeModal()
      router.push('/home')
    } else {
      await api.delete(`/boards/${props.boardId}/members/${userId}`)
      // Re-fetch members to update UI
      await fetchMembers()
    }
  } catch (e) {
    alert(isLeaving ? 'Lỗi khi rời khỏi Board' : 'Lỗi khi xoá thành viên')
  }
}

</script>

<template>
  <div class="wrapper">
    <div class="modal-overlay" @click.self="closeModal">
      <!-- Container -->
      <div class="modal-content">
        <h3 class="modal-title">Cài đặt Board</h3>

        <!-- Form fields container -->
        <div class="form-container">
          <div class="modal-body inline-form">
            <div class="input-group flex-1">
              <label>Tên</label>
              <BaseInput
                v-model="boardName"
                type="text"
                class="modal-input"
                :disabled="isFetching || isSaving || isDeleting"
                @keyup.enter="handleSave"
              />
            </div>
            
            <BaseButton 
              :type="isPrivate ? 'dark' : 'ghost'"
              :text="isPrivate ? 'Riêng tư' : 'Công khai'"
              class="btn-toggle-private"
              @click="togglePrivate"
            />
          </div>

          <div class="modal-body">
            <label>Mô tả</label>
            <BaseInput
              v-model="boardDesc"
              type="text"
              class="modal-input"
              :disabled="isFetching || isSaving || isDeleting"
              @keyup.enter="handleSave"
            />
          </div>

          <div class="modal-footer">
            <BaseButton
              type="delete"
              text="Xóa"
              @click="handleDeleteBoard"
              :disabled="isFetching || isSaving || isDeleting"
            />

            <div class="footer-actions center-actions" v-if="!showMembers">
              <BaseButton
                type="ghost"
                text="Thành viên"
                @click="toggleMembersSection"
              />
            </div>

            <div class="footer-actions right-actions">
              <BaseButton
                type="transparent"
                text="Huỷ"
                @click="closeModal"
                :disabled="isSaving || isDeleting"
              />
              <BaseButton
                type="primary"
                text="Lưu"
                @click="handleSave"
                :disabled="isFetching || isSaving || isDeleting"
              />
            </div>
          </div>
        </div>

        <!-- Members Section -->
        <div v-if="showMembers" class="members-section">
          <!-- Board Members List -->
          <div class="members-list">
            <div 
              v-for="member in boardMembers" 
              :key="member.userResponse?.id" 
              class="member-item"
            >
              <div class="member-info">
                <div class="avatar">{{ getAvatarText(member.userResponse?.name) }}</div>
                <span class="member-name">{{ member.userResponse?.name }}</span>
              </div>
              <div class="member-actions">
                <span class="role-badge" :class="{ 'owner-badge': member.owner, 'added-badge': !member.owner }">
                  {{ member.owner ? 'Người tạo' : 'Đã thêm' }}
                </span>
                <button 
                  class="btn-remove-member" 
                  type="button" 
                  @click.stop="handleRemoveMember(member.userResponse?.id)"
                  :disabled="member.owner"
                  title="Xóa khỏi Board"
                >×</button>
              </div>
            </div>
          </div>

          <hr class="members-divider" />

          <!-- Space Members List (Available to add) -->
          <div class="members-list">
            <div 
              v-for="member in availableSpaceMembers" 
              :key="member.userResponse?.id" 
              class="member-item"
            >
              <div class="member-info">
                <div class="avatar">{{ getAvatarText(member.userResponse?.name) }}</div>
                <span class="member-name">{{ member.userResponse?.name }}</span>
              </div>
              <div class="member-actions">
                <button class="role-badge add-badge" type="button" @click="handleAddMember(member.userResponse?.id)">
                  Thêm
                </button>
                <button class="btn-remove-member" type="button" disabled>×</button>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Quicksand:wght@400;500;600;700&display=swap');

.wrapper {
  display: flex;
  align-self: center;
  align-items: center;
  justify-items: center;
  margin-top: 50px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  font-family: 'Quicksand', sans-serif;
}

.modal-content {
  background: white;
  width: 500px;
  border-radius: 1.25rem;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  max-height: 90vh;
}

.form-container {
  background: white;
  border-radius: 1.25rem;
  border: 1px solid #d4ecf8;
  padding: 20px;
}

.modal-title {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 700;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.inline-form {
  flex-direction: row;
  align-items: flex-end;
  gap: 16px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.flex-1 {
  flex: 1;
}

.modal-body label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #555;
}

.modal-input {
  width: 100% !important;
  text-align: left !important;
  padding: 10px 16px;
  border: 1px solid #d4ecf8;
  border-radius: 1.5rem;
  font-size: 0.95rem;
  outline: none;
  font-family: 'Quicksand', sans-serif;
  box-sizing: border-box;
  color: #2c3e50;
}

.modal-input:focus {
  border-color: #38bdf8;
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.2);
}

.btn-toggle-private {
  width: 120px;
}

/* Footer & Actions */
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  position: relative;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.center-actions {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.right-actions {
  justify-content: flex-end;
}

/* Members Section */
.members-section {
  background: #e8f4fa;
  border-radius: 1.25rem;
  padding: 16px;
  margin-top: -10px; 
  border: 1px solid #d4ecf8;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
  border-top: none;
}

/* Redesigning for the exact mockup look where it's a separate box below */
.form-container {
  z-index: 2;
  background: white;
}
.members-section {
  z-index: 1;
  margin-top: 10px;
  border-radius: 1.25rem;
  border-top: 1px solid #d4ecf8;
}

.members-divider {
  border: 0;
  border-top: 1px solid #a0d8f1;
  margin: 8px 0;
  opacity: 0.5;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 8px 16px;
  border-radius: 1.5rem;
  border: 1px solid #d4ecf8;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8f4fa;
  color: #1a5270;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
  border: 1px solid #a0d8f1;
}

.member-name {
  font-size: 0.9rem;
  color: #2c3e50;
  font-weight: 600;
}

.member-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-badge {
  padding: 6px 16px;
  border-radius: 1.5rem;
  font-size: 0.85rem;
  font-weight: 700;
  background: transparent;
  border: 1px solid #a0d8f1;
  color: #64748b;
  font-family: 'Quicksand', sans-serif;
  cursor: default;
  min-width: 90px;
  text-align: center;
}

.owner-badge {
  color: #1a5270;
  border-color: #a0d8f1;
  background-color: transparent;
}

.added-badge {
  color: #88a7b8;
  border-color: #d4ecf8;
}

.add-badge {
  color: #1a5270;
  border-color: #a0d8f1;
  cursor: pointer;
  background-color: transparent;
  transition: all 0.2s;
}

.add-badge:hover {
  background-color: #e8f4fa;
}

.btn-remove-member {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #e09ba5;
  background: white;
  color: #d97998;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  transition: all 0.2s;
}

.btn-remove-member:hover:not(:disabled) {
  background: #feedf3;
}

.btn-remove-member:disabled {
  border-color: #d4ecf8;
  color: #a0d8f1;
  cursor: not-allowed;
  background: white;
}
</style>
