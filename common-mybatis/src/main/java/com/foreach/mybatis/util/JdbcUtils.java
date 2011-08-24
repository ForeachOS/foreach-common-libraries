package com.foreach.mybatis.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JdbcUtils
{
	protected static Map<Integer,String> map = new HashMap<Integer,String>();


	{
        Field[] fields = java.sql.Types.class.getFields();
        for (int i=0; i<fields.length; i++) {
            try {
                String name = fields[i].getName();
                Integer value = (Integer)fields[i].get(null);
                map.put(value, name);
            } catch (IllegalAccessException e) {
            }
        }
	}


	public static String getJdbcTypeName(int jdbcType) {

        return map.get( jdbcType );
	}
}
