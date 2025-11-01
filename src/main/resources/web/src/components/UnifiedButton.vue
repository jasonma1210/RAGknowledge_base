<template>
  <el-button
    :type="type"
    :size="size"
    :loading="loading"
    :disabled="disabled"
    :icon="icon"
    :round="round"
    :circle="circle"
    :plain="plain"
    :text="text"
    :link="link"
    :bg="bg"
    :autofocus="autofocus"
    :native-type="nativeType"
    :block="block"
    :class="buttonClass"
    @click="handleClick"
  >
    <slot></slot>
  </el-button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text' | ''
  size?: 'large' | 'default' | 'small'
  loading?: boolean
  disabled?: boolean
  icon?: string
  round?: boolean
  circle?: boolean
  plain?: boolean
  text?: boolean
  link?: boolean
  bg?: boolean
  autofocus?: boolean
  nativeType?: 'button' | 'submit' | 'reset'
  block?: boolean
  variant?: 'default' | 'outlined' | 'text' | 'elevated'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'default',
  loading: false,
  disabled: false,
  round: false,
  circle: false,
  plain: false,
  text: false,
  link: false,
  bg: false,
  autofocus: false,
  nativeType: 'button',
  block: false,
  variant: 'default'
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => {
  return [
    'unified-button',
    `unified-button--${props.variant}`,
    `unified-button--${props.size}`,
    {
      'unified-button--loading': props.loading,
      'unified-button--disabled': props.disabled,
      'unified-button--round': props.round,
      'unified-button--circle': props.circle,
      'unified-button--block': props.block
    }
  ]
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.unified-button {
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
}

.unified-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.unified-button:hover::before {
  left: 100%;
}

.unified-button--default {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

.unified-button--default:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.unified-button--outlined {
  background: transparent;
  border: 2px solid #667eea;
  color: #667eea;
  box-shadow: none;
}

.unified-button--outlined:hover {
  background: #667eea;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.unified-button--text {
  background: transparent;
  border: none;
  color: #667eea;
  box-shadow: none;
  padding-left: 8px;
  padding-right: 8px;
}

.unified-button--text:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.unified-button--elevated {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.unified-button--elevated:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* 尺寸变体 */
.unified-button--large {
  height: 40px;
  padding: 0 24px;
  font-size: 16px;
}

.unified-button--small {
  height: 28px;
  padding: 0 12px;
  font-size: 12px;
}

/* 状态样式 */
.unified-button--loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.unified-button--disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.unified-button--round {
  border-radius: 20px;
}

.unified-button--circle {
  border-radius: 50%;
  padding: 0;
}

.unified-button--block {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 类型覆盖 */
.unified-button[type="success"] {
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  border-color: #67c23a;
}

.unified-button[type="success"]:hover {
  background: linear-gradient(135deg, #5fb11c 0%, #4a8a26 100%);
}

.unified-button[type="warning"] {
  background: linear-gradient(135deg, #e6a23c 0%, #cf9236 100%);
  border-color: #e6a23c;
}

.unified-button[type="warning"]:hover {
  background: linear-gradient(135deg, #d39433 0%, #c2832d 100%);
}

.unified-button[type="danger"] {
  background: linear-gradient(135deg, #f56c6c 0%, #dd6161 100%);
  border-color: #f56c6c;
}

.unified-button[type="danger"]:hover {
  background: linear-gradient(135deg, #f45656 0%, #d84a4a 100%);
}

.unified-button[type="info"] {
  background: linear-gradient(135deg, #909399 0%, #7d8086 100%);
  border-color: #909399;
}

.unified-button[type="info"]:hover {
  background: linear-gradient(135deg, #83868c 0%, #6f7278 100%);
}
</style>