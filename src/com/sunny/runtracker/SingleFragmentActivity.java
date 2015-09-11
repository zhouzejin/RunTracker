package com.sunny.runtracker;

import com.baidu.mapapi.SDKInitializer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {
	
	protected int getLayoutResId() {
		return R.layout.activity_fragment;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        // 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext()); // 使用百度地图API需要改设置
		// setContentView(R.layout.activity_fragment);
		setContentView(getLayoutResId());
		
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		
		if (null == fragment) {
			fragment = createFragment();
			fm.beginTransaction()
				.add(R.id.fragment_container, fragment)
				.commit();
		}
	}

	abstract protected Fragment createFragment();

}
