<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-amber-50 via-yellow-50 to-orange-50 px-4">
    <Card class="w-full max-w-md">
      <div class="p-8">
        <!-- Logo/Header -->
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-amber-500 rounded-full mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <h1 class="text-3xl font-bold text-gray-900 mb-2">The Hive</h1>
          <p class="text-gray-600">Time banking community platform</p>
        </div>

        <!-- Toggle between Login/Register -->
        <div class="flex gap-2 mb-6 bg-gray-100 p-1 rounded-lg">
          <button
            @click="isLogin = true"
            :class="[
              'flex-1 py-2 px-4 rounded-md font-medium transition-all',
              isLogin 
                ? 'bg-white text-gray-900 shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            Login
          </button>
          <button
            @click="isLogin = false"
            :class="[
              'flex-1 py-2 px-4 rounded-md font-medium transition-all',
              !isLogin 
                ? 'bg-white text-gray-900 shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            Register
          </button>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          {{ errorMessage }}
        </div>

        <!-- Login Form -->
        <form v-if="isLogin" @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <Input
              id="email"
              v-model="loginForm.email"
              type="email"
              placeholder="your@email.com"
              required
            />
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <Input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="••••••••"
              required
            />
          </div>
          <Button type="submit" class="w-full" :disabled="loading">
            <span v-if="loading">Logging in...</span>
            <span v-else>Login</span>
          </Button>
        </form>

        <!-- Register Form -->
        <form v-else @submit.prevent="handleRegister" class="space-y-4">
          <div>
            <label for="register-name" class="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <Input
              id="register-name"
              v-model="registerForm.name"
              type="text"
              placeholder="Your Name"
              required
            />
          </div>
          <div>
            <label for="register-email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <Input
              id="register-email"
              v-model="registerForm.email"
              type="email"
              placeholder="your@email.com"
              required
            />
          </div>
          <div>
            <label for="register-password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <Input
              id="register-password"
              v-model="registerForm.password"
              type="password"
              placeholder="••••••••"
              required
            />
          </div>
          <Button type="submit" class="w-full" :disabled="loading">
            <span v-if="loading">Creating account...</span>
            <span v-else>Create Account</span>
          </Button>
        </form>
      </div>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '../../stores/appStore'
import Card from '../ui/Card.vue'
import Input from '../ui/Input.vue'
import Button from '../ui/Button.vue'
import { login, register } from '../../services/authService'

const appStore = useAppStore()
const isLogin = ref(true)
const loading = ref(false)
const errorMessage = ref('')

const loginForm = ref({
  email: '',
  password: ''
})

const registerForm = ref({
  name: '',
  email: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''
  
  try {
    const response = await login(loginForm.value)
    appStore.setCurrentUser(response.user, response.token)
  } catch (error: any) {
    errorMessage.value = error.message || 'Login failed. Please try again.'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''
  
  try {
    const response = await register(registerForm.value)
    appStore.setCurrentUser(response.user, response.token)
  } catch (error: any) {
    errorMessage.value = error.message || 'Registration failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

