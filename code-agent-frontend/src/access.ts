import { useLoginUserStore } from '@/stores/loginUser'
import { message } from 'ant-design-vue'
import router from '@/router'

// 是否为首次获取登录用户
let firstFetchLoginUser = true

/**
 * 全局权限校验
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  
  // 确保页面刷新，首次加载时，能够等后端返回用户信息后再校验权限
  if (firstFetchLoginUser) {
    try {
      await loginUserStore.fetchLoginUser()
    } catch (error) {
      // 捕获异常：如果是未登录状态，后端报错不会导致路由卡死
      console.log('获取登录用户信息失败或用户未登录');
    }
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  
  const toUrl = to.fullPath
  // 如果要访问 admin 开头的页面
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      message.error('没有权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  
  // 确保无论如何最后都会调用 next() 放行
  next()
})