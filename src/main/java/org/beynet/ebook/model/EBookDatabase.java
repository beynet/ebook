package org.beynet.ebook.model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.IndexedEBook;

public class EBookDatabase extends Observable {

    private final static Logger logger = LogManager.getLogger(EBookDatabase.class);

    private EBookDatabase(Path database) {
        // create lucene index
        try {
            Directory dir = FSDirectory.open(database);
            Analyzer analyzer = new MyAnalyser();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            this.writer = new IndexWriter(dir, iwc);
        } catch (IOException e) {
            throw new RuntimeException("error init lucene directory", e);
        }
    }

    public static EBookDatabase getInstance(Path... database) {
        if (_instance == null) {
            Path d;
            if (database.length > 0)
                d = database[0];
            else
                d = EBookUtils.getTargetDirectory().resolve("db");
            _instance = new EBookDatabase(d);
        }
        return _instance;
    }

    /**
     * clear all the library indexes
     * 
     * @throws IOException
     */
    public void clearIndexes() throws IOException {
        writer.deleteAll();
        writer.commit();
        needToReload = true;
    }

    /**
     * add provided path to database
     * 
     * @param pathToIndex the path to be indexed
     * @throws IOException
     */
    private void indexeDirectory(Path pathToIndex) throws IOException {
        // delete previous
        Term idTerm = new Term(FIELD_PATH, pathToIndex.toString());
        Query query = new TermQuery(idTerm);
        writer.deleteDocuments(query);

        // index it
        Document document = new Document();
        Field rootPath = new StringField(FIELD_ROOT_PATH, "true", Field.Store.YES); // all directories to be watch are indexed as "root path"
        document.add(rootPath);
        Field path = new StringField(FIELD_PATH, pathToIndex.toString(), Field.Store.YES);
        document.add(path);
        writer.addDocument(document);
    }

    /**
     * will index provided path and all sub directories
     * @param pathToIndex
     * @throws IOException
     */
    public void indexePath(Path pathToIndex) throws IOException {

        try {
            pathToIndex = pathToIndex.toAbsolutePath();
            indexeDirectory(pathToIndex);

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
                        eBook.index(
                                (e)->{
                                    try {
                                        _indexe(e);
                                    }catch(IOException ex)
                                    {
                                        throw new RuntimeException(ex);
                                    }
                                },
                                ()->{
                                    try {
                                        writer.commit();
                                    }catch(IOException ex)
                                    {
                                        throw new RuntimeException(ex);
                                    }
                                }
                        );
                    } catch (Exception e) {
                        logger.error("unable to index file " + file.toString(), e);
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
        } finally {
            writer.commit();
            needToReload = true;
        }
    }

    /**
     * index ebook in database
     * @param ebook
     */
    public void indexe(EBook ebook) throws IOException {
        try {
            // delegate indexation to ebook providing method.
            ebook.index(
                (e)->{
                try {
                    _indexe(e);
                }catch(IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            },
            ()->{
                try {
                    writer.commit();
                }catch(IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            );
        }
        catch(RuntimeException e) {
            if (e.getCause()!=null && IOException.class.isAssignableFrom(e.getClass().getClass())) {
                throw ((IOException)e.getCause());
            }
            throw e;
        }
    }

    public void unIndexe(Path path) throws IOException {
        // unindex previous version
        Term pathTerm = new Term(FIELD_PATH, path.toString());
        Query query = new TermQuery(pathTerm);
        writer.deleteDocuments(query);
    }

    private void _indexe(EBook ebook) throws IOException {

        // unindex previous version
        String id = ebook.getIdentifier().orElse("");
        id=id.stripTrailing();
        if ("".equals(id)) id=ebook.getTitle().orElse("").concat(" ").concat(ebook.getAuthor().orElse(" "));

        Term idTerm = new Term(FIELD_ID, id);
        Query query = new TermQuery(idTerm);
        writer.deleteDocuments(query);

        Document document = new Document();

        Field rootPath = new StringField(FIELD_ROOT_PATH, "false", Field.Store.YES);
        document.add(rootPath);

        Field idField = new StringField(FIELD_ID, id, Field.Store.YES);
        document.add(idField);

        Field pathField = new StringField(FIELD_PATH, ebook.getPath().toString(), Field.Store.YES);
        document.add(pathField);

        Field titleField = new TextField(FIELD_TITLE, ebook.getTitle().orElse(""), Field.Store.YES);
        document.add(titleField);

        Field authorField = new TextField(FIELD_AUTHOR, ebook.getAuthor().orElse(""), Field.Store.YES);
        document.add(authorField);

        String text = ebook.getAuthor().orElse("").concat(" ").concat(ebook.getTitle().orElse(""));
        if (!ebook.getSubjects().isEmpty()) {
            text = text.concat(" ").concat(ebook.getSubjects().get(0));
        }
        Field textField = new TextField(FIELD_TEXT,text
                , Field.Store.YES);
        document.add(textField);

        for (String subject : ebook.getSubjects()) {
            Field subjectField = new StringField(FIELD_SUBJECT, subject, Field.Store.YES);
            document.add(subjectField);
        }
        writer.addDocument(document);
        logger.debug("ebook "+ebook.getPath()+" indexed");
    }

    private IndexReader createReader() throws IOException {
        return DirectoryReader.open(writer, true, true);
    }

    /**
     * list all indexed ebooks matching query
     * 
     * @param query
     * @return
     * @throws IOException
     */
    public List<EBook> list(String query) throws IOException {
        if (query == null || "".equals(query)) {
            return list();
        } else {
            List<EBook> result = new ArrayList<>();
            final IndexReader reader = createReader();
            try {
                IndexSearcher searcher = new IndexSearcher(reader);
                QueryParser parser = new QueryParser(FIELD_TEXT,new MyAnalyser());
                parser.setAllowLeadingWildcard(true);
                BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

                /*Query patternQuery = new WildcardQuery(new Term(FIELD_TEXT, "*" + query + "*"));*/
                final Query patternQuery ;
                try {
                    patternQuery = parser.parse("*".concat(query).concat("*"));
                } catch (ParseException e) {
                    logger.error("error in query",e);
                    throw new IOException("error in query",e);
                }
                booleanQueryBuilder.add(patternQuery, BooleanClause.Occur.MUST);

                TopScoreDocCollector collector = TopScoreDocCollector.create(1000, 1000);

                searcher.search(booleanQueryBuilder.build(), collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    if (d.get(FIELD_ROOT_PATH).equals("true")) continue;
                    result.add(ebookFromDocument(d));
                }
                return result;
            } finally {
                reader.close();
            }
        }
    }

    /**
     * convert lucene document to fake ebook "IndexedEBook"
     * 
     * @param d lucene document
     * @return
     */
    private EBook ebookFromDocument(Document d) {
        List<String> subjects = Arrays.stream(d.getValues(FIELD_SUBJECT)).toList();
        IndexedEBook indexed = new IndexedEBook(d.get(FIELD_ID), d.get(FIELD_PATH), d.get(FIELD_AUTHOR),
                d.get(FIELD_TITLE), subjects);
        return indexed;
    }

    /**
     * @return indexed folder list
     * @throws IOException
     */
    public List<Path> listIndexedFolder() throws IOException {
        List<Path> result = new ArrayList<>();
        final IndexReader reader = createReader();
        try {
            IndexSearcher searcher = new IndexSearcher(reader);

            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

            Query allDocsQuery = new MatchAllDocsQuery();
            booleanQueryBuilder.add(allDocsQuery, BooleanClause.Occur.MUST);
            synchronized (this) {

                TotalHitCountCollector collector = new TotalHitCountCollector();
                BooleanQuery query = booleanQueryBuilder.build();
                // retrieve number of hits
                searcher.search(query, collector);
                // read all
                ScoreDoc[] results = searcher.search(query, Math.max(1, collector.getTotalHits())).scoreDocs;

                for (int i = 0; i < results.length; ++i) {
                    int docId = results[i].doc;
                    Document d = searcher.doc(docId);
                    if (!"true".equals(d.get(FIELD_ROOT_PATH)))
                        continue;
                    Path path = Paths.get(d.get(FIELD_PATH));
                    result.add(path);
                    logger.info("indexed folder "+path.toString());
                }
            }
            return result;
        } finally {
            reader.close();
        }
    }

    public void startWatchService() {
        /*try {
            List<Path> toIndex = listIndexedFolder();
            clearIndexes();
            toIndex.forEach(p -> {
                try {
                    indexeDirectory(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch(IOException e) {
            logger.error("error when reconstructing indexes");
        }*/
        Runnable r = () -> {

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

                while (!Thread.currentThread().isInterrupted()) {

                    // if needToReload has been invoked
                    if (needToReload==true) {
                        logger.info("loading directories to watch");
                        for (WatchKey watchKey : watched.keySet()) {
                            watchKey.cancel();
                        }
                        watched.clear();
                        needToReload = false ;
                        List<Path> indexedFolders = listIndexedFolder();
                        List<Path> allFolders = new ArrayList<>();
                        //allFolders.addAll(indexedFolders);
                        // add all child off indexed folders to directories to be watched
                        indexedFolders.forEach(p->{
                            try {
                                Files.walkFileTree(p, new FileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                        allFolders.add(dir);
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        allFolders.forEach(p -> {
                            try {
                                logger.info("will watch " + p.toString());
                                WatchKey register = p.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                                        StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                                watched.put(register, p);
                            } catch (IOException e) {
                                logger.error("unable to watch path " + p.toString(), e);
                            }
                        });
                    }
                    logger.info("entering into loop");

                    WatchKey taken;
                    try {
                        taken=watchService.poll(1, TimeUnit.SECONDS);
                    }catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (taken!=null) {
                        try {
                            logger.debug("new event");
                            for (WatchEvent<?> event : taken.pollEvents()) {
                                WatchEvent.Kind<?> kind = event.kind();

                                // OVERFLOW
                                if (kind == StandardWatchEventKinds.OVERFLOW) {
                                    continue;
                                }
                                // The filename is the
                                // context of the event.
                                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                                Path currentDir = watched.get(taken);
                                Path filename = currentDir.resolve(ev.context());

                                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    logger.info("File " + filename.toString() + " modified");
                                    if (Files.isDirectory(filename)) continue;
                                    try {
                                        EBook ebookModified = EBookFactory.createEBook(filename);
                                        indexe(ebookModified);
                                        notifyObservers(new EBookModifiedOrAdded(ebookModified));
                                    } catch (IOException e) {
                                        logger.error("unable to read or index ebook " + filename.toString(), e);
                                    }
                                }
                                else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                    logger.info("File " + filename.toString() + " deleted");
                                    if (Files.isDirectory(filename)) continue;
                                    try {
                                        unIndexe(filename);
                                        notifyObservers(new EBookDeleted(filename));
                                    } catch (IOException e) {
                                        logger.error("unable to read or index ebook " + filename.toString(), e);
                                    }
                                }
                            }
                        } finally {
                            taken.reset();
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("unexpected error", e);
            } finally {
                logger.info("end of thread");
                watched.clear();
            }
        };
        logger.info("will start watch service");
        watchServiceThread = new Thread(r);
        watchServiceThread.start();

    }

    public void stopWatchService() {
        logger.info("stopping service");
        watchServiceThread.interrupt();
        try {
            watchServiceThread.join();
        } catch (InterruptedException e) {
            //
        }
    }

    /**
     * @return all indexed ebooks
     * @throws IOException
     */
    public List<EBook> list() throws IOException {
        List<EBook> result = new ArrayList<>();
        final IndexReader reader = createReader();
        try {
            IndexSearcher searcher = new IndexSearcher(reader);

            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

            Query allDocsQuery = new MatchAllDocsQuery();
            booleanQueryBuilder.add(allDocsQuery, BooleanClause.Occur.MUST);
            synchronized (this) {

                TotalHitCountCollector collector = new TotalHitCountCollector();
                BooleanQuery query = booleanQueryBuilder.build();
                // retrieve number of hits
                searcher.search(query, collector);
                // read all
                ScoreDoc[] results = searcher.search(query, Math.max(1, collector.getTotalHits())).scoreDocs;

                for (int i = 0; i < results.length; ++i) {
                    int docId = results[i].doc;
                    Document d = searcher.doc(docId);
                    if (d.get(FIELD_ROOT_PATH).equals("true"))
                        continue;
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

    private IndexWriter          writer;
    private static EBookDatabase _instance = null;
    private Thread               watchServiceThread = null;
    private Map<WatchKey, Path>  watched = new HashMap<>();
    private boolean              needToReload = true;

    private static final String FIELD_PATH = "path";
    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_ROOT_PATH = "rootpath";
}
