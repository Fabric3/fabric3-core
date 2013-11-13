package org.fabric3.android;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.widget.TextView;

@RunWith(RobolectricTestRunner.class)
public class HelloAndroidActivityTest {

    @Test
    public void shouldDisplayHello() throws Exception {
    	HelloAndroidActivity activity = Robolectric.buildActivity(HelloAndroidActivity.class).create().get();
        String hello = activity.getResources().getString(R.string.hello);
        TextView helloView = (TextView) activity.findViewById(R.id.hello_view);
        assertEquals(helloView.getText(), hello);
    }
}