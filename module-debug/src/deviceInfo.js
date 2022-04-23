export function loadDeviceInfo() {
    let ua = navigator.userAgent.toLowerCase();
    let sys = {}
    if (ua.match(/chrome\/([\d.]+)/)) {
        sys = 'Android'
    } else if (ua.match(/version\/([\d.]+).*safari/)) {
        sys = 'iOS'
    }

    const platform = sys
    return {
        platform: () => {
            return platform
        }
    }
}