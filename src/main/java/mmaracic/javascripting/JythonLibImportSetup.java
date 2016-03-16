package mmaracic.javascripting;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * Log4j in Jython setup - log4j.properties file placing separate of standard Java placing - in appplication path
 * This is the reason of multiple log4j files in the project 
 * We will try to import mpmath library in the Jython environment
 * mpmath library folder is expected to reside within libs subolder of the current application path i.e. .\libs\mpmath-0.19.
 * Within the library path (.\libs\mpmath-0.19) we expect additional mpmath folder from which the import will be made
 * The expected result is 50 digits of PI - 3.1415926535897932384626433832795028841971693993751
 * 
 * To run construct the object and then call execute() method
 * 
 * @author marijom
 */
public class JythonLibImportSetup {
    
    private final PythonInterpreter interp;
    private final Logger logger;

    
    JythonLibImportSetup()
    {
        //initialize the interpreter
        interp = new PythonInterpreter();
        
        //initialize the logger
        logger = Logger.getLogger(JythonLibImportSetup.class);
    }
    
    public void execute()
    {
        logger.log(Level.INFO, "Starting JythonLibImportSetup module");
        
        interp.exec("import os");
        interp.exec("import sys");
        interp.exec("from org.apache.log4j import Logger, PropertyConfigurator");
        
        //Log4j in Jython setup - log4j.properties file placing separate of standard Java placing - in appplication path
        interp.exec("log = Logger.getLogger('JythonLibImportSetup Jython')");
        interp.exec("PropertyConfigurator.configure('.\\log4j.properties')");
        
        //current application path - equal to Jython current path
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        //Jython current path
        interp.exec("log.info('Current folder:'+os.getcwd());");
        
        //Environment object
        PySystemState sys = interp.getSystemState();
        //library folder is expected to reside within libs subolder of the current path i.e. .\libs\mpmath-0.19.
        //Within the library path (.\libs\mpmath-0.19) we expect additional mpmath folder from which the import will be made
        String mpmathPath = s+"\\libs\\mpmath-0.19";
        //sys.path is Jyton environment variable PATH; module path is included in that path by applying it
        sys.path.append(new PyString(mpmathPath));
        
        //importing mpmath from  .\libs\mpmath-0.19
        interp.exec("from mpmath import mp");
        //testing if the module was imported
        interp.exec("mp.dps = 50");
        interp.exec("test = mp.quad(lambda x: mp.exp(-x**2), [-mp.inf, mp.inf]) ** 2");
        interp.exec("log.info(str(test))");
        //result should be 3.1415926535897932384626433832795028841971693993751
    }
}
