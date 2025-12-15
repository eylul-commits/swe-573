<template>
  <div class="badge-display">
    <div v-if="badge" class="badge-item" :title="badge.description">
      <span class="badge-icon">{{ badge.iconUrl }}</span>
      <span class="badge-name">{{ badge.name }}</span>
    </div>
    <div v-else class="no-badge">
      <span class="badge-icon">🌱</span>
      <span class="badge-name">Newcomer</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Badge } from '../types'

interface Props {
  badge?: Badge | null
}

defineProps<Props>()
</script>

<style scoped>
.badge-display {
  display: inline-flex;
  align-items: center;
}

.badge-item,
.no-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  font-size: 0.875rem;
}

.badge-icon {
  font-size: 1rem;
}

.badge-name {
  font-weight: 500;
  color: #333;
}

.dark .badge-name {
  color: #fff;
}
</style>

