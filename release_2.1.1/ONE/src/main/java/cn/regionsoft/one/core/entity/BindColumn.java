package cn.regionsoft.one.core.entity;

import java.lang.reflect.Field;

public class BindColumn {
	private String name;//数据库表列名
	private Class<?> bindType;
	private int length;
	private Field field;
	private boolean isIdColumn = false;
	
	private Class<?> listTypeArgument;//如果是list范型的类型
	public Class<?> getListTypeArgument() {
		return listTypeArgument;
	}
	public void setListTypeArgument(Class<?> listTypeArgument) {
		this.listTypeArgument = listTypeArgument;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<?> getBindType() {
		return bindType;
	}
	public void setBindType(Class<?> bindType) {
		this.bindType = bindType;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public boolean isIdColumn() {
		return isIdColumn;
	}
	public void setIdColumn(boolean isIdColumn) {
		this.isIdColumn = isIdColumn;
	}
	
}
