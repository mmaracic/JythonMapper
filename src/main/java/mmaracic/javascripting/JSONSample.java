/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.javascripting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Marijo
 */
public class JSONSample {
    
    JsonObject sample;
    
    public JSONSample() throws FileNotFoundException{
        JsonReader jr = Json.createReader(new FileInputStream("json.txt"));
        sample = jr.readObject();
    }
    
    public JsonObject getSample()
    {
        return sample;
    }
        
}
