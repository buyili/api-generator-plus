module.exports = {
    title: 'Api Generator Plus',
    description: '',
    base: '/api-generator-plus/',
    themeConfig: {
        logo: '/assets/img/logo.png',
        nav: [
            { text: '指南', link: '/guide/' },
            { text: '配置', link: '/config/' },
            { text: 'v1.0.13', link: '/' },
            // {
            //     text: 'Languages',
            //     ariaLabel: 'Language Menu',
            //     items: [
            //         { text: 'Chinese', link: '/language/chinese/' },
            //         { text: 'Japanese', link: '/language/japanese/' }
            //     ]
            // }
        ],
        sidebar: {
            '/guide/':[
                {
                    title: '指南',
                    // path: '/foo/',      // 可选的, 标题的跳转链接，应为绝对路径且必须存在
                    collapsable: false, // 可选的, 默认值是 true,
                    sidebarDepth: 1,    // 可选的, 默认值是 1
                    children: [
                        '', /* /guide/ */
                        // 'api-generator-plus',
                    ]
                },
                // '', /* /guide/ */
                // 'api-generator-plus',
            ],
            // '/config/':[
            //     // {
            //     //     title: '配置',
            //     //     // path: '/foo/',      // 可选的, 标题的跳转链接，应为绝对路径且必须存在
            //     //     collapsable: false, // 可选的, 默认值是 true,
            //     //     // sidebarDepth: 1,    // 可选的, 默认值是 1
            //     //     children: [
            //     //         '',
            //     //     ]
            //     // },
            //     '', /* /config/ */
            //     // 'api-generator-plus',
            // ],
        },
        // 假定是 GitHub. 同时也可以是一个完整的 GitLab URL
        repo: 'buyili/api-generator-plus',
        // 自定义仓库链接文字。默认从 `themeConfig.repo` 中自动推断为
        // "GitHub"/"GitLab"/"Bitbucket" 其中之一，或是 "Source"。
        repoLabel: 'GitHub',

        // 以下为可选的编辑链接选项

        // 假如你的文档仓库和项目本身不在一个仓库：
        docsRepo: 'buyili/api-generator-plus',
        // 假如文档不是放在仓库的根目录下：
        docsDir: 'docs',
        // 假如文档放在一个特定的分支下：
        docsBranch: 'master',
        // 默认是 false, 设置为 true 来启用
        editLinks: true,
        // 默认为 "Edit this page"
        editLinkText: '帮助我们改善此页面！',

        lastUpdated: 'Last Updated', // string | boolean
    }
  }
