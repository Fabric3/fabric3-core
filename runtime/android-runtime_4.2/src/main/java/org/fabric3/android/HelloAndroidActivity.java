package org.fabric3.android;

import org.fabric3.api.node.Fabric;

import android.app.Activity;
import android.os.Bundle;

public class HelloAndroidActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {   
        	Fabric fabric = new AndroidFabric("f3.config.xml", this);
        	fabric.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

