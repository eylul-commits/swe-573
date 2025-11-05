<template>
  <div class="flex-1 flex flex-col bg-white">
    <!-- Header -->
    <div class="border-b border-gray-200 px-6 py-4">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-gray-900 flex items-center gap-2">
            <MessageSquare class="w-6 h-6" />
            Messages
          </h1>
          <p class="text-sm text-gray-600 mt-1">
            Chat with community members you've connected with
          </p>
        </div>
      </div>
    </div>

    <div class="flex-1 flex overflow-hidden">
      <!-- Contacts List -->
      <div :class="[selectedContact ? 'hidden md:block' : 'block', 'w-full md:w-80 border-r border-gray-200 flex flex-col']">
        <div class="px-4 py-3 border-b border-gray-200">
          <h3 class="text-sm text-gray-700">Connections</h3>
        </div>
        
        <ScrollArea class="flex-1">
          <div class="divide-y divide-gray-100">
            <button
              v-for="contact in contacts"
              :key="contact.id"
              @click="selectContact(contact)"
              :class="[
                'w-full px-4 py-3 hover:bg-gray-50 transition-colors text-left',
                selectedContact?.id === contact.id ? 'bg-amber-50' : ''
              ]"
            >
              <div class="flex items-start gap-3">
                <div class="relative">
                  <Avatar class="w-12 h-12">
                    <AvatarImage :src="contact.avatar" :alt="contact.name" />
                  </Avatar>
                  <div v-if="contact.unread > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-emerald-500 rounded-full flex items-center justify-center text-white text-xs">
                    {{ contact.unread }}
                  </div>
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-sm text-gray-900">{{ contact.name }}</span>
                    <span class="text-xs text-gray-500">{{ contact.lastMessageTime }}</span>
                  </div>
                  
                  <p class="text-xs text-gray-600 truncate mb-2">{{ contact.lastMessage }}</p>
                  
                  <div class="flex items-center gap-2 text-xs">
                    <Handshake class="w-3 h-3 text-gray-400" />
                    <span class="text-gray-500">{{ contact.handshakeDate }}</span>
                  </div>
                </div>
              </div>
            </button>
          </div>
        </ScrollArea>
      </div>

      <!-- Chat Area -->
      <div v-if="selectedContact" class="flex-1 flex flex-col">
        <!-- Chat Header -->
        <div class="px-6 py-4 border-b border-gray-200 flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            @click="selectedContact = null"
            class="md:hidden"
          >
            <ArrowLeft class="w-5 h-5" />
          </Button>
          
          <Avatar class="w-10 h-10">
            <AvatarImage :src="selectedContact.avatar" :alt="selectedContact.name" />
          </Avatar>
          
          <div class="flex-1">
            <h3 class="text-sm text-gray-900">{{ selectedContact.name }}</h3>
            <div class="flex items-center gap-3 text-xs text-gray-500">
              <span class="text-emerald-600">{{ selectedContact.hoursGiven }}h given</span>
              <span>•</span>
              <span class="text-blue-600">{{ selectedContact.hoursReceived }}h received</span>
            </div>
          </div>
        </div>

        <!-- Messages -->
        <ScrollArea class="flex-1 px-6 py-4">
          <div class="space-y-4">
            <template v-for="message in currentMessages" :key="message.id">
              <!-- System messages -->
              <div v-if="message.senderId === 'system'" class="flex justify-center my-4">
                <div class="bg-emerald-50 border border-emerald-200 rounded-lg px-4 py-2 flex items-center gap-2">
                  <Handshake class="w-4 h-4 text-emerald-600" />
                  <span class="text-sm text-emerald-700">{{ message.text }}</span>
                  <span class="text-xs text-emerald-500">• {{ message.timestamp }}</span>
                </div>
              </div>
              
              <!-- Regular messages -->
              <div
                v-else
                :class="['flex', message.senderId === 'me' ? 'justify-end' : 'justify-start']"
              >
                <div
                  :class="[
                    'max-w-[70%] rounded-lg px-4 py-2',
                    message.senderId === 'me'
                      ? 'bg-amber-500 text-white'
                      : 'bg-gray-100 text-gray-900'
                  ]"
                >
                  <p class="text-sm">{{ message.text }}</p>
                  <span :class="[
                    'text-xs mt-1 block',
                    message.senderId === 'me' ? 'text-amber-100' : 'text-gray-500'
                  ]">
                    {{ message.timestamp }}
                  </span>
                </div>
              </div>
            </template>
          </div>
        </ScrollArea>

        <!-- Message Input -->
        <div class="px-6 py-4 border-t border-gray-200">
          <div class="flex items-center gap-2">
            <Input
              v-model="messageText"
              @keypress.enter="handleSendMessage"
              placeholder="Type a message..."
              class="flex-1"
            />
            <Button @click="handleSendMessage" size="icon">
              <Send class="w-4 h-4" />
            </Button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="hidden md:flex flex-1 items-center justify-center bg-gray-50">
        <div class="text-center">
          <MessageSquare class="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 class="text-gray-900 mb-2">Select a conversation</h3>
          <p class="text-sm text-gray-600">
            Choose a contact to start messaging
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { MessageSquare, Send, ArrowLeft, Handshake } from 'lucide-vue-next'
import Avatar from '../ui/Avatar.vue'
import AvatarImage from '../ui/AvatarImage.vue'
import Button from '../ui/Button.vue'
import Input from '../ui/Input.vue'
import ScrollArea from '../ui/ScrollArea.vue'

interface Contact {
  id: string
  name: string
  avatar: string
  hoursGiven: number
  hoursReceived: number
  lastMessage: string
  lastMessageTime: string
  unread: number
  handshakeDate: string
}

interface Message {
  id: string
  senderId: string
  text: string
  timestamp: string
}

const contacts = ref<Contact[]>([
  {
    id: "1",
    name: "Ayşe Yılmaz",
    avatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
    hoursGiven: 47,
    hoursReceived: 42,
    lastMessage: "Thanks for the cooking lesson! I'll try the recipe this weekend.",
    lastMessageTime: "2h ago",
    unread: 2,
    handshakeDate: "March 2025"
  },
  {
    id: "2",
    name: "Deniz Yıldız",
    avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
    hoursGiven: 8,
    hoursReceived: 12,
    lastMessage: "See you tomorrow at 3 PM for the tutoring session!",
    lastMessageTime: "5h ago",
    unread: 0,
    handshakeDate: "March 2025"
  }
])

const messages = ref<Record<string, Message[]>>({
  "1": [
    { id: "0", senderId: "system", text: "Handshake Completed", timestamp: "March 15" },
    { id: "1", senderId: "1", text: "Hi! I'm interested in your Turkish cooking class offer", timestamp: "10:30 AM" },
    { id: "2", senderId: "me", text: "Great! I'd love to teach you. When works for you?", timestamp: "10:35 AM" },
    { id: "3", senderId: "1", text: "How about this Saturday at 2 PM?", timestamp: "10:40 AM" },
    { id: "4", senderId: "me", text: "Perfect! I'll prepare everything. We'll make menemen and gözleme.", timestamp: "10:45 AM" },
    { id: "5", senderId: "1", text: "Thanks for the cooking lesson! I'll try the recipe this weekend.", timestamp: "4:20 PM" }
  ],
  "2": [
    { id: "0", senderId: "system", text: "Handshake Completed", timestamp: "March 18" },
    { id: "1", senderId: "2", text: "Hello, I need help with calculus homework", timestamp: "Yesterday" },
    { id: "2", senderId: "me", text: "I can help! What topics are you struggling with?", timestamp: "Yesterday" },
    { id: "3", senderId: "2", text: "Derivatives and integration mainly", timestamp: "Yesterday" },
    { id: "4", senderId: "me", text: "No problem, those are my specialty. We can meet at my place.", timestamp: "Yesterday" },
    { id: "5", senderId: "2", text: "See you tomorrow at 3 PM for the tutoring session!", timestamp: "11:00 AM" }
  ]
})

const selectedContact = ref<Contact | null>(null)
const messageText = ref("")

const currentMessages = computed(() => {
  return selectedContact.value ? (messages.value[selectedContact.value.id] || []) : []
})

const selectContact = (contact: Contact) => {
  selectedContact.value = contact
}

const handleSendMessage = () => {
  if (messageText.value.trim()) {
    // In a real app, this would send the message
    console.log("Sending message:", messageText.value)
    messageText.value = ""
  }
}
</script>

