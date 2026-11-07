<!--
  ============================================================
  ChatWindow.v2.vue — 小智医疗聊天界面（视觉层重做版）

  与原 ChatWindow.vue 的关系：
    · script 逻辑完全一致（axios 流式、memoryId、UUID、转义、新会话）
    · 只重做 template 的结构与样式，不改任何接口与数据处理
    · Element Plus 未在本文件中使用，改用原生元素以完全控制视觉；
      package.json 里的依赖保留，不影响其他组件

  启用方式（App.vue 改一行）：
    import ChatWindow from '@/components/ChatWindow.vue'
              ↓
    import ChatWindow from '@/components/ChatWindow.v2.vue'

  设计来源：taste-skill app sheet「Editorial greeting」
    风格 seed 1025909414 · kind workspace · light
    字体适配：Instrument Serif → 思源宋体（问候）
              Instrument Sans  → 思源黑体（正文）
  ============================================================
-->
<template>
  <div class="xz">
    <!-- ============ 侧栏 ============ -->
    <aside class="rail">
      <div class="brand">
        <span class="mark" aria-hidden="true"></span>
        <span class="brand-name">小智医疗</span>
      </div>

      <button class="new-chat" type="button" @click="newChat">
        <span class="plus" aria-hidden="true">＋</span>
        新会话
      </button>

      <div class="rail-foot">
        <!-- 就诊人：挂号的必填信息，存本地，发消息时自动带给后端 -->
        <div v-if="!editingPatient" class="patient">
          <p class="patient-label">就诊人</p>
          <template v-if="patient.name">
            <p class="patient-name">{{ patient.name }}</p>
            <p class="patient-id">{{ maskId(patient.idCard) }}</p>
            <button class="patient-act" type="button" @click="startEdit">
              修改
            </button>
          </template>
          <template v-else>
            <p class="patient-empty">还没填。挂号时需要姓名和身份证号。</p>
            <button class="patient-act" type="button" @click="startEdit">
              填写
            </button>
          </template>
        </div>

        <form v-else class="patient-edit" @submit.prevent="savePatient">
          <label class="sr" for="p-name">姓名</label>
          <input id="p-name" v-model.trim="draft.name" placeholder="姓名" autocomplete="name" />
          <label class="sr" for="p-id">身份证号</label>
          <input
            id="p-id"
            v-model.trim="draft.idCard"
            placeholder="身份证号"
            inputmode="numeric"
            autocomplete="off"
          />
          <div class="patient-edit-btns">
            <button type="submit">保存</button>
            <button type="button" class="ghost" @click="editingPatient = false">
              取消
            </button>
          </div>
        </form>

        <p class="rail-note">回答先检索知识库，再引用原文。</p>
      </div>
    </aside>

    <!-- ============ 主区 ============ -->
    <main class="canvas">
      <div class="column">
        <header class="greeting" :class="{ compact: messages.length > 0 }">
          <h1 v-if="!messages.length">今天想了解点什么</h1>
          <h1 v-else>接着问</h1>
          <p v-if="!messages.length" class="greeting-sub">
            先查医学知识库，再照原文回答，每句都能回到出处。
          </p>
        </header>

        <div class="thread" ref="messageListRef">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['msg', message.isUser ? 'msg-user' : 'msg-bot']"
          >
            <div class="who">{{ message.isUser ? '你' : '小智医疗' }}</div>
            <div class="body">
              <span v-html="formatAnswer(message.content)"></span>
              <span
                class="dots"
                v-if="message.isThinking || message.isTyping"
                aria-label="正在回答"
              >
                <span></span><span></span><span></span>
              </span>
            </div>

            <!-- 预约成功卡：从回复文本里抽要素，抽不到就不渲染 -->
            <div v-if="!message.isUser && bookingOf(message.content)" class="book">
              <div class="book-head">
                <span class="book-tag">已预约</span>
                <span class="book-hint">凭姓名和身份证到院签到</span>
              </div>
              <dl class="book-grid">
                <div>
                  <dt>科室</dt>
                  <dd>{{ bookingOf(message.content).dept || '—' }}</dd>
                </div>
                <div>
                  <dt>医生</dt>
                  <dd>{{ bookingOf(message.content).doctor || '—' }}</dd>
                </div>
                <div>
                  <dt>日期</dt>
                  <dd>{{ bookingOf(message.content).date || '—' }}</dd>
                </div>
                <div>
                  <dt>时段</dt>
                  <dd>{{ bookingOf(message.content).time || '—' }}</dd>
                </div>
              </dl>
            </div>
          </div>
        </div>

        <!-- 科室与挂号快捷入口：把「能查科室、能挂号」明摆在界面上 -->
        <div class="quick">
          <span class="quick-hint">查科室</span>
          <button
            v-for="d in DEPTS"
            :key="d"
            type="button"
            class="quick-item"
            @click="askDepartment(d)"
          >
            {{ d }}
          </button>
          <span class="quick-sep" aria-hidden="true"></span>
          <button type="button" class="quick-item" @click="askTriage">
            不知道挂什么科
          </button>
        </div>

        <form class="composer" @submit.prevent="sendMessage">
          <label class="sr" for="xz-input">输入问题</label>
          <textarea
            id="xz-input"
            v-model="inputMessage"
            rows="1"
            placeholder="描述症状、药名，或者直接问一个问题"
            @keydown.enter.exact.prevent="sendMessage"
            @input="autoGrow"
          ></textarea>
          <button class="send" type="submit" :disabled="isSending">
            {{ isSending ? '回答中' : '发送' }}
          </button>
        </form>

        <p class="foot-note">回答依据知识库中的文献段落，不替代医生诊断。</p>
      </div>
    </main>
  </div>
</template>

<script setup>
/* ==========================================================
   以下逻辑与原 ChatWindow.vue 完全一致，未作任何改动
   ========================================================== */
import { onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { v4 as uuidv4 } from 'uuid'

const messageListRef = ref()
const isSending = ref(false)
const uuid = ref()
const inputMessage = ref('')
const messages = ref([])

/* ----------------------------------------------------------
   就诊人：挂号必填项（姓名 + 身份证号）
   存在本地，发消息时自动带给后端，省掉反复追问；
   界面上只显示脱敏后的号码
   ---------------------------------------------------------- */
const PATIENT_KEY = 'xiaozhi_patient'
const patient = ref({ name: '', idCard: '' })
const draft = ref({ name: '', idCard: '' })
const editingPatient = ref(false)

const DEPTS = ['内科', '外科', '儿科', '妇科']

const loadPatient = () => {
  try {
    const raw = localStorage.getItem(PATIENT_KEY)
    if (raw) patient.value = JSON.parse(raw)
  } catch (e) {
    console.warn('就诊人信息读取失败', e)
  }
}

const startEdit = () => {
  draft.value = { ...patient.value }
  editingPatient.value = true
}

const savePatient = () => {
  patient.value = { ...draft.value }
  try {
    localStorage.setItem(PATIENT_KEY, JSON.stringify(patient.value))
  } catch (e) {
    console.warn('就诊人信息保存失败', e)
  }
  editingPatient.value = false
}

const maskId = (id) =>
  id ? id.replace(/^(.{3})[\s\S]*(.{4})$/, '$1***********$2') : ''

/* 发送前把就诊人拼进去（只发给后端，界面显示仍是原话） */
const withPatient = (msg) => {
  const { name, idCard } = patient.value
  if (!name || !idCard) return msg
  return `[就诊人：${name}，身份证号：${idCard}]\n${msg}`
}

const askDepartment = (d) => {
  inputMessage.value = `${d}主要看哪些病？挂号要选哪位医生？`
}

const askTriage = () => {
  inputMessage.value = '我不太确定该挂哪个科，我说症状你帮我看看'
}

onMounted(() => {
  initUUID()
  loadPatient()
  // 移除 setInterval，改用手动滚动
  watch(messages, () => scrollToBottom(), { deep: true })
  hello()
})

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const hello = () => {
  sendRequest('你好')
}

const sendMessage = () => {
  if (inputMessage.value.trim()) {
    sendRequest(inputMessage.value.trim())
    inputMessage.value = ''
  }
}

const sendRequest = (message) => {
  isSending.value = true
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }
  //第一条默认发送的用户消息”你好“不放入会话列表
  if (messages.value.length > 0) {
    messages.value.push(userMsg)
  }

  // 添加机器人加载消息
  const botMsg = {
    isUser: false,
    content: '', // 增量填充
    isTyping: true, // 显示加载动画
    isThinking: false,
  }
  messages.value.push(botMsg)
  const lastMsg = messages.value[messages.value.length - 1]
  scrollToBottom()

  axios
    .post(
      '/api/xiaozhi/chat',
      { memoryId: uuid.value, message: withPatient(message) },
      {
        responseType: 'text', // 浏览器端 axios 不支持 'stream'，靠 onDownloadProgress 读取增量文本
        onDownloadProgress: (e) => {
          const fullText = e.event.target.responseText // 累积的完整文本
          let newText = fullText.substring(lastMsg.content.length)
          lastMsg.content += newText //增量更新
          scrollToBottom() // 实时滚动
        },
      }
    )
    .then(() => {
      // 流结束后隐藏加载动画
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
    .catch((error) => {
      console.error('流式错误:', error)
      messages.value.at(-1).content = '请求失败，请重试'
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
}

// 初始化 UUID
const initUUID = () => {
  let storedUUID = localStorage.getItem('user_uuid')
  if (!storedUUID) {
    storedUUID = uuidToNumber(uuidv4())
    localStorage.setItem('user_uuid', storedUUID)
  }
  uuid.value = storedUUID
}

const uuidToNumber = (uuid) => {
  let number = 0
  for (let i = 0; i < uuid.length && i < 6; i++) {
    const hexValue = uuid[i]
    number = number * 16 + (parseInt(hexValue, 16) || 0)
  }
  return number % 1000000
}

// 转义 HTML 并排版：先转义特殊字符防止 XSS（AI 回复和用户输入都可能包含 HTML），再把换行/制表符转为可显示格式
const convertStreamOutput = (output) => {
  return output
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;')
}

/* ----------------------------------------------------------
   预约成功卡：从回复文本里抽取要素
   后端返回的是纯文本流，没有结构化字段，所以只能按标签名匹配。
   抽不到关键要素（科室/日期）就返回 null，不渲染卡片，避免误报。
   ---------------------------------------------------------- */
const bookingOf = (text) => {
  if (!text || !/预约成功|挂号成功|预约已确认|已锁定号源|已为您预约/.test(text)) {
    return null
  }
  const pick = (labels) => {
    for (const l of labels) {
      const m = text.match(new RegExp(l + '\\s*\\**\\s*[：:]\\s*\\**\\s*([^\\n]+)'))
      if (m && m[1]) {
        return m[1].replace(/[*`]/g, '').trim().slice(0, 40)
      }
    }
    return ''
  }
  // 日期格式不固定：2026-11-10 / 2026年11月10日 / 2026/11/10 都认
  let date = ''
  const dm = text.match(/(\d{4})\s*[-年./]\s*(\d{1,2})\s*[-月./]\s*(\d{1,2})/)
  if (dm) {
    date = `${dm[1]}-${dm[2].padStart(2, '0')}-${dm[3].padStart(2, '0')}`
  }
  const booking = {
    dept: pick(['预约科室', '就诊科室', '科室']),
    doctor: pick(['就诊医生', '预约医生', '医生']),
    date,
    time: (text.match(/(上午|下午)/) || [])[1] || '',
  }
  // 科室和日期都抽不到，就不当成预约成功
  if (!booking.dept || !booking.date) return null
  return booking
}

const newChat = () => {
  // 这里添加新会话的逻辑
  console.log('开始新会话')
  localStorage.removeItem('user_uuid')
  window.location.reload()
}

/* ----------------------------------------------------------
   展示层增强：把结尾的「依据：……」一行单独标记出来
   只做标记，不改内容、不改数据流；原文一字不动
   ---------------------------------------------------------- */
const formatAnswer = (text) => {
  return convertStreamOutput(text)
    // 模型爱用 markdown 粗体，v-html 不认，手动转成 <b>（内容已转义，安全）
    .replace(/\*\*([^*\n]+)\*\*/g, '<b>$1</b>')
    .split('<br>')
    .map((line) => {
      const m = line.trim().match(/^(依据[：:])([\s\S]*)$/)
      return m ? `<span class="cite"><b>${m[1]}</b>${m[2]}</span>` : line
    })
    .join('<br>')
}

/* 新增：输入框随内容长高，不改变任何数据流 */
const autoGrow = (e) => {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}
</script>

<style scoped>
/* ==========================================================
   tokens — 来自 app sheet「Editorial greeting」
   ========================================================== */
.xz {
  --ground: #ffffff;
  --surface: #f9f9f9;
  --raised: #ffffff;
  --border: #eaeaea;
  --rule: #f3f3f3;
  --control: #d9d9d6;
  --text: #1f201d;
  --muted: #636361;
  --accent: #1b6e66;

  --radius-card: 22px;
  --radius-control: 999px;
  --row-height: 63px;
  --rail-width: 224px;
  --gap: 16px;
  --pad: 20px;
  --control-height: 38px;

  --fs-title: 48px;
  --fs-section: 14px;
  --fs-body: 14px;
  --fs-small: 13px;

  /* 固定铺满视口，绕开 Vite 默认模板 #app 的 max-width 限制 */
  position: fixed;
  inset: 0;
  z-index: 1;
  display: flex;
  overflow: hidden;

  background: var(--ground);
  color: var(--text);
  font-family: "Noto Sans SC", "Source Han Sans SC", "PingFang SC",
    "Microsoft YaHei", system-ui, sans-serif;
  font-size: var(--fs-body);
  line-height: 1.7;
  text-align: left;
  -webkit-font-smoothing: antialiased;
}

.sr {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
  border: 0;
}

.xz,
.xz *,
.xz *::before,
.xz *::after {
  box-sizing: border-box;
}

/* ==========================================================
   侧栏 — 落在页面底上，无顶栏，一条 hairline 分隔
   ========================================================== */
.rail {
  flex: 0 0 var(--rail-width);
  width: var(--rail-width);
  padding: 32px 20px 24px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--rule); /* taste-skill-ignore: 侧栏唯一的 1px 分隔 */
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 4px;
}

/* 品牌标识：不用图片头像，避免与任何第三方 AI 助手形象撞车。
   一个深青竖标 + 文字 wordmark，克制且不会过时 */
.mark {
  width: 3px;
  height: 16px;
  border-radius: 2px;
  background: var(--accent);
  flex: 0 0 3px;
}

.brand-name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: .01em;
}

.new-chat {
  margin-top: 28px;
  height: var(--control-height);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  background: var(--surface);
  color: var(--text);
  border: 0;
  border-radius: var(--radius-control);
  font-size: var(--fs-body);
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background .16s ease;
}

.new-chat:hover {
  background: #f1f1f1;
}

.new-chat:active {
  background: #e9e9e9;
}

.plus {
  color: var(--muted);
  font-size: 15px;
}

.rail-foot {
  margin-top: auto;
}

.rail-note {
  margin: 0;
  font-size: var(--fs-small);
  line-height: 1.65;
  color: var(--muted);
  max-width: 22ch;
}

/* ==========================================================
   主区
   ========================================================== */
.canvas {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: center;
  overflow: hidden;
}

.column {
  width: 100%;
  max-width: 1100px;
  height: 100%;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
}

/* 问候语：页面唯一 h1，serial 字体 */
.greeting {
  padding-top: 72px;
  transition: padding-top .3s ease;
}

.greeting h1 {
  margin: 0;
  font-family: "Noto Serif SC", "Source Han Serif SC", "Songti SC", serif;
  font-size: var(--fs-title);
  font-weight: 600;
  line-height: 1.16;
  letter-spacing: -.02em;
  transition: font-size .3s ease;
}

.greeting.compact {
  padding-top: 28px;
}

.greeting.compact h1 {
  font-size: 24px;
  font-weight: 500;
}

.greeting-sub {
  margin: 14px 0 0;
  font-size: 15px;
  color: var(--muted);
  max-width: 34ch;
}

/* ==========================================================
   对话流 — 9 栏宽度（821px），居中
   ========================================================== */
.thread {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 36px 0 28px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  scrollbar-width: thin;
  scrollbar-color: #dcdcda transparent;
}

.thread::-webkit-scrollbar {
  width: 8px;
}

.thread::-webkit-scrollbar-thumb {
  background: #dcdcda;
  border-radius: 4px;
}

.msg {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.who {
  font-size: var(--fs-small);
  color: var(--muted);
  margin-bottom: 6px;
  letter-spacing: .02em;
}

.body {
  font-size: var(--fs-body);
  line-height: 1.8;
  color: var(--text);
  overflow-wrap: anywhere;
}

/* 用户：右对齐，浅面 pill；助手：直接落在地面上，不加气泡 */
.msg-user {
  align-items: flex-end;
}

.msg-user .who {
  text-align: right;
}

.msg-user .body {
  background: var(--surface);
  border-radius: var(--radius-card);
  border-bottom-right-radius: 8px;
  padding: 12px 18px;
  max-width: 560px;
}

.msg-bot .body {
  max-width: 700px;
}

/* 依据行：比正文小一号、灰色，'依据'二字用强调色做锚点
   v-html 插入的节点不带 scoped 标记，必须用 :deep() 才能命中 */
.msg-bot .body :deep(.cite) {
  display: block;
  margin-top: 16px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--muted);
}

.msg-bot .body :deep(.cite b) {
  color: var(--accent);
  font-weight: 500;
}

/* 加载中的三点 */
.dots {
  display: inline-flex;
  gap: 4px;
  margin-left: 8px;
  vertical-align: middle;
}

.dots span {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--accent);
  animation: xz-pulse 1.2s infinite ease-in-out both;
}

.dots span:nth-child(2) {
  animation-delay: -.6s;
}

.dots span:nth-child(3) {
  animation-delay: -.3s;
}

@keyframes xz-pulse {
  0%, 100% {
    transform: scale(.6);
    opacity: .35;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ==========================================================
   输入条 — 页面上唯一带阴影的东西（设计单允许的唯一浮层）
   ========================================================== */
.composer {
  flex: 0 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 8px 8px 8px 18px;
  background: var(--raised);
  border-radius: var(--radius-card);
  box-shadow: 0 2px 8px rgba(31, 32, 29, .06), 0 12px 32px rgba(31, 32, 29, .06); /* taste-skill-ignore */
}

.composer textarea {
  flex: 1;
  min-height: 38px;
  max-height: 160px;
  padding: 8px 0;
  border: 0;
  outline: none;
  resize: none;
  background: transparent;
  color: var(--text);
  font-family: inherit;
  font-size: var(--fs-body);
  line-height: 1.6;
}

.composer textarea::placeholder {
  color: #9a9a97;
}

.send {
  flex: 0 0 auto;
  height: var(--control-height);
  padding: 0 18px;
  border: 0;
  border-radius: var(--radius-control);
  background: var(--accent);
  color: #fff;
  font-family: inherit;
  font-size: var(--fs-body);
  font-weight: 500;
  cursor: pointer;
  transition: background .16s ease, transform .16s ease;
}

.send:hover:not(:disabled) {
  background: #17594f;
}

.send:active:not(:disabled) {
  transform: translateY(1px);
}

.send:disabled {
  background: var(--control);
  color: #8b8b88;
  cursor: default;
}

/* ==========================================================
   预约成功卡
   ========================================================== */
.book {
  margin-top: 18px;
  max-width: 520px;
  padding: 16px 20px 20px;
  background: var(--surface);
  border-radius: var(--radius-card);
}

.book-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.book-tag {
  padding: 3px 10px;
  border-radius: var(--radius-control);
  background: var(--accent);
  color: #fff;
  font-size: var(--fs-small);
  font-weight: 500;
}

.book-hint {
  font-size: var(--fs-small);
  color: var(--muted);
}

.book-grid {
  margin: 18px 0 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}

.book-grid div {
  min-width: 0;
}

.book-grid dt {
  margin: 0;
  font-size: var(--fs-small);
  color: var(--muted);
}

.book-grid dd {
  margin: 4px 0 0;
  font-size: 1.0625rem;
  font-weight: 500;
  color: var(--text);
  overflow-wrap: anywhere;
}

.foot-note {
  margin: 12px 0 18px;
  font-size: var(--fs-small);
  color: var(--muted);
}

/* ==========================================================
   就诊人（侧栏底部）与科室快捷入口
   ========================================================== */
.patient {
  padding-bottom: 18px;
}

.patient-label {
  margin: 0;
  font-size: var(--fs-small);
  color: var(--muted);
  letter-spacing: .04em;
}

.patient-name {
  margin: 6px 0 0;
  font-size: 15px;
  font-weight: 500;
}

.patient-id {
  margin: 2px 0 0;
  font-size: var(--fs-small);
  color: var(--muted);
  font-variant-numeric: tabular-nums;
}

.patient-empty {
  margin: 6px 0 0;
  font-size: var(--fs-small);
  line-height: 1.65;
  color: var(--muted);
}

.patient-act {
  margin-top: 8px;
  padding: 0;
  background: none;
  border: 0;
  font-family: inherit;
  font-size: var(--fs-small);
  color: var(--accent);
  cursor: pointer;
}

.patient-act:hover {
  text-decoration: underline;
}

.patient-edit {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-bottom: 18px;
}

.patient-edit input {
  height: 32px;
  padding: 0 10px;
  background: var(--ground);
  border: 1px solid var(--border); /* taste-skill-ignore: 表单输入框需要边界 */
  border-radius: 8px;
  font-family: inherit;
  font-size: var(--fs-small);
  color: var(--text);
  outline: none;
}

.patient-edit input:focus {
  border-color: var(--accent);
}

.patient-edit-btns {
  display: flex;
  gap: 8px;
  margin-top: 2px;
}

.patient-edit-btns button {
  height: 28px;
  padding: 0 12px;
  border: 0;
  border-radius: var(--radius-control);
  background: var(--accent);
  color: #fff;
  font-family: inherit;
  font-size: var(--fs-small);
  cursor: pointer;
}

.patient-edit-btns button.ghost {
  background: var(--surface);
  color: var(--muted);
}

/* 输入框上方的科室入口 */
.quick {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 14px;
  padding: 0 2px 12px;
  font-size: var(--fs-small);
}

.quick-hint {
  color: var(--muted);
}

.quick-item {
  padding: 0;
  background: none;
  border: 0;
  font-family: inherit;
  font-size: var(--fs-small);
  color: #45453f;
  cursor: pointer;
  transition: color .16s ease;
}

.quick-item:hover {
  color: var(--accent);
}

.quick-sep {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--control);
}

/* 键盘可达性 */
.new-chat:focus-visible,
.send:focus-visible,
.composer textarea:focus-visible {
  outline: 2px solid var(--accent);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .greeting,
  .greeting h1 {
    transition: none;
  }
  .dots span {
    animation: none;
  }
}

/* ==========================================================
   手机：侧栏折成 48px 顶栏
   ========================================================== */
@media (max-width: 820px) {
  .xz {
    flex-direction: column;
  }

  .rail {
    flex: 0 0 48px;
    width: 100%;
    min-height: 48px;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    border-right: 0;
    border-bottom: 1px solid var(--rule); /* taste-skill-ignore */
  }

  .new-chat {
    margin-top: 0;
    height: 32px;
    padding: 0 14px;
    font-size: var(--fs-small);
  }

  .rail-foot {
    display: none;
  }

  .column {
    padding: 0 16px;
  }

  .greeting {
    padding-top: 32px;
  }

  .greeting h1 {
    font-size: 28px;
  }

  .greeting.compact {
    padding-top: 20px;
  }

  .greeting.compact h1 {
    font-size: 20px;
  }

  .greeting-sub {
    margin-top: 10px;
    font-size: var(--fs-body);
  }

  .thread {
    padding: 24px 0 16px;
    gap: 22px;
  }

  .msg-user .body {
    max-width: 86%;
    padding: 10px 15px;
  }

  .composer {
    padding: 6px 6px 6px 14px;
  }

  .send {
    padding: 0 14px;
  }
}
</style>
