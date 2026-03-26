<script setup>
import { ref,onMounted,watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'
import MainLayout from '@/components/layout/MainLayout.vue'
import Card from '@/components/base/BaseBoard.vue'
import Button from '@/components/base/BaseButton.vue'
import CreateBoardModal from '@/components/layout/CreateBoardModal.vue'; 
import router from '@/router'
import { globalBus } from '@/stores/eventbus.js';

const route=useRoute();
// 2. Thêm biến để điều khiển ẩn/hiện Modal
const isCreateBoardModalOpen = ref(false);

// 3. Cập nhật lại hàm mở Modal mà lúc nãy ta đã tạo
const openCreateBoardModal = () => {
  isCreateBoardModalOpen.value = true;
}

const closeCreateBoardModal = () => {
  isCreateBoardModalOpen.value = false;
}

// 4. Hàm xử lý khi nhận được dữ liệu từ Modal gửi lên
const handleCreateBoard = async (boardData) => {
  try {
    const spaceId = route.params.id; 
    
    if (!spaceId) {
      alert("Lỗi: Không tìm thấy ID của Không gian!");
      return;
    }

    const url = `/spaces/${spaceId}/boards`;
    
    const requestData = {
      name: boardData.name,               // Khớp @NotBlank String name;
      description: boardData.description, // Khớp String description;
      isPrivate: boardData.isPrivate,                   
    };

    console.log("Đang gửi data:", requestData, "đến URL:", url);

    const response = await api.post(url, requestData);
    
    boards.value.push(response.data.data);
    closeCreateBoardModal();
    alert("Tạo bảng thành công!");

  } catch (error) {
    // Xử lý lỗi chi tiết để không bị mù mờ nữa
    if (error.response) {
      // Có phản hồi từ Backend (Lỗi 400, 403, 404, 500...)
      console.error("LỖI TỪ BACKEND:", error.response.data);
      alert("Backend báo lỗi: " + JSON.stringify(error.response.data));
    } else {
      // Lỗi mạng hoặc không gọi được API
      console.error("LỖI MẠNG:", error);
      alert("Lỗi mạng: Không thể kết nối tới server!");
    }
  }
}
const isLoading = ref(true);

const spaceData = ref(null);

const boards=ref([]);

const fetchSpaceBoard= async (SpaceId)=>{
  isLoading.value=true;
  try{
    console.log(`📡 [SpaceView] Fetching boards for space: ${SpaceId}`);
    const SpaceRes= await api.get(`/spaces/${SpaceId}?t=${Date.now()}`);
    spaceData.value=SpaceRes.data.data || SpaceRes.data;

    const BoardRes =await api.get(`/spaces/${SpaceId}/boards?t=${Date.now()}`);
    const boardListData = BoardRes.data.data || BoardRes.data || [];
    boards.value = Array.isArray(boardListData) ? boardListData : [];
    console.log(`✅ [SpaceView] Received ${boards.value.length} boards for space ${SpaceId}.`, boards.value);
  }
  catch(error){
    console.error("❌ [SpaceView] Lỗi khi lấy dữ liệu bảng:", error);
    boards.value=[];
  }
  finally{
    isLoading.value=false;
  }
}

onMounted(()=>{
 if(route.params.id) 
 fetchSpaceBoard(route.params.id)
});

watch(
  ()=>route.params.id,
  (newId)=>{
    if(newId) fetchSpaceBoard(newId)
  }
)

watch(() => globalBus.signal, (newSignal) => {
  if (!newSignal) return;
  console.log("🕵️ SpaceView watch signal:", newSignal.action, newSignal);

  const currentSpaceId = route.params.id;
  if (!currentSpaceId) return;

  if (newSignal.action === 'RELOAD_BOARDS' || newSignal.action === 'RELOAD_ALL' || newSignal.action === 'RELOAD_PAGE') {
    // Chỉ reload nếu đúng space hoặc là reload_all/page
    const incomingSpaceId = String(newSignal.spaceId || '');
    const isMatch = newSignal.action === 'RELOAD_ALL' || newSignal.action === 'RELOAD_PAGE' || incomingSpaceId === String(currentSpaceId);
    
    if (isMatch) {
      console.log(`🚀 SpaceView: Tín hiệu khớp (${newSignal.action}). Đang thực hiện reload...`);
      setTimeout(() => {
        fetchSpaceBoard(currentSpaceId);
      }, 500); // Tăng delay lên một chút cho chắc chắn
    } else {
      console.log(`⏭️ SpaceView: Tín hiệu không dành cho space này (${incomingSpaceId} != ${currentSpaceId})`);
    }
  }
}, { deep: true });

const goTo_BoardCardView=(boardId)=>{
  const spaceId = route.params.id;
  router.push(`/spaces/${spaceId}/boards/${boardId}/cards`);
}
</script>
<template>
  <MainLayout>
    <div class="NameSpace">
        <div>{{ spaceData?.name || 'Đang tải...' }}</div>
    </div>
      <Button 
          @click="openCreateBoardModal"
          :text="'+ Tạo bảng mới'"
          :type="'ghost'">
      </Button>
        
        <CreateBoardModal 
        v-if="isCreateBoardModalOpen" 
        @close-modal="closeCreateBoardModal"
        @create-board="handleCreateBoard"
        />
        <div class="border_card">
        <Card
          v-for="board in boards"
          :key="board.id"
          :boardId="board.id"
          :title="board.name"
          :status_text="(board.isPrivate === true || board.private === true) ? 'Private' : 'Public'"
          :content="board.description||'Chưa có mô tả'"
          @click="goTo_BoardCardView(board.id)"></Card>
          </div>
  </MainLayout>
</template>
<style scoped> 
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.border_card{
  width: 96%;
  margin-right: 50px;
  display: grid;
  gap:20px 50px;
  grid-template-columns: repeat(3, 1fr);;
}
.NameSpace div{
  font-family:"Quicksand", sans-serif; 
  font-size: 25px;
  font-weight:600;
  margin-bottom: 5px;
}
</style>
