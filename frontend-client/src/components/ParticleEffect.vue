<template>
  <div class="particle-container">
    <div 
      v-for="particle in particles" 
      :key="particle.id"
      class="particle"
      :style="particle.style"
    >
      {{ particle.emoji }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'christmas' // christmas 或 spring-festival
  }
})

const particles = ref([])
let intervalId = null

const createParticle = () => {
  const emoji = props.type === 'christmas' 
    ? ['🎁', '🎄', '⛄', '🎅', '🔔'][Math.floor(Math.random() * 5)]
    : ['🧧', '🏮', '🎆', '🧨', '🐉'][Math.floor(Math.random() * 5)]
  
  const id = Date.now() + Math.random()
  const left = Math.random() * 100
  const duration = 5 + Math.random() * 5
  const size = 20 + Math.random() * 20
  const delay = Math.random() * 2
  
  return {
    id,
    emoji,
    style: {
      left: `${left}%`,
      fontSize: `${size}px`,
      animationDuration: `${duration}s`,
      animationDelay: `${delay}s`
    }
  }
}

const startParticles = () => {
  // 初始创建一些粒子
  for (let i = 0; i < 15; i++) {
    particles.value.push(createParticle())
  }
  
  // 定期添加新粒子
  intervalId = setInterval(() => {
    if (particles.value.length < 30) {
      particles.value.push(createParticle())
    }
    
    // 移除旧粒子
    if (particles.value.length > 30) {
      particles.value.shift()
    }
  }, 1000)
}

const stopParticles = () => {
  if (intervalId) {
    clearInterval(intervalId)
    intervalId = null
  }
  particles.value = []
}

onMounted(() => {
  startParticles()
})

onUnmounted(() => {
  stopParticles()
})

watch(() => props.type, () => {
  stopParticles()
  startParticles()
})
</script>

<style scoped>
.particle-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9998;
  overflow: hidden;
}

.particle {
  position: absolute;
  top: -50px;
  animation: fall linear infinite;
  opacity: 0.8;
}

@keyframes fall {
  0% {
    transform: translateY(-50px) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
    opacity: 0;
  }
}
</style>
