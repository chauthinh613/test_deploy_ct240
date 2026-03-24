import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/auth/LoginView.vue'
import HomeView from '../components/layout/MainLayout.vue'
import SpaceView from '../views/space/SpaceView.vue'
import SpaceMemberView from '../views/space/SpaceMember.vue'
import Board_CardView from '@/views/board_card/Board_CardView.vue'
import BoardView from '../views/board_card/BoardView.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/auth/RegisterView.vue'),
    },
    {
      path:'/space',
      name:'space',
      component: () =>import('../views/space/SpaceView.vue'),
    },
    { path:'/space/:id',
      name: 'space-detail',
      component: SpaceView
    },
    { path:'/space/:id/members',
      name:'space-members',
      component:SpaceMemberView
    },
    { path:'/spaces/:idSpace/boards/:idBoard/cards',
      name: 'board-card',
      component: Board_CardView
    },
    { path: '/boards',
      name: 'boards',
      component: BoardView
    }
  ],
})

export default router
