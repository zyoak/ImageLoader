/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.sample.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.sample.Constants;
import com.nostra13.universalimageloader.sample.R;
import com.nostra13.universalimageloader.sample.fragment.ImageGalleryFragment;
import com.nostra13.universalimageloader.sample.fragment.ImageGridFragment;
import com.nostra13.universalimageloader.sample.fragment.ImageListFragment;
import com.nostra13.universalimageloader.sample.fragment.ImagePagerFragment;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class HomeActivity extends Activity {

	private static final String TEST_FILE_NAME = "imageloader.png";

	private ImageView iv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_home);

		iv = findViewById(R.id.iv);

		File testImageOnSdCard = new File("/mnt/sdcard", TEST_FILE_NAME);
		if (!testImageOnSdCard.exists()) {
			copyTestImageToSdCard(testImageOnSdCard);
		}
	}

	public void onImageListClick(View view) {
		Intent intent = new Intent(this, SimpleImageActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageListFragment.INDEX);
		startActivity(intent);
	}

	public void onImageGridClick(View view) {
		Intent intent = new Intent(this, SimpleImageActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageGridFragment.INDEX);
		startActivity(intent);
	}

	public void onImagePagerClick(View view) {
		Intent intent = new Intent(this, SimpleImageActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImagePagerFragment.INDEX);
		startActivity(intent);
	}

	public void onImageGalleryClick(View view) {
		Intent intent = new Intent(this, SimpleImageActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageGalleryFragment.INDEX);
		startActivity(intent);
	}

	public void onFragmentsClick(View view) {
		Intent intent = new Intent(this, ComplexImageActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		ImageLoader.getInstance().stop();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.item_clear_memory_cache:
				ImageLoader.getInstance().clearMemoryCache();
				return true;
			case R.id.item_clear_disc_cache:
				ImageLoader.getInstance().clearDiskCache();
				return true;
			default:
				return false;
		}
	}

	private void copyTestImageToSdCard(final File testImageOnSdCard) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = getAssets().open(TEST_FILE_NAME);
					FileOutputStream fos = new FileOutputStream(testImageOnSdCard);
					byte[] buffer = new byte[8192];
					int read;
					try {
						while ((read = is.read(buffer)) != -1) {
							fos.write(buffer, 0, read);
						}
					} finally {
						fos.flush();
						fos.close();
						is.close();
					}
				} catch (IOException e) {
					L.w("Can't copy test image onto SD card");
				}
			}
		}).start();
	}


	public void saveFile(View view){
		Log.e("HomeActivity" , Environment.getExternalStorageDirectory().getAbsolutePath());
		Log.e("HomeActivity" , getCacheDir().getAbsolutePath());
		try {
			DiskLruCache cache = DiskLruCache.open(getCacheDir() , 1 , 2 , 1024*1024*100 , 100);
			Md5FileNameGenerator nameGenerator = new Md5FileNameGenerator();
			DiskLruCache.Editor editor = cache.edit(nameGenerator.generate("imageloader.png"));

			Log.e("HomeActivity" , nameGenerator.generate("imageloader.png"));

			OutputStream fos = editor.newOutputStream(0);
			IoUtils.copyStream(getAssets().open("imageloader.png"), fos, null);
			IoUtils.closeSilently(fos);

			fos = editor.newOutputStream(1);
			IoUtils.copyStream(getAssets().open("living.jpg") , fos , null);
			IoUtils.closeSilently(fos);
			editor.commit();

		} catch (IOException e) {
			e.printStackTrace();
		}


	}


	public void showImg(View view){
		try {
			DiskLruCache cache = DiskLruCache.open(getCacheDir() , 1 , 2 , 1024*1024*100 , 100);
			Md5FileNameGenerator nameGenerator = new Md5FileNameGenerator();
//			DiskLruCache.Editor editor = cache.edit(nameGenerator.generate("imageloader.png"));
//			InputStream fis = editor.newInputStream(0);

			DiskLruCache.Snapshot snapshot = cache.get(nameGenerator.generate("imageloader.png"));
			InputStream fis = snapshot.getInputStream(0);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = 32;
			Bitmap bitmap = BitmapFactory.decodeStream(fis , new Rect(0 , 0 ,100 , 100) , options);
			iv.setImageBitmap(bitmap);
			snapshot.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}