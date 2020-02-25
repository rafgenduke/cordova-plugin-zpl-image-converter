let exec = cordova.require("cordova/exec");

/*ZPLImageConverter.prototype.convertImage = function(fileName, success, failure) {
    exec(success, failure, "ZPLImageConverter", "convertImage", [fileName]);
}

let zplImageConverter = new ZPLImageConverter();
module.exports = zplImageConverter;
 */
function ZPLImageConverter() {}

ZPLImageConverter.prototype.convertImage = function(fileName, width, height, angle, success, failure) {
    exec(success, failure, "ZPLImageConverter", "convertImage", [fileName, width, height, angle]);
}

ZPLImageConverter.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.zplImageConverter = new ZPLImageConverter();
  return window.plugins.zplImageConverter;
};
cordova.addConstructor(ZPLImageConverter.install);