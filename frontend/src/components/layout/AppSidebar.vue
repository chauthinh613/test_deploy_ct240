<script setup>
import IconBoard from '../icons/IconBoard.vue'
//import IconHome from '../icons/IconHome.vue'
import IconMember from '../icons/IconMember.vue'
import IconGears from '../icons/IconGears.vue'
import { workspaceStore } from '@/stores/workspaceStore.js'
import { deburr } from 'lodash'
import { ref, onMounted, watch } from 'vue' 
import { useRoute, useRouter } from 'vue-router'
import { globalBus } from '@/stores/eventbus.js';
import EditSpaceModal from './EditSpaceModal.vue'
const router=useRouter();
const route = useRoute();

import api from '@/services/api';

const openSpaceId=ref(null);

const toggleSpace=(id)=>{
  openSpaceId.value = openSpaceId.value=== id ? null:id;
}

const isShow = ref(false)

const fetchWorkspaces = async () => {
  try {
    console.log("AppSidebar: Đang lấy danh sách workspaces...");
    // Thêm timestamp để bypass cache
    const response = await api.get(`/spaces?t=${Date.now()}`);
    
    // Gán dữ liệu lấy được vào kho chung 
    const data = response.data.data || response.data; 
    workspaceStore.workspaces = Array.isArray(data) ? data : [];
    console.log("AppSidebar: Đã cập nhật workspaces:", workspaceStore.workspaces);
    
  } catch (error) {
    console.error("AppSidebar: Lỗi khi lấy danh sách không gian làm việc:", error);
  }
}

// Chạy hàm fetchWorkspaces ngay khi Sidebar xuất hiện
onMounted(() => {
  fetchWorkspaces();
  if (route.params.idSpace && route.path.startsWith('/space/')) {
    openSpaceId.value = parseInt(route.params.idSpace)
  }
});

// Đồng bộ hóa khi có tín hiệu thay đổi Space
watch(() => globalBus.signal, (newSignal) => {
  if (!newSignal) return;
  console.log("AppSidebar: Nhận được tín hiệu từ globalBus:", newSignal);
  
  if (newSignal.action === 'RELOAD_SPACES' || newSignal.action === 'RELOAD_ALL' || newSignal.action === 'RELOAD_PAGE') {
    console.log(`AppSidebar: Tín hiệu khớp (${newSignal.action}). Đang thực hiện reload...`);
    // Thêm delay nhỏ để backend kịp đồng bộ nếu cần
    setTimeout(() => {
      fetchWorkspaces();
    }, 300);
  }
}, { deep: true });

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
  
  <div class="Sidebar_items" @click="goToAllBoards" :class="{ 'current-page': route.path === '/boards' }">
    <IconBoard></IconBoard>
    <div>Bảng</div>
  </div> 
  <!--<div class="Sidebar_items" :class="{ 'current-page': route.path === '/home' || route.path === '/' }">
    <IconHome></IconHome>
    <div>Trang chủ</div>
  </div>-->
  <div class="separator"></div>
  <div class="label">Không gian làm việc</div>
  <div class="workspace-list">
      <div v-if="workspaceStore.workspaces.length === 0" class="empty-text">
        Chưa có không gian nào. Hãy tạo mới!
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
  padding: 2px 15px;
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
.current-page{
  background-color:#d4ecf8;
  border-radius: 1.25rem;
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