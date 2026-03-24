<script setup>
import IconBoard from '../icons/IconBoard.vue'
import IconHome from '../icons/IconHome.vue'
import IconMember from '../icons/IconMember.vue'
import IconGears from '../icons/IconGears.vue'
import { workspaceStore } from '@/stores/workspaceStore.js'
import { deburr } from 'lodash'
import { ref, onMounted } from 'vue' 
import { useRoute, useRouter } from 'vue-router'
import EditSpaceModal from './EditSpaceModal.vue'
const router=useRouter();
const route = useRoute();

import axios from 'axios'; // ĐÃ THÊM: axios để gọi API

const openSpaceId=ref(null);

const toggleSpace=(id)=>{
  openSpaceId.value = openSpaceId.value=== id ? null:id;
}

// ĐÃ THÊM: onMounted để chạy code ngay khi mở trang


const isShow = ref(false)

// HÀM MỚI: Kéo danh sách Space từ server về khi load trang
const fetchWorkspaces = async () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) return; // Nếu chưa đăng nhập thì thôi không gọi

    const response = await axios.get("http://localhost:8080/api/spaces", {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    // Gán dữ liệu lấy được vào kho chung (Tùy cấu trúc API trả về mà bạn chỉnh .data cho đúng nhé)
    workspaceStore.workspaces = response.data.data || response.data; 
    
  } catch (error) {
    console.error("Lỗi khi lấy danh sách không gian làm việc:", error);
  }
}

// Chạy hàm fetchWorkspaces ngay khi Sidebar xuất hiện
onMounted(() => {
  fetchWorkspaces();
  if (route.params.idSpace) {
    openSpaceId.value = parseInt(route.params.idSpace);
  }
});

const goToSpace = (id) => {
  router.push(`/space/${id}`);
}

const goToAllBoards = () => {
  router.push('/boards');
}

const gotoSpaceMember =(id)=>{
  router.push(`/space/${id}/members`);
}

//Modal chỉnh sửa Space
const isEditSpaceModalOpen = ref(false)
const editSpaceId = ref(null)

// Mở popup chỉnh sửa space theo spaceId
const openEditSpace = (spaceId) => {
  editSpaceId.value = spaceId
  isEditSpaceModalOpen.value = true
}

const closeEditSpaceModal = () => {
  isEditSpaceModalOpen.value = false
  editSpaceId.value = null
}
      
</script>

<template>
  <div class="sidebar">
  
  <div class="Sidebar_items" @click="goToAllBoards" :class="{ 'current-page': route.path === '/boards' || route.path.startsWith('/space') }">
    <IconBoard></IconBoard>
    <div>Bảng</div>
  </div> 
  <div class="Sidebar_items" :class="{ 'current-page': route.path === '/home' || route.path === '/' }">
    <IconHome></IconHome>
    <div>Trang chủ</div>
  </div>
  <div class="separator"></div>
  <div class="label">Không gian làm việc</div>
  <div class="workspace-list">
      <div v-if="workspaceStore.workspaces.length === 0" class="empty-text">
        Chưa có bảng nào. Hãy tạo mới!
      </div>
      <div 
        v-for="space in workspaceStore.workspaces" 
        :key="space.id" 
        class="workspace-item"
      >
      <div class="space" :class="{ 'active': route.params.idSpace == space.id }" @click="toggleSpace(space.id)">
          <div class="space_avatar">{{deburr(space.name ? space.name.charAt(0).toUpperCase():'W') }}</div>
          <div class="space_name">{{ space.name}}</div>
      </div>
  <div v-if="openSpaceId === space.id" class="DetailOptionSpace">
      <div @click="goToSpace(space.id)" class="Option" :class="{ 'active': route.params.idSpace == space.id && !route.path.includes('/members') }">
        <IconBoard></IconBoard>
        <div>Bảng</div>
      </div>
      <div class="Option" :class="{ 'active': route.params.idSpace == space.id && route.path.includes('/members') }" @click="gotoSpaceMember(space.id)">
        <IconMember></IconMember>
        <div>Thành viên</div>
      </div>
      <div class="Option" @click.stop="openEditSpace(space.id)">
        <IconGears></IconGears>
        <div>Cài đặt</div>
      </div>
    </div>
  </div>
</div>
  </div>

  <!-- Popup chỉnh sửa Space -->
  <EditSpaceModal
    v-if="isEditSpaceModalOpen"
    :spaceId="editSpaceId"
    @close-modal="closeEditSpaceModal"
  />
</template>
  


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.icon_setting{
  width: 10px;
  height: 10px;
}
.DetailOptionSpace {
  margin-left:30px;
  padding: 10px;
  font-size: 14px;
}
.DetailOptionSpace div{
  margin-top: 2px;
  display: flex;
  align-items: center;
  align-content: center;
  flex-direction: row;
  gap: 7px;
}
.DetailOptionSpace div div{ 
  margin-left: 10px;
} 
.sidebar{
    padding: 30px 10%;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    font-family: "Quicksand", sans-serif;
    width: 100%;
    min-height: 100vh;
    height: 100%;
    gap: 5px;
    font-size: 17px;
    cursor:pointer;
    background-color: #ffff;
}
div.Sidebar_items{
  display: flex;
  align-items: center;
  flex-direction: row;
  color:#2c3e50;
  padding: 5px 15px;
  transition: all 0.2s ease;
}
div.Sidebar_items:hover{
  background-color:#e7e8eb;
  border-radius: 1.25rem;
}
.Sidebar_items div{
  align-self:center;
  margin-left: 6%;
  font-size: 18px;
  font-weight: 400;
}
.separator{
  width: 100%;
  height:1px;
  background-color: grey;
}
.space{
  display: flex;
  flex-direction: row;
  gap:10px;
  align-items: center;
  font-size: 13px;
  margin-top: 10px;
  transition: all 0.2s ease;
  padding: 5px;
  margin-left: -5px;
  border-radius: 1.25rem;
}
.space:hover{
  background-color:#e7e8eb;
}
.space.active{
  background-color:#f0f7ff;
}
.space_avatar{
  width: 30px;
  height: 30px;
  background-color:#74c5e1;
  border-radius: 1.25rem;
  color:rgb(255, 255, 255);
  font-weight:bold;
  text-align: center;
  align-content: center;
  justify-content: center;
}
.space_name{
  font-weight: 600;
}
.label{
  font-weight: 700;
}
.workspace-list{
  margin-left:3%;
}
.Option{
  font-size: 13px;
  padding-left: 20px;
  transition: all 0.2s ease;
}
.Option:hover{
  background-color:#e7e8eb;
  border-radius: 1.25rem;
}
.Option.active{
  background-color:#f0f7ff;
  border-radius: 1.25rem;
}
</style >