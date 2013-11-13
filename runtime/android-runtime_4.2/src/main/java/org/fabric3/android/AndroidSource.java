package org.fabric3.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;

import android.content.res.AssetManager;
import android.os.Environment;

public class AndroidSource implements Source {
	
	private AssetManager assets;
	private String location;

	public AndroidSource(AssetManager assets, String loc) {
		this.assets = assets;
		this.location = loc;
	}

	@Override
	public String getSystemId() {
		return null;
	}

	@Override
	public URL getBaseLocation() {
		return null;
	}

	@Override
	public InputStream openStream() throws IOException {
		return assets.open(location);
	}

	@Override
	public Source getImportSource(String parentLocation, String importLocation) throws IOException {
		File dataDirectory = Environment.getDataDirectory();
		File parent = new File(dataDirectory, parentLocation);
		File importSource = new File(parent, parentLocation);
		return new UrlSource(importSource.toURL());
	}

	public AssetManager getAssets() {
		return assets;
	}
	

}
