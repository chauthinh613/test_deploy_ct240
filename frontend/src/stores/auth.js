import { defineStore } from 'pinia';
import api from '@/services/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
  }),
  actions: {
    async login(credentials) {
      try {
        const response = await api.post('/auth/login', credentials);
        this.token = response.data.token;
        this.user = response.data.user;
        localStorage.setItem('token', this.token); 
        return true;
      } catch (error) {
        console.error("Lỗi đăng nhập:", error);
        return false;
      }
    },
    logout() {
      this.token = null;
      localStorage.removeItem('token');
    }
  }
});