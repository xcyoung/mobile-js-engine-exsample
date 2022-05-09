export default {
    loadDeviceInfo: function (platform: string) {
        global['deviceInfo'] = {
            platform: () => { return platform }
        }
    }
}