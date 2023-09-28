/*
	Multiverse by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
*/

(function ($) {

    const $window = $(window);
    const $body = $('body');

    // Breakpoints.
    breakpoints({
        xlarge: ['1281px', '1680px'],
        large: ['981px', '1280px'],
        medium: ['737px', '980px'],
        small: ['481px', '736px'],
        xsmall: [null, '480px']
    });

    // Hack: Enable IE workarounds.
    if (browser.name === 'ie')
        $body.addClass('ie');

    // Touch?
    if (browser.mobile)
        $body.addClass('touch');

    // Transitions supported?
    if (browser.canUse('transition')) {

        // Play initial animations on page load.
        $window.on('load', function () {
            window.setTimeout(function () {
                $body.removeClass('is-preload');
            }, 100);
        });

        // Prevent transitions/animations on resize.
        let resizeTimeout;

        $window.on('resize', function () {
            window.clearTimeout(resizeTimeout);
            $body.addClass('is-resizing');
            resizeTimeout = window.setTimeout(function () {
                $body.removeClass('is-resizing');
            }, 100);
        });
    }

    // Scroll back to top.
    $window.scrollTop(0);

    // Panels.
    let $panels = $('.panel');

    $panels.each(function () {

        let $this = $(this),
            $toggles = $('[href="#' + $this.attr('id') + '"]'),
            $closer = $('<div class="closer" />').appendTo($this);

        // Closer.
        $closer
            .on('click', function (event) {
                $this.trigger('---hide');
            });

        // Events.
        $this
            .on('click', function (event) {
                event.stopPropagation();
            })
            .on('---toggle', function () {

                if ($this.hasClass('active'))
                    $this.triggerHandler('---hide');
                else
                    $this.triggerHandler('---show');

            })
            .on('---show', function () {

                // Hide other content.
                if ($body.hasClass('content-active'))
                    $panels.trigger('---hide');

                // Activate content, toggles.
                $this.addClass('active');
                $toggles.addClass('active');

                // Activate body.
                $body.addClass('content-active');

            })
            .on('---hide', function () {

                // Deactivate content, toggles.
                $this.removeClass('active');
                $toggles.removeClass('active');

                // Deactivate body.
                $body.removeClass('content-active');

            });

        // Toggles.
        $toggles
            .removeAttr('href')
            .css('cursor', 'pointer')
            .on('click', function (event) {

                event.preventDefault();
                event.stopPropagation();

                $this.trigger('---toggle');

            });

    });

    // Global events.
    $body
        .on('click', function (event) {
            if ($body.hasClass('content-active')) {
                event.preventDefault();
                event.stopPropagation();
                $panels.trigger('---hide');
            }
        });

    $window
        .on('keyup', function (event) {

            if (event.keyCode === 27
                && $body.hasClass('content-active')) {

                event.preventDefault();
                event.stopPropagation();

                $panels.trigger('---hide');

            }

        });

    // Header.
    let $header = $('#header');

    // Links.
    $header.find('a').each(function () {

        let $this = $(this),
            href = $this.attr('href');

        // Internal link? Skip.
        if (!href
            || href.charAt(0) === '#')
            return;

        // Redirect on click.
        $this
            .removeAttr('href')
            .css('cursor', 'pointer')
            .on('click', function (event) {

                event.preventDefault();
                event.stopPropagation();

                window.location.href = href;

            });

    });
})(jQuery);