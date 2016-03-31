/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 *
 * @author Marijo
 */
public class FunctionsSample {
    
    String sample;
    
    public FunctionsSample() throws IOException{
       File f = new File("functions.txt");
       byte[] b = Files.readAllBytes(f.toPath());
       sample = new String(b, Charset.forName("UTF-8"));
    }
    
    public String getSample()
    {
        return sample;
    }
        
}
