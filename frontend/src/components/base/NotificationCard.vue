<script setup>
import { computed } from 'vue';

import axios from 'axios';

const props = defineProps({
  notification: {
    type: Object,
    required: true,
    default: () => ({
      id: '',
      content: '',
      readStatus: false,
      type: '',
      referenceId: '',
      createAt: ''
    })
  }
});

const emit = defineEmits(['marked-read', 'deleted']);

const handleAction = async () => {
  const token = localStorage.getItem('token');
  try {
    if (!props.notification.readStatus) {
      // Chưa đọc -> Gọi API đổi trạng thái thành đã đọc
      await axios.put(`http://localhost:8080/api/notifications/${props.notification.id}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      emit('marked-read', props.notification.id);
    } else {
      // Đã đọc -> Gọi API xoá thông báo
      await axios.delete(`http://localhost:8080/api/notifications/${props.notification.id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      emit('deleted', props.notification.id);
    }
  } catch (error) {
    console.error("Lỗi khi xử lý thông báo", error);
  }
};


//hiển thị từ gian ra xuất hiện thông báo
const formattedDate = computed(() => {
  if (!props.notification.createAt) return '';
  const date = new Date(props.notification.createAt);
  const now = new Date();
  
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
  if (diffInSeconds < 60) {
    return 'Vừa xong';
  }
  
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes} phút trước`;
  }
  
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours} giờ trước`;
  }
  
  // Hiển thị ngày và giờ nếu quá 1 ngày
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${hours}:${minutes} - ${day}/${month}/${year}`;
});
</script>

<template>
  <div class="notification-card" :class="{ 'is-read': notification.readStatus }">
    <div class="notification-content">
      {{ notification.content }}
    </div>
    
    <div class="notification-actions">
      <button 
        class="action-btn"
        :class="{ 'is-read': notification.readStatus }"
        @click="handleAction"
        :title="notification.readStatus ? 'Xoá thông báo' : 'Đánh dấu đã đọc'"
      >
        <span v-if="!notification.readStatus" class="icon-wrapper">
          <!-- Dấu tick cho chưa đọc -->
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="icon"><polyline points="20 6 9 17 4 12"></polyline></svg>
        </span>
        <span v-else class="icon-wrapper">
          <!-- Dấu X cho đã đọc -->
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="icon"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        </span>
      </button>
      
      <div class="notification-date">
        {{ formattedDate }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-card {
  display: flex;
  justify-content: space-between;
  background-color: var(--card, #ffffff);
  border: 1px solid var(--border, rgba(160, 216, 241, 0.3));
  border-radius: var(--radius, 1.25rem);
  padding: 16px 20px;
  margin-bottom: 12px;
  min-height: 80px;
  font-family: var(--font, 'Quicksand', -apple-system, sans-serif);
  color: var(--fg, #2c3e50);
  transition: background var(--transition, 0.22s ease), border-color var(--transition, 0.22s ease);
}

/* Kiểu cho notification chưa đọc, neengs sáng hơn*/
.notification-card:not(.is-read) {
  background-color: var(--muted, #e8f4fa);
  border-color: var(--primary, #a0d8f1);
}

.notification-content {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  padding-right: 16px;
  display: flex;
  align-items: center;
  line-height: 1.6;
}

/* Khi đã đọc, nội dung mờ đi */
.notification-card.is-read .notification-content {
  color: var(--muted-fg, #6b8799);
  font-weight: 400;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  min-width: 100px;
}

/* Nút hành động */
.action-btn {
  background-color: var(--primary, #a0d8f1);
  color: var(--primary-fg, #1a5270);
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  padding: 0;
  margin-bottom: 8px;
  transition: all var(--transition, 0.22s ease);
  outline: none;
}

.action-btn:hover {
  background-color: var(--accent, #bce3f5);
  transform: scale(1.05);
}

/* Nút khi đã đọc đổi sang màu xám*/
.action-btn.is-read {
  background-color: transparent;
  color: var(--muted-fg, #6b8799);
  border: 1.5px solid var(--border, rgba(160, 216, 241, 0.3));
}

.action-btn.is-read:hover {
  background-color: var(--muted, #e8f4fa);
  color: var(--fg, #2c3e50);
}

.icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.icon {
  width: 14px;
  height: 14px;
}

.notification-date {
  font-size: 12px;
  color: var(--muted-fg, #6b8799);
  white-space: nowrap;
}
</style>
