import { reactive } from 'vue';

export const notificationStore = reactive({
  notifications: [],
  
  setNotifications(list) {
    this.notifications = list.sort((a, b) => new Date(b.createAt) - new Date(a.createAt));
  },
  
  addNotification(newNotification) {
    console.log("Store: addNotification called", newNotification);
    this.notifications.unshift(newNotification);
    // Keep it sorted just in case
    this.notifications.sort((a, b) => new Date(b.createAt) - new Date(a.createAt));
  },
  
  markAsRead(id) {
    const n = this.notifications.find(x => x.id === id);
    if (n) n.readStatus = true;
  },
  
  removeNotification(id) {
    this.notifications = this.notifications.filter(x => x.id !== id);
  }
});
