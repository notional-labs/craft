;(function ($) {
	'use strict';

	var $window = $(window)

	$.fn.getHappySettings = function() {
		return this.data('happy-settings');
	};

	function debounce(func, wait, immediate) {
		var timeout;
		return function() {
			var context = this, args = arguments;
			var later = function() {
				timeout = null;
				if (!immediate) func.apply(context, args);
			};
			var callNow = immediate && !timeout;
			clearTimeout(timeout);
			timeout = setTimeout(later, wait);
			if (callNow) func.apply(context, args);
		};
	}

	function initFilterNav($scope, filterFn) {
		var $filterNav = $scope.find('.hajs-filter'),
			defaultFilter = $filterNav.data('default-filter');

		if ($filterNav.length) {
			$filterNav.on('click.onFilterNav', 'button', function(event) {
				event.stopPropagation();

				var $current = $(this);
				$current
					.addClass('ha-filter__item--active')
					.siblings()
					.removeClass('ha-filter__item--active');
				filterFn($current.data('filter'));
			});

			$filterNav.find('[data-filter="'+defaultFilter+'"]').click();
		}
	}

	/**
	 * Initialize magnific lighbox gallery
	 *
	 * @param {$element, selector, isEnabled, key} settings
	 */
	function initPopupGallery(settings) {
		settings.$element.on('click', settings.selector, function(event) {
			event.preventDefault();
		});

		if ( ! $.fn.magnificPopup ) {
			return;
		}

		if ( ! settings.isEnabled ) {
			$.magnificPopup.close();
			return;
		}

		var windowWidth = $(window).width(),
			mobileWidth = elementorFrontendConfig.breakpoints.md,
			tabletWidth = elementorFrontendConfig.breakpoints.lg;

		settings.$element.find(settings.selector).magnificPopup({
			key: settings.key,
			type: 'image',
			image: {
				titleSrc: function(item) {
					return item.el.attr('title') ? item.el.attr('title') : item.el.find('img').attr('alt');
				}
			},
			gallery: {
				enabled: true,
				preload: [1,2]
			},
			zoom: {
				enabled: true,
				duration: 300,
				easing: 'ease-in-out',
				opener: function(openerElement) {
					return openerElement.is('img') ? openerElement : openerElement.find('img');
				}
			},
			disableOn: function() {
				if (settings.disableOnMobile && windowWidth < mobileWidth) {
					return false;
				}

				if (settings.disableOnTablet && windowWidth >= mobileWidth && windowWidth < tabletWidth) {
					return false;
				}

				return true;
			}
		});
	}

	var HandleImageCompare = function($scope) {
		var $item = $scope.find('.hajs-image-comparison'),
			settings = $item.getHappySettings(),
			fieldMap = {
				on_hover: 'move_slider_on_hover',
				on_swipe: 'move_with_handle_only',
				on_click: 'click_to_move'
			};

		settings[fieldMap[settings.move_handle || 'on_swipe']] = true;
		delete settings.move_handle;

		$item.imagesLoaded().done(function() {
			$item.twentytwenty(settings);

			var t = setTimeout(function() {
				$window.trigger('resize.twentytwenty');
				clearTimeout(t);
			}, 400);
		});
	};

	$window.on('elementor/frontend/init', function() {
		var ModuleHandler = elementorModules.frontend.handlers.Base;

		var SliderBase = ModuleHandler.extend({
			bindEvents: function() {
				this.removeArrows();
				this.run();
			},

			removeArrows: function() {
				var _this = this;

				this.elements.$container.on('init', function() {
					_this.elements.$container.siblings().hide();
				});
			},

			getDefaultSettings: function() {
				return {
					autoplay    : true,
					arrows      : false,
					checkVisible: false,
					container   : '.hajs-slick',
					dots        : false,
					infinite    : true,
					rows        : 0,
					slidesToShow: 1,
					prevArrow   : $('<div />').append(this.findElement('.slick-prev').clone().show()).html(),
					nextArrow   : $('<div />').append(this.findElement('.slick-next').clone().show()).html()
				}
			},

			getDefaultElements: function () {
				return {
					$container: this.findElement(this.getSettings('container'))
				};
			},

			onElementChange: debounce(function() {
				this.elements.$container.slick('unslick');
				this.run();
			}, 200),

			getSlickSettings: function() {
				var settings = {
					infinite: !! this.getElementSettings('loop'),
					autoplay: !! this.getElementSettings('autoplay'),
					autoplaySpeed: this.getElementSettings('autoplay_speed'),
					speed: this.getElementSettings('animation_speed'),
					centerMode: !! this.getElementSettings('center'),
					vertical: !! this.getElementSettings('vertical'),
					slidesToScroll: 1,
				};

				switch (this.getElementSettings('navigation')) {
					case 'arrow':
						settings.arrows = true;
						break;
					case 'dots':
						settings.dots = true;
						break;
					case 'both':
						settings.arrows = true;
						settings.dots = true;
						break;
				}

				settings.slidesToShow = parseInt( this.getElementSettings('slides_to_show') ) || 1;
				settings.responsive = [
					{
						breakpoint: elementorFrontend.config.breakpoints.lg,
						settings: {
							slidesToShow: (parseInt(this.getElementSettings('slides_to_show_tablet')) || settings.slidesToShow),
						}
					},
					{
						breakpoint: elementorFrontend.config.breakpoints.md,
						settings: {
							slidesToShow: (parseInt(this.getElementSettings('slides_to_show_mobile')) || parseInt(this.getElementSettings('slides_to_show_tablet'))) || settings.slidesToShow,
						}
					}
				];

				return $.extend({}, this.getSettings(), settings);
			},

			run: function() {
				this.elements.$container.slick(this.getSlickSettings());
			}
		});

		var NumberHandler = function($scope) {
			elementorFrontend.waypoint($scope, function () {
				var $number = $scope.find('.ha-number-text');
				$number.numerator($number.data('animation'));
			});
		};

		var SkillHandler = function($scope) {
			elementorFrontend.waypoint($scope, function () {
				$scope.find('.ha-skill-level').each(function() {
					var $current = $(this),
						$lt = $current.find('.ha-skill-level-text'),
						lv = $current.data('level');

					$current.animate({
						width: lv+'%'
					}, 500);
					$lt.numerator({
						toValue: lv + '%',
						duration: 1300,
						onStep: function() {
							$lt.append('%');
						}
					});
				});
			});
		};

		var ImageGrid = ModuleHandler.extend({
			onInit: function () {
				ModuleHandler.prototype.onInit.apply(this, arguments);
				this.run();
				this.runFilter();

				$window.on('resize', debounce(this.run.bind(this), 100));
			},

			getLayoutMode: function() {
				var layout = this.getElementSettings('layout');
				return ( layout === 'even' ? 'masonry' : layout );
			},

			getDefaultSettings: function() {
				return {
					itemSelector: '.ha-image-grid__item',
					percentPosition: true,
					layoutMode: this.getLayoutMode(),
				};
			},

			getDefaultElements: function() {
				return {
					$container: this.findElement('.hajs-isotope')
				};
			},

			getLightBoxSettings: function() {
				return {
					key            : 'imagegrid',
					$element       : this.$element,
					selector       : '.ha-js-lightbox',
					isEnabled      : !!this.getElementSettings('enable_popup'),
					disableOnTablet: !!this.getElementSettings('disable_lightbox_on_tablet'),
					disableOnMobile: !!this.getElementSettings('disable_lightbox_on_mobile')
				};
			},

			runFilter: function() {
				var self = this,
					lbSettings = this.getLightBoxSettings();

				initFilterNav(this.$element, function(filter) {
					self.elements.$container.isotope({
						filter: filter
					});

					if (filter !== '*') {
						lbSettings.selector = filter;
					}

					initPopupGallery(lbSettings);
				});
			},

			onElementChange: function(changedProp) {
				if (['layout', 'image_height', 'columns', 'image_margin', 'enable_popup'].indexOf(changedProp) !== -1) {
					this.run();
				}
			},

			run: function() {
				var self = this;

				self.elements
					.$container
					.isotope(self.getDefaultSettings())
					.imagesLoaded().progress(function() {
						self.elements.$container.isotope('layout');
					});

				initPopupGallery(self.getLightBoxSettings());
			}
		});

		var JustifiedGrid = ModuleHandler.extend({
			onInit: function () {
				ModuleHandler.prototype.onInit.apply(this, arguments);
				this.run();
				this.runFilter();

				$window.on('resize', debounce(this.run.bind(this), 100));
			},

			getDefaultSettings: function() {
				return {
					rowHeight: +this.getElementSettings('row_height.size') || 150,
					lastRow: this.getElementSettings('last_row'),
					margins: +this.getElementSettings('margins.size'),
					captions: !!this.getElementSettings('show_caption')
				};
			},

			getDefaultElements: function() {
				return {
					$container: this.findElement('.hajs-justified-grid')
				};
			},

			getLightBoxSettings: function() {
				return {
					key            : 'justifiedgallery',
					$element       : this.$element,
					selector       : '.ha-js-lightbox',
					isEnabled      : !!this.getElementSettings('enable_popup'),
					disableOnTablet: !!this.getElementSettings('disable_lightbox_on_tablet'),
					disableOnMobile: !!this.getElementSettings('disable_lightbox_on_mobile')
				};
			},

			runFilter: function() {
				var self = this,
					lbSettings = this.getLightBoxSettings(),
					settings = {
						lastRow: this.getElementSettings('last_row')
					};

				initFilterNav(self.$element, function(filter) {
					if (filter !== '*') {
						settings.lastRow = 'nojustify';
						lbSettings.selector = filter
					}

					settings.filter = filter;
					self.elements.$container.justifiedGallery(settings);

					initPopupGallery(lbSettings);
				});
			},

			onElementChange: function(changedProp) {
				if (['row_height', 'last_row', 'margins', 'show_caption', 'enable_popup'].indexOf(changedProp) !== -1) {
					this.run();
				}
			},

			run: function() {
				this.elements
					.$container
					.justifiedGallery(this.getDefaultSettings());

				initPopupGallery(this.getLightBoxSettings());
			}
		});

		// NewsTicker
		var NewsTicker = ModuleHandler.extend({

			onInit: function () {
				ModuleHandler.prototype.onInit.apply(this, arguments);
				this.wrapper = this.$element.find('.ha-news-ticker-wrapper');
				this.run();
			},
			onElementChange: function (changed_prop) {
				if( changed_prop === 'item_space' || changed_prop === 'title_typography_font_size' ){
					this.run();
				}
			},
			run: function () {
				if( 0 == this.wrapper.length){
					return;
				}
				var wrapper_height = this.wrapper.innerHeight(),
					wrapper_width = this.wrapper.innerWidth(),
					container = this.wrapper.find('.ha-news-ticker-container'),
					single_item = container.find('.ha-news-ticker-item'),
					scroll_direction = this.wrapper.data('scroll-direction'),
					scroll = "scroll" + scroll_direction + parseInt(wrapper_height) + parseInt(wrapper_width),
					duration = this.wrapper.data('duration'),
					direction = 'normal',
					all_title_width = 10;

				var start = {'transform': 'translateX(0'+wrapper_width+'px)'},
					end = {'transform': 'translateX(-101%)'};
				if('right' === scroll_direction){
					direction = 'reverse';
				}
				single_item.each(function(){
					all_title_width += $(this).outerWidth(true);
				});
				container.css({'width':all_title_width,'display':'flex'});
				$.keyframe.define([{
					name: scroll,
					'0%': start,
					'100%':end,
				}]);
				container.playKeyframe({
					name: scroll,
					duration: duration.toString() + "ms",
					timingFunction: 'linear',
					delay: '0s',
					iterationCount: 'infinite',
					direction: direction,
					fillMode: 'none',
					complete: function(){
					}
				});
			}
		});

		// Fun factor
		var FunFactor = function ($scope) {
			elementorFrontend.waypoint($scope, function () {
				var $fun_factor = $scope.find('.ha-fun-factor__content-number');
				$fun_factor.numerator($fun_factor.data('animation'));
			});
		};

		var BarChart = function($scope) {
			elementorFrontend.waypoint($scope, function () {
				var $chart = $(this),
					$container = $chart.find( '.ha-bar-chart-container' ),
					$chart_canvas = $chart.find( '#ha-bar-chart' ),
					settings      = $container.data( 'settings' );

				if ( $container.length ) {
					new Chart( $chart_canvas, settings );
				}
			} );
		};

		//twitter Feed
		var TwitterFeed = function($scope) {
			var button = $scope.find('.ha-twitter-load-more');
			var twitter_wrap = $scope.find('.ha-tweet-items');
			button.on("click", function(e) {
				e.preventDefault();
				var $self = $(this),
					query_settings = $self.data("settings"),
					total = $self.data("total"),
					items = $scope.find('.ha-tweet-item').length;
				$.ajax({
					url: HappyLocalize.ajax_url,
					type: 'POST',
					data: {
						action: "ha_twitter_feed_action",
						security: HappyLocalize.nonce,
						query_settings: query_settings,
						loaded_item: items,
					},
					success: function(response) {
						if(total > items){
							$(response).appendTo(twitter_wrap);
						}else{
							$self.text('All Loaded').addClass('loaded');
							setTimeout( function(){
								$self.css({"display": "none"});
							},800);
						}
					},
					error: function(error) {}
				});
			});
		};

		//PostTab
		var PostTab = ModuleHandler.extend({

			onInit: function () {
				ModuleHandler.prototype.onInit.apply(this, arguments);
				this.wrapper = this.$element.find('.ha-post-tab');
				this.run();
			},
			run: function () {
				var filter_wrap = this.wrapper.find('.ha-post-tab-filter'),
					filter = filter_wrap.find('li'),
					event = this.wrapper.data('event'),
					args = this.wrapper.data('query-args');

				filter.on(event, debounce(function (e) {
					e.preventDefault();

					var $self = $(this),
						term_id = $self.data("term"),
						$wrapper = $self.closest(".ha-post-tab"),
						content = $wrapper.find('.ha-post-tab-content'),
						loading = content.find('.ha-post-tab-loading'),
						tab_item = content.find('.ha-post-tab-item-wrapper'),
						$content_exist = false;

					if (0 === loading.length) {
						filter.removeClass('active');
						tab_item.removeClass('active');
						$self.addClass('active');

						tab_item.each(function () {
							var $self = $(this),
								$content_id = $self.data("term");
							if (term_id === $content_id) {
								$self.addClass('active');
								$content_exist = true;
							}
						});

						if (false === $content_exist) {
							$.ajax({
								url: HappyLocalize.ajax_url,
								type: 'POST',
								data: {
									action: "ha_post_tab_action",
									security: HappyLocalize.nonce,
									post_tab_query: args,
									term_id: term_id,
								},
								beforeSend: function () {
									content.append('<span class="ha-post-tab-loading"><i class="eicon-spinner eicon-animation-spin"></i></span>');
								},
								success: function (response) {
									content.find('.ha-post-tab-loading').remove();
									content.append(response);
								},
								error: function (error) {
								}
							});

						}
					}

				}, 200));
			}
		});

		var DataTable = function($scope) {
			var columnTH = $scope.find('.ha-table__head-column-cell');
			var rowTR = $scope.find('.ha-table__body-row');

			rowTR.each( function( i, tr) {
				var th = $(tr).find('.ha-table__body-row-cell');
				th.each( function( index, th ) {
					$(th).prepend( '<div class="ha-table__head-column-cell">' + columnTH.eq(index).html() + '</div>' );
				} );
			} );
		};

		//Threesixty Rotation
		var Threesixty_Rotation = function($scope) {
			var ha_circlr = $scope.find('.ha-threesixty-rotation-inner');
			var cls = ha_circlr.data('selector');
			var autoplay = ha_circlr.data('autoplay');
			var glass_on = $scope.find('.ha-threesixty-rotation-magnify');
			var t360 = $scope.find('.ha-threesixty-rotation-360img');
			var zoom = glass_on.data('zoom');

			var playb = $scope.find('.ha-threesixty-rotation-play');

			var crl = circlr(cls, {
				play : true,
			});

			if( 'on' ===autoplay ){
				var autoplay_btn = $scope.find('.ha-threesixty-rotation-autoplay');
				autoplay_btn.on('click', function(el) {
					el.preventDefault();
					crl.play();
					t360.remove();
				});
				setTimeout(function(){
					autoplay_btn.trigger('click');
					autoplay_btn.remove();
				},1000);
			}else {
				playb.on('click', function(el) {
					el.preventDefault();
					var $self = $(this);
					var $i = $self.find('i');
					if($i.hasClass('hm-play-button')){
						$i.removeClass('hm-play-button');
						$i.addClass('hm-stop');
						crl.play();
					}else{
						$i.removeClass('hm-stop');
						$i.addClass('hm-play-button');
						crl.stop();
					}
					t360.remove();
				});
			}

			glass_on.on('click', function(el) {
				var img_block = $scope.find('img');
				img_block.each(function(){
					var style = $(this).attr('style');
					if( -1 !== style.indexOf("block") ){
						HappySimplaMagnify($(this)[0],zoom);
						glass_on.css('display','none');
						t360.remove();
					}
				});
			});

			$(document).on('click', function (e) {
				var t = $(e.target);
				var magnifier = $scope.find('.ha-img-magnifier-glass');
				var i = glass_on.find('i');
				if( magnifier.length && t[0] !== i[0] ){
					magnifier.remove();
					glass_on.removeAttr('style');
				}
				if( t[0] === ha_circlr[0] ){
					t360.remove();
				}
			});

			ha_circlr.on('mouseup mousedown', function (e) {
				t360.remove();
			});

		};

		//Event Calendar
		var Event_Calendar = function($scope) {
			var calendarEl =  $scope.find('.ha-ec');
			var popup = $scope.find('.ha-ec-popup-wrapper');
			var popupClose = $scope.find(".ha-ec-popup-close");
			var events = calendarEl.data('events');
			var initialview = calendarEl.data('initialview');
			var firstday = calendarEl.data('firstday');
			var locale = calendarEl.data('locale');
			var showPopup = calendarEl.data('show-popup');
			var allday_text = calendarEl.data('allday-text');

			if( 'undefined' == typeof events){
				return;
			}

			var option = {
				stickyHeaderDates: false,
				locale: locale,
				headerToolbar: {
					left: "prev,next today",
					center: "title",
					right: "dayGridMonth,timeGridWeek,timeGridDay,listMonth"
				},
				initialView: initialview,
				firstDay: firstday,
				eventTimeFormat: { // like '7pm'
					hour: 'numeric',
					minute: '2-digit',
					meridiem: 'short'
				},
				events: events,

				height: 'auto',

				eventClick: function (info) {
					info.jsEvent.preventDefault(); // don't let the browser navigate

					if( 'yes' != showPopup){
						return;
					}

					function getTheDate(timeString) {
						return new Date(timeString);
					}

					function timeFormat(date) {
						var hours = date.getHours();
						var minutes = date.getMinutes();
						var ampm = hours >= 12 ? 'pm' : 'am';
						hours = hours % 12;
						hours = hours ? hours : 12; // the hour '0' should be '12'
						minutes = minutes < 10 ? '0' + minutes : minutes;
						var strTime = hours + ':' + minutes + '' + ampm;
						return strTime;
					}

					var todayDateString = info.view.calendar.currentData.currentDate.toString(),
						allDay = info.event.allDay,
						title = info.event.title,
						startDate = info.event.startStr,
						endDate = info.event.endStr,
						guest = info.event.extendedProps.guest,
						location = info.event.extendedProps.location,
						description = info.event.extendedProps.description,
						detailsUrl = info.event.url,
						imageUrl = info.event.extendedProps.image;

					var titleWrap = popup.find('.ha-ec-event-title'),
						timeWrap = popup.find('.ha-ec-event-time-wrap'),
						guestWrap = popup.find('.ha-ec-event-guest-wrap'),
						locationWrap = popup.find('.ha-ec-event-location-wrap'),
						descWrap = popup.find('.ha-ec-popup-desc'),
						detailsWrap = popup.find('.ha-ec-popup-readmore-link'),
						imageWrap = popup.find('.ha-ec-popup-image');

					// display none
					imageWrap.css('display', 'none');
					titleWrap.css('display', 'none');
					timeWrap.css('display', 'none');
					guestWrap.css('display', 'none');
					locationWrap.css('display', 'none');
					descWrap.css('display', 'none');
					detailsWrap.css('display', 'none');

					popup.addClass("ha-ec-popup-ready");

					// image markup
					if (imageUrl) {
						imageWrap.removeAttr("style");
						imageWrap.find('img').attr("src", imageUrl );
						imageWrap.find('img').attr("alt", title );
					}

					// title markup
					if (title) {
						titleWrap.removeAttr("style");
						titleWrap.html(title);
					}

					// guest markup
					if (guest) {
						guestWrap.removeAttr("style");
						guestWrap.find('span.ha-ec-event-guest').html( guest );
					}

					// location markup
					if (location) {
						locationWrap.removeAttr("style");
						locationWrap.find('span.ha-ec-event-location').html( location );
					}

					// description markup
					if (description) {
						descWrap.removeAttr("style");
						descWrap.html(description);
					}

					// time markup
					if (allDay !== true) {
						timeWrap.removeAttr("style");
						startDate = Date.parse(getTheDate(startDate));
						endDate = Date.parse(getTheDate(endDate));
						var startTimeText = timeFormat(getTheDate(startDate));
						var endTimeText = 'Invalid Data';
						if (startDate < endDate) {
							endTimeText = timeFormat(getTheDate(endDate));
						}
						timeWrap.find('span.ha-ec-event-time').html(startTimeText + ' - ' + endTimeText);
					}else{
						timeWrap.removeAttr("style");
						timeWrap.find('span.ha-ec-event-time').html(allday_text);
					}

					// read more markup
					if (detailsUrl) {
						detailsWrap.removeAttr("style");
						detailsWrap.attr("href", detailsUrl);
						if ("on" === info.event.extendedProps.external) {
							detailsWrap.attr("target", "_blank");
						}
						if ("on" === info.event.extendedProps.nofollow) {
							detailsWrap.attr("rel", "nofollow");
						}
					}
				},
				dateClick: function (arg) {
					itemDate = arg.date.toUTCString();
				}
			}

			var calendar = new FullCalendar.Calendar( calendarEl[0], option );
				calendar.render();

			$scope.find(".ha-ec-popup-wrapper").on("click", function (e) {
				e.stopPropagation();

				if(e.target === e.currentTarget || e.target == popupClose[0] || e.target == popupClose.find(".eicon-editor-close")[0]){
					popup.addClass("ha-ec-popup-removing").removeClass("ha-ec-popup-ready");
				}
			});

		};

		//Image Accordion
		var Image_Accordion = function($scope) {
			if($scope.hasClass('ha-image-accordion-click')) {
				var items = $scope.find('.ha-ia-item');
				items.each(function (inx, btn){
					$(this).on('click', function(e) {
						// e.preventDefault();
						if($(this).hasClass('active')) {
							return;
						}else {
							items.removeClass('active');
							$(this).addClass('active');
						}
					} );
				});
			}
		};

		//Content Switcher
		var Content_Switcher = function($scope) {
			var parent = $scope.find('.ha-content-switcher-wrapper'),
				designType = parent.data('design-type');

			if(designType == 'button') {
				var buttons = parent.find('.ha-cs-button'),
					contents = parent.find('.ha-cs-content-section');
				buttons.each(function (inx, btn){
					$(this).on('click', function(e) {
						e.preventDefault();
						if($(this).hasClass('active')) {
							return;
						}else {
							buttons.removeClass('active');
							$(this).addClass('active');

							contents.removeClass('active');
							var contentId = $(this).data('content-id');
							parent.find('#'+contentId).addClass('active');
						}
					} );
				});

			}else{
				var toggleSwitch = parent.find('.ha-cs-switch.ha-input-label'),
					input = parent.find('input.ha-cs-toggle-switch'),
					primarySwitcher = parent.find('.ha-cs-switch.primary'),
					secondarySwitcher = parent.find('.ha-cs-switch.secondary'),
					primaryContent = parent.find('.ha-cs-content-section.primary'),
					secondaryContent = parent.find('.ha-cs-content-section.secondary');

				toggleSwitch.on('click', function(e){

					if(input.is(':checked')){
						primarySwitcher.removeClass('active');
						primaryContent.removeClass('active');
						secondarySwitcher.addClass('active');
						secondaryContent.addClass('active');
					}else {
						secondarySwitcher.removeClass('active');
						secondaryContent.removeClass('active');
						primarySwitcher.addClass('active');
						primaryContent.addClass('active');
					}
				});

			}

		};

		var MailChimp = elementorModules.frontend.handlers.Base.extend({

			onInit: function () {
				elementorModules.frontend.handlers.Base.prototype.onInit.apply(this, arguments);
				this.elForm = this.$element.find('.ha-mailchimp-form');
				this.elMessage = this.$element.find('.ha-mc-response-message');
				this.successMessage = this.elForm.data('success-message');
				this.run();
			},
			getReadySettings: function () {
				var settings = {
					formAlign: this.getElementSettings('form_alignment'),
					formAlignTablet: this.getElementSettings('form_alignment_tablet') || this.getElementSettings('form_alignment'),
					formAlignMobile: this.getElementSettings('form_alignment_mobile') || this.getElementSettings('form_alignment_tablet') || this.getElementSettings('form_alignment'),
				};
				return $.extend({}, settings);
			},
			onElementChange: function () {
				this.run();
			},
			run: function () {
				var settings = this.getReadySettings();
				var elForm = this.elForm;
				var elMessage = this.elMessage;
				var successMessage = this.successMessage;

				elForm.on('submit', function(e){
					e.preventDefault();

					var data = {
						action: 'ha_mailchimp_ajax',
						security: HappyLocalize.nonce,
						subscriber_info: elForm.serialize(),
						list_id: elForm.data('list-id'),
						post_id: elForm.parent().data('post-id'),
						widget_id: elForm.parent().data('widget-id'),
					};

					$.ajax({
						type: 'post',
						url: HappyLocalize.ajax_url,
						data: data,
						success: function(response) {
							elForm.trigger('reset');

							if(response.status){
								elMessage.removeClass('error');
								elMessage.addClass('success');
								elMessage.text(successMessage);
							}else {
								elMessage.addClass('error');
								elMessage.removeClass('success');
								elMessage.text(response.msg);
							}

						},
						error: function(error) {

						}
					});

				});

				var mobileWidth = elementorFrontendConfig.breakpoints.sm;
				var tabletWidth = elementorFrontendConfig.breakpoints.md;

				function responsiveClass(){

					var windowWidth = $(window).width();

					if (windowWidth > tabletWidth) {
						elForm.removeClass('vertical');
						elForm.removeClass('horizontal');
						elForm.addClass(settings.formAlign);
					}else if(windowWidth > mobileWidth && windowWidth <= tabletWidth) {
						elForm.removeClass('vertical');
						elForm.removeClass('horizontal');
						elForm.addClass(settings.formAlignTablet);
					}else if ( windowWidth <= mobileWidth ) {
						elForm.removeClass('vertical');
						elForm.removeClass('horizontal');
						if ( elForm.hasClass('multiple_form_fields') ){
							elForm.addClass('vertical');
						}else {
							elForm.addClass(settings.formAlignMobile);
						}
					}

				};

				responsiveClass();
				$(window).on('load, resize', responsiveClass);

			}
		});



		//Team Member
		var Team_Member = function($scope) {
			var btn = $scope.find('.ha-btn');
			var lightBox = $scope.find('.ha-member-lightbox');
			if( lightBox.length > 0 ){

				var close = lightBox.find('.ha-member-lightbox-close');

				btn.on('click', function(){
					lightBox.addClass('ha-member-lightbox-show');
				});

				lightBox.on('click', function(e){
					if( lightBox.hasClass('ha-member-lightbox-show') ) {
						if( e.target == lightBox[0] ) {
							lightBox.removeClass('ha-member-lightbox-show');
						}
						else if( e.target == close[0] ) {
							lightBox.removeClass('ha-member-lightbox-show');
						}
						else if( e.target == close.find('i.eicon-editor-close')[0] ) {
							lightBox.removeClass('ha-member-lightbox-show');
						}
					}
				});
			}
		};

		//Creative Button
		var Creative_Button = function($scope) {
			var btn_wrap = $scope.find('.ha-creative-btn-wrap');
			var magnetic = btn_wrap.data('magnetic');
			var btn = btn_wrap.find('a.ha-creative-btn');
			if( 'yes' == magnetic ){
				btn_wrap.on('mousemove', function(e) {
					var x = e.pageX - ( btn_wrap.offset().left + ( btn_wrap.outerWidth() / 2 ) );
					var y = e.pageY - ( btn_wrap.offset().top + ( btn_wrap.outerHeight() / 2 ) );
					btn.css("transform", "translate(" + x * 0.3 + "px, " + y * 0.5 + "px)");
				});
				btn_wrap.on('mouseout', function(e){
					btn.css("transform", "translate(0px, 0px)");
				});
			}
			//For expandable button style only
			var expandable = $scope.find('.ha-eft--expandable');
			var text = expandable.find('.text');
			if ( expandable.length > 0 && text.length > 0 ) {
				text[0].addEventListener("transitionend", function () {
					if (text[0].style.width) {
						text[0].style.width = "auto";
					}
				});
				expandable[0].addEventListener("mouseenter", function (e) {
					e.currentTarget.classList.add('hover');
					text[0].style.width = "auto";
					var predicted_answer = text[0].offsetWidth;
					text[0].style.width = "0";
					window.getComputedStyle(text[0]).transform;
					text[0].style.width = "".concat(predicted_answer, "px");

				});
				expandable[0].addEventListener("mouseleave", function (e) {
					e.currentTarget.classList.remove('hover');
					text[0].style.width = "".concat(text[0].offsetWidth, "px");
					window.getComputedStyle(text[0]).transform;
					text[0].style.width = "";
				});
			}
		};

		var PDF_View = function($scope){
			var $id = $scope.data('id');
			var $settings = $scope.find(".viewer-"+$id).data('pdf-settings');
			var options = {
				width: $settings.width,
				height: $settings.height,
				page: $settings.page_number
			};
			PDFObject.embed($settings.pdf_url, "#"+$settings.unique_id, options);
		};
		

		// Slider
		elementorFrontend.hooks.addAction(
			'frontend/element_ready/ha-slider.default',
			function ($scope) {
				elementorFrontend.elementsHandler.addHandler(SliderBase, {
					$element: $scope
				});
			}
		);

		// Carousel
		elementorFrontend.hooks.addAction(
			'frontend/element_ready/ha-carousel.default',
			function ($scope) {
				elementorFrontend.elementsHandler.addHandler(SliderBase, {
					$element: $scope
				});
			}
		);

		//Horizontal Timeline
		elementorFrontend.hooks.addAction(
			'frontend/element_ready/ha-horizontal-timeline.default',
			function ($scope) {
				elementorFrontend.elementsHandler.addHandler(SliderBase, {
					$element  : $scope,
					autoplay  : false,
					container : '.ha-horizontal-timeline-wrapper',
					navigation: 'arrow',
					arrows    : true,
				});
				var img_wrap = $scope.find(".ha-horizontal-timeline-image");
				var magnific_popup = img_wrap.data("mfp-src");

				if( undefined !== magnific_popup ){
					img_wrap.magnificPopup({
						type: "image",
						gallery: {
							enabled: true
						}
					});
				}
			}
		);

		elementorFrontend.hooks.addAction(
			'frontend/element_ready/ha-mailchimp.default',
			function ($scope) {
				elementorFrontend.elementsHandler.addHandler(MailChimp, {
					$element: $scope,
				});
			}
		);

		$('body').on('click.onWrapperLink', '[data-ha-element-link]', function() {
			var $wrapper = $(this),
			    data     = $wrapper.data('ha-element-link'),
			    id       = $wrapper.data('id'),
			    anchor   = document.createElement('a'),
				anchorReal,
				timeout;

			anchor.id            = 'happy-addons-wrapper-link-' + id;
			anchor.href          = data.url;
			anchor.target        = data.is_external ? '_blank' : '_self';
			anchor.rel           = data.nofollow ? 'nofollow noreferer' : '';
			anchor.style.display = 'none';

			document.body.appendChild(anchor);

			anchorReal = document.getElementById(anchor.id);
			anchorReal.click();

			timeout = setTimeout(function() {
				document.body.removeChild(anchorReal);
				clearTimeout(timeout);
			});
		});

		// Background overlay extension
		var BackgroundOverlay = function($scope) {
			$scope.hasClass('elementor-element-edit-mode') && $scope.addClass('ha-has-bg-overlay');
		}

		var fnHanlders = {
			'ha-image-compare.default'      : HandleImageCompare,
			'ha-number.default'             : NumberHandler,
			'ha-skills.default'             : SkillHandler,
			'ha-fun-factor.default'         : FunFactor,
			'ha-bar-chart.default'          : BarChart,
			'ha-twitter-feed.default'       : TwitterFeed,
			'ha-threesixty-rotation.default': Threesixty_Rotation,
			'ha-data-table.default'         : DataTable,
			'widget'                        : BackgroundOverlay,
			'ha-event-calendar.default'		: Event_Calendar,
			'ha-image-accordion.default'	: Image_Accordion,
			'ha-content-switcher.default'	: Content_Switcher,
			'ha-member.default'		        : Team_Member,
			'ha-creative-button.default'	: Creative_Button,
			'ha-pdf-view.default'			: PDF_View
		};

		$.each( fnHanlders, function( widgetName, handlerFn ) {
			elementorFrontend.hooks.addAction( 'frontend/element_ready/' + widgetName, handlerFn );
		});

		var classHandlers = {
			'ha-image-grid.default'       : ImageGrid,
			'ha-justified-gallery.default': JustifiedGrid,
			'ha-news-ticker.default'      : NewsTicker,
			'ha-post-tab.default'         : PostTab
		};

		$.each( classHandlers, function( widgetName, handlerClass ) {
			elementorFrontend.hooks.addAction( 'frontend/element_ready/' + widgetName, function( $scope ) {
				elementorFrontend.elementsHandler.addHandler( handlerClass, { $element: $scope });
			});
		});

	});

} (jQuery));
