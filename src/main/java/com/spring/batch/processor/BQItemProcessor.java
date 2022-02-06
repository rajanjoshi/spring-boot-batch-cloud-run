package com.spring.batch.processor;

import com.spring.batch.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class BQItemProcessor implements ItemProcessor<Corpus, Coffee> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BQItemProcessor.class);

    @Override
    public Coffee process(final Corpus corpus) throws Exception {
        Coffee transformedCorpus = new Coffee(corpus.getName().toUpperCase(),corpus.getName(),corpus.getName().toLowerCase());
        LOGGER.info("Converting ( {} ) into ( {} )", corpus, transformedCorpus);

        return transformedCorpus;
    }

}
