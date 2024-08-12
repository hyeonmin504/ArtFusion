package _2.ArtFusion.service.util.singleton;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SingletonQueueUtilForDallE2<T> {

    private final BlockingQueue<T> queue;

    // 일단 큐를 100개 까지만 생성 제한
    private SingletonQueueUtilForDallE2() {
        this.queue = new LinkedBlockingQueue<>(100);
    }

    // JVM의 클래스 초기화 과정에서 원자성을 보장하는 원리를 이용한 방식.싱글톤 패턴을 위한 Lazy Holder 클래스
    private static class InstanceHolder {
        private static final SingletonQueueUtilForDallE2<?> instance = new SingletonQueueUtilForDallE2<>();
    }

    //인스턴스 요청
    @SuppressWarnings("unchecked")
    public static <T> SingletonQueueUtilForDallE2<T> getInstance() {
        return (SingletonQueueUtilForDallE2<T>) SingletonQueueUtilForDallE2.InstanceHolder.instance;
    }

    public void enqueue(T object) {
        queue.add(object);
    }

    public T dequeue() throws InterruptedException {
        return queue.take();
    }

    public boolean getIsEmpty() {
        return queue.isEmpty();
    }

    public Integer getSize() {
        return queue.size();
    }
}
