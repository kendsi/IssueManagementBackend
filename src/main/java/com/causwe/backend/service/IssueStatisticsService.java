package com.causwe.backend.service;

import java.util.Map;

public interface IssueStatisticsService {
    Map<String, Long> getIssuesPerMonth();
    // TODO: IssueStaticticsService.java와 함께 통계 기능 추가
}