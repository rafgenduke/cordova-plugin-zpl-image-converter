package com.doka.cordova;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.graphics.Matrix;

public class ZPLImageConverter extends CordovaPlugin{
    private int blackLimit = 380;
    private int total;
    private int widthBytes;
    private boolean compressHex = false;
    private static Map<Integer, String> mapCode = new HashMap<Integer, String>();
    {
        mapCode.put(1, "G");
        mapCode.put(2, "H");
        mapCode.put(3, "I");
        mapCode.put(4, "J");
        mapCode.put(5, "K");
        mapCode.put(6, "L");
        mapCode.put(7, "M");
        mapCode.put(8, "N");
        mapCode.put(9, "O");
        mapCode.put(10, "P");
        mapCode.put(11, "Q");
        mapCode.put(12, "R");
        mapCode.put(13, "S");
        mapCode.put(14, "T");
        mapCode.put(15, "U");
        mapCode.put(16, "V");
        mapCode.put(17, "W");
        mapCode.put(18, "X");
        mapCode.put(19, "Y");
        mapCode.put(20, "g");
        mapCode.put(40, "h");
        mapCode.put(60, "i");
        mapCode.put(80, "j");
        mapCode.put(100, "k");
        mapCode.put(120, "l");
        mapCode.put(140, "m");
        mapCode.put(160, "n");
        mapCode.put(180, "o");
        mapCode.put(200, "p");
        mapCode.put(220, "q");
        mapCode.put(240, "r");
        mapCode.put(260, "s");
        mapCode.put(280, "t");
        mapCode.put(300, "u");
        mapCode.put(320, "v");
        mapCode.put(340, "w");
        mapCode.put(360, "x");
        mapCode.put(380, "y");        
        mapCode.put(400, "z");            
    }
    public String convertfromImg(Bitmap image) throws IOException {
        String hexAscii = createBody(image);
        if (compressHex) {
            hexAscii = encodeHexAscii(hexAscii);
        }

        String zplCode = "^GFA," + total + "," + total + "," + widthBytes + ", " + hexAscii;

        /* if (true) {
            String header = "^XA " + "^FO0,0^GFA," + total + "," + total + "," + widthBytes + ", ";
            String footer = "^FS" + "^XZ";
            zplCode = header + zplCode + footer;
        } */
        return zplCode;      
    }
    private String createBody(Bitmap originalImage) throws IOException {
        StringBuffer sb = new StringBuffer();
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int rgb, red, green, blue, index=0;        
        char auxBinaryChar[] =  {'0', '0', '0', '0', '0', '0', '0', '0'};
        widthBytes = width/8;
        if(width%8>0){
            widthBytes= (((int)(width/8))+1);
        } else {
            widthBytes= width/8;
        }
        this.total = widthBytes*height;
        for (int h = 0; h<height; h++)
        {
            for (int w = 0; w<width; w++)
            {
                rgb = originalImage.getPixel(w, h);
                red = (rgb >> 16 ) & 0x000000FF;
                green = (rgb >> 8 ) & 0x000000FF;
                blue = (rgb) & 0x000000FF;
                char auxChar = '1';
                int totalColor = red + green + blue;
                if(totalColor>blackLimit){
                    auxChar = '0';
                }
                auxBinaryChar[index] = auxChar;
                index++;
                if(index==8 || w==(width-1)){
                    sb.append(fourByteBinary(new String(auxBinaryChar)));
                    auxBinaryChar =  new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
                    index=0;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    private String fourByteBinary(String binaryStr){
        int decimal = Integer.parseInt(binaryStr,2);
        if (decimal>15){
            return Integer.toString(decimal,16).toUpperCase();
        } else {
            return "0" + Integer.toString(decimal,16).toUpperCase();
        }
    }
    private String encodeHexAscii(String code){
        int maxlinea =  widthBytes * 2;        
        StringBuffer sbCode = new StringBuffer();
        StringBuffer sbLinea = new StringBuffer();
        String previousLine = null;
        int counter = 1;
        char aux = code.charAt(0);
        boolean firstChar = false; 
        for(int i = 1; i< code.length(); i++ ){
            if(firstChar){
                aux = code.charAt(i);
                firstChar = false;
                continue;
            }
            if(code.charAt(i)=='\n'){
                if(counter>=maxlinea && aux=='0'){
                    sbLinea.append(",");
                } else     if(counter>=maxlinea && aux=='F'){
                    sbLinea.append("!");
                } else if (counter>20){
                    int multi20 = (counter/20)*20;
                    int resto20 = (counter%20);
                    sbLinea.append(mapCode.get(multi20));
                    if(resto20!=0){
                        sbLinea.append(mapCode.get(resto20) + aux);    
                    } else {
                        sbLinea.append(aux);    
                    }
                } else {
                    sbLinea.append(mapCode.get(counter) + aux);
                    if(mapCode.get(counter)==null){
                    }
                }
                counter = 1;
                firstChar = true;
                if(sbLinea.toString().equals(previousLine)){
                    sbCode.append(":");
                } else {
                    sbCode.append(sbLinea.toString());
                }                
                previousLine = sbLinea.toString();
                sbLinea.setLength(0);
                continue;
            }
            if(aux == code.charAt(i)){
                counter++;                
            } else {
                if(counter>20){
                    int multi20 = (counter/20)*20;
                    int resto20 = (counter%20);
                    sbLinea.append(mapCode.get(multi20));
                    if(resto20!=0){
                        sbLinea.append(mapCode.get(resto20) + aux);    
                    } else {
                        sbLinea.append(aux);    
                    }
                } else {
                    sbLinea.append(mapCode.get(counter) + aux);
                }
                counter = 1;
                aux = code.charAt(i);
            }            
        }
        return sbCode.toString();
    }
    /* private String headDoc(){
        String str = "^XA " +
                        "^FO0,0^GFA,"+ total + ","+ total + "," + widthBytes +", ";
        return str;
    }
    private String footDoc(){
        String str = "^FS"+
                        "^XZ";        
        return str;
    } */
    public void setCompressHex(boolean compressHex) {
        this.compressHex = compressHex;
    }
    public void setBlacknessLimitPercentage(int percentage){
        blackLimit = (percentage * 768 / 100);
    }
    public Bitmap rotateBitmap(Bitmap image, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }
    
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equals("convertImage")) {
            String fname = new File(args.getString(0).replaceFirst("(.*):///", "/")).getAbsolutePath();
            try{
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 8;
                options.inMutable = true;
                Bitmap originalImage = BitmapFactory.decodeFile(fname, options);
                if (args.getInt(1) != 0 && args.getInt(2) != 0) {
                    int newWidth;
                    int newHeight;
                    if (originalImage.getWidth() > originalImage.getHeight()) {
                        newWidth = args.getInt(1);
                        newHeight = (int)((float)args.getInt(1) / ((float)originalImage.getWidth() / (float)originalImage.getHeight())); 
                    } else {
                        newHeight = args.getInt(2);
                        newWidth = (int)((float)args.getInt(2) / ((float)originalImage.getHeight() / (float)originalImage.getWidth())); 
                    }
                    originalImage = Bitmap.createScaledBitmap(originalImage,newWidth,newHeight,false);
                }
                ZPLImageConverter zp = new ZPLImageConverter();
                originalImage = zp.rotateBitmap(originalImage, args.getInt(3));
                zp.setCompressHex(true);
                zp.setBlacknessLimitPercentage(50);
                callbackContext.success(zp.convertfromImg(originalImage));
            } catch(Exception err) {
                callbackContext.error(err.toString() + " File-URI:" + fname);
                return false;
            }
        }
        return true;
    }
    
    /*public static void main(String[] args) throws Exception {
    	Bitmap bitmap1 = BitmapFactory.decodeFile("C:\\Users\\dherzog2\\Pictures\\246x0w.png");
        ZPLImageConverter zp = new ZPLImageConverter();
        zp.setCompressHex(true);
        zp.setBlacknessLimitPercentage(50);        
        System.out.println(zp.convertfromImg(originalImage));        
    }*/
}