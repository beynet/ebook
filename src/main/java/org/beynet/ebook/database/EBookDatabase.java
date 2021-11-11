package org.beynet.ebook.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.IndexedEBook;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EBookDatabase {

    private final static Logger logger = LogManager.getLogger(EBookDatabase.class);

    private EBookDatabase(Path database) {
        //create lucene index
        try {
            Directory dir = FSDirectory.open(database);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            this.writer = new IndexWriter(dir, iwc);
        }catch(IOException e) {
            throw new RuntimeException("error init lucene directory",e);
        }
    }

    public static EBookDatabase getInstance(Path ... database) {
        if (_instance==null) {
            Path d ;
            if (database.length>0) d = database[0];
            else d = EBookUtils.getTargetDirectory().resolve("db");
            _instance  = new EBookDatabase(d);
        }
        return _instance;
    }


    public void clear() throws IOException {
        writer.deleteAll();
        writer.commit();
    }

    public void indexePath(Path pathToIndex) throws IOException {
        Files.walkFileTree(pathToIndex, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final EBook eBook;
                try {
                    eBook = EBookFactory.createEBook(file);
                    indexe(eBook);
                } catch (Exception e) {
                    logger.info("unable to index file "+file.toString(),e);
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * index ebook in database
     * @param ebook
     */
    public void indexe(EBook ebook) throws IOException {
        // unindex previous version
        Term idTerm = new Term(FIELD_ID, ebook.getIdentifier().orElse(""));
        Query query = new TermQuery(idTerm);
        writer.deleteDocuments(query);

        Document document = new Document();

        Field idField = new StringField(FIELD_ID, ebook.getIdentifier().orElse(""), Field.Store.YES);
        document.add(idField);

        Field pathField = new StringField(FIELD_PATH, ebook.getPath().toString(), Field.Store.YES);
        document.add(pathField);

        Field titleField = new TextField(FIELD_TITLE, ebook.getTitle().orElse(""), Field.Store.YES);
        document.add(titleField);

        Field authorField = new TextField(FIELD_AUTHOR, ebook.getAuthor().orElse(""), Field.Store.YES);
        document.add(authorField);

        Field textFiled = new TextField(FIELD_TEXT, ebook.getAuthor().orElse("").concat(" ").concat(ebook.getTitle().orElse("")), Field.Store.YES);
        document.add(textFiled);

        for (String subject : ebook.getSubjects()) {
            Field subjectField = new StringField(FIELD_SUBJECT, subject, Field.Store.YES);
            document.add(subjectField);
        }


        writer.addDocument(document);
        writer.commit();
    }


    private IndexReader createReader() throws IOException {
        return DirectoryReader.open(writer,true,true);
    }

    public List<EBook> list(String query) throws IOException {
        if (query==null || "".equals(query)) {
            return list();
        }
        else {
            List<EBook> result = new ArrayList<>();
            final IndexReader reader = createReader();
            try {
                IndexSearcher searcher = new IndexSearcher(reader);
                BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

                Query patternQuery = new WildcardQuery(new Term(FIELD_TEXT, "*" + query + "*"));
                booleanQueryBuilder.add(patternQuery, BooleanClause.Occur.MUST);

               
                TopScoreDocCollector collector = TopScoreDocCollector.create(1000, 1000);

                searcher.search(booleanQueryBuilder.build(), collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    result.add(ebookFromDocument(d));
                }
                return result;
            } finally {
                reader.close();
            }
        }
    }

    private EBook ebookFromDocument(Document d) {
        List<String> subjects = Arrays.stream(d.getValues(FIELD_SUBJECT)).toList();
        IndexedEBook indexed = new IndexedEBook(d.get(FIELD_ID), d.get(FIELD_PATH), d.get(FIELD_AUTHOR),
                d.get(FIELD_TITLE), subjects);
        return indexed;
    }

    public List<EBook> list() throws IOException {
        List<EBook> result = new ArrayList<>();
        final IndexReader reader = createReader();
        try {
            IndexSearcher searcher = new IndexSearcher(reader);

            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

            Query allDocsQuery = new MatchAllDocsQuery();
            booleanQueryBuilder.add(allDocsQuery, BooleanClause.Occur.MUST);
            synchronized (this) {

                TotalHitCountCollector  collector = new TotalHitCountCollector();
                BooleanQuery query = booleanQueryBuilder.build();
                // retrieve number of hits
                searcher.search(query, collector);
                //read all
                ScoreDoc[] results = searcher.search(query, Math.max(1, collector.getTotalHits())).scoreDocs;

                for (int i = 0; i < results.length; ++i) {
                    int docId = results[i].doc;
                    Document d = searcher.doc(docId);
                    result.add(ebookFromDocument(d));

                }
            }
            return result;
        } finally {
            reader.close();
        }
    }

    public void dispose() throws IOException {
        writer.close();
        _instance = null;
    }

    private IndexWriter writer;
    private static EBookDatabase _instance = null;

    private static final String FIELD_PATH = "path";
    private static final String FIELD_ID   = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_TEXT = "text";
}
