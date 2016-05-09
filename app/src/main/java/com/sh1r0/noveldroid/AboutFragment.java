package com.sh1r0.noveldroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutFragment extends Fragment {

	public AboutFragment() {
		// Required empty public constructor
	}

	public static AboutFragment newInstance(String version) {
		AboutFragment f = new AboutFragment();
		Bundle args = new Bundle();
		args.putString("version", version);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		getActivity().setTitle(R.string.about);

		Bundle args = getArguments();
		String version = args.getString("version");

		Element versionElement = new Element();
		versionElement.setTitle(getString(R.string.version_tag) + version);
		Element authorElement = new Element();
		authorElement.setTitle(getString(R.string.author_tag) + getString(R.string.author_name));

		View aboutPage = new AboutPage(getContext())
				.setDescription(getString(R.string.app_name))
				.setImage(R.drawable.icon)
				.addItem(versionElement)
				.addItem(authorElement)
				.addGitHub("sh1r0/NovelDroid")
				.create();
		return aboutPage;
	}

}
