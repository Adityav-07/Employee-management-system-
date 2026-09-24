package com.project.hrms.dto;

import java.time.LocalDate;

public class RecentActivityResponse {

    private String type;

    private String title;

    private LocalDate date;

    public RecentActivityResponse(
            String type,
            String title,
            LocalDate date
    ) {

        this.type = type;

        this.title = title;

        this.date = date;

    }

    public String getType() {

        return type;

    }

    public String getTitle() {

        return title;

    }

    public LocalDate getDate() {

        return date;

    }

}