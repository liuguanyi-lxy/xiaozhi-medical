/*
 * old-preview.js — 原版 ChatWindow.vue 的预览入口
 *
 * ⚠️ 预览辅助文件，不属于产品代码，可随时删除。
 *
 * 做了一件事：把 axios 的适配器换成本地假数据，并按字吐出来。
 * 这样组件里的 onDownloadProgress 会像接后端时一样被逐段调用，
 * 界面上就是真实的流式打字效果 —— 组件本身一行都没改。
 */
import { createApp } from 'vue'
import axios from 'axios'
import ChatWindowV2 from './components/ChatWindow.vue'
import './style.css'

/* ---------- 假回答库 ---------- */
const ANSWERS = [
  [
    '你好，我是小智医疗。',
    '\n回答之前我会先从医学知识库里检索，再照着检索到的原文作答，每个结论后面都跟着出处。',
    '\n\n你可以直接描述症状、问一个药名，或者问某份指南里的具体条目。',
  ],
  [
    '结论：单纯发热三天、没有脓痰和肺部体征时，通常不需要抗菌药物。\n',
    '抗菌药物对病毒无效，只在明确或高度怀疑细菌感染时才用；病毒性上呼吸道感染的病程多在 5 到 7 天自行缓解。\n',
    '如果出现呼吸困难、意识改变，或者高热持续不退，直接去医院，不要再等。\n\n',
    '依据：抗菌药物临床应用指导原则 §2.1；内科学（第九版）· 发热待查；社区获得性肺炎诊治指南 第 3 章',
  ],
  [
    '结论：诊室血压测到 140/90 mmHg 及以上才考虑高血压。\n',
    '家里自测的线更低一些，135/85 mmHg 就算升高。单次升高不能下结论，要在不同的日子复测三次。\n\n',
    '依据：中国高血压防治指南 §3.2；家庭血压监测指南 §2',
  ],
]
let turn = 0

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

/* ---------- 替换 axios 适配器：本地造流 ---------- */
axios.defaults.adapter = async (config) => {
  const chunks = ANSWERS[Math.min(turn, ANSWERS.length - 1)]
  turn += 1

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
