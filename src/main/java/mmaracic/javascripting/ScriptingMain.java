/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.JsonObject;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Marijo
 */
public class ScriptingMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PyException {
        
        // Create an instance of the PythonInterpreter
        PythonInterpreter interp = new PythonInterpreter();
        
        threading(interp);
        //sample(interp);
        //objects(interp);
        //importLib(interp);
        
        //standalone interpreter class
        //JythonLibImportSetup jlis = new JythonLibImportSetup();
        //jlis.execute();
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
}
