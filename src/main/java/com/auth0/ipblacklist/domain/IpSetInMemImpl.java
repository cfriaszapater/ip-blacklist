package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Component
public class IpSetInMemImpl implements IpSet {
  private final String netsetPath;

  private Set<String> ipset = new HashSet<>();
  private Map<Integer, Map<String, String>> netmapsBySignificantBits = new TreeMap<>();

  @Autowired
  public IpSetInMemImpl(@Value("${netset.path}") String netsetPath) {
    this.netsetPath = netsetPath;
  }

  @Override
  public Mono<Boolean> matches(String ip) {
    return Mono.just(
      ipset.contains(ip) || anyNetmapMatches(ip)
    );
  }

  private boolean anyNetmapMatches(String ip) {
    return netmapsBySignificantBits.entrySet().stream()
      .anyMatch(entry ->
        entry.getValue().containsKey(SubNet.bitMaskFromIp(ip, entry.getKey()))
      );
  }

  @Override
  public Mono<Void> reload() throws ReloadException {
    // TODO allow list of netsetPaths
    return reload(Paths.get(netsetPath));
  }

  Mono<Void> reload(Path netsetPath) throws ReloadException {
    try (Stream<String> lines = Files.lines(netsetPath)) {
      lines
        .map(s -> s.trim())
        .filter(s -> !s.startsWith("#"))
        .forEach(ipOrSubnet -> add(ipOrSubnet));
    } catch (IOException e) {
      throw new ReloadException(e);
    }

    return Mono.empty();
  }

  void add(String ipOrSubnet) {
    if (SubNet.isSubnet(ipOrSubnet)) {
      String subnet = ipOrSubnet;
      // Add to corresponding map as per number of significant bits
      netmapForSignificantBits(subnet).put(SubNet.bitMaskOfSignificantBits(subnet), subnet);
    } else {
      ipset.add(ipOrSubnet);
    }
  }

  private Map<String, String> netmapForSignificantBits(String subnet) {
    return netmapForSignificantBits(SubNet.significantBits(subnet));
  }

  Map<String, String> netmapForSignificantBits(int significantBits) {
    if (!netmapsBySignificantBits.containsKey(significantBits)) {
      netmapsBySignificantBits.put(significantBits, new HashMap());
    }
    return netmapsBySignificantBits.get(significantBits);
  }
}
