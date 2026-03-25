<script setup>
import { ref,onMounted,watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'
import draggable from 'vuedraggable'

import MainLayout from '@/components/layout/MainLayout.vue'
import Button from '@/components/base/BaseButton.vue'
import Card from '@/components/base/BaseCard.vue'
import Task from '@/components/base/BaseTask.vue'
import ModalCreateCard from '@/components/layout/CreateCard.vue' 
import TaskInfo from '@/components/layout/TaskInfo.vue'
import EditBoardModal from '@/components/layout/EditBoardModal.vue'

const isDetailTaskOpen=ref(false);
const isEditBoardModalOpen = ref(false);
const selectedTask=ref(null);
const OpenDetailTask=(task)=>{
  selectedTask.value=task;
  isDetailTaskOpen.value=true;
}

// Hàm đón dữ liệu update checkbox từ BaseTask.vue gửi lên
const handleUpdateTaskStatus = async (taskId, isChecked) => {
  try {
    
    let targetTask = null;
    for (const card of cards.value) {
      if (!card.tasks) continue;
      const found = card.tasks.find(t => t.id === taskId);
      if (found) {
        targetTask = found;
        break;
      }
    }
    
    const payload = targetTask ? {
      name: targetTask.name,
      deadline: targetTask.deadline,
      completed: isChecked
    } : { completed: isChecked };

    // Gọi API PUT để cập nhật trạng thái
    await api.put(`/tasks/${taskId}`, payload);
    
    console.log(`Đã cập nhật trạng thái task ${taskId} thành: ${isChecked}`);
    
    // Gọi lại API load board để giao diện tự động đồng bộ (giống như lúc bạn xóa thẻ)
    const spaceId = route.params.idSpace;
    const boardId = route.params.idBoard;
    fetchBoardCard(spaceId, boardId);
    
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái Task:", error);
    alert("Không thể cập nhật trạng thái!");
  }
};

const handleTaskChange = async (event, targetCardId) => {

  const action = event.added || event.moved;
  
  if (action) {
    const task = action.element; // Dữ liệu của task vừa kéo
    const newIndex = action.newIndex; // Vị trí mới trên frontend

    // Find the target card to calculate the average position
    const targetCard = cards.value.find(c => c.id === targetCardId);
    let newPosition = 1000;

    if (targetCard && targetCard.tasks) {
      const prevTask = targetCard.tasks[newIndex - 1];

      const nextTask = targetCard.tasks[newIndex + 1];

      if (!prevTask && !nextTask) {
        newPosition = 1000;
      } else if (!prevTask) {
        newPosition = Math.round(nextTask.position / 2);
      } else if (!nextTask) {
        newPosition = prevTask.position + 1000;
      } else {
        newPosition = Math.round((prevTask.position + nextTask.position) / 2);
      }
    }

    try {
      // Gọi API
      await api.put(`/tasks/${task.id}/move`, {
        cardId: targetCardId, // ID của cột hiện tại
        position: newPosition // Vị trí mới
      });

      // Quan trọng: Cập nhật lại position ở client để các lần thả sau không bị sai số
      task.position = newPosition;

      console.log(`Đã lưu Task "${task.name}" vào vị trí ${newPosition} của cột ${targetCardId}`);
    } catch (error) {
      console.error("Lỗi cập nhật vị trí Task:", error);
    }
  }
};


const handleCardChange = async (event) => {
  if (event.moved) {
    const card = event.moved.element;
    const newIndex = event.moved.newIndex;

    // Lấy danh sách cards hiện tại (đã được Vue cập nhật thứ tự sau khi kéo)
    const prevCard = cards.value[newIndex - 1]; // card phía trên
    const nextCard = cards.value[newIndex + 1]; // card phía dưới

    console.log(cards)
    console.log(prevCard)
    console.log(nextCard)

    let newPosition;

    if (!prevCard && !nextCard) {
      // Chỉ có 1 card
      newPosition = 1000;
    } else if (!prevCard) {
      // Kéo lên đầu danh sách
      newPosition = Math.round(nextCard.position / 2);
    } else if (!nextCard) {
      // Kéo xuống cuối danh sách
      newPosition = prevCard.position + 1000;
    } else {
      // Xen giữa 2 card → công thức của bạn
      newPosition = Math.round((prevCard.position + nextCard.position) / 2);
    }

    try {
      await api.put(`/cards/${card.id}/move`, {
        position: newPosition
      });

      // Quan trọng: Cập nhật lại position ở client để các lần thả sau không bị dùng dữ liệu cũ
      card.position = newPosition;

      console.log(`Đã chuyển Card "${card.name}" sang vị trí ${newPosition}`);
    } catch (error) {
      console.error("Lỗi cập nhật vị trí Card:", error);
    }
  }
};

const route=useRoute();
const isLoading = ref(true);

const boardData = ref(null);

const cards=ref([]);

const isModalOpen = ref(false);

const refreshListAfterCreate = () => {
  const spaceId = route.params.idSpace;
  const boardId = route.params.idBoard;
  fetchBoardCard(spaceId, boardId);
}

const fetchBoardCard = async (SpaceId, BoardId) => {
  isLoading.value = true;
  try {
    const BoardRes = await api.get(`/boards/${BoardId}`);
    boardData.value = BoardRes.data.data || BoardRes.data;

    const CardRes = await api.get(`/boards/${BoardId}/cards`);
    let tempCards = CardRes.data.data || CardRes.data;

    // Dùng Promise.all để lấy tất cả Task của các Card cùng lúc 
    const cardsWithTasks = await Promise.all(tempCards.map(async (card) => {
      try {
        // Gọi API lấy task của từng card 
        const taskRes = await api.get(`/cards/${card.id}/tasks`);
        
        // Trả về card có kèm thêm mảng tasks vừa lấy được
        return {
          ...card,
          tasks: taskRes.data.data || taskRes.data // Nhét task vào đây
        };
      } catch (err) {
        console.error(`Không lấy được task cho card ${card.id}`, err);
        return { ...card, tasks: [] }; // Lỗi thì cho mảng rỗng
      }
    }));

    //Cập nhật lại biến cards đã có đủ Task
    cards.value = cardsWithTasks;

  } catch (error) {
    console.error("Lỗi tổng thể:", error);
    cards.value = [];
  } finally {
    isLoading.value = false;
  }
}

const handleCreateTask = async (cardId, taskName) => {
  try {
    const payload = {
      name: taskName,
      completed: false 
    };

    await api.post(`/cards/${cardId}/tasks`, payload);

    console.log(`Đã thêm thành công thẻ: ${taskName} vào danh sách ${cardId}`);

    const spaceId = route.params.idSpace;
    const boardId = route.params.idBoard;
    fetchBoardCard(spaceId, boardId);

  } catch (error) {
    console.error("Lỗi khi gọi API thêm thẻ:", error);
  }
};

const handleDeleteCard=async(cardId)=>{
  try{
    await api.delete(`/cards/${cardId}`);
    
    console.log(`Đã xóa thành công danh sách có ID: ${cardId}`);
  const spaceId = route.params.idSpace;
    const boardId = route.params.idBoard;
    fetchBoardCard(spaceId, boardId);
    
  } catch (error) {
    console.error("Lỗi khi xóa danh sách (Card):", error);
    alert("Không thể xóa danh sách này, vui lòng thử lại!");
  }

}

const handleUpdateCardName = async (cardId, newName) => {
  try {
    await api.put(`/cards/${cardId}`, {
      name: newName
    });
    
    console.log(`Đã cập nhật tên danh sách ${cardId} thành: ${newName}`);
    
    const spaceId = route.params.idSpace;
    const boardId = route.params.idBoard;
    fetchBoardCard(spaceId, boardId);
    
  } catch (error) {
    console.error("Lỗi khi cập nhật tên danh sách:", error);
    alert("Không thể cập nhật tên danh sách!");
  }
};

onMounted(() => {
  const spaceId = route.params.idSpace; 
  const boardId = route.params.idBoard;
  
  if (spaceId && boardId) {
    fetchBoardCard(spaceId, boardId); 
  }
});

watch(
  () => route.params, 
  (newParams) => {
    if (newParams.idSpace && newParams.idBoard) {
      fetchBoardCard(newParams.idSpace, newParams.idBoard);
    }
  },
  { deep: true }
);
</script>
<template>
  <MainLayout>
    <div class="board-header">
      <div class="NameSpace">
          <div>{{ boardData?.name || 'Đang tải...' }}</div>
      </div>
      <div class="header-actions">
        <Button 
            :text="'Cài đặt'"
            :type="'ghost'"
            @click="isEditBoardModalOpen = true">
        </Button>
      </div>
    </div>
    <Button 
        :text="'+ Thêm thẻ mới'"
        :type="'ghost'"
        @click="isModalOpen = true">
    </Button>
          <draggable 
          v-model="cards" 
          item-key="id" 
          class="border_card" 
          animation="200"
          handle=".drag-handle-card" 
          ghost-class="ghost-card"
          group="cards"
          @change="handleCardChange"
        >
      <template #item="{ element: card }">
        <div class="drag-handle-card"> 
          <Card :key="card.id"
                :cardId="card.id"
                :name_card="card.name"
                @add-new-task="handleCreateTask"
                @delete-card="handleDeleteCard"
                @update-card-name="handleUpdateCardName"
          >
            <draggable 
            v-model="card.tasks" 
            item-key="id"
            group="tasks" 
            animation="200"
            ghost-class="ghost-task"
            class="task-list-container" 
            @change="handleTaskChange($event, card.id)"
            :filter="'input, label, button'"
            :preventOnFilter="false"
          >
            <template #item="{ element: task }">
            <Task
               :key="task.id"
               :taskId="task.id"
               :task_name="task.name"
               :deadline="task.deadline"
               :completed="task.completed"
               @update-status="handleUpdateTaskStatus"
               @click="OpenDetailTask(task)">
            </Task>
            </template>
            </draggable>
          </Card>
          </div>
      </template>
      </draggable>
        <ModalCreateCard 
      v-if="isModalOpen"
      :boardId="route.params.idBoard"
      @close="isModalOpen = false"
      @created="refreshListAfterCreate"
    />
    <TaskInfo v-if="isDetailTaskOpen"
      :task="selectedTask"
      @update-task="refreshListAfterCreate"
      @close="isDetailTaskOpen=false">
    </TaskInfo>
    <EditBoardModal
      v-if="isEditBoardModalOpen"
      :boardId="route.params.idBoard"
      @close-modal="isEditBoardModalOpen = false"
      @board-updated="refreshListAfterCreate"
    />
  </MainLayout>
</template>
<style scoped> 
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.border_card{
  border-radius: 1.25rem;
  border: 1px solid #bce3f5;
  width: 77.5vw;
  height: 70vh;
  margin-top: 20px;
  margin-right: 50px;
  display: flex;
  overflow-x: auto;
  gap:30px;
  background-color: #ffffff;
  padding:25px 25px;
  cursor: pointer;
}
.NameSpace div{
  font-family:"Quicksand", sans-serif; 
  font-size: 25px;
  font-weight:600;
  margin-bottom: 5px;
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-right: 50px;
}

.border_card::-webkit-scrollbar {
  height: 14px; 
}

.border_card::-webkit-scrollbar-track {
  background: transparent; 
  margin: 0 20px; 
}

.border_card::-webkit-scrollbar-thumb {
  background-color: #bce3f5; 
  border-radius: 20px; 
  border: 4px solid #ffffff; 
}

.border_card::-webkit-scrollbar-thumb:hover {
  background-color: #8bbcd6; 
}

/* Phần css cho hiệu ứng drag */
.task-list-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap:5px;
}
.ghost-card {
  opacity: 0.9;
}

.ghost-task {
  opacity: 0.9;
}

.drag-handle-card {
  cursor: grab;
}
.drag-handle-card:active {
  cursor: grabbing;
}
</style>
