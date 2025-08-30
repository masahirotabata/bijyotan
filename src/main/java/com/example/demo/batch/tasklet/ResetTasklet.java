package com.example.demo.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.example.demo.domain.repository.WordRepository;

@Component
public class ResetTasklet implements Tasklet {

	private final WordRepository wordRepository;

	public ResetTasklet(WordRepository wordRepository) {
		this.wordRepository = wordRepository;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		System.out.println("ResetTaskletの実行");
		//wordRepository.resertStatus();
		return RepeatStatus.FINISHED;
	}

}
