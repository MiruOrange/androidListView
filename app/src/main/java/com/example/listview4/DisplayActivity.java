package com.example.listview4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayActivity extends AppCompatActivity {

    private ImageView imageViewPic;
    private TextView textViewName;
    private String name;
    private int price, picture;
    private ImageButton imageButtonMinus,imageButtonPlus, imageButtonOK;
    private TextView textViewNumber;
    private int number;
    private int sum;
    private String result;
    private final int ReturnCode = 100;
    String TAG = "mytag";
    private DBHelper dbHelper;
    private SQLiteDatabase orderDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        orderDatabaseInit();            //在一進此頁面時，就把資料庫打開
        //記得在onDestroy()要關閉，離開此頁面時關閉資料庫。


        displayActivityInit();          //將display的頁面所有元件做初始化
        getIntentValue();               //接收MainActivity傳送過來的資料
        displayIntentValue();           //將接收的資料呈現出來
        actionBarInit();                //將actionBar初始化
        //**!!重要性!**下一步是設定onOptionsItemSelected的函式，它與onCreate同等級，所以沒有寫在這裡，在最下面。要設定完成，按下actionBar才會回去home。
        setImageButtonClickListener();  //監聽圖片按鈕，+號、-號以及ok等圖片按鈕。
        //特別注意ok圖片按鈕按下的設定，使用alertDialog
        //另外，按下alertDialog的ok後，將值返回到主程式
    }

    private void orderDatabaseInit() {
        dbHelper = new DBHelper(DisplayActivity.this); //資料庫其實也可以宣告成local，好處是不用在離開時還要將資料庫關閉
        orderDatabase = dbHelper.getWritableDatabase();
    }

    private void setImageButtonClickListener() {
        number = 1;
        imageButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number>0){
                    number = number-1;
                    textViewNumber.setText(String.valueOf(number));
                }
            }
        });
        imageButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number <50){
                    number = number +1;
                    textViewNumber.setText(String.valueOf(number));
                }
            }
        });
        imageButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DisplayActivity.this);
                builder.setTitle(name);
                sum = price*number;
                result = "$ "+price +" x "+number+" = "+sum;
                builder.setMessage(String.valueOf(price)+" * "+number+"="+sum);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateOrderDataToDatabase();
                        //重要!!使用者點選ok後，這個function就會把資料更新到資料庫。
                        //數量增加會減少都會有同樣的效果。

                        textViewName.setText(name+" : " +price+"\n");
                        textViewName.append(result);
                        sendValueBackToMain();      //這裡是把資料送回到main。
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
    }

    private void updateOrderDataToDatabase() {
        ContentValues contentValue = new ContentValues();
        contentValue.put("volumn", number);
        String[] data = {name};
        int count = orderDatabase.update("order_list", contentValue, "foodname=?", data);
        Log.d(TAG, "update count ="+count);
    }

    private void sendValueBackToMain() {
        Intent intent = new Intent();
        intent.putExtra("food_name", name);
        intent.putExtra("result", result);
        intent.putExtra("sum", sum);
        setResult(ReturnCode, intent);
    }

    private void actionBarInit() {
        ActionBar actBar = getSupportActionBar();
        actBar.setDisplayHomeAsUpEnabled(true);
    }

    private void displayIntentValue() {
        imageViewPic.setImageResource(picture);
        setTitle(name);
        textViewName.setText(name+" : $"+price);
    }

    private void getIntentValue() {
        Intent intent = getIntent();
        name = intent.getStringExtra("food");
        price = intent.getIntExtra("price", 0);
        picture = intent.getIntExtra("picture", R.drawable.blacktea);
    }

    private void displayActivityInit() {
        imageViewPic = (ImageView) findViewById(R.id.imageView_display_id);
        textViewName = (TextView) findViewById(R.id.textView_display_name);
        imageButtonMinus = (ImageButton) findViewById(R.id.imageButton_display_minus);
        imageButtonPlus = (ImageButton) findViewById(R.id.imageButton_display_plus);
        imageButtonOK = (ImageButton) findViewById(R.id.imageButton_display_ok);
        textViewNumber = (TextView) findViewById(R.id.textView_display_number);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        orderDatabase.close();
        super.onDestroy();
    }
}