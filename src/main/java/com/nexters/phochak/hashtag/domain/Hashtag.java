package com.nexters.phochak.hashtag.domain;

import com.nexters.phochak.post.domain.Post;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(indexes =
        {@Index(name = "idx01_hashtag", columnList = "TAG"),
        @Index(name = "idx02_hashtag", columnList = "POST_ID")})
public class Hashtag {
    public static final int HASHTAG_MAX_SIZE = 20;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="TAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="POST_ID", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @Size(min = 1, max = HASHTAG_MAX_SIZE)
    private String tag;

    public Hashtag() {
    }

    @Builder
    public Hashtag(Post post, String tag) {
        this.post = post;
        this.tag = tag;
    }
}
