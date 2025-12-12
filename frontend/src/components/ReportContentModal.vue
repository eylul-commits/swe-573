<template>
  <Dialog v-model="isOpen">
    <DialogContent class="max-w-2xl">
      <DialogHeader>
        <DialogTitle>Report Inappropriate Content</DialogTitle>
        <DialogDescription>
          Help us maintain a safe community by reporting content or behavior that violates our guidelines.
        </DialogDescription>
      </DialogHeader>

      <div class="px-6">
        <div class="space-y-4">
        <div>
          <label class="text-sm font-medium mb-2 block">What are you reporting?</label>
          <Select v-model="form.reportType">
            <SelectTrigger>
              <SelectValue placeholder="Select report type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="USER">User Behavior</SelectItem>
              <SelectItem value="OFFER">Offer</SelectItem>
              <SelectItem value="REQUEST">Request</SelectItem>
              <SelectItem value="FORUM_POST">Forum Post</SelectItem>
              <SelectItem value="FORUM_TOPIC">Forum Topic</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div>
          <label class="text-sm font-medium mb-2 block">Reported User *</label>
          <Input
            v-model="reportedUserName"
            type="text"
            placeholder="User name or email"
            disabled
          />
          <input v-model="form.reportedUserId" type="hidden" />
        </div>

        <div v-if="form.reportType === 'OFFER'">
          <label class="text-sm font-medium mb-2 block">Offer ID</label>
          <Input
            v-model.number="form.reportedOfferId"
            type="number"
            placeholder="Offer ID"
          />
        </div>

        <div v-if="form.reportType === 'REQUEST'">
          <label class="text-sm font-medium mb-2 block">Request ID</label>
          <Input
            v-model.number="form.reportedRequestId"
            type="number"
            placeholder="Request ID"
          />
        </div>

        <div v-if="form.reportType === 'FORUM_POST'">
          <label class="text-sm font-medium mb-2 block">Forum Post ID</label>
          <Input
            v-model.number="form.reportedForumPostId"
            type="number"
            placeholder="Forum Post ID"
            disabled
          />
        </div>

        <div v-if="form.reportType === 'FORUM_TOPIC'">
          <label class="text-sm font-medium mb-2 block">Forum Topic ID</label>
          <Input
            v-model.number="form.reportedForumTopicId"
            type="number"
            placeholder="Forum Topic ID"
            disabled
          />
        </div>

        <div>
          <label class="text-sm font-medium mb-2 block">Reason for Report *</label>
          <Textarea
            v-model="form.message"
            placeholder="Please describe why you are reporting this content or behavior..."
            rows="5"
            required
          />
        </div>
        </div>
      </div>

      <DialogFooter>
        <Button @click="close" variant="outline">Cancel</Button>
        <Button @click="submitReport" :disabled="!canSubmit">Submit Report</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Dialog from './ui/Dialog.vue'
import DialogContent from './ui/DialogContent.vue'
import DialogDescription from './ui/DialogDescription.vue'
import DialogFooter from './ui/DialogFooter.vue'
import DialogHeader from './ui/DialogHeader.vue'
import DialogTitle from './ui/DialogTitle.vue'
import Button from './ui/Button.vue'
import Input from './ui/Input.vue'
import Textarea from './ui/Textarea.vue'
import Select from './ui/Select.vue'
import SelectContent from './ui/SelectContent.vue'
import SelectItem from './ui/SelectItem.vue'
import SelectTrigger from './ui/SelectTrigger.vue'
import SelectValue from './ui/SelectValue.vue'
import { createReport, type CreateReportRequest, type ReportType } from '../services/reportService'

const props = withDefaults(defineProps<{
  modelValue?: boolean
  reportedUserId: number
  reportedUserName: string
  reportedOfferId?: number
  reportedRequestId?: number
  reportedForumPostId?: number
  reportedForumTopicId?: number
  reportType?: ReportType
}>(), {
  modelValue: false
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'submitted': [report: any]
}>()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const reportedUserName = ref(props.reportedUserName)

const form = ref<CreateReportRequest>({
  reportType: props.reportType || 'USER',
  reportedUserId: props.reportedUserId,
  message: '',
  ...(props.reportedOfferId && { reportedOfferId: props.reportedOfferId }),
  ...(props.reportedRequestId && { reportedRequestId: props.reportedRequestId }),
  ...(props.reportedForumPostId && { reportedForumPostId: props.reportedForumPostId }),
  ...(props.reportedForumTopicId && { reportedForumTopicId: props.reportedForumTopicId })
})

watch(() => props.reportedUserId, (newId) => {
  form.value.reportedUserId = newId
})

watch(() => props.reportType, (newType) => {
  if (newType) {
    form.value.reportType = newType
  }
})

watch(() => props.reportedOfferId, (newId) => {
  if (newId) {
    form.value.reportedOfferId = newId
  }
})

watch(() => props.reportedRequestId, (newId) => {
  if (newId) {
    form.value.reportedRequestId = newId
  }
})

watch(() => props.reportedForumPostId, (newId) => {
  if (newId) {
    form.value.reportedForumPostId = newId
  } else {
    delete form.value.reportedForumPostId
  }
})

watch(() => props.reportedForumTopicId, (newId) => {
  if (newId) {
    form.value.reportedForumTopicId = newId
  } else {
    delete form.value.reportedForumTopicId
  }
})

const canSubmit = computed(() => {
  return form.value.message.trim().length > 0 && form.value.reportedUserId > 0
})

function close() {
  isOpen.value = false
  form.value = {
    reportType: props.reportType || 'USER',
    reportedUserId: props.reportedUserId,
    message: '',
    ...(props.reportedOfferId && { reportedOfferId: props.reportedOfferId }),
    ...(props.reportedRequestId && { reportedRequestId: props.reportedRequestId }),
    ...(props.reportedForumPostId && { reportedForumPostId: props.reportedForumPostId }),
    ...(props.reportedForumTopicId && { reportedForumTopicId: props.reportedForumTopicId })
  }
}

async function submitReport() {
  if (!canSubmit.value) return

  try {
    const report = await createReport(form.value)
    emit('submitted', report)
    close()
    alert('Report submitted successfully. Thank you for helping keep our community safe.')
  } catch (error: any) {
    console.error('Failed to submit report:', error)
    alert(error.message || 'Failed to submit report. Please try again.')
  }
}
</script>

