package com.liyunet.common.util;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Properties;

/**
 * 根据操作系统来判断上传的资源在哪
 * @author wuchengf	
 * @date 2016年5月30日
 * @since 1.8
 */
public class UploadResourceLocation {
	public static String disPath="linux";
	private static final String DISKPATH = getDiskPath();
	private static final String ASSETS = "";
	//Ueditor相关
	public static final String UEDITOR_PIC_PATH = DISKPATH + "/ue/";
	public static final String UEDITOR_PIC_URI = "/ue/";
	//用户头像
	public static final String USER_HEAD_PATH = DISKPATH + "/user/img/";
	public static final String USER_HEAD_IMG = "/user/img/";
	public static final String USER_HEAD_URI = ASSETS + "/user/img/";
	//测试版本:讨论组聊天资源文件
	public static final String GROUP_TALK_PATH_ =DISKPATH+ "/grouptalk/source/";
	public static final String GROUP_TALK_PATH = "/grouptalk/source/";
	//咨讯的图片
	public static final String INFORMATION_IMG_PATH = DISKPATH+"/information/img/";
	public static final String INFORMATION_IMG_URI = "/information/img/";
	//与身份证相关
	public static final String IDENTITYCARD_IMG_PATH = DISKPATH+"/identitycard/img/";
	public static final String IDENTITYCARD_IMG_URI = "/identitycard/img/";
	public static String getDiskPath(){
		Properties props = System.getProperties(); //系统属性
		String osName = props.getProperty("os.name").toLowerCase();
		String diskPath = "";
		System.out.println(osName+"-------------");
		if(osName.contains("windows")){
			diskPath = "D:/assets/timetreaty";
		}else if (osName.contains("linux")){
			diskPath = "/usr/local/assets";
//			diskPath = "/data/src/timetreaty_certification/img";
			disPath="linux";
		}else if (osName.contains("mac os x")){
			diskPath = "/Users/wuyunan/yunan/var/timetreaty_org";
			disPath="mac os x";
		}
		
		File file = new File(diskPath);
		if(!file.exists())
			file.mkdirs();
		
		return diskPath;
	}
	/**
	 * 返回工程url <Context path="/" docBase="/data" debug="0" reloadable="true"/>
	 * @return
	 */
	public static String getProjectPath(HttpServletRequest request){
		if(disPath.equals("linux")){
			return "http://ttcapi.liyugame.com/pictures";
//			return "http://118.89.219.59:8013/pictures";
		}else{
			String contextPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/timetreaty";
//			String contextPath = "/Users/wuyunan/yunan/var/timetreaty_certification";
			return contextPath;
		}
	}
	
}
