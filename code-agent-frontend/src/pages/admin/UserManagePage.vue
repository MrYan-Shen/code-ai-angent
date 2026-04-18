<template>
  <div id="userManagePage">
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    
    <div style="margin-top: 16px;">
      <a-button type="primary" @click="openAddModal">新增用户</a-button>
    </div>
    
    <a-divider />
    
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="60" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="openEditModal(record)">修改</a-button>
            <a-button danger size="small" @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      :confirmLoading="submitLoading"
    >
      <a-form :model="formData" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="账号" name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formData.userAccount" placeholder="请输入账号" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="formData.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像" name="userAvatar">
          <a-input v-model:value="formData.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介" name="userProfile">
          <a-textarea v-model:value="formData.userProfile" placeholder="请输入简介" />
        </a-form-item>
        <a-form-item label="角色" name="userRole">
          <a-select v-model:value="formData.userRole" placeholder="请选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage, addUser, updateUser } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  { title: 'id', dataIndex: 'id' },
  { title: '账号', dataIndex: 'userAccount' },
  { title: '用户名', dataIndex: 'userName' },
  { title: '头像', dataIndex: 'userAvatar' },
  { title: '简介', dataIndex: 'userProfile' },
  { title: '用户角色', dataIndex: 'userRole' },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action' },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// === 弹窗相关状态 ===
const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const isEdit = ref(false)
const submitLoading = ref(false)
const formData = reactive({
  id: undefined as string | undefined,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// === 新增/修改 方法 ===
const openAddModal = () => {
  modalTitle.value = '新增用户'
  isEdit.value = false
  // 清空表单数据
  Object.assign(formData, {
    id: undefined,
    userAccount: '',
    userName: '',
    userAvatar: '',
    userProfile: '',
    userRole: 'user',
  })
  modalVisible.value = true
}

const openEditModal = (record: any) => {
  modalTitle.value = '修改用户'
  isEdit.value = true
  // 数据回显
  Object.assign(formData, {
    id: record.id,
    userAccount: record.userAccount,
    userName: record.userName,
    userAvatar: record.userAvatar,
    userProfile: record.userProfile,
    userRole: record.userRole,
  })
  modalVisible.value = true
}

const handleModalOk = async () => {
  if (!formData.userAccount) {
    message.warning('请输入账号')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      // 执行修改
      const res = await updateUser(formData as API.UserUpdateRequest)
      if (res.data.code === 0) {
        message.success('修改成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error('修改失败：' + res.data.message)
      }
    } else {
      // 执行新增
      const res = await addUser(formData as API.UserAddRequest)
      if (res.data.code === 0) {
        message.success('新增成功，初始密码为：12345678')
        modalVisible.value = false
        fetchData()
      } else {
        message.error('新增失败：' + res.data.message)
      }
    }
  } finally {
    submitLoading.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>