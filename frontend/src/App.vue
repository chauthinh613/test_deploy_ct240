<script setup>
import { onMounted, ref, watch } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import { globalBus } from '@/stores/eventbus.js';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { notificationStore } from '@/stores/notificationStore.js';
import { useAuthStore } from '@/stores/auth.js';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const controller = ref(new AbortController());
let reconnectTimeout = null;

const setupGlobalSSE = async () => {
  const token = authStore.token || localStorage.getItem('token');

  console.log("setupGlobalSSE called");
  console.log("token:", token ? token.substring(0, 20) + '...' : 'NULL');

  if (!token) {
    console.log("SSE: Không có token, bỏ qua kết nối.");
    return;
  }

  if (reconnectTimeout) {
    clearTimeout(reconnectTimeout);
    reconnectTimeout = null;
  }

  if (controller.value) {
    controller.value.abort();
  }
  controller.value = new AbortController();

  try {
    await fetchEventSource('http://localhost:8080/api/notifications/subscribe', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'text/event-stream',
      },
      signal: controller.value.signal,
      openWhenHidden: true,

      onopen(response) {
        if (response.ok) {
          console.log("SSE: Kết nối thành công!");
        } else if (response.status === 401 || response.status === 403) {
          console.error("SSE: Token không hợp lệ hoặc hết hạn. Dừng kết nối.");
          controller.value.abort();
        } else {
          console.error("SSE: Kết nối thất bại, status:", response.status);
          throw new Error(`SSE error status: ${response.status}`);
        }
      },

      onmessage(event) {
        try {
          if (!event.data || event.data === ':heartbeat' || event.data === ':') return;

          const data = JSON.parse(event.data);

          const isReloadSignal = data.code === 2000;
          const isNotification = data.id !== undefined;

          // --- Tín hiệu reload ---
          if (isReloadSignal) {
            const typeStr = (data.type || '').toUpperCase();
            const currentRoute = route.name;
            const sid = data.spaceId;

            switch (typeStr) {

              case 'SPACE_BOARD_UPDATE':
                // Reload all bất kể đang ở trang nào
                globalBus.emitSignal('RELOAD_ALL', { spaceId: sid });
                console.log(`SSE: RELOAD_ALL, spaceId: ${sid}`);
                break;

              case 'CARD_TASK_UPDATE':
                // Chỉ reload khi đang ở trang board
                if (currentRoute === 'board-card') {
                  globalBus.emitSignal('RELOAD_BOARD_TASKS', { spaceId: sid });
                  console.log(`SSE: RELOAD_BOARD_TASKS, spaceId: ${sid}`);
                }
                break;

              case 'SPACE_MEMBER_UPDATE':
                // Chỉ reload khi đang ở trang space members
                if (currentRoute === 'space-members') {
                  globalBus.emitSignal('RELOAD_SPACE_MEMBERS', { spaceId: sid });
                  console.log(`SSE: RELOAD_SPACE_MEMBERS, spaceId: ${sid}`);
                }
                break;

              default:
                console.log(`SSE: Chưa xử lý type "${typeStr}"`);
            }
          }

          // --- Notification ---
          if (isNotification) {
            notificationStore.addNotification(data);

            const notiType = (data.type || '').toUpperCase();

            if (notiType === 'ADD_USER_IN_SPACE') {
              globalBus.emitSignal('RELOAD_ALL', { spaceId: data.referenceId });
              console.log(`SSE: RELOAD_ALL (ADD_USER_IN_SPACE), spaceId: ${data.referenceId}`);
            }

            if (notiType === 'DELETE_USER_FROM_SPACE') {
              const removedSpaceId = data.referenceId;

              // Nếu đang ở trong space bị xóa thif ra ngoài
              const currentPath = route.path;
              if (currentPath.includes(`/spaces/${removedSpaceId}`) || 
                  currentPath.includes(`/space/${removedSpaceId}`)) {
                console.log("SSE: Bị xóa khỏi space đang mở, redirect về home...");
                router.push('/home');
              }

              // Reload lại toàn bộ sau khi redirect
              globalBus.emitSignal('RELOAD_ALL', { spaceId: removedSpaceId });
            }

            if (notiType === 'CHANGE_ROLE_SPACE') {
              globalBus.emitSignal('RELOAD_ALL', { spaceId: data.referenceId });
            }
          }

        } catch (e) {
          console.error("SSE: Lỗi xử lý message:", e, event.data);
        }
      },

      onclose() {
        if (!controller.value.signal.aborted) {
          console.log("SSE: Server đóng kết nối. Thử lại sau 5s...");
          reconnectTimeout = setTimeout(() => setupGlobalSSE(), 5000);
        }
      },

      onerror(err) {
        if (controller.value.signal.aborted) return;
        if (err?.name === 'AbortError') return;
        console.error("SSE: Lỗi:", err);
      }
    });
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error("SSE: Lỗi kết nối:", error);
      reconnectTimeout = setTimeout(() => setupGlobalSSE(), 5000);
    }
  }
};

onMounted(() => {
  console.log("App.vue onMounted");
  setupGlobalSSE();
});

watch(() => authStore.token, (newToken) => {
  if (newToken) {
    setupGlobalSSE();
  } else {
    if (reconnectTimeout) clearTimeout(reconnectTimeout);
    controller.value.abort();
  }
});
</script>

<template>
  <RouterView />
</template>