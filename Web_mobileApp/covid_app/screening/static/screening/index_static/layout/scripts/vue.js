

const Home = {
    template: '<title>Accueil</title>',
    name: 'Home'
}
const statistiques = {
    template: '<title>statistiques</title>',
    name: 'statisiques'
}
const forum = {
    template: '<title>forum</title>', 
    name: 'forum'
}
const contact = {
    template: '<title>contact</title>',
    name: 'contact'
}

//router
const router = new VueRouter({
    routes: [ 
        { path: '/', component: Home, name: 'Home'},
        { path: '/statistiques', component: statistiques, name: 'statistiques'},
        { path: '/forum', component: forum, name: 'forum'},
        { path: '/contact', component: contact, name: 'forum'},

    ]

}) 

const vue = new vue({
    el:"#app", 
    router
}).$mount('#app')