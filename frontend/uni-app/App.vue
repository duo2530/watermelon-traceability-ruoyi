<script setup>
  import config from './config'
  import { getToken } from '@/utils/auth'
  import { useConfigStore } from '@/store'
  import { getCurrentInstance } from "vue"
  import { onLaunch, onShow } from '@dcloudio/uni-app'
  import { installAutoSync, flush, pendingCount } from '@/utils/offlineQueue'

  const { proxy } = getCurrentInstance()

  onLaunch(() => {
    initApp()
    installAutoSync()
  })

  // 每次回到前台尝试同步离线录入
  onShow(() => {
    if (getToken() && pendingCount() > 0) {
      flush()
    }
  })

  // 初始化应用
  function initApp() {
    // 初始化应用配置
    initConfig()
    // 检查用户登录状态
    //#ifdef H5
    checkLogin()
    //#endif
  }

  function initConfig() {
    useConfigStore().setConfig(config)
  }

  function checkLogin() {
    // 消费者扫码页无需登录
    const hash = window.location.hash || ''
    if (hash.indexOf('/pages/consumer/') !== -1) {
      return
    }
    if (!getToken()) {
      proxy.$tab.reLaunch('/pages/login') 
    }
  }
</script>

<style lang="scss">
  @import '@/static/scss/index.scss'
</style>
