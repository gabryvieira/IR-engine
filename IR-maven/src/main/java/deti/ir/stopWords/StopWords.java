
package deti.ir.stopWords;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Universidade de Aveiro, DETI, Recuperação de Informação 
 * @author Gabriel Vieira, gabriel.vieira@ua.pt
 * @author Rui Oliveira, ruipedrooliveira@ua.pt
 */
public class StopWords {
    
    ArrayList<String> stopWords; // array list de stop words de stop words
    
    public StopWords(Path dirPath) throws Exception{
        stopWords = new ArrayList<>();
        
        // obter ficheiros de um dado directorio
        try(Stream<String> lines = Files.lines(dirPath)){
            lines.filter(line -> line.length() > 2).forEach(s -> stopWords.add(s));
        }   
    }
    
            // verificar se e stop word
    public boolean isStopWord(String token){
        return stopWords.contains(token);
    }
    
    public int getSize(){
        return stopWords.size();
    }
    
     
    public boolean StopListEmpty(){
        return stopWords.isEmpty();
    }
}
