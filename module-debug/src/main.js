import Vue from 'vue'
import App from './App.vue'
import { loadDeviceInfo } from './deviceInfo'
import { loadDB } from './db'

Vue.config.productionTip = false

loadDB({
  dbPath: './db/test.db'
}).then((db) => {
  global['database'] = db
  console.log('load db complete')
})

global['deviceInfo'] = loadDeviceInfo()
console.log('load deviceInfo complete')

new Vue({
  render: h => h(App),
}).$mount('#app')
