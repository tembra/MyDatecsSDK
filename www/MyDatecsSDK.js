var exec = require('cordova/exec');

var myDatecsSDK = {
    platforms: ['android'],

    isSupported = function() {
        if (window.device) {
            var platform = window.device.platform;
            if ((platform !== undefined) && (platform !== null)) {
                return (this.platforms.indexOf(platform.toLowerCase()) >= 0);
            }
        }
        return false;
    },

    connect = function(address, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'connect', [address]);
    },

    printText = function(text, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'printText', [text]);
    }
};

module.exports  = myDatecsSDK;
