package com.Server;

import org.apache.beam.sdk.schemas.JavaFieldSchema;
import org.apache.beam.sdk.schemas.annotations.DefaultSchema;

@DefaultSchema(JavaFieldSchema.class)
public class VoteElement {
    public String vote_id;
    @javax.annotation.Nullable public String vote;
    public Integer number_vote;
}