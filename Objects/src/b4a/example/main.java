package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public anywheresoftware.b4a.objects.ButtonWrapper _fulladbutton = null;
public anywheresoftware.b4a.objects.ButtonWrapper _rewardvideoadbutton = null;
public anywheresoftware.b4a.objects.ButtonWrapper _closablevideoadbutton = null;
public anywheresoftware.b4a.objects.ButtonWrapper _generalvideoadbutton = null;
public ir.adad.androidsdk.b4a.AdadB4aPlugin _adad = null;
public b4a.example.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 37;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 41;BA.debugLine="adad.initializeAdad(\"64674edf2e2741d8b4cf5dadc674";
mostCurrent._adad.initializeAdad(mostCurrent.activityBA,"64674edf2e2741d8b4cf5dadc674c4ce");
 //BA.debugLineNum = 43;BA.debugLine="adad.addBannerAdToActivity(\"805da553-1801-4ad7-88";
mostCurrent._adad.addBannerAdToActivity(mostCurrent.activityBA,"805da553-1801-4ad7-8843-9c91ab25e8f0","bottom"," center");
 //BA.debugLineNum = 45;BA.debugLine="adad.addBannerAdToActivity(\"805da553-1801-4ad7-88";
mostCurrent._adad.addBannerAdToActivity(mostCurrent.activityBA,"805da553-1801-4ad7-8843-9c91ab25e8f0","top"," center");
 //BA.debugLineNum = 47;BA.debugLine="adad.prepareFullscreenBannerAd(\"2F54A2B2-B85D-490";
mostCurrent._adad.prepareFullscreenBannerAd(mostCurrent.activityBA,"2F54A2B2-B85D-490D-B59C-5CF4AEDD0F3C");
 //BA.debugLineNum = 49;BA.debugLine="fullAdButton.Initialize(\"fullAdButton\")";
mostCurrent._fulladbutton.Initialize(mostCurrent.activityBA,"fullAdButton");
 //BA.debugLineNum = 50;BA.debugLine="fullAdButton.Text = \"Show fullscreen Banner Ad\"";
mostCurrent._fulladbutton.setText(BA.ObjectToCharSequence("Show fullscreen Banner Ad"));
 //BA.debugLineNum = 51;BA.debugLine="Activity.AddView(fullAdButton, 0, 10%y, 100dip, 1";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._fulladbutton.getObject()),(int) (0),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)));
 //BA.debugLineNum = 53;BA.debugLine="adad.prepareRewardVideoAd(\"b0ffb55b-c999-432a-a10";
mostCurrent._adad.prepareRewardVideoAd(mostCurrent.activityBA,"b0ffb55b-c999-432a-a109-ab5033ffbd32");
 //BA.debugLineNum = 55;BA.debugLine="rewardVideoAdButton.Initialize(\"rewardVideoAdButt";
mostCurrent._rewardvideoadbutton.Initialize(mostCurrent.activityBA,"rewardVideoAdButton");
 //BA.debugLineNum = 56;BA.debugLine="rewardVideoAdButton.Text = \"Show Reward Video Ad\"";
mostCurrent._rewardvideoadbutton.setText(BA.ObjectToCharSequence("Show Reward Video Ad"));
 //BA.debugLineNum = 57;BA.debugLine="Activity.AddView(rewardVideoAdButton, 20%x, 10%y,";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._rewardvideoadbutton.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)));
 //BA.debugLineNum = 60;BA.debugLine="adad.prepareClosableVideoAd(\"42474279-d0ba-4f00-a";
mostCurrent._adad.prepareClosableVideoAd(mostCurrent.activityBA,"42474279-d0ba-4f00-ae5d-2827d5eb3771");
 //BA.debugLineNum = 61;BA.debugLine="closableVideoAdButton.Initialize(\"closableVideoAd";
mostCurrent._closablevideoadbutton.Initialize(mostCurrent.activityBA,"closableVideoAdButton");
 //BA.debugLineNum = 62;BA.debugLine="closableVideoAdButton.Text = \"Show Closable Video";
mostCurrent._closablevideoadbutton.setText(BA.ObjectToCharSequence("Show Closable Video Ad"));
 //BA.debugLineNum = 63;BA.debugLine="Activity.AddView(closableVideoAdButton, 0, 20%y,";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._closablevideoadbutton.getObject()),(int) (0),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)));
 //BA.debugLineNum = 66;BA.debugLine="adad.prepareGeneralVideoAd(\"42474279-d0ba-4f00-ae";
mostCurrent._adad.prepareGeneralVideoAd(mostCurrent.activityBA,"42474279-d0ba-4f00-ae5d-2827d5eb3771");
 //BA.debugLineNum = 67;BA.debugLine="generalVideoAdButton.Initialize(\"generalVideoAdBu";
mostCurrent._generalvideoadbutton.Initialize(mostCurrent.activityBA,"generalVideoAdButton");
 //BA.debugLineNum = 68;BA.debugLine="generalVideoAdButton.Text = \"Show General Video A";
mostCurrent._generalvideoadbutton.setText(BA.ObjectToCharSequence("Show General Video Ad"));
 //BA.debugLineNum = 69;BA.debugLine="Activity.AddView(generalVideoAdButton, 20%x, 20%y";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._generalvideoadbutton.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)));
 //BA.debugLineNum = 71;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 77;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 79;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 73;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 75;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadbanneradactionoccurred(int _id,int _code) throws Exception{
 //BA.debugLineNum = 139;BA.debugLine="Sub adadevent_adadbanneradactionoccurred(id As Int";
 //BA.debugLineNum = 140;BA.debugLine="Log(\"banner ad action occurred event with id -> \"";
anywheresoftware.b4a.keywords.Common.Log("banner ad action occurred event with id -> "+BA.NumberToString(_id));
 //BA.debugLineNum = 141;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadbanneraddestroy(int _id) throws Exception{
 //BA.debugLineNum = 131;BA.debugLine="Sub adadevent_adadbanneraddestroy(id As Int)";
 //BA.debugLineNum = 132;BA.debugLine="Log(\"banner ad destroy event with id -> \" & id)";
anywheresoftware.b4a.keywords.Common.Log("banner ad destroy event with id -> "+BA.NumberToString(_id));
 //BA.debugLineNum = 133;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadbanneraderror(int _id,int _errorcode,String _errormessage) throws Exception{
 //BA.debugLineNum = 127;BA.debugLine="Sub adadevent_adadbanneraderror(id As Int, errorCo";
 //BA.debugLineNum = 129;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadbanneradhide(int _id) throws Exception{
 //BA.debugLineNum = 135;BA.debugLine="Sub adadevent_adadbanneradhide(id As Int)";
 //BA.debugLineNum = 136;BA.debugLine="Log(\"banner ad hide event with id -> \" & id)";
anywheresoftware.b4a.keywords.Common.Log("banner ad hide event with id -> "+BA.NumberToString(_id));
 //BA.debugLineNum = 137;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadbanneradload(int _id) throws Exception{
 //BA.debugLineNum = 119;BA.debugLine="Sub adadevent_adadbanneradload(id As Int)";
 //BA.debugLineNum = 120;BA.debugLine="Log(\"banner ad load event with id -> \" & id)";
anywheresoftware.b4a.keywords.Common.Log("banner ad load event with id -> "+BA.NumberToString(_id));
 //BA.debugLineNum = 121;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadbanneradshow(int _id) throws Exception{
 //BA.debugLineNum = 123;BA.debugLine="Sub adadevent_adadbanneradshow(id As Int)";
 //BA.debugLineNum = 124;BA.debugLine="Log(\"banner ad show event with id -> \" & id)";
anywheresoftware.b4a.keywords.Common.Log("banner ad show event with id -> "+BA.NumberToString(_id));
 //BA.debugLineNum = 125;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoadactionoccurred(int _code) throws Exception{
 //BA.debugLineNum = 229;BA.debugLine="Sub adadevent_adadclosablevideoadactionoccurred(co";
 //BA.debugLineNum = 230;BA.debugLine="Log(\"closable video ad action occurred event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad action occurred event");
 //BA.debugLineNum = 231;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoadclose() throws Exception{
 //BA.debugLineNum = 237;BA.debugLine="Sub adadevent_adadclosablevideoadclose";
 //BA.debugLineNum = 238;BA.debugLine="Log(\"closable video ad close event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad close event");
 //BA.debugLineNum = 239;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoadcomplete() throws Exception{
 //BA.debugLineNum = 241;BA.debugLine="Sub adadevent_adadclosablevideoadcomplete";
 //BA.debugLineNum = 242;BA.debugLine="Log(\"closable video ad complete event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad complete event");
 //BA.debugLineNum = 243;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoaddestroy() throws Exception{
 //BA.debugLineNum = 233;BA.debugLine="Sub adadevent_adadclosablevideoaddestroy";
 //BA.debugLineNum = 234;BA.debugLine="Log(\"closable video ad destroy event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad destroy event");
 //BA.debugLineNum = 235;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoaderror(int _errorcode,String _errormessage) throws Exception{
 //BA.debugLineNum = 225;BA.debugLine="Sub adadevent_adadclosablevideoaderror(errorCode A";
 //BA.debugLineNum = 226;BA.debugLine="Log(\"video ad error event -> \" & errorMessage)";
anywheresoftware.b4a.keywords.Common.Log("video ad error event -> "+_errormessage);
 //BA.debugLineNum = 227;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoadload() throws Exception{
 //BA.debugLineNum = 217;BA.debugLine="Sub adadevent_adadclosablevideoadload";
 //BA.debugLineNum = 218;BA.debugLine="Log(\"closable video ad load event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad load event");
 //BA.debugLineNum = 219;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoadshow() throws Exception{
 //BA.debugLineNum = 221;BA.debugLine="Sub adadevent_adadclosablevideoadshow";
 //BA.debugLineNum = 222;BA.debugLine="Log(\"closable video ad show event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad show event");
 //BA.debugLineNum = 223;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadclosablevideoadstart() throws Exception{
 //BA.debugLineNum = 245;BA.debugLine="Sub adadevent_adadclosablevideoadstart";
 //BA.debugLineNum = 246;BA.debugLine="Log(\"closable video ad start event\")";
anywheresoftware.b4a.keywords.Common.Log("closable video ad start event");
 //BA.debugLineNum = 247;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadfullscreenadactionoccurred(int _code) throws Exception{
 //BA.debugLineNum = 170;BA.debugLine="Sub adadevent_adadfullscreenadactionoccurred(code";
 //BA.debugLineNum = 171;BA.debugLine="Log(\"full screen ad action occurred event\")";
anywheresoftware.b4a.keywords.Common.Log("full screen ad action occurred event");
 //BA.debugLineNum = 172;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadfullscreenadclose() throws Exception{
 //BA.debugLineNum = 166;BA.debugLine="Sub adadevent_adadfullscreenadclose";
 //BA.debugLineNum = 167;BA.debugLine="Log(\"full screen ad close event\")";
anywheresoftware.b4a.keywords.Common.Log("full screen ad close event");
 //BA.debugLineNum = 168;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadfullscreenaddestroy() throws Exception{
 //BA.debugLineNum = 162;BA.debugLine="Sub adadevent_adadfullscreenaddestroy";
 //BA.debugLineNum = 163;BA.debugLine="Log(\"full screen ad destroy event\")";
anywheresoftware.b4a.keywords.Common.Log("full screen ad destroy event");
 //BA.debugLineNum = 164;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadfullscreenaderror(int _errorcode,String _errormessage) throws Exception{
 //BA.debugLineNum = 158;BA.debugLine="Sub adadevent_adadfullscreenaderror(errorCode As I";
 //BA.debugLineNum = 159;BA.debugLine="Log(\"full screen ad error event -> \" & errorMessa";
anywheresoftware.b4a.keywords.Common.Log("full screen ad error event -> "+_errormessage);
 //BA.debugLineNum = 160;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadfullscreenadload() throws Exception{
 //BA.debugLineNum = 150;BA.debugLine="Sub adadevent_adadfullscreenadload";
 //BA.debugLineNum = 151;BA.debugLine="Log(\"full screen ad load event\")";
anywheresoftware.b4a.keywords.Common.Log("full screen ad load event");
 //BA.debugLineNum = 152;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadfullscreenadshow() throws Exception{
 //BA.debugLineNum = 154;BA.debugLine="Sub adadevent_adadfullscreenadshow";
 //BA.debugLineNum = 155;BA.debugLine="Log(\"full screen ad show event\")";
anywheresoftware.b4a.keywords.Common.Log("full screen ad show event");
 //BA.debugLineNum = 156;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoadactionoccurred(int _code) throws Exception{
 //BA.debugLineNum = 267;BA.debugLine="Sub adadevent_adadgeneralvideoadactionoccurred(cod";
 //BA.debugLineNum = 268;BA.debugLine="Log(\"general video ad action occurred event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad action occurred event");
 //BA.debugLineNum = 269;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoadclose() throws Exception{
 //BA.debugLineNum = 275;BA.debugLine="Sub adadevent_adadgeneralvideoadclose";
 //BA.debugLineNum = 276;BA.debugLine="Log(\"general video ad close event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad close event");
 //BA.debugLineNum = 277;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoadcomplete() throws Exception{
 //BA.debugLineNum = 279;BA.debugLine="Sub adadevent_adadgeneralvideoadcomplete";
 //BA.debugLineNum = 280;BA.debugLine="Log(\"general video ad complete event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad complete event");
 //BA.debugLineNum = 281;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoaddestroy() throws Exception{
 //BA.debugLineNum = 271;BA.debugLine="Sub adadevent_adadgeneralvideoaddestroy";
 //BA.debugLineNum = 272;BA.debugLine="Log(\"general video ad destroy event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad destroy event");
 //BA.debugLineNum = 273;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoaderror(int _errorcode,String _errormessage) throws Exception{
 //BA.debugLineNum = 263;BA.debugLine="Sub adadevent_adadgeneralvideoaderror(errorCode As";
 //BA.debugLineNum = 264;BA.debugLine="Log(\"video ad error event -> \" & errorMessage)";
anywheresoftware.b4a.keywords.Common.Log("video ad error event -> "+_errormessage);
 //BA.debugLineNum = 265;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoadload() throws Exception{
 //BA.debugLineNum = 255;BA.debugLine="Sub adadevent_adadgeneralvideoadload";
 //BA.debugLineNum = 256;BA.debugLine="Log(\"general video ad load event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad load event");
 //BA.debugLineNum = 257;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoadshow() throws Exception{
 //BA.debugLineNum = 259;BA.debugLine="Sub adadevent_adadgeneralvideoadshow";
 //BA.debugLineNum = 260;BA.debugLine="Log(\"general video ad show event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad show event");
 //BA.debugLineNum = 261;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadgeneralvideoadstart() throws Exception{
 //BA.debugLineNum = 283;BA.debugLine="Sub adadevent_adadgeneralvideoadstart";
 //BA.debugLineNum = 284;BA.debugLine="Log(\"general video ad start event\")";
anywheresoftware.b4a.keywords.Common.Log("general video ad start event");
 //BA.debugLineNum = 285;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoadactionoccurred(int _code) throws Exception{
 //BA.debugLineNum = 191;BA.debugLine="Sub adadevent_adadrewardvideoadactionoccurred(code";
 //BA.debugLineNum = 192;BA.debugLine="Log(\"Reward video ad action occurred event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad action occurred event");
 //BA.debugLineNum = 193;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoadclose() throws Exception{
 //BA.debugLineNum = 199;BA.debugLine="Sub adadevent_adadrewardvideoadclose";
 //BA.debugLineNum = 200;BA.debugLine="Log(\"Reward video ad close event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad close event");
 //BA.debugLineNum = 201;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoadcomplete() throws Exception{
 //BA.debugLineNum = 203;BA.debugLine="Sub adadevent_adadrewardvideoadcomplete";
 //BA.debugLineNum = 204;BA.debugLine="Log(\"Reward video ad complete event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad complete event");
 //BA.debugLineNum = 205;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoaddestroy() throws Exception{
 //BA.debugLineNum = 195;BA.debugLine="Sub adadevent_adadrewardvideoaddestroy";
 //BA.debugLineNum = 196;BA.debugLine="Log(\"Reward video ad destroy event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad destroy event");
 //BA.debugLineNum = 197;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoaderror(int _errorcode,String _errormessage) throws Exception{
 //BA.debugLineNum = 187;BA.debugLine="Sub adadevent_adadrewardvideoaderror(errorCode As";
 //BA.debugLineNum = 188;BA.debugLine="Log(\"video ad error event -> \" & errorMessage)";
anywheresoftware.b4a.keywords.Common.Log("video ad error event -> "+_errormessage);
 //BA.debugLineNum = 189;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoadload() throws Exception{
 //BA.debugLineNum = 179;BA.debugLine="Sub adadevent_adadrewardvideoadload";
 //BA.debugLineNum = 180;BA.debugLine="Log(\"Reward video ad load event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad load event");
 //BA.debugLineNum = 181;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoadshow() throws Exception{
 //BA.debugLineNum = 183;BA.debugLine="Sub adadevent_adadrewardvideoadshow";
 //BA.debugLineNum = 184;BA.debugLine="Log(\"Reward video ad show event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad show event");
 //BA.debugLineNum = 185;BA.debugLine="End Sub";
return "";
}
public static String  _adadevent_adadrewardvideoadstart() throws Exception{
 //BA.debugLineNum = 207;BA.debugLine="Sub adadevent_adadrewardvideoadstart";
 //BA.debugLineNum = 208;BA.debugLine="Log(\"Reward video ad start event\")";
anywheresoftware.b4a.keywords.Common.Log("Reward video ad start event");
 //BA.debugLineNum = 209;BA.debugLine="End Sub";
return "";
}
public static String  _closablevideoadbutton_click() throws Exception{
 //BA.debugLineNum = 101;BA.debugLine="Sub closableVideoAdButton_Click";
 //BA.debugLineNum = 102;BA.debugLine="If adad.isClosableVideoAdReady Then";
if (mostCurrent._adad.isClosableVideoAdReady()) { 
 //BA.debugLineNum = 103;BA.debugLine="adad.showClosableVideoAd()";
mostCurrent._adad.showClosableVideoAd(mostCurrent.activityBA);
 }else {
 //BA.debugLineNum = 105;BA.debugLine="ToastMessageShow(\"ClosableVideoAd is not ready t";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("ClosableVideoAd is not ready to show, please try later"),anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 107;BA.debugLine="End Sub";
return "";
}
public static String  _fulladbutton_click() throws Exception{
 //BA.debugLineNum = 82;BA.debugLine="Sub fullAdButton_Click";
 //BA.debugLineNum = 83;BA.debugLine="If adad.isFullscreenBannerReady() Then";
if (mostCurrent._adad.isFullscreenBannerReady()) { 
 //BA.debugLineNum = 84;BA.debugLine="adad.showFullscreenBannerAd()";
mostCurrent._adad.showFullscreenBannerAd(mostCurrent.activityBA);
 }else {
 //BA.debugLineNum = 86;BA.debugLine="ToastMessageShow(\"fullScreenAd is not ready to s";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("fullScreenAd is not ready to show, try later"),anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 89;BA.debugLine="End Sub";
return "";
}
public static String  _generalvideoadbutton_click() throws Exception{
 //BA.debugLineNum = 110;BA.debugLine="Sub generalVideoAdButton_Click";
 //BA.debugLineNum = 111;BA.debugLine="If adad.isGeneralVideoAdReady() Then";
if (mostCurrent._adad.isGeneralVideoAdReady()) { 
 //BA.debugLineNum = 112;BA.debugLine="adad.showGeneralVideoAd()";
mostCurrent._adad.showGeneralVideoAd(mostCurrent.activityBA);
 }else {
 //BA.debugLineNum = 114;BA.debugLine="ToastMessageShow(\"GeneralVideoAd is not ready to";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("GeneralVideoAd is not ready to show, please try later"),anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 116;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 21;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 25;BA.debugLine="Private fullAdButton As Button";
mostCurrent._fulladbutton = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private rewardVideoAdButton As Button";
mostCurrent._rewardvideoadbutton = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private closableVideoAdButton As Button";
mostCurrent._closablevideoadbutton = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private generalVideoAdButton As Button";
mostCurrent._generalvideoadbutton = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Private adad As AdadB4aPlugin";
mostCurrent._adad = new ir.adad.androidsdk.b4a.AdadB4aPlugin();
 //BA.debugLineNum = 35;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="End Sub";
return "";
}
public static String  _rewardvideoadbutton_click() throws Exception{
 //BA.debugLineNum = 92;BA.debugLine="Sub rewardVideoAdButton_Click";
 //BA.debugLineNum = 93;BA.debugLine="If adad.isRewardVideoAdReady Then";
if (mostCurrent._adad.isRewardVideoAdReady()) { 
 //BA.debugLineNum = 94;BA.debugLine="adad.showRewardVideoAd()";
mostCurrent._adad.showRewardVideoAd(mostCurrent.activityBA);
 }else {
 //BA.debugLineNum = 96;BA.debugLine="ToastMessageShow(\"RewardVideoAd is not ready to";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("RewardVideoAd is not ready to show, please try later"),anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 98;BA.debugLine="End Sub";
return "";
}
}
