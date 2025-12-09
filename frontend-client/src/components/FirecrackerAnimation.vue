<template>
  <div>
    <!-- 左侧男孩放鞭炮 -->
    <div class="firecracker-boy">
      <div class="character">
        <div class="chinese-boy">
          <div class="face">😊</div>
          <div class="hat">🧧</div>
        </div>
        <div class="firecracker">🧨</div>
      </div>
    </div>
    
    <!-- 右侧女孩放鞭炮 -->
    <div class="firecracker-girl">
      <div class="character">
        <div class="chinese-girl">
          <div class="face">😊</div>
          <div class="hat">🎀</div>
        </div>
        <div class="firecracker">🧨</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.firecracker-boy,
.firecracker-girl {
  position: fixed;
  bottom: 50%;
  transform: translateY(50%);
  z-index: 998;
  animation: bounce 2s ease-in-out infinite;
  pointer-events: none;
}

.firecracker-boy {
  left: 10px;
}

.firecracker-girl {
  right: 10px;
}

.character {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.chinese-boy,
.chinese-girl {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: wave 1.5s ease-in-out infinite;
}

.face {
  font-size: 50px;
  filter: drop-shadow(0 2px 4px rgba(255, 0, 0, 0.3));
}

.hat {
  position: absolute;
  top: -15px;
  font-size: 25px;
  animation: float 2s ease-in-out infinite;
}

.firecracker {
  font-size: 28px;
  animation: spark 0.6s ease-in-out infinite;
  filter: drop-shadow(0 2px 4px rgba(255, 0, 0, 0.5));
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(50%);
  }
  50% {
    transform: translateY(calc(50% - 8px));
  }
}

@keyframes wave {
  0%, 100% {
    transform: rotate(-3deg);
  }
  50% {
    transform: rotate(3deg);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes spark {
  0%, 100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: scale(1.15) rotate(10deg);
    opacity: 0.9;
  }
}

/* 响应式 - 在小屏幕上隐藏 */
@media (max-width: 768px) {
  .firecracker-boy,
  .firecracker-girl {
    display: none;
  }
}

/* 在中等屏幕上缩小 */
@media (max-width: 1024px) {
  .face {
    font-size: 40px;
  }
  
  .hat {
    font-size: 20px;
    top: -12px;
  }
  
  .firecracker {
    font-size: 22px;
  }
}
</style>
