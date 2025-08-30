package com.example.demo.batch.chunk;

import java.beans.PropertyEditorSupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.example.demo.domain.entity.Word;

import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@Slf4j
public class CsvItemReader extends FlatFileItemReader<Word> implements InitializingBean {

    @Value("#{jobParameters['filePath']}")
    private String filePath;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("-----CSVデータ登録開始 filePath={}-----", filePath);

        setResource(new FileSystemResource(filePath));
        setLinesToSkip(1); // ヘッダー行スキップ
        setEncoding(StandardCharsets.UTF_8.name());

        DefaultLineMapper<Word> lineMapper = new DefaultLineMapper<>();

        // CSVのカラム名とエンティティのフィールド名を合わせる
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("word", "meaning", "pictDescription", "status", "nextPresentation", "userId");

        BeanWrapperFieldSetMapper<Word> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Word.class);

        // LocalDateTime変換対応（ここが追加ポイント）
        fieldSetMapper.setCustomEditors(Map.of(
            LocalDateTime.class, new PropertyEditorSupport() {
                @Override
                public void setAsText(String text) {
                    setValue(LocalDateTime.parse(text));
                }
            }
        ));

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        setLineMapper(lineMapper);
        super.afterPropertiesSet();
    }
}
