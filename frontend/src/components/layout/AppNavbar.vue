<script setup>
//import IconSetting from '../icons/IconSetting.vue';
import IconCreate from '../icons/IconCreate.vue';
import IconUser from '../icons/IconUser.vue';
import BaseButton from '../base/BaseButton.vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const emit = defineEmits(['open-modal','toggle-profile','toggle-notification']);

const goToLogin = () => {
    router.push('/');
}
const goToRegister = () => {
    router.push('/register');
}

const goToBoardView=()=>{
  router.push('/boards');
}
const props = defineProps({
  hasUnread: {
    type: Boolean,
    default: false
  },
  isAuthPage: {
    type: Boolean,
    default: false
  }
});
</script>

<template>
  <div class="navbar">
    <div class="list_icon" @click="goToBoardView">
      <img id="logo" src="../../assets/logo.png"></img>
      <div>Kabala</div>
    </div>
    <!-- <input class="search" placeholder="Search"></input> -->
    
    <div class="create" @click="emit('open-modal')" v-if="!isAuthPage">
      <div>Tạo mới</div>
      <IconCreate></IconCreate>
    </div>
    
    <div class="right_navbar" v-if="!isAuthPage">
      <div class="notification-container" @click="emit('toggle-notification')">
        <i id="icon1" class="fa-solid fa-bell"></i>
        <span v-if="hasUnread" class="unread-dot"></span>
      </div>
      <div id="icon2" class="avatar" @click="emit('toggle-profile')"><IconUser></IconUser></div>
      <!--<div id="icon3"><IconSetting/></div>-->
    </div>
    
    <div class="right_navbar_auth" v-if="isAuthPage">
      <BaseButton class="auth-btn login-btn" text="Đăng nhập" type="ghost" @click="goToLogin" />
      <BaseButton class="auth-btn register-btn" text="Đăng ký" type="ghost" @click="goToRegister" />
    </div>
  </div>
</template>
  


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
  #logo{
    margin-left: 5px;
    width: 23px;
    height: 23px;
  }
  .list_icon div{
    font-size: 22px;
    margin-left: 7px;
    font-weight: 550;
  }
  .list_icon{
    display: flex;
    font-size: 30px;
    color:rgb(255, 255, 255);
    align-content: center;
    align-items: center;
    justify-items: center;
    margin-right: 10px;
    cursor:pointer;
  }
  /*.search{
    border:1px solid grey;
    width: 50vw;
    height: 35px;
    font-size: 20px;
    border-radius: 1.25rem;
    text-align: center;
    justify-self:center;
    align-self: center;
    margin-left: 0;
  }
  .search::placeholder{
    color:#5a5a5a7e;
    text-align: center;
  }
  */
  i{
    font-size: 15px;
    color:#2c3e50;
  }
  #icon2{
    display: flex;
    background-color: #ffffff;
    border-radius: 20px;
    color:#2c3e50;
    justify-content: center;
    align-content: center;
    text-align: center;
    text-justify: auto;
    font-weight: bold;
  }
  #icon3{
    display: flex;
    color: #2c3e50;
    align-items: center;
    justify-content: center;
  }
  .navbar{
    background-color: #3d5875;
    display: flex;
    width: 100%;
    height: 60px;
    padding: 0 20px;
    justify-content: space-between;
    font-family: "Quicksand", sans-serif;
  }
  .right_navbar{
    display: flex;
    align-items: center;
    margin-right: 0px;
    cursor: pointer;
  }
  .right_navbar div{
    position: relative;
    width: 35px;
    height: 35px;
    background-color: #ffffff;
    color:#2c3e50;
    border-radius: 20px;
    justify-content: center;
    align-content: center;
    text-align: center;
    text-justify: auto;
    margin-left: 5px;
    margin-right: 5px;
    align-items: center;
    justify-items: center;
    transition: all 0.2s ease;
  }
  
   /*dấu chấm*/
  .unread-dot {
    position: absolute;
    top: 0px;
    right: 0px;
    width: 10px;
    height: 10px;
    background-color: #a0d8f1;
    border-radius: 50%;
    /* Optionally you can add a small padding or border if you want, but this is simple enough */
  }
  .right_navbar div:active{
    transform: scale(0.95);
  }
  .right_navbar_auth {
    display: flex;
    align-items: center;
    gap: 15px; /* Tăng khoảng cách chút */
  }
  
  /* Tuỳ chỉnh Button trong Auth Page */
  .auth-btn {
    width: 120px;
    font-weight: 600;
  }
  
  .login-btn {
    background-color: #bce3f5 !important;
  }
  
  .login-btn:hover {
    background-color: #d4ecf8 !important; /* Hover có màu đậm hơn chút xíu để nhận biết */
  }
  
  .register-btn:hover {
    background-color: #f2f7fc !important;
  }
  .create{
    cursor: pointer;
    display: flex;
    gap:5px;
    width: 100px;
    height: 35px;
    font-size: 16px;
    border-radius: 1.25rem;
    align-self: center;
    align-content: center;
    justify-content: center;
    padding: 5px;
    color:#2c3e50;
    align-items: center;
    font-weight: 500;
    margin-left: auto;
    margin-right: 10px;
    background-color: #ffffff;
    transition: all 0.2s ease;
  }
  .create:active {
  transform: scale(0.95); 
}
</style >