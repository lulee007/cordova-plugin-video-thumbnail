# cordova-plugin-video-thumbnail
获取本地视频的缩略图
### Using the plugin
`videoThumbnail.build`
params:
    * success callback function
    * error callback function
    * configs object
```javascript
window.plugins.videoThumbnail.build(
      function (data) {
        console.log(data);
        $scope.thumbnail=data;
        $scope.$apply();
      }, function (error) {
        console.log(error);
      },
      {
        videoPath:"/Users/xxx/Library/Developer/CoreSimulator/Devices/7ECDF8F2-B5DC-4F10-BB1F-FF4FA3757BC1/data/Containers/Data/Application/BA3DD2ED-6EF5-4E41-943E-4232CC52D950/Documents/video1.mov",
        width:100,
        height:100,
        kind:1//android only
      });
```
### Installing the plugin
```bash
   cordova plugin add https://github.com/lulee007/cordova-plugin-video-thumbnail.git
```
