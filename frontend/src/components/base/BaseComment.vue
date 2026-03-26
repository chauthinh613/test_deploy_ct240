<script setup>
import IconUser from '../icons/IconUserComment.vue';
const props = defineProps({
  userName: {
    type: String,
    default: 'Username'
  },
  content: {
    type: String,
    default: ''
  },
  createAt: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['delete']);

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  
  const timeStr = date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  });
  
  const dayStr = date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
  
  return `${timeStr} - ${dayStr}`;
}
</script>
<template>
    <div class="info_user">
        <div class="user_avatar"><IconUser></IconUser></div>
        <div class="name_user">{{ userName }}</div>
        <div v-if="createAt" class="comment_date">{{ formatDate(createAt) }}</div>
        <div class="delete_comment_btn" @click="$emit('delete')" title="Xóa bình luận">×</div>
    </div>
    <div class="container">
        <div class="comment">{{ content }}</div>
    </div>
</template>
<style scoped>
.info_user{
    display: flex;
    margin-left: 20px;
    gap: 8px;
    flex-direction: row;
    margin-top: 10px;
    align-items: center;
    position: relative;
    width: 92%;
}
.name_user{
    font-size: 14px;
    color: #2f4562;
    font-weight: 700;
}
.comment_date {
    font-size: 13px;
    color: #88a7b8;
    font-weight: 500;
}
.delete_comment_btn {
    margin-left: 120px;
    font-size: 18px;
    cursor: pointer;
    color: #88a7b8;
    line-height: 1;
    padding: 3px 6px;
    transition: all 0.2s;
}
.delete_comment_btn:hover {
    color: #74c5e1;
}
.comment{
    margin-left: 10px;
    margin-right: 7px;
    font-size: 14px;
    color:#6b8799;
}
.container{
    margin-top: 5px;
    align-self:center;
    display:flex;
    align-items: center;
    padding:5px;
    gap:5px;
    width: 92%;
    height: fit-content;
    min-height: 40px;
    border-radius: 1.25rem;
    background-color: white;
    margin-bottom: 15px;
}
.user_avatar{
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  background-color: #bce3f5;
  color: white;
  padding: 7px;
  flex-shrink: 0;
}
</style>