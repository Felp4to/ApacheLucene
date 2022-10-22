package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
public class Searcher {

    private Directory Directory;
    private IndexReader reader;
    private IndexSearcher searcher;

    public Searcher(String indexPath) {
        try {
            this.Directory = FSDirectory.open(Paths.get(indexPath));
            this.reader = DirectoryReader.open(this.Directory);
            this.searcher = new IndexSearcher(this.reader);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void runQuery(String inputString) throws IOException, ParseException {
        boolean phraseQuery = false;
        String termsString;
        String[] words = inputString.split("\\s");
        if((inputString.indexOf("titolo:")!=0)&&(inputString.indexOf("contenuto:")!=0))
        {
            System.out.println("Syntax error!");
            return;
        }
        // mi prendo il campo e il tipo di query
        String field = words[0].substring(0, words[0].length() - 1);;
        phraseQuery = (inputString.indexOf("\"")==8) || (inputString.indexOf("\"")==11);

        if(Objects.equals(field, "titolo") == true)
        {
            if(phraseQuery){
                termsString = inputString.substring(9, inputString.length()-1);
                System.out.println("terms: " + termsString);
            } else {
                termsString = inputString.substring(8, inputString.length());
                System.out.println("terms: " + termsString);
            }
        } else {
            if(phraseQuery){
                termsString = inputString.substring(12, inputString.length()-1);
                System.out.println("terms: " + termsString);
            } else {
                termsString = inputString.substring(11, inputString.length());
                System.out.println("terms: " + termsString);
            }
        }

        Analyzer a = CustomAnalyzer.builder()
                .withTokenizer(WhitespaceTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                .build();
        QueryParser parser;
        if(Objects.equals(field, "titolo")){
            parser = new QueryParser(field, a);
        } else {
            parser = new QueryParser(field, new ItalianAnalyzer());
        }
        Query query = parser.parse(termsString);
        TopDocs hits = searcher.search(query, 10);
        if (hits.scoreDocs.length != 0) {
            System.out.println("Questi sono i primi 10 risultati:");
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = hits.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                System.out.printf("%d) %s%n", i + 1, doc.get("titolo"));
            }
        } else {
            System.out.println("No results found");
        }
    }

    public void close() {
        try {
            this.Directory.close();
            this.reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
