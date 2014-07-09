var exec = require('cordova/exec');

var myDatecsSDK = {
    platforms: ['android'],

    isSupported: function() {
        if (window.device) {
            var platform = window.device.platform;
            if ((platform !== undefined) && (platform !== null)) {
                return (this.platforms.indexOf(platform.toLowerCase()) >= 0);
            }
        }
        return false;
    },

    connect: function(address, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'connect', [address]);
    },

    disconnect: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'disconnect', []);
    },

    reset: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'reset', []);
    },

    printText: function(text, charset, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'printText', [text, charset]);
    },

    printTaggedText: function(text, charset, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'printTaggedText', [text, charset]);
    },

    feedPaper: function(lines, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'feedPaper', [lines]);
    },

    flush: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'flush', []);
    },

    selectPageMode: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'selectPageMode', []);
    },

    selectStandardMode: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'selectStandardMode', []);
    },

    printPage: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'printPage', []);
    },

    setAlign: function(align, onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'setAlign', [align]);
    },

    printSelfTest: function(onSuccess, onError) {
        exec(onSuccess, onError, 'MyDatecsSDK', 'printSelfTest', []);
    }
};

module.exports = myDatecsSDK;
