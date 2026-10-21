package com.photovault.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PICK_MEDIA = 100;

    private static final String PIN = "1234";

    private final int BG = Color.rgb(13, 17, 23);
    private final int SURFACE = Color.rgb(22, 27, 34);
    private final int PRIMARY = Color.rgb(31, 111, 235);
    private final int DANGER = Color.rgb(218, 54, 51);
    private final int TEXT = Color.rgb(230, 237, 243);
    private final int MUTED = Color.rgb(139, 148, 158);

    private LinearLayout content;

    private final ArrayList<File> vaultFiles =
            new ArrayList<>();

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        loadFiles();

        showPinScreen();
    }

    // ============================================================
    // PIN
    // ============================================================

    private void showPinScreen() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(30),
                dp(30),
                dp(30),
                dp(30)
        );

        root.setBackgroundColor(BG);

        TextView icon =
                new TextView(this);

        icon.setText("🔐");
        icon.setTextSize(55);
        icon.setGravity(Gravity.CENTER);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(80)
                )
        );

        TextView title =
                new TextView(this);

        title.setText("PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "أدخل رمز PIN لفتح الخزنة"
        );

        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        EditText pin =
                new EditText(this);

        pin.setHint("PIN");
        pin.setHintTextColor(MUTED);
        pin.setTextColor(TEXT);
        pin.setTextSize(20);
        pin.setGravity(Gravity.CENTER);

        pin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        root.addView(
                pin,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        Button unlock =
                new Button(this);

        unlock.setText("فتح الخزنة");
        unlock.setTextColor(Color.WHITE);
        unlock.setTextSize(16);
        unlock.setAllCaps(false);

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();

        bg.setColor(PRIMARY);
        bg.setCornerRadius(dp(14));

        unlock.setBackground(bg);

        LinearLayout.LayoutParams up =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        up.topMargin = dp(15);

        root.addView(
                unlock,
                up
        );

        TextView hint =
                new TextView(this);

        hint.setText(
                "رمز التجربة: 1234"
        );

        hint.setTextColor(MUTED);
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);

        root.addView(
                hint,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        unlock.setOnClickListener(v -> {

            String value =
                    pin.getText()
                            .toString()
                            .trim();

            if (value.equals(PIN)) {

                showVault();

            } else {

                Toast.makeText(
                        this,
                        "❌ رمز PIN غير صحيح",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        setContentView(root);
    }

    // ============================================================
    // Vault
    // ============================================================

    private void showVault() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        // Header
        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(10)
        );

        header.setBackgroundColor(SURFACE);

        TextView title =
                new TextView(this);

        title.setText("🔐 PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(21);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button lock =
                new Button(this);

        lock.setText("🔒 قفل");
        lock.setAllCaps(false);

        lock.setOnClickListener(
                v -> showPinScreen()
        );

        header.addView(
                lock,
                new LinearLayout.LayoutParams(
                        -2,
                        dp(45)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // Scroll
        ScrollView scroll =
                new ScrollView(this);

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(30)
        );

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // Add button
        Button add =
                new Button(this);

        add.setText(
                "＋ إضافة صور وفيديوهات"
        );

        add.setTextColor(Color.WHITE);
        add.setTextSize(16);
        add.setAllCaps(false);

        android.graphics.drawable.GradientDrawable addBg =
                new android.graphics.drawable.GradientDrawable();

        addBg.setColor(PRIMARY);
        addBg.setCornerRadius(dp(14));

        add.setBackground(addBg);

        add.setOnClickListener(
                v -> openGallery()
        );

        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        addParams.setMargins(
                dp(12),
                dp(8),
                dp(12),
                dp(12)
        );

        root.addView(add, addParams);

        setContentView(root);

        refreshVault();
    }

    // ============================================================
    // Gallery
    // ============================================================

    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType("*/*");

        String[] types = {
                "image/*",
                "video/*"
        };

        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                types
        );

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        startActivityForResult(
                intent,
                PICK_MEDIA
        );
    }

    // ============================================================
    // Result
    // ============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != PICK_MEDIA ||
                resultCode != RESULT_OK ||
                data == null) {

            return;
        }

        int count = 0;

        try {

            if (data.getClipData() != null) {

                count =
                        data.getClipData()
                                .getItemCount();

                for (int i = 0;
                     i < count;
                     i++) {

                    Uri uri =
                            data.getClipData()
                                    .getItemAt(i)
                                    .getUri();

                    copyToVault(uri);
                }

            } else if (data.getData() != null) {

                count = 1;

                copyToVault(
                        data.getData()
                );
            }

            refreshVault();

            Toast.makeText(
                    this,
                    "تمت إضافة " + count + " ملف",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "حدث خطأ: " +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // Copy file
    // ============================================================

    private void copyToVault(Uri uri)
            throws Exception {

        File vault =
                new File(
                        getFilesDir(),
                        "vault"
                );

        if (!vault.exists()) {
            vault.mkdirs();
        }

        String extension =
                getExtension(uri);

        String timestamp =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss_SSS",
                        Locale.US
                ).format(new Date());

        String name =
                "vault_" +
                timestamp +
                extension;

        File destination =
                new File(vault, name);

        InputStream input =
                getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            throw new Exception(
                    "تعذر قراءة الملف"
            );
        }

        FileOutputStream output =
                new FileOutputStream(
                        destination
                );

        byte[] buffer =
                new byte[8192];

        int length;

        while ((length =
                input.read(buffer)) > 0) {

            output.write(
                    buffer,
                    0,
                    length
            );
        }

        output.flush();
        output.close();
        input.close();
    }

    // ============================================================
    // Extension
    // ============================================================

    private String getExtension(Uri uri) {

        String type =
                getContentResolver()
                        .getType(uri);

        if (type == null) {
            return ".dat";
        }

        if (type.equals("image/jpeg")) {
            return ".jpg";
        }

        if (type.equals("image/png")) {
            return ".png";
        }

        if (type.equals("image/webp")) {
            return ".webp";
        }

        if (type.equals("video/mp4")) {
            return ".mp4";
        }

        if (type.equals("video/webm")) {
            return ".webm";
        }

        return ".dat";
    }

    // ============================================================
    // Load files
    // ============================================================

    private void loadFiles() {

        vaultFiles.clear();

        File vault =
                new File(
                        getFilesDir(),
                        "vault"
                );

        if (!vault.exists()) {
            vault.mkdirs();
        }

        File[] files =
                vault.listFiles();

        if (files != null) {

            for (File file : files) {

                if (file.isFile()) {
                    vaultFiles.add(file);
                }
            }
        }
    }

    // ============================================================
    // Refresh vault
    // ============================================================

    private void refreshVault() {

        if (content == null) {
            return;
        }

        loadFiles();

        content.removeAllViews();

        TextView count =
                new TextView(this);

        count.setText(
                "🔒 الملفات داخل الخزنة: " +
                        vaultFiles.size()
        );

        count.setTextColor(TEXT);
        count.setTextSize(17);
        count.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        count.setPadding(
                dp(5),
                dp(10),
                dp(5),
                dp(15)
        );

        content.addView(count);

        if (vaultFiles.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "الخزنة فارغة\n\n" +
                    "أضف صورًا أو فيديوهات للبدء."
            );

            empty.setTextColor(MUTED);
            empty.setTextSize(16);
            empty.setGravity(
                    Gravity.CENTER
            );

            content.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(180)
                    )
            );

            return;
        }

        for (File file : vaultFiles) {

            addFileItem(file);
        }
    }

    // ============================================================
    // File item
    // ============================================================

    private void addFileItem(File file) {

        LinearLayout item =
                new LinearLayout(this);

        item.setGravity(
                Gravity.CENTER_VERTICAL
        );

        item.setPadding(
                dp(14),
                dp(8),
                dp(8),
                dp(8)
        );

        android.graphics.drawable.GradientDrawable itemBg =
                new android.graphics.drawable.GradientDrawable();

        itemBg.setColor(SURFACE);
        itemBg.setCornerRadius(dp(12));

        item.setBackground(itemBg);

        TextView info =
                new TextView(this);

        String name =
                file.getName();

        info.setText(
                "🔒  " + name
        );

        info.setTextColor(TEXT);
        info.setTextSize(14);

        item.addView(
                info,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        Button delete =
                new Button(this);

        delete.setText("حذف");
        delete.setTextColor(Color.WHITE);
        delete.setAllCaps(false);

        android.graphics.drawable.GradientDrawable deleteBg =
                new android.graphics.drawable.GradientDrawable();

        deleteBg.setColor(DANGER);
        deleteBg.setCornerRadius(dp(10));

        delete.setBackground(deleteBg);

        delete.setOnClickListener(
                v -> deleteFile(file)
        );

        item.addView(
                delete,
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(45)
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                );

        params.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        content.addView(
                item,
                params
        );
    }

    // ============================================================
    // Delete
    // ============================================================

    private void deleteFile(File file) {

        if (file.exists() &&
                file.delete()) {

            Toast.makeText(
                    this,
                    "تم حذف الملف من الخزنة",
                    Toast.LENGTH_SHORT
            ).show();

            refreshVault();

        } else {

            Toast.makeText(
                    this,
                    "تعذر حذف الملف",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}