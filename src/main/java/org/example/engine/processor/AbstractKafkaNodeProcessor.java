package org.example.engine.processor;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.example.engine.executor.NodeRuntimeContext;

//public abstract class AbstractKafkaNodeProcessor implements NodeProcessor {
//
//    protected NodeRuntimeContext context;
//    protected final KafkaConsumer<String, String> consumer;
//    protected final KafkaProducer<String, String> producer;
//
//    protected AbstractKafkaNodeProcessor(
//            KafkaConsumer<String, String> consumer,
//            KafkaProducer<String, String> producer
//    ) {
//        this.consumer = consumer;
//        this.producer = producer;
//    }
//
//    @Override
//    public void init(NodeRuntimeContext context) {
//        this.context = context;
//        consumer.subscribe(context.getInputTopics());
//    }
//
//    @Override
//    public void start() {
//        new Thread(this::pollLoop, "node-" + context.getNodeId()).start();
//    }
//
//    protected void pollLoop() {
//        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
//            for (ConsumerRecord<String, String> record : records) {
//                process(record.value());
//            }
//        }
//    }
//
//    protected abstract void process(String message);
//}

