<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import LoginLayout from '@/components/layout/RegisterLayout.vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'

const router = useRouter()

// 1. Khai báo biến lưu trữ dữ liệu
const form = reactive({
  name:'',
  username: '',
  password: '',
  confirmpassword: ''
})

const errorMessage = ref('')
const isLoading = ref(false) // Thêm biến này để làm hiệu ứng loading cho nút


const handleRegister = async () => {

  errorMessage.value = ''

  if (!form.name||!form.username || !form.password||!form.confirmpassword) {
    errorMessage.value = "Vui lòng nhập đầy đủ thông tin!"
    return
  }

  isLoading.value = true

  try {

    const response = await api.post(
      "/auth/register",
      {
        name: form.name,
        username: form.username,
        password: form.password,
        //confirmpassword: form.confirmpassword
      }
    )

    console.log("Đăng ký thành công:", response.data)

    router.push("/")

  } catch (error) {

    const code = error.response?.data?.code

    if (code === 1100) {
      errorMessage.value = "Username đã tồn tại, vui lòng nhập username khác"
    } 

  console.log(errorMessage)

  } finally {

    isLoading.value = false

  }
  
  //Chuyển lại trang đăng nhập
  
}

</script>
<template>
  <div class="auth-wrapper">
    <AppNavbar :isAuthPage="true" />
    <RegisterLayout>
          <form @submit.prevent="handleRegister">
  <div class="card">
        <div class="Title_content">Đăng ký</div>
        <div  class="label">Name</div>
        <input v-model="form.name" class="input">
        <div class="label">Username</div>
        <input v-model="form.username" class="input">
        <div class="label">Password</div>
        <input v-model="form.password" class="input" type="password">
        <div class="label">Confirm password</div>
        <input v-model="form.confirmpassword" class="input" type="password">
        <button class="button" id="register" type="submit">Đăng ký</button>
  </div>
  </form>
  </RegisterLayout>
  </div>
</template>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.auth-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f0f7ff;
}
form{
  align-items: center;
  justify-items: center;
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
  height: 390px;
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
  height: 35px;
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
    margin-top:5px;
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
.button{
  margin-top: 15px;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  display: flex;
  align-items: center;     
  justify-content: center;
  border-radius: 0.7rem;
  border-width: 0;
  width: 90px;
  height: 30px;
}
#register{
  padding: 5px;
  border: 1px solid #bce3f5;
  background-color: white;
  box-shadow: 0 1px #bce3f5;
  color: #2f4562
}
#register:hover {
  background-color: #f2f7fc; 
  color: #0e171f;            
}
#register:active {
  transform: scale(0.95); 
}
</style>
