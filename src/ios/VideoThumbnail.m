/********* VideoThumbnail.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVAsset.h>

#import <MediaPlayer/MediaPlayer.h>
@interface VideoThumbnail : CDVPlugin {
    // Member variables go here.
}

- (void)buildThumbnail:(CDVInvokedUrlCommand*)command;
@end

@implementation VideoThumbnail

- (void)buildThumbnail:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    __block NSString* payload = nil;

    NSString* videoPath = [command.arguments objectAtIndex:0];
    NSString* saveFolder = [command.arguments objectAtIndex:4];
    if(!videoPath ||videoPath.length==0){
        payload=@"videoPath was wrong";
        pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:payload];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    if(!saveFolder || saveFolder.length==0){
        payload=@"saveFolder was wrong";
        pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:payload];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    if(![saveFolder hasSuffix:@"/"]){
        saveFolder=[NSString stringWithFormat:@"%@/",saveFolder];
    }
    NSTimeInterval  now=   [NSDate date].timeIntervalSince1970;
    NSString* nowString=[NSString stringWithFormat:@"%.0f",now];
    NSString * savePath=[NSString stringWithFormat:@"%@thumbnail_%@.jpg",saveFolder,nowString];
    [self.commandDelegate runInBackground:^{
        if(extractVideoThumbnail(videoPath, savePath)){
            payload=savePath;
        }else{
            payload=@"Could not save thumbnail";
        }
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:payload];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
    
}

BOOL extractVideoThumbnail ( NSString *theSourceVideoName,
                            NSString *theTargetImageName )
{
    
    UIImage *thumbnail;
    NSURL *url;
    NSString *revisedTargetImageName = [[theTargetImageName stringByReplacingOccurrencesOfString:@"file://" withString:@""] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    
    if ( [theSourceVideoName rangeOfString:@"://"].location == NSNotFound )
    {
        url = [NSURL URLWithString:[[@"file://localhost" stringByAppendingString:theSourceVideoName]
                                    stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }
    else
    {
        url = [NSURL URLWithString:[theSourceVideoName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }
    
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:[NSURL fileURLWithPath:theSourceVideoName] options:nil];
    AVAssetImageGenerator *gen = [[AVAssetImageGenerator alloc] initWithAsset:asset];
    gen.appliesPreferredTrackTransform = YES;
    CMTime time = CMTimeMakeWithSeconds(0.0, 600);
    NSError *error = nil;
    CMTime actualTime;
    CGImageRef image = [gen copyCGImageAtTime:time actualTime:&actualTime error:&error];
    thumbnail = [[UIImage alloc] initWithCGImage:image];
    CGImageRelease(image);
    // write out the thumbnail; a return of NO will be a failure.
    return [UIImageJPEGRepresentation ( thumbnail, 1.0) writeToFile:revisedTargetImageName atomically:YES];
}

@end
