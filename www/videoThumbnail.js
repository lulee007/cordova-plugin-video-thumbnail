/**
 * Created by luxiaohui on 15/10/22.
 */
/*
 According to apache license

 https://github.com/lulee007/cordova-plugin-video-thumbnail

 */

var exec = require('cordova/exec');

var videoThumbnail = {

    build: function(success, failure, config) {
        exec(success || function() {},
            failure || function() {},
            'VideoThumbnail',
            'buildThumbnail',
            [
                config.videoPath,
                config.width,
                config.height,
                config.kind
            ]);
    }


};

/* @Deprecated */
window.plugins = window.plugins || {};
window.plugins.videoThumbnail = videoThumbnail;

module.exports = videoThumbnail;
