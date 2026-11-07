module.exports = {
  /*
  ** Headers of the page
  */
  head: {
    title: 'yygh-site',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: '尚医通 - 预约挂号统一平台' }
    ],
    link: [
      { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
    ]
  },
  /*
  ** Customize the progress bar color
  */
  loading: { color: '#3B8070' },

  plugins: [{
    src: '~plugins/myPlugin',
    ssr: true,
  }],
  css: [
    'element-ui/lib/theme-chalk/index.css'
  ],
  
  // 禁用资源提示以避免 renderResourceHints 错误
  render: {
    resourceHints: false,
    // 明确配置 bundleRenderer 选项
    bundleRenderer: {
      shouldPrefetch: () => false,
      shouldPreload: () => false
    }
  },
  
  /*
  ** Build configuration
  */
  build: {
    /*
    ** Run ESLint on save
    */
    extend (config, { isDev, isClient }) {
      if (isDev && isClient) {
        config.module.rules.push({
          enforce: 'pre',
          test: /\.(js|vue)$/,
          loader: 'eslint-loader',
          exclude: /(node_modules)/
        })
      }
    }
  }
}

