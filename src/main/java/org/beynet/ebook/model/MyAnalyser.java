package org.beynet.ebook.model;

import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Created by beynet on 01/05/2015.
 */
public class MyAnalyser extends StopwordAnalyzerBase {

    public MyAnalyser() {
        super(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();

        TokenStream tokenStream = source;
        tokenStream = new LowerCaseFilter(tokenStream);
        tokenStream = new StopFilter(tokenStream, getStopwordSet());
        tokenStream = new ASCIIFoldingFilter(tokenStream);
        return new TokenStreamComponents(source, tokenStream);
    }
}
