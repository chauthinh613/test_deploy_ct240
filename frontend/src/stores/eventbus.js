import { reactive } from 'vue';

export const globalBus = reactive({
  signal: null,
  
  emitSignal(action, data = {}) {
    console.log(`📡 [globalBus] Emitting signal: ${action}`, data);
    this.signal = { 
      action, 
      ...data, 
      timestamp: Date.now() 
    };
  }
});