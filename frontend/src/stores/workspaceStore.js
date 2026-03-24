import { reactive } from 'vue';

// Dùng reactive để tạo một biến toàn cục. 
// Bất cứ component nào thay đổi biến này, các component khác sẽ tự động cập nhật!
export const workspaceStore = reactive({
  workspaces: [], // Mảng chứa danh sách các bảng (không gian làm việc)
  
  // Hàm thêm một bảng mới vào mảng
  addWorkspace(newWorkspace) {
    this.workspaces.push(newWorkspace);
  },

  // Hàm cập nhật toàn bộ danh sách (dùng khi mới load trang)
  setWorkspaces(list) {
    this.workspaces = list;
  }
});