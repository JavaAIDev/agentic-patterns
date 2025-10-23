package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import java.util.List;

public record AlgorithmArticleGenerationRequest(String algorithm,
                                                List<String> languages) {

}
