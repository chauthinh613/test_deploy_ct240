<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import MainLayout from '@/components/layout/MainLayout.vue'
import Button from '@/components/base/BaseButton.vue'
import MemberBar from '@/components/base/MemberBar.vue'
const route=useRoute();
const router = useRouter()
const isLoading = ref(true);

/**
 * Dữ liệu thông tin Space (dùng để hiển thị tên Space).
 */
const spaceData = ref(null);

/**
 * Danh sách thành viên trong Space để render UI.
 * Dạng dữ liệu backend trả về:
 * - { userResponse: { id, name, username, avatarURL }, role: OWNER|ADMIN|MEMBER }
 */
const members = ref([]);

/**
 * Từ khoá search user (bind vào input).
 * - Mỗi ký tự gõ vào sẽ gọi API `GET /api/users/search?keyword=...`.
 */
const searchKeyword = ref('')

/**
 * Danh sách gợi ý người dùng từ API search.
 */
const userSuggestions = ref([])

/**
 * User đang được chọn từ dropdown (dùng để lấy `userId` khi bấm "Thêm").
 */
const selectedUser = ref(null)

/**
 * Điều khiển ẩn/hiện dropdown gợi ý.
 */
const isSuggestionOpen = ref(false)

/**
 * Ref wrapper của search để detect click ra ngoài (đóng dropdown gợi ý).
 */
const searchWrapEl = ref(null)

/**
 * Trạng thái loading của search.
 */
const isSearching = ref(false)

/**
 * Trạng thái loading khi gọi API thêm member.
 */
const isAddingMember = ref(false)

/**
 * ID của user hiện tại (người đang đăng nhập).
 * Dùng để xác định trường hợp bấm X xoá chính mình => gọi API "rời khỏi Space".
 */
const currentUserId = ref(null)

/**
 * Lấy profile user hiện tại để lấy `id`.
 */
const fetchCurrentUserProfile = async () => {
  try {
    const res = await api.get('/users/profile')

    const data = res.data?.data ?? res.data
    currentUserId.value = data?.id ?? data?.user?.id ?? null
  } catch (e) {
    currentUserId.value = null
  }
}

/**
 * Chặn watch(searchKeyword) gọi API khi vừa chọn suggestion (fill programmatically).
 * - Mục tiêu: click chọn user là "xong luôn", không bị khựng do gọi API lại.
 */
const suppressSearch = ref(false)

/**
 * Lưu ID request search mới nhất để tránh race condition khi gõ nhanh.
 */
let latestSearchRequestId = 0

/**
 * Chuẩn hoá item search response -> UserResponse.
 * API có thể trả dạng `{ user: {...} }` hoặc trả thẳng object user.
 */
const normalizeUser = (x) => {
  if (!x) return null
  const u = x.user ?? x.userResponse ?? x.profile ?? x
  if (!u?.id) return null
  return {
    id: u.id,
    username: u.username ?? '',
    name: u.name ?? '',
    avatarURL: u.avatarURL ?? null
  }
}

/**
 * Label của user đang chọn (dùng để nhận biết user có đang gõ lại hay không).
 */
const selectedUserLabel = computed(() => {
  const u = selectedUser.value
  if (!u) return ''
  return u.name || u.username || ''
})

/**
 * Gọi API search user theo keyword.
 */
const fetchUserSuggestions = async (keyword) => {
  const requestId = ++latestSearchRequestId
  const trimmed = (keyword || '').trim()

  if (!trimmed) {
    userSuggestions.value = []
    isSuggestionOpen.value = false
    isSearching.value = false
    selectedUser.value = null
    return
  }

  isSearching.value = true
  isSuggestionOpen.value = true

  try {
    const res = await api.get('/users/search', {
      params: { keyword: trimmed }
    })

    // Nếu response không thuộc request mới nhất -> bỏ qua
    if (requestId !== latestSearchRequestId) return

    const raw = res.data?.data ?? res.data ?? []
    const list = Array.isArray(raw) ? raw : []
    userSuggestions.value = list.map(normalizeUser).filter(Boolean)
  } catch (e) {
    if (requestId !== latestSearchRequestId) return
    userSuggestions.value = []
  } finally {
    if (requestId !== latestSearchRequestId) return
    isSearching.value = false
  }
}

/**
 * Chọn 1 user trong dropdown:
 * - điền text vào input
 * - lưu `selectedUser` để khi bấm "Thêm" lấy `userId`
 * - đóng dropdown
 */
const selectSuggestion = (u) => {
  selectedUser.value = u
  // Fill vào input bằng username (theo yêu cầu)
  suppressSearch.value = true
  // invalidate request đang chạy (nếu có) để không reopen dropdown
  latestSearchRequestId += 1

  searchKeyword.value = u?.username || ''
  userSuggestions.value = []
  isSuggestionOpen.value = false

  // Mở lại search bình thường cho lần gõ tiếp theo
  queueMicrotask(() => {
    suppressSearch.value = false
  })
}

/**
 * Thêm member vào Space:
 * - POST /api/spaces/{spaceId}/members
 * - body: { userId, role: 'MEMBER' } (mặc định MEMBER theo yêu cầu)
 * - xong sẽ reload danh sách members.
 */
const addMemberToSpace = async () => {
  const spaceId = route.params.id
  const userId = selectedUser.value?.id
  if (!spaceId || !userId) return

  isAddingMember.value = true
  try {
    await api.post(
      `/spaces/${spaceId}/members`,
      { userId, role: 'MEMBER' }
    )

    // Reset input sau khi thêm
    selectedUser.value = null
    searchKeyword.value = ''
    userSuggestions.value = []
    isSuggestionOpen.value = false

    await fetchSpaceMember(spaceId)
  } finally {
    isAddingMember.value = false
  }
}

/**
 * Chuẩn hoá response backend -> list dễ render trong UI.
 * - Giữ nguyên `userResponse` để lấy id/name/username.
 * - Giữ `role` để bind combobox.
 */
const normalizeMembers = (raw) => {
  if (!Array.isArray(raw)) return []
  return raw
    .filter((x) => x && (x.userResponse || x.user || x.profile))
    .map((x) => ({
      userResponse: x.userResponse ?? x.user ?? x.profile,
      role: x.role ?? 'MEMBER'
    }))
}

const fetchSpaceMember= async (SpaceId)=>{
  isLoading.value=true;
  try{
  const SpaceRes= await api.get(`/spaces/${SpaceId}`);
  spaceData.value=SpaceRes.data.data || SpaceRes.data;

  // Lấy danh sách member theo endpoint riêng (nếu backend có).
  // Nếu API của bạn trả về khác key, bạn chỉ cần chỉnh đoạn map ở dưới.
  try {
    const MemberRes = await api.get(`/spaces/${SpaceId}/members`);
    const raw = MemberRes.data?.data ?? MemberRes.data ?? [];
    members.value = normalizeMembers(Array.isArray(raw) ? raw : (raw.members ?? []));
  } catch (e) {
    // Fallback: nếu endpoint /members chưa có, thử đọc từ spaceData (nếu backend nhúng sẵn members)
    const embedded = spaceData.value?.members ?? spaceData.value?.memberList ?? [];
    members.value = normalizeMembers(Array.isArray(embedded) ? embedded : []);
  }
  }
  catch(error){
    console.error("Lỗi khi lấy dữ liệu",error);
  }
  finally{
    isLoading.value=false;
  }
}

const onDocumentMouseDown = (e) => {
  const root = searchWrapEl.value
  if (!root) return
  if (!root.contains(e.target)) {
    isSuggestionOpen.value = false
  }
}

const onDocumentKeyDown = (e) => {
  if (e.key === 'Escape') isSuggestionOpen.value = false
}

onMounted(()=>{
  document.addEventListener('mousedown', onDocumentMouseDown)
  document.addEventListener('keydown', onDocumentKeyDown)

  // Lấy current user id để phục vụ chức năng "rời khỏi Space"
  fetchCurrentUserProfile()

  if(route.params.id) fetchSpaceMember(route.params.id)
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onDocumentMouseDown)
  document.removeEventListener('keydown', onDocumentKeyDown)
})

watch(
  ()=>route.params.id,
  (newId)=>{
    if(newId) fetchSpaceMember(newId)
  }
)

/**
 * Watch keyword: mỗi lần gõ 1 ký tự sẽ gọi API search.
 * - Nếu user đang gõ mà không khớp label đã chọn -> reset selectedUser.
 */
watch(
  () => searchKeyword.value,
  (val) => {
    if (suppressSearch.value) return
    if (val !== selectedUserLabel.value) selectedUser.value = null
    fetchUserSuggestions(val)
  }
)

/**
 * Hàm lấy tên hiển thị của member.
 * - Ưu tiên `name`, fallback sang `username`.
 */
const getMemberName = (m) => m?.name || m?.username || 'Người dùng'

/**
 * Hàm lấy role hiện tại của member (OWNER|ADMIN|MEMBER).
 * - Nếu backend trả về lowercase/khác format, bạn chỉnh ở đây để normalize.
 */
const getMemberRole = (m) => (m?.role || m?.memberRole || 'MEMBER')

/**
 * Lấy ID người dùng từ item member (dùng cho key, update role, remove).
 */
const getMemberUserId = (m) => m?.userResponse?.id || m?.id || m?.userId || m?.memberId

/**
 * Lấy tên hiển thị từ dạng backend { userResponse, role }.
 */
const getMemberDisplayName = (m) =>
  // Hiển thị: "Tên (@username)"; nếu không có name thì chỉ hiện "@username"
  (() => {
    const name = m?.userResponse?.name || ''
    const username = m?.userResponse?.username || ''
    if (name && username) return `${name} (@${username})`
    if (username) return `@${username}`
    return name || getMemberName(m)
  })()

/**
 * Lấy role từ dạng backend { userResponse, role }.
 */
const getMemberDisplayRole = (m) => getMemberRole(m)

/**
 * Danh sách userId đang được gọi API update role (dùng để disable UI theo từng dòng).
 */
const updatingRoleUserIds = ref(new Set())

/**
 * Danh sách userId đang được gọi API xoá member (dùng để disable UI theo từng dòng).
 */
const deletingUserIds = ref(new Set())

/**
 * Check 1 userId có đang update role hay không.
 */
const isUpdatingRole = (userId) => updatingRoleUserIds.value.has(userId)

/**
 * Check 1 userId có đang xoá member hay không.
 */
const isDeletingMember = (userId) => deletingUserIds.value.has(userId)

/**
 * Bật/tắt trạng thái updating theo userId (tạo Set mới để Vue nhận biết thay đổi).
 */
const setUpdatingRole = (userId, isUpdating) => {
  const next = new Set(updatingRoleUserIds.value)
  if (isUpdating) next.add(userId)
  else next.delete(userId)
  updatingRoleUserIds.value = next
}

/**
 * Bật/tắt trạng thái deleting theo userId (tạo Set mới để Vue nhận biết thay đổi).
 */
const setDeletingMember = (userId, isDeleting) => {
  const next = new Set(deletingUserIds.value)
  if (isDeleting) next.add(userId)
  else next.delete(userId)
  deletingUserIds.value = next
}

/**
 * Xử lý đổi role từ combobox.
 * - Gọi API PUT /api/spaces/{spaceId}/members/{userId}
 * - Nếu lỗi (ví dụ không có quyền OWNER): báo "Bạn không có quyền" và rollback bằng cách reload list.
 */
const handleUpdateRole = async (member, newRole) => {
  const spaceId = route.params.id
  const userId = getMemberUserId(member)
  const currentRole = getMemberDisplayRole(member)

  // Backend chỉ cho update ADMIN | MEMBER
  if (newRole === 'OWNER') {
    alert('Không thể chuyển vai trò sang OWNER.')
    return
  }

  if (!spaceId || !userId) return

  // Nếu role không đổi thì thôi
  if (newRole === currentRole) return

  setUpdatingRole(userId, true)

  // Optimistic UI
  members.value = members.value.map((m) => {
    const mid = getMemberUserId(m)
    return mid === userId ? { ...m, role: newRole } : m
  })

  try {
    await api.put(
      `/spaces/${spaceId}/members/${userId}`,
      {
        id: userId, // thuộc tính `id`: id của user cần update
        role: newRole // thuộc tính `role`: role mới (ADMIN | MEMBER)
      }
    )
  } catch (e) {
    // Rollback: load lại từ server để đảm bảo role đúng
    alert('Bạn không có quyền')
    await fetchSpaceMember(spaceId)
  } finally {
    setUpdatingRole(userId, false)
  }
}

/**
 * Xử lý xoá member (bấm nút X).
 * - Hiển thị confirm trước khi xoá.
 * - Nếu đồng ý: gọi API DELETE /api/spaces/{spaceId}/members/{userId}
 * - Xoá xong: reload danh sách members.
 */
const handleRemoveMember = async (member) => {
  const spaceId = route.params.id
  const userId = getMemberUserId(member)
  const name = getMemberDisplayName(member)

  if (!spaceId || !userId) return

  // Nếu người dùng tự xoá chính mình => rời khỏi Space (endpoint khác)
  const isSelf = currentUserId.value && userId === currentUserId.value

  // Confirm xoá/rời
  const ok = window.confirm(
    isSelf
      ? 'Bạn có chắc chắn muốn rời khỏi Space này không?'
      : `Bạn có chắc chắn muốn xoá "${name}" khỏi Space không?`
  )
  if (!ok) return

  setDeletingMember(userId, true)
  try {

    if (isSelf) {
      // API: user hiện tại rời khỏi Space. OWNER không được quyền rời (backend sẽ trả lỗi).
      await api.delete(`/spaces/${spaceId}/members`)

      // Rời khỏi space thì điều hướng ra ngoài cho UX mượt (về /home).
      router.push('/home')
      return
    }

    await api.delete(`/spaces/${spaceId}/members/${userId}`)
    await fetchSpaceMember(spaceId)
  } catch (e) {
    alert('Bạn không có quyền')
  } finally {
    setDeletingMember(userId, false)
  }
}
</script>
<template>
  <MainLayout>
    <div class="NameSpace">
        <div>{{ spaceData?.name || 'Đang tải...' }}</div>
    </div>
    <div class="container">
    <div class="AddMember">
      <div ref="searchWrapEl" class="search-wrap">
        <!-- searchKeyword: text đang gõ để gọi API search -->
        <input
          v-model="searchKeyword"
          class="search-input"
          type="text"
          placeholder="Nhập tên người dùng muốn thêm vào Space"
          @focus="isSuggestionOpen = !!searchKeyword"
          @keydown.esc="isSuggestionOpen = false"
        />

        <!-- Dropdown gợi ý user (bo tròn giống popover hồ sơ) -->
        <div v-if="isSuggestionOpen" class="suggestions">
          <div v-if="isSearching" class="suggestion-empty">Đang tìm...</div>
          <div v-else-if="userSuggestions.length === 0" class="suggestion-empty">Không tìm thấy người dùng</div>
          <template v-else>
            <button
              v-for="u in userSuggestions"
              :key="u.id"
              class="suggestion-item"
              type="button"
              @click="selectSuggestion(u)"
            >
              <div class="suggestion-name">{{ u.name || u.username }}</div>
              <div class="suggestion-username" v-if="u.name && u.username">@{{ u.username }}</div>
            </button>
          </template>
        </div>
      </div>

      <!-- addMemberToSpace: gọi API add member với role mặc định MEMBER -->
      <Button
        :type="'secondary'"
        :text="isAddingMember ? 'Đang thêm...' : 'Thêm'"
        @click="addMemberToSpace"
      />
    </div>
    <div class="membersInSpace">
      <div v-if="isLoading" class="empty">Đang tải danh sách thành viên...</div>
      <div v-else-if="members.length === 0" class="empty">Chưa có thành viên nào trong Space.</div>
      <div v-else class="list">
        <!--
          name: tên hiển thị của member
          role: role enum (OWNER|ADMIN|MEMBER) để bind combobox
          roleEditable: cho phép đổi role (false = chỉ xem)
          removable: hiện/ẩn nút X
          disabled: disable thẻ khi đang gọi API
        -->
        <MemberBar
          v-for="m in members"
          :key="getMemberUserId(m) ?? getMemberDisplayName(m)"
          :name="getMemberDisplayName(m)"
          :role="getMemberDisplayRole(m)"
          :roleEditable="getMemberDisplayRole(m) !== 'OWNER'"
          :removable="true"
          :disabled="isUpdatingRole(getMemberUserId(m)) || isDeletingMember(getMemberUserId(m))"
          @update:role="(r) => handleUpdateRole(m, r)"
          @remove="() => handleRemoveMember(m)"
        />
      </div>
    </div>
    </div>
  </MainLayout>
</template>
<style scoped> 
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');
.NameSpace div{
  font-family:"Quicksand", sans-serif; 
  font-size: 25px;
  font-weight:600;
  margin-bottom: 5px;
}
.container{
    margin-top:20px;
    display: flex;
    justify-self:center;
    flex-direction: column;
    gap:10px;
    width: 80%;
    align-items: center;
    justify-content: center;
}
.AddMember{
    display: flex;
    align-items: center;
    justify-content: center;
    gap:10px;
    width: 100%;
}

/* Hover nút "Thêm" (BaseButton type=secondary) ngay trong SpaceMember */
:deep(.btn-secondary:hover){
  background-color: #ffffff; /* nền trắng hơn khi hover */
  cursor: pointer;
  border: 1px solid #d4ecf8;
}

/* Wrapper cho input + dropdown gợi ý */
.search-wrap{
  position: relative;
  width: 50%;
}

/* Input tìm user (match style pastel) */
.search-input{
  font-family:"Quicksand", sans-serif;
  font-size: 15px;
  border-radius: 1.25rem;
  width: 100%;
  height: 40px;
  outline: none;
  padding: 4px 14px;
  border: 1px solid #d4ecf8;
  color:#2f4562;
  background-color:#ffffff;
}
.search-input:focus{
  border: 2px solid #d4ecf8;
}

/* Dropdown gợi ý: bo tròn giống popover hồ sơ */
.suggestions{
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  z-index: 60;

  background: #ffffff;
  border: 0.5px solid #d4ecf8;
  border-radius: 1.25rem;
  padding: 8px;

  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 260px;
  overflow: auto;
}
.suggestion-item{
  width: 100%;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  border-radius: 0.9rem;
  padding: 10px 12px;
  font-family:"Quicksand", sans-serif;
}
.suggestion-item:hover{
  background: #e8f4fa;
}
.suggestion-name{
  font-weight: 700;
  color: #2c3e50;
  font-size: 14px;
}
.suggestion-username{
  margin-top: 2px;
  color: #6b8799;
  font-size: 12px;
  font-weight: 600;
}
.suggestion-empty{
  padding: 10px 12px;
  color: #6b8799;
  font-weight: 600;
  font-size: 13px;
}
.membersInSpace{
    display: flex;
    flex-direction: column; /* danh sách xếp dọc */
    margin-top: 50px;
    border-radius: 1.25rem;
    width: 750px;
    height: 400px;
    background-color: #e8f4fa; /* muted background giống design system */
    border: 1px solid #d4ecf8;
    padding: 14px;
    overflow-y: auto; /* nhiều item thì cuộn dọc */
    overflow-x: hidden;
    box-sizing: border-box;
}

.list{
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty{
  font-family:"Quicksand", sans-serif;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #6b8799;
  font-weight: 600;
  font-size: 14px;
  text-align: center;
}
</style>
