package com.example.erickivet.securitypricedisplay;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by erickivet on 9/3/16.
 */
public class SecuritiesContract {

    public static final String AUTHORITY = "com.example.erickivet.stockportfolio.SecuritiesContentProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Securities implements BaseColumns{
        public static final String TABLE_SECURITIES = "securities";
        public static final String COLUMN_SECURITY_SYMBOL = "symbol";
        public static final String COLUMN_SECURITYNAME = "securityname";
        public static final String COLUMN_QUANT = "quant";
        public static final String COLUMN_EXCHANGE = "exchange";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, "securities");

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/vnd.com.example.erickivet.securities";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/vnd.com.example.erickivet.securities";
    }
}
