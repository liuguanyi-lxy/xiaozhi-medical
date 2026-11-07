/*
 * v2-preview.js — ChatWindow.v2.vue 的预览入口
 *
 * ⚠️ 预览辅助文件，不属于产品代码，可随时删除。
 *
 * 做了一件事：把 axios 的适配器换成本地假数据，并按字吐出来。
 * 这样组件里的 onDownloadProgress 会像接后端时一样被逐段调用，
 * 界面上就是真实的流式打字效果 —— 组件本身一行都没改。
 */
import { createApp } from 'vue'
import axios from 'axios'
import ChatWindowV2 from './components/ChatWindow.v2.vue'
import './style.css'

/* ---------- 假回答库（按用户问的话匹配，仅用于预览） ---------- */
const REPLIES = {
  intro: [
    '你好，我是小智医疗。',
    '\n我可以做三件事：帮你不确定挂哪个科时做分导诊、直接对话完成预约挂号、以及针对具体医学问题查知识库作答。',
    '\n\n想挂号的话，告诉我科室、日期和上午还是下午就行。',
  ],
  dept: [
    '内科看的是不需要开刀的那一类：感冒发热、咳嗽、胃肠不适、高血压、糖尿病随访，都属于内科。',
    '\n\n如果只是发热咳嗽，先挂内科；如果已经明确是某个部位的外科问题，才转外科。',
    '\n\n要挂号的话，告诉我日期和上午还是下午，我先帮你查有没有号源。',
  ],
  triage: [
    '你说症状，我帮你判断。',
    '\n\n顺便说一下分导诊的逻辑：持续发热咳嗽偏内科，外伤和需要处理的包块偏外科，14 岁以下偏儿科，月经与妊娠相关偏妇科。',
    '\n\n拿不准就直接描述，比如「右边下腹疼了两天」。',
  ],
  booking: [
    '我查了一下，2026-11-10 上午内科还有号源。',
    '\n\n确认一下就帮你约上：',
    '\n就诊人 张三｜科室 内科｜日期 2026-11-10｜时段 上午｜医生 随机分配',
    '\n\n这样约可以吗？回复「确认」我就提交。',
  ],
  antibiotic: [
    '结论：单纯发热三天、没有脓痰和肺部体征时，通常不需要抗菌药物。\n',
    '抗菌药物对病毒无效，只在明确或高度怀疑细菌感染时才用；病毒性上呼吸道感染的病程多在 5 到 7 天自行缓解。\n',
    '如果出现呼吸困难、意识改变，或者高热持续不退，直接去医院，不要再等。\n\n',
    '依据：抗菌药物临床应用指导原则 §2.1；内科学（第九版）· 发热待查；社区获得性肺炎诊治指南 第 3 章',
  ],
  bp: [
    '结论：诊室血压测到 140/90 mmHg 及以上才考虑高血压。\n',
    '家里自测的线更低一些，135/85 mmHg 就算升高。单次升高不能下结论，要在不同的日子复测三次。\n\n',
    '依据：中国高血压防治指南 §3.2；家庭血压监测指南 §2',
  ],
}

const pickReply = (msg) => {
  if (/不知道挂什么科|不确定该挂|帮我看看/.test(msg)) return REPLIES.triage
  if (/内科|外科|儿科|妇科|哪些病|什么科/.test(msg)) return REPLIES.dept
  if (/^你好|介绍/.test(msg)) return REPLIES.intro
  if (/血压|高压|低压/.test(msg)) return REPLIES.bp
  if (/抗生素|发烧|发热/.test(msg)) return REPLIES.antibiotic
  if (/预约|挂号|号源|上午|下午/.test(msg)) return REPLIES.booking
  return REPLIES.intro
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

/* ---------- 替换 axios 适配器：本地造流 ---------- */
axios.defaults.adapter = async (config) => {
  let body = {}
  try {
    body = JSON.parse(config.data || '{}')
  } catch (e) {
    body = {}
  }
  const chunks = pickReply(body.message || '')

  await sleep(420) // 模拟首字节延迟，让加载点先转起来

  let acc = ''
  for (const chunk of chunks) {
    // 按小段切开，做出逐字出现的观感
    for (let i = 0; i < chunk.length; i += 3) {
      acc += chunk.slice(i, i + 3)
      config.onDownloadProgress?.({
        event: { target: { responseText: acc } },
        loaded: acc.length,
        total: 0,
      })
      await sleep(26)
    }
  }

  return {
    data: acc,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
    request: {},
  }
}

createApp(ChatWindowV2).mount('#app')
