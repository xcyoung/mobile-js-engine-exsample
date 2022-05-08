const express = require('express')
const path = require('path')

const app = express()

app.listen(8082, () => {
    console.log('running at http://127.0.0.1');
})

app.get('/config', (req, res) => {
    res.sendFile(path.resolve(__dirname, './Config.json'))
})

app.get('/script/*', (req, res) => {
    let url = req.originalUrl
    url = url.replace('/script', path.resolve(__dirname, '../dist/'))
    res.sendFile(url)
})