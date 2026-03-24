<script setup>
defineProps({
  title: {
    type: String,
    default:'Tên bảng'
  },
  type:{
    type: String,
    default:'primary'
  },
  status_text:{
    type: String,
    default:'Public'
  },
  content:{
    type:String,
    default:'Các task frontend cần hoàn thành trong sprint đầu tiên.'
  }
})
</script>

<template>
  <div class="card">
    <div class="Title">
        <div class="Title_content">{{ title }}</div>
        <div :class="['status',`status-${status_text}`]">{{ status_text }}</div>
    </div>
    <div class="content">{{ content }}</div>
    <div class="RowMember">
    <div class="member_card">
        <div class="avatar_1">A</div>
        <div class="avatar_2">B</div>
        <div class="avatar_2">C</div>
        <div class="memberCount">3 members</div>
    </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.card:hover .Title .status{
  background-color: #ffffff;
  border: 1.5px solid #3d5875;
}
.card:hover .Title .status-Private{
  color:#2f4562;
  background-color: #ffffff;
  border: 1.5px solid #3d5875;
}
.card:hover{
  background-color:#d4ecf8;
  border: 1.5px solid #3d5875;
}
.card:hover .member_card .avatar_2{
  background-color: #ffffff;
  border: 1.5px solid #d4ecf8;
}
.card:hover .member_card .avatar_1{
  background-color: #ffffff;
  border: 1.5px solid #d4ecf8;
}
.card:active{
  transform: scale(0.95);
}
.card{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-size: 15px;
  display: flex;
  flex-direction: column;    
  justify-items: left;
  border-radius: 1.25rem;
  border-width: 0;
  width: 100%;
  height: 180px;
  border: 1px solid #bce3f5;
  background-color: white;
  color:#2f4562;
  margin-top: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.Title_content{
    display: flex;
    font-size: large;
    align-self:flex-start;
    margin-left: 20px;
    margin-top: 15px;
    font-weight: bold;
}
.content{
    display: flex;
    justify-self:flex-start;
    font-size: large;
    margin-left: 20px;
    margin-right: 20px;
    margin-top: 15px;
    text-align: justify;
}
.status{
  font-family:"Quicksand", sans-serif;
  font-optical-sizing: auto;
  font-style:normal;
  font-weight: bold;
  font-size: 12px;
  display: flex;
  align-items: center;     
  justify-content: center;
  border-radius: 1.25rem;
  border-width: 0;
  width: 60px;
  height: 25px;
  border: 1.5px solid #74c5e1;
  background-color: #f0f7ff;
  color:#3d5875;
  margin-top: 15px;
}
.status-Private{
  border: 0px;
  background-color: #3d5875;
  color:#ffffff;
}
.Title{
    display: flex;
    justify-content: space-between;
    margin-right:20px;
}
.member_card{
    display: flex;
    margin-top: 10px;
    margin-left: 20px;
}
.avatar_1{
    display: flex;
    align-content: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border: 1.5px solid #ffffff;
    border-radius: 30px;
    background-color: #74c5e1;
    color:#3d5875;
    text-align: center;
    font-weight: bold;
}
.avatar_2{
    width: 30px;
    height: 30px;
    border: 2px solid #ffffff;
    border-radius: 30px;
    background-color: #74c5e1;
    color:#3d5875;
    margin-left: -5px;
    text-align: center;
    font-weight: bold;
}
.RowMember{
    display: flex;
    margin-top: auto; 
    padding-bottom: 15px;
}
.memberCount{
    margin-top: 2px;
    margin-left: 10px;

}
</style>
