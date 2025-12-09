<template>
  <div class="christmas-hat">
    <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <!-- 帽子主体 -->
      <path d="M 20 60 Q 50 20 80 60" fill="#c41e3a" />
      <!-- 帽子边缘 -->
      <ellipse cx="50" cy="60" rx="32" ry="8" fill="#ffffff" />
      <!-- 帽子顶部球 -->
      <circle cx="50" cy="20" r="8" fill="#ffffff" />
    </svg>
  </div>
</template>

<style scoped>
.christmas-hat {
  position: absolute;
  top: -15px;
  right: -10px;
  width: 40px;
  height: 40px;
  z-index: 10;
  pointer-events: none;
  animation: swing 2s ease-in-out infinite;
}

@keyframes swing {
  0%, 100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}
</style>
