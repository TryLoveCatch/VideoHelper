package android.luna.net.videohelper.bean.user;

import cn.bmob.v3.BmobUser;

/**
 * 基础用户数据模型
 * 
 * @author bintou
 *
 */
public class User extends BmobUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 813312304060984503L;

	/**
	 * 地址
	 */
	private String address;

	private int age;

	/*
	 * 性别 0:男 1：女
	 */
	private int sex;

	private String headPhoto;
	
	public String getHeadPhoto() {
		return headPhoto;
	}

	public void setHeadPhoto(String headPhoto) {
		this.headPhoto = headPhoto;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getAge() {
		return age;
	}

	public int getSex() {
		return sex;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	
	
}
