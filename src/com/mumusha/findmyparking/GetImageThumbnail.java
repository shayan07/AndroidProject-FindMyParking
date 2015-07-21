package com.mumusha.findmyparking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class GetImageThumbnail {

private static int getPowerOfTwoForSampleRatio(double ratio) {
    int k = Integer.highestOneBit((int) Math.floor(ratio));
    if (k == 0)
        return 1;
    else
        return k;
}

public Bitmap getThumbnail(Uri uri, Context context)
        throws FileNotFoundException, IOException {
    InputStream input = context.getContentResolver().openInputStream(uri);

    BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
    onlyBoundsOptions.inJustDecodeBounds = true;
    onlyBoundsOptions.inDither = true;// optional
    onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
    BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
    input.close();
    if ((onlyBoundsOptions.outWidth == -1)
            || (onlyBoundsOptions.outHeight == -1))
        return null;

    int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
            : onlyBoundsOptions.outWidth;

    double ratio = (originalSize > 400) ? (originalSize / 350) : 1.0;

    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
    bitmapOptions.inDither = true;// optional
    bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
    input = context.getContentResolver().openInputStream(uri);
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
    input.close();
    return bitmap;
}
}
