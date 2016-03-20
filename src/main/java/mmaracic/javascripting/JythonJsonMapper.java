/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.util.HashMap;
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
import org.python.core.PyString;
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
            
            switch(type)
            {
                case OBJECT: pyJson.put(key, JsonToJython((JsonObject)value)); break;
                case ARRAY: pyJson.put(key, JsonToJython((JsonArray)value)); break;
                case STRING: pyJson.put(key, new PyString(value.toString())); break;
                case NUMBER: pyJson.put(key, determineNumber(value)); break;
                case TRUE: pyJson.put(key, new PyBoolean(true)); break;
                case FALSE: pyJson.put(key, new PyBoolean(false)); break;
                default: throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return pyJson;
    }
    
    public static PyArray JsonToJython(JsonArray json) throws IllegalArgumentException
    {
        Class pClass;
        if (json.size()>0)
        {
            ValueType jElementType = json.get(0).getValueType();
            switch(jElementType)
            {
                case OBJECT: pClass=PyObject.class; break;
                case ARRAY: pClass=PyArray.class; break;
                case STRING: pClass=PyString.class; break;
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
            
            switch(type)
            {
                case OBJECT: pyJson.append(JsonToJython((JsonObject)value)); break;
                case ARRAY: throw new IllegalArgumentException("Array within array!");
                case STRING: pyJson.append(new PyString(value.toString())); break;
                case NUMBER: pyJson.append(determineNumber(value)); break;
                case TRUE: pyJson.append(new PyBoolean(true)); break;
                case FALSE: pyJson.append(new PyBoolean(false)); break;
                default: throw new IllegalArgumentException("Problematic value: "+value.toString());
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
            
            if (PyString.TYPE.equals(type)){
                jObjBuild.add(key.toString(), value.toString());
            }
            else if (PyLong.TYPE.equals(type)){
                String valueStr = value.toString();
                if (valueStr.endsWith("l") || valueStr.endsWith("L")){
                    valueStr = valueStr.substring(0, valueStr.length()-1);
                }
                jObjBuild.add(key.toString(), Long.parseLong(valueStr));
            }
            else if (PyFloat.TYPE.equals(type)){
                jObjBuild.add(key.toString(), Float.parseFloat(value.toString()));
            }
            else if (PyDictionary.TYPE.equals(type)){
                jObjBuild.add(key.toString(), JythonToJson((PyDictionary)value));
            }
            else if (PyArray.TYPE.equals(type)){
                jObjBuild.add(key.toString(), JythonToJson((PyArray)value));
            } else {
                throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return jObjBuild.build();
    }
    
    public static JsonArray JythonToJson(PyArray pyObj) throws IllegalArgumentException
    {
        JsonArrayBuilder jArrayBuild = Json.createArrayBuilder();
        
        PyObject[] array = (PyObject[]) pyObj.getArray();
        for(PyObject value: array)
        {
            PyType type = value.getType();
            
            if (PyString.TYPE.equals(type)){
                jArrayBuild.add(value.toString());
            }
            else if (PyLong.TYPE.equals(type)){
                String valueStr = value.toString();
                if (valueStr.endsWith("l") || valueStr.endsWith("L")){
                    valueStr = valueStr.substring(0, valueStr.length()-1);
                }
                jArrayBuild.add(Long.parseLong(valueStr));
            }
            else if (PyFloat.TYPE.equals(type)){
                jArrayBuild.add(Float.parseFloat(value.toString()));
            }
            else if (PyDictionary.TYPE.equals(type)){
                jArrayBuild.add(JythonToJson((PyDictionary)value));
            }
            else if (PyArray.TYPE.equals(type)){
                jArrayBuild.add(JythonToJson((PyArray)value));
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
 
            if (value instanceof String){
                pyJson.put(key, new PyString((String)value.toString()));
            }
            else if (value instanceof Long){
                pyJson.put(key, new PyLong((Long)value));
            }
            else if (value instanceof Float){
                pyJson.put(key, new PyFloat((Float)value));
            }
            if (value instanceof Integer){
                pyJson.put(key, new PyInteger((Integer)value));
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
            
            if (PyString.TYPE.equals(type)){
                jMap.put(key.toString(), value.toString());
            }
            if (PyLong.TYPE.equals(type)){
                String valueStr = value.toString();
                if (valueStr.endsWith("l") || valueStr.endsWith("L")){
                    valueStr = valueStr.substring(0, valueStr.length()-1);
                }
                jMap.put(key.toString(), Long.parseLong(valueStr));
            }
            if (PyFloat.TYPE.equals(type)){
                jMap.put(key.toString(), Float.parseFloat(value.toString()));
            }
            if (PyDictionary.TYPE.equals(type)){
                jMap.put(key.toString(), JythonToJson((PyDictionary)value));
            }
            if (PyArray.TYPE.equals(type)){
                jMap.put(key.toString(), JythonToJson((PyArray)value));
            } else {
                throw new IllegalArgumentException("Problematic value: "+value.toString());
            }
        }
        return jMap;
    }
}
