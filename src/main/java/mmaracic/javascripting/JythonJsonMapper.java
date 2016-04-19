/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.python.core.PyArray;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyUnicode;
import org.python.core.PyType;

/**
 *
 * @author Marijo
 */
public class JythonJsonMapper {
    
    private static PyObject determineNumber(JsonValue value)
    {
        try
        {
            long l = Long.parseLong(value.toString());
            return new PyLong(l);
        }
        catch (NumberFormatException ex){}
        try
        {
            float f = Float.parseFloat(value.toString());
            return new PyFloat(f);
        }
        catch (NumberFormatException ex){}
        return null;
    }
    
    private static PyType determineNumberType(JsonValue value)
    {
        try
        {
            long l = Long.parseLong(value.toString());
            return PyLong.TYPE;
        }
        catch (NumberFormatException ex){}
        try
        {
            float f = Float.parseFloat(value.toString());
            return PyFloat.TYPE;
        }
        catch (NumberFormatException ex){}
        return null;
    }
    
    private static Class determineNumberClass(JsonValue value)
    {
        try
        {
            long l = Long.parseLong(value.toString());
            return PyLong.class;
        }
        catch (NumberFormatException ex){}
        try
        {
            float f = Float.parseFloat(value.toString());
            return PyFloat.class;
        }
        catch (NumberFormatException ex){}
        return null;
    }
    
    public static PyDictionary JsonToJython(JsonObject json) throws IllegalArgumentException
    {
        PyDictionary pyJson = new PyDictionary();
        for(Entry<String, JsonValue> kvp: json.entrySet())
        {
            String key = kvp.getKey();
            JsonValue value = kvp.getValue();
            ValueType type = value.getValueType();

            try
            {
                switch(type)
                {
                    case OBJECT: pyJson.put(key, JsonToJython((JsonObject)value)); break;
                    case ARRAY: pyJson.put(key, JsonToJython((JsonArray)value)); break;
                    case STRING: pyJson.put(key, new PyUnicode(value.toString())); break;
                    case NUMBER: pyJson.put(key, determineNumber(value)); break;
                    case TRUE: pyJson.put(key, new PyBoolean(true)); break;
                    case FALSE: pyJson.put(key, new PyBoolean(false)); break;
                    default: throw new IllegalArgumentException("Problematic value: "+value.toString());
                }
            } catch (Exception ex){
                throw ex;
            }
        }
        return pyJson;
    }
    
    public static PySequence JsonToJython(JsonArray json) throws IllegalArgumentException
    {
        Class pClass;
        if (json.size()>0)
        {
            ValueType jElementType = json.get(0).getValueType();
            switch(jElementType)
            {
                case OBJECT: pClass=PyObject.class; break;
                case ARRAY: pClass=PyArray.class; break;
                case STRING: pClass=PyUnicode.class; break;
                case NUMBER: pClass=determineNumberClass(json); break;
                case TRUE: pClass=PyBoolean.class; break;
                case FALSE: pClass=PyBoolean.class; break;
                default: throw new IllegalArgumentException("Problematic type: "+jElementType.toString());
            }
        } else {
            pClass=PyObject.class;
        }

        PyArray pyJson = new PyArray(pClass, 0);
        for(JsonValue value: json)
        {
            ValueType type = value.getValueType();

            try{
                switch(type)
                {
                    case OBJECT: pyJson.append(JsonToJython((JsonObject)value)); break;
                    case ARRAY: throw new IllegalArgumentException("Array within array!");
                    case STRING: pyJson.append(new PyUnicode(value.toString())); break;
                    case NUMBER: pyJson.append(determineNumber(value)); break;
                    case TRUE: pyJson.append(new PyBoolean(true)); break;
                    case FALSE: pyJson.append(new PyBoolean(false)); break;
                    default: throw new IllegalArgumentException("Problematic value: "+value.toString());
                }
            } catch (Exception ex){
                throw ex;
            }
        }
        return pyJson;
    }
    
    public static JsonObject JythonToJson(PyDictionary pyObj) throws IllegalArgumentException
    {
        JsonObjectBuilder jObjBuild = Json.createObjectBuilder();
        
        ConcurrentMap<PyObject, PyObject> map = pyObj.getMap();
        for(Entry<PyObject, PyObject> kvp: map.entrySet())
        {
            PyObject key = kvp.getKey();
            PyObject value = kvp.getValue();
            PyType type = value.getType();
            
            if (type.isSubType(PyUnicode.TYPE)){
                jObjBuild.add(key.toString(), value.toString());
            }
            else if (type.isSubType(PyBoolean.TYPE)){
                jObjBuild.add(key.toString(), Boolean.parseBoolean(value.toString()));
            }
            else if (type.isSubType(PyInteger.TYPE)){
                jObjBuild.add(key.toString(), Integer.parseInt(value.toString()));
            }
            else if (type.isSubType(PyLong.TYPE)){
                String valueStr = value.toString();
                if (valueStr.endsWith("l") || valueStr.endsWith("L")){
                    valueStr = valueStr.substring(0, valueStr.length()-1);
                }
                jObjBuild.add(key.toString(), Long.parseLong(valueStr));
            }
            else if (type.isSubType(PyFloat.TYPE)){
                jObjBuild.add(key.toString(), Float.parseFloat(value.toString()));
            }
            else if (type.isSubType(PyDictionary.TYPE)){
                jObjBuild.add(key.toString(), JythonToJson((PyDictionary)value));
            }
            else if (type.isSubType(PySequence.TYPE)){
                jObjBuild.add(key.toString(), JythonToJson((PySequence)value));
            } else {
                throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return jObjBuild.build();
    }
    
    public static JsonArray JythonToJson(PySequence pySeq) throws IllegalArgumentException
    {
        JsonArrayBuilder jArrayBuild = Json.createArrayBuilder();
        
        Iterator<PyObject> it = pySeq.asIterable().iterator();
        while(it.hasNext())
        {
            PyObject value = it.next();
            PyType type = value.getType();
            
            if (type.isSubType(PyUnicode.TYPE)){
                jArrayBuild.add(value.toString());
            }
            else if (type.isSubType(PyBoolean.TYPE)){
                jArrayBuild.add(Boolean.parseBoolean(value.toString()));
            }
            else if (type.isSubType(PyInteger.TYPE)){
                jArrayBuild.add(Integer.parseInt(value.toString()));
            }
            else if (type.isSubType(PyLong.TYPE)){
                String valueStr = value.toString();
                if (valueStr.endsWith("l") || valueStr.endsWith("L")){
                    valueStr = valueStr.substring(0, valueStr.length()-1);
                }
                jArrayBuild.add(Long.parseLong(valueStr));
            }
            else if (type.isSubType(PyFloat.TYPE)){
                jArrayBuild.add(Float.parseFloat(value.toString()));
            }
            else if (type.isSubType(PyDictionary.TYPE)){
                jArrayBuild.add(JythonToJson((PyDictionary)value));
            }
            else if (type.isSubType(PySequence.TYPE)){
                jArrayBuild.add(JythonToJson((PySequence)value));
            } else {
                throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return jArrayBuild.build();
    }

    public static PyDictionary MapToJython(Map<String,Object> map) throws Exception
    {
        PyDictionary pyJson = new PyDictionary();
        for(Entry<String, Object> kvp: map.entrySet())
        {
            String key = kvp.getKey();
            Object value = kvp.getValue();
 
            if (value == null){
                //skip value
            }
            else if (value instanceof String){
                pyJson.put(key, new PyUnicode((String)value.toString()));
            }
            else if (value instanceof Long){
                pyJson.put(key, new PyLong((Long)value));
            }
            else if (value instanceof Float){
                pyJson.put(key, new PyFloat((Float)value));
            }
            else if (value instanceof Double){
                pyJson.put(key, new PyFloat((Double)value));
            }
            else if (value instanceof Integer){
                pyJson.put(key, new PyInteger((Integer)value));
            }
            else if (value instanceof java.sql.Timestamp){
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                String timeStampString = df.format((java.sql.Timestamp)value);
                pyJson.put(key, new PyUnicode(timeStampString));
            } else {   
                 throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return pyJson;        
    }
    
    public static Map<String,Object> JythonToMap(PyDictionary pyObj) throws IllegalArgumentException
    {
        Map<String,Object> jMap = new HashMap<>();
        
        ConcurrentMap<PyObject, PyObject> map = pyObj.getMap();
        for(Entry<PyObject, PyObject> kvp: map.entrySet())
        {
            PyObject key = kvp.getKey();
            PyObject value = kvp.getValue();
            PyType type = value.getType();
            
            if (type.isSubType(PyUnicode.TYPE)){
                jMap.put(key.toString(), value.toString());
            }
            else if (type.isSubType(PyBoolean.TYPE)){
                jMap.put(key.toString(), Boolean.parseBoolean(value.toString()));
            }
            else if (type.isSubType(PyInteger.TYPE)){
                jMap.put(key.toString(), Integer.parseInt(value.toString()));
            }
            else if (type.isSubType(PyLong.TYPE)){
                String valueStr = value.toString();
                if (valueStr.endsWith("l") || valueStr.endsWith("L")){
                    valueStr = valueStr.substring(0, valueStr.length()-1);
                }
                jMap.put(key.toString(), Long.parseLong(valueStr));
            }
            else if (type.isSubType(PyFloat.TYPE)){
                jMap.put(key.toString(), Float.parseFloat(value.toString()));
            }
            else if (type.isSubType(PyDictionary.TYPE)){
                jMap.put(key.toString(), JythonToJson((PyDictionary)value));
            }
            else if (type.isSubType(PySequence.TYPE)){
                jMap.put(key.toString(), JythonToJson((PySequence)value));
            } else {
                throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return jMap;
    }
}
