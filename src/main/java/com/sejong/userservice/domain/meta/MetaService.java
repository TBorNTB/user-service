package com.sejong.userservice.domain.meta;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.EXTERNAL_SERVER_ERROR;

import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.internal.PostInternalFacade;
import com.sejong.userservice.support.common.internal.UserInternalService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetaService {

    private final UserInternalService userInternalService;
    private final PostInternalFacade postInternalFacade;
    private final Executor metaAsyncExecutor;

    public MetaCountResponse getMetaCountInfo() {
        long start = System.currentTimeMillis();

        CompletableFuture<Long> userFuture = CompletableFuture
                .supplyAsync(userInternalService::getUserCount, metaAsyncExecutor)
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("[MetaService] User count 조회 실패", ex);
                    throw new BaseException(EXTERNAL_SERVER_ERROR);
                });

        CompletableFuture<MetaPostCountDto> postFuture = CompletableFuture
                .supplyAsync(postInternalFacade::getPostCount, metaAsyncExecutor)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("[MetaService] Post count 조회 실패", ex);
                    throw new BaseException(EXTERNAL_SERVER_ERROR);
                });

        CompletableFuture.allOf(userFuture, postFuture).join();

        log.info("[MetaService] 전체 조회 시간: {}ms", System.currentTimeMillis() - start);
        return MetaCountResponse.of(userFuture.join(), postFuture.join());
    }
}
