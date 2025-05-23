package com.cyn.qcscribe.domain;

public record Chunk(String id, String text, float[] embedding) {
}
