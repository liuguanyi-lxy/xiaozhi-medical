"use strict";
// 引入 Node.js 的 path 模块，用于处理文件路径
const path = require("path");
// 引入项目配置文件，获取应用标题等设置
const defaultSettings = require("./src/settings.js");

/**
 * 解析绝对路径
 * @param {string} dir - 相对于项目根目录的路径
 * @returns {string} - 绝对路径
 */
function resolve(dir) {
  return path.join(__dirname, dir);
}

// 获取应用标题，优先使用配置文件中的标题，否则使用默认值
const name = defaultSettings.title || "vue Admin Template";

// 开发服务器端口配置
// 优先级：环境变量 port > npm_config_port > 默认 9528
const port = process.env.port || process.env.npm_config_port || 9528;

// Vue CLI 配置文档：https://cli.vuejs.org/config/
module.exports = {
  /**
   * 部署基础路径
   * 如果部署在子路径下（如 GitHub Pages），需要设置此值
   * 例如部署到 https://foo.github.io/bar/，则设置为 "/bar/"
   * 大多数情况下使用 '/' 即可
   */
  publicPath: "/",
  // 构建输出目录
  outputDir: "dist",
  // 静态资源目录（相对于 outputDir）
  assetsDir: "static",
  // 是否在开发环境下开启 eslint 保存时检查
  lintOnSave: false,
  // 生产环境关闭 source map，减少构建体积
  productionSourceMap: false,
  // 开发服务器配置
  devServer: {
    port: port, // 开发服务器端口
    open: true, // 启动后自动打开浏览器
    overlay: {
      warnings: false, // 不显示警告
      errors: true, // 显示编译错误
    },
    // Mock 服务器配置，用于模拟接口数据
    before: require("./mock/mock-server.js"),
  },
  configureWebpack: {
    // 设置 webpack name，便于在 index.html 中注入正确的标题
    name: name,
    resolve: {
      // 路径别名配置，@ 指向 src 目录
      alias: {
        "@": resolve("src"),
      },
    },
  },
  // 链式 webpack 配置
  chainWebpack(config) {
    // 预加载策略：提升首屏加载速度
    config.plugin("preload").tap(() => [
      {
        rel: "preload",
        // 排除 runtime.js，避免重复加载
        fileBlacklist: [/\.map$/, /hot-update\.js$/, /runtime\..*\.js$/],
        include: "initial",
      },
    ]);

    // 删除预取插件，避免大量页面造成无意义的请求
    config.plugins.delete("prefetch");

    // SVG 雪碧图加载器配置
    config.module.rule("svg").exclude.add(resolve("src/icons")).end();
    config.module
      .rule("icons")
      .test(/\.svg$/)
      .include.add(resolve("src/icons"))
      .end()
      .use("svg-sprite-loader")
      .loader("svg-sprite-loader")
      .options({
        symbolId: "icon-[name]",
      })
      .end();

    // 生产环境优化配置
    config.when(process.env.NODE_ENV !== "development", (config) => {
      // 内联 runtime.js，提升性能
      config
        .plugin("ScriptExtHtmlWebpackPlugin")
        .after("html")
        .use("script-ext-html-webpack-plugin", [
          {
            // 必须与 runtimeChunk 名称一致，默认为 "runtime"
            inline: /runtime\..*\.js$/,
          },
        ])
        .end();
      // 代码分割配置
      config.optimization.splitChunks({
        chunks: "all",
        cacheGroups: {
          // 第三方库分割
          libs: {
            name: "chunk-libs",
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: "initial", // 仅分割初始依赖的第三方库
          },
          // ElementUI 单独打包
          elementUI: {
            name: "chunk-elementUI",
            priority: 20, // 权重需大于 libs 和 app，否则会打包到其他 chunk
            test: /[\\/]node_modules[\\/]_?element-ui(.*)/, // 兼容 cnpm
          },
          // 公共组件分割
          commons: {
            name: "chunk-commons",
            test: resolve("src/components"), // 可自定义规则
            minChunks: 3, // 最小公共引用次数
            priority: 5,
            reuseExistingChunk: true,
          },
        },
      });
      // 提取 runtime 代码为单独文件
      config.optimization.runtimeChunk("single");
    });
  },
};
