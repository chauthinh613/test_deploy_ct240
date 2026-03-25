<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import LoginLayout from '@/components/layout/LoginLayout.vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'

const router = useRouter()

// 1. Khai báo biến lưu trữ dữ liệu
const form = reactive({
  username: '',
  password: ''
})

const errorMessage = ref('')
const isLoading = ref(false) // Thêm biến này để làm hiệu ứng loading cho nút


const handleLogin = async () => {

  errorMessage.value = ''

  if (!form.username || !form.password) {
    errorMessage.value = "Vui lòng nhập đầy đủ thông tin!"
    return
  }

  isLoading.value = true

  try {

    const response = await api.post(
      "/auth/login",
      {
        username: form.username,
        password: form.password
      }
    )

    console.log("Đăng nhập thành công:", response.data)

    // lấy token đúng chỗ
    const token = response.data.data.token

    if (token) {
      localStorage.setItem("token", token)
    }

    router.push("/home")

  } catch (error) {

    const code = error.response?.data?.code

    console.log(code)

    if (code === 1101) {
      errorMessage.value = "Tài khoản không tồn tại!"
      alert("Tài khoản không tồn tại!")
    }else if (code === 1002) {
      errorMessage.value = "Tài khoản hoặc mật khẩu không hợp lệ"
      alert("Tài khoản hoặc mật khẩu không hợp lệ!")
    }
    else if (code === 1003) {
      errorMessage.value = "Sai mật khẩu!"
      alert("Sai mật khẩu!")
    } else {
      errorMessage.value = "Đăng nhập thất bại, vui lòng thử lại!"
      alert("Đăng nhập thất bại, vui lòng thử lại!")
    }

  } finally {

    isLoading.value = false

  }
}

// 3. Hàm chuyển trang đăng ký
const goToRegister = () => {
  router.push('/register')
}

</script>
<template>
  <div class="auth-wrapper">
    <AppNavbar :isAuthPage="true" />
    <LoginLayout>
       <form @submit.prevent="handleLogin">
  <div class="card">
        <div class="Title_content">Đăng nhập</div>
        <div class="label">Username</div>
        <input v-model="form.username"class="input">
        <div class="label">Password</div>
        <input v-model="form.password" class="input" type="password">
        <button class="button" id="login" type="submit">Đăng nhập</button>
        <button class="button" id="register" @click="goToRegister" type="button" :disabled="isLoading">Tạo tài khoản mới</button>
  </div>
  </form>
  </LoginLayout>
  </div>
</template>
<style scoped> 
.auth-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f0f7ff;
}
.card{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  display: flex;
  flex-direction: column;    
  border-radius: 1.25rem;
  border-width: 0;
  width: 300px;
  height: 350px;
  border: 1px solid #bce3f5;
  background-color: white;
  color:#2f4562;
  align-items: center;
  align-self: center;
  margin-top: 15vh;
}
.Title_content{
    font-size: large;
    margin-top: 15px;
    font-weight: bold;
}
.input{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  border-radius: 1.25rem;
  width: 250px;
  height: 40px;
  outline: none;
  padding: 4px;
  border: 2px solid #d4ecf8;
  color:#2f4562;
  background-color:#f0f7ff;
  margin-top: 5px;
  text-align: center;
}
.label{
    align-self: flex-start;
    margin-top:15px;
    margin-left:45px;
}
.input:focus{
  background-color:white;
  border: 2px solid #d4ecf8;
}
.forgot_pw{
    align-self: flex-start;
    margin-top: 10px;
    font-size: 13px;
    margin-left:45px;
    color:#74c5e1;
}
.register{
    align-self:center;
    margin-top: 10px;
    font-size: 13px;
    color:#74c5e1;
}
.button{
  margin-top: 30px;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  display: flex;
  align-items: center;     
  justify-content: center;
  border-radius: 1rem;
  border-width: 0;
  width: 120px;
  height: 30px;
}
#login{
  padding: 5px;
  border: 1px solid #bce3f5;
  background-color: white;
  box-shadow: 0 1px #bce3f5;
  color: #2f4562;
  cursor:pointer;
}
#register{
  margin-top: 20px;
  width: 150px;
  background-color: white;
  color:#2f4562;
  cursor:pointer;
  transition: all 0.3s ease;
}
#login:hover {
  background-color: #f2f7fc; 
  color: #0e171f;            
}
#login:active {
  transform: scale(0.95); 
}
#register:hover{
  color: #bce3f5; 
}
</style>
