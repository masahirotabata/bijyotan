package com.example.demo.batch.chunk;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.example.demo.domain.entity.Word;
import com.example.demo.domain.repository.WordRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@Slf4j
public class CsvItemWriter implements ItemWriter<Word> {

	private final WordRepository wordRepository;

	public CsvItemWriter(WordRepository wordRepository) {
		this.wordRepository = wordRepository;
	}

	@Override
	public void write(Chunk<? extends Word> chunk) throws Exception {
		log.info("登録データ={}", chunk);
		wordRepository.saveAll(chunk);

		log.info("-----CSVデータ登録完了-----");
	}

}
