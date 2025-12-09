<template>
  <div class="friend-link-button">
    <button class="link-btn" @click="showModal = true">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
      </svg>
      <span class="link-text">关联</span>
    </button>
    
    <FriendLinkModal v-model:visible="showModal" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FriendLinkModal from './FriendLinkModal.vue'

const showModal = ref(false)
</script>

<style scoped>
.friend-link-button {
  position: fixed;
  left: 20px;
  bottom: 80px;
  z-index: 100;
}

.link-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.link-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
}

.link-btn:active {
  transform: translateY(0);
}

.link-text {
  font-size: 15px;
}

@media (max-width: 768px) {
  .friend-link-button {
    left: 16px;
    bottom: 70px;
  }
  
  .link-btn {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .link-text {
    font-size: 14px;
  }
}
</style>
