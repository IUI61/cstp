<script setup>
import {reactive,ref} from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import server from '@/utils/request'

const imageUrl = ref('')
const emit = defineEmits(['close'])
const registerForm = reactive({
  username: '',
  password: '',
  phone: '',
  name: '',
  birthday: '',
  gender: '',
  avatar: ''
})
const registerFormRef = ref(null)
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { min: 11, max: 11, message: '长度为11个字符', trigger: 'blur' }
  ]
}
const handleAvatarChange = (uploadFile) => {
  imageUrl.value = URL.createObjectURL(uploadFile.raw)
  registerForm.avatar = uploadFile.raw
}
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像图片必须为JPG/PNG格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片大小不能超过2MB!')
    return false
  }
  return true
}
const onRegister = async () => {
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    ElMessage.error('请填写正确的表单信息！')
    return
  }
  const formData = new FormData()
  formData.append('username', registerForm.username)
  formData.append('password', registerForm.password)
  formData.append('phone', registerForm.phone)
  formData.append('name', registerForm.name)
  formData.append('birthday', registerForm.birthday)
  formData.append('gender', registerForm.gender)
  formData.append('avatarFile', registerForm.avatar)
  try {
    const res = await server.post('/user/register', formData,{
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.data.code === 200) {
      ElMessage.success(res.data.message)
      emit('close')
    }
    else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error(error.response.data.message)
  }
}
/*清空表单*/
const clearForm = () => {
  registerForm.username = ''
  registerForm.password = ''
  registerForm.phone = ''
  registerForm.name = ''
  registerForm.birthday = ''
  registerForm.gender = ''
  registerForm.avatar = ''
  imageUrl.value = ''
  // 清校验状态
  registerFormRef.value?.resetFields()
}
</script>

<template>
<div class="box">
  <div class="register">
    <div class="title">注册</div>
    <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-width="auto" size="large">
      <el-form-item label="用户名:" prop="username">
        <el-input clearable v-model="registerForm.username"></el-input>
      </el-form-item>
      <el-form-item label="密码:" prop="password">
        <el-input clearable v-model="registerForm.password"></el-input>
      </el-form-item>
      <el-form-item label="手机号码:" prop="phone">
        <el-input clearable v-model="registerForm.phone"></el-input>
      </el-form-item>
      <el-form-item label="姓名:">
        <el-input clearable v-model="registerForm.name"></el-input>
      </el-form-item>
      <el-form-item label="生日:">
        <el-col :span="11">
          <el-date-picker
              v-model="registerForm.birthday"
              type="date"
              placeholder="生日"
              value-format="YYYY-MM-DD"
              style="width: 300px"
          />
        </el-col>
      </el-form-item>
      <el-form-item label="性别:">
        <el-radio-group v-model="registerForm.gender">
          <el-radio  value="1">男</el-radio>
          <el-radio  value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="头像:">
        <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
            :before-upload="beforeAvatarUpload"
        >
          <img v-if="imageUrl" :src="imageUrl" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item class="button">
        <el-button type="info" @click="clearForm(); emit('close')">取消</el-button>
        <el-button type="primary" @click="onRegister">注册</el-button>
      </el-form-item>
    </el-form>
  </div>
</div>
</template>

<style scoped>
.box {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1001;
  display: flex;
  justify-content: center;
  align-items: center;
}
.register {
  width: 400px;
  height: 580px;
  padding: 20px 10px;
  background: #fff;
  border-radius: 10px;
}
/*标题*/
.title {
  font-family: 宋体;
  font-size: 30px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 40px;
}
.register :deep(.el-radio-group) {
  width: 300px;
}
/*生日*/

/*头像*/
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 50px;
  height: 50px;
  background: #f6f2f2;
}
.button :deep(.el-form-item__content) {
  justify-content: center;
}
.avatar {
  width: 50px;
  height: 50px;
}
</style>