package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import reactor.core.publisher.Mono;

public interface IpSet {
  Mono<Boolean> match(String ip);

  Mono<Void> reload() throws ReloadException;
}
