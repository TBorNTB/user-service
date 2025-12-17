package com.sejong.userservice.support.common.util;

import java.util.Objects;

public class Filepath {
    private final String path;

    private Filepath(String path) {
        this.path = path;
    }

    public static Filepath of(String path) {
        return new Filepath(path);
    }

    public String path() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filepath filepath = (Filepath) o;
        return Objects.equals(path, filepath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}