package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
public class IpSetInMemImplTest {

  private final IpSetInMemImpl ipSet = new IpSetInMemImpl("src/test/resources");

  @Test
  public void GivenIpBlacklisted_WhenMatches_ThenTrue() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/simple.netset")).block();

    Mono<Boolean> result = ipSet.matches("23.107.124.53");

    StepVerifier.create(result).expectNextMatches(b -> b == true).verifyComplete();
  }

  @Test
  public void GivenSubnetBlacklisted_WhenMatches_ThenTrue() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/simple.netset")).block();

    Mono<Boolean> result = ipSet.matches("31.11.43.233");

    StepVerifier.create(result).expectNextMatches(b -> b == true).verifyComplete();
  }

  @Test
  public void GivenIpNotBlacklisted_WhenMatches_ThenFalse() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/simple.netset")).block();

    Mono<Boolean> result = ipSet.matches("1.1.1.1");

    StepVerifier.create(result).expectNextMatches(b -> b == false).verifyComplete();
  }

}