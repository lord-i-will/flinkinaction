package com.manning.fia.c06;

import com.manning.fia.model.media.NewsFeed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

public class SlidingNewsFlinkEventTimeWindow extends WindowAssigner<NewsFeed, TimeWindow> {

  private final long slide;
  private final long size;

  public SlidingNewsFlinkEventTimeWindow(long size, long slide) {
    this.slide = slide;
    this.size = size;
  }

  @Override
  public Collection<TimeWindow> assignWindows(NewsFeed newsFeed, long timestamp,
                                              WindowAssignerContext windowAssignerContext) {

    long slide = newsFeed instanceof SlidingNewsFeed ? ((SlidingNewsFeed) newsFeed).getSlide() : this.slide;

    if (timestamp > Long.MIN_VALUE) {
      List<TimeWindow> windows = new ArrayList<>((int) (size / slide));
      long offset = 0L;
      long lastStart = TimeWindow.getWindowStartWithOffset(timestamp, offset, slide);
      for (long start = lastStart;
           start > timestamp - size;
           start -= slide) {
        windows.add(new TimeWindow(start, start + size));
      }
      return windows;
    } else {
      throw new RuntimeException("Record has Long.MIN_VALUE timestamp (= no timestamp marker). " +
        "Is the time characteristic set to 'ProcessingTime', or did you forget to call " +
        "'DataStream.assignTimestampsAndWatermarks(...)'?");
    }
  }

  @Override
  public Trigger<NewsFeed, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment streamExecutionEnvironment) {
    return NewsCountEventTimeOutTrigger.of(3);
  }

  @Override
  public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
    return new TimeWindow.Serializer();
  }

  public static SlidingNewsFlinkEventTimeWindow of(Time size, Time slide) {
    return new SlidingNewsFlinkEventTimeWindow(size.toMilliseconds(), slide.toMilliseconds());
  }

  @Override
  public boolean isEventTime() {
    return true;
  }
}