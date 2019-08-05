package com.rd.sh.common;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class LoadConf {
	private Properties prop = new Properties();

	private void loadconf() throws FileNotFoundException, IOException {
		String path="/data/dongfanghao/config/CommentsCrawler.properties";
//		String path = "C:\\Users\\admin\\Desktop\\ReadCrawler\\src\\main\\java\\com\\rd\\sh\\common\\CommentsCrawler.properties";
		try {
			prop.load(new InputStreamReader(new FileInputStream(path),"utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LoadConf() throws FileNotFoundException, IOException {
		loadconf();
	}

	public  boolean chkProperty(String _key) {
		return prop.containsKey(_key);
	}

	public String getProperty(String _key) {
		return prop.getProperty(_key);
	}

}
