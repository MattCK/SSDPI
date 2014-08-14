/**
 * @file
 * Fully-commented example intended to both demonstrate how to extend the Ooyala
 * API while also documenting it.
 * 
 * This is the part of this documentation project. Improvements and suggestions
 * always welcome!
 * 
 * @author Ændrew Rininsland (@aendrew)
 * @see http://aendrew.github.io/ooyala-documentation
 */

"use strict"; // All examples should pass lint!

/**
 * Defines the player module.
 * 
 * @param plugin_namespace The namespace of the plugin.
 * 
 * The callback takes the following parameters:
 * 
 * @param OO The broader Ooyala library object.
 * @param _  The version of Underscore.js that comes with Ooyala.
 * @param $$ The version of jQuery that comes with Ooyala.
 * @param W  The global window object. I don't know why this is here... "window" is already global! #itisamystery
 * 
 * @note Ooyala's documentation says to supply Ooyala's jQuery as "$", however,
 * doing so will override your jQuery's "$" namespace within the plugin closure.
 * If you've added any jQuery plugins (Transit.js has been added to this project
 * as an example), the "$" object supplied by Ooyala won't be able to access any
 * of them. As such -- and given Ooyala won't give us an option to trim out any
 * unneeded libraries from the player code download -- Ooyala jQuery has been
 * supplied as "$$". This has the benefit of leaving Ooyala's dependencies in
 * place while leaving "$" mapped to the developer-supplied jQuery.
 * 
 */

OO.plugin("ExampleOoyalaModule", function (OO, _, $$, W) {
  var Example = {};
  
  /**
   * Constructor. Called during instantiation in OO.Player.create().
   * 
   * @param mb This is the message bus object. You subscribe to hear events.
   * @param id This is unique ID, supposedly to help with debugging. #itisamystery
   */
  Example.ExampleOoyalaModule = function(mb, id) {
    this.mb;
    this.id = id;
    this.init();
  };
  
  /**
   * Public functions. 
   */
  Example.ExampleOoyalaModule.prototype = {
    /**
     * Bind OO.EVENTS to public functions.
     * 
     * Note that this uses Underscore.js bind() as supplied by Ooyala.
     * If wanting to use own version of Underscore, change the reference in the
     * OO.plugin() function declaration.
     * 
     * Note that the "subscriber" has been called "ExampleModule". My guess is
     * that you could call it the same as the plugin namespace ("ExampleOoyalaModule"),
     * but I really don't care to verify that at the moment. Someone else care to?
     * 
     * @todo Improve last paragraph.
     * 
     * @remark Don't worry if this looks daunting. This just binds ALL the 
     *         messagebus events. You don't need to call your methods 
     *         onSomething(), though I have for simplicity. Also, I've given the
     *         official Ooyala description as the second line of each comment, 
     *         in quotes.
     * 
     * @see http://support.ooyala.com/developers/documentation/api/player_v3_api_events.html
     * @sa http://underscorejs.org/#bind
     */
    init: function () {
      this.mb.subscribe(OO.EVENTS.ADS_CLICK, 'ExampleModule', _.bind(this.onAdsClick, this));
      this.mb.subscribe(OO.EVENTS.ADS_PLAYED, 'ExampleModule', _.bind(this.onAdsPlayed, this));
      this.mb.subscribe(OO.EVENTS.AD_AUTHORIZATION_FETCHED, 'ExampleModule', _.bind(this.onAdAuthorizationFetched, this));
      this.mb.subscribe(OO.EVENTS.AD_CONFIG_READY, 'ExampleModule', _.bind(this.onAdConfigReady, this));
      this.mb.subscribe(OO.EVENTS.AUTHORIZATION_FETCHED, 'ExampleModule', _.bind(this.onAuthorizationFetched, this));
      this.mb.subscribe(OO.EVENTS.BUFFERED, 'ExampleModule', _.bind(this.onBuffered, this));
      this.mb.subscribe(OO.EVENTS.BUFFERING, 'ExampleModule', _.bind(this.onBuffering, this));
      this.mb.subscribe(OO.EVENTS.CHANGE_VOLUME, 'ExampleModule', _.bind(this.onChangeVolume, this));
      this.mb.subscribe(OO.EVENTS.CONTENT_TREE_FETCHED, 'ExampleModule', _.bind(this.onContentTreeFetched, this));
      this.mb.subscribe(OO.EVENTS.CONTROLS_HIDDEN, 'ExampleModule', _.bind(this.onControlsHidden, this));
      this.mb.subscribe(OO.EVENTS.CONTROLS_SHOWN, 'ExampleModule', _.bind(this.onControlsShown, this));
      this.mb.subscribe(OO.EVENTS.DESTROY, 'ExampleModule', _.bind(this.onDestroy, this));
      this.mb.subscribe(OO.EVENTS.DOWNLOADING, 'ExampleModule', _.bind(this.onDownloading, this));
      this.mb.subscribe(OO.EVENTS.EMBED_CODE_CHANGED, 'ExampleModule', _.bind(this.onEmbedCodeChanged, this));
      this.mb.subscribe(OO.EVENTS.ERROR, 'ExampleModule', _.bind(this.onError, this));
      this.mb.subscribe(OO.EVENTS.FIRST_AD_FETCHED, 'ExampleModule', _.bind(this.onFirstAdFetched, this));
      this.mb.subscribe(OO.EVENTS.FULLSCREEN_CHANGED, 'ExampleModule', _.bind(this.onFullscreenChanged, this));
      this.mb.subscribe(OO.EVENTS.METADATA_FETCHED, 'ExampleModule', _.bind(this.onMetadataFetched, this));
      this.mb.subscribe(OO.EVENTS.MIDROLL_PLAY_FAILED, 'ExampleModule', _.bind(this.onMidrollPlayFailed, this));
      this.mb.subscribe(OO.EVENTS.MIDROLL_STREAM_PLAYED, 'ExampleModule', _.bind(this.onMidrollStreamPlayed, this));
      this.mb.subscribe(OO.EVENTS.PAUSE, 'ExampleModule', _.bind(this.onPause, this));
      this.mb.subscribe(OO.EVENTS.PAUSED, 'ExampleModule', _.bind(this.onPaused, this));
      this.mb.subscribe(OO.EVENTS.PAUSE_STREAM, 'ExampleModule', _.bind(this.onPauseStream, this));
      this.mb.subscribe(OO.EVENTS.PLAY, 'ExampleModule', _.bind(this.onPlay, this));
      this.mb.subscribe(OO.EVENTS.PLAYBACK_READY, 'ExampleModule', _.bind(this.onPlaybackReady, this));
      this.mb.subscribe(OO.EVENTS.PLAYED, 'ExampleModule', _.bind(this.onPlayed, this));
      this.mb.subscribe(OO.EVENTS.PLAYER_CREATED, 'ExampleModule', _.bind(this.onPlayerCreated, this));
      this.mb.subscribe(OO.EVENTS.PLAYHEAD_TIME_CHANGED, 'ExampleModule', _.bind(this.onPlayheadTimeChanged, this));
      this.mb.subscribe(OO.EVENTS.PLAYING, 'ExampleModule', _.bind(this.onPlaying, this));
      this.mb.subscribe(OO.EVENTS.PLAY_FAILED, 'ExampleModule', _.bind(this.onPlayFailed, this));
      this.mb.subscribe(OO.EVENTS.PLAY_MIDROLL_STREAM, 'ExampleModule', _.bind(this.onPlayMidrollStream, this));
      this.mb.subscribe(OO.EVENTS.PLAY_STREAM, 'ExampleModule', _.bind(this.onPlayStream, this));
      this.mb.subscribe(OO.EVENTS.PRELOAD_STREAM, 'ExampleModule', _.bind(this.onPreloadStream, this));
      this.mb.subscribe(OO.EVENTS.SEEK, 'ExampleModule', _.bind(this.onSeek, this));
      this.mb.subscribe(OO.EVENTS.SEEK_STREAM, 'ExampleModule', _.bind(this.onSeekStream, this));
      this.mb.subscribe(OO.EVENTS.SET_EMBED_CODE, 'ExampleModule', _.bind(this.onSetEmbedCode, this));
      this.mb.subscribe(OO.EVENTS.SINGLE_AD_PLAYED, 'ExampleModule', _.bind(this.onSingleAdPlayed, this));
      this.mb.subscribe(OO.EVENTS.SIZE_CHANGED, 'ExampleModule', _.bind(this.onSizeChanged, this));
      this.mb.subscribe(OO.EVENTS.STREAM_PAUSED, 'ExampleModule', _.bind(this.onStreamPaused, this));
      this.mb.subscribe(OO.EVENTS.STREAM_PLAYED, 'ExampleModule', _.bind(this.onStreamPlayed, this));
      this.mb.subscribe(OO.EVENTS.STREAM_PLAYING, 'ExampleModule', _.bind(this.onStreamPlaying, this));
      this.mb.subscribe(OO.EVENTS.STREAM_PLAY_FAILED, 'ExampleModule', _.bind(this.onStreamPlayFailed, this));
      this.mb.subscribe(OO.EVENTS.VOLUME_CHANGED, 'ExampleModule', _.bind(this.onVolumeChanged, this));
      this.mb.subscribe(OO.EVENTS.WILL_CHANGE_FULLSCREEN, 'ExampleModule', _.bind(this.onWillChangeFullscreen, this));
      this.mb.subscribe(OO.EVENTS.WILL_FETCH_ADS, 'ExampleModule', _.bind(this.onWillFetchAds, this));
      this.mb.subscribe(OO.EVENTS.WILL_FETCH_AD_AUTHORIZATION, 'ExampleModule', _.bind(this.onWillFetchAdAuthorization, this));
      this.mb.subscribe(OO.EVENTS.WILL_FETCH_AUTHORIZATION, 'ExampleModule', _.bind(this.onWillFetchAuthorization, this));
      this.mb.subscribe(OO.EVENTS.WILL_FETCH_CONTENT_TREE, 'ExampleModule', _.bind(this.onWillFetchContentTree, this));
      this.mb.subscribe(OO.EVENTS.WILL_FETCH_METADATA, 'ExampleModule', _.bind(this.onWillFetchMetadata, this));
      this.mb.subscribe(OO.EVENTS.WILL_PLAY, 'ExampleModule', _.bind(this.onWillPlay, this));
      this.mb.subscribe(OO.EVENTS.WILL_PLAY_ADS, 'ExampleModule', _.bind(this.onWillPlayAds, this));
      this.mb.subscribe(OO.EVENTS.WILL_PLAY_FROM_BEGINNING, 'ExampleModule', _.bind(this.onWillPlayFromBeginning, this));
      this.mb.subscribe(OO.EVENTS.WILL_PLAY_SINGLE_AD, 'ExampleModule', _.bind(this.onWillPlaySingleAd, this));
      this.mb.subscribe(OO.EVENTS.WILL_PLAY_STREAM, 'ExampleModule', _.bind(this.onWillPlayStream, this));
      this.mb.subscribe(OO.EVENTS.WILL_RESUME_MAIN_VIDEO, 'ExampleModule', _.bind(this.onWillResumeMainVideo, this));
      this.mb.subscribe(OO.EVENTS.WILL_SHOW_COMPANION_ADS, 'ExampleModule', _.bind(this.onWillShowCompanionAds, this));
    },
    /**
     * Handles the ADS_CLICK event.
     * 
     * "An ad has been clicked."
     * 
     * @param event The name of the event.
     * 
     * @note Triggers an ad analytics "AD_CLICK" event.
     */
    onAdsClick: function(event){
      console.log('Ad clicked! Normally would load in a new window now.');
    },
    
    /**
     * Handles the ADS_PLAYED event.
     * 
     * "A set of ads have played."
     * 
     * @param event The name of the event.
     * @param ad_duration The duration of the ad.
     * @param item_id The ID of the item to play.
     * 
     * @remark Assuming by "context" they mean "argument position". 
     * @todo Please verify the second and third parameters.
     */
    onAdsPlayed: function(event, ad_duration, item_id){
      console.log('A set of ads has played.');
    },
    
    /**
     * Handles the AD_AUTHORIZATION_FETCHED event.
     * 
     * "Ad authorization data was received."
     * 
     * @note Ad authorization data is passed within this event.
     * 
     * @param event The name of the event.
     * @param ads IDs of ads affected by authorization.
     */
    onAdAuthorizationFetched: function(event, ads){
      console.log("The following ads were authorised:");
      console.dir(ads);
    },
    
    /**
     * Handles the AD_CONFIG_READY event.
     * 
     * "An ad set configuration is ready for insertion and/or play."
     * 
     * @param event The name of the event.
     * @param loader Depending on context, a pointer to the ad loader.
     * 
     * @remark Seriously, WTF does "depending on context" mean?
     */
    onAdConfigReady: function(event, loader){
      console.log('Ad config is ready.');
    },
    
    /**
     * Handles the AUTHORIZATION_FETCHED event.
     * 
     * "Playback was authorized."
     * 
     * @param event The name of the event.
     * @param id ID of authorization for asset.
     */
    onAuthorizationFetched: function(event, id){
      console.log('Authorisation fetched for: ' + id);
    },
    
    /**
     * Handles the BUFFERED event.
     * 
     * "Play resumes because player has completed buffering."
     * 
     * @param event The name of the event.
     * @param url The URL of the stream.
     */
    onBuffered: function(event, url){
      console.log('Buffering complete, play resuming at: ' + url);
    },
    
    /**
     * Handles the BUFFERING event.
     * 
     * "The player is buffering the data stream."
     * 
     * @param event The name of the event.
     * @param url The URL of the stream.
     */
    onBuffering: function(event, url){},
    
    /**
     * Handles the CHANGE_VOLUME event.
     * 
     * "A request to change volume has been made."
     * 
     * @param event The name of the event.
     * @param level The (new?) volume level (Likely as a percentage?)
     * 
     * @todo Verify volume is a percentage and it's the requested new value.
     */
    onChangeVolume: function(event, level){
      console.log('Volume changed requested to: ' + level);
    },
    
    /**
     * Handles the CONTENT_TREE_FETCHED event.
     * 
     * "A content tree was fetched."
     * 
     * @param event The name of the event.
     * @param content The current asset
     * @param ids The IDs of all affected assets.
     * 
     * @remark I may be wrong about the last parameter. Could somebody please 
     *         verify for me?
     *         
     * @todo Confirm that the "content" and "ids" parameters are correct.
     * 
     * @note Records a "display".
     * @sa http://support.ooyala.com/developers/documentation/concepts/analytics_plays-and-displays.html
     */
    onContentTreeFetched: function(event, content, ids){
      console.log('Content tree fetched for asset: ' + content);
    },
    
    /**
     * Handles the CONTROLS_HIDDEN event.
     * 
     * "Controls are hidden."
     * 
     * @param event The name of the event.
     */
    onControlsHidden: function(event){
      console.log('Controls have been hidden.');
    },
    
    /**
     * Handles the CONTROLS_SHOWN event.
     * 
     * "Controls are shown."
     * 
     * @param event The name of the event.
     */
    onControlsShown: function(event){
      console.log('Controls have been shown.');
    },
    
    /**
     * Handles the DESTROY event.
     * 
     * "The player is currently being destroyed and anything created by your module
     *  needs to be deleted as well. After the destruction is complete, there is
     *  nothing left to send an event."
     *  
     * @remark This was driving me nuts for awhile. Note that you can't do 
     *         anything else after you've called player.destroy(). For some 
     *         reason, I was attempting to do that before loading a new video, 
     *         which wasn't working due to nothing being there to receive the 
     *         request. To load a new video, use OO.Player.setEmbedCode();
     * 
     * @param event The name of the event.
     * 
     * @note Destroying is done via OO.Player's destroy() method.
     */
    onDestroy: function(event){
      console.log('Y U NO LEIK OOYOOYA?!?!?!!????');
    },
    
    /**
     * Handles the DOWNLOADING event.
     * 
     * "Player is downloading content (could be playing while downloading)."
     * 
     * @param event The name of the event.
     * @param time The time of the event.
     */
    onDownloading: function(event, time){
      console.log('Content downloaded at: ' + time);
    },
    
    /**
     * Handles the EMBED_CODE_CHANGED event.
     * 
     * "The player's embed code has changed."
     * 
     * @param event The name of the event.
     * @param something "ID (embed code) of asset with options," whatever that 
     *        means. #itisamystery
     */
    onEmbedCodeChanged: function(event, something){
      console.log('Embed code has been changed!');
      console.log('Here\'s a dump of that confusing second param: ');
      console.dir(something);
    },
    
    /**
     * Handles the ERROR event.
     * 
     * "An error has occured" [sic]
     * 
     * @param event The name of the event.
     * @param error_code The error code.
     */
    onError: function(event, error_code){
      console.log('ERMAHGERRRRD! THURRS BERN AHN EHHHROAAAR!');
    },
    
    /**
     * Handles the FIRST_AD_FETCHED event.
     * 
     * "The first in a set of multiple ads was fetched."
     * 
     * @param event The name of the event.
     */
    onFirstAdFetched: function(event){
      console.log('Oh snap! HERE COMES THE FIRST AD!');
    },
    
    /**
     * Handles the FULLSCREEN_CHANGED event.
     * 
     * "The fullscreen state has changed."
     * 
     * @param event The name of the event.
     * @param isFullscreen Fullscreen state as boolean.
     * @param isPaused Pause state as boolean.
     * 
     * @note This is another one of those "depending on context" events, in that
     *       it seems the third param may or may not be included. 
     *       When? When not? #itisamystery
     */
    onFullscreenChanged: function(event, isFullscreen, isPaused){
      console.log('Fullscreen changed! isFullScreen? ' + isFullscreen + ' isPaused? ' + isPaused);
    },
    
    /**
     * Handles the METADATA_FETCHED event.
     * 
     * "The metadata (typically set in Backlot) was retrieved."
     * 
     * @param event The name of the event.
     * @param embedcode ID of asset.
     */
    onMetadataFetched: function(event, embedcode){
      console.log('Metadata fetched for' + embedcode);
    },
    
    /**
     * Handles the MIDROLL_PLAY_FAILED event.
     * 
     * "An attempt to play a midroll ad has failed."
     * 
     * @param event The name of the event.
     * @param args The arguments that were passed in.
     */
    onMidrollPlayFailed: function(event, args){
      console.log('Boooo! Midroll play FAAAAAAILED! Here are the args passed in: ');
      console.dir(args);
    },
    
    /**
     * Handles the MIDROLL_STREAM_PLAYED event.
     * 
     * "A midroll stream was played."
     * 
     * @param event The name of the event.
     */
    onMidrollStreamPlayed: function(event){
      console.log('You just saw a midroll stream play!');
    },
    
    /**
     * Handles the PAUSE event.
     * 
     * "A player pause request has been made."
     * 
     * @param event The name of the event.
     * 
     * @note Pausing is done via OO.Player's pause() method.
     */
    onPause: function(event){
      console.log('Itsa pause!');
    },
    
    /**
     * Handles the PAUSED event.
     * 
     * "The player was paused."
     * 
     * @param event The name of the event.
     */
    onPaused: function(event){
      console.log('Itsa paused!');
      this.pause();
    },
    
    /**
     * Handles the PAUSE_STREAM event.
     * 
     * "An attempt to pause a video stream has occured." [sic]
     * 
     * @param event The name of the event.
     */
    onPauseStream: function(event){
      console.log('The stream issa pause!');
    },
    
    /**
     * Handles the PLAY event.
     * 
     * "A playback request has been made."
     * 
     * @param event The name of the event.
     * 
     * @note Playing is done via OO.Player's play() method.
     */
    onPlay: function(event){
      console.log('Itsa play!');
      this.play();
    },
    
    /**
     * Handles the PLAYBACK_READY event.
     * 
     * "The player has indicated that it is in a playback ready state. All 
     *  preparations are complete, and the player is ready to receive playback 
     *  commands (such as play, seek, and so forth). The default UI shows the 
     *  Play button (it displays the non-clickable spinner before this point)."
     * 
     * @note This is a good event to use if wanting to hook into the play/pause/seek functionality.
     * 
     * @param event The name of the event.
     */
    onPlaybackReady: function(event){
      console.log('Playback ready!');
      console.log('Now would be a good time to setup events and stuff!');
    },
    
    /**
     * Handles the PLAYED event.
     * 
     * "The video was played."
     * 
     * @param event The name of the event.
     * @param args The arguments that were passed in.
     */
    onPlayed: function(event, args){
      console.log('To quote Strong Bad: It\'s over!!');
      console.log('Those args were, btw: ');
      console.dir(args);
    },
    
    /**
     * Handles the PLAYER_CREATED event.
     * 
     * @param event The name of the event
     * @param elementId Element ID of player container.
     * @param args Parameters passed to player on creation.
     */
    onPlayerCreated: function(event, elementId, args){
      console.log('New player created at element #' + elementId);
      console.log('The args passed in were: ');
      console.dir(args);
    },
    
    /**
     * Handles the PLAYHEAD_TIME_CHANGED event.
     * 
     * @param event The name of the event.
     * @param time The current time.
     * @param duration The duration.
     * @param buffer The name of the buffer.
     * @param seek The seek range.
     * 
     * @remark This event fires continuously. Don't even think about setting up events here.
     */
    onPlayheadTimeChanged: function(event, time, duration, buffer, seek){
      console.log('TIME TRAVEL TIMEZ!');
      console.log('Current time: ' + time + ' Duration: ' + duration + ' Buffer name: ' + buffer + ' Seek range: ' + seek);
    },
    
    /**
     * Handles the PLAYING event.
     * 
     * "A video is playing."
     * 
     * @param event The name of the event.
     */
    onPlaying: function(event){
      console.log('We be playin\'!');
    },
    
    /**
     * Handles the PLAY_FAILED event.
     * 
     * "A request to play video has failed."
     * 
     * @param event
     */
    onPlayFailed: function(event){
      console.log('Play failed. Booooooo... yala!');
    },
    
    /**
     * Handles the PLAY_MIDROLL_STREAM event.
     * 
     * "A request to play a midroll stream was made."
     * 
     * @param event The name of the event.
     * @param time The current time.
     */
    onPlayMidrollStream: function(event, time){
      console.log('Playing midroll stream. Time: ' + time);
    },
    
    /**
     * Handles the PLAY_STREAM event.
     * 
     * "A notification occurred indicating that a video stream playback request
     *  was made."
     * 
     * @param event The name of the event.
     */
    onPlayStream: function(event){
      console.log('Video stream playback requested.');
    },
    
    /**
     * Handles the PRELOAD_STREAM event.
     * 
     * "A video stream is preloaded."
     * 
     * @param event The name of the event.
     * @param url The URL of the stream.
     */
    onPreloadStream: function(event, url){
      console.log('A stream has been preloaded from: ' + url);
    },
    
    /**
     * Handles the SEEK event.
     * 
     * "A request to perform a seek has occurred. The playhead is requested to
     *  move to a specific location in milliseconds.
     * 
     * @param event The name of the event.
     * @param seconds Number of seconds to seek from current position.
     * 
     * @note Seeking is done via the seek() method of OO.Player.
     *       
     * @todo Confirm "seconds" is relative from current time, is milliseconds.
     */
    onSeek: function(event, seconds){
      console.log('A SEEK IS SOUGHT! ' + seconds + '\'s worth!');
    },
    
    /**
     * Handles the SEEK_STREAM event.
     * 
     * "An attempt to move the playhead to a position in a video stream has 
     *  occurred."
     * 
     * @param event The name of the event.
     * @param seconds Number of seconds to seek from current position.
     * 
     * @todo Confirm "seconds" is relative from current time, is milliseconds.
     */
    onSeekStream: function(event, seconds){
      console.log('The playhead was moved ' + seconds + ' seconds!');
    },
    
    /**
     * Handles the SET_EMBED_CODE event.
     * 
     * "An attempt has been made to set an embed code."
     * 
     * @param event The name of the event.
     * @param embedcode The ID of the asset.
     * 
     * @note Second param can come with "options", based on context.
     * @todo Figure out what that context is, and whether "embedcode" is an
     * object in the second occurrence (or if options are a third param.). 
     */
    onSetEmbedCode: function(event, embedcode){
      console.log('New embed code set using value: ' + embedcode);
      console.log('In case the second param has options, here it is: ');
      console.dir(embedcode);
    },
    
    /**
     * Handles the SINGLE_AD_PLAYED event.
     * 
     * "Triggered after an individual ad (as opposed to a group of ads) has played."
     * 
     * @param event The name of the event.
     */
    onSingleAdPlayed: function(event){
      console.log('One lonely, solitary ad has played.');
    },
    
    /**
     * Handles the SIZE_CHANGED event.
     * 
     * "The screen sized was changed. This event can also be triggered by an
     *  orientation change for handheld devices."
     * 
     * @param event The name of the event.
     * @param width The width of the player.
     * @param height The height of the player.
     * 
     * @note I'm assuming the above is the NEW width/height.
     * @todo Confirm the above is the new width/height -- not the original.
     */
    onSizeChanged: function(event, width, height){
      console.log('HAI THURR! DUN BE ALL CHANGIN UP MAH SIZEN!');
      console.log('width: ' + width + ' height: ' + height);
    },
    /**
     * Handles the STREAM_PAUSED event.
     * 
     * "A video stream was paused."
     * 
     * @param event The name of the event.
     * @param something Either the current time or URL of the stream. Which? #itisamystery
     * 
     * @todo Seriously, figure out WTF would cause totally different second params.
     * @remark That said, I think it's "video is mid-play" vs. "video has completed".
     */
    onStreamPaused: function(event, something){
      console.log('The stream has been paused.');
      console.log('Mysterious second parameter is: ');
      console.dir(something);
    },
    
    /**
     * Handles the "STREAM_PLAYED event.
     * 
     * "A video stream has played."
     * 
     * @param event The name of the event.
     * @param something Either the current time or URL of the stream. Which? #itisamystery
     * 
     * @todo Seriously, figure out WTF would cause totally different second params.
     * @remark That said, I think it's "video is mid-play" vs. "video has completed".
     */
    onStreamPlayed: function(event, something){
      console.log('Stream played!');
      console.log('Mysterious second parameter:');
      console.dir(something);
    },
    
    /**
     * Handles the "STREAM_PLAYING" event.
     * 
     * "An individual video stream is playing."
     * 
     * @param event The name of the event.
     * @param url The URL of the stream.
     */
    onStreamPlaying: function(event, url){
      console.log('The stream is playing from URL: ' + url);
    },
    
    /**
     * Handles the STREAM_PLAY_FAILED event.
     * 
     * "An attempt to play a video stream has failed."
     * 
     * @param event The name of the event.
     * @param args The arguments that were passed in.
     */
    onStreamPlayFailed: function(event, args){
      console.log('Boooooyala! Stream play FAILED!');
    },
    
    /**
     * Handles the VOLUME_CHANGED event.
     * 
     * "The volume has changed."
     * 
     * @param event The name of the event.
     * @param level Current volume level.
     * 
     * @remarks I think "level" is "current" as of after the volume change.
     *          Further, I think it's a percentage, but don't quote me on that.
     * @todo Verify that "level" is a percentage. 
     */
    onVolumeChanged: function(event, level){
      console.log('Volume has been changed to level: ' + level);
    },
    
    /**
     * Handles the WILL_CHANGE_FULLSCREEN event.
     * 
     * "Triggered before a change was made to the full screen setting of the
     *  player screen."
     * 
     * @param event The name of the event.
     * @param schrodingersCat SOME RANDOM BOOLEAN! #ITISATOTALMYSTERYCHAPS
     * 
     * @remark My guess is the second param is whether the player is full-screen
     *         already or not. Seriously, the documentation says it is, depending
     *         on context, simply "either true or false." THANKS, OOYALA!
     */
    onWillChangeFullscreen: function(event, schrodingersCat){
      console.log('About to go fullscreen.');
      console.log('WTF *is* this param?');
      console.dir(schrodingersCat);
    },
    
    /**
     * Handles the WILL_FETCH_ADS event.
     * 
     * "Triggered before ads are fetched."
     * 
     * @param event The name of the event.
     */
    onWillFetchAds: function(event){
      console.log('About to go fetch me some ads!');
    },
    
    /**
     * Handles the WILL_FETCH_AD_AUTHORIZATION event.
     * 
     * "Triggered before the ad authorization is fetched."
     * 
     * @param event The name of the event.
     * @param id ID of requested ad.
     */
    onWillFetchAdAuthorization: function(event, id){
      console.log('About to go fetch me some ad auths! Id:' + id);
    },
    
    /**
     * Handles the WILL_FETCH_AUTHORIZATION event.
     * 
     * "Triggered before an authorization is fetched."
     * 
     * @param event The name of the event.
     * @param id The ID of authorization request.
     */
    onWillFetchAuthorization: function(event, id){
      console.log('About to go fetch me some authorizations! Id: ' + id);
    },
    
    /**
     * Handles the WILL_FETCH_CONTENT_TREE event.
     * 
     * "Triggered before the content_tree is retrieved.
     * 
     * @param event The name of the event.
     * @param api_request The passed-in API request.
     */
    onWillFetchContentTree: function(event, api_request){
      console.log('About to go fetch me some content trees! API request: ' + api_request);
    },
    
    /**
     * Handles the WILL_FETCH_METADATA event.
     * 
     * "Triggered before metadata is fetched for a video."
     * 
     * @param event The name of the event.
     * @param api_request The passed-in API request.
     */
    onWillFetchMetadata: function(event, api_request){
      console.log('About to go fetch me some metadata! API request: ' + api_request);
    },
    
    /**
     * Handles the WILL_PLAY event.
     * 
     * "Triggered before a video is played."
     * @param event The name of the event.
     */
    onWillPlay: function(event){
      console.log('About to go play me some content!');
    },
    
    /**
     * Handles the WILL_PLAY_ADS event.
     * 
     * "Triggered before an ad is played."
     * 
     * @param event The name of the event.
     * @param something Either duration of ad or ID of item to play.
     * 
     * @todo Figure out the contexts for param #2.
     */
    onWillPlayAds: function(event, something){
      console.log('About to go play me some ads!')
    },
    
    /**
     * Handles the WILL_PLAY_FROM_BEGINNING event.
     * 
     * "Triggered before a video is played from the starting point."
     * 
     * @param event The name of the event.
     * 
     * @note Seriously, I have no idea what this does.
     */
    onWillPlayFromBeginning: function(event){
      console.log('About to play from beginning or something!');
    },
    
    /**
     * Handles the WILL_PLAY_SINGLE_AD event.
     * 
     * "Triggered before an individual ad (as opposed to group of ads) is about
     *  to be played."
     * 
     * @param event The name of the event.
     */
    onWillPlaySingleAd: function(event){
      console.log('About to play a single, solitary, lonely-ass ad!');
    },
    
    /**
     * Handles the WILL_PLAY_STREAM event.
     * 
     * "Triggered before an individual video stream (one video, one ad)."
     * 
     * @param event The name of the event.
     * @param url The URL of the stream.
     */
    onWillPlayStream: function(event, url){
      console.log('About to play me some stream! Url: ' + url);
    },
    
    /**
     * Handles the WILL_RESUME_MAIN_VIDEO event.
     * 
     * "Triggered before the main video is resumed."
     * 
     * @param event The name of the event.
     */
    onWillResumeMainVideo: function(event){
      console.log('About to resume me some main video!');
    },
    
    /**
     * Handles the WILL_SHOW_COMPANION_ADS event.
     * 
     * "Triggered before the companion ads are shown.
     * 
     * @param event The name of the event.
     * @param ads Either IDs of all companion ads, or ID of a single companion ad.
     * 
     * @todo Figure out the context of param #2.
     */
    onWillShowCompanionAds: function(event, ads){
      console.log('About to play me some ads!');
    },
    
    /**
     * WTF is this, anyway? 
     * 
     * I don't know. But it needs to be there, apparently. #itisamystery
     */
    __end_marker: true
  };
  
  // Return the constructor. 
  return Example.ExampleOoyalaModule;
  
});