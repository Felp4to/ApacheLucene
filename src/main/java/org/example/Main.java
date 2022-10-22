package org.example;

import java.util.Scanner;


public class Main {

    public static final String DirectoryFilesTXT = ".\\src\\main\\java\\org\\example\\txt";
    public static final String indexPath = "target/idxH";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Inserisci la tua query: ");
        String query = scanner.nextLine();
        Indice i = new Indice(indexPath, DirectoryFilesTXT);
        i.creaIndice();
        i.close();
        Searcher s = new Searcher(indexPath);
        s.runQuery(query);
        s.close();
    }
}