<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-6">Admin Panel</h1>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <Card class="p-4">
          <div class="text-sm text-gray-600 mb-1">Total Users</div>
          <div class="text-2xl font-bold">{{ statistics?.totalUsers || 0 }}</div>
          <div class="text-xs text-gray-500 mt-1">
            {{ statistics?.activeUsers || 0 }} active, {{ statistics?.deactivatedUsers || 0 }} deactivated
          </div>
        </Card>
        <Card class="p-4">
          <div class="text-sm text-gray-600 mb-1">Active Offers</div>
          <div class="text-2xl font-bold">{{ statistics?.activeOffers || 0 }}</div>
          <div class="text-xs text-gray-500 mt-1">of {{ statistics?.totalOffers || 0 }} total</div>
        </Card>
        <Card class="p-4">
          <div class="text-sm text-gray-600 mb-1">Active Requests</div>
          <div class="text-2xl font-bold">{{ statistics?.activeRequests || 0 }}</div>
          <div class="text-xs text-gray-500 mt-1">of {{ statistics?.totalRequests || 0 }} total</div>
        </Card>
        <Card class="p-4">
          <div class="text-sm text-gray-600 mb-1">Open Reports</div>
          <div class="text-2xl font-bold text-red-600">{{ statistics?.openReports || 0 }}</div>
          <div class="text-xs text-gray-500 mt-1">
            {{ statistics?.inReviewReports || 0 }} in review, {{ statistics?.resolvedReports || 0 }} resolved
          </div>
        </Card>
      </div>

      <!-- Tabs -->
      <Tabs v-model="activeTab" class="mb-6">
        <TabsList>
          <TabsTrigger value="reports">Reports</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
        </TabsList>

        <!-- Reports Tab -->
        <TabsContent value="reports" class="mt-4">
          <div class="mb-4 flex gap-2">
            <Button @click="loadReports()" variant="outline">All</Button>
            <Button @click="loadReports('OPEN')" variant="outline">Open</Button>
            <Button @click="loadReports('IN_REVIEW')" variant="outline">In Review</Button>
            <Button @click="loadReports('RESOLVED')" variant="outline">Resolved</Button>
          </div>

          <div class="space-y-4">
            <Card v-for="report in reports" :key="report.id" class="p-4">
              <div class="flex justify-between items-start mb-2">
                <div>
                  <div class="font-semibold">
                    Report #{{ report.id }} - {{ report.reportType }}
                  </div>
                  <div class="text-sm text-gray-600">
                    Reported by: {{ report.reporterName }} ({{ report.reporterEmail }})
                  </div>
                  <div class="text-sm text-gray-600">
                    Reported user: {{ report.reportedUserName }} ({{ report.reportedUserEmail }})
                  </div>
                  <div v-if="report.reportedOfferTitle" class="text-sm text-gray-600">
                    Offer: {{ report.reportedOfferTitle }}
                  </div>
                  <div v-if="report.reportedRequestTitle" class="text-sm text-gray-600">
                    Request: {{ report.reportedRequestTitle }}
                  </div>
                </div>
                <Badge :variant="getStatusVariant(report.status)">
                  {{ report.status }}
                </Badge>
              </div>
              <div class="text-sm mb-2">{{ report.message }}</div>
              <div v-if="report.adminNotes" class="text-sm text-gray-600 mb-2">
                <strong>Admin Notes:</strong> {{ report.adminNotes }}
              </div>
              <div class="text-xs text-gray-500">
                Created: {{ formatDate(report.createdAt) }}
                <span v-if="report.resolvedAt">
                  | Resolved: {{ formatDate(report.resolvedAt) }} by {{ report.resolvedByName }}
                </span>
              </div>
              <div v-if="report.status !== 'RESOLVED'" class="mt-3 flex gap-2">
                <Button @click="openResolveDialog(report)" size="sm">Resolve</Button>
              </div>
            </Card>
          </div>
        </TabsContent>

        <!-- Users Tab -->
        <TabsContent value="users" class="mt-4">
          <div class="space-y-4">
            <Card v-for="user in users" :key="user.id" class="p-4">
              <div class="flex justify-between items-start">
                <div>
                  <div class="font-semibold">{{ user.name }} ({{ user.email }})</div>
                  <div class="text-sm text-gray-600">
                    Role: {{ user.role }} | Status: {{ user.accountStatus }}
                    <span v-if="user.warningCount > 0"> | Warnings: {{ user.warningCount }}</span>
                  </div>
                  <div class="text-sm text-gray-600">
                    Balance: {{ user.balanceHours }} hours
                  </div>
                </div>
                <div class="flex gap-2">
                  <Button @click="openUserManagementDialog(user)" size="sm" variant="outline">
                    Manage
                  </Button>
                </div>
              </div>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>

    <!-- Resolve Report Dialog -->
    <Dialog v-model="resolveDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Resolve Report</DialogTitle>
        </DialogHeader>
        <div class="px-6">
        <div class="space-y-4">
          <div>
            <label class="text-sm font-medium">Status</label>
            <Select v-model="resolveForm.status">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="RESOLVED">Resolved</SelectItem>
                <SelectItem value="IN_REVIEW">In Review</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <label class="text-sm font-medium">Admin Notes</label>
            <Textarea v-model="resolveForm.adminNotes" placeholder="Add notes about resolution..." />
          </div>
          <div>
            <label class="text-sm font-medium">Action on User</label>
            <Select v-model="resolveForm.action">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="NO_ACTION">No Action</SelectItem>
                <SelectItem value="WARN">Warn User</SelectItem>
                <SelectItem value="DEACTIVATE">Deactivate User</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button @click="resolveDialogOpen = false" variant="outline">Cancel</Button>
          <Button @click="handleResolveReport">Resolve</Button>
        </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>

    <!-- User Management Dialog -->
    <Dialog v-model="userManagementDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Manage User</DialogTitle>
        </DialogHeader>
        <div class="px-6">
        <div class="space-y-4">
          <div>
            <label class="text-sm font-medium">Action</label>
            <Select v-model="userManagementForm.action">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="WARN">Warn</SelectItem>
                <SelectItem value="DEACTIVATE">Deactivate</SelectItem>
                <SelectItem value="ACTIVATE">Activate</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <label class="text-sm font-medium">Reason</label>
            <Textarea v-model="userManagementForm.reason" placeholder="Reason for action..." />
          </div>
        </div>
        <DialogFooter>
          <Button @click="userManagementDialogOpen = false" variant="outline">Cancel</Button>
          <Button @click="handleManageUser">Apply</Button>
        </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Card from '../ui/Card.vue'
import Button from '../ui/Button.vue'
import Badge from '../ui/Badge.vue'
import Tabs from '../ui/Tabs.vue'
import TabsContent from '../ui/TabsContent.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import Dialog from '../ui/Dialog.vue'
import DialogContent from '../ui/DialogContent.vue'
import DialogFooter from '../ui/DialogFooter.vue'
import DialogHeader from '../ui/DialogHeader.vue'
import DialogTitle from '../ui/DialogTitle.vue'
import Select from '../ui/Select.vue'
import SelectContent from '../ui/SelectContent.vue'
import SelectItem from '../ui/SelectItem.vue'
import SelectTrigger from '../ui/SelectTrigger.vue'
import SelectValue from '../ui/SelectValue.vue'
import Textarea from '../ui/Textarea.vue'
import { getAdminStatistics, getAllUsers, getAllReports, resolveReport as resolveReportApi, manageUser, type ResolveReportRequest, type UserManagementRequest } from '../../services/adminService'
import type { AdminStatistics, User } from '../../services/adminService'
import type { Report } from '../../services/reportService'

const statistics = ref<AdminStatistics | null>(null)
const reports = ref<Report[]>([])
const users = ref<User[]>([])
const activeTab = ref('reports')
const resolveDialogOpen = ref(false)
const userManagementDialogOpen = ref(false)
const selectedReport = ref<Report | null>(null)
const selectedUser = ref<User | null>(null)

const resolveForm = ref<ResolveReportRequest>({
  status: 'RESOLVED',
  adminNotes: '',
  action: 'NO_ACTION'
})

const userManagementForm = ref<UserManagementRequest>({
  userId: 0,
  action: 'WARN',
  reason: ''
})

onMounted(async () => {
  await loadStatistics()
  await loadReports()
  await loadUsers()
})

async function loadStatistics() {
  try {
    statistics.value = await getAdminStatistics()
  } catch (error) {
    console.error('Failed to load statistics:', error)
  }
}

async function loadReports(status?: string) {
  try {
    reports.value = await getAllReports(status as any)
  } catch (error) {
    console.error('Failed to load reports:', error)
  }
}

async function loadUsers() {
  try {
    users.value = await getAllUsers()
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

function openResolveDialog(report: Report) {
  selectedReport.value = report
  resolveForm.value = {
    status: 'RESOLVED',
    adminNotes: '',
    userId: report.reportedUserId,
    action: 'NO_ACTION'
  }
  resolveDialogOpen.value = true
}

async function handleResolveReport() {
  if (!selectedReport.value) return
  try {
    await resolveReportApi(selectedReport.value.id, resolveForm.value)
    resolveDialogOpen.value = false
    await loadReports()
    await loadStatistics()
  } catch (error) {
    console.error('Failed to resolve report:', error)
    alert('Failed to resolve report')
  }
}

function openUserManagementDialog(user: User) {
  selectedUser.value = user
  userManagementForm.value = {
    userId: user.id,
    action: 'WARN',
    reason: ''
  }
  userManagementDialogOpen.value = true
}

async function handleManageUser() {
  if (!selectedUser.value) return
  try {
    await manageUser(userManagementForm.value)
    userManagementDialogOpen.value = false
    await loadUsers()
    await loadStatistics()
  } catch (error) {
    console.error('Failed to manage user:', error)
    alert('Failed to manage user')
  }
}

function getStatusVariant(status: string) {
  switch (status) {
    case 'OPEN': return 'destructive'
    case 'IN_REVIEW': return 'default'
    case 'RESOLVED': return 'secondary'
    default: return 'default'
  }
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString()
}
</script>

