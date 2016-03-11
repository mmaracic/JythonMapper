/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.util.Map.Entry;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.python.core.PyArray;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
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
    
    public static PyDictionary JsonToJython(JsonObject json) throws Exception
    {
        PyDictionary pyJson = new PyDictionary();
        for(Entry<String, JsonValue> kvp: json.entrySet())
        {
            String key = kvp.getKey();
            JsonValue value = kvp.getValue();
            ValueType type = value.getValueType();
            
            switch(type)
            {
                case OBJECT: pyJson.put(key, JsonToJython(value)); break;
                case ARRAY: pyJson.put(key, JsonToJython(value)); break;
                case STRING: pyJson.put(key, new PyString(value.toString())); break;
                case NUMBER: pyJson.put(key, determineNumberType(value)); break;
                case TRUE: pyJson.put(key, new PyBoolean(true)); break;
                case FALSE: pyJson.put(key, new PyBoolean(false)); break;
                default: throw new Exception("Problematic value: "+value.toString());
            }
        }
        return pyJson;
    }
    
    public static PyArray JsonToJython(JsonArray json) throws Exception
    {
        PyType pType;
        ValueType jType = json.getValueType();
        switch(jType)
        {
            case OBJECT: pType=PyObject.TYPE; break;
            case ARRAY: pType=PyArray.TYPE; break;
            case STRING: pType=PyString.TYPE; break;
            case NUMBER: pType=PyNumber.TYPE; break;
            case TRUE: pType=PyBoolean.TYPE; break;
            case FALSE: pType=PyBoolean.TYPE; break;
            default: throw new Exception("Problematic type: "+jType.toString());
        }

        PyArray pyJson = new PyArray(pType);
        for(JsonValue value: json)
        {
            ValueType type = value.getValueType();
            
            switch(type)
            {
                case OBJECT: pyJson.append(JsonToJython(value)); break;
                case ARRAY: pyJson.append(value); break;
                case STRING: pyJson.append(new PyString(value.toString())); break;
                case NUMBER: pyJson.append(determineNumber(value)); break;
                case TRUE: pyJson.append(new PyBoolean(true)); break;
                case FALSE: pyJson.append(new PyBoolean(false)); break;
                default: throw new Exception("Problematic value: "+value.toString());
            }
        }
        return pyJson;
    }
}
