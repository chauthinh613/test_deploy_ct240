<script setup>
import IconUser from '../icons/IconUserComment.vue';
defineProps({
  userName: {
    type: String,
    default: 'Username'
  },
  content: {
    type: String,
    default: ''
  }
});
</script>
<template>
    <div class="info_user">
    <div class="user_avatar"><IconUser></IconUser></div>
        <div class="name_user">{{ userName }}</div>
        </div>
    <div class="container">
        <div class="comment">{{ content }}</div>
        </div>
</template>
<style scoped>
.info_user{
    display: flex;
    margin-left: 25px;
    gap:5px;
    flex-direction: row;
    margin-top: 10px;
}
.name_user{
    font-size: 14px;
    color: #2f4562;
    font-weight: 500;
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
}
</style>