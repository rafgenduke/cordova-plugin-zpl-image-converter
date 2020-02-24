# Cordova Plugin Image to ZPL Code

Hi! This cordova plugin converts images to ZPL-Code. To use this plugin you need the permission READ_EXTERNAL_STORAGE. This plugin is Android only.

## Installation

    npm install cordova-plugin-zpl-image-converter
    cordova plugin add cordova-plugin-zpl-image-converter

## Usage

    (window as any).plugins.zplImageConverter.convertImage(filePath: string, width: number, height: number, zplCode => {
        // Your code
      }, err => {
        // Your error-handling
    });

    If you set the height and width parameters to null, no scaling will be performed.

## Special Mentions

Based on: http://www.jcgonzalez.com/java-image-to-zpl-example