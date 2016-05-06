/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import org.python.core.PyArray;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Marijo
 * 
 * build sa: ant jar-standalone
 * Importanje standalone jython library-a u maven:
 *     mvn install:install-file -Dfile=.\jython-standalone.jar -DgroupId=org.python -DartifactId=jython-standalone -Dversion=2.7.1b3 -Dpackaging=jar
 */
public class ScriptingMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PyException {
        
        // Create an instance of the PythonInterpreter
        PythonInterpreter interp = new PythonInterpreter();
        
        //useFunctions(interp);
        //threading(interp);
        //sample(interp);
        //objects(interp);
        //importLib(interp);
        
        //standalone interpreter class
        //JythonLibImportSetup jlis = new JythonLibImportSetup();
        //jlis.execute();
        //datesTest(interp);
        //stringTest(interp);
        memoryTest(interp);
    }
    
    //multithreading test
    private static void threading(PythonInterpreter interp)
    {
        int noThreads = 100;
        int noOutputs = 10;
        ExecutorService pool = Executors.newFixedThreadPool(5);
        for (int i=0;i<noThreads;i++)
        {
            pool.execute(new Processor(interp, i, noOutputs));
        }
        pool.shutdown();
    }
    
    //initial sample mapping test
    private static void sample (PythonInterpreter interp)
    {
        // The exec() method executes strings of code
        interp.exec("import sys");
        interp.exec("print sys");

        // Set variable values within the PythonInterpreter instance
        interp.set("a", new PyInteger(42));
        interp.exec("print a");
        interp.exec("x = 2+2");

        // Obtain the value of an object from the PythonInterpreter and store it
        // into a PyObject.
        PyObject x = interp.get("x");
        System.out.println("x: " + x);        
    }
    
   //object mapping test
    private static void objects (PythonInterpreter interp)
    {
        try
        {
            for(int i=0; i<10; i++){
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
                System.out.println(pj2d);
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    private static long measureDurationMilis(Calendar start, Calendar finish)
    {
        return finish.getTimeInMillis() - start.getTimeInMillis();
    }
    
    //library import test
    private static void importLib(PythonInterpreter interp)
    {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current Java relative path is: " + s);

        interp.exec("import os");
        interp.exec("print os");
        interp.exec("import sys");
        interp.exec("print sys");
        interp.exec("print sys.path");
        
        PySystemState sys = interp.getSystemState();
        String currPath = sys.getCurrentWorkingDir();
        System.out.println("Java sees current Python path as: "+currPath);
        String libPath = s+"\\libs";
        String mpmathPath = s+"\\libs\\mpmath-0.19";
        String sympyPath = s+"\\libs\\sympy-1.0";
        String gsPath = s+"\\libs\\geoscript-1.2.1";
        sys.path.append(new PyString(mpmathPath));
        sys.path.append(new PyString(sympyPath));
        sys.path.append(new PyString(libPath));
        
        interp.exec("print sys.path");

        //mpmath libs\mpmath-0.19
//        interp.exec("from mpmath import mp");
//        interp.exec("mp.dps = 50");
//        interp.exec("print(mp.quad(lambda x: mp.exp(-x**2), [-mp.inf, mp.inf]) ** 2)");
        
        //sympy \libs\sympy-1.0
        interp.exec("print('Current folder', os.getcwd());");
//        interp.exec("os.chdir('"+sympyPath+"')");
//        interp.exec("print('New current folder', os.getcwd());");
        

//        interp.exec("from sympy.core.add import *");
//        interp.exec("import sympy");
//        interp.exec("from sympy.core import *");
//        interp.exec("from sympy import *");
//        interp.exec("print(integrate(1/x, x))");
        
        //GeoScript
        //interp.exec("os.chdir('"+gsPath+"')");
        interp.exec("import geoscript");
        
    }
    
    private static void useFunctions(PythonInterpreter interp)
    {
        try {
            FunctionsSample fs = new FunctionsSample();
            interp.exec("import inspect");
            
            interp.exec(fs.getSample());
            
            interp.exec("objs = globals().copy()");
            interp.exec("functions = [o for o in objs if inspect.isfunction(objs[o])]");
            interp.exec("print(functions)");
            
            PyList functions = (PyList) interp.get("functions");
            
            interp.exec("args = inspect.getargspec("+functions.get(0)+")[0]");
            interp.exec("print(args)");
            
            PyList args = (PyList) interp.get("args");
            
            System.out.println("Argumrnts received!"+args.toString());
            
            //Provjera null-ova
            interp.exec("res = cur_mapping({\"id\": 6}, {\"country\": \"hrv\", \"city\": \"zagreb\"})");
            PyObject res = interp.get("res");
            
            System.out.println("Input mapping result received!"+res.toString());
            
        } catch (IOException ex) {
            Logger.getLogger(ScriptingMain.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private static void datesTest(PythonInterpreter interp)
    {
        interp.exec("from datetime import datetime, date, time");
        interp.exec("dt=datetime.now()");
        PyObject dt = interp.get("dt");
        
        System.out.println("Argumrnts received!"+dt.toString());
    }
    
    private static void stringTest(PythonInterpreter interp)
    {
        try {
            interp.exec("import sys");
            interp.exec("print (sys.stdout.encoding)");
            
            String test = "ščćšžđ test string";
            Charset myCharset = Charset.defaultCharset();
            Map<String,Charset> charsets = Charset.availableCharsets();
            String testNew = new String(test.getBytes("utf-16"),"utf-16");
            byte[] bytes = test.getBytes();
            byte[] utf8 = test.getBytes("utf-8");
            byte[] utf16 = test.getBytes("utf-16");
            PyUnicode stringSample = new PyUnicode(test);
            PyUnicode stringSample2 = new PyUnicode(testNew);
            interp.set("stringSample", stringSample);
            interp.set("stringSample2", stringSample2);
            interp.exec("print(stringSample.encode('windows-1250'))");
            interp.exec("print(stringSample2)");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ScriptingMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void memoryTest(PythonInterpreter interp)
    {
        PyInteger count = new PyInteger(0);
        interp.set("count", count);
        for(int i=0; i<1000000; i++)
        {
            String test = "This is a fairly long sentence that will be serving as a memory conpsumption example";
            PyUnicode stringSample = new PyUnicode(test);
            interp.set("stringSample", stringSample);
            interp.set("i", new PyInteger(i));
            interp.exec("count+=len(stringSample)");
            interp.exec("print(i, ': ', count)");
            interp.cleanup();
        }
        count = interp.get("count", PyInteger.class);
        System.out.println("Rezultat je "+count);
        
    }
}
