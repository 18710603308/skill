package com.cjl.skill;

import java.util.concurrent.ConcurrentHashMap;

public class MapTest {

	public static void main(String[] args) {
		 ConcurrentHashMap<String, Boolean> SOLD_OUT_FLAGS = new ConcurrentHashMap<>();
		 SOLD_OUT_FLAGS.put("1", true);
		 if(SOLD_OUT_FLAGS.get("1")!=null)
		 {
			 System.out.println("sold out");
		 }
	}

}
