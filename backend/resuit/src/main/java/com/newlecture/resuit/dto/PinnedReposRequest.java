package com.newlecture.resuit.dto;

import java.util.List;

public record PinnedReposRequest(List<String> repoFullNames) {
}
