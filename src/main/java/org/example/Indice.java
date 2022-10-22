package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Indice {

    private String indexPath = "target/idxH";
    private String documentsPath;
    private Directory Directory;

    public Indice(String indexPath, String documentsPath) {
        try {
            this.indexPath = indexPath;
            this.documentsPath = documentsPath;
            this.Directory = FSDirectory.open(Paths.get(indexPath));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void creaIndice() throws Exception {
        Map<String, String> files = MapFiles();
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        // associazione campo-analyzer
        Analyzer a = CustomAnalyzer.builder()
                .withTokenizer(WhitespaceTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                .build();
        perFieldAnalyzers.put("titolo", a);
        perFieldAnalyzers.put("contenuto", new ItalianAnalyzer());
        Analyzer analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzers);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setCodec(new SimpleTextCodec());
        IndexWriter writer = new IndexWriter(this.Directory, config);
        writer.deleteAll();
        for (Map.Entry<String, String> file : files.entrySet()) {
            //System.out.println(file.getKey() + ":" + file.getValue());
            Document doc = new Document();
            doc.add(new TextField("titolo", file.getKey(), Field.Store.YES));
            doc.add(new TextField("contenuto", file.getValue(), Field.Store.YES));
            writer.addDocument(doc);
            writer.commit();
        }
        writer.close();
    }

    public Map<String, String> MapFiles() throws Exception {
        Map<String, String> files = new HashMap<>();
        File folder = new File(String.valueOf(this.documentsPath));
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles != null)
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    //System.out.println(file.getName());
                    File f = new File(this.documentsPath + "\\" + file.getName());
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    StringBuilder stringBuilder = new StringBuilder();
                    String ls = System.getProperty("line.separator");
                    String line;
                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append(ls);
                    }
                    String name = getNameWithoutExtension(file.getName());
                    String content = stringBuilder.toString();
                    files.put(name, content);
                    //System.out.print(content);
                }
            }
        //System.out.println(files);
        return files;
    }

    public static String getNameWithoutExtension(String file) {
        int dotIndex = file.lastIndexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }

    public void close() {
        try {
            Directory.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }



}
