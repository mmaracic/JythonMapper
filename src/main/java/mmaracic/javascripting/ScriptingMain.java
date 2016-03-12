/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.JsonObject;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
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
        
        //threading(interp);
        //sample(interp);
        objects(interp);        
    }
    
    //multithreading test
    static void threading(PythonInterpreter interp)
    {
        int noThreads = 10;
        int noOutputs = 10;
        ExecutorService pool = Executors.newFixedThreadPool(5);
        for (int i=0;i<noThreads;i++)
        {
            pool.execute(new Processor(interp, i, noOutputs));
        }
        pool.shutdown();
    }
    
    //initial sample mapping test
    static void sample (PythonInterpreter interp)
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
    static void objects (PythonInterpreter interp)
    {
        try
        {
            JSONSample jsample = new JSONSample();
            JsonObject j = jsample.getSample();
            
            // The exec() method executes strings of code
            interp.exec("import sys");
            interp.exec("print sys");
            interp.exec("import json");
            interp.exec("print json");

            // Set variable values within the PythonInterpreter instance
            interp.set("a", new PyInteger(42));
            interp.exec("print a");
            interp.exec("x = 2+2");

            // Obtain the value of an object from the PythonInterpreter and store it
            // into a PyObject.
            PyObject x = interp.get("x");
            System.out.println("x: " + x);    
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
