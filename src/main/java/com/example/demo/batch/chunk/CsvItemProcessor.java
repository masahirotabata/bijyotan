package com.example.demo.batch.chunk;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.demo.domain.entity.Word;

import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@Slf4j
public class CsvItemProcessor implements ItemProcessor<Word, Word> {

	@Override
	public Word process(Word item) throws Exception {
		log.info("CSVファイルの編集を行います item={}", item);
		item.setStatus(false);
		item.setNextPresentation(null);
		return item;
	}

}
