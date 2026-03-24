<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { deburr } from 'lodash'

const props = defineProps({
  /**
   * Tên hiển thị của thành viên (dùng để render text + lấy ký tự avatar).
   */
  name: {
    type: String,
    required: true
  },

  /**
   * Role hiện tại của thành viên (khớp enum backend: OWNER | ADMIN | MEMBER).
   * Dùng để set giá trị mặc định cho combobox.
   */
  role: {
    type: String,
    required: true,
    validator: (v) => ['OWNER', 'ADMIN', 'MEMBER'].includes(v)
  },

  /**
   * Cho phép chỉnh role hay không.
   * Nếu false: combobox sẽ bị disable (UI chỉ để xem).
   */
  roleEditable: {
    type: Boolean,
    default: true
  },

  /**
   * Cho phép hiển thị nút X để xoá thành viên khỏi space hay không.
   * Nếu false: ẩn nút X.
   */
  removable: {
    type: Boolean,
    default: true
  },

  /**
   * Trạng thái disable toàn bộ thẻ (thường dùng khi đang gọi API update role/xoá).
   */
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  /**
   * Bắn ra khi người dùng đổi role từ combobox.
   * Payload: role mới (OWNER | ADMIN | MEMBER).
   */
  'update:role',

  /**
   * Bắn ra khi bấm nút X xoá member.
   */
  'remove'
])

const avatarText = computed(() => {
  const first = (props.name || '').trim().charAt(0) || 'U'
  return deburr(first).toUpperCase()
})

const roleLabel = (r) => {
  // Label tiếng Việt để hiển thị trong combobox
  if (r === 'OWNER') return 'Người tạo'
  if (r === 'ADMIN') return 'Admin'
  return 'Thành viên'
}

/**
 * Điều khiển trạng thái mở/đóng menu role (combobox custom).
 * Lý do: native <select> không style được dropdown bo tròn như popover hồ sơ.
 */
const isRoleMenuOpen = ref(false)

/**
 * Ref root wrapper để detect click ra ngoài (đóng menu).
 */
const roleMenuRootEl = ref(null)

/**
 * Toggle mở/đóng menu role.
 */
const toggleRoleMenu = () => {
  if (props.disabled || !props.roleEditable) return
  isRoleMenuOpen.value = !isRoleMenuOpen.value
}

/**
 * Chọn role mới từ menu.
 * - emit update:role để parent cập nhật state / gọi API.
 * - đóng menu sau khi chọn.
 */
const selectRole = (newRole) => {
  emit('update:role', newRole)
  isRoleMenuOpen.value = false
}

const onDocumentMouseDown = (e) => {
  const root = roleMenuRootEl.value
  if (!root) return
  if (!root.contains(e.target)) {
    isRoleMenuOpen.value = false
  }
}

const onDocumentKeyDown = (e) => {
  if (e.key === 'Escape') isRoleMenuOpen.value = false
}

onMounted(() => {
  document.addEventListener('mousedown', onDocumentMouseDown)
  document.addEventListener('keydown', onDocumentKeyDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onDocumentMouseDown)
  document.removeEventListener('keydown', onDocumentKeyDown)
})
</script>

<template>
  <div class="memberbar" :class="{ 'is-disabled': disabled }">
    <div class="left">
      <div class="avatar" aria-hidden="true">{{ avatarText }}</div>
      <div class="name" :title="name">{{ name }}</div>
    </div>

    <div class="right">
      <div ref="roleMenuRootEl" class="role-menu-root">
        <button
          class="role-trigger"
          type="button"
          :disabled="disabled || !roleEditable"
          aria-label="Chọn vai trò"
          :aria-expanded="isRoleMenuOpen ? 'true' : 'false'"
          @click="toggleRoleMenu"
        >
          <span>{{ roleLabel(role) }}</span>
          <span class="chev" aria-hidden="true">▾</span>
        </button>

        <div v-if="isRoleMenuOpen" class="role-menu" role="listbox" aria-label="Danh sách vai trò">
          <button
            class="role-item"
            type="button"
            role="option"
            :aria-selected="role === 'ADMIN' ? 'true' : 'false'"
            @click="selectRole('ADMIN')"
          >
            {{ roleLabel('ADMIN') }}
          </button>
          <button
            class="role-item"
            type="button"
            role="option"
            :aria-selected="role === 'MEMBER' ? 'true' : 'false'"
            @click="selectRole('MEMBER')"
          >
            {{ roleLabel('MEMBER') }}
          </button>
        </div>
      </div>

      <button
        v-if="removable"
        class="remove"
        type="button"
        :disabled="disabled"
        aria-label="Xoá thành viên"
        @click="emit('remove')"
      >
        ×
      </button>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Google+Sans+Flex:opsz,wght@6..144,1..1000&family=Quicksand:wght@300..700&display=swap');

.memberbar {
  font-family: 'Quicksand', sans-serif;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;

  width: 100%;
  height: 52px;
  padding: 0 10px;

  border-radius: 1.5rem;
  background: #ffffff;
  border: 1px solid rgba(160, 216, 241, 0.3); /* giống token --border */
}

.memberbar.is-disabled {
  opacity: 0.75;
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;

  background: #e8f4fa; /* muted */
  border: 1px solid rgba(160, 216, 241, 0.3);
  color: #2c3e50;
  font-weight: 700;
  font-size: 13px;
}

.name {
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
}

.role-menu-root {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.role-trigger {
  position: relative; /* để đặt icon ▾ cố định bên phải */
  font-family: 'Quicksand', sans-serif;
  font-size: 13px;
  font-weight: 700;

  height: 34px;
  width: 120px; /* cố định chiều dài để đồng bộ giữa các dòng */
  padding: 0 34px 0 14px; /* chừa chỗ cho icon ▾ bên phải */
  border-radius: 999px;

  background: #e8f4fa; /* muted */
  border: 1px solid rgba(160, 216, 241, 0.3);
  color: #1a5270;
  cursor: pointer;
  outline: none;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
}

.role-trigger:disabled {
  cursor: not-allowed;
  opacity: 0.85;
}

.role-trigger:focus {
  border-color: #a0d8f1;
  background: #ffffff;
}

.chev {
  position: absolute;
  right: 12px; /* cố định sát phải */
  font-size: 12px;
  color: #6b8799;
}

.role-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;

  display: flex;
  flex-direction: column;
  gap: 2px;

  padding: 8px;
  border-radius: 1.25rem; 
  border: 0.5px solid #d4ecf8;
  background: #ffffff;
  z-index: 50;
}

.role-item {
  font-family: 'Quicksand', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;

  width: 100%;
  text-align: left;
  padding: 10px 12px;
  border-radius: 0.9rem;
  border: none;
  background: transparent;
  cursor: pointer;
}

.role-item[aria-selected='true'] {
  background: #d4ecf8; /* secondary */
  color: #1a5270;
}

.role-item:hover {
  background: #e8f4fa; /* muted */
}

.remove {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  border: 1px solid #d97998;
  background: #feedf3;
  color: #7d1f3a;
  cursor: pointer;
  font-size: 18px;
  line-height: 0;
  display: grid;
  place-items: center;
}

.remove:hover {
  background: #f8a5c2; /* secondary */
}

.remove:active {
  transform: scale(0.96);
}

.remove:disabled {
  cursor: not-allowed;
}
</style>

