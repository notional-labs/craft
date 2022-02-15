(function( $ ) {
	'use strict';

	/**
	 * All of the code for your public-facing JavaScript source
	 * should reside in this file.
	 *
	 * Note: It has been assumed you will write jQuery code here, so the
	 * $ function reference has been prepared for usage within the scope
	 * of this function.
	 *
	 * This enables you to define handlers, for when the DOM is ready:
	 *
	 * $(function() {
	 *
	 * });
	 *
	 * When the window is loaded:
	 *
	 * $( window ).load(function() {
	 *
	 * });
	 *
	 * ...and/or other possibilities.
	 *
	 * Ideally, it is not considered best practise to attach more than a
	 * single DOM-ready or window-load handler for a particular page.
	 * Although scripts in the WordPress core, Plugins and Themes may be
	 * practising this, we should strive to set a better example in our own work.
	 */

	// Answer Sound Muter
	$(document).on('click', '.ays_music_sound', function() {
		var $this = $(this);
		var audioEl = $(document).find('.ays_pb_sound').get(0);
		if($this.hasClass('ays_sound_active')){
			audioEl.volume = 0;
			$this.find('.ays_pb_fa_volume').remove();
			$this.html(pbLocalizeObj.icons.volume_mute_icon);
			$this.find('.ays_pb_fa_volume').addClass('ays_pb_fa_volume_off').removeClass('ays_pb_fa_volume');
			$this.removeClass('ays_sound_active');
		} else {
			audioEl.volume = 1;
			$this.find('.ays_pb_fa_volume_off').remove();
			$this.html(pbLocalizeObj.icons.volume_up_icon);
			$this.find('.ays_pb_fa_volume_off').addClass('ays_pb_fa_volume').removeClass('ays_pb_fa_volume_off');
			$this.addClass('ays_sound_active');
		}
	});
})( jQuery );
