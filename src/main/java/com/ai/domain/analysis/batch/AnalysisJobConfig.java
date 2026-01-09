package com.ai.domain.analysis.batch;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import com.ai.domain.analysis.dto.AnalysisResultDto;
import com.ai.domain.analysis.service.GptAnalysisService;
import com.ai.global.utils.CsvToExcelConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AnalysisJobConfig {
    private final GptAnalysisService gptAnalysisService;
    private final JobProgressListener jobProgressListener;
    private final FailureLogListener failureLogListener;

    @Bean
    public Job analysisJob(JobRepository jobRepository, Step analysisStep,JobResultListener jobResultListener) {
        return new JobBuilder("analysisJob", jobRepository)
                .start(analysisStep)
                .listener(jobResultListener)
                .build();
    }

    @Bean
    public Step analysisStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("analysisStep", jobRepository)
                .<AnalysisRequestDto, AnalysisResultDto>chunk(10, transactionManager)
                .reader(dynamicReader(null,null))
                .processor(gptProcessor())
                .writer(synchronizedJsonWriter(null))
                .taskExecutor(executor())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(1000)
                .skip(Exception.class)
                .listener(failureLogListener)
                .listener(jobProgressListener)
                .build();
    }

    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("gpt-batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<AnalysisRequestDto> dynamicReader(
        @Value("#{jobParameters['filePath']}") String filePath,
        @Value("#{jobParameters['fileType']}") String fileType
    ) {
        if (filePath == null) {
            log.warn("JobParameter 'filePath' is null. Using default or empty reader.");
        }
        
        ItemStreamReader<AnalysisRequestDto> delegateReader;
        if ("EXCEL".equalsIgnoreCase(fileType)) {
            EasyExcelItemReader excelReader = new EasyExcelItemReader(filePath);
            excelReader.open(new ExecutionContext());
            delegateReader = excelReader;
        } else {
             throw new IllegalArgumentException("지원하지 않는 파일 타입입니다(지원 : CSV,EXCEL) : " + fileType);
        }

        return new SynchronizedItemStreamReaderBuilder<AnalysisRequestDto>()
                .delegate(delegateReader)
                .build();

    }

    @Bean
    public ItemProcessor<AnalysisRequestDto, AnalysisResultDto> gptProcessor() {
        return item -> {
            AnalysisResultDto result = gptAnalysisService.analyzeWord(item);
            log.info("[{}] Processing: {}", Thread.currentThread().getName(), item.getExpression());

            boolean isFail =
                    result == null ||
                            result.getExampleJp() == null ||
                            result.getMeaningKr().contains("GPT processing failed");

            if (isFail) {
                FailedWordsCollector.add(item);
                return null;
            }


            return gptAnalysisService.analyzeWord(item);
        };
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamWriter<AnalysisResultDto> synchronizedJsonWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath
    ) {
        if (outputPath == null) {
            outputPath = System.getProperty("user.dir") + "/result/default_result.json";
        }

        File file = new File(outputPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        JsonFileItemWriter<AnalysisResultDto> jsonWriter = new JsonFileItemWriterBuilder<AnalysisResultDto>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource(outputPath))
                .name("jsonWriter")
                .append(true)
                .build();

        return new SynchronizedItemStreamWriterBuilder<AnalysisResultDto>()
                .delegate(jsonWriter)
                .build();
    }
}