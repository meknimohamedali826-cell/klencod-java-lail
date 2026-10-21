package com.photovault.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
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
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PICK_MEDIA = 100;
    private static final int DELETE_MEDIA = 200;

    private static final String PIN = "1234";

    private final int BG = Color.rgb(13, 17, 23);
    private final int SURFACE = Color.rgb(22, 27, 34);
    private final int SURFACE_ALT = Color.rgb(33, 38, 45);
    private final int PRIMARY = Color.rgb(31, 111, 235);
    private final int DANGER = Color.rgb(218, 54, 51);
    private final int TEXT = Color.rgb(230, 237, 243);
    private final int MUTED = Color.rgb(139, 148, 158);

    private LinearLayout galleryContainer;

    private final ArrayList<VaultItem> vaultItems =
            new ArrayList<>();

    private final ArrayList<Uri> pendingDeleteUris =
            new ArrayList<>();

    private int currentTab = 0;

    // ============================================================
    // DP
    // ============================================================

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

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

        root.setOrientation(
                LinearLayout.VERTICAL
        );

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

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        // HEADER
        LinearLayout header = new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

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

        // TABS
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

        // SCROLL
        ScrollView scroll = new ScrollView(this);

        scroll.setFillViewport(true);

        galleryContainer =
                new LinearLayout(this);

        galleryContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        galleryContainer.setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(100)
        );

        scroll.addView(
                galleryContainer
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // ADD
        Button add = new Button(this);

        add.setText(
                "＋ إضافة صور وفيديوهات"
        );

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

            if (currentTab == 0 &&
                    !item.video) {

                filtered.add(item);
            }

            if (currentTab == 1 &&
                    item.video) {

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

            addThumbnail(
                    grid,
                    item
            );
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
                roundedBackground(
                        SURFACE,
                        10
                )
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

                image.setImageBitmap(
                        thumbnail
                );

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

        FrameLayout frame =
                new FrameLayout(this);

        frame.addView(
                image,
                new FrameLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        if (item.video) {

            TextView play =
                    new TextView(this);

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

        TextView name =
                new TextView(this);

        name.setText(
                item.file.getName()
        );

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

        grid.addView(
                card,
                params
        );
    }

    // ============================================================
    // FILE MENU
    // ============================================================

    private void showFileMenu(
            VaultItem item
    ) {

        String[] options = {
                "فتح",
                "إرجاع إلى الهاتف",
                "حذف نهائياً من الخزنة"
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

    private void openFile(
            VaultItem item
    ) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            Uri uri =
                    Uri.fromFile(
                            item.file
                    );

            intent.setDataAndType(
                    uri,
                    item.video
                            ? "video/*"
                            : "image/*"
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "تعذر فتح الملف",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ============================================================
    // SELECT PHOTOS / VIDEOS
    // ============================================================

    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

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

        if (requestCode == PICK_MEDIA &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<Uri> uris =
                    new ArrayList<>();

            if (data.getClipData() != null) {

                int count =
                        data.getClipData()
                                .getItemCount();

                for (int i = 0;
                     i < count;
                     i++) {

                    uris.add(
                            data.getClipData()
                                    .getItemAt(i)
                                    .getUri()
                    );
                }

            } else if (data.getData() != null) {

                uris.add(
                        data.getData()
                );
            }

            int imported = 0;

            for (Uri uri : uris) {

                if (copyToVault(uri)) {

                    imported++;
                }
            }

            Toast.makeText(
                    this,
                    "تمت إضافة " +
                            imported +
                            " ملف إلى الخزنة",
                    Toast.LENGTH_SHORT
            ).show();

            refreshGallery();
        }

        if (requestCode == DELETE_MEDIA &&
                resultCode == RESULT_OK) {

            for (Uri uri :
                    pendingDeleteUris) {

                try {

                    getContentResolver()
                            .delete(
                                    uri,
                                    null,
                                    null
                            );

                } catch (Exception ignored) {
                }
            }

            pendingDeleteUris.clear();

            Toast.makeText(
                    this,
                    "تم حذف الملفات",
                    Toast.LENGTH_SHORT
            ).show();

            refreshGallery();
        }
    }

    // ============================================================
    // COPY TO VAULT
    // ============================================================

    private boolean copyToVault(
            Uri sourceUri
    ) {

        String fileName =
                getFileName(sourceUri);

        if (fileName == null ||
                fileName.trim().isEmpty()) {

            fileName =
                    "file_" +
                    System.currentTimeMillis();
        }

        String extension =
                getExtension(fileName);

        boolean video =
                isVideo(sourceUri);

        if (extension.isEmpty()) {

            extension =
                    video
                            ? ".mp4"
                            : ".jpg";
        }

        String safeName =
                "vault_" +
                System.currentTimeMillis() +
                "_" +
                Math.abs(
                        fileName.hashCode()
                ) +
                extension;

        File vaultDir =
                new File(
                        getFilesDir(),
                        "vault"
                );

        if (!vaultDir.exists()) {

            if (!vaultDir.mkdirs()) {

                Toast.makeText(
                        this,
                        "تعذر إنشاء الخزنة",
                        Toast.LENGTH_SHORT
                ).show();

                return false;
            }
        }

        File destination =
                new File(
                        vaultDir,
                        safeName
                );

        try {

            InputStream input =
                    getContentResolver()
                            .openInputStream(
                                    sourceUri
                            );

            if (input == null) {
                return false;
            }

            OutputStream output =
                    new FileOutputStream(
                            destination
                    );

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

            return true;

        } catch (Exception e) {

            if (destination.exists()) {
                destination.delete();
            }

            return false;
        }
    }

    // ============================================================
    // RESTORE
    // ============================================================

    private void restoreFile(
            VaultItem item
    ) {

        ContentResolver resolver =
                getContentResolver();

        String mime =
                item.video
                        ? "video/mp4"
                        : "image/jpeg";

        String displayName =
                getRestoreName(
                        item.file.getName(),
                        item.video
                );

        ContentValues values =
                new ContentValues();

        values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                displayName
        );

        values.put(
                MediaStore.MediaColumns.MIME_TYPE,
                mime
        );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q) {

            values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    item.video
                            ? "Movies/PhotoVault"
                            : "Pictures/PhotoVault"
            );

            values.put(
                    MediaStore.MediaColumns.IS_PENDING,
                    1
            );
        }

        Uri collection;

        if (item.video) {

            collection =
                    MediaStore.Video.Media
                            .getContentUri(
                                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                            );

        } else {

            collection =
                    MediaStore.Images.Media
                            .getContentUri(
                                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                            );
        }

        Uri destination =
                resolver.insert(
                        collection,
                        values
                );

        if (destination == null) {

            Toast.makeText(
                    this,
                    "تعذر إرجاع الملف",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            InputStream input =
                    new FileInputStream(
                            item.file
                    );

            OutputStream output =
                    resolver.openOutputStream(
                            destination
                    );

            if (output == null) {

                input.close();

                resolver.delete(
                        destination,
                        null,
                        null
                );

                return;
            }

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

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q) {

                ContentValues done =
                        new ContentValues();

                done.put(
                        MediaStore.MediaColumns.IS_PENDING,
                        0
                );

                resolver.update(
                        destination,
                        done,
                        null,
                        null
                );
            }

            if (item.file.exists()) {

                item.file.delete();
            }

            Toast.makeText(
                    this,
                    "تم إرجاع الملف إلى الهاتف",
                    Toast.LENGTH_SHORT
            ).show();

            refreshGallery();

        } catch (Exception e) {

            resolver.delete(
                    destination,
                    null,
                    null
            );

            Toast.makeText(
                    this,
                    "حدث خطأ أثناء إرجاع الملف",
                    Toast.LENGTH_SHORT
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
                        "هل تريد حذف هذا الملف نهائياً من الخزنة؟"
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .setPositiveButton(
                        "حذف",
                        (dialog, which) -> {

                            if (item.file.exists()) {

                                if (item.file.delete()) {

                                    Toast.makeText(
                                            this,
                                            "تم حذف الملف نهائياً",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {

                                    Toast.makeText(
                                            this,
                                            "تعذر حذف الملف",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            refreshGallery();
                        }
                )
                .show();
    }

    // ============================================================
    // DELETE MEDIA FROM PHONE
    // ============================================================

    private void deleteFromPhone(
            ArrayList<Uri> uris
    ) {

        if (uris == null ||
                uris.isEmpty()) {

            return;
        }

        pendingDeleteUris.clear();

        pendingDeleteUris.addAll(
                uris
        );

        try {

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.R) {

                PendingIntent pendingIntent =
                        MediaStore.createDeleteRequest(
                                getContentResolver(),
                                pendingDeleteUris
                        );

                startIntentSenderForResult(
                        pendingIntent.getIntentSender(),
                        DELETE_MEDIA,
                        null,
                        0,
                        0,
                        0
                );

            } else {

                for (Uri uri : uris) {

                    try {

                        getContentResolver()
                                .delete(
                                        uri,
                                        null,
                                        null
                                );

                    } catch (Exception ignored) {
                    }
                }

                pendingDeleteUris.clear();

                Toast.makeText(
                        this,
                        "تم حذف الملفات",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "تعذر طلب حذف الملفات",
                    Toast.LENGTH_SHORT
            ).show();

            pendingDeleteUris.clear();
        }
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

            vaultDir.mkdirs();

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

            boolean video =
                    isVideoFile(
                            file.getName()
                    );

            vaultItems.add(
                    new VaultItem(
                            file,
                            video
                    )
            );
        }
    }

    // ============================================================
    // FILE NAME
    // ============================================================

    private String getFileName(
            Uri uri
    ) {

        String result = null;

        try {

            Cursor cursor =
                    getContentResolver()
                            .query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null
                            );

            if (cursor != null) {

                int index =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (index >= 0 &&
                        cursor.moveToFirst()) {

                    result =
                            cursor.getString(index);
                }

                cursor.close();
            }

        } catch (Exception ignored) {
        }

        if (result == null) {

            result =
                    uri.getLastPathSegment();
        }

        return result;
    }

    // ============================================================
    // EXTENSION
    // ============================================================

    private String getExtension(
            String name
    ) {

        if (name == null) {
            return "";
        }

        int dot =
                name.lastIndexOf(".");

        if (dot < 0) {
            return "";
        }

        return name.substring(dot)
                .toLowerCase(
                        Locale.US
                );
    }

    // ============================================================
    // IS VIDEO
    // ============================================================

    private boolean isVideo(
            Uri uri
    ) {

        try {

            String type =
                    getContentResolver()
                            .getType(uri);

            if (type != null) {

                return type.startsWith(
                        "video/"
                );
            }

        } catch (Exception ignored) {
        }

        String name =
                getFileName(uri);

        if (name == null) {
            return false;
        }

        return isVideoFile(name);
    }

    // ============================================================
    // VIDEO FILE
    // ============================================================

    private boolean isVideoFile(
            String name
    ) {

        String ext =
                getExtension(name);

        return ext.equals(".mp4") ||
                ext.equals(".mkv") ||
                ext.equals(".3gp") ||
                ext.equals(".webm") ||
                ext.equals(".avi") ||
                ext.equals(".mov") ||
                ext.equals(".m4v");
    }

    // ============================================================
    // RESTORE NAME
    // ============================================================

    private String getRestoreName(
            String name,
            boolean video
    ) {

        String extension =
                getExtension(name);

        if (extension.isEmpty()) {

            extension =
                    video
                            ? ".mp4"
                            : ".jpg";
        }

        String base =
                "PhotoVault_" +
                System.currentTimeMillis();

        return base + extension;
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

        VaultItem(
                File file,
                boolean video
        ) {

            this.file = file;
            this.video = video;
        }
    }
}