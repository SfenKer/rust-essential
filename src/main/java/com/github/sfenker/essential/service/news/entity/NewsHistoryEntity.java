package com.github.sfenker.essential.service.news.entity;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "news_history")
public class NewsHistoryEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    public @NotNull Long id;

    @Column(name = "url")
    public @NotNull String url;

}
