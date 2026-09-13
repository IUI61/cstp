<template>
  <div class="web">
    <div class="nav">
      <div class="title">校园二手交易平台</div>
      <div class="nav_avatar">
        <el-dropdown v-if="isLoggedIn" @command="handleCommand">
          <span class="el-dropdown-link">
            <el-avatar :src="userAvatar" :size="50"/>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登入</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-avatar v-else :size="50" @click="login">登入</el-avatar >
      </div>
    </div>
    <router-view class="router-view"></router-view>
  </div>
  <!--功能框-->
  <div class="home_function">
    <el-tooltip content="发布闲置" placement="left" effect="dark">
      <div class="function_btn function_btn-primary" @click="issuanceGoods">
        <el-icon :size="18"><Plus /></el-icon>
      </div>
    </el-tooltip>
    <!--我的消息-->
    <el-tooltip content="我的消息" placement="left" effect="dark">
      <div class="function_btn function_btn-light" @click="myMessages">
        <el-icon :size="16"><ChatDotRound /></el-icon>
      </div>
    </el-tooltip>
    <!--我的订单-->
    <el-tooltip content="我的订单" placement="left" effect="dark">
      <div class="function_btn function_btn-light" @click="myOrders">
        <el-icon :size="16"><List /></el-icon>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup>
import {computed ,ref} from 'vue'
import {useRouter} from 'vue-router'
import { Plus, ChatDotRound ,List } from '@element-plus/icons-vue'

const isLoggedIn = ref(!!localStorage.getItem('token'))
const router = useRouter()

router.afterEach(() => {
  isLoggedIn.value = !!localStorage.getItem('token')
})

const userAvatar = computed(() => {
  if (!isLoggedIn.value) return ''
  try {
    return JSON.parse(localStorage.getItem('user'))?.avatar || ''
  } catch {
    return ''
  }
})
const login = () => {
  router.push('/login')
}

const handleCommand = (command) => {
  if(command === 'logout'){
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    isLoggedIn.value = false
    router.push('/')
  }
}
/*发布商品*/
const issuanceGoods = () => {
  router.push('/issuanceGoods')
}

const myMessages = () => {
  router.push('/chat')
}
const myOrders = () => {
  router.push('/orders')
}
</script>

<style scoped>
.web {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #EEEDF0;
}
.nav {
  padding: 20px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #4f9ccc, #8fd3f4);
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.1);
}
.nav .title{
  font-family: 楷体;
  color: #ffffff;
  font-size: 40px;
}
.nav_avatar{
  margin-right: 40px;
  cursor: pointer;
}
.router-view {
  flex: 1;
}
/*功能框*/
.home_function{
  position: fixed;
  top: 50%;
  right: 14px;
  transform: translateY(-50%);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 8px 6px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  box-shadow: 0 6px 18px rgba(79, 156, 204, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.function_btn{
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
/*发布闲置：与导航栏呼应的蓝色渐变主按钮*/
.function_btn-primary{
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  box-shadow: 0 3px 8px rgba(79, 156, 204, 0.4);
}
.function_btn-primary:hover{
  transform: translateY(-3px) scale(1.06);
  box-shadow: 0 6px 14px rgba(79, 156, 204, 0.5);
}
.function_btn-primary:active{
  transform: scale(0.94);
}
/*我的消息：白底蓝边次要按钮*/
.function_btn-light{
  background: #ffffff;
  color: #4f9ccc;
  border: 1px solid rgba(79, 156, 204, 0.4);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
}
.function_btn-light:hover{
  transform: translateY(-3px) scale(1.06);
  border-color: #4f9ccc;
  box-shadow: 0 5px 12px rgba(79, 156, 204, 0.28);
}
.function_btn-light:active{
  transform: scale(0.94);
}
</style>