package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCustomDate;   // 輸入 yyyy-MM-dd

    private StdDBHelper dbHelper;

    private final String[] foodNames = {
            "鐵板麵", "豬排", "牛排", "小卷排", "雞腿排",
            "綜合排", "厚重大比目魚", "黑胡椒鮮蝦排", "厚重豬排",
            "香辣厚豬排", "厚重牛排", "海陸大餐", "厚重菲力牛排",
            "頂級沙朗", "厚重雪花牛排"
    };

    private final double[] foodPrices = {
            70.0, 120.0, 130.0, 130.0, 130.0,
            140.0, 160.0, 160.0, 160.0,
            170.0, 170.0, 190.0, 220.0,
            220.0, 240.0
    };

    private Spinner[] spinners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new StdDBHelper(this);
        initSpinners();
        initButtons();
    }

    private void initSpinners() {
        spinners = new Spinner[]{
                findViewById(R.id.spinner1), findViewById(R.id.spinner2), findViewById(R.id.spinner3),
                findViewById(R.id.spinner4), findViewById(R.id.spinner5), findViewById(R.id.spinner6),
                findViewById(R.id.spinner7), findViewById(R.id.spinner8), findViewById(R.id.spinner9),
                findViewById(R.id.spinner10), findViewById(R.id.spinner11), findViewById(R.id.spinner12),
                findViewById(R.id.spinner13), findViewById(R.id.spinner14), findViewById(R.id.spinner15)
        };
    }

    private void initButtons() {
        Button buttonAdd = findViewById(R.id.buttonAdd);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        Button buttonShow = findViewById(R.id.buttonShow);
        Button buttonMulti = findViewById(R.id.buttonMulti);
        Button buttonCustomDate = findViewById(R.id.buttonCustomDate);
        editTextCustomDate = findViewById(R.id.editTextCustomDate);

        buttonAdd.setOnClickListener(view -> handleMenuItemAddition(null));
        buttonCustomDate.setOnClickListener(view -> handleMenuItemAddition(getCustomDateInput()));
        buttonDelete.setOnClickListener(view -> deleteAllMenuItems());
        buttonShow.setOnClickListener(view -> showMenuItems());
        buttonMulti.setOnClickListener(view -> showAllStatsInOneDialog());
    }

    private String getCustomDateInput() {
        String dateInput = editTextCustomDate.getText().toString().trim();
        if (dateInput.isEmpty()) {
            Toast.makeText(this, "請輸入日期 (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (!dateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "日期格式需為 yyyy-MM-dd，例如 2025-01-05", Toast.LENGTH_SHORT).show();
            return null;
        }
        return dateInput;
    }

    private void handleMenuItemAddition(String customDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int totalQuantity = 0;

        for (int i = 0; i < spinners.length; i++) {
            String selectedItem = spinners[i].getSelectedItem() != null ? spinners[i].getSelectedItem().toString() : null;
            if (selectedItem != null && selectedItem.endsWith("份")) {
                try {
                    int quantity = Integer.parseInt(selectedItem.replace("份", "").trim());
                    if (quantity > 0) {
                        ContentValues values = new ContentValues();
                        values.put("name", foodNames[i]);
                        values.put("price", foodPrices[i]);
                        values.put("quantity", quantity);
                        values.put("order_time", customDate != null ? customDate : getCurrentDate());

                        db.insert("menu_items", null, values);
                        totalQuantity += quantity;
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "解析 Spinner 時發生異常", e);
                }
            }
        }

        if (totalQuantity > 0) {
            Toast.makeText(this, "新增成功！總份數: " + totalQuantity, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "請選擇至少一個餐點", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDate() {
        String[] dateFormats = {
                "yyyy-M-dd", // 例如 2025-1-06
                "yyyy-MM-d", // 例如 2025-01-6
                "yyyy-MM-dd" // 標準格式 例如 2025-01-06
        };

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.format(new Date());
            } catch (Exception e) {
                // 在這裡捕捉並繼續嘗試其他格式
            }
        }

        // 若無法匹配，返回一個預設值或處理錯誤情況
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void deleteAllMenuItems() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM menu_items");
        Toast.makeText(this, "所有餐點已清空！", Toast.LENGTH_SHORT).show();
    }

    private void showMenuItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, price, quantity, order_time FROM menu_items", null);

        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder sb = new StringBuilder();
            double totalPrice = 0.0;

            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String otime = cursor.getString(cursor.getColumnIndexOrThrow("order_time"));

                double itemTotal = price * quantity;
                totalPrice += itemTotal;

                sb.append("餐點名稱: ").append(name)
                        .append(", 價格: $").append(price)
                        .append(", 數量: ").append(quantity)
                        .append(", 小計: $").append(itemTotal)
                        .append(", 時間: ").append(otime)
                        .append("\n");

            } while (cursor.moveToNext());

            cursor.close();
            sb.append("\n總金額: $").append(totalPrice);

            new AlertDialog.Builder(this)
                    .setTitle("餐點清單")
                    .setMessage(sb.toString())
                    .setPositiveButton("確定", null)
                    .show();
        } else {
            Toast.makeText(this, "目前沒有餐點資料", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllStatsInOneDialog() {
        int todayQty = getQtyForDays(1);
        double todayRevenue = getRevenueForDays(1);

        int threeDayQty = getQtyForDays(3);
        double threeDayRevenue = getRevenueForDays(3);

        int sevenDayQty = getQtyForDays(7);
        double sevenDayRevenue = getRevenueForDays(7);

        String message =
                "【當日】\n"
                        + "總品項數量: " + todayQty + "\n"
                        + "總營業額: $" + todayRevenue + "\n\n"
                        + "【三日】\n"
                        + "總品項數量: " + threeDayQty + "\n"
                        + "總營業額: $" + threeDayRevenue + "\n\n"
                        + "【七日】\n"
                        + "總品項數量: " + sevenDayQty + "\n"
                        + "總營業額: $" + sevenDayRevenue;

        new AlertDialog.Builder(this)
                .setTitle("多區間營業資訊")
                .setMessage(message)
                .setPositiveButton("確定", null)
                .show();
    }

    private int getQtyForDays(int days) {
        int totalQty = 0;
        String sql = "SELECT SUM(quantity) AS total_qty FROM menu_items "
                + "WHERE date(order_time) >= date('now','localtime','-" + (days - 1) + " day')";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        if (c != null) {
            if (c.moveToFirst()) {
                totalQty = c.getInt(c.getColumnIndexOrThrow("total_qty"));
            }
            c.close();
        }
        return totalQty;
    }

    private double getRevenueForDays(int days) {
        double totalRev = 0.0;
        String sql = "SELECT SUM(price * quantity) AS total_revenue FROM menu_items "
                + "WHERE date(order_time) >= date('now','localtime','-" + (days - 1) + " day')";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        if (c != null) {
            if (c.moveToFirst()) {
                totalRev = c.getDouble(c.getColumnIndexOrThrow("total_revenue"));
            }
            c.close();
        }
        return totalRev;
    }
}