package cn.regionsoft.one.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class TreeUtil {
	/*
	 * id type only be long or string
	 * @param treeNodeList  targetList
	 * @param parentIdAttr	parent Id Attr name
	 * @param childsLsAttr  childLs Attr Name
	 * @return
	 */
	/*public static <T> List<T> resolvePlainLsAsTreeLs(List<T> treeNodeList,String nodeIdAttr,String parentIdAttr, String childsLsAttr,String parentAttr){
		try{
			Class<?> parentIdClassType = null;
			
			List<T> resultLs = new ArrayList<T>();
			Map<String,T> treeMap = resolvePlainLsAsTreeMap(treeNodeList,nodeIdAttr,parentIdAttr, childsLsAttr,parentAttr);
			
			for(Entry<String,T> entry : treeMap.entrySet()){
				T treeNode = entry.getValue();
				Object parentId = getObjFieldValue(treeNode, parentIdAttr);
				
				String parentIdStr = null;
				if(parentId==null){
					parentIdStr = null;
				}
				else{
					if(parentIdClassType == Long.class){
						parentIdStr = String.valueOf(parentId);
					}
					else{
						parentIdStr = (String) parentId;
					}
				}
				
				boolean isRootNode = (parentIdStr==null||parentIdStr.equals("0"));
				
				if(isRootNode){
					resultLs.add(treeNode);
				}
			}
			
			return resultLs;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}*/
	
	
	public static Map resolvePlainLsAsTreeMap(List treeNodeList,String nodeIdAttr,String parentIdAttr, String childsLsAttr,String parentAttr){
		try{
			Class<?> idClassType = null;
			
			Map<Object,Object> treeMap = new LinkedHashMap<Object,Object>();
			
			for(Object node:treeNodeList){
				Object fieldVal = getObjFieldValue(node, nodeIdAttr);
				if(idClassType==null){
					idClassType = fieldVal.getClass();
				}
				treeMap.put(fieldVal, node);
			}

			for(Entry<Object,Object> entry : treeMap.entrySet()){
				Object treeNode = entry.getValue();
				Object parentId = getObjFieldValue(treeNode, parentIdAttr);
				
				boolean notRootNode = false;
				if(parentId==null){
					notRootNode = false;
				}
				else{
					if(parentId.getClass() == String.class){
						notRootNode = !parentId.equals("0");
					}
					else if(parentId.getClass() == Long.class){
						notRootNode = !parentId.equals(0);
					}
				}
				
				
				if(notRootNode){
					Object parentNode = treeMap.get(parentId);
					setObjFieldValue(treeNode,parentAttr,parentNode);
					@SuppressWarnings("unchecked")
					List<Object> childs = (List<Object>) getObjFieldValue(parentNode, childsLsAttr);
					if(childs==null){
						childs = new ArrayList<Object>();
						setObjFieldValue(parentNode,childsLsAttr,childs);
					}
					childs.add(treeNode);
				}
			}
			return treeMap;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void setObjFieldValue(Object target, String fieldNM, Object value) throws Exception {
		try{
			Field field = getFieldByFieldName(target.getClass(),fieldNM);
			field.setAccessible(true);
			field.set(target, value);
		}
		catch(NoSuchFieldException e){
			throw new Exception(e);
		}
	}
	
	public static Object getObjFieldValue(Object target,String fieldNM) throws Exception{
		try{
			Field field = getFieldByFieldName(target.getClass(),fieldNM);
			field.setAccessible(true);
			return field.get(target);
		}
		catch(NoSuchFieldException e){
			throw new Exception(e);
		}
	}
	
	public static Field getFieldByFieldName(Class<?> classType,String fieldNM) throws NoSuchFieldException, SecurityException{
		try{
			return classType.getDeclaredField(fieldNM);
		}
		catch(NoSuchFieldException e){
			if(classType==Object.class)throw new NoSuchFieldException(fieldNM);
			return classType.getSuperclass().getDeclaredField(fieldNM);
		}
	}
}
