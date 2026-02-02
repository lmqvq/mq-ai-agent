import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
    {
        path: '/',
        name: 'Landing',
        component: () => import('../views/Landing.vue'),
        meta: {
            title: 'AI健身教练 - 让科学健身触手可及'
        }
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: {
            title: '主页 - AI健身教练'
        }
    },
    {
        path: '/fitness',
        name: 'FitnessMaster',
        component: () => import('../views/FitnessMaster.vue'),
        meta: {
            title: 'AI健身教练'
        }
    },
    {
        path: '/agent',
        name: 'SuperAgent',
        component: () => import('../views/SuperAgent.vue'),
        meta: {
            title: 'AI超级智能体'
        }
    },
    {
        path: '/profile',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue'),
        meta: {
            title: '个人中心 - AI健身教练'
        }
    },
    {
        path: '/data',
        name: 'FitnessData',
        component: () => import('../views/FitnessData.vue'),
        meta: {
            title: '健身数据 - AI健身教练'
        }
    },
    {
        path: '/knowledge',
        name: 'FitnessKnowledge',
        component: () => import('../views/FitnessKnowledge.vue'),
        meta: {
            title: '健身知识库 - AI健身教练'
        }
    },
    {
        path: '/ranking',
        name: 'FitnessRanking',
        component: () => import('../views/FitnessRanking.vue'),
        meta: {
            title: '健身排行榜 - AI健身教练'
        }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: {
            title: '登录 - AI健身教练'
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: {
            title: '注册 - AI健身教练'
        }
    },
    {
        path: '/admin/knowledge',
        name: 'KnowledgeManage',
        component: () => import('../views/KnowledgeManage.vue'),
        meta: {
            title: '知识库管理 - AI健身教练',
            requiresAdmin: true
        }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局前置守卫设置页面标题和权限检查
router.beforeEach((to, from, next) => {
    if (to.meta.title) {
        document.title = to.meta.title
    }
    
    // 管理员权限检查
    if (to.meta.requiresAdmin) {
        const userStr = localStorage.getItem('user_info')
        if (!userStr) {
            next({ name: 'Login', query: { redirect: to.fullPath } })
            return
        }
        try {
            const user = JSON.parse(userStr)
            if (user.userRole !== 'admin') {
                next({ name: 'Home' })
                return
            }
        } catch {
            next({ name: 'Login', query: { redirect: to.fullPath } })
            return
        }
    }
    
    next()
})

export default router 