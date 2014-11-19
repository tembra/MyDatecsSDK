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
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'connect', [address]);
        }, 1000);
    },

    disconnect: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'disconnect', []);
        }, 1000);
    },

    init: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'init', []);
        }, 1000);
    },

    finish: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'finish', []);
        }, 1000);
    },

    reset: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'reset', []);
        }, 1000);
    },

    printText: function(text, charset, onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'printText', [text, charset]);
        }, 1000);
    },

    printTaggedText: function(text, charset, onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'printTaggedText', [text, charset]);
        }, 1000);
    },

    feedPaper: function(lines, onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'feedPaper', [lines]);
        }, 1000);
    },

    flush: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'flush', []);
        }, 1000);
    },

    selectPageMode: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'selectPageMode', []);
        }, 1000);
    },

    selectStandardMode: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'selectStandardMode', []);
        }, 1000);
    },

    printPage: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'printPage', []);
        }, 1000);
    },

    setAlign: function(align, onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'setAlign', [align]);
        }, 1000);
    },

    printSelfTest: function(onSuccess, onError) {
        setTimeout(function() {
            exec(onSuccess, onError, 'MyDatecsSDK', 'printSelfTest', []);
        }, 1000);
    }
};

module.exports = myDatecsSDK;
