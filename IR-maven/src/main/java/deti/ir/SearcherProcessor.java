package deti.ir;

import deti.ir.indexer.Indexer;
import deti.ir.stemmer.Stemmer;
import deti.ir.stopWords.StopWords;
import deti.ir.tokenizer.Tokenizer;
import java.io.BufferedReader;
import javax.management.Query;

/**
 *
 * @author gabriel
 */

public class SearcherProcessor {
    private StopWords sw; 
    private Stemmer stemmer; 
    private Indexer indexer; 
    private Tokenizer tok; 
    
    public SearcherProcessor(){
        this.tok = new Tokenizer(); 
        this.indexer = new Indexer();
        this.stemmer = new Stemmer();
        this.sw = new StopWords(); 
    }
    
    public void start(){
        
    }
    
    // buffRead to read user inputs
    private Query getQuery(BufferedReader buffRead){
        Query query = null;
        return query;
    }
}
