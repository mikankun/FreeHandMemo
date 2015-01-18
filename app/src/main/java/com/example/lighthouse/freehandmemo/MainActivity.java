package com.example.lighthouse.freehandmemo;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity  implements View.OnTouchListener{

    Canvas  canvas;
    Paint   paint;
    Path    path;
    Bitmap  bitmap;
    float x1, y1;
    int w ,h;

    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.imageView);
        Display disp = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        disp.getSize(point);
        w = point.x;//横幅
        h = point.y;//縦幅

       bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        //αチャネル、RGB 各8bit

        paint = new Paint();
        path = new Path();
        canvas = new Canvas(bitmap);

        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawColor(Color.WHITE);
        iv.setImageBitmap(bitmap);
        iv.setOnTouchListener(this);


    }

    /*

    タッチイベントの取得
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(x,y);
                x1 = x;
                y1 = y;
                break;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(x1,y1,x,y);
                x1 = x;
                y1 = y;
                canvas.drawPath(path,paint);
                path.reset();
                path.moveTo(x,y);
                break;
            case MotionEvent.ACTION_UP:
                if( x == x1 && y == y1 ) y1= y1+1;
                path.quadTo(x1,y1,x,y);
                canvas.drawPath(path,paint);
                path.reset();
                break;
        }
        iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageBitmap(bitmap);

        return true;

    }


    //save
    public void save(){

        //テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(MainActivity.this);
         final String saveDir = Environment.getExternalStorageDirectory().getPath() + "/memo";
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("テキスト入力ダイアログ")
                        //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        //FileName
                        String filename = editView.getText().toString();

                        File file = new File(saveDir);
                        if (!file.exists()) {
                            file.mkdir();
                        }

                        File saveFile = new File(saveDir, filename + ".png");
                        try {
                            FileOutputStream fo = new FileOutputStream(saveFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fo);
                            fo.flush();
                            fo.close();

                            //ギャリーへ反映
                            ContentValues values = new ContentValues();
                            ContentResolver resolver = MainActivity.this.getContentResolver();
                            values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
                            values.put("_data",saveFile.getPath());
                            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(MainActivity.this, "保存しました", Toast.LENGTH_LONG).show();


                    }
                })
                .setNegativeButton("キャンセル", null)
                .show();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        //色を変える
        if( id == R.id.change_color){
            String[] colornames = getResources().getStringArray(R.array.color_name);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("色を選択してください")
                    .setItems(colornames, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TypedArray colors = getResources().obtainTypedArray(R.array.selectable_colors);
                                    int color = colors.getColor(0, 0);
                                    paint.setColor(colors.getColor(which, 0));
                                }
                            }
                    )
                    .create()
                    .show();
        }
        //保存
        if( id == R.id.save){
            save();
        }
        //リセット
        if( id == R.id.reset){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("確認")
                   .setMessage("クリアします。よろしいですか？")
                   .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {

                               bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                               canvas = new Canvas(bitmap);
                               iv.setImageBitmap(bitmap);
                           }
                       }
                    )
                    .setNegativeButton("NO",null)
                    .create()
                    .show();


        }

            return super.onOptionsItemSelected(item);
    }
}
