package deti.ir.indexer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.regex.Pattern;

/**
 * Indexer component of the Pipeline Processor.
 * @author vsantos,mvicente
 */
public class Indexer {

    /**
     * Hashmap containing the term as a key and an Hashmap as value. The hashmap
     * consists on a set of documents where the term was found and the number of
     * times that it was found in each document.
     */
    private final TokenPost[] termReferences;
    
    private Pattern pattern_ad = Pattern.compile("^[a-d]\\w*");
    private Pattern pattern_el = Pattern.compile("[e-l]\\w*");
    private Pattern pattern_mr = Pattern.compile("^[m-r]\\w*");
    private Pattern pattern_sz = Pattern.compile("^[s-z]\\w*");


    /**
     * Alphabet letters.
     */
    private final String[] alphapet = {
        "\\", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
        "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };

    /**
     * Value of the current value of the root for normalization.
     */
    private double sumxi;

    /**
     * Term frequency in the current document.
     */
    private HashMap<String, Integer> tokenFreqDocMap;
    
    /**
     * Term positions in the current document.
     */
    private HashMap<String, String> tokenPosDocMap;
    
    /**
     * Indexer constructor that creates an Inverted Index.
     */
    public Indexer() {
        sumxi = 0;
        //We split the indexers in five, 4 for groups of the alphabet and 1 for numbers
        termReferences = new TokenPost[5];
        for (int i = 0; i < 5; i++) {
            termReferences[i] = new TokenPost(i, 0);
        }
        
        tokenFreqDocMap = new HashMap<>();
        tokenPosDocMap = new HashMap<>();
    }

    /**
     * Add new term to the Indexer. If the term was already found, update the
     * Hashmaps, otherwise add it to the Hashmaps.
     * @param term new term.
     * @param docId document identification.
     */
    public void addTerm(String term, int docId, int position) {
        //System.out.println(docId);
        int mapIndex = findTermMap(term);

        tokenFreqDocMap.merge(term, 1, (a, b) -> a + b);
        tokenPosDocMap.merge(term, position + ";", (a, b) -> a + b);
        
    }

    /**
     * Compute TF of the values of the current document.
     * @param docId document identification.
     */
    public void calculateTF(int docId) {
        
        Map<String, String> tmp;
        tmp = tokenFreqDocMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> String.valueOf(computeValue(e.getValue()))));
        
        //System.out.println(termFreqOfDoc.toString()) ;
        
        tokenFreqDocMap = new HashMap<>();
        HashMap<String, String> tmp2 = new HashMap<>(tmp);
        
        //System.out.println(tmp2.toString()) ;
         
        tmp2.entrySet().forEach((entry) -> {
            //System.out.println( "getValue: "+rootVal); 
            termReferences[findTermMap(entry.getKey())].compute(entry.getKey(), (k, v) -> v == null ? getNewHM(docId, entry.getValue(), tokenPosDocMap.get(entry.getKey())) : updateHM(docId, entry.getValue(), v, tokenPosDocMap.get(entry.getKey())));
        });
        

        sumxi = 0;
        tokenPosDocMap = new HashMap<>();
    }

    /**
     * Compute the updated value of the document.
     * @param value new value found.
     * @return updated value.
     */
    private double computeValue(double value) {
        
        double tf = 1 + Math.log10(value);
        sumxi += Math.pow(tf, 2);
        //System.out.println("val:"+val+" root:"+rootVal); 
        return tf;
    }

    /**
     * Add a new hashmap to the TermReferences Index.
     * @param docId document identification.
     */
    private HashMap<Integer, String> getNewHM(int docId, String value, String pos) {
        //System.out.println("------------------>"+value); 
        HashMap<Integer, String> map = new HashMap<>();
        map.put(docId, tfNormalization(Double.valueOf(value)) + "_" + pos);
        return map;
    }

    /**
     * Update an existing hashmap in the TermReferences Index with a new value.
     * @param docId document identification.
     * @param hm hashmap with update
     */
    private HashMap<Integer, String> updateHM(int docId, String value, HashMap<Integer, String> hm, String pos) {
        //System.out.println("VALUE "+value); 
        hm.put(docId, tfNormalization(Double.valueOf(value)) + "_" + pos);
        return hm;
    }

    /**
     * Compute normalization in the current document.
     * @param value value computed.
     * @return normalization result.
     */
    private double tfNormalization(double value) {
        //System.out.println(value); 
        //System.out.println("fct"+df.format(value / Math.sqrt(rootVal))); 
        return  value/Math.sqrt(sumxi);
    }

    /**
     * Write the Reference Maps to a file to free memory space.
     */
    public void freeRefMaps() {
        int i = 0;
        for (TokenPost tf : termReferences) {
            if (!tf.isEmpty()) {
                tf.storeTermRefMap(tf.getSubId());
                termReferences[i] = new TokenPost(i, tf.getSubId() + 1);
                
            }
            i++;
        }

    }

    /**
     * Join Reference Maps written in files to current in memory and write them
     * all to a file.
     */
    public void joinRefMaps() {
        freeRefMaps();

        for (String letter : alphapet) {
            int numberOfFiles = termReferences[findTermMap(letter)].getSubId();
            TokenPost tr = new TokenPost(0, 0);
            for (int i = 0; i < numberOfFiles; i++) {
                TokenPost tri = new TokenPost(findTermMap(letter), i);
                tri.loadTermRefMap(letter, i);
                tr.mergeRefMap(tri);
                tri = null;
            }
            try {
                for (int j = 0; j < numberOfFiles; j++) {
                    Files.delete(FileSystems.getDefault().getPath("outputs/termRef_" + letter + "_" + findTermMap(letter) + j));
                }
            } catch (IOException ex) {
            }
            tr.storeFinalMap(letter);
        }
    }
    
   
    private int findTermMap(String term) {

        if (pattern_ad.matcher(term).matches()) {
            return 0;
        } else if (pattern_el.matcher(term).matches()) {
            return 1;
        } else if (pattern_mr.matcher(term).matches()) {
            return 2;
        } else if (pattern_sz.matcher(term).matches()) {
            return 3;
        } else {
            return 4;
        }
    }
        

}
