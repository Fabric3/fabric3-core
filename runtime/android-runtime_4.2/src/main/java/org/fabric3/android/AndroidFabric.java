package org.fabric3.android;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.InputStreamContributionSource;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.node.Fabric;
import org.fabric3.api.node.FabricException;
import org.fabric3.node.AbstractFabric;

import android.content.Context;

public class AndroidFabric extends AbstractFabric {

	private Context context;

	public AndroidFabric(String configSource, Context context) {
		super(new AndroidSource(context.getAssets(), configSource));
		this.context = context;
	}

	@Override
	public Fabric addProfile(URL location) {
		return this;
	}

	@Override
	public Fabric addExtension(URL location) {
		return this;
	}

	@Override
	protected ContributionSource createContributionSource(URI uri, Source location) {
		try {
			return new InputStreamContributionSource(uri, location.openStream());
		} catch (IOException e) {
			throw new FabricException(e);
		}
	}

	@Override
	protected ContributionSource createContributionSource(Source location) {
		return createContributionSource(URI.create("contrib:"+UUID.randomUUID()), location);
	}

	@Override
	protected Source resolveSystemConfiguration(Source configSource) {
		return configSource;
	}

	@Override
	protected List<File> scanClasspathForProfileArchives() throws IOException {
		return new ArrayList<File>();
	}

	@Override
	protected File getRepositoryDir() {
		return context.getFilesDir();
	}

	@Override
	protected File getTmpDir() {
		return context.getCacheDir();
	}

}
