package com.example.marco.talkmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;

import java.io.IOException;
import java.net.URL;

/**
 * Created by marco on 16/10/28.
 */
public class Obj_Marker {

    private String phoneid;
    private String msg ;
    private String image ;
    private String lat ="0" ;
    private String lon ="0" ;
    private String name ;
    private String online ;
    private Bitmap pic = null ;
private  String time ;



    public String getPhoneid() {
        return phoneid;
    }

    public void setPhoneid(String phoneid) {
        this.phoneid = phoneid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        url_to_bitmap(image);
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public Bitmap getPic() {
        return pic;
    }

    public void setPic(Bitmap pic) {
        this.pic = pic;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void url_to_bitmap(String profilePicUrl)
    {

        final Bitmap[] image = new Bitmap[1];
        try {
            final URL url = new URL(profilePicUrl);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        image[0] = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        pic =  GetBitmapClippedCircle(zoomImage(image[0], 100, 100));
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("picture錯誤:" + e);
                    }
                }
            }).start();


        } catch (IOException e) {
            System.out.println(e);
        }



    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {


        if (bgimage == null) {
            System.out.println("沒照片");

            return null;
        }

        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    public static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float) (width / 2)
                , (float) (height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }
}
