<template>
  <div class="box">
    <div class="login">
      <div class="title">
        校园二手交易平台
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" size="large" class="loginForm">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <div class="button">
            <el-button type="primary" @click="onSubmit">登录</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-link underline="never" @click="onForget">忘记密码</el-link>
          <span>|</span>
          <el-link underline="never" @click="onRegister">注册</el-link>
        </el-form-item>
      </el-form>
      <div class="cotops" v-show="showModel">
        <component :is="component" @close="showModel = false"></component>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive,ref} from 'vue'
import { ElMessage } from 'element-plus'
import Register from '@/components/Register.vue'
import server from '@/utils/request'
import {useRouter} from 'vue-router'

const router = useRouter()
const loginForm = reactive({
  username: '',
  password: ''
})
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}
const showModel = ref(false)
const component = ref()
const onSubmit = async () => {
  try{
    const res = await server.post('/user/login',loginForm)
    if(res.data.code === 200){
      ElMessage.success('登录成功!')
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.data.user))
      router.push('/')
    }
    else{
      ElMessage.error('登录失败!')
    }
  }catch(error){
    ElMessage.error(error.response.data.message)
  }
}
const onForget = () => {
  console.log('忘记密码!')
}
const onRegister = () => {
  console.log('注册!')
  showModel.value = true
  component.value = Register
}
</script>

<style scoped>
.box {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.login {
  width: 400px;
  height: 300px;
  padding: 20px 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
.title {
  font-family: 宋体;
  font-size: 30px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 40px;
}
.login .loginForm {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login :deep(.el-form-item__content) {
  gap: 20px;
}
.login .button > :deep(.el-button),
.login :deep(.el-input)
{
  width: 300px;
}
</style>