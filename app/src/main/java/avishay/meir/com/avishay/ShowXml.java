package avishay.meir.com.avishay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class ShowXml extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle == null) {
        setContentView(R.layout.show_xml);



            WebView wv = (WebView) findViewById(R.id.myWebView);
            Button close = (Button) findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowXml.this.finish();
                }
            });

            final String mimeType = "text/html";
            final String encoding = "UTF-8";
            String html = getIntent().getStringExtra("html");


            wv.loadDataWithBaseURL("", html, mimeType, encoding, "");
        }
    }

    @Override
    public void onBackPressed() {
        // Write your code here
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}