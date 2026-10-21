package com.photovault.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int PICK_MEDIA = 100;

    private static final String PIN = "1234";

    private final int BG = Color.rgb(13, 17, 23);
    private final int SURFACE = Color.rgb(22, 27, 34);
    private final int PRIMARY = Color.rgb(31, 111, 235);
    private final int DANGER = Color.rgb(218, 54, 51);
    private final int TEXT = Color.rgb(230, 237, 243);
    private final int MUTED = Color.rgb(139, 148, 158);

    private LinearLayout galleryContainer;

    private final ArrayList<VaultItem> vaultItems =
            new ArrayList<>();

    private int currentTab = 0;

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

        showPinScreen();
    }

    // ============================================================
    // PIN
    // ============================================================

    private void showPinScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(
                dp(30),
                dp(30),
                dp(30),
                dp(30)
        );
        root.setBackgroundColor(BG);

        TextView icon = new TextView(this);
        icon.setText("🔐");
        icon.setTextSize(58);
        icon.setGravity(Gravity.CENTER);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(90)
                )
        );

        TextView title = new TextView(this);
        title.setText("PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(30);
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

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "خزنة خاصة للصور والفيديوهات"
        );
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );

        EditText pin = new EditText(this);
        pin.setHint("أدخل PIN");
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

        Button unlock = new Button(this);
        unlock.setText("فتح الخزنة");
        unlock.setTextColor(Color.WHITE);
        unlock.setTextSize(16);
        unlock.setAllCaps(false);

        unlock.setBackground(
                roundedBackground(PRIMARY, 14)
        );

        LinearLayout.LayoutParams unlockParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        unlockParams.topMargin = dp(15);

        root.addView(
                unlock,
                unlockParams
        );

        TextView demo = new TextView(this);
        demo.setText("PIN التجريبي: 1234");
        demo.setTextColor(MUTED);
        demo.setTextSize(12);
        demo.setGravity(Gravity.CENTER);

        root.addView(
                demo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        unlock.setOnClickListener(v -> {

            String entered =
                    pin.getText().toString().trim();

            if (entered.equals(PIN)) {
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
    // VAULT
    // ============================================================

    private void showVault() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);

        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(
                dp(16),
                dp(8),
                dp(10),
                dp(8)
        );

        header.setBackgroundColor(SURFACE);

        TextView title = new TextView(this);
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

        Button lock = new Button(this);
        lock.setText("🔒");
        lock.setTextSize(18);
        lock.setAllCaps(false);

        lock.setOnClickListener(
                v -> showPinScreen()
        );

        header.addView(
                lock,
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(50)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // ========================================================
        // TABS
        // ========================================================

        LinearLayout tabs = new LinearLayout(this);

        tabs.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(5)
        );

        Button photos = new Button(this);
        photos.setText("🖼️ الصور");
        photos.setAllCaps(false);

        Button videos = new Button(this);
        videos.setText("🎥 الفيديوهات");
        videos.setAllCaps(false);

        tabs.addView(
                photos,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        tabs.addView(
                videos,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        root.addView(
                tabs,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        photos.setOnClickListener(v -> {
            currentTab = 0;
            refreshGallery();
        });

        videos.setOnClickListener(v -> {
            currentTab = 1;
            refreshGallery();
        });

        // ========================================================
        // GALLERY
        // ========================================================

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        galleryContainer = new LinearLayout(this);

        galleryContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        galleryContainer.setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(100)
        );

        scroll.addView(galleryContainer);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // ========================================================
        // ADD BUTTON
        // ========================================================

        Button add = new Button(this);

        add.setText("＋ إضافة صور وفيديوهات");
        add.setTextColor(Color.WHITE);
        add.setTextSize(16);
        add.setAllCaps(false);

        add.setBackground(
                roundedBackground(PRIMARY, 15)
        );

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
                dp(5),
                dp(12),
                dp(10)
        );

        root.addView(
                add,
                addParams
        );

        setContentView(root);

        refreshGallery();
    }

    // ============================================================
    // REFRESH
    // ============================================================

    private void refreshGallery() {

        if (galleryContainer == null) {
            return;
        }

        loadVault();

        galleryContainer.removeAllViews();

        ArrayList<VaultItem> filtered =
                new ArrayList<>();

        for (VaultItem item : vaultItems) {

            if (currentTab == 0 && !item.video) {
                filtered.add(item);
            }

            if (currentTab == 1 && item.video) {
                filtered.add(item);
            }
        }

        TextView count = new TextView(this);

        count.setText(
                currentTab == 0
                        ? "🖼️ الصور: " + filtered.size()
                        : "🎥 الفيديوهات: " + filtered.size()
        );

        count.setTextColor(TEXT);
        count.setTextSize(17);
        count.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        count.setPadding(
                dp(5),
                dp(8),
                dp(5),
                dp(12)
        );

        galleryContainer.addView(count);

        if (filtered.isEmpty()) {

            TextView empty = new TextView(this);

            empty.setText(
                    currentTab == 0
                            ? "لا توجد صور في الخزنة\n\nاضغط + لإضافة صور"
                            : "لا توجد فيديوهات في الخزنة\n\nاضغط + لإضافة فيديوهات"
            );

            empty.setTextColor(MUTED);
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);

            galleryContainer.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(220)
                    )
            );

            return;
        }

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);

        galleryContainer.addView(
                grid,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        for (VaultItem item : filtered) {
            addThumbnail(grid, item);
        }
    }

    // ============================================================
    // THUMBNAIL
    // ============================================================

    private void addThumbnail(
            GridLayout grid,
            VaultItem item
    ) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(Gravity.CENTER);

        card.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
        );

        card.setBackground(
                roundedBackground(SURFACE, 10)
        );

        ImageView image = new ImageView(this);

        image.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        File file = item.file;

        if (item.video) {

            Bitmap thumbnail =
                    ThumbnailUtils.createVideoThumbnail(
                            file.getAbsolutePath(),
                            MediaStore.Video.Thumbnails.MINI_KIND
                    );

            if (thumbnail != null) {
                image.setImageBitmap(thumbnail);
            } else {
                image.setImageResource(
                        android.R.drawable.ic_media_play
                );
            }

        } else {

            image.setImageURI(
                    Uri.fromFile(file)
            );
        }

        FrameLayout frame = new FrameLayout(this);

        frame.addView(
                image,
                new FrameLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        if (item.video) {

            TextView play = new TextView(this);

            play.setText("▶");
            play.setTextColor(Color.WHITE);
            play.setTextSize(28);
            play.setGravity(Gravity.CENTER);

            frame.addView(
                    play,
                    new FrameLayout.LayoutParams(
                            -1,
                            dp(105)
                    )
            );
        }

        card.addView(
                frame,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        TextView name = new TextView(this);

        name.setText(item.file.getName());
        name.setTextColor(MUTED);
        name.setTextSize(9);
        name.setGravity(Gravity.CENTER);

        card.addView(
                name,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        card.setOnClickListener(
                v -> showFileMenu(item)
        );

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

        params.width = 0;
        params.height = dp(140);

        params.columnSpec =
                GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                );

        params.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
        );

        grid.addView(card, params);
    }

    // ============================================================
    // MENU
    // ============================================================

    private void showFileMenu(VaultItem item) {

        String[] options = {
                "فتح",
                "إرجاع إلى الهاتف",
                "حذف نهائياً"
        };

        new AlertDialog.Builder(this)
                .setTitle(
                        item.video
                                ? "🎥 الفيديو"
                                : "🖼️ الصورة"
                )
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                openFile(item);

                            } else if (which == 1) {

                                restoreFile(item);

                            } else {

                                deleteVaultFile(item);
                            }
                        }
                )
                .show();
    }

    // ============================================================
    // OPEN FILE
    // ============================================================

    private void openFile(VaultItem item) {

        Toast.makeText(
                this,
                "الملف موجود داخل الخزنة",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ============================================================
    // PICKER
    // ============================================================

    private void openGallery() {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("*/*");

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                new String[]{
                        "image/*",
                        "video/*"
                }
        );

        startActivityForResult(
                intent,
                PICK_MEDIA
        );
    }

    // ============================================================
    // RESULT
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

        ArrayList<Uri> uris =
                new ArrayList<>();

        if (data.getClipData() != null) {

            int count =
                    data.getClipData()
                            .getItemCount();

            for (int i = 0; i < count; i++) {

                uris.add(
                        data.getClipData()
                                .getItemAt(i)
                                .getUri()
                );
            }

        } else if (data.getData() != null) {

            uris.add(data.getData());
        }

        for (Uri uri : uris) {

            moveToVault(uri);
        }

        refreshGallery();
    }

    // ============================================================
    // MOVE TO VAULT
    // ============================================================

    private void moveToVault(Uri uri) {

        try {

            String name =
                    getFileName(uri);

            if (name == null ||
                    name.trim().isEmpty()) {

                name =
                        "media_" +
                        System.currentTimeMillis();
            }

            String mime =
                    getContentResolver()
                            .getType(uri);

            boolean video =
                    mime != null &&
                    mime.startsWith("video");

            File vaultDir =
                    new File(
                            getFilesDir(),
                            "vault"
                    );

            if (!vaultDir.exists()) {
                vaultDir.mkdirs();
            }

            String safeName =
                    System.currentTimeMillis()
                            + "_" +
                            name;

            File destination =
                    new File(
                            vaultDir,
                            safeName
                    );

            InputStream input =
                    getContentResolver()
                            .openInputStream(uri);

            if (input == null) {
                return;
            }

            FileOutputStream output =
                    new FileOutputStream(destination);

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            output.flush();
            output.close();
            input.close();

            // بعد نجاح النسخ، نحاول حذف الأصل
            deleteOriginal(uri);

            Toast.makeText(
                    this,
                    "تم نقل الملف إلى الخزنة",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "حدث خطأ أثناء نقل الملف",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // DELETE ORIGINAL
    // ============================================================

    private void deleteOriginal(Uri uri) {

        try {

            ContentResolver resolver =
                    getContentResolver();

            if (Build.VERSION.SDK_INT >= 30) {

                ArrayList<Uri> list =
                        new ArrayList<>();

                list.add(uri);

                Intent intent =
                        MediaStore.createDeleteRequest(
                                resolver,
                                list
                        );

                startActivityForResult(
                        intent,
                        300
                );

            } else {

                resolver.delete(
                        uri,
                        null,
                        null
                );
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "تم حفظ نسخة داخل الخزنة، لكن يجب تأكيد حذف الأصل من النظام",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // RESTORE
    // ============================================================

    private void restoreFile(VaultItem item) {

        new AlertDialog.Builder(this)
                .setTitle("إرجاع الملف")
                .setMessage(
                        "هل تريد إعادة هذا الملف إلى معرض الهاتف؟"
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .setPositiveButton(
                        "إرجاع",
                        (dialog, which) -> {

                            restoreToGallery(item);
                        }
                )
                .show();
    }

    // ============================================================
    // RESTORE TO GALLERY
    // ============================================================

    private void restoreToGallery(
            VaultItem item
    ) {

        try {

            ContentResolver resolver =
                    getContentResolver();

            String mime =
                    item.video
                            ? "video/mp4"
                            : "image/jpeg";

            ContentValues values =
                    new ContentValues();

            values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    item.originalName
            );

            values.put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    mime
            );

            if (Build.VERSION.SDK_INT >= 29) {

                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        item.video
                                ? Environment.DIRECTORY_MOVIES
                                : Environment.DIRECTORY_PICTURES
                );

                values.put(
                        MediaStore.MediaColumns.IS_PENDING,
                        1
                );
            }

            Uri collection =
                    item.video
                            ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Uri newUri =
                    resolver.insert(
                            collection,
                            values
                    );

            if (newUri == null) {
                throw new Exception(
                        "Cannot create MediaStore item"
                );
            }

            OutputStream output =
                    resolver.openOutputStream(newUri);

            if (output == null) {
                throw new Exception(
                        "Cannot open output"
                );
            }

            FileInputStream input =
                    new FileInputStream(item.file);

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            input.close();
            output.flush();
            output.close();

            if (Build.VERSION.SDK_INT >= 29) {

                ContentValues done =
                        new ContentValues();

                done.put(
                        MediaStore.MediaColumns.IS_PENDING,
                        0
                );

                resolver.update(
                        newUri,
                        done,
                        null,
                        null
                );
            }

            if (item.file.exists()) {
                item.file.delete();
            }

            refreshGallery();

            Toast.makeText(
                    this,
                    "✅ تم إرجاع الملف إلى الهاتف",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "❌ تعذر إرجاع الملف",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // DELETE FROM VAULT
    // ============================================================

    private void deleteVaultFile(
            VaultItem item
    ) {

        new AlertDialog.Builder(this)
                .setTitle("حذف نهائي")
                .setMessage(
                        "سيتم حذف الملف نهائياً من الخزنة. هل أنت متأكد؟"
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .setPositiveButton(
                        "حذف",
                        (dialog, which) -> {

                            if (item.file.exists()) {

                                item.file.delete();
                            }

                            refreshGallery();

                            Toast.makeText(
                                    this,
                                    "تم حذف الملف نهائياً",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    // ============================================================
    // LOAD VAULT
    // ============================================================

    private void loadVault() {

        vaultItems.clear();

        File vaultDir =
                new File(
                        getFilesDir(),
                        "vault"
                );

        if (!vaultDir.exists()) {
            return;
        }

        File[] files =
                vaultDir.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (!file.isFile()) {
                continue;
            }

            String name =
                    file.getName().toLowerCase();

            boolean video =
                    name.endsWith(".mp4") ||
                    name.endsWith(".3gp") ||
                    name.endsWith(".mkv") ||
                    name.endsWith(".webm") ||
                    name.endsWith(".avi");

            String originalName =
                    name;

            int separator =
                    name.indexOf("_");

            if (separator >= 0 &&
                    separator + 1 < name.length()) {

                originalName =
                        file.getName()
                                .substring(
                                        separator + 1
                                );
            }

            vaultItems.add(
                    new VaultItem(
                            file,
                            video,
                            originalName
                    )
            );
        }
    }

    // ============================================================
    // GET FILE NAME
    // ============================================================

    private String getFileName(Uri uri) {

        String result = null;

        Cursor cursor = null;

        try {

            cursor =
                    getContentResolver()
                            .query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null
                            );

            if (cursor != null &&
                    cursor.moveToFirst()) {

                int index =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (index >= 0) {

                    result =
                            cursor.getString(index);
                }
            }

        } catch (Exception ignored) {

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        if (result == null) {

            result =
                    uri.getLastPathSegment();
        }

        return result;
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    private android.graphics.drawable.GradientDrawable
    roundedBackground(
            int color,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(
                dp(radius)
        );

        return drawable;
    }

    // ============================================================
    // VAULT ITEM
    // ============================================================

    private static class VaultItem {

        File file;

        boolean video;

        String originalName;

        VaultItem(
                File file,
                boolean video,
                String originalName
        ) {

            this.file = file;
            this.video = video;
            this.originalName = originalName;
        }
    }
}