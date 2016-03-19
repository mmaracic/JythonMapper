/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import org.python.core.PyDictionary;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Marijo
 */
public class Processor implements Runnable{
    
    PythonInterpreter interp;
    int index;
    int count;
    
    public Processor(PythonInterpreter interp, int index, int count)
    {
        this.interp = new PythonInterpreter();
        //this.interp = interp;
        this.index = index;
        this.count = count;
    }

//    @Override
//    public void run() {
//        for(int i=0; i<count; i++){
//            interp.set("i", new PyInteger(i));
//            interp.set("index", new PyInteger(index));
//            interp.exec("print str(i)+' : index '+str(index)");
//        }
//    }    
    
    @Override
    public void run() {
        try
        {
//            for(int i=0; i<10; i++){
                System.gc();
                Calendar cal = Calendar.getInstance();
                Calendar start = Calendar.getInstance();
                JSONSample jsample = new JSONSample();
                JsonObject j = jsample.getSample();
                Calendar read = Calendar.getInstance();
                System.out.println("Java read json: "+measureDurationMilis(start, read)+" miliseconds");

                String sj = j.toString();
                Calendar string = Calendar.getInstance();
                System.out.println("Json to String: "+measureDurationMilis(read, string)+" miliseconds");

                PyObject pj = JythonJsonMapper.JsonToJython(j);
                Calendar conversionIn = Calendar.getInstance();
                System.out.println("Java to Python conversion: "+measureDurationMilis(string, conversionIn)+" miliseconds");

                // The exec() method executes strings of code
                interp.exec("import sys");
                Calendar importSys = Calendar.getInstance();
                System.out.println("Java ImportSys: "+measureDurationMilis(conversionIn, importSys)+" miliseconds");
                //interp.exec("print sys");
                interp.exec("import json");
                Calendar importJson = Calendar.getInstance();
                System.out.println("Java importJson: "+measureDurationMilis(importSys, importJson)+" miliseconds");
                //interp.exec("print json");

                // Set variable values within the PythonInterpreter instance
                interp.set("pj", pj);
                Calendar putIn = Calendar.getInstance();
                System.out.println("Java to python: "+measureDurationMilis(importJson, putIn)+" miliseconds");
                //interp.exec("print pj");
                interp.set("sj", sj);
                Calendar putInStr = Calendar.getInstance();
                System.out.println("Java to python string: "+measureDurationMilis(putIn, putInStr)+" miliseconds");
                //interp.exec("print sj");
                interp.exec("ppj = json.loads(sj)");
                 //interp.exec("print ppj");
                Calendar str2Json = Calendar.getInstance();
                System.out.println("Python string to json: "+measureDurationMilis(putInStr, str2Json)+" miliseconds");
                PyObject pj2 = interp.get("pj");
                Calendar putOut = Calendar.getInstance();
                System.out.println("Python to java: "+measureDurationMilis(str2Json, putOut)+" miliseconds");
                PyDictionary pj2d = (PyDictionary) pj2;
                JsonObject j2 = JythonJsonMapper.JythonToJson(pj2d);
                Calendar conversionOut = Calendar.getInstance();
                System.out.println("Python to Java conversion: "+measureDurationMilis(putOut, conversionOut)+" miliseconds");
                //System.out.println(pj2d);
//            }       
        }   
        catch (FileNotFoundException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static long measureDurationMilis(Calendar start, Calendar finish)
    {
        return finish.getTimeInMillis() - start.getTimeInMillis();
    }
}
