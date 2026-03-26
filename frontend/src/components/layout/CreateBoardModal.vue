<script setup>
import { ref } from 'vue';

// Khai báo các sự kiện (emit) để gửi tín hiệu về cho trang cha (SpaceView)
const emit = defineEmits(['close-modal', 'create-board']);

const boardName = ref('');
const boardDescription = ref('');
const boardMode = ref('false');

// Hàm xử lý khi bấm nút Tạo mới
const handleCreate = () => {
  if (!boardName.value.trim()) {
    alert('Vui lòng nhập tên bảng!');
    return;
  }
  
  // Gửi dữ liệu đã nhập lên cho SpaceView xử lý
  emit('create-board', {
    name: boardName.value,
    description: boardDescription.value,
    isPrivate: boardMode.value
  });
};

// Hàm xử lý khi bấm nút Hủy hoặc bấm ra ngoài hộp thoại
const handleClose = () => {
  emit('close-modal');
};
</script>

<template>
  <div class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <h2 class="modal-title">Tạo bảng mới</h2>
      
      <div class="form-group">
        <label>Tên bảng <span class="required">*</span></label>
        <input v-model="boardName" type="text" placeholder="Nhập tên bảng..." />
      </div>
      
      <div class="form-group">
        <label>Mô tả bảng</label>
        <textarea v-model="boardDescription" placeholder="Nhập mô tả ngắn gọn..."></textarea>
      </div>
      <div class="form-group">
        <label> Chế độ hiển thị<span class="required"> *</span></label>
        <div class="mode">
        <input type="radio" id="public" name="access" :value="false" v-model="boardMode">
        <label for="public">Công khai</label>
        <input type="radio" id="private" name="access" :value="true" v-model="boardMode">
        <label for="private">Riêng tư</label>
        </div>
      </div>

      <div class="modal-actions">
        <button class="btn-cancel" @click="handleClose">Hủy</button>
        <button class="btn-create" @click="handleCreate">Tạo mới</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5); /* Nền mờ đen */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* Đảm bảo nổi lên trên cùng */
  font-family: "Quicksand", sans-serif;
}

.modal-content {
  background-color: white;
  padding: 25px;
  border-radius: 1.25rem;
  width: 400px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.modal-title {
  color: #2f4562;
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: bold;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 5px;
  color: #3d5875;
}

.required {
  color: red;
}

.form-group input,
.form-group textarea {
  padding: 10px;
  border: 1px solid #bce3f5;
  border-radius: 8px;
  font-family: inherit;
  outline: none;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #74c5e1;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

button {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  border: none;
  font-family: inherit;
}

.btn-cancel {
  background-color: #f0f7ff;
  color: #3d5875;
}

.btn-cancel:hover {
  background-color: #e2e8f0;
}

.btn-create {
  background-color: #74c5e1;
  color: white;
}

.btn-create:hover {
  background-color: #5baec9;
}
.mode{
    display: flex;
    gap:15px;
    align-items: center;
}
</style>