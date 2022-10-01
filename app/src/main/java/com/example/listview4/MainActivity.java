package com.example.listview4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView listViewFood;
    private String[] foodNameArray;
    private TypedArray foodPictureArray;
    private ArrayList<Map<String, Object>> listFood;
    String TAG = "mytag";
    private SimpleAdapter adapter;
    private int[] foodPriceArray;
    private Intent intent;
    private ActivityResultLauncher<Intent> getResult;
    private TextView textViewResult;
    private final int ReturnCode = 100;
    private String[] dessertNameArray;
    private TypedArray dessertPicArray;
    private int[] dessertPriceArray;
    private Button buttonMainCourse;
    private Button buttonDessert;
    private DBHelper dbHelper;
    private SQLiteDatabase orderDatabase;
    private Cursor dataCursor;
    private int count;
    private ContentValues contentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityAreaInit();                 //mainActivity所有元件初始化
        listViewDataInit();                     //將listView內的資料初始化
        setListViewAdapter();                   //設定listView的Adapter
        setListViewClickListener();             //監聽listView
        intentInit();                           //將intent初始化，指向DisplayActivity
        getIntentBackWithValue();               //接收DisplayActivity回傳的資料
        setButtonMainCourseClickListener();     //監聽主菜的按鈕
        setButtonDessertClickListener();        //監聽點心的按鈕
        orderDatabaseInit();                    //將資料庫初始化，但裡面還沒有資料。
        insertDataIntoDatabase();               //將資料放入資料庫。
        orderDatabase.close();                  //要瞭解生命週期，當intent去另一頁時，並沒有跳到onDestroy()，所以選擇在onCreate()的最後關掉database。
    }
    private void insertDataIntoDatabase() {
        dataCursor = orderDatabase.rawQuery("SELECT foodname FROM order_list", null);
        count = dataCursor.getCount();
        Log.d(TAG, "count = "+count);
        if(count == 0){     //如果資料庫裡資料筆數等於0，那就把資料放入；如果有資料，表示已經放過了，就不用再放一次。
            int number = 1;
            for(int i=0; i<foodNameArray.length; i++){
                contentValue = new ContentValues();
                contentValue.put("_id",number);
                number ++;
                contentValue.put("foodname", foodNameArray[i]);
                contentValue.put("price", foodPriceArray[i]);
                contentValue.put("volumn", 0);
                long id = orderDatabase.insert("order_list", null, contentValue);
                Log.d(TAG, "id = "+id);
            }
            for(int i=0; i<dessertNameArray.length ;i++){
                contentValue = new ContentValues();
                contentValue.put("_id",number);
                number ++;
                contentValue.put("foodname", dessertNameArray[i]);
                contentValue.put("price", dessertPriceArray[i]);
                contentValue.put("volumn", 0);
                long id = orderDatabase.insert("order_list", null, contentValue);
                Log.d(TAG, "id = "+id);
            }
        }else{  //如果資料庫有資料，就做個清除，讓資料庫重置，下一個使用者使用時，就可以重新再計算。
            //-----------以下是老師的寫法，又臭又長--------------------
//            for(int i=1; i<= count; i++){
//                contentValue = new ContentValues();
//                contentValue.put("volumn", 0);  //把資料庫每筆資料的volumn都歸0，方便下一次使用。
//                String[] data = {String.valueOf(i)};
//                int count_ok = orderDatabase.update("order_list", contentValue, "_id=?", data);
//                Log.d(TAG, "count = "+count_ok);
//            }
            //-------------以上是老師的寫法，又臭又長
            orderDatabase.execSQL("UPDATE order_list SET volumn=0 WHERE _id>0;");   //斌哥分享，用資料庫語法更新資料庫資料，更簡潔!!!
        }
    }

    private void orderDatabaseInit() {
        dbHelper = new DBHelper(MainActivity.this);
        orderDatabase = dbHelper.getWritableDatabase();
    }

    private void setButtonDessertClickListener() {
        buttonDessert.setOnClickListener(new MyButtonClass());
    }

    private void setButtonMainCourseClickListener() {
        buttonMainCourse.setOnClickListener(new MyButtonClass());
    }

    private void getIntentBackWithValue() {
        getResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == ReturnCode){
                    //----------下面這塊備註的區塊，是沒有使用資料庫的原始寫法------------------
//                    Intent intentResult = result.getData();
//                    String foodName = intentResult.getStringExtra("food_name");
//                    String foodResult = intentResult.getStringExtra("result");
//                    int totalSum = intentResult.getIntExtra("sum", 0);
//                    Log.d(TAG, "foodName = "+foodName);
//                    Log.d(TAG, "foodResult = "+foodResult);
//                    Log.d(TAG, "TotalSum = "+totalSum);
//                    textViewResult.append(foodName+" : "+foodResult+"\n");
                    //----------上面這塊備註的區塊，是沒有使用資料庫的原始寫法，所以當我們要使用資料庫時，就不需要上面這段code了------------------
                    orderDatabase = dbHelper.getWritableDatabase();
                    dataCursor = orderDatabase.rawQuery("SELECT _id, foodname, price, volumn FROM order_list", null);
                    count = dataCursor.getCount();  //取得資料庫有多少筆資料
//                    Log.d(TAG, "order count = "+count);
                    dataCursor.moveToFirst();
                    textViewResult.setText("");     //要取出要顯示的文字前，先做清除。
                    int sum =0;
                    while(!dataCursor.isAfterLast()){
                        int number = dataCursor.getInt(dataCursor.getColumnIndexOrThrow("volumn")); //取得每筆資料的volumn數
                        Log.d(TAG, "number = "+number);
                        if(number>0){   //當volumn大於0，等於使用者有下單，我們再來綜整。
                            String name = dataCursor.getString(dataCursor.getColumnIndexOrThrow("foodname"));
                            Log.d(TAG, "foodname ="+ name);
                            int price = dataCursor.getInt(dataCursor.getColumnIndexOrThrow("price"));
                            Log.d(TAG, "price = "+price);
                            textViewResult.append(name+" : "+"$"+price+" * "+number+" = "+(number*price)+"\n");
                            sum = sum+number*price;
                        }
                        dataCursor.moveToNext();
                    }
                    textViewResult.append("Total fee : $"+sum);
                }
            }
        });
    }

    private void intentInit() {
        intent = new Intent(MainActivity.this, DisplayActivity.class);
//        startActivities(intent); //這裡不用這方法，因為還要接收回傳的資料，必須用其它方式，參考自訂的getIntentBackWithValue()
    }

    private void setListViewClickListener() {
        listViewFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(position);
                String food = item.get("food").toString();
                int picture_id = (int) item.get("picture");
//                int price = foodPriceArray[position];
                int price = (int) item.get("price");
                intent.putExtra("food", food);
                intent.putExtra("price", price);
                intent.putExtra("picture", picture_id);
//                startActivity(intent);    //如果還要接收回傳資料，就不能用startActivity
                getResult.launch(intent);
            }
        });
    }

    private void setListViewAdapter() {
        adapter = new SimpleAdapter(MainActivity.this, listFood, R.layout.item_layout, new String[]{"food", "price", "picture"},
                new int[]{R.id.textView_name, R.id.textView_price, R.id.imageView_pic});
        listViewFood.setAdapter(adapter);
    }

    private void mainActivityAreaInit() {
        listViewFood = (ListView) findViewById(R.id.listView_id);               //
        foodNameArray = getResources().getStringArray(R.array.food_name);
        foodPriceArray = getResources().getIntArray(R.array.food_price);
        foodPictureArray = getResources().obtainTypedArray(R.array.food_pic);
        textViewResult = (TextView) findViewById(R.id.textView_result);
        dessertNameArray = getResources().getStringArray(R.array.dessert_name);
        dessertPriceArray = getResources().getIntArray(R.array.dessert_price);
        dessertPicArray = getResources().obtainTypedArray(R.array.dessert_pic);
        buttonMainCourse = (Button) findViewById(R.id.button_mainCourse);
        buttonDessert = (Button) findViewById(R.id.button_dessert);
        textViewResult.setText("");
    }

    private void listViewDataInit() {
        listFood = new ArrayList<Map<String, Object>>();
        for(int i=0; i<foodNameArray.length; i++){
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("food", foodNameArray[i]);
            data.put("price",foodPriceArray[i]);
            data.put("picture", foodPictureArray.getResourceId(i,0));
            listFood.add(data);
        }
    }

    private class MyButtonClass implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.button_mainCourse:
                    listFood.clear();                       //可以試試看，如果沒有清除的話，會發生什麼事。
                    for(int i=0; i<foodNameArray.length; i++){
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("food", foodNameArray[i]);
                        data.put("price",foodPriceArray[i]);
                        data.put("picture", foodPictureArray.getResourceId(i,0));
                        listFood.add(data);
                    }
                    adapter.notifyDataSetChanged();         //通知adapter，list裡的data有改變哦。
                    break;
                case R.id.button_dessert:
                    listFood.clear();                       //可以試試看，如果沒有清除的話，會發生什麼事。
                    for(int i=0; i<dessertNameArray.length; i++){
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("food", dessertNameArray[i]);
                        data.put("price", dessertPriceArray[i]);
                        int picId = dessertPicArray.getResourceId(i, 0);
                        data.put("picture", picId);
                        Log.d(TAG, "dessert = "+dessertNameArray[i]);
                        Log.d(TAG, "price = "+dessertPriceArray[i]);
                        listFood.add(data);
                    }
                    adapter.notifyDataSetChanged();         //通知adapter，list裡的data有改變哦。
                    break;
            }
        }
    }
}