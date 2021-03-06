package com.manning.fia.transformations.media;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple5;

import com.manning.fia.utils.DateUtils;
import com.manning.fia.model.media.NewsFeed;
import org.joda.time.format.DateTimeFormat;

@SuppressWarnings("serial")
public class NewsFeedMapper2 implements MapFunction<String, Tuple5<String, String, Long, Long, Long>> {
    private DateUtils dateUtils = new DateUtils();

    @Override
    public Tuple5<String, String, Long, Long, Long> map(String value)
            throws Exception {
        NewsFeed newsFeed = NewsFeedParser.mapRow(value);
        long timeSpent = dateUtils.getTimeSpentOnPage(newsFeed);
        Tuple5<String, String, Long, Long, Long> tuple5 = new Tuple5<>(newsFeed.getSection(),
                newsFeed.getSubSection(),
                DateTimeFormat.forPattern("yyyyMMddHHmmss")
                        .parseDateTime(newsFeed.getStartTimeStamp()).getMillis(),
                DateTimeFormat.forPattern("yyyyMMddHHmmss")
                        .parseDateTime(newsFeed.getEndTimeStamp()).getMillis(),
                timeSpent);
        return tuple5;
    }
}

