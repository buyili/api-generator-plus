module.exports = {
    title: 'Api Generator Plus',
    description: '',
    themeConfig: {
        nav: [
            { text: '指南', link: '/guide/' },
            { text: '配置', link: '/config/' },
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
                        'api-generator-plus',
                    ]
                },
                '', /* /guide/ */
                'api-generator-plus',
            ],
            '/config/':[
                // {
                //     title: '生成YApi接口',
                //     // path: '/foo/',      // 可选的, 标题的跳转链接，应为绝对路径且必须存在
                //     collapsable: false, // 可选的, 默认值是 true,
                //     sidebarDepth: 1,    // 可选的, 默认值是 1
                //     children: [
                //         '',
                //         'api-generator-plus',
                //     ]
                // },
                '', /* /config/ */
                // 'api-generator-plus',
            ],
        }
    }
  }