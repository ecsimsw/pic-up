// let descriptionArea = document.getElementById("description");
// let imageBoxButton = document.getElementById("imageBoxButton");
//
// descriptionArea.addEventListener('input', function () {
//   let content = this.value;
//   let maxRows = 2;
//   const rows = content.split('\n').length;
//   if (rows > maxRows) {
//     this.value = content.slice(0, -1);
//   }
// }, false);
//
// imageBoxButton.addEventListener('change', function () {
//   let content = this.value
//   let filePath = content.split('\\');
//   let fileName = filePath[filePath.length - 1];
//   let imageBoxName = document.getElementById("imageBoxText");
//   imageBoxName.readOnly = false;
//   imageBoxName.value = fileName;
//   imageBoxName.readOnly = true;
// }, false);
//
// function initCreationPanel() {
//   const $body = $('body');
//   let $panels = $('.panel');
//
//   $panels.each(function () {
//     let $this = $(this),
//         $toggles = $('[href="#' + $this.attr('id') + '"]'),
//         $closer = $('<div class="closer" />').appendTo($this);
//
//     // Closer.
//     $closer
//     .on('click', function (event) {
//       $this.trigger('---hide');
//     });
//
//     // Events.
//     $this
//     .on('click', function (event) {
//       event.stopPropagation();
//     })
//     .on('---toggle', function () {
//
//       if ($this.hasClass('active')) {
//         $this.triggerHandler('---hide');
//       } else {
//         $this.triggerHandler('---show');
//       }
//
//     })
//     .on('---show', function () {
//
//       // Hide other content.
//       if ($body.hasClass('content-active')) {
//         $panels.trigger('---hide');
//       }
//
//       // Activate content, toggles.
//       $this.addClass('active');
//       $toggles.addClass('active');
//
//       // Activate body.
//       $body.addClass('content-active');
//
//     })
//     .on('---hide', function () {
//
//       // Deactivate content, toggles.
//       $this.removeClass('active');
//       $toggles.removeClass('active');
//
//       // Deactivate body.
//       $body.removeClass('content-active');
//
//     });
//
//     // Toggles.
//     $toggles
//     .removeAttr('href')
//     .css('cursor', 'pointer')
//     .on('click', function (event) {
//
//       event.preventDefault();
//       event.stopPropagation();
//
//       $this.trigger('---toggle');
//     });
//   });
// }
//
// initCreationPanel()
