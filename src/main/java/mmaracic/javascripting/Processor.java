/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import org.python.core.PyInteger;
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

    @Override
    public void run() {
        for(int i=0; i<count; i++){
            interp.set("i", new PyInteger(i));
            interp.set("index", new PyInteger(index));
            interp.exec("print str(i)+' : index '+str(index)");
        }
    }    
}
