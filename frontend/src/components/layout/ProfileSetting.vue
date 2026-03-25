<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'

const emit = defineEmits(['close'])

const userInfo = ref({
  id: '',
  username: '',
  name: '',
  avatarURL: null
})

const isLoading = ref(true)

// Toggles
const isEditingPassword = ref(false)
const isEditingName = ref(false)

// Form states
const editName = ref('')
const passwords = ref({
  currentPassword: '',
  newPassword: ''
})

const fetchProfile = async () => {
  try {
    const res = await api.get('/users/profile')
    
    const data = res.data.data || res.data
    userInfo.value = data
    editName.value = data.name || ''
  } catch (error) {
    console.error('Lỗi khi tải thông tin user:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchProfile()
})

const handleEnableNameEdit = () => {
  isEditingName.value = true
}

const handleCancel = () => {
  emit('close');
}

const handleSave = async () => {
  const token = localStorage.getItem('token')
  const headers = { Authorization: `Bearer ${token}` }

  try {
    if (isEditingPassword.value) {
      if (!passwords.value.currentPassword || passwords.value.newPassword.length < 8) {
        alert('Mật khẩu mới phải có ít nhất 8 ký tự!');
        return;
      }
      
      // Gọi API đổi mật khẩu
      await api.put('/users/update/password', {
        currentPassword: passwords.value.currentPassword,
        newPassword: passwords.value.newPassword
      })
      
      alert('Đổi mật khẩu thành công!')
      emit('close')
    } else {
      // Gọi API đổi tên (và avatar nếu có)
      await api.put('/users/update', {
        name: editName.value,
        avatarURL: userInfo.value.avatarURL // Lấy lại avatar hiện tại
      })
      
      alert('Cập nhật thông tin thành công!')
      emit('close')
    }
  } catch (error) {
    const code = error.response?.data?.code;
    if (code === 1004) {
    alert('Mật khẩu không đúng!');
  } else {
    alert('Có lỗi xảy ra, vui lòng kiểm tra lại!');
  }
    console.error('Lỗi khi lưu:', error)
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="handleCancel">
    <div class="modal-container">
      
      <!-- Avatar & Username Section -->
      <div class="profile-header">
        <div class="avatar-circle">
          <template v-if="userInfo.avatarURL">
            <img :src="userInfo.avatarURL" alt="Avatar" class="avatar-img"/>
          </template>
          <template v-else>
            {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : '' }}
          </template>
        </div>
        <div class="username-display">@{{ userInfo.username }}</div>
      </div>

      <!-- Main Content Area -->
      <div v-if="!isEditingPassword" class="content-body">
        
        <!-- Name Field -->
        <div class="input-group">
          <div class="name-edit-container" :class="{ editing: isEditingName }">
            <input 
              v-model="editName" 
              type="text" 
              :readonly="!isEditingName"
              placeholder="Tên người dùng"
            />
            <button v-if="!isEditingName" @click="handleEnableNameEdit" class="icon-btn">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="pencil-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125" />
              </svg>
            </button>
          </div>
        </div>

        <div class="switch-mode-text" @click="isEditingPassword = true">
          Đổi mật khẩu
        </div>
      </div>

      <!-- Password Change Area -->
      <div v-else class="content-body">
        <div class="input-group">
          <label>Nhập mật khẩu hiện tại</label>
          <input 
            v-model="passwords.currentPassword" 
            type="password" 
            class="pill-input"
          />
        </div>
        <div class="input-group">
          <label>Nhập mật khẩu mới</label>
          <input 
            v-model="passwords.newPassword" 
            type="password" 
            class="pill-input"
          />
        </div>
      </div>

      <!-- Footer Buttons -->
      <div class="modal-footer">
        <button class="btn-cancel" @click="handleCancel">Huỷ</button>
        <button class="btn-save" @click="handleSave">Lưu</button>
      </div>

    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Quicksand:wght@300..700&display=swap');

* {
  box-sizing: border-box;
  font-family: "Quicksand", sans-serif;
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

.modal-container {
  background-color: #ffffff;
  width: 340px;
  border-radius: 30px;
  padding: 40px 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-circle {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background-color: #a3d8f4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #ffffff;
  font-weight: 500;
  overflow: hidden;
  margin-bottom: 15px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username-display {
  font-size: 16px;
  color: #333;
}

.content-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 14px;
  color: #333;
  margin-left: 15px;
}

.pill-input {
  width: 100%;
  height: 44px;
  border-radius: 22px;
  border: 1px solid #bce3f5;
  padding: 0 15px;
  outline: none;
  font-size: 14px;
  color: #333;
  transition: border-color 0.2s;
}

.pill-input:focus {
  border-color: #0277bd;
}

.name-edit-container {
  display: flex;
  align-items: center;
  width: 100%;
  height: 44px;
  border-radius: 22px;
  border: 1px solid #bce3f5;
  padding: 0 10px 0 20px;
  background-color: #fff;
  transition: border-color 0.2s;
}

.name-edit-container.editing {
  border-color: #0277bd;
}

.name-edit-container input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
  width: 100%;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  color: #555;
  transition: color 0.2s;
}

.icon-btn:hover {
  color: #0277bd;
}

.pencil-icon {
  width: 16px;
  height: 16px;
}

.switch-mode-text {
  text-align: center;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  margin-top: 10px;
  transition: color 0.2s;
}

.switch-mode-text:hover {
  text-decoration: underline;
  color: #555;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
}

.btn-cancel {
  background: transparent;
  border: none;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  padding: 10px 15px;
  margin-left: 5px;
}

.btn-cancel:hover {
  color: #000;
}

.btn-save {
  background-color: #0077c2;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 35px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-save:hover {
  background-color: #015f97;
}
</style>
