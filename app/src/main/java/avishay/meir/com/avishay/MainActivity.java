package avishay.meir.com.avishay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    static public Metadata metadata;
    Activity activity;
    Context context;
    private ArrayList<String> images;
    String html;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        context = this;

        images = new ArrayList();
        GridView gallery = (GridView) findViewById(R.id.galleryGridView);

        gallery.setAdapter(new ImageAdapter(this));

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty())
                    Toast.makeText(
                            getApplicationContext(),
                            "position " + position + " " + images.get(position),
                            Toast.LENGTH_LONG).show();
                ;

            }
        });

    }
    /**
     * The Class ImageAdapter.
     */
    private class ImageAdapter extends BaseAdapter {

        /** The context. */
        private Activity context;

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext
         *            the local context
         */
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;

            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(screenWidth/2, screenWidth/2));

            } else {
                picturesView = (ImageView) convertView;

            }
            picturesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageView iv = (ImageView)view;
                    int pos = position;
                    String fname = images.get(pos);
                    try {
                        ExifInterface exif = new ExifInterface(fname);
                        String a =exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String b =exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String c =exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String d =exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                        File jpegFile = new File(fname);
                        metadata = ImageMetadataReader.readMetadata(jpegFile);
                        html = "";
                        for (Directory directory : metadata.getDirectories()) {
                            boolean dateSent = false;
                            for (Tag tag : directory.getTags()) {
//                                Log.d("EXIF", String.valueOf(tag));

//                                Log.d("EXIF",directory.getName() + " >" + tag.getTagName()+" : " + tag.getDescription().toString());
                                Date date = null;
                                if (!dateSent)
                                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                                dateSent = true;

                                ExifToHtml(date,directory.getName(),tag.getTagName(),tag.getDescription());
                                if (date != null)
                                    date = null;
                            }
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ImageProcessingException e) {
                        e.printStackTrace();
                    }
                }
            });

            Glide.with(context).load(images.get(position))
                    .placeholder(R.drawable.av1).centerCrop()
                    .listener(new RequestListener<String, GlideDrawable>() {

                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource,
                                                       String model, Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            ///Here I need to obtain the cached disk images based on the url path
                            images.add(model);
                            return false;
                        }
                    })
                    .into(picturesView);


            return picturesView;
        }

        String prevDirectory = "";

        void ExifToHtml(Date date,String directory,String tag,String description){

            String fDate="";
            if (date != null) {
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                fDate = dateFormat.format(date);
            }


            html += "<style> header, footer {\n" +
                    "    padding: 1em;\n" +
                    "    color: white;\n" +
                    "    background-color: green;\n" +
                    "    clear: left;\n" +
                    "    text-align: center;\n" +
                    "}" +
                    "</style> <div style='border:2px blue solid; font-size:12px;'>";

            String dir = directory;
            if (!prevDirectory.contains(directory))
                dir = "<header><h2>"+directory+"</h2></header>";
            else
                dir = "";

            html +=  fDate.length() > 0 ? "<h1>"+fDate+"</h1>" : ""+ //(B ? Y : Z);?"<h1>"+fDate+"</h1>"+
                    dir+
                "<b>"+tag+"</b><br>"+
                "<p>"+description+"</p>";
            html += "</div>";
            prevDirectory = directory;

            fDate = "";

            Intent intent = new Intent(context, ShowXml.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("html",html);
            startActivity(intent);

        }
        /**
         * Getting All Images Path.
         *
         * @param activity
         *            the activity
         * @return ArrayList with images Path
         */
        private ArrayList<String> getAllShownImagesPath(Activity activity) {
            Uri uri;
            Cursor cursor;
            int column_index_data, column_index_folder_name;
            ArrayList<String> listOfAllImages = new ArrayList<String>();
            String absolutePathOfImage = null;
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

            cursor = activity.getContentResolver().query(uri, projection, null,
                    null, null);

            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);

                listOfAllImages.add(absolutePathOfImage);
            }
            return listOfAllImages;
        }
    }

}