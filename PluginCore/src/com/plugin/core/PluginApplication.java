package com.plugin.core;

import com.plugin.util.LogUtil;
import com.plugin.util.RefInvoker;

import android.app.Application;
import android.app.Instrumentation;
import android.os.Handler;

public class PluginApplication extends Application {
	
	private String mProcessName;
	
	public String getProcessName() {
		return mProcessName;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();	
		injectInstrumentation();
		injectClassLoader();
	}
	
	/**
	 * 注入Instrumentation主要是为了支持Activity
	 */
	private void injectInstrumentation() {
		LogUtil.d("injectInstrumentation");
		
		//从ThreadLocal中取出来的
		Object activityThread = RefInvoker.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread",
				(Class[])null, (Object[])null);
	
		mProcessName = (String)RefInvoker.invokeMethod(activityThread, "android.app.ActivityThread",
				"getProcessName", (Class[])null, (Object[])null);
	
		//给Instrumentation添加一层代理，用来实现隐藏api的调用
		Instrumentation originalInstrumentation = (Instrumentation)RefInvoker.getFieldObject(activityThread, "android.app.ActivityThread", "mInstrumentation");
		RefInvoker.setFieldObject(activityThread, "android.app.ActivityThread", "mInstrumentation",
				new PluginInstrumentionWrapper(originalInstrumentation));
		
		//getHandler
		Handler handler = (Handler)RefInvoker.getStaticFieldObject("android.app.ActivityThread", "sMainThreadHandler");
	
		//给handler添加一个callback
		RefInvoker.setFieldObject(handler, Handler.class.getName(), "mCallback", new PluginAppTrace(handler));
	
	}
	
	/**
	 * 注入classloader主要是为了支持Service和Receiver
	 */
	private void injectClassLoader() {
		//
		LogUtil.d("injectClassLoader");
	}
	

}
