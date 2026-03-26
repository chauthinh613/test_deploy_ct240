<script setup>
import { ref, watch } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { workspaceStore } from '@/stores/workspaceStore.js'

const props = defineProps({
  /**
   * ID của space cần chỉnh sửa.
   * Parent mở modal chỉ cần truyền `spaceId`.
   */
  spaceId: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close-modal'])

const router = useRouter()

// Loading states
const isFetching = ref(false)
const isSaving = ref(false)
const isDeleting = ref(false)

// Form state
const spaceName = ref('')
const spaceDesc = ref('')

const closeModal = () => {
  emit('close-modal')
}

/**
 * Lấy thông tin space để hiển thị khi modal mở.
 * Endpoint đang dùng giống các chỗ khác trong dự án:
 * GET /api/spaces/{spaceId}
 */
const fetchSpace = async () => {
  if (!props.spaceId) return
  isFetching.value = true
  try {
    const token = localStorage.getItem('token')
    const headers = { Authorization: `Bearer ${token}` }

    const res = await api.get(`/spaces/${props.spaceId}`, { headers })
    const data = res.data?.data ?? res.data ?? {}

    spaceName.value = data.name ?? ''
    spaceDesc.value = data.description ?? ''
  } catch (e) {
    alert('Không thể tải thông tin Space!')
  } finally {
    isFetching.value = false
  }
}

watch(
  () => props.spaceId,
  () => fetchSpace(),
  { immediate: true }
)

/**
 * PUT /api/spaces/{spaceId}
 * - Chỉ gửi các field người dùng đã điền (optional theo API).
 * - Yêu cầu quyền OWNER (backend tự chặn).
 */
const handleSave = async () => {
  if (!props.spaceId) return

  isSaving.value = true
  try {
    const token = localStorage.getItem('token')
    const headers = { Authorization: `Bearer ${token}` }

    const reqBody = {}
    const nextName = spaceName.value.trim()
    const nextDesc = spaceDesc.value.trim()

    if (nextName) reqBody.name = nextName
    if (nextDesc) reqBody.description = nextDesc

    if (Object.keys(reqBody).length === 0) {
      alert('Vui lòng nhập ít nhất tên hoặc mô tả để cập nhật.')
      return
    }

    const res = await api.put(
      `/spaces/${props.spaceId}`,
      reqBody,
      { headers }
    )

    const updated = res.data?.data ?? res.data
    // Cập nhật lại store nếu bạn đang dùng workspaceStore
    if (updated && workspaceStore?.updateWorkspace) {
      workspaceStore.updateWorkspace(updated)
    }

    closeModal()
    alert('Lưu thành công!')
  } catch (e) {
    // Nếu backend trả 403/permission error thì báo "không có quyền"
    alert('Bạn không có quyền')
  } finally {
    isSaving.value = false
  }
}

/**
 * DELETE /api/spaces/{spaceId}
 * - Hiển thị confirm trước khi xoá.
 * - Sau khi xoá thành công: close modal + điều hướng về /home.
 */
const handleDelete = async () => {
  if (!props.spaceId) return

  const ok = window.confirm('Bạn có chắc chắn muốn xoá Space này không?')
  if (!ok) return

  isDeleting.value = true
  try {
    const token = localStorage.getItem('token')
    const headers = { Authorization: `Bearer ${token}` }

    await api.delete(`/spaces/${props.spaceId}`, { headers })

    closeModal()
    router.push('/home')
    alert('Xoá Space thành công!')
  } catch (e) {
    alert('Bạn không có quyền')
  } finally {
    isDeleting.value = false
  }
}
</script>

<template>
  <div class="wrapper">
    <div class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 class="modal-title">Chỉnh sửa không gian làm việc</h3>

        <div class="modal-body">
          <label>
            Tên không gian làm việc <span style="color: red;">*</span>
          </label>
          <input
            v-model="spaceName"
            type="text"
            placeholder="Ví dụ: Dự án Website..."
            class="modal-input"
            :disabled="isFetching || isSaving || isDeleting"
            @keyup.enter="handleSave"
          />
        </div>

        <div class="modal-body">
          <label>Mô tả</label>
          <input
            v-model="spaceDesc"
            type="text"
            placeholder="Mô tả ngắn..."
            class="modal-input"
            :disabled="isFetching || isSaving || isDeleting"
            @keyup.enter="handleSave"
          />
        </div>

        <div class="modal-footer">
          <button
            class="btn-delete"
            type="button"
            @click="handleDelete"
            :disabled="isFetching || isSaving || isDeleting"
          >
            {{ isDeleting ? 'Đang xoá...' : 'Xoá' }}
          </button>

          <div class="footer-actions">
            <button
              class="btn-cancel"
              type="button"
              @click="closeModal"
              :disabled="isSaving || isDeleting"
            >
              Hủy
            </button>
            <button
              class="btn-submit"
              type="button"
              @click="handleSave"
              :disabled="isFetching || isSaving || isDeleting"
            >
              {{ isSaving ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
  width: 420px;
  border-radius: 1.25rem;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-title {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 1.2rem;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 10px;
}

.modal-body label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #555;
}

.modal-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 1.2rem;
  font-size: 1rem;
  outline: none;
  font-family: 'Quicksand', sans-serif;
  box-sizing: border-box;
}

.modal-input:focus {
  border-color: #3498db;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex: 1;
}

.modal-footer button {
  padding: 8px 16px;
  border-radius: 1.25rem;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: 'Quicksand', sans-serif;
  font-weight: 600;
  border: none;
}

.btn-cancel {
  background-color: transparent;
  color: #555;
}

.btn-cancel:hover {
  background-color: #f0f0f0;
}

.btn-submit {
  background-color: #0079bf;
  color: white;
}

.btn-submit:hover {
  background-color: #026aa7;
}

.btn-delete {
  background-color: #d97998;
  color: #ffffff;
}

.btn-delete:hover {
  opacity: 0.9;
}
</style>

