<script setup>
import { ref } from 'vue'
import axios from 'axios'

// 1. Khai báo Props (nhận dữ liệu từ cha)
const props = defineProps({
  boardId: {
    type: [String, Number],
    required: true
  }
})

// 2. Khai báo Emits (gửi sự kiện lên cha)
const emit = defineEmits(['close', 'created'])

const newCardName = ref('')
const isCreating = ref(false)

const closeModal = () => {
  newCardName.value = ''; // Reset input
  emit('close'); // Báo cho cha biết để đóng modal
}

const handleCreateCard = async () => {
  if (!newCardName.value.trim()) return;

  isCreating.value = true;
  try {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };

    // Body request (nhớ map đúng field với CardCreationRequest của backend)
    const requestBody = {
      name: newCardName.value 
    };

    await axios.post(`http://localhost:8080/api/boards/${props.boardId}/cards`, requestBody, { headers });

    // Thành công: báo cho cha biết để tải lại danh sách, rồi đóng modal
    emit('created');
    closeModal();
    
  } catch (error) {
    console.error("Lỗi khi tạo thẻ mới:", error);
    alert("Có lỗi xảy ra khi tạo thẻ!");
  } finally {
    isCreating.value = false;
  }
}
</script>

<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>Tạo thẻ mới</h2>
        
        <input 
          v-model="newCardName" 
          type="text" 
          placeholder="Nhập tên thẻ..." 
          class="modal-input"
          @keyup.enter="handleCreateCard" 
          autofocus
        />
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeModal" :disabled="isCreating">Hủy</button>
          <button class="btn-submit" @click="handleCreateCard" :disabled="isCreating">
            {{ isCreating ? 'Đang tạo...' : 'Lưu thẻ' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* Di chuyển toàn bộ CSS của Modal từ file cũ sang đây */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.4); display: flex;
  justify-content: center; align-items: center; z-index: 1000;
}
.modal-content {
  background: white; padding: 24px; border-radius: 1.25rem;
  width: 400px; max-width: 90%; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  font-family: "Quicksand", sans-serif;
}
.modal-content h2 { margin-top: 0; margin-bottom: 16px; font-size: 20px; }
.modal-input {
  width: 100%; padding: 10px 12px; border: 1px solid #ccc;
  border-radius: 1.25rem; font-size: 16px; margin-bottom: 20px;
  box-sizing: border-box; font-family: inherit;
}
.modal-input:focus {
  outline: none; border-color: #74c5e1;
}
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; }
.btn-cancel, .btn-submit {
  padding: 8px 16px; border-radius: 1.25rem; cursor: pointer;
  font-weight: 600; font-family: inherit; border: none;
}
.btn-cancel { background: #f1f1f1; color: #333; }
.btn-cancel:hover { background: #e1e1e1; }
.btn-submit { background: #0079bf;; color: white; }
.btn-submit:hover { background: #00659f;; }
.btn-submit:disabled, .btn-cancel:disabled { opacity: 0.6; cursor: not-allowed; }
</style>