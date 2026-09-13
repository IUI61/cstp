<script setup>
  import {useRoute, useRouter} from 'vue-router'
  import {ref,onMounted,onBeforeUnmount,nextTick,computed} from 'vue'
  import server from '../utils/request.js'
  import {ElMessage} from 'element-plus'
  import {ArrowLeft, ChatDotRound} from '@element-plus/icons-vue'

  const route = useRoute()
  const router = useRouter()
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  // 统一存字符串：后端 MessagesListVo 的 senderId 序列化后是字符串，直接===比较才正确
  const senderId = ref(user.id ? String(user.id) : '')
  const conversationId = ref(route.params.id ? String(route.params.id) : null)
  const content = ref('')
  const messagesList = ref([])
  const conversationsList = ref([])
  const contentRef = ref(null)

  const sendAvatar = ref(user.avatar || '')
  const sendName = user.name || ''
  const textareaRef = ref(null)

  // 头像兜底:取名字首字符,并按名字 hash 出稳定的底色
  const firstChar = (name) => {
    const n = (name || '').trim()
    return n ? n.charAt(0) : '?'
  }
  const avatarColors = ['#5b9bd5', '#6f8fd9', '#49b6a2', '#e08a5a', '#a08bd6', '#d96f8f']
  const avatarColor = (name) => {
    const n = (name || '').trim()
    if (!n) return avatarColors[0]
    let hash = 0
    for (let i = 0; i < n.length; i++) hash = (hash * 31 + n.charCodeAt(i)) % 997
    return avatarColors[hash % avatarColors.length]
  }
  // 会话时间友好显示:兼容后端 LocalDateTime 的数组/ISO字符串/时间戳三种形态
  const pad = (num) => String(num).padStart(2, '0')
  const ymd = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
  const toDate = (time) => {
    if (Array.isArray(time)) {
      // 后端 LocalDateTime 未关闭时间戳模式时输出 [年, 月, 日, 时, 分, 秒]
      const [y, m = 1, d = 1, h = 0, min = 0, s = 0] = time
      return new Date(y, m - 1, d, h, min, s)
    }
    if (typeof time === 'number') return new Date(time) // 毫秒时间戳
    return new Date(String(time).replace(' ', 'T'))
  }
  const formatTime = (time) => {
    if (time === null || time === undefined || time === '') return ''
    const date = toDate(time)
    if (isNaN(date.getTime())) {
      return Array.isArray(time) ? time.slice(0, 6).join('-') : String(time)
    }
    const hm = `${pad(date.getHours())}:${pad(date.getMinutes())}`
    const today = new Date()
    if (ymd(today) === ymd(date)) return hm
    const yesterday = new Date()
    yesterday.setDate(today.getDate() - 1)
    if (ymd(yesterday) === ymd(date)) return `昨天 ${hm}`
    if (date.getFullYear() === today.getFullYear()) return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${hm}`
    return ymd(date)
  }

  let ws = null
  let heartbeatTimer = null
  let reconnectTimer = null

  const scrollToBottom = () => {
    nextTick(() => {
      if (contentRef.value) contentRef.value.scrollTop = contentRef.value.scrollHeight
    })
  }

  // ====== WebSocket 连接 ======
  const connectWs = () => {
    const token = localStorage.getItem('token')
    if (!token) return
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
    ws = new WebSocket(`${protocol}://${location.host}/ws/chat?token=${encodeURIComponent(token)}`)
    ws.onopen = () => {
      console.log('WebSocket 已连接')
      // 心跳保活，防止空闲被代理/服务器断开
      heartbeatTimer = setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({type: 'ping'}))
        }
      }, 30000)
    }
    ws.onmessage = (event) => {
      handleReceiveMessage(JSON.parse(event.data))
    }
    ws.onclose = () => {
      clearInterval(heartbeatTimer)
      // 断线后 3 秒自动重连
      clearTimeout(reconnectTimer)
      reconnectTimer = setTimeout(connectWs, 3000)
    }
    ws.onerror = () => { if (ws) ws.close() }
  }

  // 收到服务端推送的新消息
  const handleReceiveMessage = (data) => {
    if (data.type == 'conversation') {
      conversationsList.value.unshift(data.conversation)
    }
    // 1. 更新左侧会话列表：最后一条消息置顶
    refreshConversationList(data.conversationId, data.content)
    // 2. 正好在看这个会话 → 直接追加气泡
    if (conversationId.value === data.conversationId) {
      messagesList.value.push({id: data.id, senderId: data.senderId, content: data.content})
      scrollToBottom()
    }
  }

  // 更新会话列表最后一条消息并置顶
  const refreshConversationList = (id, text) => {
    const index = conversationsList.value.findIndex(c => String(c.conversationId) === String(id))
    if (index > -1) {
      conversationsList.value[index].lastMessage = text
      const conv = conversationsList.value.splice(index, 1)[0]
      conversationsList.value.unshift(conv)
    }
  }

  // ====== 发送消息（HTTP 发送，稳定兜底） ======
  const sendMessage = async () => {
    if (!senderId.value) { ElMessage.warning('请先登录'); return }
    if (!conversationId.value) { ElMessage.warning('请先选择会话'); return }
    if (!content.value.trim()) return
    const text = content.value.trim()
    const res = await server.post('/chat/sendMessage', {
      conversationId: conversationId.value,
      content: text
    })
    if (res.data.code === 200) {
      // 服务端返回 {id, senderId, content}，发送方本地立即渲染，不依赖推送
      messagesList.value.push(res.data.data)
      content.value = ''
      textareaRef.value?.focus()
      refreshConversationList(conversationId.value, text)
      scrollToBottom()
    } else {
      ElMessage.error(res.data.message)
    }
  }

  // 输入框键盘事件:Enter 发送、Shift+Enter 换行,中文输入法选词回车不误发
  const handleInputKeydown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
      e.preventDefault()
      sendMessage()
    }
  }

  // 拉取某个会话的历史消息
  const getMessagesList = async (id) => {
    const res = await server.get('/chat/messagesList', {params: {conversationId: id}})
    if (res.data.code === 200) {
      messagesList.value = res.data.data // 直接赋值，切换会话时先清空避免消息叠加
    } else {
      ElMessage.error(res.data.message)
    }
  }

  // 点击左侧会话
  const switchConversation = async (id) => {
    conversationId.value = String(id)
    messagesList.value = []
    await getMessagesList(id)
    scrollToBottom()
  }

  const conversationInfo = computed(() => {
    if (conversationId.value === null || conversationsList.value.length === 0) return null
    return conversationsList.value.find(c => String(c.conversationId) === String(conversationId.value))
  })

  onMounted(async () => {
    const res = await server.get('/chat/conversationsList')
    if (res.data.code === 200) {
      conversationsList.value.push(...res.data.data)
    } else {
      ElMessage.error(res.data.message)
    }
    if (conversationId.value) {
      await getMessagesList(conversationId.value)
      scrollToBottom()
    }
    connectWs()
  })

  onBeforeUnmount(() => {
    clearInterval(heartbeatTimer)
    clearTimeout(reconnectTimer)
    if (ws) ws.close()
  })
</script>

<template>
  <div class="chat">
    <!-- 左侧:会话列表 -->
    <div class="conversation_list">
      <div class="list_header">
        <span class="list_title">消息</span>
        <span class="list_badge">{{ conversationsList.length }}</span>
      </div>
      <div class="list_body">
        <div v-if="conversationsList.length === 0" class="list_empty">
          还没有会话,去商品详情页找 TA 聊聊吧
        </div>
        <div v-for="conversation in conversationsList" :key="conversation.conversationId"
             class="conversation"
             :class="{ active: String(conversation.conversationId) === conversationId }"
             @click="switchConversation(conversation.conversationId)">
          <div class="avatar">
            <img v-if="conversation.hisAvatar" :src="conversation.hisAvatar" alt="" />
            <span v-else class="avatar_fallback" :style="{ background: avatarColor(conversation.hisName) }">
              {{ firstChar(conversation.hisName) }}
            </span>
          </div>
          <div class="conversation_main">
            <div class="conversation_top">
              <span class="conversation_name">{{ conversation.hisName }}</span>
              <span class="conversation_time">{{ formatTime(conversation.updateTime) }}</span>
            </div>
            <div class="conversation_message">{{ conversation.lastMessage || '开始聊天吧' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧:聊天窗口 -->
    <div class="message_view">
      <template v-if="conversationInfo">
        <div class="header">
          <div class="header_back" @click="router.back()">
            <el-icon :size="20"><ArrowLeft /></el-icon>
          </div>
          <div class="header_center">
            <span class="header_name">{{ conversationInfo.hisName }}</span>
          </div>
          <div class="header_side"></div>
        </div>

        <div class="body">
          <div class="content" ref="contentRef">
            <div v-if="messagesList.length === 0" class="content_tip">还没有消息,打个招呼吧~</div>
            <div v-for="message in messagesList" :key="message.id"
                 class="message_row"
                 :class="message.senderId === senderId ? 'sent' : 'received'">
              <div class="row_avatar">
                <img v-if="(message.senderId === senderId ? sendAvatar : conversationInfo.hisAvatar)"
                     :src="message.senderId === senderId ? sendAvatar : conversationInfo.hisAvatar" alt="" />
                <span v-else class="avatar_fallback"
                      :style="{ background: avatarColor(message.senderId === senderId ? sendName : conversationInfo.hisName) }">
                  {{ firstChar(message.senderId === senderId ? sendName : conversationInfo.hisName) }}
                </span>
              </div>
              <div class="message_text">{{ message.content }}</div>
            </div>
          </div>
          <div class="input">
            <textarea ref="textareaRef" v-model="content"
                      :placeholder="`发消息给 ${conversationInfo.hisName}…`"
                      @keydown="handleInputKeydown"></textarea>
            <button class="send_button" :disabled="!content.trim()" @click="sendMessage">发送</button>
          </div>
        </div>
      </template>

      <!-- 未选择会话时的欢迎页 -->
      <div v-else class="message_empty">
        <div class="empty_icon">
          <el-icon :size="50"><ChatDotRound /></el-icon>
        </div>
        <p class="empty_title">选择左侧会话开始聊天</p>
        <p class="empty_sub">与卖家沟通闲置详情,谈妥价格后线下交易更安心</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== 整体布局:铺满页面高度,内部滚动 ===== */
.chat{
  height: 100%;
  box-sizing: border-box;
  padding: 20px 70px;
  display: flex;
  gap: 16px;
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif;
}

/* ===== 左侧:会话列表 ===== */
.conversation_list{
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border: 1px solid #e7ecf3;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(31, 86, 141, 0.08);
}
.list_header{
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 18px;
  background: #ffffff;
  border-bottom: 1px solid #eef1f6;
}
.list_title{
  font-size: 20px;
  font-weight: 700;
  color: #37474f;
  letter-spacing: 1px;
}
.list_badge{
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  box-sizing: border-box;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
}
.list_body{
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 10px;
  background: #fbfcfe;
}
.conversation{
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 4px;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.conversation:hover{
  background-color: #f2f6fb;
}
.conversation.active{
  background-color: #e4f0f9;
  box-shadow: inset 3px 0 0 #4f9ccc;
}
.avatar{
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 50%;
}
.avatar img,
.row_avatar img{
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
}
.avatar_fallback{
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #ffffff;
  font-weight: 600;
  user-select: none;
}
.avatar .avatar_fallback{
  font-size: 18px;
}
.conversation_main{
  flex: 1;
  min-width: 0;
  margin-left: 12px;
}
.conversation_top{
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.conversation_name{
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conversation_time{
  flex-shrink: 0;
  margin-left: 8px;
  font-size: 12px;
  color: #a3b1c2;
}
.conversation_message{
  font-size: 13px;
  color: #8a97a8;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.list_empty{
  padding: 60px 20px;
  text-align: center;
  font-size: 13px;
  line-height: 1.8;
  color: #b0bcc9;
}

/* ===== 右侧:聊天窗口 ===== */
.message_view{
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border: 1px solid #e7ecf3;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(31, 86, 141, 0.08);
}
.header{
  flex-shrink: 0;
  display: grid;
  grid-template-columns: 64px 1fr 64px;
  align-items: center;
  padding: 10px 0;
  background: #ffffff;
  border-bottom: 1px solid #eef1f6;
}
.header_back{
  justify-self: start;
  width: 36px;
  height: 36px;
  margin-left: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #5b6b7c;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
}
.header_back:hover{
  background-color: #eef3f8;
  color: #4f9ccc;
}
.header_center{
  justify-self: center;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.header_name{
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 消息区 ===== */
.body{
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f7fafd, #eef3f9);
}
.content{
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px 18px 14px;
}
.content_tip{
  margin-top: 60px;
  text-align: center;
  font-size: 14px;
  color: #a9b7c6;
}
.message_row{
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 76%;
}
.message_row.received{
  align-self: flex-start;
  flex-direction: row;
}
.message_row.sent{
  align-self: flex-end;
  flex-direction: row-reverse;
}
.row_avatar{
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 50%;
}
.row_avatar .avatar_fallback{
  font-size: 16px;
}
.message_text{
  position: relative;
  padding: 10px 14px;
  font-size: 15px;
  line-height: 1.55;
  border-radius: 14px;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}
.received .message_text{
  background-color: #ffffff;
  color: #333c46;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(31, 86, 141, 0.08);
}
.received .message_text::before{
  content: "";
  position: absolute;
  left: -7px;
  top: 12px;
  border: 7px solid transparent;
  border-right-color: #ffffff;
  border-left-width: 0;
}
.sent .message_text{
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 6px rgba(79, 156, 204, 0.25);
}
.sent .message_text::after{
  content: "";
  position: absolute;
  right: -7px;
  top: 12px;
  border: 7px solid transparent;
  border-left-color: #71b5e2;
  border-right-width: 0;
}

/* ===== 输入区 ===== */
.input{
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 14px;
  background: #ffffff;
  border-top: 1px solid #eef1f6;
}
.input textarea{
  flex: 1;
  min-width: 0;
  height: 80px;
  box-sizing: border-box;
  padding: 12px 14px;
  resize: none;
  border: none;
  outline: none;
  border-radius: 12px;
  background-color: #f3f6fa;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.5;
  color: #2c3e50;
  transition: background-color 0.15s ease, box-shadow 0.15s ease;
}
.input textarea:focus{
  background-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(79, 156, 204, 0.25);
}
.input textarea::placeholder{
  color: #a9b7c6;
}
.send_button{
  height: 42px;
  padding: 0 26px;
  border: none;
  border-radius: 21px;
  background: linear-gradient(135deg, #4f9ccc, #8fd3f4);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  box-shadow: 0 3px 10px rgba(79, 156, 204, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}
.send_button:hover:not(:disabled){
  transform: translateY(-1px);
  filter: brightness(1.05);
  box-shadow: 0 5px 14px rgba(79, 156, 204, 0.45);
}
.send_button:active:not(:disabled){
  transform: scale(0.96);
}
.send_button:disabled{
  background: #ccd6e0;
  box-shadow: none;
  cursor: not-allowed;
}

/* ===== 未选择会话的欢迎页 ===== */
.message_empty{
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8fafd, #eef3f9);
}
.empty_icon{
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffffff;
  color: #8fb7d8;
  box-shadow: 0 8px 24px rgba(79, 156, 204, 0.14);
}
.empty_title{
  font-size: 17px;
  font-weight: 600;
  color: #5b6b7c;
}
.empty_sub{
  margin-top: 8px;
  font-size: 13px;
  color: #9fb0c1;
}

/* ===== 细滚动条 ===== */
.list_body::-webkit-scrollbar,
.content::-webkit-scrollbar{
  width: 6px;
}
.list_body::-webkit-scrollbar-thumb,
.content::-webkit-scrollbar-thumb{
  background: #cdd9e6;
  border-radius: 3px;
}
.list_body::-webkit-scrollbar-thumb:hover,
.content::-webkit-scrollbar-thumb:hover{
  background: #b3c3d5;
}
</style>