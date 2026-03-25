<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import MainLayout from '@/components/layout/MainLayout.vue'
import Card from '@/components/base/BaseBoard.vue'

const router = useRouter();
const spaces = ref([]);
const isLoading = ref(true);

const fetchAllSpacesAndBoards = async () => {
  try {
    const response = await api.get("/spaces");
    
    let spaceData = response.data.data || response.data;
    if (!Array.isArray(spaceData)) spaceData = [];

    // Gọi API lấy boards cho từng space song song
    const spacesWithBoards = await Promise.all(
      spaceData.map(async (space) => {
        try {
          const boardRes = await api.get(`/spaces/${space.id}/boards`);
          return {
            ...space,
            boards: boardRes.data.data || boardRes.data || []
          };
        } catch (err) {
          console.error(`Không lấy được boards cho space ${space.id}`);
          return { ...space, boards: [] }; // Trả về mảng rỗng nếu lỗi
        }
      })
    );

    spaces.value = spacesWithBoards;

  } catch (error) {
    console.error("Lỗi khi lấy dữ liệu các Không gian làm việc:", error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchAllSpacesAndBoards();
});

const goToBoard = (spaceId, boardId) => {
  router.push(`/spaces/${spaceId}/boards/${boardId}/cards`);
};

const goToSpace = (spaceId) => {
  router.push(`/space/${spaceId}`);
};
</script>

<template>
  <MainLayout>
    <div class="boards-page">
      <div class="page-header">Các bảng của bạn</div>
      
      <div v-if="isLoading" class="loading-state">Đang tải dữ liệu...</div>
      
      <div v-else-if="spaces.length === 0" class="empty-state">
        Bạn chưa có không gian làm việc nào.
      </div>

      <div v-else>
        <!-- Lặp từng Space -->
        <div v-for="space in spaces" :key="space.id" class="space-section">
          <div class="space-title" @click="goToSpace(space.id)">
            <div class="space-icon">{{ space.name ? space.name.charAt(0).toUpperCase() : 'S' }}</div>
            <div class="space-name">{{ space.name }}</div>
          </div>
          
          <div class="border_card">
            <!-- Hiển thị tối đa 2 boards -->
            <Card 
              v-for="board in space.boards.slice(0, 2)" 
              :key="board.id"
              :boardId="board.id"
              :title="board.name"
              :status_text="(board.isPrivate === true || board.private === true) ? 'Private' : 'Public'"
              :content="board.description || 'Chưa có mô tả'"
              @click="goToBoard(space.id, board.id)"
            />

            <!-- Nếu còn trên 2 boards, thẻ thứ 3 sẽ là nút xem thêm -->
            <div 
              v-if="space.boards.length > 2" 
              class="extra-boards"
              @click="goToSpace(space.id)"
            >
              <div class="extra-text">Xem thêm +{{ space.boards.length - 2 }}</div>
            </div>
            
            <div v-if="space.boards.length === 0" class="no-board-text">
               Không gian này chưa có bảng nào.
            </div>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');

.boards-page {
  font-family: "Quicksand", sans-serif;
  color: #1a5270;
}
.page-header {
  font-size: 25px;
  font-weight: 600;
  margin-bottom: 25px;
}
.loading-state, .empty-state, .no-board-text {
  font-size: 15px;
  color: #6b8799;
  font-style: italic;
  margin-left: 20px;
}
.space-section {
  margin-bottom: 35px;
}
.space-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: opacity 0.2s;
  width: fit-content;
}
.space-title:hover {
  opacity: 0.8;
}
.space-icon {
  width: 32px;
  height: 32px;
  background-color: #74c5e1;
  color: white;
  border-radius: 8px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.space-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.border_card{
  width: 96%;
  display: grid;
  gap:20px 50px;
  grid-template-columns: repeat(3, 1fr);
  margin-bottom: 20px;
}

/* Style riêng cho nút con số lượng bổ sung */
.extra-boards {
  background-color: #ffffff;
  border-radius: 1.25rem;
  border: 2px dashed #bce3f5;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
  height: 180px;
  width: 100%;
  margin-top: 18px;
  box-sizing: border-box;
}
.extra-boards:hover {
  background-color: #e7e8eb;
  border-color: #a0d8f1;
}
.extra-text {
  font-size: 16px;
  font-weight: 600;
  color: #6b8799;
}
</style>
