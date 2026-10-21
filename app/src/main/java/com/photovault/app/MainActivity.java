package com.photovault.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
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
    private static final int DELETE_MEDIA = 200;

    private static final String PIN = "1234";

    private final int BG = Color.rgb(13, 17, 23);
    private final int SURFACE = Color.rgb(22, 27, 34);
    private final int SURFACE_ALT = Color.rgb(33, 38, 45);
    private final int PRIMARY = Color.rgb(31, 111, 235);
    private final int DANGER = Color.rgb(218, 54, 51);
    private final int SUCCESS = Color.rgb(35, 134, 54);
    private final int TEXT = Color.rgb(230, 237, 243);
    private final int MUTED = Color.rgb(139, 148, 158);

    private LinearLayout galleryContainer;

    private final ArrayList<VaultItem> vaultItems =
            new ArrayList<>();

    private final ArrayList<Uri> pendingDeleteUris =
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
        root.setPadding(dp(30), dp(30), dp(30), dp(30));
        root.setBackgroundColor(BG);

        TextView icon = new TextView(this);
        icon.setText("🔐");
        icon.setTextSize(58);
        icon.setGravity(Gravity.CENTER);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(-1, dp(90))
        );

        TextView title = new TextView(this);
        title.setText("PhotoVault");
        title.setTextColor(TEXT);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        root.addView(
                title,
                new LinearLayout.LayoutParams(-1, dp(55))
        );

        TextView subtitle = new TextView(this);
        subtitle.setText("خزنة خاصة للصور والفيديوهات");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(-1, dp(45))
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
                new LinearLayout.LayoutParams(-1, dp(60))
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
                new LinearLayout.LayoutParams(-1, dp(55));

        unlockParams.topMargin = dp(15);

        root.addView(unlock, unlockParams);

        TextView demo = new TextView(this);
        demo.setText("PIN التجريبي: 1234");
        demo.setTextColor(MUTED);
        demo.setTextSize(12);
        demo.setGravity(Gravity.CENTER);

        root.addView(
                demo,
                new LinearLayout.LayoutParams(-1, dp(40))
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

        // HEADER

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

        lock.setOnClickListener(v -> showPinScreen());

        header.addView(
                lock,
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(50)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(-1, -2)
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
                new LinearLayout.LayoutParams(-1, -2)
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

        // ADD BUTTON

        Button add = new Button(this);
        add.setText("＋ إضافة صور وفيديوهات");
        add.setTextColor(Color.WHITE);
        add.setTextSize(16);
        add.setAllCaps(false);
        add.setBackground(
                roundedBackground(PRIMARY, 15)
        );

        add.setOnClickListener(v -> openGallery());

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

        root.addView(add, addParams);

        setContentView(root);

        refreshGallery();
    }

    // ============================================================
    // OPEN GALLERY
    // ============================================================

    private void openGallery() {

        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT
        );

        intent.setType("*/*");

        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                new String[]{
                        "image/*",
                        "video/*"
                }
        );

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        try {
            startActivityForResult(
                    Intent.createChooser(
                            intent,
                            "اختر الصور والفيديوهات"
                    ),
                    PICK_MEDIA
            );
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "تعذر فتح معرض الصور",
                    Toast.LENGTH_LONG
            ).show();
        }
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
                        data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {

                    Uri uri =
                            data.getClipData()
                                    .getItemAt(i)
                                    .getUri();

                    uris.add(uri);
                }

            } else if (data.getData() != null) {

                uris.add(data.getData());
            }

            if (!uris.isEmpty()) {

                importMedia(uris);
            }
        }

        if (requestCode == DELETE_MEDIA &&
                resultCode == RESULT_OK) {

            pendingDeleteUris.clear();

            Toast.makeText(
                    this,
                    "✅ تم حذف الملفات الأصلية من الهاتف",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ============================================================
    // IMPORT MEDIA
    // ============================================================

    private void importMedia(
            ArrayList<Uri> uris
    ) {

        int imported = 0;

        for (Uri uri : uris) {

            try {

                String mime =
                        getContentResolver()
                                .getType(uri);

                boolean isVideo =
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

                String extension =
                        getExtension(mime, isVideo);

                String name =
                        "vault_" +
                        System.currentTimeMillis() +
                        "_" +
                        imported +
                        extension;

                File destination =
                        new File(
                                vaultDir,
                                name
                        );

                copyUriToFile(
                        uri,
                        destination
                );

                if (destination.exists() &&
                        destination.length() > 0) {

                    imported++;

                    /*
                     * نحاول حذف الأصل من الهاتف.
                     */
                    boolean deleted =
                            deleteOriginal(uri);

                    if (!deleted &&
                            Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.R) {

                        pendingDeleteUris.add(uri);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        refreshGallery();

        if (imported > 0) {

            Toast.makeText(
                    this,
                    "✅ تمت إضافة " +
                    imported +
                    " ملف إلى الخزنة",
                    Toast.LENGTH_LONG
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "تعذر إضافة الملفات",
                    Toast.LENGTH_LONG
            ).show();
        }

        /*
         * Android 11+
         * يطلب من المستخدم تأكيد حذف
         * الملفات الأصلية.
         */
        if (!pendingDeleteUris.isEmpty() &&
                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R) {

            requestDeletePermission();
        }
    }

    // ============================================================
    // DELETE ORIGINAL
    // ============================================================

    private boolean deleteOriginal(Uri uri) {

        try {

            int deleted =
                    getContentResolver()
                            .delete(uri, null, null);

            return deleted > 0;

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // REQUEST DELETE
    // ============================================================

    private void requestDeletePermission() {

        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.R) {
            return;
        }

        try {

            IntentSenderWrapper wrapper =
                    new IntentSenderWrapper();

            wrapper.start(
                    pendingDeleteUris
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "تم نقل الملفات للخزنة، لكن يجب السماح بحذف الأصل",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // COPY URI TO PRIVATE FILE
    // ============================================================

    private void copyUriToFile(
            Uri uri,
            File destination
    ) throws Exception {

        InputStream input =
                getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            throw new Exception(
                    "Cannot open file"
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

            } else if (currentTab == 1 &&
                    item.video) {

                filtered.add(item);
            }
        }

        TextView count =
                new TextView(this);

        count.setText(
                currentTab == 0
                        ? "🖼️ الصور: " +
                          filtered.size()
                        : "🎥 الفيديوهات: " +
                          filtered.size()
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

            TextView empty =
                    new TextView(this);

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

        GridLayout grid =
                new GridLayout(this);

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

            String name =
                    file.getName()
                            .toLowerCase();

            boolean video =
                    name.endsWith(".mp4") ||
                    name.endsWith(".3gp") ||
                    name.endsWith(".mkv") ||
                    name.endsWith(".webm") ||
                    name.endsWith(".avi") ||
                    name.endsWith(".mov");

            vaultItems.add(
                    new VaultItem(
                            file,
                            video
                    )
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

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

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

        ImageView image =
                new ImageView(this);

        image.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        if (item.video) {

            Bitmap thumbnail =
                    ThumbnailUtils.createVideoThumbnail(
                            item.file.getAbsolutePath(),
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
                    Uri.fromFile(item.file)
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
                "حذف نهائي"
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

                                deletePermanently(item);
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
                    "لا يوجد تطبيق مناسب لفتح الملف",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // RESTORE FILE
    // ============================================================

    private void restoreFile(
            VaultItem item
    ) {

        new AlertDialog.Builder(this)
                .setTitle("إرجاع الملف")
                .setMessage(
                        "سيتم إرجاع الملف إلى معرض الهاتف وحذفه من الخزنة."
                )
                .setPositiveButton(
                        "إرجاع",
                        (dialog, which) -> {

                            if (restoreToGallery(item)) {

                                if (item.file.delete()) {

                                    refreshGallery();

                                    Toast.makeText(
                                            this,
                                            "✅ تم إرجاع الملف إلى الهاتف",
                                            Toast.LENGTH_LONG
                                    ).show();

                                } else {

                                    Toast.makeText(
                                            this,
                                            "تم إرجاع الملف، لكن تعذر حذف نسخة الخزنة",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }

                            } else {

                                Toast.makeText(
                                        this,
                                        "❌ تعذر إرجاع الملف",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    // ============================================================
    // RESTORE TO GALLERY
    // ============================================================

    private boolean restoreToGallery(
            VaultItem item
    ) {

        try {

            ContentValues values =
                    new ContentValues();

            values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    item.file.getName()
            );

            values.put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    item.video
                            ? "video/mp4"
                            : "image/jpeg"
            );

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q) {

                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        item.video
                                ? Environment.DIRECTORY_MOVIES
                                : Environment.DIRECTORY_PICTURES
                );
            }

            Uri collection =
                    item.video
                            ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Uri uri =
                    getContentResolver()
                            .insert(
                                    collection,
                                    values
                            );

            if (uri == null) {
                return false;
            }

            OutputStream output =
                    getContentResolver()
                            .openOutputStream(uri);

            if (output == null) {
                return false;
            }

            FileInputStream input =
                    new FileInputStream(
                            item.file
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

            input.close();
            output.flush();
            output.close();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    // ============================================================
    // DELETE PERMANENTLY
    // ============================================================

    private void deletePermanently(
            VaultItem item
    ) {

        new AlertDialog.Builder(this)
                .setTitle("حذف نهائي")
                .setMessage(
                        "هل تريد حذف هذا الملف نهائيًا من الخزنة؟"
                )
                .setPositiveButton(
                        "حذف",
                        (dialog, which) -> {

                            if (item.file.delete()) {

                                refreshGallery();

                                Toast.makeText(
                                        this,
                                        "🗑 تم حذف الملف نهائيًا",
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
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    // ============================================================
    // EXTENSION
    // ============================================================

    private String getExtension(
            String mime,
            boolean video
    ) {

        if (mime == null) {

            return video
                    ? ".mp4"
                    : ".jpg";
        }

        if (mime.equals("image/png")) {
            return ".png";
        }

        if (mime.equals("image/webp")) {
            return ".webp";
        }

        if (mime.equals("image/gif")) {
            return ".gif";
        }

        if (mime.equals("video/webm")) {
            return ".webm";
        }

        if (mime.equals("video/3gpp")) {
            return ".3gp";
        }

        if (mime.equals("video/quicktime")) {
            return ".mov";
        }

        return video
                ? ".mp4"
                : ".jpg";
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

    // ============================================================
    // DELETE REQUEST WRAPPER
    // ============================================================

    private class IntentSenderWrapper {

        void start(
                ArrayList<Uri> uris
        ) throws Exception {

            if (Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.R) {

                return;
            }

            android.app.PendingIntent
                    pendingIntent =
                    MediaStore.createDeleteRequest(
                            getContentResolver(),
                            uris
                    );

            startIntentSenderForResult(
                    pendingIntent.getIntentSender(),
                    DELETE_MEDIA,
                    null,
                    0,
                    0,
                    0
            );
        }
    }
}