<script setup>
import { ref,onMounted,watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import MainLayout from '@/components/layout/MainLayout.vue'
import Card from '@/components/base/BaseBoard.vue'
import Button from '@/components/base/BaseButton.vue'
import CreateBoardModal from '@/components/layout/CreateBoardModal.vue'; 
import router from '@/router'

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

    const token = localStorage.getItem('token');
    
    const url = `http://localhost:8080/api/spaces/${spaceId}/boards`;
    
    const requestData = {
      name: boardData.name,               // Khớp @NotBlank String name;
      description: boardData.description, // Khớp String description;
      isPrivate: boardData.isPrivate,                   
    };

    console.log("Đang gửi data:", requestData, "đến URL:", url);

    const response = await axios.post(url, requestData, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
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
  const token=localStorage.getItem('token');
  const headers={'Authorization':`Bearer ${token}`};

  const SpaceRes= await axios.get(`http://localhost:8080/api/spaces/${SpaceId}`,{headers});
  spaceData.value=SpaceRes.data.data || SpaceRes.data;

  const BoardRes =await axios.get(`http://localhost:8080/api/spaces/${SpaceId}/boards`,{headers});
  boards.value=BoardRes.data.data|| BoardRes.data;
  }
  catch(error){
    console.error("Lỗi khi lấy dữ liệu bảng:",error);
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
