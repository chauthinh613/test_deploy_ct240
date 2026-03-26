<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '@/services/api'

const props = defineProps({
  boardId: {
    type: [String, Number],
    required: false
  },
  title: {
    type: String,
    default: 'Tên bảng'
  },
  type: {
    type: String,
    default: 'primary'
  },
  status_text: {
    type: String,
    default: 'Public'
  },
  content: {
    type: String,
    default: 'Các task frontend cần hoàn thành trong sprint đầu tiên.'
  }
})

const boardMembers = ref([]) // [{ userResponse: UserResponse, isOwner }]
const isFetchingMembers = ref(false)

const fetchBoardMembers = async () => {
  if (!props.boardId) return
  isFetchingMembers.value = true
  try {
    const token = localStorage.getItem('token')
    const headers = { Authorization: `Bearer ${token}` }
    const res = await api.get(`/boards/${props.boardId}/members`, { headers })
    boardMembers.value = res.data?.data ?? res.data ?? []
  } catch (e) {
    console.error('Load board members error:', e)
    boardMembers.value = []
  } finally {
    isFetchingMembers.value = false
  }
}

const getAvatarTextFromUser = (u) => {
  const raw = (u?.name || u?.username || '').toString()
  if (!raw.trim()) return 'U'
  const first = raw.trim().charAt(0)
  return first.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase()
}

onMounted(() => {
  fetchBoardMembers()
})

watch(
  () => props.boardId,
  () => fetchBoardMembers()
)

const usersInBoard = computed(() => {
  return (boardMembers.value || []).map((bm) => bm?.userResponse).filter(Boolean)
})

const shouldCollapseToPlus = computed(() => usersInBoard.value.length > 3)
const firstMembers = computed(() => {
  // Khi hien thi tren 3 avt, avt cuoi se bang tong so luong -2
  return shouldCollapseToPlus.value ? usersInBoard.value.slice(0, 2) : usersInBoard.value.slice(0, 3)
})

const remainingMembers = computed(() => {
  // Chỉ dùng cho tooltip khi ô + xuất hiện
  return shouldCollapseToPlus.value ? usersInBoard.value.slice(2) : []
})

const remainingCount = computed(() => {
  return shouldCollapseToPlus.value ? Math.max(0, usersInBoard.value.length - 2) : 0
})

const totalCountText = computed(() => `${usersInBoard.value.length} members`)

const hoveredMember = ref(null) // UserResponse | null
const showRemainingTooltip = ref(false)
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
        <div class="member-avatars">
          <div
            v-for="(u, idx) in firstMembers"
            :key="u.id"
            class="member-avatar"
            :class="{ 'member-avatar-overlap': idx > 0 }"
            @mouseenter="hoveredMember = u"
            @mouseleave="hoveredMember = null"
          >
            {{ getAvatarTextFromUser(u) }}
            <div v-if="hoveredMember?.id === u.id" class="member-tooltip">
              {{ u.username }}
            </div>
          </div>

          <div
            v-if="shouldCollapseToPlus && remainingCount > 0"
            class="member-more"
            @mouseenter="showRemainingTooltip = true"
            @mouseleave="showRemainingTooltip = false"
          >
            <div class="member-avatar member-avatar-overlap">
              +{{ remainingCount }}
            </div>

            <div v-if="showRemainingTooltip" class="member-more-tooltip">
              <div
                v-for="u in remainingMembers"
                :key="u.id"
                class="member-more-item"
              >
                {{ u.username }}
              </div>
            </div>
          </div>
        </div>

        <div class="memberCount">{{ totalCountText }}</div>
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
.card:hover .member_card .member-avatar{
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
    align-items: center;
    margin-top: 10px;
    margin-left: 20px;
}
.member-avatars{
    display: flex;
    align-items: center;
    position: relative;
}
.member-avatar{
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
    position: relative;
}
.member-avatar-overlap{
    margin-left: -5px;
}
.member-tooltip{
    position: absolute;
    top: 34px;
    left: 50%;
    transform: translateX(-50%);
    padding: 4px 8px;
    border-radius: 10px;
    border: 1px solid #d4ecf8;
    background: #ffffff;
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
    white-space: nowrap;
    z-index: 10;
    pointer-events: none;
    box-shadow: 0 4px 14px rgba(0,0,0,0.06);
}

.member-more{
    position: relative;
}

.member-more-tooltip{
    position: absolute;
    top: 38px;
    left: 50%;
    transform: translateX(-50%);
    background: #ffffff;
    border: 1px solid #d4ecf8;
    border-radius: 12px;
    padding: 8px 10px;
    min-width: 120px;
    max-width: 240px;
    z-index: 10;
    box-shadow: 0 4px 14px rgba(0,0,0,0.06);
}

.member-more-item{
    font-size: 12px;
    font-weight: 700;
    color: #64748b;
    padding: 4px 0;
    white-space: nowrap;
}

.member-more-item + .member-more-item{
    border-top: 1px solid rgba(212, 236, 248, 0.7);
}
.RowMember{
    display: flex;
    margin-top: auto; 
    padding-bottom: 15px;
}
.memberCount{
    margin-top: 2px;
    margin-left: 10px;
    font-size: 14px;
}
</style>
