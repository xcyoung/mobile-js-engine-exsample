import Vue from 'vue'
import App from './App.vue'
import { loadDeviceInfo } from './deviceInfo'
import { loadDB } from './db'

Vue.config.productionTip = false

loadDB({
  dbPath: './idt_basho_db_323285appplusappplus_6bf21efaa61a4e0e9c858ff9ae5e9a96'
}).then((db) => {
  global['db'] = db
  console.log('load db complete')
})

global['deviceInfo'] = loadDeviceInfo()
console.log('load deviceInfo complete')

new Vue({
  render: h => h(App),
}).$mount('#app')
