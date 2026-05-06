<template>
  <div class="action-buttons">
    <router-link to="/help" class="action-btn help-btn" title="帮助中心">
      <i class="fas fa-question-circle"></i>
    </router-link>
  </div>
</template>

<script setup>
</script>

<style scoped>
.action-buttons {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  text-decoration: none;
  border: none;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.action-btn:active {
  transform: translateY(-1px);
}

.help-btn {
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
  color: white;
}

.help-btn:hover {
  background: linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%);
  color: white;
}

@media (max-width: 768px) {
  .action-buttons {
    right: 16px;
    bottom: 16px;
    gap: 10px;
  }
  
  .action-btn {
    width: 50px;
    height: 50px;
    font-size: 22px;
  }
}
</style>
