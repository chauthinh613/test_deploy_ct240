<script setup>
import { ref } from 'vue'
import { workspaceStore } from '@/stores/workspaceStore.js'; 
import api from '@/services/api';
import { useRouter } from 'vue-router'

const emit=defineEmits(['close-modal']);
//Cac trang thai cua Modal
const newSpaceName = ref('');
const newSpaceDesc = ref('');

const closeModal = () => {
  emit('close-modal'); // Bắn tín hiệu kêu Layout tắt Modal đi
}

const handleCreateSpace = async () => {
   if(newSpaceName.value.trim() === ''){
    alert("Vui lòng nhập tên Space!");
    return;
  }

  try {
    const token = localStorage.getItem("token");
    if (!token) {
      alert("Phiên đăng nhập đã hết hạn!");
      router.push("/");
      return;
    }
    console.log("Đang gọi API tạo Space...");
    const response = await api.post("/spaces",
      { name: newSpaceName.value,
        description: newSpaceDesc.value
       }
    );
    const newWorkspace = response.data.data;
    workspaceStore.addWorkspace(newWorkspace);
    console.log("Tạo thành công:", response.data);
    closeModal();
    alert("Tạo bảng thành công");
  } catch (error) {
    console.error("Lỗi khi tạo bảng:", error);
    alert("Tạo thất bại, vui lòng thử lại!");
  }
}
</script>

<template>
  <div class="wrapper">
    <div class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <h3 class="modal-title">Tạo không gian làm việc mới</h3>
      
      <div class="modal-body">
        <label>Tên không gian làm việc <span style="color: red;">*</span></label>
        <input 
          v-model="newSpaceName" 
          type="text" 
          placeholder="Ví dụ: Dự án Website..." 
          class="modal-input"
          @keyup.enter="handleCreateSpace" 
        />
        </div>
        <div class="modal-body">
        <label>Mô tả</label>
        <input 
          v-model="newSpaceDesc" 
          type="text" 
          placeholder="" 
          class="modal-input"
          @keyup.enter="handleCreateSpace" 
        />
        </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-submit" @click="handleCreateSpace">Tạo mới</button>
      </div>
    </div>
  </div>
  </div>
</template>

<style scoped>
.wrapper{
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
  background-color: rgba(0, 0, 0, 0.4); /* Lớp nền đen mờ */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; /* Đảm bảo modal luôn nổi lên trên cùng */
  font-family: "Quicksand", sans-serif;
}

.modal-content {
  background: white;
  width: 400px;
  border-radius: 1.25rem;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
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
.modal-body:nth-last-child(2){
  margin-bottom: 30px;
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
  font-family: "Quicksand", sans-serif;
  box-sizing: border-box; /* Đảm bảo padding không làm tràn width */
}

.modal-input:focus {
  border-color: #3498db; /* Đổi màu viền khi đang gõ */
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-footer button {
  padding: 8px 16px;
  border-radius: 1.25rem;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: "Quicksand", sans-serif;
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
  background-color: #0079bf; /* Màu xanh giống Trello */
  color: white;
}

.btn-submit:hover {
  background-color: #026aa7;
}
</style>
