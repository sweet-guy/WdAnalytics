package com.wdcloud.wdanalytics;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.wdcloud.analytics.greendao.gen.DaoSession;
import com.wdcloud.wdanalytics.bean.CrashAnalyticsBean;
import com.wdcloud.wdanalytics.bean.EventBean;
import com.wdcloud.wdanalytics.bean.WdAnalyticsBean;
import com.wdcloud.wdanalytics.service.AnalyticsService;
import com.wdcloud.wdanalytics.util.AnalyticsBeanFactory;
import com.wdcloud.wdanalytics.util.AnalyticsNetUtil;
import com.wdcloud.wdanalytics.util.AnalyticsSharedPreference;
import com.wdcloud.wdanalytics.util.GreenDaoManager;
import com.wdcloud.wdanalytics.util.LogUtil;
import com.wdcloud.wdanalytics.util.TimeUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;


/**
 * Info:统计sdk初始化操作
 * Created by Yanxin.
 * CreateTime: 2019/11/27 15:45
 */
public class WdAnalytics {
    private static DaoSession event_session;
    private static DaoSession crash_session;
    private static List<WdAnalyticsBean.EventsBean> eventsBeanList;
    private static boolean networkConnected;
    private static String openTime;
    public static Boolean isDebug;
    private static String pagestart_time;
    private static WdAnalyticsBean.ProPertiesBean proPertiesBean;

    //统计初始化
    public static void init(Context context, String project, Boolean isdebug, int millis)
    {
        AnalyticsSharedPreference.init(context);
        GreenDaoManager.init(context);
        networkConnected = isNetworkConnected(context);
        openTime = TimeUtil.longToDate();
        if(isdebug==false)
        {
            Log.e("WdAnalytic","伟东统计debug模式未开启");
        }
        else
        {
            Log.e("WdAnalytic","伟东统计debug模式已开启，请查看打印信息");
        }
        isDebug=isdebug;
        AnalyticsSharedPreference instance = AnalyticsSharedPreference.getInstance();
        instance.saveData("Debug",isdebug);
        AnalyticsBeanFactory.saveWdAanlyticsFactory(context,project,millis);
        String phoneinfo = AnalyticsBeanFactory.getCrashWdAanlyticsFactory().toString();
        LogUtil.e("伟东统计设备信息",phoneinfo);
        Intent intent = new Intent(context, AnalyticsService.class);
        context.startService(intent);
        event_session= GreenDaoManager.getInstance().getEventSession();
        crash_session=GreenDaoManager.getInstance().getCrashSession();
        Log.e("伟东统计初始化完毕","============================");
    }
    //设置用户信息  登录用户ID：distinctId  地区：locationCode 渠道：channel
    public static void setUesrinfo(String distinctId,String locationCode,String channel)
    {
        AnalyticsSharedPreference instance = AnalyticsSharedPreference.getInstance();
        instance.saveData("distinctId",distinctId);
        instance.saveData("locationCode",locationCode);
        instance.saveData("channel",channel);
        LogUtil.e("统计用户信息为：","distinctId:"+distinctId+"-*- locationCode:"+locationCode+"-*- channel:"+channel);
    }
    //使用时长统计
    public static void setExitApp(){
        AnalyticsSharedPreference instance = AnalyticsSharedPreference.getInstance();
        instance.saveData("pageName","");
        instance.saveData("pageId","");
        String exitTime = TimeUtil.longToDate();
        String exittimeDifference = TimeUtil.getTimeDifference(openTime, exitTime);
        EventBean eventBean = new EventBean(null, false, "event", "", exitTime, exittimeDifference, "rrt.common.duration", "使用时长", "", "", "", "","");
        long insert = event_session.insert(eventBean);
        LogUtil.e("伟东统计应用使用时长=============",exittimeDifference);
    }
    //奔溃统计
    public static void setCrashAnalytics(Throwable callStack,String ip){
        String crash_Time = TimeUtil.getTimeStame();
        String crash_info = changeCrashInfo(callStack);
        CrashAnalyticsBean crashAnalyticsBean = new CrashAnalyticsBean(null, crash_Time, " ", " ", crash_info, networkConnected +"", ip, " ");
        crash_session.insert(crashAnalyticsBean);
        LogUtil.e("伟东统计奔溃完毕",crash_Time+":"+crash_info);
    }
    //进入页面统计
    public static void onPageStart(){
        pagestart_time = TimeUtil.longToDate();
    }
    //退出页面和停留時長統計
    public static void onPageEnd(String pageName,String pageId){
        AnalyticsSharedPreference instance = AnalyticsSharedPreference.getInstance();
        String prefixPageName = (String) instance.getData("pageName", "");
        String prefixPageId = (String) instance.getData("pageId", "");
        String pageEndTime = TimeUtil.getTimeStame();
        String pageend_time = TimeUtil.longToDate();
        String timeDifference = TimeUtil.getTimeDifference(pagestart_time, pageend_time);
        EventBean eventBean = new EventBean(null, false, "page", "返回", pageEndTime, timeDifference, "", "", pageId, pageName, prefixPageId, prefixPageName,"");
        long insert = event_session.insert(eventBean);
        instance.saveData("pageName",pageName);
        instance.saveData("pageId",pageId);
        LogUtil.e("伟东统计收集页面退出完毕","===页面名称：===="+pageName+"==页面ID==="+pageId);
    }
    //事件统计
    public static void setAnalyticEvent(Context context, String eventName, String eventId, Boolean isReal, HashMap<String,Object> custom){
        String eventTime = TimeUtil.getTimeStame();
        String myEventinfo="";
        WdAnalyticsBean wdAanlyticsFactory = AnalyticsBeanFactory.getWdAanlyticsFactory();
        wdAanlyticsFactory.setUpdateTime(eventTime);
        if(eventsBeanList==null)
        {
            eventsBeanList = new ArrayList<>();
        }
        eventsBeanList.clear();
        if(custom!=null)
        {
            myEventinfo=custom.toString();
        }
        if(isReal==true)
        {
            WdAnalyticsBean.EventsBean eventsBean = new WdAnalyticsBean.EventsBean(false, "event", "", eventTime, "0", eventId, eventName, "", "", "", "");
            if(myEventinfo.equals(""))
            {
//                LogUtil.e("伟东统计===未使用自定义参数统计","=========");
            }
            else
            {
//                LogUtil.e("伟东统计===已使用自定义参数统计","=========");
                proPertiesBean = new WdAnalyticsBean.ProPertiesBean();
                proPertiesBean.setCustomInfo(myEventinfo);
                eventsBean.setProPertiesBean(proPertiesBean);
            }
            EventBean eventBean = new EventBean(null, false, "event", "", eventTime, "0", eventId, eventName, "", "", "", "",myEventinfo);
            eventsBeanList.add(eventsBean);
            LogUtil.e("伟东统计收集事件完毕(实时)","type：event  duration ：0  eventName："+eventName+" eventId："+eventId+"自定义属性："+myEventinfo);
            wdAanlyticsFactory.setRecords(eventsBeanList);
            tryToConnect(context,wdAanlyticsFactory,eventBean);
        }
        else
        {
            EventBean eventBean = new EventBean(null, false, "event", "点击", eventTime, "0", eventId, eventName, "", "", "", "",myEventinfo);
            long insert = event_session.insert(eventBean);
            LogUtil.e("伟东统计收集事件完毕(非实时)","type：event  duration ：0  eventName："+eventName+" eventId："+eventId+"自定义参数："+myEventinfo);
        }
    }
    //统计上传
    private static void tryToConnect(Context context,WdAnalyticsBean wdAnalyticsBean, final EventBean eventBean){
        boolean networkConnected = isNetworkConnected(context);
        if(networkConnected)
        {
            LogUtil.e("伟东统计===设备目前有网","============");
            AnalyticsNetUtil.postJson("capture/client/userBehavior", wdAnalyticsBean, new AnalyticsNetUtil.AnalyticNetCallBack() {
                @Override
                public void failed(Response response, String errorMsg) {
                    LogUtil.e("伟东统计(实时)统计上传失败！",errorMsg);
                    event_session.insert(eventBean);
                }

                @Override
                public void successed(Response response, String data) {
                    LogUtil.e("伟东统计(实时)统计上传成功！",data.toString());
                }
            });
        }
        else
        {
            LogUtil.e("伟东统计===设备目前没有网","======插入数据库======");
            event_session.insert(eventBean);
        }
    }
    //判断是否有网
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    private static String changeCrashInfo(Throwable ex){
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.flush();
        printWriter.close();
        String result = writer.toString();
        return result;
    }
}
