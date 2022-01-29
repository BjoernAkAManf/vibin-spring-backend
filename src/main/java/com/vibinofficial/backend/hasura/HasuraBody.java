package com.vibinofficial.backend.hasura;

import lombok.Data;

@Data
public class HasuraBody<T> {
    private T input;
}
